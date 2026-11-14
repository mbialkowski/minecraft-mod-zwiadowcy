package pl.zwiadowcy.entity.projectile;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import pl.zwiadowcy.registry.ModEntities;
import pl.zwiadowcy.registry.ModItems;
import pl.zwiadowcy.stealth.SmokeClouds;

/** Rzucony garnek z dymem: kula gęstego dymu (promień 4) na 6 sekund. */
public class SmokePotEntity extends ThrowableItemProjectile {

    public SmokePotEntity(EntityType<? extends SmokePotEntity> type, Level level) {
        super(type, level);
    }

    public SmokePotEntity(Level level, LivingEntity thrower) {
        super(ModEntities.GARNEK_Z_DYMEM.get(), thrower, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.GARNEK_Z_DYMEM.get();
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (level().isClientSide) return;
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.DECORATED_POT_SHATTER, SoundSource.NEUTRAL, 1f, 0.8f);
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.FIRE_EXTINGUISH, SoundSource.NEUTRAL, 1f, 0.6f);
        if (level() instanceof ServerLevel sl) SmokeClouds.add(sl, position(), 4.0, 120);
        discard();
    }
}
