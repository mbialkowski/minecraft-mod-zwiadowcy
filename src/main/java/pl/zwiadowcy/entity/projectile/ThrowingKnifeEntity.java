package pl.zwiadowcy.entity.projectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import pl.zwiadowcy.registry.ModEntities;
import pl.zwiadowcy.registry.ModItems;

import javax.annotation.Nullable;

/** Rzucony nóż: zadaje obrażenia raz, potem spada i można go podnieść. */
public class ThrowingKnifeEntity extends AbstractArrow {
    private ItemStack knife = ItemStack.EMPTY;
    private boolean dealtDamage;

    public ThrowingKnifeEntity(EntityType<? extends ThrowingKnifeEntity> type, Level level) {
        super(type, level);
    }

    public ThrowingKnifeEntity(Level level, LivingEntity shooter, ItemStack stack) {
        super(ModEntities.NOZ.get(), shooter, level);
        this.knife = stack.copy();
    }

    @Override
    @Nullable
    protected EntityHitResult findHitEntity(Vec3 from, Vec3 to) {
        return dealtDamage ? null : super.findHitEntity(from, to);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();
        Entity owner = getOwner();
        float damage = 6.0f;
        DamageSource source = damageSources().thrown(this, owner == null ? this : owner);
        dealtDamage = true;
        if (target.hurt(source, damage) && target instanceof LivingEntity living && owner instanceof LivingEntity lo) {
            doPostHurtEffects(living);
            living.setLastHurtByMob(lo);
        }
        setDeltaMovement(getDeltaMovement().multiply(-0.05, -0.1, -0.05));
        playSound(SoundEvents.TRIDENT_HIT, 0.8f, 1.5f);
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.TRIDENT_HIT_GROUND;
    }

    @Override
    protected ItemStack getPickupItem() {
        return knife.isEmpty() ? new ItemStack(ModItems.NOZ_DO_RZUCANIA.get()) : knife.copy();
    }

    public boolean isStuck() {
        return inGround;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("Knife", getPickupItem().save(new CompoundTag()));
        tag.putBoolean("DealtDamage", dealtDamage);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Knife")) knife = ItemStack.of(tag.getCompound("Knife"));
        dealtDamage = tag.getBoolean("DealtDamage");
    }
}
