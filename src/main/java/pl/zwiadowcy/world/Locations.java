package pl.zwiadowcy.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import pl.zwiadowcy.Zwiadowcy;

import javax.annotation.Nullable;

/**
 * Dostęp do lokacji fabularnych. Odwołania mają postać "lokacja" (środek struktury)
 * albo "lokacja:kotwica" (punkt oznaczony w pliku .nbt blokiem struktury w trybie DATA).
 */
public final class Locations {

    @Nullable
    public static LocationData.Placed ensure(MinecraftServer server, String id) {
        LocationData data = LocationData.get(server);
        LocationData.Placed placed = data.placed(id);
        if (placed != null) return placed;
        StoryLocation loc = StoryLocation.byId(id);
        ServerLevel level = Araluen.level(server);
        if (loc == null || level == null) {
            Zwiadowcy.LOGGER.warn("Nieznana lokacja lub brak wymiaru: {}", id);
            return null;
        }
        long start = System.currentTimeMillis();
        placed = StructurePlacer.place(level, loc, data);
        data.putPlaced(id, placed);
        Zwiadowcy.LOGGER.info("Postawiono lokację {} w {} ({} ms, {} kotwic)", id, placed.center,
                System.currentTimeMillis() - start, placed.anchors.size());
        return placed;
    }

    /** Rozwiązuje odwołanie "lokacja[:kotwica]" na pozycję w Araluenie (stawiając strukturę w razie potrzeby). */
    @Nullable
    public static BlockPos resolve(MinecraftServer server, @Nullable String ref) {
        if (ref == null || ref.isEmpty()) return null;
        String[] parts = ref.split(":", 2);
        LocationData.Placed placed = ensure(server, parts[0]);
        if (placed == null) return null;
        if (parts.length == 1) return placed.center;
        BlockPos anchor = placed.anchors.get(parts[1]);
        if (anchor == null) {
            Zwiadowcy.LOGGER.warn("Brak kotwicy '{}' w lokacji '{}'", parts[1], parts[0]);
            return placed.center;
        }
        return anchor;
    }

    /** Jak {@link #resolve}, ale bez stawiania struktury (do HUD i synchronizacji). */
    @Nullable
    public static BlockPos peek(MinecraftServer server, @Nullable String ref) {
        if (ref == null || ref.isEmpty()) return null;
        String[] parts = ref.split(":", 2);
        LocationData.Placed placed = LocationData.get(server).placed(parts[0]);
        if (placed == null) {
            StoryLocation loc = StoryLocation.byId(parts[0]);
            return loc == null ? null : new BlockPos(loc.x, 70, loc.z);
        }
        if (parts.length == 1) return placed.center;
        return placed.anchors.getOrDefault(parts[1], placed.center);
    }

    private Locations() {}
}
