package pl.zwiadowcy.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.entity.EntityTypeTest;
import pl.zwiadowcy.Zwiadowcy;
import pl.zwiadowcy.registry.ModEntities;
import pl.zwiadowcy.world.Araluen;
import pl.zwiadowcy.world.LocationData;
import pl.zwiadowcy.world.Locations;
import pl.zwiadowcy.world.StoryLocation;

import javax.annotation.Nullable;
import java.util.List;

/** Tworzenie, przenoszenie i odnajdywanie postaci fabularnych. */
public final class NpcService {

    /** Znacznik "npc:id" w strukturze. Postacie unikalne pojawiają się tylko w swojej lokacji startowej. */
    public static void spawnFromMarker(ServerLevel level, LocationData data, StoryLocation loc, String npcId, BlockPos pos, float yaw) {
        NpcDefs.NpcDef def = NpcDefs.get(npcId);
        if (def == null) {
            Zwiadowcy.LOGGER.warn("Nieznany NPC w znaczniku: {}", npcId);
            return;
        }
        if (def.unique()) {
            if (!loc.id.equals(def.initialLocation()) || data.wasSpawned(npcId)) return;
            data.markSpawned(npcId);
            spawn(level, npcId, pos, data.generation(npcId), yaw, null);
        } else {
            spawn(level, npcId, pos, -1, yaw, null);
        }
    }

    @Nullable
    public static StoryNpc spawn(ServerLevel level, String npcId, BlockPos pos, int generation, float yaw, @Nullable StoryNpc.Mode mode) {
        StoryNpc npc = ModEntities.NPC.get().create(level);
        if (npc == null) return null;
        npc.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, yaw, 0f);
        npc.setup(npcId, pos, generation, yaw);
        if (mode != null) npc.setMode(mode, null);
        level.addFreshEntity(npc);
        return npc;
    }

    /** Przenosi unikalnego NPC w nowe miejsce (stara kopia zniknie przy najbliższym sprawdzeniu pokolenia). */
    @Nullable
    public static StoryNpc move(MinecraftServer server, String npcId, String targetRef, @Nullable StoryNpc.Mode mode,
                                @Nullable String faceRef, @Nullable ServerPlayer owner) {
        ServerLevel level = Araluen.level(server);
        BlockPos target = Locations.resolve(server, targetRef);
        if (level == null || target == null) return null;
        LocationData data = LocationData.get(server);
        removeLoaded(level, npcId);
        int gen = data.bumpGeneration(npcId);
        float yaw = 0f;
        BlockPos face = faceRef != null ? Locations.resolve(server, faceRef) : null;
        if (face != null) yaw = yawTowards(target, face);
        StoryNpc npc = spawn(level, npcId, target, gen, yaw, null);
        if (npc != null && mode != null) npc.setMode(mode, owner);
        return npc;
    }

    public static void despawn(MinecraftServer server, String npcId) {
        ServerLevel level = Araluen.level(server);
        if (level != null) removeLoaded(level, npcId);
        LocationData.get(server).bumpGeneration(npcId);
    }

    private static void removeLoaded(ServerLevel level, String npcId) {
        NpcDefs.NpcDef def = NpcDefs.get(npcId);
        if (def == null || !def.unique()) return;
        for (StoryNpc npc : findLoaded(level, npcId)) npc.discard();
    }

    public static List<StoryNpc> findLoaded(ServerLevel level, String npcId) {
        return new java.util.ArrayList<>(level.getEntities(EntityTypeTest.forClass(StoryNpc.class),
                n -> n.isAlive() && n.npcId().equals(npcId)));
    }

    @Nullable
    public static StoryNpc findNearest(ServerPlayer player, String npcId) {
        if (!(player.level() instanceof ServerLevel level)) return null;
        StoryNpc best = null;
        double bestD = Double.MAX_VALUE;
        for (StoryNpc npc : findLoaded(level, npcId)) {
            double d = npc.distanceToSqr(player);
            if (d < bestD) {
                bestD = d;
                best = npc;
            }
        }
        return best;
    }

    public static float yawTowards(BlockPos from, BlockPos to) {
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        return (float) (Mth.atan2(dz, dx) * (180.0 / Math.PI)) - 90f;
    }

    private NpcService() {}
}
