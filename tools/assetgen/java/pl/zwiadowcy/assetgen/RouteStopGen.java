package pl.zwiadowcy.assetgen;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Postoje na szlaku (structures/postoje/): małe scenki przy trakcie na długich trasach kampanii.
 * Każda ma kotwicę "miejsce" (środek traktu) i znacznik postaci; potyczki mają też kotwice wrog_1..3 na skraju scenki.
 */
final class RouteStopGen {

    static void all(File out) throws Exception {
        new File(out, "postoje").mkdirs();
        wozKupca();
        zasadzkaBandytow();
        zasadzkaWargalow();
        rannyDrwal();
    }

    /** Odcinek traktu przecinający scenkę (ubita ziemia z domieszką żwiru i grubej ziemi). */
    static void road(Structure s) {
        s.fillMix(0, 0, 6, 14, 0, 8, new String[]{"minecraft:dirt_path", "minecraft:coarse_dirt", "minecraft:gravel"}, new double[]{0.7, 0.2, 0.1});
    }

    /** Rozsypany towar: przedmioty leżące na ziemi - nie znikają, lekko świecą, HUD wskazuje najbliższy. */
    static void looseItem(Structure s, int x, int z, String item) {
        Map<String, Object> stack = new LinkedHashMap<>();
        stack.put("id", item);
        stack.put("Count", (byte) 1);
        Map<String, Object> forge = new LinkedHashMap<>();
        forge.put("zw_quest_drop", (byte) 1);
        Map<String, Object> nbt = new LinkedHashMap<>();
        nbt.put("id", "minecraft:item");
        nbt.put("Item", stack);
        nbt.put("Age", (short) -32768);
        nbt.put("PickupDelay", (short) 0);
        nbt.put("Glowing", (byte) 1);
        nbt.put("Invulnerable", (byte) 1);
        nbt.put("ForgeData", forge);
        s.entity(x + 0.5, 1.1, z + 0.5, nbt);
    }

    // ------------------------------------------------------------------ porzucony wóz kupca (zbierz i oddaj)
    static void wozKupca() throws Exception {
        Structure s = new Structure(15, 6, 15, 9911);
        road(s);
        // wóz przewrócony na bok: dno pionowo, burta, koło odpadło
        s.fill(5, 1, 4, 8, 2, 4, "minecraft:spruce_planks");
        s.fill(5, 1, 3, 8, 1, 3, "minecraft:spruce_slab[type=bottom]");
        s.set(5, 1, 5, "minecraft:spruce_trapdoor[facing=south,half=bottom,open=true]");
        s.set(8, 2, 5, "minecraft:spruce_trapdoor[facing=south,half=bottom,open=true]");
        s.set(10, 1, 4, "minecraft:spruce_trapdoor[facing=north,half=bottom,open=false]");
        s.set(9, 1, 3, "minecraft:barrel[facing=east,open=false]");
        s.set(4, 1, 3, "minecraft:barrel[facing=up,open=false]");
        s.set(4, 2, 3, "minecraft:barrel[facing=north,open=false]");
        s.set(10, 1, 2, "minecraft:hay_block");
        // ścinki sukna na trawie
        for (int[] c : new int[][]{{7, 10}, {3, 10}, {11, 9}, {2, 5}}) s.set(c[0], 1, c[1], "minecraft:red_carpet");
        s.set(9, 1, 10, "minecraft:yellow_carpet");
        s.npc(4, 1, 7, "kupiec", -90f);
        s.anchor(7, 1, 7, "miejsce");
        for (int[] c : new int[][]{{2, 11}, {11, 12}, {13, 3}, {1, 2}, {9, 13}, {13, 9}}) looseItem(s, c[0], c[1], "zwiadowcy:bela_sukna");
        StructureGen.save(s, "postoje/woz_kupca");
    }

    // ------------------------------------------------------------------ zasadzka bandytów na kupca (potyczka)
    static void zasadzkaBandytow() throws Exception {
        Structure s = new Structure(15, 6, 15, 9912);
        road(s);
        // wóz z płóciennym dachem, zatrzymany w poprzek traktu
        s.fill(5, 1, 6, 9, 1, 7, "minecraft:spruce_slab[type=top]");
        for (int[] p : new int[][]{{5, 6}, {9, 6}, {5, 7}, {9, 7}}) s.set(p[0], 2, p[1], "minecraft:spruce_fence");
        s.fill(5, 3, 6, 9, 3, 7, "minecraft:white_wool");
        s.set(6, 2, 6, "minecraft:barrel[facing=up,open=false]");
        s.set(8, 2, 7, "minecraft:barrel[facing=up,open=false]");
        s.set(4, 1, 6, "minecraft:spruce_trapdoor[facing=west,half=bottom,open=true]");
        s.set(10, 1, 7, "minecraft:spruce_trapdoor[facing=east,half=bottom,open=true]");
        // porozbijane skrzynki i wysypane jabłka (dekoracja)
        s.set(11, 1, 4, "minecraft:barrel[facing=south,open=true]");
        s.set(3, 1, 10, "minecraft:barrel[facing=west,open=false]");
        s.set(12, 1, 9, "minecraft:hay_block");
        s.npc(6, 1, 4, "kupiec", 0f);
        s.anchor(7, 1, 9, "miejsce");
        s.anchor(1, 1, 13, "wrog_1");
        s.anchor(13, 1, 13, "wrog_2");
        s.anchor(13, 1, 1, "wrog_3");
        StructureGen.save(s, "postoje/zasadzka_bandytow");
    }

    // ------------------------------------------------------------------ zasadzka Wargalów na uciekinierów (potyczka)
    static void zasadzkaWargalow() throws Exception {
        Structure s = new Structure(15, 6, 15, 9913);
        road(s);
        // spalony wóz uciekinierów: zwęglone deski, popiół, rozrzucony dobytek
        s.fill(5, 1, 3, 8, 1, 4, "minecraft:dark_oak_slab[type=bottom]");
        s.set(5, 2, 3, "minecraft:dark_oak_fence");
        s.set(8, 2, 4, "minecraft:dark_oak_fence");
        s.set(6, 1, 2, "minecraft:blackstone");
        s.set(9, 1, 3, "minecraft:campfire[lit=false,facing=north,signal_fire=false]");
        for (int[] c : new int[][]{{4, 9}, {6, 10}, {10, 10}, {11, 5}}) s.set(c[0], 1, c[1], c[0] % 2 == 0 ? "minecraft:gray_carpet" : "minecraft:brown_carpet");
        s.set(3, 1, 4, "minecraft:cauldron");
        s.set(10, 1, 11, "minecraft:barrel[facing=north,open=true]");
        // głaz, za którym chowa się uciekinierka
        s.set(12, 1, 2, "minecraft:mossy_cobblestone");
        s.set(13, 1, 2, "minecraft:cobblestone");
        s.set(12, 2, 2, "minecraft:mossy_cobblestone");
        s.npc(12, 1, 3, "uciekinierka", 0f);
        s.anchor(7, 1, 7, "miejsce");
        s.anchor(1, 1, 13, "wrog_1");
        s.anchor(7, 1, 14, "wrog_2");
        s.anchor(13, 1, 12, "wrog_3");
        StructureGen.save(s, "postoje/zasadzka_wargalow");
    }

    // ------------------------------------------------------------------ obozowisko rannego drwala (zbierz zioła i oddaj)
    static void rannyDrwal() throws Exception {
        Structure s = new Structure(15, 6, 15, 9914);
        road(s);
        // szałas z desek na palikach
        for (int[] p : new int[][]{{2, 1}, {6, 1}, {2, 4}, {6, 4}}) s.fill(p[0], 1, p[1], p[0], 2, p[1], "minecraft:spruce_fence");
        s.fill(2, 3, 1, 6, 3, 4, "minecraft:spruce_slab[type=bottom]");
        s.fill(3, 1, 2, 5, 1, 3, "minecraft:brown_carpet");
        // stos drewna, pniaki, pieniek do rąbania z siekierą
        s.fill(9, 1, 1, 12, 1, 2, "minecraft:oak_log[axis=x]");
        s.fill(10, 2, 1, 11, 2, 2, "minecraft:oak_log[axis=x]");
        for (int[] p : new int[][]{{13, 5}, {1, 11}, {11, 12}}) s.set(p[0], 1, p[1], "minecraft:oak_log[axis=y]");
        s.set(8, 1, 4, "minecraft:stripped_oak_log[axis=y]");
        // palenisko z kociołkiem i ławy
        Decor.cookingPot(s, 7, 1, 10);
        s.set(5, 1, 10, "minecraft:spruce_log[axis=z]");
        s.set(9, 1, 10, "minecraft:spruce_log[axis=z]");
        s.npc(4, 1, 6, "drwal", 90f);
        s.anchor(7, 1, 7, "miejsce");
        // rumianek rośnie na skraju polany
        for (int[] c : new int[][]{{1, 9}, {2, 13}, {6, 13}, {12, 13}, {13, 9}, {13, 3}, {0, 4}, {9, 0}}) s.set(c[0], 1, c[1], "minecraft:oxeye_daisy");
        StructureGen.save(s, "postoje/ranny_drwal");
    }

    private RouteStopGen() {}
}
