package pl.zwiadowcy.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pl.zwiadowcy.Zwiadowcy;
import pl.zwiadowcy.registry.ModBlocks;

/**
 * Jednorazowe poprawki lokacji postawionych przez starszą wersję moda (przy starcie świata):
 * 1 - zapalone ogniska w kominkach, pod kotłami i w koksownikach zamieniamy na paleniska (bez gęstego dymu).
 * Ognisko na szczycie komina (kolumna bruku pod spodem) zostaje - jego dym jest widoczny z daleka.
 */
@Mod.EventBusSubscriber(modid = Zwiadowcy.MODID)
public final class LocationUpgrade {
    private static final int VERSION = 1;

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        ServerLevel level = Araluen.level(server);
        if (level == null) return;
        LocationData data = LocationData.get(server);
        if (data.upgrade() >= VERSION) return;
        long start = System.currentTimeMillis();
        int replaced = 0;
        for (var e : data.allPlaced().entrySet()) {
            Vec3i size = level.getStructureManager().get(Zwiadowcy.id(RouteStop.templateOf(e.getKey()))).map(StructureTemplate::getSize).orElse(null);
            if (size == null) continue;
            BlockPos o = e.getValue().origin;
            for (BlockPos pos : BlockPos.betweenClosed(o, o.offset(size.getX() - 1, size.getY() - 1, size.getZ() - 1))) {
                BlockState s = level.getBlockState(pos);
                Block hearth = s.is(Blocks.CAMPFIRE) ? ModBlocks.PALENISKO.get() : s.is(Blocks.SOUL_CAMPFIRE) ? ModBlocks.PALENISKO_DUSZ.get() : null;
                if (hearth == null || !s.getValue(CampfireBlock.LIT) || chimneyTop(level, pos)) continue;
                level.setBlock(pos.immutable(), hearth.defaultBlockState()
                        .setValue(HorizontalDirectionalBlock.FACING, s.getValue(CampfireBlock.FACING)), Block.UPDATE_ALL);
                replaced++;
            }
        }
        data.setUpgrade(VERSION);
        Zwiadowcy.LOGGER.info("Lokacje: {} ognisk zamienionych na paleniska ({} ms)", replaced, System.currentTimeMillis() - start);
    }

    private static boolean chimneyTop(ServerLevel level, BlockPos pos) {
        for (int i = 1; i <= 3; i++) if (!level.getBlockState(pos.below(i)).is(Blocks.COBBLESTONE)) return false;
        return true;
    }

    private LocationUpgrade() {}
}
