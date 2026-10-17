package pl.zwiadowcy.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import pl.zwiadowcy.entity.projectile.RangerArrowEntity;
import pl.zwiadowcy.entity.projectile.RangerArrowType;
import pl.zwiadowcy.quest.QuestActions;
import pl.zwiadowcy.quest.QuestEngine;
import pl.zwiadowcy.registry.ModEffects;

import java.util.EnumSet;

/**
 * Kalkar - bestia Morgaratha. Zwykła broń rani go słabo, ogień - bardzo mocno.
 * Fazy: tropienie → szarże (poniżej 66% HP) → szał (poniżej 33% HP).
 * Spojrzenie grozy paraliżuje tego, kto patrzy mu w oczy.
 */
public class KalkarEntity extends Monster {
    private static final EntityDataAccessor<Boolean> CHARGING = SynchedEntityData.defineId(KalkarEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> PHASE = SynchedEntityData.defineId(KalkarEntity.class, EntityDataSerializers.INT);

    private final ServerBossEvent bossEvent = (ServerBossEvent) new ServerBossEvent(
            Component.literal("Kalkar").withStyle(ChatFormatting.DARK_RED),
            BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_10).setDarkenScreen(true);

    private int chargeCooldown = 100;
    private int roarCooldown = 200;
    private boolean halfFired;
    private final java.util.Map<java.util.UUID, Integer> gaze = new java.util.HashMap<>();

    public KalkarEntity(EntityType<? extends KalkarEntity> type, Level level) {
        super(type, level);
        xpReward = 120;
        setPersistenceRequired();
        setMaxUpStep(1.25f);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 150.0)
                .add(Attributes.MOVEMENT_SPEED, 0.27)
                .add(Attributes.ATTACK_DAMAGE, 11.0)
                .add(Attributes.ATTACK_KNOCKBACK, 1.5)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.ARMOR, 6.0)
                .add(Attributes.FOLLOW_RANGE, 40.0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(CHARGING, false);
        entityData.define(PHASE, 1);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new ChargeGoal());
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.7));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 16f));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public boolean isCharging() { return entityData.get(CHARGING); }
    public int phase() { return entityData.get(PHASE); }

    // ------------------------------------------------------------------ obrażenia

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return super.hurt(source, amount);
        Entity direct = source.getDirectEntity();
        boolean fire = source.is(DamageTypeTags.IS_FIRE)
                || (direct instanceof AbstractArrow arrow && arrow.isOnFire())
                || (direct instanceof RangerArrowEntity ra && ra.arrowType() == RangerArrowType.PLONACA);
        if (fire) {
            amount *= 3.0f;
            if (!level().isClientSide && level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.FLAME, getX(), getY() + 1.5, getZ(), 8, 0.4, 0.8, 0.4, 0.02);
            }
        } else {
            amount *= 0.35f;
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        float extra = isCharging() ? 5f : 0f;
        boolean r = super.doHurtTarget(target);
        if (r && target instanceof LivingEntity le) {
            if (extra > 0) le.hurt(damageSources().mobAttack(this), extra);
            Vec3 push = le.position().subtract(position()).normalize().scale(isCharging() ? 1.6 : 0.6);
            le.push(push.x, 0.35, push.z);
        }
        playSound(SoundEvents.RAVAGER_ATTACK, 1.2f, 0.7f);
        return r;
    }

    // ------------------------------------------------------------------ logika bossa

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        float hp = getHealth() / getMaxHealth();
        bossEvent.setProgress(hp);
        int newPhase = hp > 0.66f ? 1 : hp > 0.33f ? 2 : 3;
        if (newPhase != phase()) {
            entityData.set(PHASE, newPhase);
            roar(newPhase == 3 ? "Kalkar wpada w szał!" : "Kalkar ryczy i szykuje się do szarży!");
            if (newPhase == 3) {
                var speed = getAttribute(Attributes.MOVEMENT_SPEED);
                if (speed != null) speed.setBaseValue(0.32);
            }
        }
        if (!halfFired && hp <= 0.5f) {
            halfFired = true;
            fireOwnerEvent("kalkar_polowa");
        }
        if (tickCount % 5 == 0) updateGaze();
        if (chargeCooldown > 0) chargeCooldown--;
        if (--roarCooldown <= 0 && phase() == 3) roar("Ryk Kalkara mrozi krew w żyłach!");
    }

    private void roar(String message) {
        roarCooldown = 300;
        playSound(SoundEvents.RAVAGER_ROAR, 2.0f, 0.6f);
        for (Player p : level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(12))) {
            p.addEffect(new MobEffectInstance(ModEffects.GROZA.get(), 80, 0));
            p.displayClientMessage(Component.literal(message).withStyle(ChatFormatting.DARK_RED), true);
        }
    }

    /** Spojrzenie grozy: gracz patrzący Kalkarowi w oczy zostaje spowolniony, a po chwili sparaliżowany. */
    private void updateGaze() {
        for (Player p : level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(20))) {
            if (p.isCreative() || p.isSpectator()) continue;
            int g = gaze.getOrDefault(p.getUUID(), 0);
            if (isLookingAtMe(p)) {
                g += 5;
                if (g == 20) {
                    p.addEffect(new MobEffectInstance(ModEffects.GROZA.get(), 60, 0));
                    p.displayClientMessage(Component.literal("Nie patrz mu w oczy! Spojrzenie Kalkara paraliżuje…").withStyle(ChatFormatting.RED), true);
                    playSound(SoundEvents.WARDEN_HEARTBEAT, 1.5f, 0.6f);
                } else if (g >= 45) {
                    p.addEffect(new MobEffectInstance(ModEffects.GROZA.get(), 40, 1));
                    p.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 80, 0));
                    g = 0;
                }
            } else {
                g = Math.max(0, g - 5);
            }
            gaze.put(p.getUUID(), g);
        }
    }

    private boolean isLookingAtMe(Player p) {
        Vec3 view = p.getViewVector(1f).normalize();
        Vec3 diff = new Vec3(getX() - p.getX(), getEyeY() - p.getEyeY(), getZ() - p.getZ());
        double d = diff.length();
        diff = diff.normalize();
        return view.dot(diff) > 1.0 - 0.3 / d && p.hasLineOfSight(this);
    }

    private void fireOwnerEvent(String event) {
        if (!getPersistentData().hasUUID(QuestActions.TAG_OWNER) || getServer() == null) return;
        ServerPlayer owner = getServer().getPlayerList().getPlayer(getPersistentData().getUUID(QuestActions.TAG_OWNER));
        if (owner != null) QuestEngine.fireEvent(owner, event, 1);
    }

    @Override
    public void die(DamageSource source) {
        if (!level().isClientSide && !halfFired) {
            halfFired = true;
            fireOwnerEvent("kalkar_polowa");
        }
        super.die(source);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossEvent.removePlayer(player);
    }

    @Override
    public void setCustomName(Component name) {
        super.setCustomName(name);
        if (name != null) bossEvent.setName(name.copy().withStyle(ChatFormatting.DARK_RED));
    }

    @Override protected boolean shouldDespawnInPeaceful() { return false; }
    @Override public boolean removeWhenFarAway(double distance) { return false; }
    @Override protected SoundEvent getAmbientSound() { return SoundEvents.RAVAGER_AMBIENT; }
    @Override protected SoundEvent getHurtSound(DamageSource src) { return SoundEvents.RAVAGER_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.RAVAGER_DEATH; }
    @Override public float getVoicePitch() { return 0.55f; }
    @Override protected float getSoundVolume() { return 1.6f; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("HalfFired", halfFired);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        halfFired = tag.getBoolean("HalfFired");
        if (hasCustomName()) bossEvent.setName(getDisplayName());
    }

    private class ChargeGoal extends Goal {
        private int ticks;

        ChargeGoal() { setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }

        @Override
        public boolean canUse() {
            LivingEntity t = getTarget();
            if (phase() < 2 || t == null || !t.isAlive() || chargeCooldown > 0) return false;
            double d = distanceToSqr(t);
            return d > 36 && d < 400 && getSensing().hasLineOfSight(t);
        }

        @Override
        public void start() {
            ticks = 35;
            entityData.set(CHARGING, true);
            level().playSound(null, blockPosition(), SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 2f, 0.8f);
        }

        @Override
        public boolean canContinueToUse() { return ticks > 0 && getTarget() != null && getTarget().isAlive(); }

        @Override
        public void tick() {
            ticks--;
            LivingEntity t = getTarget();
            if (t == null) return;
            getLookControl().setLookAt(t, 30f, 30f);
            getNavigation().moveTo(t, phase() == 3 ? 2.6 : 2.2);
            if (distanceToSqr(t) < 3.2 * 3.2) {
                doHurtTarget(t);
                ticks = 0;
            }
        }

        @Override
        public void stop() {
            entityData.set(CHARGING, false);
            chargeCooldown = phase() == 3 ? 100 : 160;
        }
    }
}
