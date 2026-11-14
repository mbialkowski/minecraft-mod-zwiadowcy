package pl.zwiadowcy.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import pl.zwiadowcy.entity.StoryNpc;
import pl.zwiadowcy.registry.ModEntities;
import pl.zwiadowcy.registry.ModItems;
import pl.zwiadowcy.stealth.Alertness;

/**
 * Rzucony kamyk: stuknięcie w promieniu 8 bloków ściąga czujnych wartowników, żeby sprawdzili to miejsce
 * (odciąganie uwagi w skradaniu 2.0). Kamień Evanlyn ogłusza trafionego wroga na sekundę.
 */
public class StoneEntity extends ThrowableItemProjectile {

    public StoneEntity(EntityType<? extends StoneEntity> type, Level level) {
        super(type, level);
    }

    public StoneEntity(Level level, LivingEntity thrower) {
        super(ModEntities.KAMYK.get(), thrower, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.KAMYK.get();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (result.getEntity() instanceof LivingEntity target && !(target instanceof StoryNpc)) {
            target.hurt(damageSources().thrown(this, getOwner()), 1f);
            if (getOwner() instanceof StoryNpc) {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 5, false, false));
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (level().isClientSide) return;
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.STONE_HIT, SoundSource.NEUTRAL, 1.4f, 1.2f);
        if (level() instanceof ServerLevel sl && !(getOwner() instanceof StoryNpc)) {
            Alertness.noise(sl, BlockPos.containing(position()), 8);
        }
        discard();
    }
}
