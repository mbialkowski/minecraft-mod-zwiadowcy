package pl.zwiadowcy.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Ślady na ziemi - subtelne; zestaw tropiciela podświetla je cząsteczkami. */
public class TracksBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 0.5, 16);

    public TracksBlock(Properties props) {
        super(props);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }
}
