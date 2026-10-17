package pl.zwiadowcy.event;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import pl.zwiadowcy.Zwiadowcy;
import pl.zwiadowcy.dialogue.DialogueDefinition;
import pl.zwiadowcy.entity.NpcDefs;
import pl.zwiadowcy.quest.QuestManager;
import pl.zwiadowcy.world.LocationData;
import pl.zwiadowcy.world.Locations;
import pl.zwiadowcy.world.StoryLocation;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Autotest danych fabularnych (uruchamiany: gradlew runServer -Pselftest=true).
 * Stawia wszystkie lokacje, sprawdza odwołania w misjach i dialogach, zapisuje raport do run/selftest.txt
 * i zatrzymuje serwer.
 */
@Mod.EventBusSubscriber(modid = Zwiadowcy.MODID)
public final class SelfTest {
    private static final Set<String> QUEST_KEYS = Set.of("quest", "quest_active", "quest_stage", "quest_completed",
            "quest_not_started", "quest_not_completed", "quest_ready");
    private static final Set<String> ITEM_KEYS = Set.of("item", "has_item", "not_has_item", "drop");
    private static final Set<String> LOCATION_KEYS = Set.of("location", "checkpoint", "to", "at", "face", "center", "home");

    @SubscribeEvent
    public static void onStarted(ServerStartedEvent event) {
        if (!Boolean.getBoolean("zwiadowcy.selftest")) return;
        MinecraftServer server = event.getServer();
        List<String> problems = new ArrayList<>();
        List<String> info = new ArrayList<>();
        try {
            run(server, problems, info);
        } catch (Exception e) {
            problems.add("Wyjątek podczas testu: " + e);
            Zwiadowcy.LOGGER.error("SELFTEST wyjątek", e);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("=== Autotest Zwiadowców ===\n");
        info.forEach(i -> sb.append("  ").append(i).append('\n'));
        sb.append(problems.isEmpty() ? "WYNIK: OK - brak problemów\n" : "WYNIK: " + problems.size() + " problemów\n");
        problems.forEach(p -> sb.append("  ! ").append(p).append('\n'));
        Zwiadowcy.LOGGER.info("\n{}", sb);
        try {
            Files.writeString(Path.of("selftest.txt"), sb.toString(), StandardCharsets.UTF_8);
        } catch (Exception ignored) {}
        server.halt(false);
    }

    private static void run(MinecraftServer server, List<String> problems, List<String> info) throws Exception {
        info.add("Misje: " + QuestManager.INSTANCE.all().size());

        // 1. lokacje - postaw wszystkie i zbierz kotwice
        for (StoryLocation loc : StoryLocation.values()) {
            long t = System.currentTimeMillis();
            LocationData.Placed p = Locations.ensure(server, loc.id);
            if (p == null) {
                problems.add("Nie udało się postawić lokacji " + loc.id);
                continue;
            }
            info.add(String.format("Lokacja %-16s @ %s, kotwic: %d (%d ms)", loc.id, p.center.toShortString(),
                    p.anchors.size(), System.currentTimeMillis() - t));
        }
        // 1. punkt odrodzenia na czas misji: każda lokacja ma bezpieczny punkt
        for (StoryLocation loc : StoryLocation.values()) {
            if (Locations.safeSpot(server, loc.id) == null) problems.add("Lokacja " + loc.id + " nie ma bezpiecznego punktu odrodzenia");
        }
        // 1a. postoje na szlaku - na odcinku między lokacjami, z kotwicami w dobrych miejscach
        for (pl.zwiadowcy.world.RouteStop stop : pl.zwiadowcy.world.RouteStop.values()) {
            long t = System.currentTimeMillis();
            LocationData.Placed p = Locations.ensure(server, stop.id);
            if (p == null) {
                problems.add("Nie udało się postawić postoju " + stop.id);
                continue;
            }
            LocationData.Placed a = LocationData.get(server).placed(stop.from), b = LocationData.get(server).placed(stop.to);
            double route = a != null && b != null ? Math.sqrt(a.center.distSqr(b.center)) : 0;
            double offRoute = a != null && b != null ? distanceToSegment(p.center, a.center, b.center) : 0;
            info.add(String.format("Postój %-24s @ %s, trasa %.0f bl., od traktu %.0f bl. (%d ms)", stop.id, p.center.toShortString(),
                    route, offRoute, System.currentTimeMillis() - t));
            if (offRoute > 160) problems.add("Postój " + stop.id + " leży daleko od trasy (" + (int) offRoute + " bloków)");
            for (Map.Entry<String, net.minecraft.core.BlockPos> an : p.anchors.entrySet()) {
                var lvl = pl.zwiadowcy.world.Araluen.level(server);
                if (lvl == null) break;
                var pos = an.getValue();
                if (!lvl.getBlockState(pos).getCollisionShape(lvl, pos).isEmpty() || !lvl.getBlockState(pos.above()).getCollisionShape(lvl, pos.above()).isEmpty()) {
                    problems.add("Kotwica " + stop.id + ":" + an.getKey() + " zasypana @ " + pos.toShortString());
                }
            }
        }
        LocationData data = LocationData.get(server);
        net.minecraft.server.level.ServerLevel level = pl.zwiadowcy.world.Araluen.level(server);
        if (level != null) {
            // biom pod każdą lokacją: żadna nie może stać w morzu, rzece ani na plaży
            for (StoryLocation loc : StoryLocation.values()) {
                LocationData.Placed p = data.placed(loc.id);
                if (p != null) checkBiome(level, "Lokacja " + loc.id, p.center, problems, info);
            }
            checkWater(level, data, problems, info);
            // przeniesienie lokacji komendą /zwiadowcy przestaw
            LocationData.Placed before = data.placed("chata_halta");
            LocationData.Placed moved = Locations.relocate(server, "chata_halta");
            if (moved == null || before == null || moved.center.equals(before.center)) {
                problems.add("Przestawienie chaty Halta nie zmieniło jej położenia");
            } else {
                checkBiome(level, "Przestawiona chata_halta", moved.center, problems, info);
            }
            for (StoryLocation loc : StoryLocation.values()) {
                LocationData.Placed p = data.placed(loc.id);
                if (p == null) continue;
                for (Map.Entry<String, net.minecraft.core.BlockPos> a : p.anchors.entrySet()) {
                    net.minecraft.core.BlockPos pos = a.getValue();
                    boolean blockedFeet = !level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
                    boolean blockedHead = !level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty();
                    boolean floating = level.getBlockState(pos.below()).getCollisionShape(level, pos.below()).isEmpty()
                            && level.getFluidState(pos.below()).isEmpty();
                    if (blockedFeet || blockedHead) problems.add("Kotwica " + loc.id + ":" + a.getKey() + " zasypana @ " + pos.toShortString());
                    if (floating) problems.add("Kotwica " + loc.id + ":" + a.getKey() + " wisi w powietrzu @ " + pos.toShortString());
                }
            }
        }

        // 1b. losowe struktury - znajdź każdą w świecie Araluenu
        if (level != null) {
            var registry = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE);
            String[] random = {"oboz_wargalow", "posterunek_morgaratha", "kryjowka_bandytow", "straznica", "kurhan",
                    "legowisko_niedzwiedzia", "kamienny_krag", "zajazd", "zagroda"};
            for (String id : random) {
                var key = net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.STRUCTURE, Zwiadowcy.id(id));
                var holder = registry.getHolder(key);
                if (holder.isEmpty()) {
                    problems.add("Brak definicji struktury losowej " + id);
                    continue;
                }
                long t = System.currentTimeMillis();
                var found = level.getChunkSource().getGenerator().findNearestMapStructure(level,
                        net.minecraft.core.HolderSet.direct(holder.get()), new net.minecraft.core.BlockPos(2500, 64, -2500), 80, false);
                if (found == null) {
                    problems.add("Nie znaleziono struktury " + id + " w promieniu 1280 bloków");
                } else {
                    net.minecraft.core.BlockPos pos = found.getFirst();
                    for (int dx = -3; dx <= 3; dx++) for (int dz = -3; dz <= 3; dz++) level.getChunk((pos.getX() >> 4) + dx, (pos.getZ() >> 4) + dz);
                    var box = new net.minecraft.world.phys.AABB(pos.getX() - 40, level.getMinBuildHeight(), pos.getZ() - 40,
                            pos.getX() + 40, level.getMaxBuildHeight(), pos.getZ() + 40);
                    java.util.Map<String, Integer> counts = new java.util.TreeMap<>();
                    for (var e : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class, box)) {
                        String name = e instanceof pl.zwiadowcy.entity.StoryNpc npc ? "npc:" + npc.npcId()
                                : net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getKey(e.getType()).getPath();
                        counts.merge(name, 1, Integer::sum);
                    }
                    info.add(String.format("Losowa %-24s @ %s (%d ms) byty: %s", id, pos.toShortString(), System.currentTimeMillis() - t, counts));
                    checkDecor(level, "losowa " + id, box, problems, info);
                }
            }
        }

        // 1c. wystrój lokacji fabularnych: obrazy i ramki muszą wisieć na ścianie (inaczej odpadną)
        if (level != null) {
            for (StoryLocation loc : StoryLocation.values()) {
                LocationData.Placed p = data.placed(loc.id);
                if (p != null) checkDecor(level, "lokacja " + loc.id, new net.minecraft.world.phys.AABB(p.center).inflate(48, 32, 48), problems, info);
            }
        }

        if (level != null && data.placed("ruiny_gorlanu") != null) {
            var c = data.placed("ruiny_gorlanu").center;
            info.add("Biom przy Ruinach Gorlanu: " + level.getBiome(c).unwrapKey().map(k -> k.location().toString()).orElse("?"));
            var crypt = data.placed("ruiny_gorlanu").anchors.get("krypta");
            if (crypt != null) {
                info.add("Krypta Gorlanu: kotwica @ " + crypt.toShortString() + ", powietrze: " + level.getBlockState(crypt).isAir()
                        + ", byty w krypcie: " + level.getEntitiesOfClass(pl.zwiadowcy.entity.WargalEntity.class,
                        new net.minecraft.world.phys.AABB(crypt).inflate(25, 4, 10)).size());
            }
            // arena: Kalkar (1,3 × 3,1 bloku) musi pojawić się na posadzce, z wolnym miejscem 3×3×4 wokół
            for (String k : new String[]{"kalkar_1", "kalkar_2", "arena"}) {
                var pos = data.placed("ruiny_gorlanu").anchors.get(k);
                if (pos == null) {
                    problems.add("Brak kotwicy ruiny_gorlanu:" + k);
                    continue;
                }
                boolean blocked = false;
                for (var b : net.minecraft.core.BlockPos.betweenClosed(pos.offset(-1, 0, -1), pos.offset(1, 3, 1))) {
                    if (!level.getBlockState(b).getCollisionShape(level, b).isEmpty()) blocked = true;
                }
                boolean floor = !level.getBlockState(pos.below()).getCollisionShape(level, pos.below()).isEmpty();
                if (blocked || !floor) problems.add("Arena Gorlanu: miejsce " + k + " " + (blocked ? "zastawione" : "bez posadzki") + " @ " + pos.toShortString());
            }
        }

        // 2. postacie unikalne mają znacznik w lokacji startowej
        for (NpcDefs.NpcDef def : NpcDefs.all()) {
            if (!def.unique() || def.initialLocation() == null) continue;
            LocationData.Placed p = data.placed(def.initialLocation());
            if (p == null || !p.anchors.containsKey(def.id())) {
                problems.add("NPC " + def.id() + " nie ma znacznika w lokacji " + def.initialLocation());
            }
        }

        // 3. misje i dialogi - surowy JSON
        Map<String, JsonObject> quests = load(server, "quests");
        Map<String, JsonObject> dialogues = load(server, "dialogues");
        info.add("Pliki misji: " + quests.size() + ", pliki dialogów: " + dialogues.size());
        quests.forEach((id, json) -> walk("misja " + id, json, server, data, quests.keySet(), problems));
        dialogues.forEach((id, json) -> {
            walk("dialog " + id, json, server, data, quests.keySet(), problems);
            try {
                DialogueDefinition def = DialogueDefinition.parse(id, json);
                for (DialogueDefinition.Entry e : def.entries()) {
                    if (!def.nodes().containsKey(e.start())) problems.add("dialog " + id + ": brak węzła startowego " + e.start());
                }
                def.nodes().forEach((nodeId, node) -> {
                    for (DialogueDefinition.Option o : node.options()) {
                        if (o.next() != null && !def.nodes().containsKey(o.next())) {
                            problems.add("dialog " + id + "/" + nodeId + ": brak węzła " + o.next());
                        }
                    }
                });
                if (NpcDefs.get(def.npc()) == null && !def.npc().equals("wyrwij")) problems.add("dialog " + id + ": nieznana postać " + def.npc());
            } catch (Exception e) {
                problems.add("dialog " + id + ": " + e.getMessage());
            }
        });
        for (NpcDefs.NpcDef def : NpcDefs.all()) {
            if (!dialogues.containsKey(def.id())) problems.add("Postać " + def.id() + " nie ma pliku dialogu");
        }
    }

    /**
     * Woda w Araluenie: siatka 100×100 punktów co 32 bloki wokół Redmontu, wysokość gruntu z szumu generatora.
     * Grunt poniżej poziomu morza = woda. Największy spójny zbiornik szerszy niż ~160 bloków to problem.
     */
    private static void checkWater(net.minecraft.server.level.ServerLevel level, LocationData data, List<String> problems, List<String> info) {
        var gen = level.getChunkSource().getGenerator();
        var noise = level.getChunkSource().randomState();
        int sea = level.getSeaLevel(), n = 100, step = 32;
        LocationData.Placed redmont = data.placed("redmont");
        net.minecraft.core.BlockPos c = redmont != null ? redmont.center : net.minecraft.core.BlockPos.ZERO;
        boolean[][] wet = new boolean[n][n];
        int count = 0;
        for (int i = 0; i < n; i++) for (int j = 0; j < n; j++) {
            int x = c.getX() + (i - n / 2) * step, z = c.getZ() + (j - n / 2) * step;
            // woda = ciecz nad gruntem (powierzchnia z wodą wyżej niż dno); suche jamy i otwory jaskiń się nie liczą
            int ground = gen.getBaseHeight(x, z, net.minecraft.world.level.levelgen.Heightmap.Types.OCEAN_FLOOR_WG, level, noise);
            int surface = gen.getBaseHeight(x, z, net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG, level, noise);
            wet[i][j] = surface > ground;
            if (wet[i][j]) count++;
        }
        boolean[][] seen = new boolean[n][n];
        int widest = 0;
        net.minecraft.core.BlockPos widestAt = null;
        for (int i = 0; i < n; i++) for (int j = 0; j < n; j++) {
            if (!wet[i][j] || seen[i][j]) continue;
            int minI = i, maxI = i, minJ = j, maxJ = j;
            java.util.ArrayDeque<int[]> queue = new java.util.ArrayDeque<>();
            queue.add(new int[]{i, j});
            seen[i][j] = true;
            while (!queue.isEmpty()) {
                int[] p = queue.poll();
                minI = Math.min(minI, p[0]); maxI = Math.max(maxI, p[0]);
                minJ = Math.min(minJ, p[1]); maxJ = Math.max(maxJ, p[1]);
                for (int[] d : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                    int a = p[0] + d[0], b = p[1] + d[1];
                    if (a < 0 || b < 0 || a >= n || b >= n || seen[a][b] || !wet[a][b]) continue;
                    seen[a][b] = true;
                    queue.add(new int[]{a, b});
                }
            }
            int size = Math.max(maxI - minI + 1, maxJ - minJ + 1) * step;
            if (size > widest) {
                widest = size;
                widestAt = new net.minecraft.core.BlockPos(c.getX() + ((minI + maxI) / 2 - n / 2) * step, sea, c.getZ() + ((minJ + maxJ) / 2 - n / 2) * step);
            }
        }
        info.add(String.format("Woda w Araluenie (siatka %d km²): %.1f%% próbek, najszerszy zbiornik ok. %d bloków%s",
                (n * step / 1000) * (n * step / 1000), 100.0 * count / (n * n), widest,
                widestAt == null ? "" : " @ " + widestAt.toShortString() + " (" + level.getBiome(widestAt).unwrapKey().map(k -> k.location().getPath()).orElse("?") + ")"));
        if (widest > 160) problems.add("W Araluenie jest duży zbiornik wody, szeroki na ok. " + widest + " bloków");
    }

    private static double distanceToSegment(net.minecraft.core.BlockPos p, net.minecraft.core.BlockPos a, net.minecraft.core.BlockPos b) {
        double ax = a.getX(), az = a.getZ(), bx = b.getX() - ax, bz = b.getZ() - az, px = p.getX() - ax, pz = p.getZ() - az;
        double len2 = bx * bx + bz * bz;
        double t = len2 == 0 ? 0 : Math.max(0, Math.min(1, (px * bx + pz * bz) / len2));
        double dx = px - t * bx, dz = pz - t * bz;
        return Math.sqrt(dx * dx + dz * dz);
    }

    private static void checkBiome(net.minecraft.server.level.ServerLevel level, String where, net.minecraft.core.BlockPos pos,
                                   List<String> problems, List<String> info) {
        var biome = level.getBiome(pos);
        String id = biome.unwrapKey().map(k -> k.location().toString()).orElse("?");
        boolean wet = biome.is(net.minecraft.tags.BiomeTags.IS_OCEAN) || biome.is(net.minecraft.tags.BiomeTags.IS_DEEP_OCEAN)
                || biome.is(net.minecraft.tags.BiomeTags.IS_RIVER) || biome.is(net.minecraft.tags.BiomeTags.IS_BEACH);
        info.add(String.format("  biom %-24s %s @ %s", where, id, pos.toShortString()));
        if (wet) problems.add(where + " stoi w biomie wodnym (" + id + ") @ " + pos.toShortString());
    }

    private static void checkDecor(net.minecraft.server.level.ServerLevel level, String where, net.minecraft.world.phys.AABB box,
                                   List<String> problems, List<String> info) {
        int paintings = 0, frames = 0;
        for (var e : level.getEntitiesOfClass(net.minecraft.world.entity.decoration.HangingEntity.class, box)) {
            boolean painting = e instanceof net.minecraft.world.entity.decoration.Painting;
            if (painting) paintings++; else frames++;
            if (!e.survives()) problems.add(where + ": " + (painting ? "obraz" : "ramka") + " odpadnie @ " + e.blockPosition().toShortString());
        }
        int stands = level.getEntitiesOfClass(net.minecraft.world.entity.decoration.ArmorStand.class, box).size();
        if (paintings + frames + stands > 0) {
            info.add(String.format("  wystrój %-26s obrazy: %d, ramki: %d, stojaki: %d", where, paintings, frames, stands));
        }
    }

    private static Map<String, JsonObject> load(MinecraftServer server, String dir) throws Exception {
        Map<String, JsonObject> out = new TreeMap<>();
        Map<ResourceLocation, Resource> res = server.getResourceManager().listResources(dir, rl -> rl.getPath().endsWith(".json"));
        for (Map.Entry<ResourceLocation, Resource> e : res.entrySet()) {
            String path = e.getKey().getPath();
            String id = path.substring(dir.length() + 1, path.length() - 5);
            try (Reader r = e.getValue().openAsReader()) {
                out.put(id, JsonParser.parseReader(r).getAsJsonObject());
            }
        }
        return out;
    }

    private static void walk(String where, JsonElement el, MinecraftServer server, LocationData data,
                             Set<String> quests, List<String> problems) {
        if (el.isJsonArray()) {
            el.getAsJsonArray().forEach(c -> walk(where, c, server, data, quests, problems));
            return;
        }
        if (!el.isJsonObject()) return;
        JsonObject o = el.getAsJsonObject();
        for (Map.Entry<String, JsonElement> e : o.entrySet()) {
            String k = e.getKey();
            JsonElement v = e.getValue();
            if (v.isJsonPrimitive() && v.getAsJsonPrimitive().isString()) {
                String s = v.getAsString();
                if (QUEST_KEYS.contains(k) && !quests.contains(s)) problems.add(where + ": nieznana misja '" + s + "' (" + k + ")");
                if (k.equals("type") && s.equals("start_quest")) { /* sprawdzane przez klucz quest */ }
                if (ITEM_KEYS.contains(k) && !s.startsWith("#")) {
                    ResourceLocation rl = ResourceLocation.tryParse(s);
                    if (rl == null || !ForgeRegistries.ITEMS.containsKey(rl)) problems.add(where + ": nieznany przedmiot '" + s + "'");
                }
                if (k.equals("entity") && !s.equals("*")) {
                    ResourceLocation rl = ResourceLocation.tryParse(s);
                    if (rl == null || !ForgeRegistries.ENTITY_TYPES.containsKey(rl)) problems.add(where + ": nieznany byt '" + s + "'");
                }
                if (k.equals("sound")) {
                    ResourceLocation rl = ResourceLocation.tryParse(s);
                    if (rl == null || !ForgeRegistries.SOUND_EVENTS.containsKey(rl)) problems.add(where + ": nieznany dźwięk '" + s + "'");
                }
                if (k.equals("npc") && NpcDefs.get(s) == null && !s.equals("wyrwij")) problems.add(where + ": nieznana postać '" + s + "'");
                if (k.equals("advancement") && server.getAdvancements().getAdvancement(Zwiadowcy.id(s)) == null) {
                    problems.add(where + ": brak osiągnięcia zwiadowcy:" + s);
                }
                if (LOCATION_KEYS.contains(k)) checkLocation(where, s, data, problems);
            } else {
                walk(where, v, server, data, quests, problems);
            }
        }
    }

    private static void checkLocation(String where, String ref, LocationData data, List<String> problems) {
        String[] parts = ref.split(":", 2);
        if (StoryLocation.byId(parts[0]) == null && pl.zwiadowcy.world.RouteStop.byId(parts[0]) == null) {
            problems.add(where + ": nieznana lokacja '" + ref + "'");
            return;
        }
        LocationData.Placed p = data.placed(parts[0]);
        if (p == null) {
            problems.add(where + ": lokacja '" + parts[0] + "' nie została postawiona");
        } else if (parts.length > 1 && !p.anchors.containsKey(parts[1])) {
            problems.add(where + ": brak kotwicy '" + ref + "'");
        }
    }

    private SelfTest() {}
}
