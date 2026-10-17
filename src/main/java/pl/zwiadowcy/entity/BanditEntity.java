package pl.zwiadowcy.entity;

import net.minecraft.ChatFormatting;
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
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/**
 * Bandyci z traktów Araluenu. Odmiany: bandyta (miecz/topór), łucznik i herszt (mini-boss).
 * Każdy dostaje losową twarz i strój (6 skórek) oraz losowe elementy skórzanej zbroi.
 */
public class BanditEntity extends Monster implements RangedAttackMob {

    public enum Variant { BANDYTA, LUCZNIK, HERSZT }

    public static final int SKINS = 6;
    private static final EntityDataAccessor<Integer> SKIN = SynchedEntityData.defineId(BanditEntity.class, EntityDataSerializers.INT);

    private final Variant variant;
    private boolean equipped;
    @Nullable private final ServerBossEvent bossEvent;

    public BanditEntity(EntityType<? extends BanditEntity> type, Level level, Variant variant) {
        super(type, level);
        this.variant = variant;
        this.xpReward = variant == Variant.HERSZT ? 35 : 8;
        this.bossEvent = variant == Variant.HERSZT
                ? (ServerBossEvent) new ServerBossEvent(Component.literal("Herszt bandytów").withStyle(ChatFormatting.GOLD),
                BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.PROGRESS)
                : null;
        if (variant == Variant.HERSZT) setPersistenceRequired();
    }

    public Variant variant() { return variant; }
    public int skin() { return entityData.get(SKIN); }

    public static AttributeSupplier.Builder attributes(Variant v) {
        AttributeSupplier.Builder b = Monster.createMonsterAttributes();
        return switch (v) {
            case BANDYTA -> b.add(Attributes.MAX_HEALTH, 24).add(Attributes.MOVEMENT_SPEED, 0.3)
                    .add(Attributes.ATTACK_DAMAGE, 4).add(Attributes.FOLLOW_RANGE, 28);
            case LUCZNIK -> b.add(Attributes.MAX_HEALTH, 20).add(Attributes.MOVEMENT_SPEED, 0.28)
                    .add(Attributes.ATTACK_DAMAGE, 3).add(Attributes.FOLLOW_RANGE, 32);
            case HERSZT -> b.add(Attributes.MAX_HEALTH, 60).add(Attributes.MOVEMENT_SPEED, 0.3)
                    .add(Attributes.ATTACK_DAMAGE, 7).add(Attributes.ARMOR, 4).add(Attributes.KNOCKBACK_RESISTANCE, 0.4)
                    .add(Attributes.FOLLOW_RANGE, 32);
        };
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(SKIN, 0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new RangedBowAttackGoal<BanditEntity>(this, 1.0, 25, 18f) {
            @Override public boolean canUse() { return variant == Variant.LUCZNIK && super.canUse(); }
        });
        goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2, false) {
            @Override public boolean canUse() { return (variant != Variant.LUCZNIK || !(getMainHandItem().getItem() instanceof BowItem)) && super.canUse(); }
        });
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10f));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    private void equip() {
        if (equipped) return;
        equipped = true;
        if (!level().isClientSide) entityData.set(SKIN, random.nextInt(SKINS));
        switch (variant) {
            case BANDYTA -> setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(switch (random.nextInt(4)) {
                case 0 -> Items.IRON_SWORD;
                case 1 -> Items.STONE_AXE;
                case 2 -> Items.IRON_AXE;
                default -> Items.STONE_SWORD;
            }));
            case LUCZNIK -> setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
            case HERSZT -> {
                setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
                setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
                setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
                setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.CHAINMAIL_HELMET));
            }
        }
        if (variant != Variant.HERSZT) {
            if (random.nextFloat() < 0.5f) setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.LEATHER_CHESTPLATE));
            if (random.nextFloat() < 0.3f) setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));
            if (random.nextFloat() < 0.3f) setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.LEATHER_LEGGINGS));
        }
        for (EquipmentSlot slot : EquipmentSlot.values()) setDropChance(slot, 0.04f);
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                        @Nullable SpawnGroupData data, @Nullable CompoundTag tag) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data, tag);
        equip();
        return result;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && !equipped && getMainHandItem().isEmpty()) equip();
        if (bossEvent != null && !level().isClientSide) bossEvent.setProgress(getHealth() / getMaxHealth());
    }

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        ItemStack arrowStack = new ItemStack(Items.ARROW);
        AbstractArrow arrow = ProjectileUtil.getMobArrow(this, arrowStack, power);
        double dx = target.getX() - getX();
        double dy = target.getY(0.3333) - arrow.getY();
        double dz = target.getZ() - getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        arrow.shoot(dx, dy + dist * 0.2, dz, 1.6f, 14 - level().getDifficulty().getId() * 4);
        playSound(SoundEvents.SKELETON_SHOOT, 1.0f, 1.0f / (getRandom().nextFloat() * 0.4f + 0.8f));
        level().addFreshEntity(arrow);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        if (bossEvent != null) bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        if (bossEvent != null) bossEvent.removePlayer(player);
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return variant != Variant.HERSZT && !getPersistentData().contains("zw_quest") && super.removeWhenFarAway(distance);
    }

    @Override protected boolean shouldDespawnInPeaceful() { return false; }
    @Override protected SoundEvent getAmbientSound() { return SoundEvents.PILLAGER_AMBIENT; }
    @Override protected SoundEvent getHurtSound(DamageSource src) { return SoundEvents.PILLAGER_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.PILLAGER_DEATH; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Skin", skin());
        tag.putBoolean("Equipped", equipped);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(SKIN, tag.getInt("Skin"));
        equipped = tag.getBoolean("Equipped");
    }
}
