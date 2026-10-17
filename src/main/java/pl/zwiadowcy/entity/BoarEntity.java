package pl.zwiadowcy.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * Dzik - zwykle spokojny, ale zraniony (lub rozwścieczony fabularnie) szarżuje na wroga.
 */
public class BoarEntity extends Animal {
    private static final EntityDataAccessor<Boolean> ENRAGED = SynchedEntityData.defineId(BoarEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> CHARGING = SynchedEntityData.defineId(BoarEntity.class, EntityDataSerializers.BOOLEAN);

    private int chargeCooldown = 40;

    public BoarEntity(EntityType<? extends BoarEntity> type, Level level) {
        super(type, level);
        xpReward = 8;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 28.0)
                .add(Attributes.MOVEMENT_SPEED, 0.27)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.ATTACK_KNOCKBACK, 1.2)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6)
                .add(Attributes.ARMOR, 2.0)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(ENRAGED, false);
        entityData.define(CHARGING, false);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new ChargeGoal());
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.3, true));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.9));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8f));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, p -> isEnraged()));
    }

    public boolean isEnraged() { return entityData.get(ENRAGED); }
    public void setEnraged(boolean v) { entityData.set(ENRAGED, v); }
    public boolean isCharging() { return entityData.get(CHARGING); }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean r = super.hurt(source, amount);
        if (r && !level().isClientSide && source.getEntity() instanceof LivingEntity attacker) {
            setEnraged(true);
            setTarget(attacker);
        }
        return r;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean r = super.doHurtTarget(target);
        if (r && target instanceof LivingEntity le) {
            Vec3 push = le.position().subtract(position()).normalize().scale(isCharging() ? 1.1 : 0.5);
            le.push(push.x, isCharging() ? 0.45 : 0.25, push.z);
        }
        playSound(SoundEvents.HOGLIN_ATTACK, 1f, 1.25f);
        return r;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && chargeCooldown > 0) chargeCooldown--;
    }

    @Override
    @Nullable
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) { return null; }

    @Override
    public boolean isFood(ItemStack stack) { return false; }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return !getPersistentData().contains("zw_quest") && super.removeWhenFarAway(distance);
    }

    @Override protected SoundEvent getAmbientSound() { return SoundEvents.HOGLIN_AMBIENT; }
    @Override protected SoundEvent getHurtSound(DamageSource src) { return SoundEvents.HOGLIN_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.HOGLIN_DEATH; }
    @Override public float getVoicePitch() { return 1.25f + (random.nextFloat() - 0.5f) * 0.2f; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Enraged", isEnraged());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setEnraged(tag.getBoolean("Enraged"));
    }

    /** Szarża: krótki, bardzo szybki bieg prosto na cel. */
    private class ChargeGoal extends Goal {
        private int ticks;

        ChargeGoal() { setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }

        @Override
        public boolean canUse() {
            LivingEntity t = getTarget();
            if (t == null || !t.isAlive() || chargeCooldown > 0) return false;
            double d = distanceToSqr(t);
            return d > 16 && d < 196 && getSensing().hasLineOfSight(t);
        }

        @Override
        public void start() {
            ticks = 30;
            entityData.set(CHARGING, true);
            playSound(SoundEvents.HOGLIN_ANGRY, 1.2f, 1.2f);
        }

        @Override
        public boolean canContinueToUse() {
            return ticks > 0 && getTarget() != null && getTarget().isAlive();
        }

        @Override
        public void tick() {
            ticks--;
            LivingEntity t = getTarget();
            if (t == null) return;
            getLookControl().setLookAt(t, 30f, 30f);
            getNavigation().moveTo(t, 2.1);
            if (distanceToSqr(t) < 2.5 * 2.5) {
                doHurtTarget(t);
                ticks = 0;
            }
        }

        @Override
        public void stop() {
            entityData.set(CHARGING, false);
            chargeCooldown = 100 + random.nextInt(60);
        }
    }
}
