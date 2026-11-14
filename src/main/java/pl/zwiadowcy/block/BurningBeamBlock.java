package pl.zwiadowcy.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Płonąca belka mostu: świeci, dymi i parzy, ale ogień nie rozchodzi się poza most (skrypt spalenia
 * {@link pl.zwiadowcy.bridge.BridgeBurn} sam decyduje, która belka zapłonie i kiedy odpadnie).
 */
public class BurningBeamBlock extends RotatedPillarBlock {
    public BurningBeamBlock(Properties props) {
        super(props);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!level.isClientSide && entity instanceof LivingEntity le && !le.fireImmune()) {
            le.setSecondsOnFire(3);
        }
        super.stepOn(level, pos, state, entity);
    }

    /** Belki, które spadły do kanionu albo zostały po skrypcie, dopalają się i zwęglają. */
    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, net.minecraft.server.level.ServerLevel level, BlockPos pos, RandomSource random) {
        if (pl.zwiadowcy.bridge.BridgeBurn.burningNow() || random.nextInt(3) != 0) return;
        level.setBlock(pos, pl.zwiadowcy.registry.ModBlocks.ZWEGLONA_BELKA.get().defaultBlockState().setValue(AXIS, state.getValue(AXIS)), 2);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double x = pos.getX() + random.nextDouble(), y = pos.getY() + 1.0, z = pos.getZ() + random.nextDouble();
        level.addParticle(ParticleTypes.FLAME, x, y, z, 0, 0.03, 0);
        if (random.nextInt(3) == 0) level.addParticle(ParticleTypes.LARGE_SMOKE, x, y + 0.2, z, 0, 0.06, 0);
        if (random.nextInt(8) == 0) level.addParticle(ParticleTypes.LAVA, x, y, z, 0, 0, 0);
    }
}
