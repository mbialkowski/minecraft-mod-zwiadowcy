package pl.zwiadowcy.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import pl.zwiadowcy.item.GrappleItem;
import pl.zwiadowcy.registry.ModEntities;
import pl.zwiadowcy.registry.ModItems;

/**
 * Hak linki z kotwiczką. Leci do 20 bloków; gdy zaczepi o krawędź (nad trafionym blokiem jest wolne miejsce),
 * podciąga gracza na górę ({@link GrappleItem#pull}). Na blokach z tagiem "zwiadowcy:bez_kotwiczki" nie trzyma.
 */
public class GrappleHookEntity extends ThrowableItemProjectile {
    private Vec3 start;

    public GrappleHookEntity(EntityType<? extends GrappleHookEntity> type, Level level) {
        super(type, level);
    }

    public GrappleHookEntity(Level level, LivingEntity thrower) {
        super(ModEntities.KOTWICZKA.get(), thrower, level);
        start = thrower.position();
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.LINKA_Z_KOTWICZKA.get();
    }

    @Override
    protected float getGravity() {
        return 0.02f;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && start != null && position().distanceTo(start) > GrappleItem.RANGE + 2) discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (level().isClientSide) return;
        BlockPos hit = result.getBlockPos();
        // zaczep: górna krawędź - nad trafionym blokiem (albo blok wyżej) są dwa wolne miejsca na stanie
        BlockPos ledge = null;
        for (int up = 0; up <= 1 && ledge == null; up++) {
            BlockPos solid = hit.above(up);
            if (!level().getBlockState(solid).isSolid()) break;
            if (level().getBlockState(solid.above()).getCollisionShape(level(), solid.above()).isEmpty()
                    && level().getBlockState(solid.above(2)).getCollisionShape(level(), solid.above(2)).isEmpty()) {
                ledge = solid.above();
            }
        }
        boolean forbidden = level().getBlockState(hit).is(GrappleItem.NO_GRAPPLE);
        if (getOwner() instanceof ServerPlayer p && ledge != null && !forbidden && result.getDirection() != Direction.DOWN) {
            level().playSound(null, hit, SoundEvents.CHAIN_PLACE, SoundSource.PLAYERS, 1f, 1.2f);
            GrappleItem.pull(p, ledge);
        } else {
            level().playSound(null, hit, SoundEvents.CHAIN_BREAK, SoundSource.PLAYERS, 0.8f, 1.4f);
            if (getOwner() instanceof ServerPlayer p) GrappleItem.missed(p, forbidden);
        }
        discard();
    }
}
