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
        RouteStop stop = loc == null ? RouteStop.byId(id) : null;
        ServerLevel level = Araluen.level(server);
        if ((loc == null && stop == null) || level == null) {
            Zwiadowcy.LOGGER.warn("Nieznana lokacja lub brak wymiaru: {}", id);
            return null;
        }
        long start = System.currentTimeMillis();
        if (loc != null) {
            placed = StructurePlacer.place(level, loc, data);
        } else {
            // postój na szlaku: punkt na odcinku między dwiema lokacjami (obie stawiamy, jeśli jeszcze ich nie ma)
            LocationData.Placed a = ensure(server, stop.from), b = ensure(server, stop.to);
            if (a == null || b == null) return null;
            BlockPos nominal = new BlockPos((int) Math.round(a.center.getX() + (b.center.getX() - a.center.getX()) * stop.along), 0,
                    (int) Math.round(a.center.getZ() + (b.center.getZ() - a.center.getZ()) * stop.along));
            placed = StructurePlacer.place(level, stop.id, stop.template, nominal, 0, java.util.Set.of(), data, null);
            if (placed == null) return null;
        }
        data.putPlaced(id, placed);
        Zwiadowcy.LOGGER.info("Postawiono lokację {} w {} ({} ms, {} kotwic)", id, placed.center,
                System.currentTimeMillis() - start, placed.anchors.size());
        return placed;
    }

    /**
     * Stawia lokację od nowa w lepszym miejscu (np. gdy w starym świecie trafiła do morza). Stara budowla zostaje,
     * ale kotwice, postacie i koń przechodzą do nowej. Redmontu nie przenosimy - to punkt przybycia do Araluenu.
     */
    @Nullable
    public static LocationData.Placed relocate(MinecraftServer server, String id) {
        StoryLocation loc = StoryLocation.byId(id);
        ServerLevel level = Araluen.level(server);
        if (loc == null || loc == StoryLocation.REDMONT || level == null) return null;
        LocationData data = LocationData.get(server);
        LocationData.Placed old = data.placed(id);
        data.removePlaced(id);
        StructurePlacer.avoid = old != null ? old.center : null;
        LocationData.Placed fresh;
        try {
            fresh = ensure(server, id);
        } finally {
            StructurePlacer.avoid = null;
        }
        if (old != null && fresh != null) {
            var size = level.getStructureManager().get(Zwiadowcy.id(RouteStop.templateOf(id))).map(t -> t.getSize()).orElse(null);
            if (size != null) Relocation.start(level, old.origin, fresh.origin, size);
        }
        return fresh;
    }

    /** Bezpieczne punkty lokacji (z dala od miejsc walki) - tam ustawiamy punkt odrodzenia na czas misji. */
    private static final java.util.Map<String, String> SAFE_SPOTS = java.util.Map.of(
            "redmont", "przybycie", "wensley", "jarmark", "chata_halta", "chata", "stajnia_boba", "stajnia",
            "legowisko_dzika", "baron", "oboz_zlotu", "oboz", "spalona_osada", "halt", "ruiny_gorlanu", "podejscie");

    /** Bezpieczny punkt postawionej lokacji fabularnej albo null (np. postój na szlaku, lokacja jeszcze niepostawiona). */
    @Nullable
    public static BlockPos safeSpot(MinecraftServer server, String locationId) {
        String anchor = SAFE_SPOTS.get(locationId);
        LocationData.Placed placed = LocationData.get(server).placed(locationId);
        if (anchor == null || placed == null) return null;
        return placed.anchors.get(anchor);
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
            return loc == null ? null : StructurePlacer.nominal(LocationData.get(server), loc).atY(70);
        }
        if (parts.length == 1) return placed.center;
        return placed.anchors.getOrDefault(parts[1], placed.center);
    }

    private Locations() {}
}
