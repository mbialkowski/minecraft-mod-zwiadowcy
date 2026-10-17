package pl.zwiadowcy.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pl.zwiadowcy.Zwiadowcy;
import pl.zwiadowcy.entity.StoryNpc;
import pl.zwiadowcy.entity.TugEntity;

import java.util.*;

/**
 * Przeniesienie lokacji komendą: postacie, koń i manekin ze starego miejsca trafiają w odpowiadające miejsca nowego.
 * Byty w niezaładowanych chunkach wczytują się z opóźnieniem, dlatego stare chunki są przez chwilę wymuszone,
 * a przesuwanie powtarza się przez kilkanaście sekund.
 */
@Mod.EventBusSubscriber(modid = Zwiadowcy.MODID)
public final class Relocation {
    private static final int DURATION = 300;

    private record Pending(ServerLevel level, AABB oldBox, AABB newBox, BlockPos shift, List<ChunkPos> chunks,
                           Set<UUID> done, int[] ticksLeft) {}

    private static final List<Pending> PENDING = new ArrayList<>();

    static void start(ServerLevel level, BlockPos oldOrigin, BlockPos newOrigin, Vec3i size) {
        AABB oldBox = box(oldOrigin, size), newBox = box(newOrigin, size);
        List<ChunkPos> chunks = new ArrayList<>();
        for (int cx = ((int) oldBox.minX) >> 4; cx <= ((int) oldBox.maxX) >> 4; cx++) {
            for (int cz = ((int) oldBox.minZ) >> 4; cz <= ((int) oldBox.maxZ) >> 4; cz++) {
                if (level.setChunkForced(cx, cz, true)) chunks.add(new ChunkPos(cx, cz));
            }
        }
        PENDING.add(new Pending(level, oldBox, newBox, newOrigin.subtract(oldOrigin), chunks, new HashSet<>(), new int[]{DURATION}));
    }

    private static AABB box(BlockPos origin, Vec3i size) {
        return new AABB(origin, origin.offset(size)).inflate(8);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || PENDING.isEmpty()) return;
        for (Iterator<Pending> it = PENDING.iterator(); it.hasNext(); ) {
            Pending p = it.next();
            if (p.ticksLeft[0] % 10 == 0) shiftEntities(p);
            if (--p.ticksLeft[0] <= 0) {
                for (ChunkPos c : p.chunks) p.level.setChunkForced(c.x, c.z, false);
                it.remove();
            }
        }
    }

    private static void shiftEntities(Pending p) {
        for (StoryNpc npc : p.level.getEntitiesOfClass(StoryNpc.class, p.oldBox)) {
            if (p.newBox.contains(npc.position()) || !p.done.add(npc.getUUID())) continue;
            npc.shiftHome(p.shift);
        }
        for (TugEntity tug : p.level.getEntitiesOfClass(TugEntity.class, p.oldBox)) {
            if (p.newBox.contains(tug.position()) || !p.done.add(tug.getUUID())) continue;
            BlockPos home = tug.homePos();
            if (home != null && p.oldBox.contains(home.getCenter())) tug.setHomePos(home.offset(p.shift));
            tug.teleportTo(tug.getX() + p.shift.getX(), tug.getY() + p.shift.getY(), tug.getZ() + p.shift.getZ());
        }
        for (ArmorStand stand : p.level.getEntitiesOfClass(ArmorStand.class, p.oldBox)) {
            if (stand.getTags().contains("zwiadowcy_manekin") && !p.newBox.contains(stand.position())) stand.discard();
        }
    }

    private Relocation() {}
}
