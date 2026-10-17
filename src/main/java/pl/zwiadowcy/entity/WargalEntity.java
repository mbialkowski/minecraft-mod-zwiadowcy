package pl.zwiadowcy.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import pl.zwiadowcy.entity.projectile.JavelinEntity;
import pl.zwiadowcy.registry.ModItems;

import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * Wargal - półzwierzęcy żołnierz Morgaratha. Odmiany: zwiadowca (szybki), wojownik, oszczepnik (rzuca oszczepami)
 * i herszt (mini-boss, wyciem wzmacnia watahę). Każda odmiana może pełnić wartę (rozgląda się, nie chodzi).
 */
public class WargalEntity extends Monster implements RangedAttackMob {

    public enum Variant { WOJOWNIK, ZWIADOWCA, OSZCZEPNIK, HERSZT }

    private static final EntityDataAccessor<Boolean> SENTRY = SynchedEntityData.defineId(WargalEntity.class, EntityDataSerializers.BOOLEAN);

    private final Variant variant;
    private float baseYaw;
    @Nullable private BlockPos distraction;
    private int distractionTicks;
    private int howlCooldown = 100;
    @Nullable private final ServerBossEvent bossEvent;

    public WargalEntity(EntityType<? extends WargalEntity> type, Level level) {
        this(type, level, Variant.WOJOWNIK);
    }

    public WargalEntity(EntityType<? extends WargalEntity> type, Level level, Variant variant) {
        super(type, level);
        this.variant = variant;
        this.xpReward = switch (variant) {
            case ZWIADOWCA -> 6;
            case WOJOWNIK -> 10;
            case OSZCZEPNIK -> 10;
            case HERSZT -> 40;
        };
        this.bossEvent = variant == Variant.HERSZT
                ? (ServerBossEvent) new ServerBossEvent(Component.literal("Herszt Wargalów").withStyle(ChatFormatting.DARK_RED),
                BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS)
                : null;
        if (variant == Variant.HERSZT) setPersistenceRequired();
        equipFor(variant);
    }

    public Variant variant() { return variant; }

    public static AttributeSupplier.Builder createAttributes() {
        return attributes(Variant.WOJOWNIK);
    }

    public static AttributeSupplier.Builder attributes(Variant v) {
        AttributeSupplier.Builder b = Monster.createMonsterAttributes();
        return switch (v) {
            case ZWIADOWCA -> b.add(Attributes.MAX_HEALTH, 16).add(Attributes.MOVEMENT_SPEED, 0.36)
                    .add(Attributes.ATTACK_DAMAGE, 3).add(Attributes.ARMOR, 1).add(Attributes.FOLLOW_RANGE, 32);
            case WOJOWNIK -> b.add(Attributes.MAX_HEALTH, 26).add(Attributes.MOVEMENT_SPEED, 0.3)
                    .add(Attributes.ATTACK_DAMAGE, 5).add(Attributes.ARMOR, 3).add(Attributes.FOLLOW_RANGE, 24);
            case OSZCZEPNIK -> b.add(Attributes.MAX_HEALTH, 22).add(Attributes.MOVEMENT_SPEED, 0.28)
                    .add(Attributes.ATTACK_DAMAGE, 4).add(Attributes.ARMOR, 2).add(Attributes.FOLLOW_RANGE, 28);
            case HERSZT -> b.add(Attributes.MAX_HEALTH, 70).add(Attributes.MOVEMENT_SPEED, 0.3)
                    .add(Attributes.ATTACK_DAMAGE, 10).add(Attributes.ARMOR, 7).add(Attributes.KNOCKBACK_RESISTANCE, 0.6)
                    .add(Attributes.FOLLOW_RANGE, 32);
        };
    }

    private void equipFor(Variant v) {
        if (v == Variant.OSZCZEPNIK) setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.OSZCZEP.get()));
        if (v == Variant.HERSZT) setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.TOPOR_HERSZTA.get()));
        setDropChance(EquipmentSlot.MAINHAND, 0f);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(SENTRY, false);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        // odmiana nie jest jeszcze znana w konstruktorze Mob (registerGoals woła się wcześniej), więc cele sprawdzają ją same
        goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0, 35, 50, 16f) {
            @Override public boolean canUse() { return variant == Variant.OSZCZEPNIK && super.canUse() && getTarget() != null && distanceToSqr(getTarget()) > 9; }
            @Override public boolean canContinueToUse() { return variant == Variant.OSZCZEPNIK && super.canContinueToUse() && getTarget() != null && distanceToSqr(getTarget()) > 9; }
        });
        goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2, false));
        goalSelector.addGoal(4, new DistractGoal());
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8) {
            @Override public boolean canUse() { return !isSentry() && super.canUse(); }
        });
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10f) {
            @Override public boolean canUse() { return !isSentry() && super.canUse(); }
        });
        goalSelector.addGoal(7, new RandomLookAroundGoal(this) {
            @Override public boolean canUse() { return !isSentry() && super.canUse(); }
        });
        targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public boolean isSentry() { return entityData.get(SENTRY); }

    public void setSentry(boolean sentry, float yaw) {
        entityData.set(SENTRY, sentry);
        baseYaw = yaw;
    }

    /** Hałas (strzała gwiżdżąca) - wargal idzie sprawdzić, co się stało. */
    public void distract(BlockPos pos) {
        if (getTarget() != null && getTarget().distanceTo(this) < 6) return;
        setTarget(null);
        distraction = pos;
        distractionTicks = 120;
        baseYaw = (float) Math.toDegrees(Math.atan2(pos.getZ() - getZ(), pos.getX() - getX())) - 90f;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        JavelinEntity javelin = new JavelinEntity(level(), this, new ItemStack(ModItems.OSZCZEP.get()));
        double dx = target.getX() - getX();
        double dy = target.getY(0.4) - javelin.getY();
        double dz = target.getZ() - getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        javelin.shoot(dx, dy + dist * 0.18, dz, 1.5f, 6f);
        javelin.pickup = net.minecraft.world.entity.projectile.AbstractArrow.Pickup.DISALLOWED;
        swing(net.minecraft.world.InteractionHand.MAIN_HAND);
        playSound(SoundEvents.TRIDENT_THROW, 1f, 0.7f);
        level().addFreshEntity(javelin);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;
        if (distractionTicks > 0 && --distractionTicks == 0) distraction = null;
        if (isSentry() && getTarget() == null && distraction == null) {
            float yaw = baseYaw + 60f * Mth.sin(tickCount / 40f);
            setYRot(yaw);
            yBodyRot = yaw;
            yHeadRot = yaw;
        }
        if (variant == Variant.HERSZT) tickChieftain();
    }

    /** Herszt: wycie wzmacnia wszystkich wargalów w pobliżu. */
    private void tickChieftain() {
        if (bossEvent != null) {
            bossEvent.setProgress(getHealth() / getMaxHealth());
            BossBarRange.update(this, bossEvent, 24);
        }
        if (howlCooldown > 0) {
            howlCooldown--;
            return;
        }
        if (getTarget() == null) return;
        howlCooldown = 400;
        playSound(SoundEvents.WOLF_HOWL, 2.5f, 0.5f);
        for (WargalEntity w : level().getEntitiesOfClass(WargalEntity.class, getBoundingBox().inflate(16))) {
            w.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 0));
            w.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 0));
            if (w != this && w.getTarget() == null) w.setTarget(getTarget());
        }
        for (Player p : level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(20))) {
            p.displayClientMessage(Component.literal("Herszt wyje - wataha wpada w szał!").withStyle(ChatFormatting.DARK_RED), true);
        }
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                        @Nullable SpawnGroupData data, @Nullable CompoundTag tag) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data, tag);
        equipFor(variant);
        return result;
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        if (bossEvent != null) bossEvent.removePlayer(player);
    }

    @Override
    protected boolean shouldDespawnInPeaceful() { return false; }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return !getPersistentData().contains("zw_quest") && variant != Variant.HERSZT && super.removeWhenFarAway(distance);
    }

    @Override protected SoundEvent getAmbientSound() { return variant == Variant.HERSZT ? SoundEvents.WOLF_GROWL : SoundEvents.WOLF_GROWL; }
    @Override protected SoundEvent getHurtSound(DamageSource src) { return SoundEvents.WOLF_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.WOLF_DEATH; }

    @Override
    public float getVoicePitch() {
        float base = switch (variant) {
            case ZWIADOWCA -> 0.8f;
            case HERSZT -> 0.45f;
            default -> 0.6f;
        };
        return base + (random.nextFloat() - 0.5f) * 0.1f;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Sentry", isSentry());
        tag.putFloat("BaseYaw", baseYaw);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setSentry(tag.getBoolean("Sentry"), tag.getFloat("BaseYaw"));
        if (bossEvent != null && hasCustomName()) bossEvent.setName(getDisplayName());
    }

    private class DistractGoal extends Goal {
        DistractGoal() { setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }

        @Override
        public boolean canUse() { return distraction != null && getTarget() == null; }

        @Override
        public void start() {
            if (distraction != null) getNavigation().moveTo(distraction.getX() + 0.5, distraction.getY(), distraction.getZ() + 0.5, 1.0);
        }

        @Override
        public boolean canContinueToUse() { return distraction != null && getTarget() == null && !getNavigation().isDone(); }

        @Override
        public void tick() {
            if (distraction != null) getLookControl().setLookAt(distraction.getX() + 0.5, distraction.getY() + 1, distraction.getZ() + 0.5);
        }
    }
}
