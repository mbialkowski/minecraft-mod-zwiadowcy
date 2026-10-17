package pl.zwiadowcy.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import pl.zwiadowcy.entity.StoryNpc;
import pl.zwiadowcy.entity.WargalEntity;
import pl.zwiadowcy.entity.BoarEntity;
import pl.zwiadowcy.quest.QuestEngine;
import pl.zwiadowcy.registry.ModBlocks;
import pl.zwiadowcy.registry.ModEntities;
import pl.zwiadowcy.registry.ModItems;

/** Strzała specjalna zwiadowcy - zachowanie zależy od typu. */
public class RangerArrowEntity extends AbstractArrow {
    private static final EntityDataAccessor<Integer> TYPE = SynchedEntityData.defineId(RangerArrowEntity.class, EntityDataSerializers.INT);

    public RangerArrowEntity(EntityType<? extends RangerArrowEntity> type, Level level) {
        super(type, level);
    }

    public RangerArrowEntity(Level level, LivingEntity shooter, RangerArrowType arrowType) {
        super(ModEntities.STRZALA.get(), shooter, level);
        setArrowType(arrowType);
        switch (arrowType) {
            case PLONACA -> setSecondsOnFire(100);
            case SZEROKI_GROT -> setBaseDamage(getBaseDamage() * 1.7);
            case GWIZDZACA -> setBaseDamage(getBaseDamage() * 0.5);
            case Z_LINA -> setBaseDamage(getBaseDamage() * 0.3);
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(TYPE, 0);
    }

    public RangerArrowType arrowType() {
        return RangerArrowType.byOrdinal(entityData.get(TYPE));
    }

    public void setArrowType(RangerArrowType t) {
        entityData.set(TYPE, t.ordinal());
    }

    @Override
    public void tick() {
        super.tick();
        RangerArrowType t = arrowType();
        if (!inGround) {
            if (t == RangerArrowType.SZEROKI_GROT) {
                Vec3 v = getDeltaMovement();
                setDeltaMovement(v.x, v.y - 0.02, v.z);
            }
            if (t == RangerArrowType.GWIZDZACA) {
                if (level().isClientSide) {
                    level().addParticle(ParticleTypes.NOTE, getX(), getY(), getZ(), 0.0, 0.0, 0.0);
                } else if (tickCount % 4 == 0) {
                    level().playSound(null, getX(), getY(), getZ(), SoundEvents.NOTE_BLOCK_FLUTE.value(), SoundSource.PLAYERS,
                            0.8f, 1.2f + Math.min(tickCount, 40) * 0.02f);
                }
            }
            if (t == RangerArrowType.PLONACA && level().isClientSide && tickCount % 2 == 0) {
                level().addParticle(ParticleTypes.FLAME, getX(), getY(), getZ(), 0.0, 0.01, 0.0);
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!level().isClientSide && arrowType() == RangerArrowType.GWIZDZACA) whistleBurst(result.getLocation());
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (level().isClientSide) return;
        switch (arrowType()) {
            case GWIZDZACA -> whistleBurst(result.getLocation());
            case Z_LINA -> {
                if (placeRope(result)) discard();
            }
            default -> {}
        }
    }

    /** Gwizd: zwierzęta uciekają, wrogowie biegną sprawdzić hałas, strażnicy odwracają wzrok. */
    private void whistleBurst(Vec3 at) {
        level().playSound(null, at.x, at.y, at.z, SoundEvents.NOTE_BLOCK_FLUTE.value(), SoundSource.PLAYERS, 2.0f, 2.0f);
        LivingEntity owner = getOwner() instanceof LivingEntity le ? le : null;
        for (Mob mob : level().getEntitiesOfClass(Mob.class, getBoundingBox().inflate(16))) {
            if (mob instanceof Animal animal && !(mob instanceof BoarEntity b && b.isEnraged())) {
                if (owner != null) animal.setLastHurtByMob(owner);
            } else if (mob instanceof StoryNpc npc && npc.getMode() == StoryNpc.Mode.SENTRY) {
                npc.setBaseYaw((float) Math.toDegrees(Math.atan2(at.z - npc.getZ(), at.x - npc.getX())) - 90f);
            } else if (mob instanceof WargalEntity w) {
                w.distract(BlockPos.containing(at));
            } else if (mob instanceof PathfinderMob pm && !(mob instanceof StoryNpc) && mob.getTarget() == owner) {
                pm.setTarget(null);
                pm.getNavigation().moveTo(at.x, at.y, at.z, 1.0);
            }
        }
        if (owner instanceof ServerPlayer sp) QuestEngine.fireEvent(sp, "gwizd", 1);
    }

    /** Lina: zwisa w dół od miejsca trafienia (maks. 24 bloki). */
    private boolean placeRope(BlockHitResult hit) {
        Direction face = hit.getDirection();
        BlockPos start = hit.getBlockPos().relative(face);
        if (face == Direction.UP) return false;
        BlockState rope = ModBlocks.LINA.get().defaultBlockState();
        int placed = 0;
        BlockPos.MutableBlockPos pos = start.mutable();
        for (int i = 0; i < 24; i++) {
            BlockState cur = level().getBlockState(pos);
            if (!cur.isAir() && !cur.canBeReplaced()) break;
            level().setBlock(pos, rope, 3);
            placed++;
            pos.move(Direction.DOWN);
        }
        if (placed > 0) {
            level().playSound(null, start, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 1f, 0.8f);
            if (level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.CRIT, start.getX() + 0.5, start.getY() + 0.5, start.getZ() + 0.5, 6, 0.2, 0.2, 0.2, 0.05);
            }
        }
        return placed > 0;
    }

    @Override
    protected ItemStack getPickupItem() {
        return switch (arrowType()) {
            case PLONACA -> new ItemStack(ModItems.STRZALA_PLONACA.get());
            case SZEROKI_GROT -> new ItemStack(ModItems.STRZALA_SZEROKI_GROT.get());
            case GWIZDZACA -> new ItemStack(ModItems.STRZALA_GWIZDZACA.get());
            case Z_LINA -> new ItemStack(ModItems.STRZALA_Z_LINA.get());
        };
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("ArrowType", arrowType().ordinal());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setArrowType(RangerArrowType.byOrdinal(tag.getInt("ArrowType")));
    }
}
