package pl.zwiadowcy.assetgen;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lokacje tomu II („Płonący most”), akty I i II. Osie jak w {@link StructureGen}: x - wschód, z - południe,
 * y=0 grunt, y=1 poziom chodzenia. Kotwice są interpretowane przez misje (data/zwiadowcy/quests/*.json).
 */
final class Tom2Structures {
    static final String[] CELT_STONE = {"minecraft:cobblestone", "minecraft:mossy_cobblestone", "minecraft:stone", "minecraft:andesite"};
    static final double[] CELT_STONE_W = {0.45, 0.25, 0.15, 0.15};
    static final String[] ROCK = {"minecraft:stone", "minecraft:andesite", "minecraft:cobblestone", "minecraft:tuff", "minecraft:gravel"};
    static final double[] ROCK_W = {0.4, 0.25, 0.15, 0.15, 0.05};

    static void all() throws Exception {
        obozNaTrakcie();
        przeleczWiatrow();
        treGwyn();
        kopalnia();
        dolinaMgiel();
        szalasPasterski();
        posterunekDobosza();
        wrzosowisko();
    }

    static void save(Structure s, String name) throws Exception {
        StructureGen.save(s, name);
    }

    /** Ognisko zwiadowców (punkt szybkiej podróży) z ławami z bali. */
    static void scoutFire(Structure s, int x, int z) {
        s.set(x, 1, z, "zwiadowcy:ognisko_zwiadowcow[facing=north]");
        s.fillMix(x - 2, 0, z - 2, x + 2, 0, z + 2, new String[]{"minecraft:coarse_dirt", "minecraft:dirt_path"}, new double[]{0.5, 0.5});
        s.set(x - 2, 1, z, "minecraft:spruce_log[axis=z]");
        s.set(x + 2, 1, z, "minecraft:spruce_log[axis=z]");
        s.set(x, 1, z - 2, "minecraft:spruce_log[axis=x]");
    }

    /** Okrągła celtycka chata z kamienia, z dachem ze strzechy (bel siana) i drzwiami w stronę „facing”. */
    static void roundHut(Structure s, int cx, int cz, int r, String doorFacing, boolean ruined) {
        for (int x = cx - r; x <= cx + r; x++) for (int z = cz - r; z <= cz + r; z++) {
            double d = Math.hypot(x - cx, z - cz);
            if (d <= r + 0.5) s.set(x, 0, z, "minecraft:cobblestone");
            if (d > r - 0.5 && d <= r + 0.5) {
                int h = ruined ? 1 + s.rnd.nextInt(3) : 3;
                for (int y = 1; y <= h; y++) s.set(x, y, z, s.pick(CELT_STONE, CELT_STONE_W));
            } else if (d <= r - 0.5) {
                s.set(x, 0, z, "minecraft:packed_mud");
            }
        }
        if (!ruined) {
            for (int i = 0; i <= r; i++) {
                int rr = r - i + 1;
                for (int x = cx - rr; x <= cx + rr; x++) for (int z = cz - rr; z <= cz + rr; z++) {
                    double d = Math.hypot(x - cx, z - cz);
                    if (d <= rr + 0.3 && d > rr - 1.0) s.set(x, 4 + i, z, "minecraft:hay_block");
                }
            }
            s.set(cx, 4 + r + 1, cz, "minecraft:hay_block");
        }
        int dx = Decor.dx(doorFacing), dz = Decor.dz(doorFacing);
        int doorX = cx + dx * r, doorZ = cz + dz * r;
        s.set(doorX, 1, doorZ, "minecraft:air");
        s.set(doorX, 2, doorZ, "minecraft:air");
        // wnętrze: palenisko pośrodku, posłanie, beczka
        if (!ruined) {
            s.set(cx, 1, cz, "minecraft:campfire[lit=false,facing=north,signal_fire=false]");
            s.bed(cx - dx * (r - 1) + dz, 1, cz - dz * (r - 1) + dx, Decor.opposite(doorFacing), "brown");
            s.barrel(cx + dz * (r - 1), 1, cz + dx * (r - 1), null);
        }
    }

    // =========================================================================== OBOZOWISKO NA TRAKCIE (M2)
    static void obozNaTrakcie() throws Exception {
        Structure s = new Structure(26, 10, 26, 20001);
        scoutFire(s, 13, 13);
        Decor.cookingPot(s, 11, 1, 11);
        // trzy posłania pod daszkiem z płótna
        for (int x = 4; x <= 10; x++) {
            s.set(x, 3, 4, "minecraft:green_wool");
            s.set(x, 2, 3, "minecraft:green_wool");
            s.set(x, 1, 2, "minecraft:green_wool");
        }
        s.fill(4, 1, 4, 4, 2, 4, "minecraft:spruce_fence");
        s.fill(10, 1, 4, 10, 2, 4, "minecraft:spruce_fence");
        s.bed(5, 1, 5, "north", "green");
        s.bed(7, 1, 5, "north", "brown");
        s.bed(9, 1, 5, "north", "gray");
        // koniowiąz i siano
        for (int x = 17; x <= 22; x++) s.set(x, 1, 6, "minecraft:spruce_fence");
        s.set(18, 1, 7, "minecraft:hay_block");
        s.set(21, 1, 7, "minecraft:hay_block");
        // wóz z zapasami
        s.fill(17, 1, 18, 20, 1, 19, "minecraft:spruce_slab[type=top]");
        s.set(17, 0, 17, "minecraft:dirt");
        s.barrel(18, 2, 18, null);
        s.barrel(19, 2, 19, null);
        s.chest(20, 2, 18, "west", "zwiadowcy:chests/oboz_zlotu");
        // plac do sparingu - ubita ziemia
        s.fillMix(2, 0, 16, 9, 0, 23, new String[]{"minecraft:dirt_path", "minecraft:coarse_dirt"}, new double[]{0.6, 0.4});
        s.set(1, 1, 16, "minecraft:spruce_fence");
        s.set(1, 1, 23, "minecraft:spruce_fence");
        s.set(1, 2, 16, "minecraft:lantern[hanging=false]");
        s.set(1, 2, 23, "minecraft:lantern[hanging=false]");
        s.tree(23, 1, 23, "oak", 5);
        s.tree(2, 1, 11, "oak", 4);
        s.anchor(13, 1, 15, "ognisko");
        s.anchor(12, 1, 10, "gilan");
        s.anchor(15, 1, 11, "horace");
        s.anchor(7, 1, 7, "poslania");
        s.anchor(5, 1, 19, "sparing");
        s.anchor(19, 1, 9, "konie");
        save(s, "oboz_na_trakcie");
    }

    // =========================================================================== PRZEŁĘCZ WIATRÓW (M3)
    /** Wąwóz z zachodu na wschód: zawał w środku, urwisko z półkami (kotwiczka), grań z widokiem, zejście na zachodzie. */
    static void przeleczWiatrow() throws Exception {
        Structure s = new Structure(48, 18, 40, 20002);
        // dno wąwozu (ścieżka) i skalne ściany
        for (int x = 0; x < 48; x++) {
            for (int z = 0; z < 40; z++) {
                boolean path = z >= 16 && z <= 22;
                s.set(x, 0, z, path ? s.pick(new String[]{"minecraft:gravel", "minecraft:dirt_path", "minecraft:coarse_dirt"}, new double[]{0.3, 0.4, 0.3})
                        : s.pick(ROCK, ROCK_W));
                if (!path) {
                    // ściany wąwozu - wyższe od strony północnej (urwisko)
                    int h = z < 16 ? 9 + (int) (3 * Math.sin(x * 0.3)) : 5 + (int) (2 * Math.cos(x * 0.25));
                    if (z < 10 || z > 28) h -= 2;
                    for (int y = 1; y <= Math.max(1, h); y++) s.set(x, y, z, s.pick(ROCK, ROCK_W));
                }
            }
        }
        // zawał na ścieżce (x 20-27): głazy i żwir aż po ściany
        for (int x = 20; x <= 27; x++) for (int z = 16; z <= 22; z++) {
            int h = 4 + s.rnd.nextInt(3);
            for (int y = 1; y <= h; y++) s.set(x, y, z, s.pick(new String[]{"minecraft:cobblestone", "minecraft:stone", "minecraft:gravel"}, new double[]{0.4, 0.4, 0.2}));
        }
        // urwisko północne: dwie półki skalne (zaczepy dla kotwiczki) prowadzące nad zawał
        s.fill(28, 5, 13, 32, 5, 15, "minecraft:stone");
        s.clear(28, 6, 13, 32, 8, 15);
        s.fill(20, 10, 11, 27, 10, 15, "minecraft:stone");
        s.clear(20, 11, 11, 27, 13, 15);
        // grań z widokiem (szczyt) i zejście w dół na zachód (schody skalne)
        s.fill(14, 10, 9, 19, 10, 15, "minecraft:stone");
        s.clear(14, 11, 9, 19, 14, 15);
        for (int i = 0; i < 9; i++) {
            int x = 13 - i;
            s.set(x, 10 - i, 15, "minecraft:cobblestone_stairs[facing=east,half=bottom]");
            s.clear(x, 11 - i, 14, x, 13 - i, 16);
        }
        // obejście dla koni: kręta ścieżka przy południowej ścianie (niska skała, dla drużyny prowadzonej osobno)
        s.fill(0, 0, 26, 47, 0, 27, "minecraft:dirt_path");
        s.clear(0, 1, 26, 47, 4, 27);
        // kopczyki kamieni i pochodnie w szczelinach
        s.set(40, 1, 19, "minecraft:cobblestone_wall");
        s.set(40, 2, 19, "minecraft:torch");
        s.set(6, 1, 18, "minecraft:cobblestone_wall");
        s.set(6, 2, 18, "minecraft:torch");
        s.anchor(44, 1, 19, "podnoze");
        s.anchor(30, 1, 19, "zawal");
        s.anchor(30, 6, 14, "polka_1");
        s.anchor(23, 11, 13, "polka_2");
        s.anchor(17, 11, 12, "szczyt");
        s.anchor(3, 1, 19, "zejscie");
        s.anchor(24, 1, 26, "konie_obejscie");
        // kusznicy na skalnych półkach nad ścieżką (południowa ściana)
        s.fill(34, 6, 24, 36, 6, 25, "minecraft:stone");
        s.clear(34, 7, 24, 36, 9, 25);
        s.anchor(35, 7, 24, "kusznik_1");
        s.fill(12, 6, 24, 14, 6, 25, "minecraft:stone");
        s.clear(12, 7, 24, 14, 9, 25);
        s.anchor(13, 7, 24, "kusznik_2");
        s.anchor(38, 1, 21, "kusznik_3");
        save(s, "przelecz_wiatrow");
    }

    // =========================================================================== TRE-GWYN (M4)
    static void treGwyn() throws Exception {
        Structure s = new Structure(56, 16, 56, 20003);
        for (int x = 0; x < 56; x++) for (int z = 0; z < 56; z++) {
            if (Math.hypot(x - 28, z - 28) < 26) {
                s.set(x, 0, z, s.pick(new String[]{"minecraft:grass_block", "minecraft:coarse_dirt", "minecraft:dirt_path"}, new double[]{0.6, 0.25, 0.15}));
            }
        }
        // główna droga (południe → północ, do szybu)
        s.fill(26, 0, 4, 29, 0, 54, "minecraft:dirt_path");
        // studnia
        s.walls(26, 1, 26, 29, 1, 29, "minecraft:mossy_cobblestone");
        s.set(27, 0, 27, "minecraft:water");
        s.set(28, 0, 27, "minecraft:water");
        s.set(27, 0, 28, "minecraft:water");
        s.set(28, 0, 28, "minecraft:water");
        s.fill(26, 2, 26, 26, 3, 26, "minecraft:spruce_fence");
        s.fill(29, 2, 29, 29, 3, 29, "minecraft:spruce_fence");
        s.fill(26, 4, 26, 29, 4, 29, "minecraft:spruce_slab[type=bottom]");
        // chaty
        roundHut(s, 12, 14, 4, "east", false);
        roundHut(s, 12, 30, 4, "east", false);
        roundHut(s, 43, 14, 4, "west", false);
        roundHut(s, 43, 40, 4, "west", true);
        roundHut(s, 14, 44, 4, "north", false);
        // dom wójta (prostokątny, większy) z wyłamanymi drzwiami
        s.wallsMix(35, 1, 22, 47, 4, 31, CELT_STONE, CELT_STONE_W);
        s.fill(36, 0, 23, 46, 0, 30, "minecraft:spruce_planks");
        s.gableRoof(35, 22, 47, 31, 5, "spruce", "minecraft:spruce_planks", true);
        s.clear(35, 1, 26, 35, 2, 27);
        s.set(34, 1, 25, "minecraft:spruce_trapdoor[facing=east,half=bottom,open=true]");
        s.set(33, 1, 27, "minecraft:spruce_slab[type=bottom]");
        Decor.table(s, 40, 1, 26, "spruce", "minecraft:candle[candles=1,lit=false]");
        s.set(44, 1, 29, "minecraft:spruce_stairs[facing=north,half=bottom]");
        s.bed(44, 1, 24, "north", "red");
        s.chest(46, 1, 30, "west", "zwiadowcy:chests/spalona_osada");
        // karczma (długi dom) z wystygłym paleniskiem i niedojedzoną strawą
        s.wallsMix(4, 1, 20, 9, 4, 26, CELT_STONE, CELT_STONE_W);
        s.fill(5, 0, 21, 8, 0, 25, "minecraft:spruce_planks");
        s.gableRoof(4, 20, 9, 26, 5, "spruce", "minecraft:spruce_planks", false);
        s.clear(9, 1, 23, 9, 2, 23);
        s.set(5, 1, 21, "minecraft:campfire[lit=false,facing=south,signal_fire=false]");
        s.set(5, 2, 21, "minecraft:cauldron");
        Decor.table(s, 7, 1, 24, "spruce", "minecraft:candle[candles=2,lit=false]");
        // wózki z rudą przy szybie (północ) i porzucone narzędzia
        s.fill(25, 1, 2, 30, 1, 2, "minecraft:rail[shape=east_west]");
        s.set(25, 1, 3, "minecraft:barrel[facing=up,open=true]");
        s.set(30, 1, 3, "minecraft:barrel[facing=up,open=true]");
        s.set(27, 1, 6, "minecraft:raw_iron_block");
        s.fill(31, 1, 7, 34, 2, 7, "minecraft:mossy_cobblestone");
        s.set(32, 1, 8, "minecraft:cobblestone_wall");
        Decor.itemFrame(s, 32, 2, 8, "south", "minecraft:iron_pickaxe");
        s.set(33, 1, 8, "minecraft:cobblestone_wall");
        Decor.itemFrame(s, 33, 2, 8, "south", "minecraft:iron_helmet");
        // ślady Wargali prowadzące na północny zachód (do kopalni)
        for (int t = 0; t < 8; t++) s.set(24 - t * 2, 1, 20 - t, "zwiadowcy:slady");
        // celtycki kamień ostrzegawczy z napisem
        s.fill(20, 1, 34, 20, 3, 34, "minecraft:chiseled_stone_bricks");
        sign(s, 21, 2, 34, "east", "Uciekajcie", "w góry!", "", "");
        // owiec i kur nie ma - przewrócone płoty zagrody
        for (int x = 36; x <= 44; x += 2) s.set(x, 1, 48, "minecraft:spruce_fence");
        s.set(39, 1, 49, "minecraft:spruce_slab[type=bottom]");
        scoutFire(s, 22, 16);
        s.anchor(28, 1, 53, "brama");
        s.anchor(22, 1, 18, "ognisko");
        s.anchor(24, 1, 22, "gilan");
        s.anchor(27, 1, 4, "wskazowka_1");
        s.anchor(34, 1, 27, "wskazowka_2");
        s.anchor(18, 1, 23, "wskazowka_3");
        s.anchor(32, 1, 9, "wskazowka_4");
        s.anchor(6, 1, 22, "wskazowka_5");
        s.anchor(21, 1, 35, "wskazowka_6");
        save(s, "tre_gwyn");
    }

    static void sign(Structure s, int x, int y, int z, String facing, String l1, String l2, String l3, String l4) {
        Map<String, Object> nbt = new LinkedHashMap<>();
        nbt.put("id", "minecraft:sign");
        Map<String, Object> front = new LinkedHashMap<>();
        List<Object> msgs = new ArrayList<>();
        for (String l : new String[]{l1, l2, l3, l4}) msgs.add("{\"text\":\"" + l + "\"}");
        front.put("messages", msgs);
        front.put("color", "black");
        front.put("has_glowing_text", (byte) 0);
        nbt.put("front_text", front);
        nbt.put("is_waxed", (byte) 1);
        s.setNbt(x, y, z, "minecraft:oak_wall_sign[facing=" + facing + ",waterlogged=false]", nbt);
    }

    // =========================================================================== KOPALNIA (M5, S1)
    /**
     * Kopalnia pod Kruczą Skałą: budynek szybu na powierzchni, poziom 1 (y=-7) - chodniki z torami, poziom 2 (y=-15) -
     * komory, zalana komora, ukryty Dafydd, poziom 3 (y=-23) - stara sztolnia, boczny szyb z Rhysem i długie
     * wyjście schodami na zbocze po drugiej stronie.
     */
    static void kopalnia() throws Exception {
        int g = 27;
        Structure s = new Structure(40, g + 10, 40, 20004, g);
        // całe podziemie: lita skała, w niej kujemy chodniki
        s.fillMix(0, -g, 0, 39, -1, 39, new String[]{"minecraft:stone", "minecraft:andesite", "minecraft:tuff", "minecraft:deepslate"},
                new double[]{0.5, 0.2, 0.15, 0.15});
        // ---- powierzchnia: budynek szybu (5×5) z kołowrotem
        s.fill(3, 0, 3, 9, 0, 9, "minecraft:spruce_planks");
        s.walls(3, 1, 3, 9, 4, 9, "minecraft:spruce_planks");
        for (int[] c : new int[][]{{3, 3}, {9, 3}, {3, 9}, {9, 9}}) s.fill(c[0], 1, c[1], c[0], 4, c[1], "minecraft:spruce_log[axis=y]");
        s.pyramidRoof(3, 3, 9, 9, 5, "spruce");
        s.clear(6, 1, 9, 6, 2, 9);
        s.set(5, 3, 6, "minecraft:chain[axis=x]");
        s.set(6, 3, 6, "minecraft:chain[axis=x]");
        s.set(7, 3, 6, "minecraft:chain[axis=x]");
        // szyb w dół do poziomu 1 z drabiną
        s.clear(5, -7, 5, 7, 0, 7);
        for (int y = -7; y <= 0; y++) s.set(6, y, 5, "minecraft:ladder[facing=south,waterlogged=false]");
        s.fill(5, -8, 5, 7, -8, 7, "minecraft:stone");
        // ---- poziom 1 (podłoga y=-8, chodniki y=-7..-5): pętla chodników z torami i podporami
        tunnel(s, 6, 6, 32, 6, -7);
        tunnel(s, 32, 6, 32, 30, -7);
        tunnel(s, 6, 6, 6, 20, -7);
        s.fill(8, -7, 6, 30, -7, 6, "minecraft:rail[shape=east_west]");
        s.fill(32, -7, 8, 32, -7, 28, "minecraft:rail[shape=north_south]");
        for (int x = 10; x <= 30; x += 5) supports(s, x, 6, -7, true);
        for (int z = 10; z <= 28; z += 5) supports(s, 32, z, -7, false);
        s.barrel(30, -7, 5, null);
        s.set(31, -7, 5, "minecraft:barrel[facing=up,open=true]");
        s.set(20, -5, 6, "minecraft:lantern[hanging=true]");
        s.set(32, -5, 20, "minecraft:lantern[hanging=true]");
        // szyb z poziomu 1 na poziom 2 - drabina urwana w połowie (zejście albo kotwiczka)
        s.clear(31, -15, 29, 33, -8, 31);
        for (int y = -10; y <= -8; y++) s.set(32, y, 29, "minecraft:ladder[facing=south,waterlogged=false]");
        s.fill(31, -16, 29, 33, -16, 31, "minecraft:stone");
        // ---- poziom 2 (podłoga y=-16, chodniki y=-15..-13)
        tunnel(s, 32, 30, 10, 30, -15);
        tunnel(s, 10, 30, 10, 12, -15);
        for (int x = 14; x <= 30; x += 6) supports(s, x, 30, -15, true);
        // komora wydobywcza z wargalami
        s.clear(16, -15, 20, 26, -12, 28);
        s.fill(16, -16, 20, 26, -16, 28, "minecraft:stone");
        s.set(18, -15, 22, "minecraft:barrel[facing=up,open=true]");
        s.set(24, -15, 26, "minecraft:raw_iron_block");
        s.set(21, -12, 24, "minecraft:lantern[hanging=true]");
        tunnel(s, 21, 28, 21, 30, -15);
        // zalana komora: woda do pasa
        s.clear(4, -15, 26, 9, -13, 34);
        s.fill(4, -16, 26, 9, -16, 34, "minecraft:stone");
        s.fill(4, -15, 26, 9, -15, 34, "minecraft:water");
        tunnel(s, 9, 30, 10, 30, -15);
        // kryjówka Dafydda (boczna komora za starymi deskami)
        s.clear(12, -15, 8, 16, -13, 11);
        s.fill(12, -16, 8, 16, -16, 11, "minecraft:spruce_planks");
        s.bed(13, -15, 9, "east", "brown");
        s.barrel(16, -15, 8, null);
        s.set(15, -15, 11, "minecraft:candle[candles=1,lit=true]");
        tunnel(s, 10, 12, 10, 10, -15);
        tunnel(s, 10, 10, 12, 10, -15);
        // zawał na drodze do poziomu 3 (kotwica: początek ucieczki) i bezpieczna komora
        tunnel(s, 10, 12, 10, 4, -15);
        s.clear(8, -15, 2, 12, -13, 5);
        s.fill(8, -16, 2, 12, -16, 5, "minecraft:stone");
        // zejście schodami na poziom 3
        for (int i = 0; i < 8; i++) {
            s.set(12 + i + 1, -16 - i, 3, "minecraft:stone_brick_stairs[facing=west,half=bottom]");
            s.clear(12 + i + 1, -15 - i, 2, 12 + i + 1, -13 - i, 4);
        }
        // ---- poziom 3 (podłoga y=-24, chodniki y=-23..-21): stara sztolnia ze ślepym szybem bocznym
        s.clear(21, -23, 2, 26, -21, 6);
        s.fill(21, -24, 2, 26, -24, 6, "minecraft:stone");
        tunnel(s, 24, 6, 24, 22, -23);
        for (int z = 9; z <= 21; z += 4) supports(s, 24, z, -23, false);
        // boczny szyb (Rhys) za gruzem
        tunnel(s, 24, 14, 34, 14, -23);
        for (int y = -23; y <= -21; y++) for (int z = 13; z <= 15; z++) s.set(29, y, z, "minecraft:gravel");
        s.clear(33, -23, 12, 36, -21, 16);
        s.fill(33, -24, 12, 36, -24, 16, "minecraft:stone");
        s.set(36, -23, 12, "minecraft:lantern[hanging=false]");
        // wyjście: schody zakosami pod górę na powierzchnię (zbocze po wschodniej stronie)
        tunnel(s, 24, 22, 24, 26, -23);
        stairsZ(s, 24, -23, 27, 10, true);
        s.clear(23, -13, 37, 28, -11, 38);
        s.fill(23, -14, 37, 28, -14, 38, "minecraft:stone");
        stairsZ(s, 27, -13, 36, 10, false);
        s.clear(26, -3, 25, 31, -1, 26);
        s.fill(26, -4, 25, 31, -4, 26, "minecraft:stone");
        stairsZ(s, 30, -3, 27, 4, true);
        s.clear(29, 1, 31, 31, 3, 34);
        // światło: latarnie pod stropem co kilka bloków na wszystkich poziomach (bez nich w kopalni nic nie widać)
        for (int[] l : new int[][]{
                // poziom 1 (strop y=-4)
                {8, -5, 6}, {13, -5, 6}, {18, -5, 6}, {23, -5, 6}, {28, -5, 6}, {32, -5, 8}, {32, -5, 13}, {32, -5, 18},
                {32, -5, 23}, {32, -5, 28}, {6, -5, 10}, {6, -5, 15}, {6, -5, 20},
                // poziom 2 (strop y=-12; komora wydobywcza y=-11)
                {12, -13, 30}, {17, -13, 30}, {23, -13, 30}, {29, -13, 30}, {10, -13, 14}, {10, -13, 19}, {10, -13, 24},
                {17, -12, 21}, {25, -12, 27}, {6, -13, 30}, {13, -13, 10}, {10, -13, 7}, {10, -13, 3},
                // poziom 3 (strop y=-20) i wyjście
                {23, -21, 4}, {24, -21, 7}, {24, -21, 11}, {24, -21, 15}, {24, -21, 19}, {27, -21, 14}, {32, -21, 14},
                {24, -21, 24}, {25, -11, 37}}) {
            s.set(l[0], l[1], l[2], "minecraft:lantern[hanging=true,waterlogged=false]");
        }
        // kotwice
        s.anchor(6, 1, 11, "wejscie");
        s.anchor(32, -15, 30, "poziom_2");
        s.anchor(18, -15, 24, "wargal_1");
        s.anchor(24, -15, 22, "wargal_2");
        s.anchor(14, -15, 30, "wargal_3");
        s.anchor(22, -15, 27, "wargal_4");
        // zawał za plecami uciekającego (dalej od kryjówki Dafydda)
        s.anchor(10, -15, 18, "zawal");
        s.anchor(10, -15, 3, "bezpieczna_komora");
        s.npc(14, -15, 10, "dafydd", 90f);
        s.anchor(24, -23, 4, "poziom_3");
        s.npc(35, -23, 14, "rhys", 270f);
        s.anchor(28, -23, 14, "gruz");
        s.anchor(30, 1, 33, "wyjscie_zbocze");
        save(s, "kopalnia");
    }

    /** Chodnik 3×3 (od podłogi y-1, wolne y..y+2) między dwoma punktami w linii prostej. */
    static void tunnel(Structure s, int x0, int z0, int x1, int z1, int y) {
        int ax = Math.min(x0, x1), bx = Math.max(x0, x1), az = Math.min(z0, z1), bz = Math.max(z0, z1);
        boolean alongX = x0 != x1;
        s.clear(alongX ? ax : ax - 1, y, alongX ? az - 1 : az, alongX ? bx : bx + 1, y + 2, alongX ? bz + 1 : bz);
    }

    /**
     * Schody wzdłuż osi z: {@code n} stopni od (x, y, z0) w górę na południe ({@code south}) albo na północ,
     * z wolnym przejściem 3×3 nad każdym stopniem.
     */
    static void stairsZ(Structure s, int x, int y, int z0, int n, boolean south) {
        for (int i = 0; i < n; i++) {
            int z = south ? z0 + i : z0 - i;
            s.set(x, y + i, z, "minecraft:stone_brick_stairs[facing=" + (south ? "south" : "north") + ",half=bottom]");
            s.clear(x - 1, y + i + 1, z, x + 1, y + i + 3, z);
        }
    }

    /** Drewniana podpora chodnika (rama z bali). */
    static void supports(Structure s, int x, int z, int y, boolean alongX) {
        if (alongX) {
            s.set(x, y, z - 1, "minecraft:spruce_fence");
            s.set(x, y + 1, z - 1, "minecraft:spruce_fence");
            s.set(x, y, z + 1, "minecraft:spruce_fence");
            s.set(x, y + 1, z + 1, "minecraft:spruce_fence");
            s.fill(x, y + 2, z - 1, x, y + 2, z + 1, "minecraft:spruce_planks");
        } else {
            s.set(x - 1, y, z, "minecraft:spruce_fence");
            s.set(x - 1, y + 1, z, "minecraft:spruce_fence");
            s.set(x + 1, y, z, "minecraft:spruce_fence");
            s.set(x + 1, y + 1, z, "minecraft:spruce_fence");
            s.fill(x - 1, y + 2, z, x + 1, y + 2, z, "minecraft:spruce_planks");
        }
    }

    // =========================================================================== DOLINA MGIEŁ (M6, M7, S3)
    static void dolinaMgiel() throws Exception {
        Structure s = new Structure(40, 18, 40, 20005);
        // ruiny okrągłej wieży (środek 26,14, promień 4)
        for (int x = 21; x <= 31; x++) for (int z = 9; z <= 19; z++) {
            double d = Math.hypot(x - 26, z - 14);
            if (d <= 4.5) s.set(x, 0, z, "minecraft:mossy_cobblestone");
            if (d > 3.5 && d <= 4.5) {
                int h = 3 + (int) (6 * Math.abs(Math.sin(x * 1.3 + z)));
                for (int y = 1; y <= h; y++) s.set(x, y, z, s.pick(new String[]{"minecraft:mossy_stone_bricks", "minecraft:cracked_stone_bricks",
                        "minecraft:stone_bricks", "minecraft:mossy_cobblestone"}, new double[]{0.35, 0.25, 0.25, 0.15}));
            }
        }
        s.clear(26, 1, 18, 26, 2, 18);
        s.clear(26, 1, 19, 26, 2, 19);
        // murek dla kuszników
        for (int x = 18; x <= 34; x++) {
            s.set(x, 1, 24, s.pick(new String[]{"minecraft:mossy_cobblestone_wall", "minecraft:cobblestone_wall"}, new double[]{0.5, 0.5}));
            if (x % 4 == 0) s.set(x, 2, 24, "minecraft:mossy_cobblestone_wall");
        }
        s.fill(19, 1, 26, 21, 1, 26, "minecraft:mossy_cobblestone");
        // ognisko w zagłębieniu (obóz po uratowaniu Evanlyn), z dala od wieży
        scoutFire(s, 8, 30);
        s.set(5, 1, 33, "minecraft:spruce_log[axis=x]");
        // olchy i krzaki (las mgieł)
        for (int[] t : new int[][]{{3, 4}, {12, 6}, {36, 6}, {35, 34}, {16, 37}, {4, 20}}) s.tree(t[0], 1, t[1], "oak", 5 + s.rnd.nextInt(2));
        for (int[] b : new int[][]{{10, 14}, {14, 20}, {32, 28}, {6, 26}, {30, 36}}) s.bush(b[0], 1, b[1], "oak");
        // tarcze do strzelania z siodła (misja „Strzał Gilana”) na łące po zachodniej stronie
        for (int[] t : new int[][]{{2, 10}, {2, 14}, {2, 18}, {12, 2}, {16, 2}, {20, 2}, {37, 22}, {37, 26}}) {
            s.set(t[0], 1, t[1], "minecraft:hay_block");
            s.set(t[0], 2, t[1], "zwiadowcy:tarcza_lucznicza");
        }
        s.anchor(18, 1, 38, "skraj");
        s.anchor(17, 1, 33, "slad_1");
        s.anchor(14, 1, 27, "slad_2");
        s.anchor(17, 1, 22, "slad_3");
        s.anchor(22, 1, 21, "slad_4");
        s.anchor(26, 1, 20, "slad_5");
        s.anchor(26, 1, 21, "wieza");
        s.npc(26, 1, 13, "evanlyn", 180f);
        s.anchor(20, 1, 25, "wargal_1");
        s.anchor(31, 1, 25, "wargal_2");
        s.anchor(22, 1, 23, "wargal_3");
        s.anchor(28, 1, 23, "wargal_4");
        s.anchor(8, 1, 32, "ognisko");
        s.anchor(10, 1, 28, "gilan");
        s.anchor(5, 1, 6, "konkurs");
        save(s, "dolina_mgiel");
    }

    // =========================================================================== SZAŁAS PASTERSKI (S2)
    static void szalasPasterski() throws Exception {
        Structure s = new Structure(22, 12, 22, 20006);
        roundHut(s, 6, 6, 3, "south", false);
        // zagroda dla owiec
        for (int x = 11; x <= 20; x++) {
            s.set(x, 1, 10, "minecraft:spruce_fence");
            s.set(x, 1, 20, "minecraft:spruce_fence");
        }
        for (int z = 10; z <= 20; z++) {
            s.set(11, 1, z, z == 15 ? "minecraft:spruce_fence_gate[facing=east,open=false,in_wall=false,powered=false]" : "minecraft:spruce_fence");
            s.set(20, 1, z, "minecraft:spruce_fence");
        }
        s.set(18, 1, 18, "minecraft:hay_block");
        for (int[] p : new int[][]{{14, 13}, {16, 16}, {18, 12}, {15, 18}}) {
            Map<String, Object> nbt = new LinkedHashMap<>();
            nbt.put("id", "minecraft:sheep");
            nbt.put("PersistenceRequired", (byte) 1);
            s.entity(p[0] + 0.5, 1, p[1] + 0.5, nbt);
        }
        scoutFire(s, 6, 15);
        s.anchor(6, 1, 17, "ognisko");
        s.npc(8, 1, 12, "morwenna", 0f);
        s.anchor(15, 1, 15, "stado");
        s.anchor(2, 1, 20, "wilk_1");
        s.anchor(20, 1, 2, "wilk_2");
        s.anchor(10, 1, 2, "wilk_3");
        save(s, "szalas_pasterski");
    }

    // =========================================================================== WARGALSKI POSTERUNEK (M7)
    static void posterunekDobosza() throws Exception {
        Structure s = new Structure(44, 16, 44, 20007);
        // palisada 4 bloki, bramy: północna (otwarta, wejście od Doliny Mgieł) i południowa (zamknięta)
        for (int i = 4; i <= 39; i++) {
            for (int y = 1; y <= 4; y++) {
                s.set(i, y, 4, "minecraft:spruce_log[axis=y]");
                s.set(i, y, 39, "minecraft:spruce_log[axis=y]");
                s.set(4, y, i, "minecraft:spruce_log[axis=y]");
                s.set(39, y, i, "minecraft:spruce_log[axis=y]");
            }
            if (i % 2 == 0) {
                s.set(i, 5, 4, "minecraft:spruce_fence");
                s.set(i, 5, 39, "minecraft:spruce_fence");
                s.set(4, 5, i, "minecraft:spruce_fence");
                s.set(39, 5, i, "minecraft:spruce_fence");
            }
        }
        s.clear(20, 1, 4, 23, 3, 4);
        // brama południowa: zamknięte wrota z bali
        for (int x = 20; x <= 23; x++) for (int y = 1; y <= 3; y++) s.set(x, y, 39, "minecraft:stripped_spruce_log[axis=x]");
        s.set(25, 2, 38, "minecraft:lever[face=wall,facing=north,powered=false]");
        // wieża dobosza (pośrodku) z platformą na y=7 i bębnem
        for (int y = 1; y <= 6; y++) for (int[] c : new int[][]{{20, 20}, {23, 20}, {20, 23}, {23, 23}}) s.set(c[0], y, c[1], "minecraft:spruce_log[axis=y]");
        s.fill(19, 7, 19, 24, 7, 24, "minecraft:spruce_planks");
        for (int x = 19; x <= 24; x++) {
            s.set(x, 8, 19, "minecraft:spruce_fence");
            s.set(x, 8, 24, "minecraft:spruce_fence");
        }
        for (int z = 20; z <= 23; z++) {
            s.set(19, 8, z, "minecraft:spruce_fence");
            s.set(24, 8, z, "minecraft:spruce_fence");
        }
        s.fill(21, 1, 20, 21, 6, 20, "minecraft:spruce_log[axis=y]");
        for (int y = 1; y <= 7; y++) s.set(21, y, 19, "minecraft:ladder[facing=north,waterlogged=false]");
        s.clear(21, 8, 19, 21, 8, 19);
        s.set(23, 8, 23, "minecraft:note_block[instrument=basedrum,note=0,powered=false]");
        // namioty i ogniska Wargali, beczki, skóry
        StructureGen.tent(s, 7, 8, 6, "minecraft:brown_wool");
        StructureGen.tent(s, 30, 8, 6, "minecraft:black_wool");
        StructureGen.tent(s, 7, 30, 6, "minecraft:brown_wool");
        s.set(30, 1, 30, "minecraft:campfire[lit=true,facing=north,signal_fire=false]");
        s.set(14, 1, 22, "minecraft:campfire[lit=true,facing=north,signal_fire=false]");
        s.barrel(33, 1, 33, null);
        s.set(34, 1, 33, "minecraft:barrel[facing=up,open=false]");
        s.set(30, 1, 34, "minecraft:skeleton_skull[rotation=4]");
        s.anchor(21, 1, 1, "skraj");
        s.anchor(22, 8, 22, "wieza_dobosza");
        s.anchor(25, 1, 37, "dzwignia");
        s.anchor(21, 1, 42, "brama_poludniowa");
        for (int x = 20; x <= 23; x++) s.anchorBlock(x, 1, 39, "brama_" + (x - 19), "minecraft:stripped_spruce_log[axis=x]");
        s.anchor(33, 1, 22, "kusznik");
        // trasa A (wewnątrz, wokół wieży) i trasa B (wzdłuż palisady od zewnątrz)
        int[][] a = {{10, 14}, {16, 14}, {28, 14}, {33, 18}, {33, 28}, {16, 30}};
        for (int i = 0; i < a.length; i++) s.anchor(a[i][0], 1, a[i][1], "trasa_A_" + (i + 1));
        int[][] b = {{2, 2}, {41, 2}, {41, 41}, {2, 41}};
        for (int i = 0; i < b.length; i++) s.anchor(b[i][0], 1, b[i][1], "trasa_B_" + (i + 1));
        save(s, "posterunek_dobosza");
    }

    // =========================================================================== KURHAN NA WRZOSOWISKU (M8)
    static void wrzosowisko() throws Exception {
        Structure s = new Structure(34, 12, 34, 20008);
        // kurhan: kopiec z trawy z kamiennym wejściem
        for (int x = 4; x <= 16; x++) for (int z = 4; z <= 16; z++) {
            double d = Math.hypot(x - 10, z - 10);
            int h = (int) Math.round(5 - d * 0.75);
            for (int y = 1; y <= h; y++) s.set(x, y, z, y == h ? "minecraft:grass_block" : "minecraft:dirt");
        }
        s.fill(9, 1, 15, 11, 3, 17, "minecraft:mossy_cobblestone");
        s.clear(10, 1, 15, 10, 2, 17);
        // menhiry
        for (int[] m : new int[][]{{24, 6}, {28, 14}, {25, 25}, {12, 28}, {4, 24}}) {
            int h = 2 + s.rnd.nextInt(3);
            for (int y = 1; y <= h; y++) s.set(m[0], y, m[1], y == h ? "minecraft:mossy_cobblestone" : "minecraft:cobblestone");
        }
        s.anchor(10, 1, 20, "kurhan");
        s.anchor(30, 1, 30, "jezdziec");
        s.anchor(28, 1, 4, "wargal_1");
        s.anchor(31, 1, 18, "wargal_2");
        s.anchor(18, 1, 31, "wargal_3");
        save(s, "wrzosowisko");
    }

    private Tom2Structures() {}
}
