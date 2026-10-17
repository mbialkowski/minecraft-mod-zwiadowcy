package pl.zwiadowcy.assetgen;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Losowe struktury Araluenu (generowane przez świat w biomach Araluenu - patrz data/zwiadowcy/worldgen).
 * Pliki trafiają do data/zwiadowcy/structures/losowe/. Warianty "_b" są wybierane przez pulę szablonów.
 */
final class RandomStructures {
    static final String[] STONE = StructureGen.STONE;
    static final double[] STONE_W = StructureGen.STONE_W;
    static final String[] RUIN = StructureGen.RUIN;
    static final double[] RUIN_W = StructureGen.RUIN_W;

    static void all(File out) throws Exception {
        File dir = new File(out, "losowe");
        obozWargalow(dir, false);
        obozWargalow(dir, true);
        posterunek(dir);
        kryjowka(dir, false);
        kryjowka(dir, true);
        straznica(dir, false);
        straznica(dir, true);
        kurhan(dir, false);
        kurhan(dir, true);
        legowisko(dir, false);
        legowisko(dir, true);
        kamiennyKrag(dir);
        zajazd(dir);
        zagroda(dir);
    }

    static String stairs(String mat, String facing) {
        return "minecraft:" + mat + "_stairs[facing=" + facing + ",half=bottom]";
    }

    static void ground(Structure s, int cx, int cz, double r, String[] mats, double[] w) {
        for (int x = 0; x < s.sx; x++) for (int z = 0; z < s.sz; z++) {
            if (Math.hypot(x - cx, z - cz) <= r) s.set(x, 0, z, s.pick(mats, w));
        }
    }

    // ===================================================================== OBÓZ WARGALÓW
    static void obozWargalow(File dir, boolean withChief) throws Exception {
        Structure s = new Structure(23, 9, 23, withChief ? 9102 : 9101);
        int c = 11;
        ground(s, c, c, 10.5, new String[]{"minecraft:coarse_dirt", "minecraft:podzol", "minecraft:dirt", "minecraft:rooted_dirt"},
                new double[]{0.4, 0.25, 0.2, 0.15});
        // palisada z zaostrzonych pali
        for (int i = 0; i < 64; i++) {
            double a = i * Math.PI * 2 / 64;
            int x = c + (int) Math.round(Math.cos(a) * 10), z = c + (int) Math.round(Math.sin(a) * 10);
            boolean gate = (Math.abs(x - c) <= 1 && (z < 3 || z > 19));
            if (gate || s.rnd.nextDouble() < 0.1) continue;
            int h = 2 + s.rnd.nextInt(2);
            for (int y = 1; y <= h; y++) s.set(x, y, z, "minecraft:spruce_log[axis=y]");
            s.set(x, h + 1, z, "minecraft:spruce_fence");
        }
        // ognisko z rożnem
        s.set(c, 1, c, "minecraft:campfire[lit=true,facing=north,signal_fire=false]");
        for (int[] d : new int[][]{{-1, -1}, {1, -1}, {-1, 1}, {1, 1}, {0, -1}, {0, 1}, {-1, 0}, {1, 0}}) s.set(c + d[0], 0, c + d[1], "minecraft:cobblestone");
        s.fill(c - 1, 2, c, c - 1, 2, c, "minecraft:spruce_fence");
        s.fill(c + 1, 2, c, c + 1, 2, c, "minecraft:spruce_fence");
        // namioty ze skór
        hideTent(s, 3, 4, "minecraft:brown_wool");
        hideTent(s, 15, 4, "minecraft:black_wool");
        hideTent(s, 4, 15, "minecraft:brown_wool");
        // totem z czaszkami i czarną chorągwią
        s.fill(16, 1, 16, 16, 3, 16, "minecraft:spruce_fence");
        s.set(16, 4, 16, "minecraft:black_banner[rotation=8]");
        for (int[] p : new int[][]{{14, 17}, {17, 14}, {7, 12}, {12, 7}}) {
            s.fill(p[0], 1, p[1], p[0], 2, p[1], "minecraft:spruce_fence");
            s.set(p[0], 3, p[1], "minecraft:skeleton_skull[rotation=" + s.rnd.nextInt(16) + "]");
        }
        for (int i = 0; i < 8; i++) {
            int x = 3 + s.rnd.nextInt(17), z = 3 + s.rnd.nextInt(17);
            if (!s.has(x, 1, z)) s.set(x, 1, z, s.rnd.nextBoolean() ? "minecraft:bone_block[axis=x]" : "minecraft:bone_block[axis=z]");
        }
        s.chest(17, 1, 6, "west", "zwiadowcy:chests/losowe/oboz_wargalow");
        // kocioł ze strawą, skóry wokół ogniska, zapasy
        Decor.cookingPot(s, 14, 1, 14);
        for (int[] p : new int[][]{{c - 2, c + 1}, {c + 2, c - 1}, {c - 1, c - 2}}) s.set(p[0], 1, p[1], (p[0] + p[1]) % 2 == 0 ? "minecraft:brown_carpet" : "minecraft:black_carpet");
        s.set(19, 1, 7, "minecraft:barrel[facing=up,open=false]");
        s.set(19, 1, 8, "minecraft:barrel[facing=west,open=false]");
        s.set(8, 1, 16, "minecraft:hay_block");
        s.mob(c, 1, 2, "zwiadowcy:wargal", "Sentry", (byte) 1, "BaseYaw", 180f);
        s.mob(c - 3, 1, c + 2, "zwiadowcy:wargal_zwiadowca");
        s.mob(c + 3, 1, c - 2, "zwiadowcy:wargal_oszczepnik");
        s.mob(c, 1, c + 4, withChief ? "zwiadowcy:wargal_herszt" : "zwiadowcy:wargal");
        StructureGen.save(s, "losowe/oboz_wargalow" + (withChief ? "_b" : ""));
    }

    static void hideTent(Structure s, int x0, int z0, String wool) {
        for (int x = x0; x < x0 + 4; x++) {
            s.set(x, 1, z0, wool);
            s.set(x, 1, z0 + 4, wool);
            s.set(x, 2, z0 + 1, wool);
            s.set(x, 2, z0 + 3, wool);
            s.set(x, 3, z0 + 2, "minecraft:spruce_log[axis=x]");
        }
        s.set(x0 + 3, 1, z0 + 2, wool);
        s.set(x0 + 1, 1, z0 + 2, "minecraft:hay_block");
        // posłanie ze skór
        s.set(x0 + 2, 1, z0 + 2, "minecraft:brown_carpet");
        s.set(x0, 1, z0 + 2, "minecraft:black_carpet");
    }

    // ===================================================================== POSTERUNEK MORGARATHA
    static void posterunek(File dir) throws Exception {
        Structure s = new Structure(15, 24, 15, 9201);
        String[] dark = {"minecraft:deepslate_bricks", "minecraft:cracked_deepslate_bricks", "minecraft:polished_blackstone_bricks"};
        double[] dw = {0.6, 0.2, 0.2};
        s.fillMix(2, 0, 2, 12, 0, 12, new String[]{"minecraft:polished_blackstone", "minecraft:blackstone", "minecraft:cobbled_deepslate"},
                new double[]{0.4, 0.3, 0.3});
        int x0 = 4, x1 = 10, top = 16;
        s.wallsMix(x0, 1, x0, x1, top, x1, dark, dw);
        s.clear(x0 + 1, 1, x0 + 1, x1 - 1, top - 1, x1 - 1);
        for (int y : new int[]{6, 12}) {
            s.fill(x0 + 1, y, x0 + 1, x1 - 1, y, x1 - 1, "minecraft:dark_oak_planks");
            s.set(x0 + 1, y, x0 + 1, "minecraft:air");
        }
        s.fill(x0 + 1, top, x0 + 1, x1 - 1, top, x1 - 1, "minecraft:polished_blackstone");
        s.set(x0 + 1, top, x0 + 1, "minecraft:air");
        for (int y = 1; y <= top; y++) s.set(x0 + 1, y, x0 + 1, "minecraft:ladder[facing=east]");
        // blanki i koksowniki
        for (int x = x0; x <= x1; x++) for (int z = x0; z <= x1; z++) {
            boolean edge = x == x0 || x == x1 || z == x0 || z == x1;
            if (edge && (x + z) % 2 == 0) s.set(x, top + 1, z, "minecraft:polished_blackstone_wall");
        }
        for (int[] p : new int[][]{{x0, x0}, {x1, x0}, {x0, x1}, {x1, x1}}) {
            s.set(p[0], top + 1, p[1], "minecraft:polished_blackstone");
            s.set(p[0], top + 2, p[1], "zwiadowcy:palenisko[facing=north]");
        }
        s.clear(7, 1, x1, 7, 2, x1);
        for (int y : new int[]{4, 9, 14}) {
            s.set(7, y, x0, "minecraft:iron_bars");
            s.set(x0, y, 7, "minecraft:iron_bars");
            s.set(x1, y, 7, "minecraft:iron_bars");
        }
        s.set(6, 8, x1 + 1, "minecraft:black_wall_banner[facing=south]");
        s.set(8, 8, x1 + 1, "minecraft:black_wall_banner[facing=south]");
        s.set(7, 8, x0 - 1, "minecraft:black_wall_banner[facing=north]");
        s.chest(9, 13, 9, "west", "zwiadowcy:chests/losowe/posterunek_morgaratha");
        s.set(9, 7, 9, "minecraft:soul_lantern[hanging=false]");
        // parter: stół wartowni, broń na ścianie, czarne skóry; piętro: legowiska z siana; szczyt: mapa podbojów
        Decor.table(s, 8, 1, 7, "dark_oak", "minecraft:soul_lantern[hanging=false]");
        Decor.chair(s, 8, 1, 8, "dark_oak", "north");
        Decor.rug(s, 6, 6, 8, 8, 1, "black", "gray");
        Decor.itemFrame(s, 9, 2, 6, "west", "minecraft:iron_axe");
        Decor.itemFrame(s, 9, 2, 8, "west", "minecraft:stone_sword");
        for (int y : new int[]{5, 11, 15}) s.set(7, y, 7, "minecraft:soul_lantern[hanging=true]");
        s.set(8, 7, 5, "minecraft:hay_block");
        s.set(9, 7, 5, "minecraft:hay_block");
        Decor.rug(s, 6, 7, 8, 8, 7, "black", "black");
        s.set(9, 13, 8, "minecraft:barrel[facing=up,open=false]");
        Decor.table(s, 7, 13, 8, "dark_oak", "minecraft:candle[candles=3,lit=true]");
        Decor.itemFrame(s, 5, 14, 8, "east", "minecraft:map");
        s.set(7, 13, 5, "minecraft:black_carpet");
        s.set(8, 13, 5, "minecraft:black_carpet");
        // pale z czaszkami wokół
        for (int[] p : new int[][]{{1, 1}, {13, 1}, {1, 13}, {13, 13}, {7, 13}, {1, 7}}) {
            s.fill(p[0], 1, p[1], p[0], 2, p[1], "minecraft:dark_oak_fence");
            s.set(p[0], 3, p[1], "minecraft:skeleton_skull[rotation=" + s.rnd.nextInt(16) + "]");
        }
        s.mob(7, 1, 12, "zwiadowcy:wargal_herszt");
        s.mob(7, top + 1, 7, "zwiadowcy:wargal_oszczepnik", "Sentry", (byte) 1, "BaseYaw", 0f);
        s.mob(8, top + 1, 6, "zwiadowcy:wargal_oszczepnik", "Sentry", (byte) 1, "BaseYaw", 180f);
        s.mob(3, 1, 7, "zwiadowcy:wargal");
        StructureGen.save(s, "losowe/posterunek_morgaratha");
    }

    // ===================================================================== KRYJÓWKA BANDYTÓW
    static void kryjowka(File dir, boolean withChief) throws Exception {
        Structure s = new Structure(25, 11, 25, withChief ? 9302 : 9301);
        int c = 12;
        ground(s, c, c, 11.5, new String[]{"minecraft:coarse_dirt", "minecraft:dirt_path", "minecraft:grass_block", "minecraft:podzol"},
                new double[]{0.3, 0.2, 0.35, 0.15});
        for (int x = 1; x <= 23; x++) for (int z = 1; z <= 23; z++) {
            boolean edge = x == 1 || x == 23 || z == 1 || z == 23;
            if (!edge || (z == 23 && x >= 11 && x <= 13)) continue;
            int h = 3 + (s.rnd.nextDouble() < 0.3 ? 1 : 0);
            for (int y = 1; y <= h; y++) s.set(x, y, z, "minecraft:stripped_oak_log[axis=y]");
        }
        s.set(11, 1, 23, "minecraft:oak_fence_gate[facing=south,open=true,in_wall=false]");
        s.set(13, 1, 23, "minecraft:oak_fence_gate[facing=south,open=true,in_wall=false]");
        hut(s, 3, 3, "north");
        hut(s, 15, 3, "north");
        // wieżyczka łucznika
        for (int[] p : new int[][]{{18, 16}, {20, 16}, {18, 18}, {20, 18}}) s.fill(p[0], 1, p[1], p[0], 4, p[1], "minecraft:oak_log[axis=y]");
        s.fill(18, 5, 16, 20, 5, 18, "minecraft:oak_planks");
        for (int x = 18; x <= 20; x++) { s.set(x, 6, 16, "minecraft:oak_fence"); if (x != 19) s.set(x, 6, 18, "minecraft:oak_fence"); }
        s.set(18, 6, 17, "minecraft:oak_fence");
        s.fill(19, 1, 18, 19, 4, 18, "minecraft:oak_log[axis=y]");
        for (int y = 1; y <= 5; y++) s.set(19, y, 19, "minecraft:ladder[facing=south]");
        // ognisko, ławy, łupy
        Decor.cookingPot(s, 10, 1, 13);
        Decor.tableSet(s, 16, 1, 11, "oak", "minecraft:candle[candles=2,lit=true]");
        Decor.decoratedPot(s, 7, 1, 20);
        Decor.decoratedPot(s, 3, 1, 20);
        s.set(8, 1, 13, "minecraft:oak_log[axis=z]");
        s.set(12, 1, 13, "minecraft:oak_log[axis=z]");
        s.set(10, 1, 15, "minecraft:oak_log[axis=x]");
        s.chest(4, 1, 19, "east", "zwiadowcy:chests/losowe/kryjowka_bandytow");
        s.barrel(5, 1, 20, null);
        s.barrel(4, 1, 20, "zwiadowcy:chests/losowe/kryjowka_bandytow");
        s.barrel(5, 2, 20, null);
        s.set(6, 1, 20, "minecraft:hay_block");
        s.set(14, 2, 9, "minecraft:red_wool");
        s.set(14, 1, 9, "minecraft:oak_fence");
        s.mob(10, 1, 11, "zwiadowcy:bandyta");
        s.mob(7, 1, 16, "zwiadowcy:bandyta");
        s.mob(19, 6, 17, "zwiadowcy:lucznik_bandytow");
        if (withChief) s.mob(9, 1, 6, "zwiadowcy:herszt_bandytow");
        StructureGen.save(s, "losowe/kryjowka_bandytow" + (withChief ? "_b" : ""));
    }

    static void hut(Structure s, int x0, int z0, String door) {
        int x1 = x0 + 5, z1 = z0 + 4;
        s.fill(x0, 0, z0, x1, 0, z1, "minecraft:oak_planks");
        s.walls(x0, 1, z0, x1, 3, z1, "minecraft:oak_planks");
        for (int y = 1; y <= 3; y++) for (int[] p : new int[][]{{x0, z0}, {x1, z0}, {x0, z1}, {x1, z1}}) s.set(p[0], y, p[1], "minecraft:oak_log[axis=y]");
        s.fill(x0 - 1, 4, z0 - 1, x1 + 1, 4, z1 + 1, "minecraft:spruce_slab[type=bottom]");
        s.door((x0 + x1) / 2, 1, z1, "south", "oak");
        s.set(x0 + 1, 2, z0, "minecraft:glass_pane");
        s.bed(x0 + 1, 1, z0 + 1, "east", "red");
        s.set(x1 - 1, 1, z0 + 1, "minecraft:crafting_table");
        s.set(x1 - 1, 3, z0 + 2, "minecraft:lantern[hanging=true]");
        // skromny wystrój: świeca na stole, skrzynka z łupem, chodnik, broń na ścianie
        s.set(x1 - 1, 2, z0 + 1, "minecraft:candle[candles=1,lit=true]");
        s.set(x1 - 1, 1, z0 + 3, "minecraft:barrel[facing=up,open=false]");
        s.set(x1 - 1, 2, z0 + 3, "minecraft:potted_dead_bush");
        Decor.rug(s, x0 + 1, z0 + 2, x0 + 3, z0 + 3, 1, "red", "red");
        Decor.itemFrame(s, x0 + 1, 2, z0 + 3, "north", "minecraft:iron_sword");
    }

    // ===================================================================== RUINA STRAŻNICY
    static void straznica(File dir, boolean occupied) throws Exception {
        Structure s = new Structure(11, 17, 11, occupied ? 9402 : 9401);
        int c = 5;
        s.fillMix(0, 0, 0, 10, 0, 10, new String[]{"minecraft:cobblestone", "minecraft:mossy_cobblestone", "minecraft:grass_block", "minecraft:gravel"},
                new double[]{0.3, 0.25, 0.3, 0.15});
        for (int x = 0; x < 11; x++) for (int z = 0; z < 11; z++) {
            double d = Math.hypot(x - c, z - c);
            if (d > 4.6 || d < 3.4) continue;
            double ang = Math.atan2(z - c, x - c);
            int h = (int) (8 + 6 * Math.cos(ang - 0.8)) + s.rnd.nextInt(2);
            for (int y = 1; y <= h; y++) s.set(x, y, z, s.pick(RUIN, RUIN_W));
        }
        s.clear(c, 1, 9, c, 2, 9);
        s.clear(c, 1, 8, c, 2, 8);
        for (int x = 2; x <= 8; x++) for (int z = 2; z <= 8; z++) {
            if (Math.hypot(x - c, z - c) < 3.4 && s.rnd.nextDouble() < 0.8) s.set(x, 6, z, "minecraft:spruce_planks");
        }
        for (int y = 1; y <= 6; y++) s.set(8, y, c, "minecraft:ladder[facing=west]");
        s.set(8, 6, c, "minecraft:air");
        s.chest(6, 7, 6, "west", "zwiadowcy:chests/losowe/straznica");
        // ślady obozowania: wygasłe ognisko i posłanie na dole, stolik ze świecą na pomoście
        s.set(4, 1, 4, "minecraft:campfire[lit=false,facing=north,signal_fire=false]");
        s.set(3, 1, 6, "minecraft:brown_carpet");
        s.set(3, 1, 7, "minecraft:brown_carpet");
        s.set(6, 1, 3, "minecraft:barrel[facing=up,open=false]");
        s.set(4, 6, 6, "minecraft:spruce_planks");
        s.set(3, 6, 6, "minecraft:spruce_planks");
        Decor.table(s, 4, 7, 6, "spruce", "minecraft:candle[candles=1,lit=" + occupied + "]");
        Decor.chair(s, 3, 7, 6, "spruce", "east");
        for (int i = 0; i < 10; i++) {
            int x = s.rnd.nextInt(11), z = s.rnd.nextInt(11);
            if (Math.hypot(x - c, z - c) > 4.8) s.set(x, 1, z, s.rnd.nextBoolean() ? "minecraft:cobblestone" : "minecraft:mossy_cobblestone_slab[type=bottom]");
        }
        if (occupied) {
            s.mob(4, 7, 5, "zwiadowcy:lucznik_bandytow");
            s.mob(c, 1, 5, "zwiadowcy:bandyta");
        }
        StructureGen.save(s, "losowe/straznica" + (occupied ? "_b" : ""));
    }

    // ===================================================================== KURHAN (krypta pod ziemią)
    static void kurhan(File dir, boolean robbers) throws Exception {
        Structure s = new Structure(21, 12, 21, robbers ? 9502 : 9501, 5);
        int c = 10;
        // nasyp z ziemi, darń na wierzchu, kamienny krąg u podstawy
        for (int x = 0; x < 21; x++) for (int z = 0; z < 21; z++) {
            double d = Math.hypot(x - c, z - c);
            if (d > 9.5) continue;
            int h = (int) Math.round(5.5 * Math.cos(Math.min(1.0, d / 9.5) * Math.PI / 2));
            for (int y = 1; y <= h; y++) s.set(x, y, z, y == h ? "minecraft:grass_block" : "minecraft:dirt");
            if (d > 8.6) s.set(x, 1, z, "minecraft:mossy_cobblestone");
        }
        // korytarz ze wschodu
        for (int x = 12; x <= 20; x++) {
            s.fill(x, 0, 9, x, 0, 11, "minecraft:mossy_stone_bricks");
            s.fill(x, 1, 9, x, 3, 9, "minecraft:mossy_stone_bricks");
            s.fill(x, 1, 11, x, 3, 11, "minecraft:mossy_stone_bricks");
            s.fill(x, 1, 10, x, 2, 10, "minecraft:air");
            s.set(x, 3, 10, "minecraft:stone_bricks");
        }
        s.set(20, 1, 9, "minecraft:chiseled_stone_bricks");
        s.set(20, 1, 11, "minecraft:chiseled_stone_bricks");
        // schody w dół do krypty
        for (int i = 0; i < 4; i++) {
            int x = 11 - i, y = -i;
            s.fill(x, y - 1, 9, x, y + 2, 11, "minecraft:stone_bricks");
            s.set(x, y, 10, stairs("stone_brick", "east"));
            s.fill(x, y + 1, 10, x, y + 2, 10, "minecraft:air");
        }
        // komora grobowa (y -5..-1)
        s.fillMix(3, -5, 6, 8, -1, 14, new String[]{"minecraft:stone_bricks", "minecraft:mossy_stone_bricks", "minecraft:cracked_stone_bricks"},
                new double[]{0.5, 0.3, 0.2});
        s.clear(4, -4, 7, 7, -2, 13);
        s.clear(7, -4, 10, 8, -2, 10);
        s.fill(5, -4, 9, 6, -4, 11, "minecraft:stone_brick_slab[type=top]");
        s.set(5, -3, 10, "minecraft:chiseled_stone_bricks");
        s.chest(4, -4, 10, "east", "zwiadowcy:chests/losowe/kurhan");
        s.set(4, -4, 7, "minecraft:candle[candles=3,lit=false]");
        s.set(4, -4, 13, "minecraft:candle[candles=2,lit=false]");
        s.set(7, -4, 7, "minecraft:cobweb");
        s.set(7, -2, 13, "minecraft:cobweb");
        s.set(6, -4, 13, "minecraft:skeleton_skull[rotation=4]");
        s.set(5, -4, 7, "minecraft:bone_block[axis=x]");
        s.set(6, -2, 10, "minecraft:soul_lantern[hanging=true]");
        // urny grobowe i świece na sarkofagu (zapalone, jeśli w kurhanie siedzą rabusie)
        for (int[] p : new int[][]{{4, 8}, {4, 12}, {7, 13}}) Decor.decoratedPot(s, p[0], -4, p[1]);
        s.set(6, -3, 9, "minecraft:candle[candles=1,lit=" + robbers + "]");
        s.set(6, -3, 11, "minecraft:candle[candles=2,lit=" + robbers + "]");
        if (robbers) {
            s.mob(6, -4, 8, "zwiadowcy:bandyta");
            s.mob(6, -4, 12, "zwiadowcy:bandyta");
            s.set(14, 1, 10, "minecraft:torch");
        }
        StructureGen.save(s, "losowe/kurhan" + (robbers ? "_b" : ""));
    }

    // ===================================================================== LEGOWISKO NIEDŹWIEDZIA
    static void legowisko(File dir, boolean cub) throws Exception {
        Structure s = new Structure(14, 9, 14, cub ? 9602 : 9601);
        int c = 7;
        String[] rock = {"minecraft:stone", "minecraft:andesite", "minecraft:mossy_cobblestone", "minecraft:cobblestone", "minecraft:moss_block"};
        double[] rw = {0.35, 0.25, 0.2, 0.1, 0.1};
        for (int x = 0; x < 14; x++) for (int z = 0; z < 14; z++) {
            double d = Math.hypot(x - c, (z - c) * 1.1);
            if (d > 6) continue;
            int h = (int) Math.round(5 * Math.cos(Math.min(1.0, d / 6) * Math.PI / 2));
            for (int y = 1; y <= h; y++) s.set(x, y, z, s.pick(rock, rw));
        }
        s.clear(5, 1, 5, 9, 2, 9);
        s.clear(6, 1, 9, 8, 2, 13);
        s.fill(5, 0, 5, 9, 0, 9, "minecraft:moss_block");
        s.set(5, 1, 5, "minecraft:bone_block[axis=x]");
        s.set(9, 1, 6, "minecraft:moss_carpet");
        s.chest(5, 1, 7, "east", "zwiadowcy:chests/losowe/legowisko_niedzwiedzia");
        for (int[] b : new int[][]{{1, 11}, {12, 10}, {2, 2}, {11, 2}}) s.set(b[0], 1, b[1], "minecraft:sweet_berry_bush[age=3]");
        s.tree(12, 1, 12, "spruce", 5);
        s.tree(1, 1, 6, "spruce", 6);
        for (int i = 0; i < 6; i++) {
            int x = s.rnd.nextInt(14), z = 10 + s.rnd.nextInt(4);
            if (!s.has(x, 1, z)) s.set(x, 1, z, "minecraft:fern");
        }
        s.mob(7, 1, 7, "zwiadowcy:niedzwiedz");
        if (cub) s.mob(8, 1, 10, "zwiadowcy:niedzwiedz", "Age", -24000);
        StructureGen.save(s, "losowe/legowisko_niedzwiedzia" + (cub ? "_b" : ""));
    }

    // ===================================================================== KAMIENNY KRĄG
    static void kamiennyKrag(File dir) throws Exception {
        Structure s = new Structure(17, 8, 17, 9701);
        int c = 8;
        String[] stones = {"minecraft:stone", "minecraft:andesite", "minecraft:cobblestone", "minecraft:mossy_cobblestone", "minecraft:smooth_stone"};
        double[] sw = {0.3, 0.25, 0.15, 0.2, 0.1};
        for (int i = 0; i < 9; i++) {
            double a = i * Math.PI * 2 / 9;
            int x = c + (int) Math.round(Math.cos(a) * 6), z = c + (int) Math.round(Math.sin(a) * 6);
            if (i == 4) {
                for (int k = 0; k < 3; k++) s.set(x + k, 1, z, s.pick(stones, sw));
                continue;
            }
            int h = 2 + s.rnd.nextInt(3);
            for (int y = 1; y <= h; y++) s.set(x, y, z, s.pick(stones, sw));
            if (s.rnd.nextBoolean()) s.set(x, h + 1, z, "minecraft:moss_carpet");
        }
        s.fill(c - 1, 0, c - 1, c + 1, 0, c + 1, "minecraft:polished_andesite");
        // ołtarz z rzeźbionego kamienia ze świecami
        s.set(c, 1, c, "minecraft:chiseled_stone_bricks");
        s.set(c, 2, c, "minecraft:candle[candles=4,lit=true]");
        s.set(c, 1, c + 1, "minecraft:stone_brick_stairs[facing=north,half=bottom]");
        s.set(c, 1, c - 1, "minecraft:stone_brick_stairs[facing=south,half=bottom]");
        s.set(c - 1, 1, c, "minecraft:moss_carpet");
        s.chest(c + 1, 1, c, "west", "zwiadowcy:chests/losowe/kamienny_krag");
        String[] flowers = {"minecraft:cornflower", "minecraft:poppy", "minecraft:oxeye_daisy", "minecraft:dandelion", "minecraft:azure_bluet"};
        for (int i = 0; i < 18; i++) {
            int x = 2 + s.rnd.nextInt(13), z = 2 + s.rnd.nextInt(13);
            if (!s.has(x, 1, z) && Math.hypot(x - c, z - c) > 2) s.set(x, 1, z, flowers[s.rnd.nextInt(flowers.length)]);
        }
        s.set(c + 3, 1, c + 2, "minecraft:campfire[lit=false,facing=north,signal_fire=false]");
        s.set(c + 3, 1, c + 4, "minecraft:brown_carpet");
        s.set(c + 4, 1, c + 4, "minecraft:brown_carpet");
        StructureGen.save(s, "losowe/kamienny_krag");
    }

    // ===================================================================== ZAJAZD PRZYDROŻNY
    static void zajazd(File dir) throws Exception {
        Structure s = new Structure(24, 14, 20, 9801);
        s.fill(0, 0, 15, 23, 0, 17, "minecraft:dirt_path");
        int x0 = 3, z0 = 3, x1 = 14, z1 = 11;
        s.fill(x0, 0, z0, x1, 0, z1, "minecraft:cobblestone");
        s.fill(x0 + 1, 0, z0 + 1, x1 - 1, 0, z1 - 1, "minecraft:spruce_planks");
        s.walls(x0, 1, z0, x1, 7, z1, "minecraft:white_terracotta");
        s.walls(x0, 1, z0, x1, 1, z1, "minecraft:cobblestone");
        for (int y = 1; y <= 7; y++) for (int[] p : new int[][]{{x0, z0}, {x1, z0}, {x0, z1}, {x1, z1}, {8, z0}, {8, z1}}) s.set(p[0], y, p[1], "minecraft:dark_oak_log[axis=y]");
        for (int x = x0; x <= x1; x++) { s.set(x, 4, z0, "minecraft:stripped_dark_oak_log[axis=x]"); s.set(x, 4, z1, "minecraft:stripped_dark_oak_log[axis=x]"); }
        s.fill(x0 + 1, 4, z0 + 1, x1 - 1, 4, z1 - 1, "minecraft:spruce_planks");
        s.gableRoof(x0, z0, x1, z1, 8, "dark_oak", "minecraft:white_terracotta", true);
        s.door(8, 1, z1, "south", "spruce");
        for (int x : new int[]{5, 11}) { s.set(x, 2, z1, "minecraft:glass_pane"); s.set(x, 6, z1, "minecraft:glass_pane"); s.set(x, 2, z0, "minecraft:glass_pane"); }
        // szynk
        s.fill(x0 + 1, 1, 6, x0 + 4, 1, 6, "minecraft:barrel[facing=up,open=false]");
        s.fill(x0 + 1, 2, 6, x0 + 4, 2, 6, "minecraft:spruce_slab[type=bottom]");
        s.chest(x0 + 1, 1, 4, "south", "zwiadowcy:chests/losowe/zajazd");
        s.set(x0 + 2, 1, 4, "minecraft:smoker[facing=south,lit=true]");
        s.set(x0 + 3, 1, 4, "minecraft:barrel[facing=up,open=false]");
        for (int[] t : new int[][]{{10, 6}, {12, 9}, {10, 9}}) {
            s.set(t[0], 1, t[1], "minecraft:spruce_fence");
            s.set(t[0], 2, t[1], "minecraft:spruce_pressure_plate");
            s.set(t[0] - 1, 1, t[1], stairs("spruce", "west"));
            s.set(t[0] + 1, 1, t[1], stairs("spruce", "east"));
        }
        s.set(8, 3, 7, "minecraft:lantern[hanging=true]");
        s.set(12, 3, 5, "minecraft:lantern[hanging=true]");
        // izba: kominek przy zachodniej ścianie, świece na stołach, dywan, beczki z piwem, obrazy
        Decor.fireplace(s, x0 + 1, 1, 8, "east", 13, "minecraft:bricks");
        s.set(x0 + 2, 4, 7, "minecraft:spruce_planks");
        s.set(x0 + 2, 4, 9, "minecraft:spruce_planks");
        for (int[] t : new int[][]{{10, 6}, {12, 9}, {10, 9}}) s.set(t[0], 2, t[1], "minecraft:candle[candles=" + (1 + (t[0] + t[1]) % 3) + ",lit=true]");
        Decor.rug(s, 5, 8, 7, 10, 1, "brown", "orange");
        s.set(7, 1, 4, "minecraft:barrel[facing=south,open=false]");
        s.set(6, 2, 4, "minecraft:barrel[facing=south,open=false]");
        s.set(x0 + 1, 3, 6, "minecraft:potted_fern");
        s.set(x0 + 4, 3, 6, "minecraft:potted_red_mushroom");
        Decor.itemFrame(s, 9, 3, 4, "south", "minecraft:honey_bottle");
        Decor.painting(s, 6, 3, 10, "north", "sunset");
        Decor.painting(s, 12, 3, 10, "north", "pool");
        // piętro: izby (drabina)
        for (int y = 1; y <= 4; y++) s.set(x1 - 1, y, z0 + 1, "minecraft:ladder[facing=west]");
        s.set(x1 - 1, 4, z0 + 1, "minecraft:air");
        s.bed(x0 + 2, 5, z0 + 2, "south", "red");
        s.bed(x0 + 5, 5, z0 + 2, "south", "blue");
        s.bed(x0 + 8, 5, z0 + 2, "south", "green");
        for (int x = 4; x <= 13; x++) s.set(x, 7, 7, "minecraft:stripped_dark_oak_log[axis=x]");
        s.set(8, 6, 7, "minecraft:lantern[hanging=true]");
        // stajnia i studnia
        s.fill(16, 4, 4, 21, 4, 10, "minecraft:spruce_slab[type=top]");
        for (int[] p : new int[][]{{16, 4}, {21, 4}, {16, 10}, {21, 10}}) s.fill(p[0], 1, p[1], p[0], 3, p[1], "minecraft:spruce_fence");
        for (int z = 4; z <= 10; z++) s.set(21, 1, z, "minecraft:spruce_fence");
        s.set(20, 1, 5, "minecraft:hay_block");
        s.set(20, 1, 9, "minecraft:water_cauldron[level=3]");
        s.set(19, 0, 13, "minecraft:water");
        for (int[] p : new int[][]{{18, 12}, {19, 12}, {20, 12}, {18, 13}, {20, 13}, {18, 14}, {19, 14}, {20, 14}}) s.set(p[0], 1, p[1], "minecraft:cobblestone");
        s.fill(17, 1, 12, 17, 3, 12, "minecraft:oak_fence");
        s.set(17, 4, 12, "minecraft:lantern[hanging=false]");
        s.set(2, 1, 13, "minecraft:spruce_fence");
        s.set(2, 2, 13, "minecraft:spruce_fence");
        s.set(2, 3, 13, "minecraft:lantern[hanging=false]");
        s.npcEntity(x0 + 2, 1, 5, "karczmarz", 0f);
        s.npcEntity(10, 1, 8, "wiesniak", 90f);
        Map<String, Object> horse = new LinkedHashMap<>();
        horse.put("id", "minecraft:horse");
        horse.put("PersistenceRequired", (byte) 1);
        s.entity(18.5, 1, 7.5, horse);
        StructureGen.save(s, "losowe/zajazd");
    }

    // ===================================================================== ZAGRODA CHŁOPSKA
    static void zagroda(File dir) throws Exception {
        Structure s = new Structure(22, 12, 20, 9901);
        StructureGen.house(s, 2, 2, 8, 7, 4, "south", "spruce");
        // stodoła
        int bx0 = 11, bz0 = 2, bx1 = 19, bz1 = 8;
        s.fill(bx0, 0, bz0, bx1, 0, bz1, "minecraft:coarse_dirt");
        s.walls(bx0, 1, bz0, bx1, 4, bz1, "minecraft:spruce_planks");
        for (int y = 1; y <= 4; y++) for (int[] p : new int[][]{{bx0, bz0}, {bx1, bz0}, {bx0, bz1}, {bx1, bz1}}) s.set(p[0], y, p[1], "minecraft:oak_log[axis=y]");
        s.clear(14, 1, bz1, 16, 3, bz1);
        s.gableRoof(bx0, bz0, bx1, bz1, 5, "spruce", "minecraft:spruce_planks", true);
        s.fill(bx0 + 1, 1, bz0 + 1, bx0 + 2, 2, bz0 + 2, "minecraft:hay_block");
        s.chest(bx1 - 1, 1, bz0 + 1, "west", "zwiadowcy:chests/losowe/zagroda");
        // stodoła: belka z latarnią, narzędzia na ścianie, kompostownik i beczki
        for (int x = bx0 + 1; x <= bx1 - 1; x++) s.set(x, 4, 5, "minecraft:stripped_spruce_log[axis=x]");
        s.set(15, 3, 5, "minecraft:lantern[hanging=true]");
        Decor.itemFrame(s, 13, 3, bz1 - 1, "north", "minecraft:shears");
        Decor.itemFrame(s, 17, 3, bz1 - 1, "north", "minecraft:bucket");
        s.set(bx1 - 1, 1, bz0 + 2, "minecraft:barrel[facing=up,open=false]");
        s.set(bx1 - 1, 1, bz0 + 3, "minecraft:composter[level=5]");
        s.set(bx0 + 1, 1, bz1 - 1, "minecraft:grindstone[face=floor,facing=east]");
        // ul i kwiaty przy domu
        s.set(9, 1, 3, "minecraft:beehive[facing=east,honey_level=3]");
        Decor.pot(s, 4, 1, 8, "poppy");
        Decor.pot(s, 6, 1, 8, "dandelion");
        // zagroda dla zwierząt
        for (int x = 11; x <= 20; x++) { s.set(x, 1, 10, "minecraft:oak_fence"); s.set(x, 1, 17, "minecraft:oak_fence"); }
        for (int z = 10; z <= 17; z++) { s.set(11, 1, z, "minecraft:oak_fence"); s.set(20, 1, z, "minecraft:oak_fence"); }
        s.set(15, 1, 10, "minecraft:oak_fence_gate[facing=north,open=false,in_wall=false]");
        s.set(18, 1, 15, "minecraft:water_cauldron[level=3]");
        // pole
        for (int x = 2; x <= 8; x++) for (int z = 11; z <= 17; z++) {
            if (x == 5) { s.set(x, 0, z, "minecraft:water"); continue; }
            s.set(x, 0, z, "minecraft:farmland[moisture=7]");
            s.set(x, 1, z, x < 5 ? "minecraft:wheat[age=7]" : "minecraft:beetroots[age=3]");
        }
        // strach na wróble
        Map<String, Object> scare = new LinkedHashMap<>();
        scare.put("id", "minecraft:armor_stand");
        List<Object> armor = new ArrayList<>();
        armor.add(new LinkedHashMap<String, Object>());
        armor.add(new LinkedHashMap<String, Object>());
        Map<String, Object> chest = new LinkedHashMap<>();
        chest.put("id", "minecraft:leather_chestplate");
        chest.put("Count", (byte) 1);
        armor.add(chest);
        Map<String, Object> head = new LinkedHashMap<>();
        head.put("id", "minecraft:carved_pumpkin");
        head.put("Count", (byte) 1);
        armor.add(head);
        scare.put("ArmorItems", armor);
        scare.put("CustomName", "{\"text\":\"Strach na wróble\"}");
        s.entity(5.5, 1, 14.5, scare);
        s.set(5, 0, 14, "minecraft:farmland[moisture=7]");
        s.npcEntity(9, 1, 10, "chlopka", 0f);
        s.npcEntity(15, 1, 9, "wiesniak", 180f);
        for (String animal : new String[]{"minecraft:cow", "minecraft:cow", "minecraft:sheep", "minecraft:sheep", "minecraft:chicken", "minecraft:chicken", "minecraft:pig"}) {
            Map<String, Object> a = new LinkedHashMap<>();
            a.put("id", animal);
            a.put("PersistenceRequired", (byte) 1);
            s.entity(12.5 + s.rnd.nextInt(7), 1, 11.5 + s.rnd.nextInt(5), a);
        }
        s.tree(0, 1, 19, "oak", 5);
        StructureGen.save(s, "losowe/zagroda");
    }

    private RandomStructures() {}
}
