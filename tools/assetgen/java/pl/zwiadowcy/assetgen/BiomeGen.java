package pl.zwiadowcy.assetgen;

import com.google.gson.*;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Generator biomów Araluenu: kopiuje biomy vanilla (drzewa, rudy, roślinność) z jara Minecrafta,
 * zmienia kolory, usuwa potwory vanilla i lochy, dodaje stworzenia moda. Tworzy też ustawienia szumu
 * "zwiadowcy:araluen" (kopia overworld z regułami powierzchni rozszerzonymi o nowe biomy).
 * Uruchamianie: gradlew generateBiomes
 */
public final class BiomeGen {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    record Spawn(String type, int weight, int min, int max) {}

    record BiomeSpec(String id, String base, float temperature, float downfall, int grass, int foliage, int sky, int fog,
                     int water, boolean keepMusic, List<Spawn> extraCreatures, List<Spawn> monsters, boolean dropFlowers) {}

    static Spawn s(String type, int weight, int min, int max) {
        return new Spawn(type, weight, min, max);
    }

    public static void main(String[] args) throws Exception {
        File jar = new File(args[0]);
        File dataRoot = new File(args[1]);
        List<BiomeSpec> specs = List.of(
                new BiomeSpec("lasy_redmont", "forest", 0.7f, 0.8f, 0x6fb04e, 0x5a9e3a, 0x78a7ff, 0xc0d8ff, 0x3f76e4, true,
                        List.of(s("zwiadowcy:dzik", 8, 1, 3), s("zwiadowcy:niedzwiedz", 2, 1, 1)),
                        List.of(s("zwiadowcy:bandyta", 10, 1, 2), s("zwiadowcy:lucznik_bandytow", 5, 1, 1)), false),
                new BiomeSpec("laki_araluenu", "plains", 0.8f, 0.4f, 0x8cc261, 0x77ab2f, 0x78a7ff, 0xc0d8ff, 0x3f76e4, true,
                        List.of(s("zwiadowcy:dzik", 3, 1, 2)),
                        List.of(s("zwiadowcy:bandyta", 8, 1, 2), s("zwiadowcy:lucznik_bandytow", 4, 1, 1)), false),
                new BiomeSpec("brzozowy_gaj", "birch_forest", 0.6f, 0.6f, 0x88bb67, 0x6ba941, 0x7aa9ff, 0xc0d8ff, 0x3f76e4, true,
                        List.of(s("zwiadowcy:dzik", 6, 1, 3)),
                        List.of(s("zwiadowcy:bandyta", 6, 1, 2)), false),
                new BiomeSpec("mroczna_puszcza", "dark_forest", 0.7f, 0.8f, 0x4f7a34, 0x3f6a28, 0x7a9ab0, 0x9aa8a0, 0x3a5a7a, false,
                        List.of(s("zwiadowcy:dzik", 8, 1, 3), s("zwiadowcy:niedzwiedz", 4, 1, 1)),
                        List.of(s("zwiadowcy:bandyta", 10, 1, 2), s("zwiadowcy:lucznik_bandytow", 6, 1, 1)), false),
                new BiomeSpec("bory_polnocy", "taiga", 0.25f, 0.8f, 0x7a9e5a, 0x5f8a50, 0x7da3ff, 0xc0d0e0, 0x3d57d6, true,
                        List.of(s("zwiadowcy:niedzwiedz", 6, 1, 2)),
                        List.of(s("zwiadowcy:bandyta", 6, 1, 2), s("zwiadowcy:lucznik_bandytow", 3, 1, 1)), false),
                new BiomeSpec("stare_bory", "old_growth_spruce_taiga", 0.25f, 0.8f, 0x6a8a4a, 0x557a44, 0x7da3ff, 0xb8c8d8, 0x3d57d6, true,
                        List.of(s("zwiadowcy:niedzwiedz", 8, 1, 2)),
                        List.of(s("zwiadowcy:bandyta", 6, 1, 2), s("zwiadowcy:lucznik_bandytow", 4, 1, 1)), false),
                new BiomeSpec("pogorze_deszczu_i_nocy", "windswept_forest", 0.2f, 0.9f, 0x5a6a4a, 0x4a5a3a, 0x5a6a7a, 0x6a7078, 0x2a3a4a, false,
                        List.of(),
                        List.of(s("zwiadowcy:wargal", 20, 2, 4), s("zwiadowcy:wargal_zwiadowca", 12, 1, 3),
                                s("zwiadowcy:wargal_oszczepnik", 8, 1, 2), s("zwiadowcy:wargal_herszt", 1, 1, 1)), false),
                new BiomeSpec("szczyty_deszczu_i_nocy", "stony_peaks", 0.1f, 0.9f, 0x5a6050, 0x4a5040, 0x4a5560, 0x5a6068, 0x2a3a4a, false,
                        List.of(),
                        List.of(s("zwiadowcy:wargal", 14, 2, 4), s("zwiadowcy:wargal_oszczepnik", 8, 1, 2),
                                s("zwiadowcy:wargal_zwiadowca", 8, 1, 3), s("zwiadowcy:wargal_herszt", 1, 1, 1)), false),
                new BiomeSpec("rownina_samotnosci", "plains", 1.0f, 0.2f, 0x9a9468, 0x8a8458, 0x8a9aa8, 0xa8a8a0, 0x4a6070, false,
                        List.of(s("minecraft:rabbit", 4, 2, 3)),
                        List.of(s("zwiadowcy:wargal", 8, 1, 3), s("zwiadowcy:wargal_zwiadowca", 6, 1, 2), s("zwiadowcy:wargal_oszczepnik", 4, 1, 2)), true),
                // tom II: Celtyka (wrzosowiska, mgliste doliny, łupkowe wzgórza) i Równina Uthal (pole bitwy)
                new BiomeSpec("wrzosowiska_celtyki", "plains", 0.6f, 0.7f, 0x9a8a6e, 0x7d7a52, 0x8c9aa6, 0x9ea6ac, 0x3a5a6a, false,
                        List.of(s("minecraft:wolf", 2, 2, 3)),
                        List.of(s("zwiadowcy:wargal_zwiadowca", 4, 1, 2)), true),
                new BiomeSpec("mgliste_doliny", "forest", 0.5f, 0.9f, 0x5f8a4e, 0x4a7a3c, 0x8a98a4, 0x8e979c, 0x2f4f5f, false,
                        List.of(), List.of(s("zwiadowcy:wargal_zwiadowca", 4, 1, 2)), true),
                new BiomeSpec("lupkowe_wzgorza", "taiga", 0.4f, 0.7f, 0x748262, 0x5f7050, 0x8c9aa6, 0x9aa2a8, 0x3a5060, false,
                        List.of(), List.of(s("zwiadowcy:wargal_zwiadowca", 4, 1, 2)), true),
                new BiomeSpec("rownina_uthal", "plains", 0.8f, 0.4f, 0x86bb5c, 0x6ea836, 0x78a7ff, 0xc0d8ff, 0x3f76e4, true,
                        List.of(), List.of(), false)
        );

        Set<String> sparseWritten = new HashSet<>();
        try (ZipFile zip = new ZipFile(jar)) {
            File biomeDir = new File(dataRoot, "zwiadowcy/worldgen/biome");
            biomeDir.mkdirs();
            for (BiomeSpec spec : specs) {
                JsonObject b = read(zip, "data/minecraft/worldgen/biome/" + spec.base + ".json");
                b.addProperty("temperature", spec.temperature);
                b.addProperty("downfall", spec.downfall);
                JsonObject eff = b.getAsJsonObject("effects");
                eff.addProperty("grass_color", spec.grass);
                eff.addProperty("foliage_color", spec.foliage);
                eff.addProperty("sky_color", spec.sky);
                eff.addProperty("fog_color", spec.fog);
                eff.addProperty("water_color", spec.water);
                if (!spec.keepMusic) eff.remove("music");
                JsonObject spawners = b.getAsJsonObject("spawners");
                JsonArray monsters = new JsonArray();
                for (Spawn m : spec.monsters) monsters.add(spawn(m));
                spawners.add("monster", monsters);
                JsonArray creatures = spec.base.equals("plains") && spec.id.equals("rownina_samotnosci")
                        ? new JsonArray() : spawners.getAsJsonArray("creature");
                for (Spawn c : spec.extraCreatures) creatures.add(spawn(c));
                spawners.add("creature", creatures);
                JsonArray features = b.getAsJsonArray("features");
                for (JsonElement step : features) {
                    JsonArray arr = step.getAsJsonArray();
                    for (Iterator<JsonElement> it = arr.iterator(); it.hasNext(); ) {
                        String f = it.next().getAsString();
                        if (f.contains("monster_room")) it.remove();
                        else if (spec.dropFlowers && (f.contains("flower") || f.contains("sugar_cane") || f.contains("pumpkin"))) it.remove();
                    }
                    // rzadsze lasy: własne kopie rozmieszczenia drzew z mniejszą liczbą na chunk (wygodna jazda konno)
                    for (int i = 0; i < arr.size(); i++) {
                        String f = arr.get(i).getAsString();
                        if (!f.startsWith("minecraft:") || !SPARSE_TREES.containsKey(f.substring(10))) continue;
                        String name = f.substring(10);
                        arr.set(i, new JsonPrimitive("zwiadowcy:rzadkie/" + name));
                        if (sparseWritten.add(name)) {
                            JsonObject placed = read(zip, "data/minecraft/worldgen/placed_feature/" + name + ".json");
                            scaleCount(placed, SPARSE_TREES.get(name));
                            File pf = new File(dataRoot, "zwiadowcy/worldgen/placed_feature/rzadkie/" + name + ".json");
                            pf.getParentFile().mkdirs();
                            write(pf, placed);
                        }
                    }
                }
                write(new File(biomeDir, spec.id + ".json"), b);
            }

            // ustawienia szumu - reguły powierzchni dla nowych biomów
            String noise = new String(zip.getInputStream(zip.getEntry("data/minecraft/worldgen/noise_settings/overworld.json")).readAllBytes(), StandardCharsets.UTF_8);
            noise = noise.replace("\"minecraft:stony_peaks\"", "\"minecraft:stony_peaks\", \"zwiadowcy:szczyty_deszczu_i_nocy\"")
                    .replace("\"minecraft:windswept_hills\"", "\"minecraft:windswept_hills\", \"zwiadowcy:pogorze_deszczu_i_nocy\"")
                    .replace("\"minecraft:old_growth_spruce_taiga\"", "\"minecraft:old_growth_spruce_taiga\", \"zwiadowcy:stare_bory\"")
                    .replace("\"minecraft:windswept_savanna\"", "\"minecraft:windswept_savanna\", \"zwiadowcy:rownina_samotnosci\"");
            // Araluen bez mórz: własne kopie funkcji gęstości terenu. Parametr generatora, który decyduje o oceanach
            // ("continents"), jest przesunięty w stronę lądu, więc nigdzie nie powstaje ocean ani plaża, a cały teren
            // jest podniesiony o kilka bloków - doliny rzek wysychają. Zostają tylko małe jeziorka z wód gruntowych.
            String[] chain = {"continents", "erosion", "ridges_folded", "offset", "factor", "jaggedness", "depth", "sloped_cheese"};
            File dfDir = new File(dataRoot, "zwiadowcy/worldgen/density_function/araluen");
            dfDir.mkdirs();
            for (String name : chain) {
                JsonObject df = JsonParser.parseString(relink(readText(zip, "data/minecraft/worldgen/density_function/overworld/" + name + ".json"), chain))
                        .getAsJsonObject();
                if (name.equals("continents")) df = inRegions(calmNearHome(towardsLand(df), CALM_CONTINENTS, false), REGION_CONTINENTS);
                if (name.equals("erosion")) df = inRegions(calmNearHome(df, CALM_EROSION_FLOOR, true), REGION_EROSION);
                if (name.equals("offset")) df = regionLift(raised(df));
                if (name.equals("factor")) df = regionFactor(df);
                if (name.equals("ridges_folded")) df = flattenRidges(df);
                write(new File(dfDir, name + ".json"), df);
            }
            noise = relink(noise, chain);
            // niższy poziom wody: doliny rzek i niecki wysychają, zostają tylko małe jeziorka w głębszych zagłębieniach
            noise = noise.replace("\"sea_level\": 63", "\"sea_level\": " + SEA_LEVEL);
            File noiseFile = new File(dataRoot, "zwiadowcy/worldgen/noise_settings/araluen.json");
            noiseFile.getParentFile().mkdirs();
            JsonObject settings = JsonParser.parseString(noise).getAsJsonObject();
            volumeTwo(settings);
            write(noiseFile, settings);
        }
        System.out.println("Biomy Araluenu wygenerowane: " + specs.size());
    }

    /**
     * Rzadsze lasy Araluenu: mnożnik liczby drzew na chunk dla rozmieszczeń vanilla (las 10 → 4, ciemny las 16 → 6).
     * Pogórze (3 drzewa) i łąki zostają bez zmian.
     */
    static final Map<String, Double> SPARSE_TREES = Map.of(
            "trees_birch_and_oak", 0.4, "trees_birch", 0.4, "dark_forest_vegetation", 0.4,
            "trees_taiga", 0.4, "trees_old_growth_spruce_taiga", 0.4);

    /** Mnoży "count" w modyfikatorach rozmieszczenia (liczba albo lista ważona), co najmniej 1. */
    static void scaleCount(JsonObject placed, double factor) {
        for (JsonElement el : placed.getAsJsonArray("placement")) {
            JsonObject mod = el.getAsJsonObject();
            if (!mod.get("type").getAsString().equals("minecraft:count")) continue;
            JsonElement count = mod.get("count");
            if (count.isJsonPrimitive()) {
                mod.addProperty("count", Math.max(1, (int) Math.round(count.getAsInt() * factor)));
            } else if (count.getAsJsonObject().has("distribution")) {
                for (JsonElement d : count.getAsJsonObject().getAsJsonArray("distribution")) {
                    JsonObject entry = d.getAsJsonObject();
                    if (entry.get("data").isJsonPrimitive()) {
                        entry.addProperty("data", Math.max(1, (int) Math.round(entry.get("data").getAsInt() * factor)));
                    }
                }
            }
        }
    }

    /** Przesunięcie terenu w górę (1.0 w "offset" to ok. 128 bloków): +0.12 ≈ 15 bloków, doliny rzek i niecki nad poziomem morza. */
    static final double RAISE = 0.12;
    /** Poziom wody w Araluenie (vanilla: 63). */
    static final int SEA_LEVEL = 50;
    /** Kontynentalność c → 0.40 + 0.35·c: z zakresu szumu ok. [-1.2, 1.2] robi się [-0.02, 0.82] - sam ląd (ocean < -0.19). */
    static final double LAND_SHIFT = 0.40, LAND_SCALE = 0.35;

    /** Odwołania vanilla "minecraft:overworld/x" → własne kopie "zwiadowcy:araluen/x" dla funkcji z łańcucha. */
    static String relink(String json, String[] chain) {
        for (String n : chain) json = json.replace("\"minecraft:overworld/" + n + "\"", "\"zwiadowcy:araluen/" + n + "\"");
        return json;
    }

    /**
     * Spokojna okolica domu: w promieniu CALM_INNER od środka świata teren bez wysokich gór, dalej niż CALM_OUTER -
     * pełne góry (Góry Deszczu i Nocy z trzeciego aktu), płynne przejście pomiędzy.
     */
    static final double CALM_INNER = 700, CALM_OUTER = 1200;
    /** Kontynentalność w okolicy domu: "środek lądu" - bez szczytów. */
    static final double CALM_CONTINENTS = 0.15;
    /** Dolna granica erozji w okolicy domu: niska erozja daje strome płaskowyże i góry. */
    static final double CALM_EROSION_FLOOR = -0.2;

    static JsonObject num(String type, Object a, Object b) {
        JsonObject o = new JsonObject();
        o.addProperty("type", type);
        if (a instanceof Number n) o.addProperty("argument1", n); else o.add("argument1", (JsonElement) a);
        if (b instanceof Number n) o.addProperty("argument2", n); else o.add("argument2", (JsonElement) b);
        return o;
    }

    /**
     * flat_cache(x) → flat_cache(spokój + g·(x - spokój)), gdzie g = 0 blisko domu i 1 daleko.
     * Dla kontynentalności "spokój" to stała; dla erozji ({@code floor}) - max(x, próg), czyli tylko odcięcie niskich wartości.
     */
    static JsonObject calmNearHome(JsonObject flatCache, double calm, boolean floor) {
        JsonElement x = flatCache.get("argument");
        JsonElement quiet = floor ? num("minecraft:max", x.deepCopy(), calm) : new JsonPrimitive(calm);
        JsonObject g = new JsonObject();
        g.addProperty("type", "zwiadowcy:odleglosc_od_srodka");
        g.addProperty("inner", CALM_INNER);
        g.addProperty("outer", CALM_OUTER);
        JsonObject diff = num("minecraft:add", x.deepCopy(), num("minecraft:mul", -1.0, quiet.deepCopy()));
        JsonObject out = new JsonObject();
        out.addProperty("type", "minecraft:flat_cache");
        out.add("argument", num("minecraft:add", quiet, num("minecraft:mul", g, diff)));
        return out;
    }

    /** flat_cache(x) → flat_cache(LAND_SHIFT + LAND_SCALE·x). */
    static JsonObject towardsLand(JsonObject flatCache) {
        JsonObject mul = new JsonObject();
        mul.addProperty("type", "minecraft:mul");
        mul.addProperty("argument1", LAND_SCALE);
        mul.add("argument2", flatCache.get("argument"));
        JsonObject add = new JsonObject();
        add.addProperty("type", "minecraft:add");
        add.addProperty("argument1", LAND_SHIFT);
        add.add("argument2", mul);
        JsonObject out = new JsonObject();
        out.addProperty("type", "minecraft:flat_cache");
        out.add("argument", add);
        return out;
    }

    /** flat_cache(x) → flat_cache(RAISE + x). */
    static JsonObject raised(JsonObject flatCache) {
        JsonObject add = new JsonObject();
        add.addProperty("type", "minecraft:add");
        add.addProperty("argument1", RAISE);
        add.add("argument2", flatCache.get("argument"));
        JsonObject out = new JsonObject();
        out.addProperty("type", "minecraft:flat_cache");
        out.add("argument", add);
        return out;
    }

    // =========================================================================== tom II
    // Krainy tomu II (współrzędne układu - muszą się zgadzać z pl.zwiadowcy.world.VolumeTwoLayout w modzie):
    // Celtyka (dwie elipsy), Przełęcz Wiatrów i trakt wschodni (kapsuły), Równina Uthal (elipsa).
    static final double[][] CELTICA = {{-1900, 400, 600, 450}, {-1400, 1010, 520, 300}};
    static final double[] PASS = {-950, 110, -1500, 230, 100};
    static final double[] ROAD = {-1150, 1060, 320, 1170, 120};
    static final double[] UTHAL = {450, 1330, 320, 230};
    /** Szerokość płynnego przejścia na krawędzi krainy: rdzeń Celtyki, pogranicze, przełęcz, trakt, Uthal. */
    static final double[] REGION_EDGE = {200, 150, 120, 100, 150};
    /**
     * Docelowa kontynentalność i erozja w krainach: wrzosowiska Celtyki (pagórki), pogranicze nad Rozpadliną
     * (wysoki płaskowyż - kanion jest głęboki), przełęcz (siodło), trakt (wzgórza) i Uthal (równina).
     */
    static final double[] REGION_CONTINENTS = {0.2, 0.2, 0.18, 0.15, 0.05};
    static final double[] REGION_EROSION = {0.05, 0.05, 0.0, 0.1, 0.75};
    /** Podniesienie terenu w krainie (1.0 w "offset" to ok. 128 bloków): pogranicze nad Rozpadliną +0.3 ≈ 38 bloków. */
    static final double[] REGION_LIFT = {0, 0.3, 0, 0, 0};
    /** "factor" (im większy, tym płaściej): trakt i Uthal spłaszczone; 0 = bez zmian. */
    static final double[] REGION_FACTOR = {0, 0, 0, 6.0, 14.0};
    /** Wygładzenie grzbietów i dolin (PV → 0) na trakcie i w Uthal. */
    static final double[] REGION_FLAT_RIDGES = {0, 0, 0.5, 1, 1};
    /** Temperatura-znacznik biomów: Celtyka [1.4, 2] i Uthal [-2, -1.4] - poza krainami nie występuje. */
    static final double CELTIC_TEMPERATURE = 1.75, UTHAL_TEMPERATURE = -1.75;
    /** Rozpadlina: półszerokość dna (min-max), dno nad poziomem wody, poszerzenie ścian ku górze. */
    static final double FISSURE_HALF_MIN = 15, FISSURE_HALF_MAX = 25, FISSURE_FLOOR = 53, FISSURE_SLOPE = 0.12;

    static JsonObject region(String shape, double[] a, double edge) {
        JsonObject o = new JsonObject();
        o.addProperty("type", "zwiadowcy:region");
        o.addProperty("shape", shape);
        JsonArray arr = new JsonArray();
        for (double v : a) arr.add(v);
        o.add("a", arr);
        o.addProperty("edge", edge);
        return o;
    }

    static JsonObject celticaMask() {
        JsonObject max = new JsonObject();
        max.addProperty("type", "minecraft:max");
        max.add("argument1", region("elipsa", CELTICA[0], REGION_EDGE[0]));
        max.add("argument2", region("elipsa", CELTICA[1], REGION_EDGE[1]));
        return max;
    }

    static JsonObject[] regionMasks() {
        return new JsonObject[]{region("elipsa", CELTICA[0], REGION_EDGE[0]), region("elipsa", CELTICA[1], REGION_EDGE[1]),
                region("kapsula", PASS, REGION_EDGE[2]), region("kapsula", ROAD, REGION_EDGE[3]), region("elipsa", UTHAL, REGION_EDGE[4])};
    }

    /** x·(1 - m) + c·m - płynne przejście do stałej c w masce m (x występuje raz, więc zagnieżdżenia nie puchną). */
    static JsonElement lerpTo(JsonElement x, JsonElement m, double c) {
        JsonObject oneMinus = num("minecraft:add", 1.0, num("minecraft:mul", -1.0, m.deepCopy()));
        return num("minecraft:add", num("minecraft:mul", x, oneMinus), num("minecraft:mul", m, c));
    }

    /** flat_cache(x) → flat_cache(x przeprowadzone kolejno przez krainy tomu II do wartości docelowych). */
    static JsonObject inRegions(JsonObject flatCache, double[] targets) {
        JsonElement x = flatCache.get("argument");
        JsonObject[] masks = regionMasks();
        for (int i = 0; i < masks.length; i++) x = lerpTo(x, masks[i], targets[i]);
        JsonObject out = new JsonObject();
        out.addProperty("type", "minecraft:flat_cache");
        out.add("argument", x);
        return out;
    }

    /** flat_cache(x) → flat_cache(x + Σ podniesienie·maska) - wysoki płaskowyż nad Rozpadliną (głęboki kanion). */
    static JsonObject regionLift(JsonObject flatCache) {
        JsonElement x = flatCache.get("argument");
        JsonObject[] masks = regionMasks();
        for (int i = 0; i < masks.length; i++) {
            if (REGION_LIFT[i] != 0) x = num("minecraft:add", x, num("minecraft:mul", masks[i], REGION_LIFT[i]));
        }
        JsonObject out = new JsonObject();
        out.addProperty("type", "minecraft:flat_cache");
        out.add("argument", x);
        return out;
    }

    /** flat_cache(x) → flat_cache(x przeprowadzone do stałego "factor" na trakcie i w Uthal). */
    static JsonObject regionFactor(JsonObject flatCache) {
        JsonElement x = flatCache.get("argument");
        JsonObject[] masks = regionMasks();
        for (int i = 0; i < masks.length; i++) {
            if (REGION_FACTOR[i] > 0) x = lerpTo(x, masks[i], REGION_FACTOR[i]);
        }
        JsonObject out = new JsonObject();
        out.addProperty("type", "minecraft:flat_cache");
        out.add("argument", x);
        return out;
    }

    /** ridges_folded (PV) → w krainach z REGION_FLAT_RIDGES > 0 płynnie do 0 („środek” - bez grzbietów i dolin). */
    static JsonObject flattenRidges(JsonObject df) {
        JsonElement x = df;
        JsonObject[] masks = regionMasks();
        for (int i = 0; i < masks.length; i++) {
            if (REGION_FLAT_RIDGES[i] <= 0) continue;
            x = lerpTo(x, num("minecraft:mul", masks[i], REGION_FLAT_RIDGES[i]), 0.0);
        }
        JsonObject out = new JsonObject();
        out.addProperty("type", "minecraft:flat_cache");
        out.add("argument", x);
        return out;
    }

    static JsonObject fissure(String mode) {
        JsonObject o = new JsonObject();
        o.addProperty("type", "zwiadowcy:rozpadlina");
        o.addProperty("mode", mode);
        o.addProperty("half_min", FISSURE_HALF_MIN);
        o.addProperty("half_max", FISSURE_HALF_MAX);
        o.addProperty("floor", FISSURE_FLOOR);
        o.addProperty("slope", FISSURE_SLOPE);
        return o;
    }

    static JsonObject minWith(JsonElement a, JsonObject b) {
        JsonObject o = new JsonObject();
        o.addProperty("type", "minecraft:min");
        o.add("argument1", a);
        o.add("argument2", b);
        return o;
    }

    /**
     * Ustawienia szumu dla tomu II: biomy Celtyki i Uthal przez znacznik temperatury, Rozpadlina wycięta w gęstości
     * terenu (także w gęstości wstępnej - od niej zależy poziom „powierzchni” dla wód gruntowych i darni) i bez zalewania.
     */
    static void volumeTwo(JsonObject settings) {
        JsonObject router = settings.getAsJsonObject("noise_router");
        JsonElement temp = lerpTo(router.get("temperature"), celticaMask(), CELTIC_TEMPERATURE);
        temp = lerpTo(temp, region("elipsa", UTHAL, REGION_EDGE[4]), UTHAL_TEMPERATURE);
        router.add("temperature", temp);
        // zatyczka dna: kilka bloków pod dnem kanionu zawsze litych, żeby jaskinie nie otwierały dziur w dnie
        JsonObject carved = minWith(router.get("final_density"), fissure("teren"));
        JsonObject plugged = new JsonObject();
        plugged.addProperty("type", "minecraft:max");
        plugged.add("argument1", carved);
        plugged.add("argument2", fissure("dno"));
        // zatyczka Uthal: pod równiną bitwy pas y≈45-74 zawsze lity (bez wejść do jaskiń i stawów w polu bitwy);
        // poza Uthal wartość -1, więc maksimum nic nie zmienia
        JsonObject band = num("minecraft:min",
                JsonParser.parseString("{\"type\":\"minecraft:y_clamped_gradient\",\"from_y\":70,\"to_y\":78,\"from_value\":1.0,\"to_value\":-1.0}"),
                JsonParser.parseString("{\"type\":\"minecraft:y_clamped_gradient\",\"from_y\":40,\"to_y\":50,\"from_value\":-1.0,\"to_value\":1.0}"));
        JsonObject uthalPlug = num("minecraft:add", num("minecraft:mul", region("elipsa", UTHAL, REGION_EDGE[4]), num("minecraft:add", band, 1.0)), -1.0);
        router.add("final_density", num("minecraft:max", plugged, uthalPlug));
        router.add("initial_density_without_jaggedness", minWith(router.get("initial_density_without_jaggedness"), fissure("teren")));
        router.add("fluid_level_floodedness", minWith(router.get("fluid_level_floodedness"), fissure("woda")));
        // łupkowe wzgórza: na powierzchni plamy tufu i łupku (głębokiego łupku)
        JsonArray seq = settings.getAsJsonObject("surface_rule").getAsJsonArray("sequence");
        JsonArray rebuilt = new JsonArray();
        rebuilt.add(JsonParser.parseString("""
                {"type": "minecraft:condition",
                 "if_true": {"type": "minecraft:biome", "biome_is": ["zwiadowcy:lupkowe_wzgorza"]},
                 "then_run": {"type": "minecraft:condition",
                   "if_true": {"type": "minecraft:above_preliminary_surface"},
                   "then_run": {"type": "minecraft:condition",
                     "if_true": {"type": "minecraft:stone_depth", "offset": 0, "surface_type": "floor", "add_surface_depth": false, "secondary_depth_range": 0},
                     "then_run": {"type": "minecraft:sequence", "sequence": [
                       {"type": "minecraft:condition", "if_true": {"type": "minecraft:noise_threshold", "noise": "minecraft:surface", "min_threshold": 0.35, "max_threshold": 10.0},
                        "then_run": {"type": "minecraft:block", "result_state": {"Name": "minecraft:deepslate", "Properties": {"axis": "y"}}}},
                       {"type": "minecraft:condition", "if_true": {"type": "minecraft:noise_threshold", "noise": "minecraft:surface", "min_threshold": 0.12, "max_threshold": 10.0},
                        "then_run": {"type": "minecraft:block", "result_state": {"Name": "minecraft:tuff"}}}
                     ]}}}}"""));
        for (JsonElement e : seq) rebuilt.add(e);
        settings.getAsJsonObject("surface_rule").add("sequence", rebuilt);
    }

    static String readText(ZipFile zip, String path) throws Exception {
        ZipEntry e = zip.getEntry(path);
        if (e == null) throw new IllegalStateException("Brak w jarze: " + path);
        return new String(zip.getInputStream(e).readAllBytes(), StandardCharsets.UTF_8);
    }

    static JsonObject spawn(Spawn s) {
        JsonObject o = new JsonObject();
        o.addProperty("type", s.type);
        o.addProperty("weight", s.weight);
        o.addProperty("minCount", s.min);
        o.addProperty("maxCount", s.max);
        return o;
    }

    static JsonObject read(ZipFile zip, String path) throws Exception {
        ZipEntry e = zip.getEntry(path);
        if (e == null) throw new IllegalStateException("Brak w jarze: " + path);
        try (Reader r = new InputStreamReader(zip.getInputStream(e), StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(r).getAsJsonObject();
        }
    }

    static void write(File f, JsonObject o) throws Exception {
        Files.writeString(f.toPath(), GSON.toJson(o), StandardCharsets.UTF_8);
    }

    private BiomeGen() {}
}
