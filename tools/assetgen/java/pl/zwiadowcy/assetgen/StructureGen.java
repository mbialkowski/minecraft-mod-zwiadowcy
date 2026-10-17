package pl.zwiadowcy.assetgen;

import java.io.File;

/**
 * Generator struktur fabularnych (.nbt). Uruchamianie: gradlew generateStructures
 * Osie: x - wschód, z - południe, y - góra. y=0 to warstwa gruntu, y=1 poziom chodzenia.
 * Znaczniki npc:/anchor:/dummy/tug są interpretowane przez StructurePlacer w modzie.
 */
public final class StructureGen {
    static File out;

    static final String[] STONE = {"minecraft:stone_bricks", "minecraft:mossy_stone_bricks", "minecraft:cracked_stone_bricks"};
    static final double[] STONE_W = {0.82, 0.10, 0.08};
    static final String[] RUIN = {"minecraft:stone_bricks", "minecraft:mossy_stone_bricks", "minecraft:cracked_stone_bricks",
            "minecraft:cobblestone", "minecraft:mossy_cobblestone"};
    static final double[] RUIN_W = {0.3, 0.25, 0.2, 0.12, 0.13};

    public static void main(String[] args) throws Exception {
        out = new File(args.length > 0 ? args[0] : "src/main/resources/data/zwiadowcy/structures");
        System.out.println("Generuję struktury do " + out.getAbsolutePath());
        redmont();
        wensley();
        chataHalta();
        stajniaBoba();
        legowisko();
        obozZlotu();
        spalonaOsada();
        ruinyGorlanu();
        RandomStructures.all(out);
    }

    static void save(Structure s, String name) throws Exception {
        s.save(new File(out, name + ".nbt"));
    }

    static String stairs(String mat, String facing) {
        return "minecraft:" + mat + "_stairs[facing=" + facing + ",half=bottom]";
    }

    static String wallTorch(String facing) {
        return "minecraft:wall_torch[facing=" + facing + "]";
    }

    // =========================================================================== REDMONT
    static void redmont() throws Exception {
        Structure s = new Structure(61, 34, 61, 1001);
        // dziedziniec - ścieżki i plac
        s.fill(29, 0, 22, 31, 0, 54, "minecraft:dirt_path");
        s.fillMix(22, 0, 23, 38, 0, 33, new String[]{"minecraft:cobblestone", "minecraft:stone_bricks", "minecraft:gravel"}, new double[]{0.5, 0.35, 0.15});
        s.fill(19, 0, 30, 28, 0, 31, "minecraft:dirt_path");
        s.fill(32, 0, 30, 43, 0, 31, "minecraft:dirt_path");

        // mury obronne (2 warstwy), chodnik na y=8, blanki y=9
        s.wallsMix(4, 1, 4, 56, 8, 56, STONE, STONE_W);
        s.wallsMix(5, 1, 5, 55, 8, 55, STONE, STONE_W);
        s.walls(4, 0, 4, 56, 0, 56, "minecraft:cobblestone");
        s.walls(5, 0, 5, 55, 0, 55, "minecraft:cobblestone");
        s.battlements(4, 4, 56, 56, 9, "minecraft:stone_bricks");
        for (int i = 8; i <= 52; i += 6) {
            s.set(i, 6, 6, wallTorch("south"));
            s.set(i, 6, 54, wallTorch("north"));
            s.set(6, 6, i, wallTorch("east"));
            s.set(54, 6, i, wallTorch("west"));
        }

        // wieże narożne
        int[][] corners = {{1, 1}, {52, 1}, {1, 52}, {52, 52}};
        for (int[] c : corners) tower(s, c[0], c[1], 8, 13);

        // brama (południe)
        s.wallsMix(24, 1, 52, 36, 11, 58, STONE, STONE_W);
        s.fillMix(24, 1, 52, 36, 11, 58, STONE, STONE_W);
        s.clear(28, 1, 52, 32, 5, 58);
        s.clear(25, 1, 53, 27, 10, 57);
        s.clear(33, 1, 53, 35, 10, 57);
        s.fill(28, 6, 55, 32, 6, 55, "minecraft:iron_bars");
        s.battlements(24, 52, 36, 58, 12, "minecraft:stone_bricks");
        s.fill(28, 0, 52, 32, 0, 58, "minecraft:cobblestone");
        s.set(27, 3, 51, "minecraft:lantern[hanging=false]");
        s.set(33, 3, 51, "minecraft:lantern[hanging=false]");
        s.set(27, 1, 51, "minecraft:cobblestone_wall");
        s.set(27, 2, 51, "minecraft:cobblestone_wall");
        s.set(33, 1, 51, "minecraft:cobblestone_wall");
        s.set(33, 2, 51, "minecraft:cobblestone_wall");

        // Wielka sala
        greatHall(s);
        // Wieża barona z gabinetem
        baronTower(s);
        // Szkoła Rycerska (koszary) - zachód
        building(s, 7, 26, 18, 38, 5, "minecraft:cobblestone", "spruce", "minecraft:spruce_planks", true);
        s.door(18, 1, 32, "east", "spruce");
        s.chest(9, 1, 28, "south", "zwiadowcy:chests/redmont_zbrojownia");
        for (int z = 28; z <= 36; z += 4) s.bed(16, 1, z, "west", "blue");
        s.set(20, 1, 27, "minecraft:hay_block");
        s.set(21, 1, 27, "zwiadowcy:tarcza_lucznicza");
        s.set(21, 2, 27, "zwiadowcy:tarcza_lucznicza");
        s.set(20, 1, 37, "minecraft:hay_block");
        s.set(20, 2, 37, "minecraft:hay_block");
        // koszary: broń na ścianie, zbroje na stojakach, stoły, skrzynie w nogach łóżek
        for (int z : new int[]{29, 31, 33, 35}) Decor.itemFrame(s, 8, 3, z, "east", z == 33 ? "minecraft:shield" : "minecraft:iron_sword");
        Decor.armorStand(s, 9, 1, 30, -90f, "minecraft:iron_boots", "minecraft:iron_leggings", "minecraft:iron_chestplate", "minecraft:iron_helmet");
        Decor.armorStand(s, 9, 1, 34, -90f, "minecraft:chainmail_boots", "minecraft:chainmail_leggings", "minecraft:chainmail_chestplate", "minecraft:chainmail_helmet");
        Decor.tableSet(s, 12, 1, 29, "spruce", "minecraft:candle[candles=2,lit=true]");
        Decor.tableSet(s, 12, 1, 35, "spruce", "minecraft:candle[candles=2,lit=true]");
        // skrzynie w nogach łóżek (bez środkowej - stoi tam wejście z dziedzińca)
        for (int z : new int[]{28, 36}) s.chest(17, 1, z, "west", "zwiadowcy:chests/redmont_zbrojownia");
        s.anchor(16, 1, 30, "koszary");
        Decor.rug(s, 11, 31, 13, 33, 1, "gray", "light_gray");
        Decor.painting(s, 10, 3, 37, "north", "courbet");
        // kuchnia - wschód
        building(s, 44, 30, 53, 40, 5, "minecraft:stone_bricks", "dark_oak", "minecraft:dark_oak_planks", false);
        s.door(44, 1, 35, "west", "oak");
        s.chest(52, 1, 31, "west", "zwiadowcy:chests/redmont_kuchnia");
        s.set(52, 1, 34, "minecraft:smoker[facing=west,lit=false]");
        s.set(52, 1, 35, "minecraft:furnace[facing=west,lit=false]");
        s.set(52, 1, 36, "minecraft:water_cauldron[level=3]");
        s.set(48, 1, 33, "minecraft:crafting_table");
        s.barrel(46, 1, 39, null);
        s.barrel(47, 1, 39, null);
        // kuchnia: wielkie palenisko, kocioł nad ogniem, stół, zioła na półkach
        Decor.fireplace(s, 49, 1, 31, "south", 12, "minecraft:bricks");
        Decor.cookingPot(s, 46, 1, 32);
        Decor.tableSet(s, 48, 1, 37, "dark_oak", "minecraft:candle[candles=3,lit=true]");
        s.set(46, 2, 39, "minecraft:cake");
        s.set(47, 2, 39, "minecraft:potted_red_mushroom");
        Decor.shelf(s, 45, 3, 32, "spruce", "minecraft:potted_fern");
        Decor.shelf(s, 45, 3, 38, "spruce", "minecraft:potted_brown_mushroom");
        s.set(45, 1, 38, "minecraft:composter[level=4]");
        s.set(51, 1, 39, "minecraft:hay_block");
        Decor.painting(s, 51, 3, 39, "north", "kebab");
        // stajnie - południowy zachód
        s.fill(7, 4, 43, 17, 4, 50, "minecraft:spruce_slab[type=bottom]");
        for (int x = 7; x <= 17; x += 5) { s.fill(x, 1, 43, x, 3, 43, "minecraft:spruce_fence"); s.fill(x, 1, 50, x, 3, 50, "minecraft:spruce_fence"); }
        s.fill(7, 1, 44, 7, 1, 49, "minecraft:spruce_fence");
        s.set(9, 1, 45, "minecraft:hay_block"); s.set(9, 2, 45, "minecraft:hay_block"); s.set(14, 1, 48, "minecraft:hay_block");
        s.set(16, 1, 45, "minecraft:water_cauldron[level=3]");
        // studnia (bluszcz porasta wieżę od strony studni)
        s.set(39, 0, 27, "minecraft:water");
        for (int x = 38; x <= 40; x++) for (int z = 26; z <= 28; z++) if (!(x == 39 && z == 27)) s.set(x, 1, z, "minecraft:cobblestone");
        s.fill(38, 2, 26, 38, 3, 26, "minecraft:oak_fence");
        s.fill(40, 2, 28, 40, 3, 28, "minecraft:oak_fence");
        s.fill(38, 2, 28, 38, 3, 28, "minecraft:oak_fence");
        s.fill(40, 2, 26, 40, 3, 26, "minecraft:oak_fence");
        s.fill(38, 4, 26, 40, 4, 28, "minecraft:oak_slab[type=bottom]");
        // latarnie na placu
        for (int[] l : new int[][]{{24, 24}, {24, 33}, {21, 40}, {28, 46}, {33, 46}}) {
            s.set(l[0], 1, l[1], "minecraft:oak_fence");
            s.set(l[0], 2, l[1], "minecraft:oak_fence");
            s.set(l[0], 3, l[1], "minecraft:lantern[hanging=false]");
        }
        // rabaty kwiatowe, ławki i kram na dziedzińcu
        String[] flowers = {"minecraft:poppy", "minecraft:cornflower", "minecraft:oxeye_daisy", "minecraft:allium", "minecraft:red_tulip", "minecraft:dandelion"};
        for (int x = 25; x <= 31; x++) s.set(x, 1, 20, flowers[x % flowers.length]);
        for (int x = 15; x <= 20; x++) s.set(x, 1, 20, flowers[(x + 3) % flowers.length]);
        for (int x : new int[]{26, 27, 32, 33}) Decor.chair(s, x, 1, 21, "dark_oak", "south");
        stall(s, 38, 36, "minecraft:red_wool");
        s.set(39, 2, 36, "minecraft:cake");
        s.set(16, 1, 23, "minecraft:barrel[facing=up,open=false]");
        s.set(17, 1, 23, "minecraft:barrel[facing=up,open=false]");
        smithy(s, 18, 41, 24, 46, "zwiadowcy:chests/redmont_zbrojownia");
        s.npc(23, 1, 40, "kowal", 180f);
        // archiwum mistrza Nigela
        building(s, 45, 9, 51, 14, 4, "minecraft:stone_bricks", "dark_oak", "minecraft:dark_oak_planks", true);
        s.door(48, 1, 14, "south", "dark_oak");
        s.fill(46, 1, 10, 50, 3, 10, "minecraft:bookshelf");
        s.set(46, 1, 12, "minecraft:lectern[facing=east,has_book=false,powered=false]");
        s.set(49, 1, 12, "minecraft:dark_oak_planks");
        s.set(49, 2, 12, "minecraft:candle[candles=3,lit=false]");
        s.set(48, 1, 12, stairs("dark_oak", "east"));
        Decor.rug(s, 46, 11, 50, 13, 1, "blue", "light_blue");
        Decor.painting(s, 50, 3, 13, "west", "alban");
        Decor.itemFrame(s, 47, 3, 13, "north", "minecraft:writable_book");
        s.set(50, 1, 13, "minecraft:potted_azure_bluet");
        // schody na mur północny (chodnik obronny)
        for (int i = 0; i < 8; i++) {
            int x = 34 + i;
            if (i > 0) s.fill(x, 1, 6, x, i, 7, "minecraft:stone_bricks");
            s.set(x, 1 + i, 6, "minecraft:stone_brick_stairs[facing=east,half=bottom]");
            s.set(x, 1 + i, 7, "minecraft:stone_brick_stairs[facing=east,half=bottom]");
        }

        // postacie i punkty
        s.anchor(30, 1, 48, "przybycie");
        s.anchor(30, 1, 31, "dziedziniec");
        s.anchor(30, 1, 28, "ceremonia");
        s.anchor(30, 1, 24, "krol");
        s.anchor(26, 1, 25, "halt_ceremonia");
        s.anchor(34, 1, 25, "baron_ceremonia");
        s.anchor(24, 1, 27, "gilan_ceremonia");
        s.anchor(36, 1, 27, "rodney_ceremonia");
        s.anchor(49, 1, 22, "start_wspinaczki");
        s.npc(30, 1, 26, "baron_arald", 0f);
        s.npc(21, 1, 31, "sir_rodney", -90f);
        s.npc(26, 1, 29, "mistrz_nigel", 0f);
        s.npc(42, 1, 35, "mistrz_chubb", 90f);
        s.npc(34, 1, 29, "lady_pauline", 0f);
        s.npc(14, 1, 21, "halt", -30f);
        s.npc(23, 1, 36, "horace", 0f);
        s.npc(35, 1, 34, "alyss", 45f);
        s.npc(43, 1, 38, "jenny", 90f);
        s.npc(34, 1, 18, "straznik", 90f);
        s.npc(36, 1, 31, "straznik", 180f);
        s.npc(27, 1, 50, "straznik", 180f);
        s.npc(12, 1, 40, "straznik", -90f);
        save(s, "redmont");
    }

    /** Otwarta kuźnia: palenisko, kowadło, szlifierka, piec hutniczy. Front od strony z0. */
    static void smithy(Structure s, int x0, int z0, int x1, int z1, String loot) {
        s.fill(x0, 0, z0, x1, 0, z1, "minecraft:cobblestone");
        for (int y = 1; y <= 4; y++) {
            for (int x = x0; x <= x1; x++) s.set(x, y, z1, "minecraft:stone_bricks");
            for (int z = z0; z <= z1; z++) { s.set(x0, y, z, "minecraft:stone_bricks"); s.set(x1, y, z, "minecraft:stone_bricks"); }
        }
        for (int x = x0; x <= x1; x += Math.max(1, x1 - x0)) s.fill(x, 1, z0, x, 4, z0, "minecraft:spruce_log[axis=y]");
        s.clear(x0 + 1, 1, z0, x1 - 1, 4, z0);
        s.fill(x0, 5, z0, x1, 5, z1, "minecraft:stone_brick_slab[type=bottom]");
        s.set(x1 - 1, 1, z1 - 1, "minecraft:campfire[lit=true,facing=north,signal_fire=false]");
        s.set(x1 - 1, 2, z1 - 1, "minecraft:cobblestone");
        s.fill(x1 - 1, 3, z1 - 1, x1 - 1, 6, z1 - 1, "minecraft:bricks");
        s.set(x0 + 2, 1, z0 + 2, "minecraft:anvil[facing=east]");
        s.set(x0 + 1, 1, z1 - 1, "minecraft:grindstone[face=floor,facing=east]");
        s.set(x0 + 1, 1, z0 + 2, "minecraft:blast_furnace[facing=east,lit=false]");
        s.set(x1 - 2, 1, z1 - 1, "minecraft:water_cauldron[level=3]");
        s.chest(x0 + 3, 1, z1 - 1, "north", loot);
        s.barrel(x1 - 1, 1, z0 + 1, null);
        s.set(x0 + 3, 4, z0 + 2, "minecraft:lantern[hanging=true]");
        // narzędzia na tylnej ścianie i kolczuga na stojaku
        Decor.itemFrame(s, x0 + 1, 3, z1 - 1, "north", "minecraft:iron_pickaxe");
        Decor.itemFrame(s, x0 + 2, 3, z1 - 1, "north", "minecraft:iron_axe");
        Decor.itemFrame(s, x0 + 3, 3, z1 - 1, "north", "minecraft:iron_sword");
        Decor.armorStand(s, x1 - 2, 1, z0 + 3, 180f, null, null, "minecraft:chainmail_chestplate", "minecraft:iron_helmet");
        s.set(x1 - 1, 2, z0 + 1, "minecraft:iron_block");
    }

    static void tower(Structure s, int x0, int z0, int size, int height) {
        int x1 = x0 + size - 1, z1 = z0 + size - 1;
        s.fill(x0, 0, z0, x1, 0, z1, "minecraft:cobblestone");
        s.wallsMix(x0, 1, z0, x1, height, z1, STONE, STONE_W);
        s.clear(x0 + 1, 1, z0 + 1, x1 - 1, height - 1, z1 - 1);
        s.fill(x0 + 1, height, z0 + 1, x1 - 1, height, z1 - 1, "minecraft:spruce_planks");
        s.fill(x0 + 1, 6, z0 + 1, x1 - 1, 6, z1 - 1, "minecraft:spruce_planks");
        s.battlements(x0, z0, x1, z1, height + 1, "minecraft:stone_bricks");
        s.set(x0 + size / 2, 3, z0, "minecraft:air");
        s.set(x0 + size / 2, 3, z1, "minecraft:air");
        s.set(x0, 9, z0 + size / 2, "minecraft:air");
        s.set(x1, 9, z0 + size / 2, "minecraft:air");
        s.set(x0 + size / 2, height + 1, z0 + size / 2, "minecraft:campfire[lit=true,facing=north,signal_fire=false]");
    }

    /** Prosty budynek: ściany, podłoga, dach dwuspadowy, okna. */
    static void building(Structure s, int x0, int z0, int x1, int z1, int wallH, String wall, String roofMat, String gable, boolean roofAlongX) {
        s.fill(x0, 0, z0, x1, 0, z1, "minecraft:cobblestone");
        s.fill(x0 + 1, 0, z0 + 1, x1 - 1, 0, z1 - 1, "minecraft:spruce_planks");
        s.walls(x0, 1, z0, x1, wallH, z1, wall);
        for (int y = 1; y <= wallH; y++) {
            s.set(x0, y, z0, "minecraft:spruce_log[axis=y]");
            s.set(x1, y, z0, "minecraft:spruce_log[axis=y]");
            s.set(x0, y, z1, "minecraft:spruce_log[axis=y]");
            s.set(x1, y, z1, "minecraft:spruce_log[axis=y]");
        }
        for (int x = x0 + 2; x < x1 - 1; x += 3) {
            s.set(x, 2, z0, "minecraft:glass_pane");
            s.set(x, 2, z1, "minecraft:glass_pane");
        }
        for (int z = z0 + 2; z < z1 - 1; z += 3) {
            s.set(x0, 2, z, "minecraft:glass_pane");
            s.set(x1, 2, z, "minecraft:glass_pane");
        }
        s.gableRoof(x0, z0, x1, z1, wallH + 1, roofMat, gable, roofAlongX);
        for (int x = x0 + 1; x <= x1 - 1; x++) s.set(x, wallH, (z0 + z1) / 2, "minecraft:stripped_spruce_log[axis=x]");
        s.set((x0 + x1) / 2, wallH - 1, (z0 + z1) / 2, "minecraft:lantern[hanging=true]");
    }

    static void greatHall(Structure s) {
        int x0 = 12, z0 = 7, x1 = 32, z1 = 19;
        s.fill(x0, 0, z0, x1, 0, z1, "minecraft:stone_bricks");
        s.fill(x0 + 1, 0, z0 + 1, x1 - 1, 0, z1 - 1, "minecraft:spruce_planks");
        s.wallsMix(x0, 1, z0, x1, 7, z1, STONE, STONE_W);
        for (int x = x0; x <= x1; x += 4) {
            for (int y = 1; y <= 7; y++) { s.set(x, y, z0, "minecraft:dark_oak_log[axis=y]"); s.set(x, y, z1, "minecraft:dark_oak_log[axis=y]"); }
        }
        for (int x = x0 + 2; x < x1; x += 4) {
            s.fill(x, 3, z0, x, 5, z0, "minecraft:glass_pane");
            s.fill(x, 3, z1, x, 5, z1, "minecraft:glass_pane");
        }
        s.gableRoof(x0, z0, x1, z1, 8, "dark_oak", "minecraft:stone_bricks", true);
        s.door(22, 1, z1, "south", "dark_oak");
        s.door(23, 1, z1, "south", "dark_oak");
        // podest i tron
        s.fill(19, 1, 8, 26, 1, 10, "minecraft:polished_andesite");
        s.set(22, 2, 9, stairs("dark_oak", "north"));
        s.set(23, 2, 9, stairs("dark_oak", "north"));
        s.fill(22, 1, 11, 23, 1, 18, "minecraft:red_carpet");
        for (int x = 14; x <= 31; x++) {
            if (x >= 20 && x <= 25) continue;
            s.set(x, 1, 13, "minecraft:spruce_fence");
            s.set(x, 2, 13, "minecraft:spruce_pressure_plate");
            s.set(x, 1, 14, stairs("spruce", "south"));
            s.set(x, 1, 12, stairs("spruce", "north"));
        }
        for (int x : new int[]{16, 20, 24, 28}) s.set(x, 4, 18, wallTorch("north"));
        s.set(20, 4, 8, "minecraft:red_wall_banner[facing=south]");
        s.set(25, 4, 8, "minecraft:red_wall_banner[facing=south]");
        s.set(20, 6, 18, "minecraft:red_wall_banner[facing=north]");
        s.set(24, 6, 18, "minecraft:red_wall_banner[facing=north]");
        // kominki na filarach północnej ściany (między oknami), komin przebija dach
        Decor.fireplace(s, 16, 1, 8, "south", 11, "minecraft:bricks");
        Decor.fireplace(s, 28, 1, 8, "south", 11, "minecraft:bricks");
        Decor.painting(s, 16, 5, 9, "south", "graham");
        Decor.painting(s, 28, 5, 9, "south", "wanderer");
        Decor.rug(s, 14, 10, 18, 11, 1, "red", "orange");
        Decor.rug(s, 27, 10, 30, 11, 1, "red", "orange");
        // żyrandole nad stołami
        for (int x : new int[]{17, 22, 27}) Decor.chandelier(s, x, 13, 13, 8, false);
        // świece na długich stołach
        for (int x : new int[]{15, 18, 27, 30}) s.set(x, 2, 13, "minecraft:candle[candles=3,lit=true]");
        s.set(16, 2, 13, "minecraft:candle[candles=1,lit=true]");
        s.set(29, 2, 13, "minecraft:candle[candles=1,lit=true]");
        // portrety na południowej ścianie
        Decor.painting(s, 15, 5, 18, "north", "graham");
        Decor.painting(s, 29, 5, 18, "north", "wanderer");
        Decor.painting(s, 31, 5, 18, "north", "plant");
        Decor.painting(s, 13, 5, 18, "north", "alban");
        // podest: dywan pod tronem
        s.fill(21, 2, 10, 24, 2, 10, "minecraft:purple_carpet");
    }

    static void baronTower(Structure s) {
        int x0 = 36, z0 = 14, x1 = 44, z1 = 22, top = 24;
        s.fill(x0, 0, z0, x1, 0, z1, "minecraft:cobblestone");
        s.wallsMix(x0, 1, z0, x1, top, z1, STONE, STONE_W);
        s.clear(x0 + 1, 1, z0 + 1, x1 - 1, top - 1, z1 - 1);
        s.fill(x0 + 1, 0, z0 + 1, x1 - 1, 0, z1 - 1, "minecraft:spruce_planks");
        for (int y : new int[]{7, 13, 18, top}) s.fill(x0 + 1, y, z0 + 1, x1 - 1, y, z1 - 1, "minecraft:spruce_planks");
        s.ironDoor(x0, 1, 18, "west");
        s.set(x0 - 1, 3, 17, wallTorch("west"));
        s.barrel(x0 + 1, 1, z0 + 1, null);
        s.barrel(x0 + 2, 1, z0 + 1, null);
        s.pyramidRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, top + 1, "dark_oak");
        // wąskie okna
        for (int y : new int[]{4, 10, 16}) {
            s.set(40, y, z0, "minecraft:glass_pane");
            s.set(x1, y, 18, "minecraft:glass_pane");
        }
        // gabinet barona (y 19..23)
        s.fill(37, 19, 15, 43, 21, 15, "minecraft:bookshelf");
        s.fill(37, 19, 15, 37, 20, 17, "minecraft:bookshelf");
        s.fill(39, 19, 16, 41, 19, 16, "minecraft:dark_oak_planks");
        s.set(40, 20, 16, "zwiadowcy:papiery_barona");
        s.set(39, 20, 16, "minecraft:candle[candles=2,lit=false]");
        s.set(40, 19, 17, stairs("dark_oak", "south"));
        Decor.rug(s, 38, 18, 42, 20, 19, "red", "orange");
        Decor.painting(s, 37, 21, 19, "east", "graham");
        Decor.pot(s, 41, 20, 16, "red_tulip");
        Decor.itemFrame(s, 42, 21, 21, "north", "minecraft:map");
        Decor.itemFrame(s, 38, 21, 21, "north", "minecraft:writable_book");
        s.set(40, 23, 18, "minecraft:lantern[hanging=true]");
        s.chest(42, 19, 21, "west", "zwiadowcy:chests/redmont_kuchnia");
        s.set(43, 21, 19, "minecraft:red_wall_banner[facing=west]");
        // okno od południa, przez które wchodzi Will
        s.set(40, 20, z1, "minecraft:air");
        s.set(40, 21, z1, "minecraft:air");
        s.set(40, 19, z1, "minecraft:stone_bricks");
        // bluszcz na południowej ścianie
        for (int y = 1; y <= 19; y++) s.set(40, y, z1 + 1, "minecraft:vine[north=true]");
        for (int y = 2; y <= 17; y++) s.set(39, y, z1 + 1, "minecraft:vine[north=true]");
        for (int y = 4; y <= 19; y++) s.set(41, y, z1 + 1, "minecraft:vine[north=true]");
        for (int y = 6; y <= 14; y++) s.set(42, y, z1 + 1, "minecraft:vine[north=true]");
        s.anchor(40, 19, 19, "gabinet");
        s.anchor(38, 19, 21, "gabinet_halt");
        s.anchor(42, 19, 20, "gabinet_baron");
    }

    // =========================================================================== WENSLEY
    static void wensley() throws Exception {
        Structure s = new Structure(48, 16, 48, 2002);
        s.fillMix(15, 0, 15, 33, 0, 33, new String[]{"minecraft:cobblestone", "minecraft:dirt_path", "minecraft:gravel", "minecraft:mossy_cobblestone"},
                new double[]{0.45, 0.3, 0.1, 0.15});
        s.fill(23, 0, 3, 25, 0, 47, "minecraft:dirt_path");
        s.fill(0, 0, 23, 47, 0, 25, "minecraft:dirt_path");
        // studnia
        s.set(24, 0, 24, "minecraft:water");
        for (int x = 23; x <= 25; x++) for (int z = 23; z <= 25; z++) if (!(x == 24 && z == 24)) s.set(x, 1, z, "minecraft:cobblestone");
        for (int[] p : new int[][]{{23, 23}, {25, 23}, {23, 25}, {25, 25}}) s.fill(p[0], 2, p[1], p[0], 3, p[1], "minecraft:oak_fence");
        s.fill(23, 4, 23, 25, 4, 25, "minecraft:spruce_slab[type=bottom]");

        // karczma (piętrowa) i zaułek
        house(s, 15, 3, 27, 11, 7, "south", "dark_oak");
        // izba karczmy: zamiast domowego umeblowania - szynkwas, beczki, stoły, kominek
        s.clear(16, 1, 4, 26, 3, 10);
        Decor.fireplace(s, 21, 1, 4, "south", 16, "minecraft:bricks");
        s.fill(16, 4, 4, 26, 4, 10, "minecraft:spruce_planks");
        s.set(21, 4, 4, "minecraft:bricks");
        for (int x = 16; x <= 26; x++) s.set(x, 4, 7, "minecraft:stripped_dark_oak_log[axis=x]");
        s.set(21, 3, 12, "minecraft:oak_wall_sign[facing=south]");
        // szynkwas w kształcie L i beczki z piwem pod wschodnią ścianą
        for (int z = 4; z <= 7; z++) s.set(23, 1, z, "minecraft:stripped_spruce_log[axis=z]");
        s.set(24, 1, 7, "minecraft:stripped_spruce_log[axis=x]");
        s.set(25, 1, 7, "minecraft:stripped_spruce_log[axis=x]");
        s.set(23, 2, 5, "minecraft:candle[candles=2,lit=true]");
        s.set(23, 2, 7, "minecraft:potted_red_mushroom");
        s.set(24, 2, 7, "minecraft:brewing_stand[has_bottle_0=true,has_bottle_1=true,has_bottle_2=false]");
        Decor.kegs(s, 26, 1, 4, 3, false, "west");
        Decor.kegs(s, 26, 2, 4, 3, false, "west");
        Decor.itemFrame(s, 26, 3, 5, "west", "minecraft:honey_bottle");
        Decor.itemFrame(s, 25, 3, 4, "south", "minecraft:glass_bottle");
        s.npc(24, 1, 5, "karczmarz", 90f);
        for (int z = 6; z <= 7; z++) Decor.chair(s, 22, 1, z, "dark_oak", "east");
        Decor.chair(s, 24, 1, 8, "dark_oak", "north");
        Decor.chair(s, 25, 1, 8, "dark_oak", "north");
        // stoły ze świecami, dywan przed kominkiem
        Decor.tableSet(s, 17, 1, 6, "dark_oak", "minecraft:candle[candles=3,lit=true]");
        Decor.tableSet(s, 19, 1, 9, "dark_oak", "minecraft:candle[candles=1,lit=true]");
        Decor.rug(s, 20, 6, 22, 8, 1, "red", "orange");
        for (int[] l : new int[][]{{17, 6}, {19, 9}, {24, 5}}) s.set(l[0], 3, l[1], "minecraft:lantern[hanging=true]");
        Decor.painting(s, 16, 3, 9, "east", "sunset");
        Decor.painting(s, 18, 3, 4, "south", "pool");
        Decor.itemFrame(s, 16, 2, 4, "south", "minecraft:cooked_chicken");
        s.set(26, 1, 10, "minecraft:barrel[facing=up,open=false]");
        s.set(26, 2, 10, "minecraft:potted_fern");
        s.set(16, 1, 10, "minecraft:barrel[facing=up,open=false]");
        house(s, 31, 3, 38, 10, 4, "south", "spruce");
        s.fillMix(28, 0, 3, 30, 0, 11, new String[]{"minecraft:coarse_dirt", "minecraft:cobblestone", "minecraft:mud"}, new double[]{0.5, 0.3, 0.2});
        s.barrel(28, 1, 3, null);
        s.barrel(28, 2, 3, null);
        s.set(30, 1, 4, "minecraft:cobweb");
        s.anchor(29, 1, 7, "zaulek");
        s.npc(29, 1, 5, "alda", 0f);
        s.npc(28, 1, 8, "bryn", -45f);
        s.npc(30, 1, 9, "jerome", 45f);

        house(s, 3, 14, 10, 20, 4, "east", "spruce");
        house(s, 3, 28, 10, 34, 4, "east", "oak");
        house(s, 14, 38, 21, 44, 4, "north", "spruce");
        house(s, 27, 38, 34, 44, 4, "north", "dark_oak");
        house(s, 38, 15, 45, 21, 4, "west", "oak");
        house(s, 38, 28, 45, 34, 4, "west", "spruce");

        // kramy jarmarczne
        stall(s, 17, 17, "minecraft:red_wool");
        stall(s, 29, 17, "minecraft:yellow_wool");
        stall(s, 17, 29, "minecraft:white_wool");
        stall(s, 29, 29, "minecraft:lime_wool");
        // pole pszenicy
        for (int x = 2; x <= 11; x++) for (int z = 38; z <= 45; z++) {
            if (x == 6) { s.set(x, 0, z, "minecraft:water"); continue; }
            s.set(x, 0, z, "minecraft:farmland[moisture=7]");
            s.set(x, 1, z, "minecraft:wheat[age=" + (5 + s.rnd.nextInt(3)) + "]");
        }
        for (int[] l : new int[][]{{16, 16}, {32, 16}, {16, 32}, {32, 32}}) {
            s.fill(l[0], 1, l[1], l[0], 2, l[1], "minecraft:oak_fence");
            s.set(l[0], 3, l[1], "minecraft:lantern[hanging=false]");
        }
        s.set(36, 1, 25, "minecraft:hay_block"); s.set(36, 1, 26, "minecraft:hay_block"); s.set(36, 2, 25, "minecraft:hay_block");
        s.set(12, 1, 22, "minecraft:pumpkin"); s.set(13, 1, 22, "minecraft:carved_pumpkin[facing=south]");
        // kuźnia
        smithy(s, 38, 38, 44, 44, "zwiadowcy:chests/redmont_zbrojownia");
        s.npc(41, 1, 37, "kowal", 180f);
        // wiatrak (północny zachód)
        s.fill(4, 0, 4, 8, 0, 8, "minecraft:cobblestone");
        s.walls(4, 1, 4, 8, 2, 8, "minecraft:cobblestone");
        s.walls(4, 3, 4, 8, 8, 8, "minecraft:spruce_planks");
        for (int y = 1; y <= 8; y++) for (int[] p : new int[][]{{4, 4}, {8, 4}, {4, 8}, {8, 8}}) s.set(p[0], y, p[1], "minecraft:stripped_spruce_log[axis=y]");
        s.pyramidRoof(4, 4, 8, 8, 9, "spruce");
        s.door(8, 1, 6, "east", "spruce");
        s.set(6, 1, 6, "minecraft:hay_block"); s.set(5, 1, 7, "minecraft:barrel[facing=up,open=false]");
        s.set(6, 6, 3, "minecraft:spruce_log[axis=z]");
        for (int d = 1; d <= 4; d++) {
            s.set(6 + d, 6, 3, "minecraft:spruce_fence");
            s.set(6 - d, 6, 3, "minecraft:spruce_fence");
            s.set(6, 6 + d, 3, "minecraft:spruce_fence");
            s.set(6, 6 - d, 3, "minecraft:spruce_fence");
            s.set(6 + d, 7, 3, "minecraft:white_wool");
            s.set(6 - d, 5, 3, "minecraft:white_wool");
            s.set(5, 6 + d, 3, "minecraft:white_wool");
            s.set(7, 6 - d, 3, "minecraft:white_wool");
        }
        s.npc(12, 1, 40, "chlopka", 90f);
        for (int[] f : new int[][]{{13, 26}, {13, 27}, {35, 20}, {35, 21}, {22, 34}}) s.set(f[0], 1, f[1], "minecraft:flower_pot");

        s.anchor(24, 1, 20, "jarmark");
        s.anchor(22, 1, 21, "horace");
        s.npc(18, 1, 19, "wiesniak", 0f);
        s.npc(30, 1, 19, "wiesniak", 0f);
        s.npc(18, 1, 31, "wiesniak", 180f);
        save(s, "wensley");
    }

    /** Dom z muru pruskiego: podmurówka, belki, tynk, dach dwuspadowy. */
    static void house(Structure s, int x0, int z0, int x1, int z1, int wallH, String doorSide, String roofMat) {
        s.fill(x0, 0, z0, x1, 0, z1, "minecraft:cobblestone");
        s.fill(x0 + 1, 0, z0 + 1, x1 - 1, 0, z1 - 1, "minecraft:oak_planks");
        s.walls(x0, 1, z0, x1, wallH, z1, "minecraft:white_terracotta");
        s.walls(x0, 1, z0, x1, 1, z1, "minecraft:cobblestone");
        for (int x = x0; x <= x1; x++) { s.set(x, wallH, z0, "minecraft:stripped_dark_oak_log[axis=x]"); s.set(x, wallH, z1, "minecraft:stripped_dark_oak_log[axis=x]"); }
        for (int z = z0; z <= z1; z++) { s.set(x0, wallH, z, "minecraft:stripped_dark_oak_log[axis=z]"); s.set(x1, wallH, z, "minecraft:stripped_dark_oak_log[axis=z]"); }
        for (int y = 1; y <= wallH; y++) {
            s.set(x0, y, z0, "minecraft:dark_oak_log[axis=y]"); s.set(x1, y, z0, "minecraft:dark_oak_log[axis=y]");
            s.set(x0, y, z1, "minecraft:dark_oak_log[axis=y]"); s.set(x1, y, z1, "minecraft:dark_oak_log[axis=y]");
        }
        int mx = (x0 + x1) / 2, mz = (z0 + z1) / 2;
        switch (doorSide) {
            case "south" -> s.door(mx, 1, z1, "south", "spruce");
            case "north" -> s.door(mx, 1, z0, "north", "spruce");
            case "east" -> s.door(x1, 1, mz, "east", "spruce");
            case "west" -> s.door(x0, 1, mz, "west", "spruce");
        }
        for (int x = x0 + 2; x < x1 - 1; x += 3) { if (x != mx) { s.set(x, 2, z0, "minecraft:glass_pane"); s.set(x, 2, z1, "minecraft:glass_pane"); } }
        for (int z = z0 + 2; z < z1 - 1; z += 3) { if (z != mz) { s.set(x0, 2, z, "minecraft:glass_pane"); s.set(x1, 2, z, "minecraft:glass_pane"); } }
        boolean alongX = (x1 - x0) >= (z1 - z0);
        s.gableRoof(x0, z0, x1, z1, wallH + 1, roofMat, "minecraft:white_terracotta", alongX);
        // belka stropowa - podpora dla latarni (pod dachem dwuspadowym nad środkiem jest pusto)
        for (int x = x0 + 1; x <= x1 - 1; x++) s.set(x, wallH, mz, "minecraft:stripped_dark_oak_log[axis=x]");
        s.set(mx, wallH - 1, mz, "minecraft:lantern[hanging=true]");
        furnishRoom(s, x0, z0, x1, z1, wallH, doorSide, mx, mz, roofMat);
    }

    /**
     * Umeblowanie izby liczone względem drzwi: kominek na ścianie naprzeciw wejścia, łóżko w rogu ze skrzynią
     * w nogach, stół warsztatowy i beczka z doniczką, stół z krzesłami i świecą, dywan, obraz na bocznej ścianie.
     * u - oś wzdłuż tylnej ściany, v - od tylnej ściany w stronę drzwi.
     */
    static void furnishRoom(Structure s, int x0, int z0, int x1, int z1, int wallH, String doorSide, int mx, int mz, String style) {
        int ox, oz, ux, uz, vx, vz, w, d, uc;
        switch (doorSide) {
            case "south" -> { ox = x0 + 1; oz = z0 + 1; ux = 1; uz = 0; vx = 0; vz = 1; w = x1 - x0 - 1; d = z1 - z0 - 1; uc = mx - ox; }
            case "north" -> { ox = x1 - 1; oz = z1 - 1; ux = -1; uz = 0; vx = 0; vz = -1; w = x1 - x0 - 1; d = z1 - z0 - 1; uc = ox - mx; }
            case "east" -> { ox = x0 + 1; oz = z1 - 1; ux = 0; uz = -1; vx = 1; vz = 0; w = z1 - z0 - 1; d = x1 - x0 - 1; uc = oz - mz; }
            default -> { ox = x1 - 1; oz = z0 + 1; ux = 0; uz = 1; vx = -1; vz = 0; w = z1 - z0 - 1; d = x1 - x0 - 1; uc = mz - oz; }
        }
        if (w < 5 || d < 4) return;
        final int fox = ox, foz = oz, fux = ux, fuz = uz, fvx = vx, fvz = vz;
        java.util.function.BiFunction<Integer, Integer, int[]> at = (u, v) -> new int[]{fox + u * fux + v * fvx, foz + u * fuz + v * fvz};
        String toDoor = dirOf(vx, vz), alongU = dirOf(ux, uz);
        String[] palette = switch (style) {
            case "dark_oak" -> new String[]{"red", "orange"};
            case "oak" -> new String[]{"green", "lime"};
            default -> new String[]{"brown", "light_gray"};
        };
        // kominek na tylnej ścianie
        int[] f = at.apply(uc, 0);
        Decor.fireplace(s, f[0], 1, f[1], toDoor, wallH + 3 + Math.max(x1 - x0, z1 - z0) / 2, "minecraft:bricks");
        // łóżko w rogu (wezgłowie przy ścianie), skrzynia w nogach
        int[] bed = at.apply(0, 1);
        s.bed(bed[0], 1, bed[1], Decor.opposite(toDoor), palette[0]);
        int[] chest = at.apply(0, 2);
        s.chest(chest[0], 1, chest[1], alongU, "minecraft:chests/village/village_plains_house");
        // kącik roboczy po drugiej stronie
        int[] craft = at.apply(w - 1, 0);
        s.set(craft[0], 1, craft[1], "minecraft:crafting_table");
        int[] barrel = at.apply(w - 1, 1);
        s.barrel(barrel[0], 1, barrel[1], null);
        Decor.pot(s, barrel[0], 2, barrel[1], (mx + mz) % 2 == 0 ? "red_tulip" : "azure_bluet");
        // stół ze świecą i krzesłami (bez zastawiania przejścia od drzwi)
        int tu = w - 2, tv = d - 2;
        if (tu > uc && tv >= 2) {
            int[] t = at.apply(tu, tv);
            Decor.table(s, t[0], 1, t[1], "spruce", "minecraft:candle[candles=2,lit=true]");
            int[] c1 = at.apply(tu, tv - 1);
            Decor.chair(s, c1[0], 1, c1[1], "spruce", toDoor);
            if (tu + 1 < w) {
                int[] c2 = at.apply(tu + 1, tv);
                Decor.chair(s, c2[0], 1, c2[1], "spruce", Decor.opposite(alongU));
            }
        }
        // dywan przed kominkiem
        int[] r0 = at.apply(uc - 1, 2), r1 = at.apply(uc + 1, Math.max(2, d - 2));
        Decor.rug(s, r0[0], r0[1], r1[0], r1[1], 1, palette[0], palette[1]);
        // obraz na bocznej ścianie (nad łóżkiem/skrzynią)
        String[] variants = {"plant", "wasteland", "alban", "aztec", "kebab", "wanderer", "graham", "sea", "sunset", "courbet"};
        int[] p = at.apply(0, Math.min(3, d - 1));
        String variant = variants[Math.floorMod(x0 * 31 + z0 * 17, variants.length)];
        Decor.painting(s, p[0], Structure.paintingSize(variant)[1] > 1 ? 2 : 3, p[1], alongU, variant);
    }

    static String dirOf(int x, int z) {
        if (x > 0) return "east";
        if (x < 0) return "west";
        return z > 0 ? "south" : "north";
    }

    static void stall(Structure s, int x, int z, String wool) {
        for (int[] p : new int[][]{{0, 0}, {2, 0}, {0, 1}, {2, 1}}) s.fill(x + p[0], 1, z + p[1], x + p[0], 2, z + p[1], "minecraft:oak_fence");
        s.fill(x, 3, z, x + 2, 3, z + 1, wool);
        s.set(x + 1, 1, z, "minecraft:barrel[facing=up,open=false]");
        s.set(x + 1, 1, z + 1, "minecraft:hay_block");
        String[] goods = {"minecraft:melon", "minecraft:pumpkin", "minecraft:cake", "minecraft:honey_block", "minecraft:potted_red_mushroom"};
        s.set(x + 1, 2, z, goods[Math.floorMod(x * 7 + z * 13, goods.length)]);
        s.set(x + 1, 2, z + 1, "minecraft:barrel[facing=up,open=false]");
    }

    // =========================================================================== CHATA HALTA
    static void chataHalta() throws Exception {
        Structure s = new Structure(84, 14, 44, 3003);
        // chata z bali
        int x0 = 6, z0 = 14, x1 = 14, z1 = 21;
        s.fill(x0, 0, z0, x1, 0, z1, "minecraft:cobblestone");
        s.fill(x0 + 1, 0, z0 + 1, x1 - 1, 0, z1 - 1, "minecraft:spruce_planks");
        for (int y = 1; y <= 4; y++) {
            for (int x = x0; x <= x1; x++) { s.set(x, y, z0, "minecraft:spruce_log[axis=x]"); s.set(x, y, z1, "minecraft:spruce_log[axis=x]"); }
            for (int z = z0; z <= z1; z++) { s.set(x0, y, z, "minecraft:spruce_log[axis=z]"); s.set(x1, y, z, "minecraft:spruce_log[axis=z]"); }
            s.set(x0, y, z0, "minecraft:stripped_spruce_log[axis=y]"); s.set(x1, y, z0, "minecraft:stripped_spruce_log[axis=y]");
            s.set(x0, y, z1, "minecraft:stripped_spruce_log[axis=y]"); s.set(x1, y, z1, "minecraft:stripped_spruce_log[axis=y]");
        }
        s.gableRoof(x0, z0, x1, z1, 5, "spruce", "minecraft:spruce_planks", true);
        s.door(10, 1, z1, "south", "spruce");
        for (int[] w : new int[][]{{8, 21}, {12, 21}, {10, 14}}) s.set(w[0], 2, w[1], "minecraft:glass_pane");
        s.set(x1, 2, 17, "minecraft:glass_pane");
        // komin
        s.fill(5, 0, 17, 5, 8, 17, "minecraft:cobblestone");
        s.set(5, 9, 17, "minecraft:campfire[lit=true,facing=north,signal_fire=false]");
        // wnętrze: kamienne palenisko przy zachodniej ścianie (łączy się z kominem na zewnątrz)
        Decor.fireplace(s, 7, 1, 17, "east", 4, "minecraft:cobblestone");
        s.set(7, 1, 20, "minecraft:smoker[facing=north,lit=false]");
        s.barrel(8, 1, 20, null);
        s.set(7, 1, 19, "minecraft:spruce_slab[type=top]");
        s.set(7, 2, 19, "minecraft:potted_brown_mushroom");
        s.set(8, 1, 15, "minecraft:crafting_table");
        s.chest(9, 1, 15, "south", "zwiadowcy:chests/chata_halta");
        s.barrel(11, 1, 15, null);
        s.set(12, 1, 15, "minecraft:bookshelf");
        s.set(12, 2, 15, "minecraft:bookshelf");
        s.bed(13, 1, 16, "north", "green");
        s.set(10, 1, 18, "minecraft:spruce_fence");
        s.set(10, 2, 18, "minecraft:spruce_pressure_plate");
        s.set(9, 1, 18, stairs("spruce", "west"));
        s.set(11, 1, 18, stairs("spruce", "east"));
        for (int x = 8; x <= 13; x++) s.set(x, 4, 17, "minecraft:stripped_spruce_log[axis=x]");
        s.set(10, 3, 17, "minecraft:lantern[hanging=true]");
        s.set(13, 1, 20, "minecraft:potted_spruce_sapling");
        // kawa z miodem na beczce, świeca na stole, dywan, łuk i strzała na ścianie
        s.set(11, 2, 15, "minecraft:brewing_stand[has_bottle_0=true,has_bottle_1=false,has_bottle_2=false]");
        s.set(10, 2, 18, "minecraft:candle[candles=2,lit=true]");
        Decor.rug(s, 9, 16, 12, 19, 1, "brown", "green");
        Decor.painting(s, 8, 2, 15, "south", "wanderer");
        Decor.itemFrame(s, 13, 3, 20, "north", "minecraft:bow");
        Decor.itemFrame(s, 11, 3, 20, "north", "minecraft:arrow");
        Decor.itemFrame(s, 13, 3, 15, "south", "minecraft:spyglass");
        // weranda
        s.fill(x0, 0, 22, x1, 0, 24, "minecraft:spruce_planks");
        s.fill(x0, 4, 22, x1, 4, 24, "minecraft:spruce_slab[type=bottom]");
        for (int x : new int[]{x0, x1}) s.fill(x, 1, 24, x, 3, 24, "minecraft:spruce_fence");
        s.set(8, 1, 23, stairs("spruce", "north"));
        s.set(12, 1, 23, stairs("spruce", "north"));
        s.set(7, 3, 23, "minecraft:lantern[hanging=true]");
        Decor.table(s, 13, 1, 23, "spruce", "minecraft:potted_blue_orchid");
        s.barrel(7, 1, 22, null);
        Decor.pot(s, 6, 1, 23, "red_tulip");
        // ognisko z kociołkiem przed chatą
        Decor.campfireRing(s, 3, 1, 29, true);
        // ul (miód do kawy) i kwietnik
        s.set(3, 1, 22, "minecraft:beehive[facing=south,honey_level=5]");
        s.set(2, 1, 22, "minecraft:oak_fence");
        for (int[] f : new int[][]{{2, 24}, {3, 25}, {4, 24}, {5, 26}, {1, 25}}) s.set(f[0], 1, f[1], f[0] % 2 == 0 ? "minecraft:cornflower" : "minecraft:dandelion");
        // drewutnia
        s.fill(16, 1, 22, 19, 2, 22, "minecraft:oak_log[axis=x]");
        s.set(17, 3, 22, "minecraft:oak_log[axis=x]");
        // stajenka Wyrwija
        s.fill(15, 4, 14, 19, 4, 19, "minecraft:spruce_slab[type=top]");
        for (int x = 15; x <= 19; x++) { s.set(x, 1, 14, "minecraft:spruce_fence"); if (x != 17) s.set(x, 1, 19, "minecraft:spruce_fence"); }
        for (int z = 14; z <= 19; z++) s.set(19, 1, z, "minecraft:spruce_fence");
        for (int[] p : new int[][]{{15, 19}, {19, 19}, {19, 14}}) s.fill(p[0], 2, p[1], p[0], 3, p[1], "minecraft:spruce_fence");
        s.set(16, 1, 15, "minecraft:hay_block"); s.set(16, 2, 15, "minecraft:hay_block");
        s.set(18, 1, 15, "minecraft:water_cauldron[level=3]");
        s.anchor(17, 1, 17, "wyrwij");

        // strzelnica: linia na x=20, tarcze 20/40/60 kratek dalej
        s.fill(20, 0, 26, 21, 0, 34, "minecraft:dirt_path");
        for (int tx : new int[]{40, 60, 80}) {
            for (int tz : new int[]{27, 30, 33}) {
                s.set(tx, 1, tz, "zwiadowcy:tarcza_lucznicza");
                s.set(tx, 2, tz, "zwiadowcy:tarcza_lucznicza");
                s.set(tx + 1, 1, tz, "minecraft:hay_block");
                s.set(tx + 1, 2, tz, "minecraft:hay_block");
            }
            s.fill(tx + 2, 1, 26, tx + 2, 3, 34, "minecraft:hay_block");
        }
        for (int x = 22; x <= 82; x += 2) {
            s.set(x, 1, 25, "minecraft:oak_fence");
            s.set(x, 1, 35, "minecraft:oak_fence");
        }
        s.set(20, 1, 25, "minecraft:oak_fence"); s.set(20, 2, 25, "minecraft:oak_fence"); s.set(20, 3, 25, "minecraft:lantern[hanging=false]");
        s.anchor(20, 1, 30, "linia_strzalu");
        // tarcze do noży i manekin
        for (int[] t : new int[][]{{5, 34}, {8, 35}, {11, 34}}) {
            s.set(t[0], 0, t[1], "minecraft:spruce_planks");
            s.set(t[0], 1, t[1], "zwiadowcy:tarcza_lucznicza");
            s.set(t[0], 2, t[1], "zwiadowcy:tarcza_lucznicza");
        }
        s.anchor(8, 1, 28, "tarcze_noze");
        s.marker(16, 1, 28, "dummy;yaw=180");

        // polana do lekcji bezruchu (północ): Halt patrzy na zachód, gracz zaczyna od zachodu
        for (int x = 22; x <= 62; x++) {
            for (int z = 1; z <= 12; z++) {
                if (Math.abs(x - 42) <= 1 && Math.abs(z - 6) <= 1) continue;
                double r = s.rnd.nextDouble();
                if (r < 0.28) s.set(x, 1, z, "minecraft:grass");
                else if (r < 0.40) s.tallPlant(x, 1, z, "tall_grass");
                else if (r < 0.47) s.set(x, 1, z, "minecraft:fern");
                else if (r < 0.51) s.tallPlant(x, 1, z, "large_fern");
            }
        }
        for (int i = 0; i < 16; i++) {
            int bx = 24 + s.rnd.nextInt(37), bz = 1 + s.rnd.nextInt(11);
            if (Math.abs(bx - 42) <= 2 && Math.abs(bz - 6) <= 2) continue;
            s.bush(bx, 1, bz, i % 3 == 0 ? "spruce" : "oak");
        }
        s.fill(30, 1, 10, 34, 1, 10, "minecraft:oak_log[axis=x]");
        s.anchor(42, 1, 6, "halt_obserwuje");
        s.anchor(45, 1, 6, "za_plecami_halta");
        s.anchor(22, 1, 7, "start_bezruch");
        // drzewa (drewno do misji i osłona)
        int[][] trees = {{2, 3}, {3, 40}, {16, 40}, {30, 41}, {48, 40}, {66, 41}, {79, 3}, {70, 2}, {2, 10}, {64, 9}};
        for (int[] t : trees) s.tree(t[0], 1, t[1], t[0] % 2 == 0 ? "oak" : "spruce", 5 + s.rnd.nextInt(2));
        s.anchor(10, 1, 24, "chata");
        s.anchor(12, 1, 26, "halt");
        save(s, "chata_halta");
    }

    // =========================================================================== FARMA STAREGO BOBA
    static void stajniaBoba() throws Exception {
        Structure s = new Structure(34, 12, 30, 4004);
        house(s, 3, 3, 10, 9, 4, "south", "spruce");
        // stajnia
        int x0 = 14, z0 = 3, x1 = 27, z1 = 12;
        s.fill(x0, 0, z0, x1, 0, z1, "minecraft:coarse_dirt");
        s.walls(x0, 1, z0, x1, 4, z1, "minecraft:spruce_planks");
        for (int x = x0; x <= x1; x += 4) for (int y = 1; y <= 4; y++) { s.set(x, y, z0, "minecraft:oak_log[axis=y]"); s.set(x, y, z1, "minecraft:oak_log[axis=y]"); }
        s.clear(19, 1, z1, 22, 3, z1);
        s.gableRoof(x0, z0, x1, z1, 5, "dark_oak", "minecraft:spruce_planks", true);
        for (int x = 17; x <= 26; x += 3) s.fill(x, 1, 4, x, 1, 7, "minecraft:oak_fence");
        for (int x = 15; x <= 26; x += 3) { s.set(x, 1, 4, "minecraft:hay_block"); s.set(x + 1, 1, 4, "minecraft:water_cauldron[level=3]"); }
        s.set(15, 1, 11, "minecraft:hay_block"); s.set(15, 2, 11, "minecraft:hay_block"); s.set(16, 1, 11, "minecraft:hay_block");
        for (int x = 15; x <= 26; x++) s.set(x, 4, 8, "minecraft:stripped_dark_oak_log[axis=x]");
        s.set(20, 3, 8, "minecraft:lantern[hanging=true]");
        // uprząż na ścianie, beczki z owsem
        Decor.itemFrame(s, 16, 3, 11, "north", "minecraft:saddle");
        Decor.itemFrame(s, 17, 3, 11, "north", "minecraft:lead");
        Decor.itemFrame(s, 24, 3, 11, "north", "minecraft:leather_horse_armor");
        s.barrel(26, 1, 11, null);
        s.barrel(25, 1, 11, null);
        s.set(26, 2, 11, "minecraft:barrel[facing=north,open=false]");
        s.set(24, 1, 11, "minecraft:composter[level=6]");
        // strach na wróble w polu i kompostownik przy grządkach
        Decor.armorStand(s, 8, 1, 16, 0f, null, "minecraft:leather_leggings", "minecraft:leather_chestplate", "minecraft:carved_pumpkin");
        s.set(11, 1, 13, "minecraft:composter[level=3]");
        s.set(11, 1, 14, "minecraft:barrel[facing=up,open=false]");
        // wybieg
        for (int x = 13; x <= 31; x++) { s.set(x, 1, 15, "minecraft:oak_fence"); s.set(x, 1, 27, "minecraft:oak_fence"); }
        for (int z = 15; z <= 27; z++) { s.set(13, 1, z, "minecraft:oak_fence"); s.set(31, 1, z, "minecraft:oak_fence"); }
        s.set(20, 1, 15, "minecraft:oak_fence_gate[facing=south,open=false,in_wall=false]");
        s.set(16, 1, 18, "minecraft:hay_block");
        s.set(28, 1, 24, "minecraft:water_cauldron[level=3]");
        s.marker(22, 1, 21, "tug;yaw=180");
        s.npc(20, 1, 13, "stary_bob", 0f);
        s.anchor(20, 1, 14, "stajnia");
        for (int x = 3; x <= 10; x++) for (int z = 13; z <= 18; z++) {
            if (x == 6) { s.set(x, 0, z, "minecraft:water"); continue; }
            s.set(x, 0, z, "minecraft:farmland[moisture=7]");
            s.set(x, 1, z, (z % 2 == 0) ? "minecraft:carrots[age=7]" : "minecraft:potatoes[age=7]");
        }
        s.tree(1, 1, 26, "oak", 5);
        s.tree(32, 1, 2, "oak", 5);
        save(s, "stajnia_boba");
    }

    // =========================================================================== LEGOWISKO DZIKA
    static void legowisko() throws Exception {
        Structure s = new Structure(32, 10, 32, 5005);
        for (int x = 2; x < 30; x++) for (int z = 2; z < 30; z++) {
            double d = Math.hypot(x - 16, z - 16);
            if (d > 13) continue;
            s.set(x, 0, z, s.pick(new String[]{"minecraft:podzol", "minecraft:coarse_dirt", "minecraft:mud", "minecraft:rooted_dirt", "minecraft:grass_block"},
                    new double[]{0.3, 0.25, 0.12, 0.08, 0.25}));
            double r = s.rnd.nextDouble();
            if (d > 6 && r < 0.18) s.set(x, 1, z, "minecraft:fern");
            else if (d > 6 && r < 0.26) s.set(x, 1, z, "minecraft:grass");
        }
        for (int i = 0; i < 11; i++) {
            double a = i * Math.PI * 2 / 11;
            int tx = 16 + (int) Math.round(Math.cos(a) * 13), tz = 16 + (int) Math.round(Math.sin(a) * 13);
            s.tree(tx, 1, tz, i % 3 == 0 ? "dark_oak" : (i % 3 == 1 ? "oak" : "spruce"), 5 + s.rnd.nextInt(3));
        }
        s.fill(6, 1, 14, 10, 1, 14, "minecraft:oak_log[axis=x]");
        s.fill(21, 1, 21, 21, 1, 25, "minecraft:dark_oak_log[axis=z]");
        for (int[] b : new int[][]{{19, 9}, {11, 23}, {24, 15}}) {
            s.set(b[0], 1, b[1], "minecraft:mossy_cobblestone");
            s.set(b[0] + 1, 1, b[1], "minecraft:mossy_cobblestone");
            s.set(b[0], 2, b[1], "minecraft:mossy_cobblestone");
            s.set(b[0], 1, b[1] + 1, "minecraft:moss_block");
        }
        // postój myśliwych barona na skraju polany
        Decor.campfireRing(s, 9, 1, 25, false);
        s.anchor(16, 1, 16, "polana");
        s.anchor(12, 1, 20, "baron");
        s.anchor(14, 1, 21, "horace");
        s.anchor(18, 1, 21, "halt");
        s.anchor(9, 1, 8, "dzik_1");
        s.anchor(23, 1, 9, "dzik_2");
        save(s, "legowisko_dzika");
    }

    // =========================================================================== OBÓZ ZLOTU
    static void obozZlotu() throws Exception {
        Structure s = new Structure(72, 12, 48, 6006);
        // ognisko i ławy
        Decor.cookingPot(s, 30, 1, 22);
        s.barrel(35, 1, 19, null);
        s.set(35, 1, 20, "minecraft:barrel[facing=east,open=false]");
        s.set(35, 2, 19, "minecraft:barrel[facing=east,open=false]");
        for (int x = 28; x <= 32; x++) { s.set(x, 1, 19, "minecraft:spruce_log[axis=x]"); s.set(x, 1, 25, "minecraft:spruce_log[axis=x]"); }
        for (int z = 20; z <= 24; z++) { s.set(27, 1, z, "minecraft:spruce_log[axis=z]"); s.set(33, 1, z, "minecraft:spruce_log[axis=z]"); }
        s.fillMix(26, 0, 18, 34, 0, 26, new String[]{"minecraft:coarse_dirt", "minecraft:dirt_path"}, new double[]{0.5, 0.5});
        // namioty
        String[] colors = {"minecraft:green_wool", "minecraft:brown_wool", "minecraft:gray_wool", "minecraft:lime_wool"};
        int[][] tents = {{8, 6}, {16, 6}, {40, 6}, {48, 6}, {8, 28}, {16, 28}, {44, 28}, {52, 28}};
        for (int i = 0; i < tents.length; i++) tent(s, tents[i][0], tents[i][1], 5, colors[i % colors.length]);
        // namiot dowództwa
        int cx0 = 24, cz0 = 3;
        for (int x = cx0; x <= cx0 + 10; x++) {
            s.set(x, 1, cz0, "minecraft:green_wool"); s.set(x, 1, cz0 + 8, x == cx0 + 5 ? "minecraft:air" : "minecraft:green_wool");
            s.set(x, 2, cz0 + 1, "minecraft:green_wool"); s.set(x, 2, cz0 + 7, "minecraft:green_wool");
            s.set(x, 3, cz0 + 2, "minecraft:green_wool"); s.set(x, 3, cz0 + 6, "minecraft:green_wool");
            s.set(x, 4, cz0 + 3, "minecraft:green_wool"); s.set(x, 4, cz0 + 5, "minecraft:green_wool");
            s.set(x, 5, cz0 + 4, "minecraft:green_wool");
        }
        for (int z = cz0; z <= cz0 + 8; z++) { s.set(cx0, 1, z, "minecraft:green_wool"); s.set(cx0 + 10, 1, z, "minecraft:green_wool"); }
        s.fill(cx0 + 1, 0, cz0 + 1, cx0 + 9, 0, cz0 + 7, "minecraft:spruce_planks");
        s.chest(28, 1, 5, "south", "zwiadowcy:chests/oboz_zlotu");
        s.set(31, 1, 5, "minecraft:crafting_table");
        s.set(30, 1, 6, "minecraft:spruce_fence");
        s.set(30, 2, 6, "minecraft:spruce_pressure_plate");
        s.set(29, 4, 7, "minecraft:lantern[hanging=true]");
        // narady dowódców: stół kartograficzny, pulpit z raportami, mapa na płótnie, dywan
        s.set(26, 1, 5, "minecraft:cartography_table");
        s.set(32, 1, 8, "minecraft:lectern[facing=west,has_book=false,powered=false]");
        Decor.itemFrame(s, 30, 2, 5, "south", "minecraft:map");
        Decor.chair(s, 29, 1, 6, "spruce", "east");
        Decor.chair(s, 31, 1, 6, "spruce", "west");
        s.set(30, 2, 6, "minecraft:candle[candles=3,lit=true]");
        s.barrel(33, 1, 6, null);
        s.set(33, 1, 7, "minecraft:barrel[facing=up,open=false]");
        Decor.decoratedPot(s, 25, 1, 8);
        Decor.rug(s, 27, 7, 31, 9, 1, "green", "lime");
        s.fill(29, 1, 13, 29, 3, 13, "minecraft:spruce_fence");
        s.set(29, 4, 13, "minecraft:green_banner[rotation=0]");
        // strzelnica konkursowa (linia x=6, tarcze 42 i 50 kratek dalej)
        s.fill(6, 0, 41, 7, 0, 47, "minecraft:dirt_path");
        for (int tx : new int[]{48, 56}) {
            for (int tz : new int[]{42, 44, 46}) {
                s.set(tx, 1, tz, "zwiadowcy:tarcza_lucznicza");
                s.set(tx, 2, tz, "zwiadowcy:tarcza_lucznicza");
            }
            s.fill(tx + 1, 1, 41, tx + 1, 3, 47, "minecraft:hay_block");
        }
        for (int x = 8; x <= 60; x += 2) s.set(x, 1, 40, "minecraft:spruce_fence");
        s.anchor(6, 1, 44, "linia_zawodow");
        // konie i proporce
        for (int x = 56; x <= 66; x++) s.set(x, 1, 12, "minecraft:spruce_fence");
        s.set(58, 1, 13, "minecraft:hay_block"); s.set(63, 1, 13, "minecraft:hay_block");
        for (int[] b : new int[][]{{22, 16}, {38, 16}, {22, 30}, {38, 30}}) {
            s.fill(b[0], 1, b[1], b[0], 3, b[1], "minecraft:spruce_fence");
            s.set(b[0], 4, b[1], "minecraft:lantern[hanging=false]");
        }
        s.tree(2, 1, 2, "spruce", 7);
        s.tree(69, 1, 3, "spruce", 6);
        s.tree(68, 1, 36, "spruce", 7);
        s.tree(3, 1, 22, "oak", 5);

        s.anchor(30, 1, 17, "oboz");
        s.anchor(25, 1, 22, "halt");
        s.npc(29, 1, 12, "crowley", 0f);
        s.npc(8, 1, 42, "gilan", -90f);
        s.npc(27, 1, 21, "zwiadowca", -90f);
        s.npc(33, 1, 23, "zwiadowca", 90f);
        s.npc(30, 1, 26, "zwiadowca", 180f);
        save(s, "oboz_zlotu");
    }

    static void tent(Structure s, int x0, int z0, int len, String wool) {
        for (int x = x0; x < x0 + len; x++) {
            s.set(x, 1, z0, wool); s.set(x, 1, z0 + 4, wool);
            s.set(x, 2, z0 + 1, wool); s.set(x, 2, z0 + 3, wool);
            s.set(x, 3, z0 + 2, wool);
        }
        s.set(x0 + len - 1, 1, z0 + 2, "minecraft:white_wool");
        s.bed(x0 + 1, 1, z0 + 2, "east", "brown");
        s.set(x0 + 3, 2, z0 + 2, "minecraft:lantern[hanging=true]");
    }

    // =========================================================================== SPALONA OSADA
    static void spalonaOsada() throws Exception {
        Structure s = new Structure(40, 10, 40, 7007);
        for (int x = 0; x < 40; x++) for (int z = 0; z < 40; z++) {
            if (Math.hypot(x - 20, z - 20) > 19) continue;
            s.set(x, 0, z, s.pick(new String[]{"minecraft:coarse_dirt", "minecraft:soul_soil", "minecraft:dirt", "minecraft:grass_block"},
                    new double[]{0.4, 0.2, 0.2, 0.2}));
        }
        burnedHouse(s, 4, 4, 11, 10);
        burnedHouse(s, 26, 5, 34, 11);
        burnedHouse(s, 5, 26, 11, 33);
        burnedHouse(s, 25, 27, 32, 34);
        s.chest(8, 1, 29, "south", "zwiadowcy:chests/spalona_osada");
        for (int x = 19; x <= 21; x++) for (int z = 19; z <= 21; z++) {
            if (x == 20 && z == 20) continue;
            if (s.rnd.nextDouble() < 0.75) s.set(x, 1, z, "minecraft:cobblestone");
        }
        s.set(20, 0, 20, "minecraft:water");
        for (int t = 0; t <= 8; t++) {
            int off = (t % 2 == 0) ? 1 : -1;
            s.set(21 + 2 * t + (off > 0 ? 1 : 0), 1, 23 + 2 * t - (off > 0 ? 0 : 1), "zwiadowcy:slady");
        }
        s.set(14, 1, 17, "minecraft:campfire[lit=false,facing=north,signal_fire=false]");
        s.set(24, 1, 15, "minecraft:campfire[lit=false,facing=north,signal_fire=false]");
        s.fill(16, 1, 34, 19, 1, 34, "minecraft:spruce_slab[type=bottom]");
        for (int x = 2; x <= 14; x += 3) if (s.rnd.nextBoolean()) s.set(x, 1, 18, "minecraft:spruce_fence");
        s.set(24, 1, 24, "minecraft:skeleton_skull[rotation=6]");
        s.anchor(20, 1, 17, "osada");
        s.anchor(16, 1, 20, "halt");
        s.anchor(18, 1, 22, "gilan");
        s.anchor(31, 1, 15, "wargal_1");
        s.anchor(8, 1, 19, "wargal_2");
        s.anchor(30, 1, 36, "wargal_3");
        s.anchor(15, 1, 2, "wargal_4");
        save(s, "spalona_osada");
    }

    static void burnedHouse(Structure s, int x0, int z0, int x1, int z1) {
        s.fill(x0, 0, z0, x1, 0, z1, "minecraft:cobblestone");
        String[] mat = {"minecraft:cobbled_deepslate", "minecraft:blackstone", "minecraft:black_concrete", "minecraft:cobblestone"};
        double[] w = {0.35, 0.3, 0.15, 0.2};
        for (int y = 1; y <= 4; y++) {
            double p = switch (y) { case 1 -> 0.9; case 2 -> 0.65; case 3 -> 0.4; default -> 0.15; };
            for (int x = x0; x <= x1; x++) for (int z = z0; z <= z1; z++) {
                boolean edge = x == x0 || x == x1 || z == z0 || z == z1;
                if (!edge) continue;
                if (s.rnd.nextDouble() < p) s.set(x, y, z, s.pick(mat, w));
            }
        }
        int[][] cs = {{x0, z0}, {x1, z0}, {x0, z1}, {x1, z1}};
        for (int[] c : cs) {
            int h = 2 + s.rnd.nextInt(4);
            for (int y = 1; y <= h; y++) s.set(c[0], y, c[1], "minecraft:dark_oak_log[axis=y]");
        }
        for (int x = x0 + 1; x < x1; x++) for (int z = z0 + 1; z < z1; z++) {
            s.set(x, 0, z, s.rnd.nextBoolean() ? "minecraft:soul_soil" : "minecraft:coarse_dirt");
            double r = s.rnd.nextDouble();
            if (r < 0.06) s.set(x, 1, z, "minecraft:cobweb");
            else if (r < 0.12) s.set(x, 1, z, "minecraft:dark_oak_log[axis=x]");
            else if (r < 0.22) s.set(x, 1, z, r < 0.17 ? "minecraft:gray_carpet" : "minecraft:black_carpet");
        }
        // jedyne, co ocalało: kamienny komin z wygasłym paleniskiem, zwęglony stół, przewrócone krzesło, kocioł
        int mx = (x0 + x1) / 2;
        int ch = 4 + s.rnd.nextInt(3);
        for (int y = 1; y <= ch; y++) {
            s.set(mx - 1, y, z0, s.rnd.nextDouble() < 0.3 ? "minecraft:cobbled_deepslate" : "minecraft:cobblestone");
            s.set(mx, y, z0, s.rnd.nextDouble() < 0.3 ? "minecraft:cobbled_deepslate" : "minecraft:cobblestone");
            if (y <= 2) s.set(mx + 1, y, z0, "minecraft:cobblestone");
        }
        s.set(mx, 1, z0 + 1, "minecraft:campfire[lit=false,facing=south,signal_fire=false]");
        s.set(x1 - 2, 1, z0 + 2, "minecraft:dark_oak_fence");
        s.set(x1 - 2, 2, z0 + 2, "minecraft:dark_oak_pressure_plate");
        s.set(x1 - 3, 1, z0 + 2, "minecraft:dark_oak_stairs[facing=west,half=top]");
        s.set(x0 + 2, 1, z1 - 2, "minecraft:cauldron");
        s.clear((x0 + x1) / 2, 1, z1, (x0 + x1) / 2, 2, z1);
    }

    // =========================================================================== RUINY GORLANU
    static void ruinyGorlanu() throws Exception {
        Structure s = new Structure(60, 28, 60, 8008, 6);
        int a = 6, b = 53;
        for (int x = a; x <= b; x++) for (int z = a; z <= b; z++) {
            s.set(x, 0, z, s.pick(new String[]{"minecraft:cobblestone", "minecraft:gravel", "minecraft:coarse_dirt", "minecraft:mossy_cobblestone", "minecraft:grass_block"},
                    new double[]{0.25, 0.15, 0.2, 0.15, 0.25}));
        }
        // mury obwodowe z wyłomami; odcinki po 4 bloki
        for (int seg = 0; seg < 12; seg++) {
            int from = a + seg * 4, to = Math.min(b, from + 3);
            int hN = segmentHeight(s, from, true), hS = segmentHeight(s, from, false);
            int hW = segmentHeight(s, from, false), hE = segmentHeight(s, from, true);
            if (from <= 32 && to >= 28) hN = 6;          // odcinek z punktem obserwacyjnym
            if (from <= 32 && to >= 28) hW = 0;          // wyłom bramy zachodniej
            for (int i = from; i <= to; i++) {
                ruinColumn(s, i, a, hN, "south");
                ruinColumn(s, i, a + 1, hN, null);
                ruinColumn(s, i, b, hS, "north");
                ruinColumn(s, i, b - 1, hS, null);
                ruinColumn(s, a, i, hW, "east");
                ruinColumn(s, a + 1, i, hW, null);
                ruinColumn(s, b, i, hE, "west");
                ruinColumn(s, b - 1, i, hE, null);
            }
        }
        // odcinek muru z punktem obserwacyjnym ma stałą wysokość 6
        s.fillMix(27, 1, a, 33, 6, a + 1, RUIN, RUIN_W);
        s.clear(27, 7, a, 33, 10, a + 1);
        // rampa z gruzu na mur północny
        for (int z = 1; z <= 5; z++) {
            int y = z;
            s.fill(29, 1, z, 31, y - 1, z, "minecraft:cobblestone");
            s.set(29, y, z, stairs("stone_brick", "south"));
            s.set(30, y, z, stairs("stone_brick", "south"));
            s.set(31, y, z, stairs("mossy_stone_brick", "south"));
        }
        s.anchor(30, 7, 7, "punkt_zwiadu");
        // zawalona wieża (NW) ze skrzynią
        s.wallsMix(8, 1, 8, 16, 14, 16, RUIN, RUIN_W);
        s.clear(9, 1, 9, 15, 13, 15);
        for (int x = 8; x <= 16; x++) for (int z = 8; z <= 16; z++) {
            for (int y = 9 + s.rnd.nextInt(6); y <= 14; y++) s.set(x, y, z, "minecraft:air");
        }
        s.clear(12, 1, 16, 12, 3, 16);
        s.chest(12, 1, 10, "south", "zwiadowcy:chests/ruiny_gorlanu");
        s.set(10, 1, 14, "minecraft:cobweb"); s.set(14, 2, 10, "minecraft:cobweb"); s.set(9, 3, 12, "minecraft:cobweb");
        s.set(14, 1, 14, "minecraft:bone_block[axis=x]");
        s.anchor(12, 1, 12, "skrzynia");
        // ruiny twierdzy (wschód)
        s.wallsMix(38, 1, 16, 48, 4, 28, RUIN, RUIN_W);
        for (int x = 38; x <= 48; x++) for (int z = 16; z <= 28; z++) {
            boolean edge = x == 38 || x == 48 || z == 16 || z == 28;
            if (!edge) continue;
            int h = 2 + s.rnd.nextInt(9);
            for (int y = 1; y <= h; y++) if (y > 4) s.set(x, y, z, s.pick(RUIN, RUIN_W));
            if (s.rnd.nextDouble() < 0.18) for (int y = 1; y <= 12; y++) s.set(x, y, z, "minecraft:air");
        }
        // arena w sercu ruin
        s.fillMix(22, 0, 22, 38, 0, 38, new String[]{"minecraft:polished_andesite", "minecraft:cobblestone", "minecraft:andesite", "minecraft:mossy_cobblestone"},
                new double[]{0.35, 0.3, 0.2, 0.15});
        for (int[] p : new int[][]{{22, 22}, {38, 22}, {22, 38}, {38, 38}, {30, 22}, {22, 30}, {38, 30}}) {
            int h = 2 + s.rnd.nextInt(5);
            for (int y = 1; y <= h; y++) s.set(p[0], y, p[1], y == h ? "minecraft:chiseled_stone_bricks" : "minecraft:stone_bricks");
        }
        for (int i = 0; i < 26; i++) {
            int x = 9 + s.rnd.nextInt(42), z = 9 + s.rnd.nextInt(42);
            if (x >= 25 && x <= 35 && z >= 25 && z <= 35) continue;
            s.set(x, 1, z, s.rnd.nextBoolean() ? "minecraft:cobblestone" : "minecraft:stone_brick_slab[type=bottom]");
            if (s.rnd.nextDouble() < 0.3) s.set(x, 2, z, "minecraft:mossy_cobblestone");
        }
        for (int i = 0; i < 18; i++) {
            int x = 8 + s.rnd.nextInt(44), z = 8 + s.rnd.nextInt(44);
            if (!s.has(x, 1, z)) s.set(x, 1, z, s.rnd.nextBoolean() ? "minecraft:dead_bush" : "minecraft:grass");
        }
        s.set(33, 1, 27, "minecraft:skeleton_skull[rotation=12]");
        s.set(26, 1, 34, "minecraft:bone_block[axis=z]");

        gorlanCrypt(s);

        s.anchor(30, 1, 30, "arena");
        s.anchor(30, 1, 41, "kalkar_1");
        s.anchor(42, 1, 22, "kalkar_2");
        s.anchor(12, 1, 30, "wartownik_1");
        s.anchor(36, 1, 13, "wartownik_2");
        s.anchor(42, 1, 34, "wartownik_3");
        s.anchor(2, 1, 30, "podejscie");
        // ukryty obóz przed ruinami: posłania do przespania dnia (Kalkarowie polują nocą)
        s.bed(1, 1, 22, "north", "brown");
        s.bed(3, 1, 22, "north", "green");
        s.barrel(4, 1, 21, null);
        s.anchor(2, 1, 26, "halt");
        s.anchor(2, 1, 34, "gilan");
        s.anchor(3, 1, 31, "horace");
        save(s, "ruiny_gorlanu");
    }

    /** Krypta pod twierdzą Gorlanu: schody z ruin twierdzy, korytarz z celami, komora z łańcuchami. */
    static void gorlanCrypt(Structure s) {
        String[] crypt = {"minecraft:stone_bricks", "minecraft:mossy_stone_bricks", "minecraft:cracked_stone_bricks", "minecraft:cobbled_deepslate"};
        double[] cw = {0.4, 0.25, 0.2, 0.15};
        // klatka schodowa w dół (w narożniku twierdzy)
        for (int i = 0; i <= 5; i++) {
            int x = 46 - i, y = -i;
            s.fillMix(x, y - 1, 24, x, y + 3, 27, crypt, cw);
            s.set(x, y, 25, "minecraft:stone_brick_stairs[facing=east,half=bottom]");
            s.set(x, y, 26, "minecraft:stone_brick_stairs[facing=east,half=bottom]");
            s.clear(x, y + 1, 25, x, y + 2, 26);
        }
        s.clear(46, 1, 25, 47, 2, 26);
        // korytarz (podłoga y=-6)
        s.fillMix(22, -6, 23, 41, -2, 28, crypt, cw);
        s.clear(22, -5, 25, 41, -3, 26);
        // cele po obu stronach
        for (int x = 25; x <= 37; x += 4) {
            for (int[] side : new int[][]{{19, 23}, {28, 32}}) {
                int z0 = side[0], z1 = side[1];
                s.fillMix(x - 1, -6, z0, x + 3, -2, z1, crypt, cw);
                int innerZ0 = z0 == 19 ? 20 : 28, innerZ1 = z0 == 19 ? 23 : 31;
                s.clear(x, -5, innerZ0, x + 2, -3, innerZ1);
                int barZ = z0 == 19 ? 24 : 27;
                boolean broken = s.rnd.nextDouble() < 0.3;
                for (int bx = x; bx <= x + 2; bx++) for (int y = -5; y <= -3; y++) {
                    s.set(bx, y, barZ, broken && bx == x + 1 && y < -3 ? "minecraft:air" : "minecraft:iron_bars");
                }
                int cz = z0 == 19 ? 21 : 30;
                s.set(x + 1, -3, cz, "minecraft:chain[axis=y]");
                s.set(x + 1, -4, cz, "minecraft:chain[axis=y]");
                if (s.rnd.nextBoolean()) s.set(x, -5, cz, "minecraft:bone_block[axis=x]");
                if (s.rnd.nextDouble() < 0.4) s.set(x + 2, -5, cz, "minecraft:skeleton_skull[rotation=" + s.rnd.nextInt(16) + "]");
            }
            s.set(x + 1, -3, 25, "minecraft:soul_lantern[hanging=true]");
        }
        // komora na zachodnim końcu
        s.fillMix(14, -6, 19, 22, -1, 32, crypt, cw);
        s.clear(15, -5, 20, 21, -2, 31);
        s.clear(21, -5, 25, 22, -3, 26);
        for (int[] p : new int[][]{{16, 21}, {20, 21}, {16, 30}, {20, 30}}) {
            for (int y = -4; y <= -2; y++) s.set(p[0], y, p[1], "minecraft:chain[axis=y]");
        }
        s.fill(15, -5, 24, 15, -5, 27, "minecraft:bone_block[axis=z]");
        s.set(16, -5, 22, "minecraft:bone_block[axis=y]");
        s.set(19, -5, 29, "minecraft:skeleton_skull[rotation=2]");
        s.chest(15, -5, 22, "east", "zwiadowcy:chests/krypta_gorlanu");
        s.set(18, -2, 25, "minecraft:soul_lantern[hanging=true]");
        s.set(18, -2, 28, "minecraft:soul_lantern[hanging=true]");
        for (int i = 0; i < 8; i++) s.set(15 + s.rnd.nextInt(7), -5 + s.rnd.nextInt(3), 20 + (s.rnd.nextBoolean() ? 0 : 11), "minecraft:cobweb");
        // dawne urny i świece Kalkarów na kościanym ołtarzu
        for (int[] p : new int[][]{{21, 21}, {21, 30}, {17, 31}, {15, 30}, {18, 20}}) Decor.decoratedPot(s, p[0], -5, p[1]);
        s.set(15, -4, 24, "minecraft:candle[candles=3,lit=true]");
        s.set(15, -4, 27, "minecraft:candle[candles=2,lit=true]");
        s.set(15, -4, 25, "minecraft:skeleton_skull[rotation=12]");
        s.set(15, -4, 26, "minecraft:candle[candles=1,lit=true]");
        s.set(16, -5, 23, "minecraft:soul_campfire[lit=true,facing=east,signal_fire=false]");
        s.set(16, -5, 28, "minecraft:soul_campfire[lit=true,facing=east,signal_fire=false]");
        s.anchor(18, -5, 26, "krypta");
        s.mob(33, -5, 25, "zwiadowcy:wargal", "Sentry", (byte) 1, "BaseYaw", 90f);
        s.mob(19, -5, 27, "zwiadowcy:wargal_oszczepnik");
    }

    static int segmentHeight(Structure s, int seed, boolean taller) {
        double r = s.rnd.nextDouble();
        if (r < 0.18) return 0;
        if (r < 0.4) return 2 + s.rnd.nextInt(2);
        if (r < 0.75) return 4 + s.rnd.nextInt(2);
        return taller ? 7 + s.rnd.nextInt(2) : 6;
    }

    static void ruinColumn(Structure s, int x, int z, int h, String vineFace) {
        int hh = h <= 0 ? 0 : Math.max(1, h + s.rnd.nextInt(3) - 1);
        for (int y = 1; y <= hh; y++) s.set(x, y, z, s.pick(RUIN, RUIN_W));
        if (hh == 0 && s.rnd.nextDouble() < 0.4) s.set(x, 1, z, "minecraft:cobblestone");
        if (vineFace != null && hh >= 3 && s.rnd.nextDouble() < 0.25) {
            int vx = x, vz = z;
            String prop;
            switch (vineFace) {
                case "south" -> { vz = z - 1; prop = "south"; }
                case "north" -> { vz = z + 1; prop = "north"; }
                case "east" -> { vx = x - 1; prop = "east"; }
                default -> { vx = x + 1; prop = "west"; }
            }
            for (int y = 1 + s.rnd.nextInt(2); y <= hh - 1; y++) s.set(vx, y, vz, "minecraft:vine[" + prop + "=true]");
        }
    }

    private StructureGen() {}
}
