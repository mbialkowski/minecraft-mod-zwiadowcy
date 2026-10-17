package pl.zwiadowcy.entity.projectile;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import pl.zwiadowcy.registry.ModEffects;
import pl.zwiadowcy.registry.ModEntities;
import pl.zwiadowcy.registry.ModItems;

/** Rzucona flasza oleju: rozbija się i oblewa wszystko w promieniu 3,5 bloku. */
public class OilFlaskEntity extends ThrowableItemProjectile {

    public OilFlaskEntity(EntityType<? extends OilFlaskEntity> type, Level level) {
        super(type, level);
    }

    public OilFlaskEntity(Level level, LivingEntity shooter) {
        super(ModEntities.FLASZA.get(), shooter, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.FLASZA_OLEJU.get();
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (level().isClientSide) return;
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.GLASS_BREAK, SoundSource.NEUTRAL, 1f, 0.8f);
        if (level() instanceof ServerLevel sl) {
            sl.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, getItem()), getX(), getY(), getZ(), 12, 0.2, 0.2, 0.2, 0.08);
            sl.sendParticles(ParticleTypes.FALLING_HONEY, getX(), getY() + 0.5, getZ(), 30, 1.5, 0.5, 1.5, 0.0);
        }
        for (LivingEntity e : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(3.5))) {
            e.addEffect(new MobEffectInstance(ModEffects.OBLANY_OLEJEM.get(), 400, 0));
            if (e.isOnFire()) e.setSecondsOnFire(10);
        }
        discard();
    }
}
