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
                        List.of(s("zwiadowcy:bandyta", 10, 1, 2), s("zwiadowcy:lucznik_bandytow", 6, 1, 1), s("zwiadowcy:wargal", 4, 1, 2)), false),
                new BiomeSpec("bory_polnocy", "taiga", 0.25f, 0.8f, 0x7a9e5a, 0x5f8a50, 0x7da3ff, 0xc0d0e0, 0x3d57d6, true,
                        List.of(s("zwiadowcy:niedzwiedz", 6, 1, 2)),
                        List.of(s("zwiadowcy:wargal_zwiadowca", 6, 1, 2), s("zwiadowcy:wargal", 4, 1, 2)), false),
                new BiomeSpec("stare_bory", "old_growth_spruce_taiga", 0.25f, 0.8f, 0x6a8a4a, 0x557a44, 0x7da3ff, 0xb8c8d8, 0x3d57d6, true,
                        List.of(s("zwiadowcy:niedzwiedz", 8, 1, 2)),
                        List.of(s("zwiadowcy:wargal", 6, 1, 3), s("zwiadowcy:wargal_zwiadowca", 4, 1, 2)), false),
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
                        List.of(s("zwiadowcy:wargal", 8, 1, 3), s("zwiadowcy:wargal_zwiadowca", 6, 1, 2), s("zwiadowcy:wargal_oszczepnik", 4, 1, 2)), true)
        );

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
                }
                write(new File(biomeDir, spec.id + ".json"), b);
            }

            // ustawienia szumu - reguły powierzchni dla nowych biomów
            String noise = new String(zip.getInputStream(zip.getEntry("data/minecraft/worldgen/noise_settings/overworld.json")).readAllBytes(), StandardCharsets.UTF_8);
            noise = noise.replace("\"minecraft:stony_peaks\"", "\"minecraft:stony_peaks\", \"zwiadowcy:szczyty_deszczu_i_nocy\"")
                    .replace("\"minecraft:windswept_hills\"", "\"minecraft:windswept_hills\", \"zwiadowcy:pogorze_deszczu_i_nocy\"")
                    .replace("\"minecraft:old_growth_spruce_taiga\"", "\"minecraft:old_growth_spruce_taiga\", \"zwiadowcy:stare_bory\"")
                    .replace("\"minecraft:windswept_savanna\"", "\"minecraft:windswept_savanna\", \"zwiadowcy:rownina_samotnosci\"");
            File noiseFile = new File(dataRoot, "zwiadowcy/worldgen/noise_settings/araluen.json");
            noiseFile.getParentFile().mkdirs();
            Files.writeString(noiseFile.toPath(), noise, StandardCharsets.UTF_8);
        }
        System.out.println("Biomy Araluenu wygenerowane: " + specs.size());
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
