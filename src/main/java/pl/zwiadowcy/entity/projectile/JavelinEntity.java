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

/** Rzucony oszczep (Wargalów albo gracza): ciężki, zadaje obrażenia raz, potem można go podnieść. */
public class JavelinEntity extends AbstractArrow {
    private ItemStack javelin = ItemStack.EMPTY;
    private boolean dealtDamage;

    public JavelinEntity(EntityType<? extends JavelinEntity> type, Level level) {
        super(type, level);
    }

    public JavelinEntity(Level level, LivingEntity shooter, ItemStack stack) {
        super(ModEntities.OSZCZEP.get(), shooter, level);
        this.javelin = stack.copy();
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
        DamageSource source = damageSources().thrown(this, owner == null ? this : owner);
        dealtDamage = true;
        if (target.hurt(source, 7.0f) && target instanceof LivingEntity living) {
            doPostHurtEffects(living);
            Vec3 push = getDeltaMovement().normalize().scale(0.4);
            living.push(push.x, 0.1, push.z);
        }
        setDeltaMovement(getDeltaMovement().multiply(-0.05, -0.1, -0.05));
        playSound(SoundEvents.TRIDENT_HIT, 1f, 0.8f);
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.TRIDENT_HIT_GROUND;
    }

    @Override
    protected ItemStack getPickupItem() {
        return javelin.isEmpty() ? new ItemStack(ModItems.OSZCZEP.get()) : javelin.copy();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("Javelin", getPickupItem().save(new CompoundTag()));
        tag.putBoolean("DealtDamage", dealtDamage);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Javelin")) javelin = ItemStack.of(tag.getCompound("Javelin"));
        dealtDamage = tag.getBoolean("DealtDamage");
    }
}
