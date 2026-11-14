package pl.zwiadowcy.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * Czarny rumak jeźdźców Morgaratha. Koń prowadzi jazdę (jeździec jest pasażerem i uderza kopią w przejeździe):
 * szarża zaczyna się od stanięcia dęba i 1,5 s rozbiegu, potem galop przez cel i nawrót. Strzały się od niego
 * odbijają - celem jest jeździec. Gdy jeździec spadnie, koń ucieka i znika.
 */
public class BlackSteedEntity extends AbstractHorse {
    private enum Phase { CIRCLE, REAR, CHARGE, FLEE }

    private Phase phase = Phase.CIRCLE;
    private int phaseTicks;
    @Nullable private Vec3 chargeTarget;
    /** Szarże zakończone unikiem gracza (Morgarath: po 3 unikach i 30% obrażeń koń się potyka). */
    private int dodgedCharges;

    public BlackSteedEntity(EntityType<? extends BlackSteedEntity> type, Level level) {
        super(type, level);
        setPersistenceRequired();
        setTamed(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractHorse.createBaseHorseAttributes()
                .add(Attributes.MAX_HEALTH, 60.0)
                .add(Attributes.MOVEMENT_SPEED, 0.36)
                .add(Attributes.FOLLOW_RANGE, 64.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new net.minecraft.world.entity.ai.goal.FloatGoal(this));
    }

    @Override
    public boolean isSaddled() {
        return true;
    }

    @Override
    @Nullable
    public LivingEntity getControllingPassenger() {
        return null;
    }

    public int dodgedCharges() { return dodgedCharges; }

    /** Jeździec spadł: koń ucieka i po 10 s znika. */
    public void flee() {
        phase = Phase.FLEE;
        phaseTicks = 200;
        ejectPassengers();
    }

    @Nullable
    private LivingEntity riderTarget() {
        return getFirstPassenger() instanceof Mob rider ? rider.getTarget() : null;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide || !(level() instanceof ServerLevel sl)) return;
        if (phase == Phase.FLEE) {
            if (--phaseTicks <= 0) {
                discard();
                return;
            }
            LivingEntity near = level().getNearestPlayer(this, 40);
            if (near != null && getNavigation().isDone()) {
                Vec3 away = position().subtract(near.position()).multiply(1, 0, 1).normalize().scale(30);
                getNavigation().moveTo(getX() + away.x, getY(), getZ() + away.z, 2.0);
            }
            return;
        }
        LivingEntity target = riderTarget();
        if (target == null || !target.isAlive()) {
            getNavigation().stop();
            return;
        }
        phaseTicks--;
        switch (phase) {
            case CIRCLE -> {
                // nawrót: odjeżdża na ok. 16 bloków i zawraca do szarży
                double d = distanceTo(target);
                if (d < 14 && getNavigation().isDone()) {
                    Vec3 away = position().subtract(target.position()).multiply(1, 0, 1).normalize().scale(18);
                    Vec3 side = new Vec3(-away.z, 0, away.x).normalize().scale(6);
                    getNavigation().moveTo(getX() + away.x + side.x, getY(), getZ() + away.z + side.z, 1.8);
                }
                if (phaseTicks <= 0 && d >= 10) {
                    phase = Phase.REAR;
                    phaseTicks = 30;
                    getNavigation().stop();
                    standIfPossible();
                    playSound(SoundEvents.HORSE_ANGRY, 2f, 0.7f);
                }
            }
            case REAR -> {
                // zapowiedź: dęba i parsknięcie, 1,5 s
                getLookControl().setLookAt(target, 30f, 30f);
                getNavigation().stop();
                if (phaseTicks % 6 == 0) sl.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 0.3, getZ(), 4, 0.4, 0.1, 0.4, 0.02);
                if (phaseTicks <= 0) {
                    Vec3 dir = target.position().subtract(position()).multiply(1, 0, 1).normalize();
                    chargeTarget = target.position().add(dir.scale(10));
                    phase = Phase.CHARGE;
                    phaseTicks = 50;
                    playSound(SoundEvents.HORSE_GALLOP, 2f, 0.8f);
                }
            }
            case CHARGE -> {
                if (chargeTarget != null) getNavigation().moveTo(chargeTarget.x, chargeTarget.y, chargeTarget.z, 2.8);
                if (phaseTicks <= 0 || (chargeTarget != null && position().distanceToSqr(chargeTarget) < 4)) {
                    if (getFirstPassenger() instanceof StoryFoe foe && !foe.hitThisCharge()) dodgedCharges++;
                    if (getFirstPassenger() instanceof StoryFoe foe) foe.resetCharge();
                    phase = Phase.CIRCLE;
                    phaseTicks = 40;
                }
            }
            default -> {}
        }
    }

    public boolean charging() {
        return phase == Phase.CHARGE;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // strzały i ciosy odbijają się od rumaka (celem jest jeździec)
        if (source.getEntity() instanceof Player) {
            playSound(SoundEvents.SHIELD_BLOCK, 0.6f, 1.4f);
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        return InteractionResult.PASS;
    }

    @Override
    public boolean canMate(Animal other) { return false; }

    @Override
    @Nullable
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) { return null; }

    @Override
    public boolean removeWhenFarAway(double distance) { return false; }

    @Override protected SoundEvent getAmbientSound() { return SoundEvents.HORSE_BREATHE; }
    @Override protected SoundEvent getHurtSound(DamageSource src) { return SoundEvents.HORSE_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.HORSE_DEATH; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Dodged", dodgedCharges);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        dodgedCharges = tag.getInt("Dodged");
    }
}
