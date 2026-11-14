package pl.zwiadowcy.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Koń towarzysza (Gilan, Horace, Evanlyn). Nie da się go dosiąść ani zabić; to koń prowadzi jazdę - jeździec
 * jest tylko pasażerem. Jedzie w szyku za graczem, a gdy towarzysz zsiada, czeka w miejscu.
 */
public class CompanionHorseEntity extends AbstractHorse {
    private static final EntityDataAccessor<String> COAT = SynchedEntityData.defineId(CompanionHorseEntity.class, EntityDataSerializers.STRING);

    @Nullable private UUID owner;
    private int formation;
    private boolean waiting;

    public CompanionHorseEntity(EntityType<? extends CompanionHorseEntity> type, Level level) {
        super(type, level);
        setPersistenceRequired();
        setTamed(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractHorse.createBaseHorseAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.34)
                .add(Attributes.JUMP_STRENGTH, 0.8)
                .add(Attributes.FOLLOW_RANGE, 64.0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(COAT, "brown");
    }

    /** Maść konia (nazwa tekstury konia vanilla: brown, chestnut, gray, white, black, creamy, darkbrown). */
    public String coat() { return entityData.get(COAT); }
    public void setCoat(String coat) { entityData.set(COAT, coat); }

    public void setup(ServerPlayer owner, int formation, String coat) {
        this.owner = owner.getUUID();
        this.formation = formation;
        this.waiting = false;
        setCoat(coat);
    }

    public void setWaiting(boolean waiting) {
        this.waiting = waiting;
        if (waiting) getNavigation().stop();
    }

    @Nullable
    private ServerPlayer ownerPlayer() {
        if (owner == null || getServer() == null) return null;
        ServerPlayer p = getServer().getPlayerList().getPlayer(owner);
        return p != null && p.level() == level() ? p : null;
    }

    @Override
    public boolean isSaddled() {
        return true;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new net.minecraft.world.entity.ai.goal.FloatGoal(this));
        goalSelector.addGoal(6, new net.minecraft.world.entity.ai.goal.LookAtPlayerGoal(this, Player.class, 8f));
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide || !(level() instanceof ServerLevel)) return;
        if (tickCount % 5 != 0 || waiting) return;
        ServerPlayer p = ownerPlayer();
        if (p == null || getPassengers().isEmpty()) return;
        // szyk: 4 bloki za graczem, rozstawieni na boki co 3 bloki
        Vec3 look = p.getLookAngle().multiply(1, 0, 1);
        if (look.lengthSqr() < 1e-4) look = new Vec3(0, 0, 1);
        look = look.normalize();
        Vec3 side = new Vec3(-look.z, 0, look.x);
        double lateral = (formation - 1) * 3.0;
        Vec3 target = p.position().subtract(look.scale(4 + formation)).add(side.scale(lateral));
        double d = position().distanceTo(target);
        if (d > 64 && !p.hasLineOfSight(this)) {
            teleportTo(target.x, p.getY(), target.z);
            getNavigation().stop();
        } else if (d > 2.5) {
            double speed = d > 24 ? 2.2 : d > 10 ? 1.8 : 1.2;
            getNavigation().moveTo(target.x, target.y, target.z, speed);
        } else {
            getNavigation().stop();
        }
    }

    @Override
    @Nullable
    public LivingEntity getControllingPassenger() {
        // koń sam prowadzi (jeździec-NPC nie steruje), więc nawigacja konia działa normalnie
        return null;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!level().isClientSide) {
            player.displayClientMessage(Component.literal("To koń twojego towarzysza.").withStyle(ChatFormatting.GRAY), true);
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!level().isClientSide && amount >= getHealth()) {
            setHealth(getMaxHealth() * 0.5f);
            ejectPassengers();
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean canMate(Animal other) { return false; }

    @Override
    @Nullable
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) { return null; }

    @Override
    public boolean removeWhenFarAway(double distance) { return false; }

    @Override protected SoundEvent getAmbientSound() { return SoundEvents.HORSE_AMBIENT; }
    @Override protected SoundEvent getHurtSound(DamageSource src) { return SoundEvents.HORSE_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.HORSE_DEATH; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (owner != null) tag.putUUID("Owner", owner);
        tag.putInt("Formation", formation);
        tag.putBoolean("Waiting", waiting);
        tag.putString("Coat", coat());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        formation = tag.getInt("Formation");
        waiting = tag.getBoolean("Waiting");
        if (tag.contains("Coat")) setCoat(tag.getString("Coat"));
    }
}
