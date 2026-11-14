package pl.zwiadowcy.world;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pl.zwiadowcy.Zwiadowcy;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Pochodnia w dłoni (Araluen): trzymana pochodnia albo latarnia oświetla okolicę gracza - na wysokości głowy stoi
 * niewidzialny blok światła, który idzie razem z nim (kopalnie i noce nie są czarne jak smoła).
 */
@Mod.EventBusSubscriber(modid = Zwiadowcy.MODID)
public final class HeldLight {
    private record Lit(ResourceKey<Level> dim, BlockPos pos) {}

    private static final Map<UUID, Lit> LIT = new HashMap<>();

    private static int lightOf(ItemStack s) {
        if (s.is(Items.TORCH) || s.is(Items.LANTERN)) return 14;
        if (s.is(Items.SOUL_TORCH) || s.is(Items.SOUL_LANTERN)) return 10;
        return 0;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer p) || p.tickCount % 2 != 0) return;
        int level = Math.max(lightOf(p.getMainHandItem()), lightOf(p.getOffhandItem()));
        Lit old = LIT.get(p.getUUID());
        BlockPos head = BlockPos.containing(p.getX(), p.getEyeY(), p.getZ());
        if (level <= 0 || !Araluen.isAraluen(p.level()) || p.isSpectator()) {
            if (old != null) clear(p, old);
            return;
        }
        if (old != null && old.pos.equals(head) && old.dim == p.level().dimension()) return;
        ServerLevel sl = p.serverLevel();
        BlockState at = sl.getBlockState(head);
        BlockPos target = head;
        if (!at.isAir() && !at.is(Blocks.LIGHT)) {
            // głowa w bloku (np. w tunelu o wysokości 2) - spróbuj na wysokości stóp
            target = p.blockPosition();
            BlockState feet = sl.getBlockState(target);
            if (!feet.isAir() && !feet.is(Blocks.LIGHT)) {
                if (old != null) clear(p, old);
                return;
            }
        }
        if (old != null && !old.pos.equals(target)) clear(p, old);
        sl.setBlock(target, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, level), 2);
        LIT.put(p.getUUID(), new Lit(p.level().dimension(), target));
    }

    private static void clear(ServerPlayer p, Lit lit) {
        LIT.remove(p.getUUID());
        if (p.getServer() == null) return;
        ServerLevel sl = p.getServer().getLevel(lit.dim);
        if (sl != null && sl.isLoaded(lit.pos) && sl.getBlockState(lit.pos).is(Blocks.LIGHT)) {
            sl.setBlock(lit.pos, Blocks.AIR.defaultBlockState(), 2);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer p) {
            Lit old = LIT.get(p.getUUID());
            if (old != null) clear(p, old);
        }
    }

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer p) {
            Lit old = LIT.get(p.getUUID());
            if (old != null) clear(p, old);
        }
    }

    private HeldLight() {}
}
