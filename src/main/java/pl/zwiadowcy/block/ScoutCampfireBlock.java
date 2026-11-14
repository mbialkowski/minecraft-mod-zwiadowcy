package pl.zwiadowcy.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import pl.zwiadowcy.network.ModNetwork;
import pl.zwiadowcy.network.OpenScreenPacket;
import pl.zwiadowcy.quest.QuestCapability;
import pl.zwiadowcy.travel.Campfires;

/** Ognisko zwiadowców: punkt szybkiej podróży. PPM odkrywa ognisko i otwiera Mapę Gilana. */
public class ScoutCampfireBlock extends HearthBlock {
    public ScoutCampfireBlock(Properties props) {
        super(props, false);
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer sp && sp.getServer() != null) {
            for (Campfires.Def def : Campfires.ALL) {
                BlockPos at = Campfires.pos(sp.getServer(), def);
                if (at != null && at.distSqr(pos) < 4 * 4 && pl.zwiadowcy.volume.Volumes.tom2Started(QuestCapability.get(sp))) {
                    Campfires.discover(sp, def);
                }
            }
            pl.zwiadowcy.quest.QuestEngine.sync(sp);
            ModNetwork.sendTo(sp, new OpenScreenPacket("mapa"));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
