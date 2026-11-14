package pl.zwiadowcy.assetgen;

/**
 * Lokacje tomu II nad Rozpadliną (akt III, część 1): ukryty obóz na krawędzi kanionu i obóz budowy mostu na północnym
 * brzegu. Sam most nie jest szablonem - buduje go mod w poprzek kanionu (pl.zwiadowcy.bridge.BridgeBuilder).
 */
final class Tom2StructuresBridge {

    static void all() throws Exception {
        krawedz();
        budowaMostu();
    }

    // =========================================================================== KRAWĘDŹ (M9-M11)
    static void krawedz() throws Exception {
        Structure s = new Structure(24, 10, 24, 20101);
        // ukryty obóz w zagłębieniu: ognisko bez dymu, posłania pod skałą, koniowiąz
        Tom2Structures.scoutFire(s, 12, 12);
        for (int x = 3; x <= 9; x++) for (int y = 1; y <= 3; y++) s.set(x, y, 3, Tom2Structures.ROCK[s.rnd.nextInt(3)]);
        s.fill(3, 4, 3, 9, 4, 5, "minecraft:mossy_cobblestone");
        s.bed(4, 1, 5, "north", "green");
        s.bed(6, 1, 5, "north", "brown");
        s.bed(8, 1, 5, "north", "white");
        for (int x = 15; x <= 21; x++) s.set(x, 1, 20, "minecraft:spruce_fence");
        s.set(16, 1, 21, "minecraft:hay_block");
        // wysunięta skała-punkt obserwacyjny (krzaki dla osłony)
        s.fill(17, 1, 4, 21, 1, 8, "minecraft:mossy_cobblestone");
        s.fill(18, 2, 5, 20, 2, 7, "minecraft:stone");
        s.bush(17, 2, 4, "oak");
        s.bush(21, 2, 8, "oak");
        for (int[] b : new int[][]{{2, 18}, {6, 21}, {22, 14}, {1, 10}}) s.bush(b[0], 1, b[1], "oak");
        s.anchor(12, 1, 14, "ognisko");
        s.anchor(19, 3, 6, "punkt_obserwacyjny");
        s.anchor(8, 1, 9, "evanlyn");
        s.anchor(14, 1, 9, "horace");
        s.anchor(10, 1, 19, "punkt_zbiorczy");
        s.anchor(18, 1, 18, "konie");
        StructureGen.save(s, "krawedz");
    }

    // =========================================================================== OBÓZ BUDOWY MOSTU (M9-M11)
    static void budowaMostu() throws Exception {
        Structure s = new Structure(40, 14, 40, 20102);
        for (int x = 0; x < 40; x++) for (int z = 0; z < 40; z++) {
            s.set(x, 0, z, s.pick(new String[]{"minecraft:coarse_dirt", "minecraft:dirt", "minecraft:gravel", "minecraft:mud"},
                    new double[]{0.45, 0.25, 0.2, 0.1}));
        }
        // zagrody jeńców (palisada z krat i bali) z trzema bramkami-kłódkami
        for (int x = 4; x <= 20; x++) {
            s.set(x, 1, 4, "minecraft:spruce_fence");
            s.set(x, 1, 14, "minecraft:spruce_fence");
            s.set(x, 2, 4, "minecraft:spruce_fence");
            s.set(x, 2, 14, "minecraft:spruce_fence");
        }
        for (int z = 4; z <= 14; z++) {
            s.set(4, 1, z, "minecraft:spruce_fence");
            s.set(20, 1, z, "minecraft:spruce_fence");
            s.set(4, 2, z, "minecraft:spruce_fence");
            s.set(20, 2, z, "minecraft:spruce_fence");
        }
        for (int x : new int[]{9, 15}) for (int z = 5; z <= 13; z++) s.set(x, 1, z, "minecraft:iron_bars");
        int[] locks = {6, 12, 18};
        for (int i = 0; i < locks.length; i++) {
            s.set(locks[i], 1, 14, "minecraft:spruce_fence_gate[facing=south,open=false,in_wall=false,powered=false]");
            s.set(locks[i], 2, 14, "minecraft:air");
            s.anchor(locks[i], 1, 16, "klodka_" + (i + 1));
            s.anchor(locks[i], 1, 9, "zagroda_" + (i + 1));
            s.anchorBlock(locks[i], 1, 14, "blok_furtka_" + (i + 1), "minecraft:spruce_fence_gate[facing=south,open=false,in_wall=false,powered=false]");
        }
        // słoma i wiadra w zagrodach, jeńcy
        for (int[] g : new int[][]{{6, 8}, {7, 11}, {11, 7}, {12, 10}, {13, 12}, {17, 7}, {18, 10}, {17, 12}}) {
            s.npc(g[0], 1, g[1], "gornik", s.rnd.nextInt(360));
        }
        for (int x = 5; x <= 19; x += 3) s.set(x, 1, 5, "minecraft:hay_block");
        // skład belek i lin (materiał na most)
        s.fill(24, 1, 4, 34, 1, 5, "minecraft:stripped_spruce_log[axis=x]");
        s.fill(24, 2, 4, 32, 2, 5, "minecraft:stripped_spruce_log[axis=x]");
        s.fill(24, 1, 8, 28, 2, 9, "minecraft:spruce_log[axis=x]");
        for (int x = 30; x <= 34; x += 2) s.set(x, 1, 8, "zwiadowcy:lina");
        // skład smoły: beczki i ciemne bloki
        for (int[] b : new int[][]{{24, 14}, {25, 14}, {24, 15}, {26, 15}, {25, 16}}) s.set(b[0], 1, b[1], "minecraft:barrel[facing=up,open=true]");
        s.set(26, 1, 14, "minecraft:black_concrete_powder");
        s.set(27, 1, 16, "minecraft:coal_block");
        s.set(25, 2, 14, "minecraft:barrel[facing=up,open=false]");
        // wieże doboszy (2) z platformą
        for (int[] t : new int[][]{{2, 30}, {35, 22}}) {
            for (int y = 1; y <= 5; y++) for (int[] c : new int[][]{{0, 0}, {2, 0}, {0, 2}, {2, 2}}) s.set(t[0] + c[0], y, t[1] + c[1], "minecraft:spruce_log[axis=y]");
            s.fill(t[0], 6, t[1], t[0] + 2, 6, t[1] + 2, "minecraft:spruce_planks");
            s.fill(t[0] + 1, 1, t[1] + 1, t[0] + 1, 5, t[1] + 1, "minecraft:spruce_log[axis=y]");
            // belka pod drabiną (drabina musi mieć oparcie)
            for (int y = 1; y <= 5; y++) s.set(t[0] + 1, y, t[1], "minecraft:spruce_log[axis=y]");
            for (int y = 1; y <= 6; y++) s.set(t[0] + 1, y, t[1] - 1, "minecraft:ladder[facing=north,waterlogged=false]");
            s.set(t[0], 7, t[1], "minecraft:note_block[instrument=basedrum,note=0,powered=false]");
        }
        // namioty, ogniska nadzorców
        StructureGen.tent(s, 24, 22, 6, "minecraft:black_wool");
        StructureGen.tent(s, 8, 24, 6, "minecraft:brown_wool");
        s.set(18, 1, 28, "minecraft:campfire[lit=true,facing=north,signal_fire=false]");
        s.set(30, 1, 32, "minecraft:campfire[lit=true,facing=north,signal_fire=false]");
        s.barrel(19, 1, 30, null);
        // brama obozu (od strony traktu na północy) - tu Horace odciąga uwagę
        s.fill(17, 1, 38, 17, 3, 38, "minecraft:spruce_log[axis=y]");
        s.fill(22, 1, 38, 22, 3, 38, "minecraft:spruce_log[axis=y]");
        s.fill(17, 4, 38, 22, 4, 38, "minecraft:spruce_log[axis=x]");
        s.anchor(19, 1, 37, "brama");
        s.anchor(12, 1, 18, "zagroda");
        s.anchor(10, 1, 20, "nadzorca_1");
        s.anchor(16, 1, 19, "nadzorca_2");
        s.anchor(3, 7, 31, "wieza_1");
        s.anchor(36, 7, 23, "wieza_2");
        s.anchor(25, 1, 18, "sklad_smoly");
        s.anchor(31, 1, 12, "kusznik_1");
        s.anchor(6, 1, 34, "kusznik_2");
        int[][] a = {{22, 12}, {34, 12}, {34, 30}, {22, 34}, {12, 34}, {6, 20}};
        for (int i = 0; i < a.length; i++) s.anchor(a[i][0], 1, a[i][1], "trasa_A_" + (i + 1));
        int[][] b = {{2, 2}, {37, 2}, {37, 37}, {2, 37}};
        for (int i = 0; i < b.length; i++) s.anchor(b[i][0], 1, b[i][1], "trasa_B_" + (i + 1));
        StructureGen.save(s, "budowa_mostu");
    }

    private Tom2StructuresBridge() {}
}
