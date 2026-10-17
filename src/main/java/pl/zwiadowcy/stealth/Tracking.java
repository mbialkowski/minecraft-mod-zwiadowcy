package pl.zwiadowcy.stealth;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import pl.zwiadowcy.network.QuestSyncPacket;
import pl.zwiadowcy.registry.ModBlocks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Efekt zestawu tropiciela: cząsteczki nad śladami i "ścieżka" w stronę celu śledzonej misji. */
public final class Tracking {
    private static final Map<UUID, Long> ACTIVE_UNTIL = new HashMap<>();
    private static final DustParticleOptions TRAIL = new DustParticleOptions(new Vector3f(0.55f, 0.38f, 0.18f), 1.2f);

    public static void start(ServerPlayer p, int ticks) {
        ACTIVE_UNTIL.put(p.getUUID(), p.level().getGameTime() + ticks);
    }

    public static void tick(ServerPlayer p) {
        Long until = ACTIVE_UNTIL.get(p.getUUID());
        if (until == null) return;
        if (p.level().getGameTime() > until) {
            ACTIVE_UNTIL.remove(p.getUUID());
            return;
        }
        if (p.tickCount % 10 != 0 || !(p.level() instanceof ServerLevel level)) return;

        BlockPos center = p.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-20, -6, -20), center.offset(20, 6, 20))) {
            if (level.getBlockState(pos).is(ModBlocks.SLADY.get())) {
                level.sendParticles(p, ParticleTypes.HAPPY_VILLAGER, true, pos.getX() + 0.5, pos.getY() + 0.3, pos.getZ() + 0.5,
                        2, 0.2, 0.1, 0.2, 0.0);
            }
        }

        QuestSyncPacket sync = QuestSyncPacket.build(p);
        if (sync.target() != null) {
            Vec3 from = p.position();
            Vec3 to = Vec3.atCenterOf(sync.target());
            Vec3 dir = new Vec3(to.x - from.x, 0, to.z - from.z);
            if (dir.lengthSqr() > 9) {
                dir = dir.normalize();
                for (int i = 2; i <= 12; i += 2) {
                    Vec3 pt = from.add(dir.scale(i));
                    level.sendParticles(p, TRAIL, true, pt.x, from.y + 0.2, pt.z, 1, 0.05, 0.02, 0.05, 0.0);
                }
            }
        }
    }

    public static void forget(ServerPlayer p) {
        ACTIVE_UNTIL.remove(p.getUUID());
    }

    private Tracking() {}
}
