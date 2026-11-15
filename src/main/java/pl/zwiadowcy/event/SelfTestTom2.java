package pl.zwiadowcy.event;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import pl.zwiadowcy.world.Araluen;
import pl.zwiadowcy.world.VolumeTwoLayout;

import java.util.List;

/**
 * Autotest tomu II: świat (Rozpadlina, Celtyka, przełęcz, trakt, Uthal). Wywoływany z {@link SelfTest} przed
 * stawianiem lokacji, żeby pomiary dotyczyły czystego terenu z generatora.
 */
final class SelfTestTom2 {

    static void world(MinecraftServer server, List<String> problems, List<String> info) {
        ServerLevel level = Araluen.level(server);
        if (level == null) {
            problems.add("Brak wymiaru Araluen");
            return;
        }
        var gen = level.getChunkSource().getGenerator();
        var noise = level.getChunkSource().randomState();
        info.add(String.format("Tom II: obrót układu krain %.0f°", VolumeTwoLayout.angleDegrees()));

        // --- Rozpadlina: szerokość dna, głębokość, brak wody
        int idx = 0;
        for (double t : new double[]{0.15, 0.3, 0.45, 0.6, 0.75, 0.9}) {
            double[] p = pointAlong(t);
            double[] n = normalAt(t);
            double[] w = VolumeTwoLayout.toWorld(p[0], p[1]);
            int cx = (int) Math.round(w[0]), cz = (int) Math.round(w[1]);
            int floor = gen.getBaseHeight(cx, cz, Heightmap.Types.OCEAN_FLOOR_WG, level, noise);
            int left = scan(gen, level, noise, p, n, -1, floor), right = scan(gen, level, noise, p, n, 1, floor);
            int width = left + right;
            int rimA = groundAt(gen, level, noise, p[0] - n[0] * (left + 8), p[1] - n[1] * (left + 8));
            int rimB = groundAt(gen, level, noise, p[0] + n[0] * (right + 8), p[1] + n[1] * (right + 8));
            int depth = Math.min(rimA, rimB) - floor;
            int water = 0;
            if (idx % 2 == 0) {
                level.getChunk(cx >> 4, cz >> 4);
                BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
                for (int y = floor - 2; y <= floor + 40; y++) if (!level.getFluidState(m.set(cx, y, cz)).isEmpty()) water++;
            }
            info.add(String.format("  Rozpadlina %.0f%% @ %d, %d: dno y=%d, szerokość dna %d, głębokość %d (brzegi y=%d / %d)%s",
                    t * 100, cx, cz, floor, width, depth, rimA, rimB, idx % 2 == 0 ? ", ciecz: " + water : ""));
            if (floor <= gen.getSeaLevel() || floor > 64) problems.add("Rozpadlina: dno na złej wysokości y=" + floor + " @ " + cx + ", " + cz);
            if (width < 20 || width > 80) problems.add("Rozpadlina: szerokość dna " + width + " @ " + cx + ", " + cz);
            if (depth < 25) problems.add("Rozpadlina: za płytka (" + depth + ") @ " + cx + ", " + cz);
            if (water > 0) problems.add("Rozpadlina: woda albo lawa w kanionie (" + water + " bloków) @ " + cx + ", " + cz);
            idx++;
        }

        // --- biomy Celtyki i Uthal
        int celtic = 0, samples = 0;
        double[] e1 = VolumeTwoLayout.CELTICA[0];
        for (int i = -2; i <= 2; i++) for (int j = -2; j <= 2; j++) {
            String b = biomeAt(level, e1[0] + i * e1[2] * 0.3, e1[1] + j * e1[3] * 0.3);
            samples++;
            if (b.equals("wrzosowiska_celtyki") || b.equals("mgliste_doliny") || b.equals("lupkowe_wzgorza")) celtic++;
        }
        String uthal = biomeAt(level, VolumeTwoLayout.UTHAL[0], VolumeTwoLayout.UTHAL[1]);
        info.add(String.format("  Biomy: Celtyka %d/%d próbek celtyckich, środek Uthal: %s", celtic, samples, uthal));
        if (celtic < samples * 0.8) problems.add("Celtyka: za mało celtyckich biomów (" + celtic + "/" + samples + ")");
        if (!uthal.equals("rownina_uthal")) problems.add("Równina Uthal: zły biom w środku (" + uthal + ")");

        // --- wysokości terenu w krainach
        int[] celt = heights(gen, level, noise, e1[0], e1[1], e1[2] * 0.6, e1[3] * 0.6);
        int[] ut = heights(gen, level, noise, VolumeTwoLayout.UTHAL[0], VolumeTwoLayout.UTHAL[1], 120, 120);
        int passMax = lineMax(gen, level, noise, VolumeTwoLayout.PASS);
        int roadMax = lineMax(gen, level, noise, VolumeTwoLayout.ROAD);
        info.add(String.format("  Teren: Celtyka y=%d..%d (średnio %d), Uthal y=%d..%d, przełęcz do y=%d, trakt do y=%d",
                celt[0], celt[1], celt[2], ut[0], ut[1], passMax, roadMax));
        if (celt[1] > 150) problems.add("Celtyka: za wysokie wzgórza (do y=" + celt[1] + ")");
        if (ut[1] - ut[0] > 6) problems.add("Równina Uthal nie jest płaska (rozrzut " + (ut[1] - ut[0]) + ")");
        if (passMax > 135) problems.add("Przełęcz Wiatrów za wysoka (y=" + passMax + ")");
        if (roadMax > 140) problems.add("Trakt wschodni za wysoki (y=" + roadMax + ")");
    }

    // ------------------------------------------------------------------ narzędzia

    private static double totalLength() {
        double total = 0;
        double[][] f = VolumeTwoLayout.FISSURE;
        for (int i = 0; i + 1 < f.length; i++) total += Math.hypot(f[i + 1][0] - f[i][0], f[i + 1][1] - f[i][1]);
        return total;
    }

    /** Punkt łamanej Rozpadliny (układ) dla parametru 0..1. */
    static double[] pointAlong(double t) {
        double[][] f = VolumeTwoLayout.FISSURE;
        double want = t * totalLength();
        for (int i = 0; i + 1 < f.length; i++) {
            double len = Math.hypot(f[i + 1][0] - f[i][0], f[i + 1][1] - f[i][1]);
            if (want <= len) {
                double u = want / len;
                return new double[]{f[i][0] + (f[i + 1][0] - f[i][0]) * u, f[i][1] + (f[i + 1][1] - f[i][1]) * u};
            }
            want -= len;
        }
        return f[f.length - 1];
    }

    /** Lokacje tomu II po postawieniu: wrota posterunku, most nad Rozpadliną i symulacja jego spalenia. */
    static void locations(MinecraftServer server, List<String> problems, List<String> info) {
        ServerLevel level = Araluen.level(server);
        if (level == null) return;
        var data = pl.zwiadowcy.world.LocationData.get(server);
        // wrota posterunku dobosza: zamknięte (bale z kotwic-bloków), po otwarciu 3 bloki wolnego przejścia
        var post = data.placed("posterunek_dobosza");
        if (post != null) {
            int closed = 0;
            for (int i = 1; i <= 4; i++) {
                BlockPos b = post.anchors.get("brama_" + i);
                if (b != null && !level.getBlockState(b).isAir() && !level.getBlockState(b.above()).isAir()) closed++;
            }
            info.add("Wrota posterunku: zamknięte kolumny " + closed + "/4");
            if (closed < 4) problems.add("Wrota posterunku dobosza mają dziurę (" + closed + "/4 kolumn zamkniętych)");
        }
        // most
        var most = pl.zwiadowcy.world.Locations.ensure(server, pl.zwiadowcy.bridge.BridgeBuilder.ID);
        if (most == null) {
            problems.add("Most nad Rozpadliną nie powstał");
            return;
        }
        var tag = data.extra(pl.zwiadowcy.bridge.BridgeBuilder.ID);
        int deckY = tag.getInt("deckY"), len = tag.getInt("nEnd") - tag.getInt("sEnd") - 1;
        BlockPos mid = most.anchors.get("srodek");
        int drop = 0;
        if (mid != null) {
            BlockPos p = mid.below(3);
            while (p.getY() > level.getMinBuildHeight() && level.getBlockState(p).isAir()) {
                drop++;
                p = p.below();
            }
        }
        info.add(String.format("Most: pomost y=%d, długość %d, pod środkiem %d bl. powietrza, kotwic %d", deckY, len, drop, most.anchors.size()));
        if (len < 20 || len > 90) problems.add("Most ma dziwną długość: " + len + " (plan: ok. 46)");
        if (drop < 25) problems.add("Pod środkiem mostu tylko " + drop + " bl. powietrza - most nie wisi nad kanionem");
        for (String a : new String[]{"podpora_1", "podpora_2", "podpora_3", "podpora_4", "przeslo_1", "przeslo_4", "kolumna_1", "kolumna_5",
                "polnoc", "poludnie", "brzeg_polnocny", "brzeg_poludniowy", "patrol_1", "patrol_2"}) {
            if (!most.anchors.containsKey(a)) problems.add("Most: brak kotwicy " + a);
        }
        for (String g : new String[]{"przeslo_1", "przeslo_2", "przeslo_3", "przeslo_4", "kikuty", "podpora_1", "podpora_2", "podpora_3", "podpora_4"}) {
            if (tag.getCompound("groups").getLongArray(g).length == 0) problems.add("Most: pusta grupa bloków " + g);
        }
        // podpory stoją na drewnie (da się je polać), zastrzały mają linę z pomostu
        for (String s : pl.zwiadowcy.bridge.BridgeBuilder.SUPPORTS) {
            BlockPos a = most.anchors.get(s);
            if (a != null && !level.getBlockState(a).is(net.minecraft.tags.BlockTags.LOGS)) problems.add("Most: kotwica " + s + " nie leży na belce @ " + a.toShortString());
        }
        // liny wież (proste odcinki) i przyczółki ścięte tak, że wieże nie tkwią w zboczu
        info.add("  " + pl.zwiadowcy.bridge.BridgeRopes.check(level, tag, problems));
        info.add("  " + pl.zwiadowcy.bridge.BridgeBurn.simulateForTest(server, problems));
    }

    /** Dane tomu II: klatki przerywników i limity bytów w bitwie (plan 16.1). */
    static void data(MinecraftServer server, List<String> problems, List<String> info) {
        var cs = pl.zwiadowcy.cutscene.Cutscenes.INSTANCE;
        int ok = 0;
        for (String id : cs.ids()) {
            var def = cs.def(id);
            if (def == null) continue;
            String loc = net.minecraft.util.GsonHelper.getAsString(def, "location", "");
            if (pl.zwiadowcy.world.Locations.peek(server, loc) == null) problems.add("Przerywnik " + id + ": nieznane miejsce '" + loc + "'");
            var keys = net.minecraft.util.GsonHelper.getAsJsonArray(def, "keyframes", new com.google.gson.JsonArray());
            int last = -1;
            boolean good = keys.size() >= 2;
            for (var k : keys) {
                var ko = k.getAsJsonObject();
                int t = net.minecraft.util.GsonHelper.getAsInt(ko, "t", -1);
                if (t <= last || !ko.has("pos") || !ko.has("look") || ko.getAsJsonArray("pos").size() != 3 || ko.getAsJsonArray("look").size() != 3) good = false;
                last = t;
            }
            if (!good) problems.add("Przerywnik " + id + ": złe klatki (min. 2, rosnący czas, pos i look po 3 liczby)");
            else ok++;
        }
        info.add("Przerywniki: " + ok + "/" + cs.ids().size() + " poprawnych");
        // bitwa: manekiny w misji narastająco (z odjęciem crowd_clear) i żywi wrogowie na etap
        for (var q : pl.zwiadowcy.quest.QuestManager.INSTANCE.all()) {
            if (q.tom() < 2) continue;
            java.util.Map<String, Integer> crowds = new java.util.HashMap<>();
            crowdScan(q.onStart(), crowds);
            int maxCrowd = sum(crowds), maxLive = 0;
            for (var st : q.stages()) {
                crowdScan(st.onStart(), crowds);
                maxCrowd = Math.max(maxCrowd, sum(crowds));
                int live = spawnCount(st.onStart());
                for (var tr : st.triggers().values()) live += spawnCount(tr);
                maxLive = Math.max(maxLive, live);
                crowdScan(st.onComplete(), crowds);
            }
            if (maxCrowd > pl.zwiadowcy.entity.CrowdDummyEntity.MAX) problems.add("Misja " + q.id() + ": " + maxCrowd + " manekinów naraz (limit 160)");
            if (maxLive > pl.zwiadowcy.quest.QuestActions.BATTLE_LIVE_LIMIT) problems.add("Misja " + q.id() + ": etap z " + maxLive + " żywymi wrogami (limit 40)");
            if (maxCrowd > 0) info.add(String.format("  bitwa %-24s manekiny naraz maks. %d, wrogowie na etap maks. %d", q.id(), maxCrowd, maxLive));
        }
    }

    private static int sum(java.util.Map<String, Integer> m) {
        return m.values().stream().mapToInt(Integer::intValue).sum();
    }

    private static void crowdScan(com.google.gson.JsonArray actions, java.util.Map<String, Integer> crowds) {
        for (var el : actions) {
            var a = el.getAsJsonObject();
            String type = net.minecraft.util.GsonHelper.getAsString(a, "type", "");
            String tag = net.minecraft.util.GsonHelper.getAsString(a, "tag", "tlum");
            if (type.equals("crowd")) crowds.merge(tag, net.minecraft.util.GsonHelper.getAsInt(a, "count", 10), Integer::sum);
            if (type.equals("crowd_clear")) {
                if (tag.isEmpty()) crowds.clear();
                else crowds.remove(tag);
            }
            if (type.equals("delay")) crowdScan(net.minecraft.util.GsonHelper.getAsJsonArray(a, "actions", new com.google.gson.JsonArray()), crowds);
        }
    }

    private static int spawnCount(com.google.gson.JsonArray actions) {
        int n = 0;
        for (var el : actions) {
            var a = el.getAsJsonObject();
            String type = net.minecraft.util.GsonHelper.getAsString(a, "type", "");
            if (type.equals("spawn") || type.equals("spawn_random")) n += net.minecraft.util.GsonHelper.getAsInt(a, "count", 1);
            if (type.equals("delay")) n += spawnCount(net.minecraft.util.GsonHelper.getAsJsonArray(a, "actions", new com.google.gson.JsonArray()));
        }
        return n;
    }

    static double[] normalAt(double t) {
        double[] a = pointAlong(Math.max(0, t - 0.02)), b = pointAlong(Math.min(1, t + 0.02));
        double dx = b[0] - a[0], dz = b[1] - a[1], len = Math.hypot(dx, dz);
        return new double[]{-dz / len, dx / len};
    }

    private static int groundAt(net.minecraft.world.level.chunk.ChunkGenerator gen, ServerLevel level,
                                net.minecraft.world.level.levelgen.RandomState noise, double lx, double lz) {
        double[] w = VolumeTwoLayout.toWorld(lx, lz);
        return gen.getBaseHeight((int) Math.round(w[0]), (int) Math.round(w[1]), Heightmap.Types.OCEAN_FLOOR_WG, level, noise);
    }

    /** Odległość od środka kanionu do miejsca, gdzie teren wznosi się 12 bloków nad dno. */
    private static int scan(net.minecraft.world.level.chunk.ChunkGenerator gen, ServerLevel level,
                            net.minecraft.world.level.levelgen.RandomState noise, double[] p, double[] n, int dir, int floor) {
        for (int d = 1; d <= 70; d++) {
            if (groundAt(gen, level, noise, p[0] + n[0] * d * dir, p[1] + n[1] * d * dir) > floor + 12) return d;
        }
        return 70;
    }

    private static String biomeAt(ServerLevel level, double lx, double lz) {
        double[] w = VolumeTwoLayout.toWorld(lx, lz);
        return level.getBiome(new BlockPos((int) w[0], 90, (int) w[1])).unwrapKey().map(k -> k.location().getPath()).orElse("?");
    }

    /** min, max i średnia wysokość gruntu na siatce 5×5 w elipsie. */
    private static int[] heights(net.minecraft.world.level.chunk.ChunkGenerator gen, ServerLevel level,
                                 net.minecraft.world.level.levelgen.RandomState noise, double cx, double cz, double rx, double rz) {
        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        long sum = 0;
        int n = 0;
        for (int i = -2; i <= 2; i++) for (int j = -2; j <= 2; j++) {
            int h = groundAt(gen, level, noise, cx + i * rx / 2, cz + j * rz / 2);
            min = Math.min(min, h);
            max = Math.max(max, h);
            sum += h;
            n++;
        }
        return new int[]{min, max, (int) (sum / n)};
    }

    /** Najwyższy punkt na osi kapsuły (przełęcz, trakt). */
    private static int lineMax(net.minecraft.world.level.chunk.ChunkGenerator gen, ServerLevel level,
                               net.minecraft.world.level.levelgen.RandomState noise, double[] capsule) {
        int max = 0;
        for (int i = 0; i <= 16; i++) {
            double t = i / 16.0;
            max = Math.max(max, groundAt(gen, level, noise, capsule[0] + (capsule[2] - capsule[0]) * t, capsule[1] + (capsule[3] - capsule[1]) * t));
        }
        return max;
    }

    private SelfTestTom2() {}
}
