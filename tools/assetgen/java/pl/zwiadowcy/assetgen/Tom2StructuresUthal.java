package pl.zwiadowcy.assetgen;

/**
 * Lokacje tomu II nad Uthal (akt III, część 2): obóz królewski, pole bitwy na Równinie Uthal i rozlewisko rzeki
 * z wilczym okrętem Skandian.
 */
final class Tom2StructuresUthal {

    static void all() throws Exception {
        obozKrolewski();
        rowninaUthal();
        brzegRzeki();
    }

    // =========================================================================== OBÓZ KRÓLEWSKI (M12-M13, M16)
    static void obozKrolewski() throws Exception {
        Structure s = new Structure(64, 14, 48, 20201);
        // ubite ścieżki: główna od bramy (zachód) do namiotu króla (wschód) i poprzeczna
        s.fillMix(0, 0, 22, 63, 0, 25, new String[]{"minecraft:dirt_path", "minecraft:coarse_dirt"}, new double[]{0.7, 0.3});
        s.fillMix(30, 0, 2, 33, 0, 45, new String[]{"minecraft:dirt_path", "minecraft:coarse_dirt"}, new double[]{0.7, 0.3});
        // ostrokół z zaostrzonych pali (tylko od strony równiny - południe) i brama od zachodu
        for (int x = 0; x < 64; x += 2) {
            s.fill(x, 1, 47, x, 2, 47, "minecraft:spruce_fence");
            s.set(x, 3, 47, "minecraft:spruce_fence");
        }
        for (int z : new int[]{19, 28}) {
            s.fill(0, 1, z, 0, 4, z, "minecraft:spruce_log[axis=y]");
            s.set(0, 5, z, "minecraft:green_banner[rotation=4]");
        }
        // namiot króla: duży, biało-niebieski, z proporcami
        int kx = 46, kz = 16;
        for (int x = kx; x <= kx + 12; x++) {
            for (int h = 0; h <= 5; h++) {
                String wool = (x % 2 == 0) ? "minecraft:white_wool" : "minecraft:blue_wool";
                if (h < 5) {
                    s.set(x, 1 + h, kz + h, wool);
                    s.set(x, 1 + h, kz + 14 - h, x == kx && h < 3 ? "minecraft:air" : wool);
                } else {
                    for (int z = kz + 5; z <= kz + 9; z++) s.set(x, 6, z, wool);
                }
            }
        }
        for (int z = kz; z <= kz + 14; z++) {
            int h = Math.min(z - kz, kz + 14 - z);
            for (int y = 1; y <= Math.min(5, h + 1); y++) s.set(kx + 12, y, z, "minecraft:white_wool");
        }
        s.clear(kx, 1, kz + 6, kx, 3, kz + 8);
        s.fill(kx + 1, 0, kz + 1, kx + 11, 0, kz + 13, "minecraft:spruce_planks");
        Decor.rug(s, kx + 3, kz + 4, kx + 9, kz + 10, 1, "blue", "white");
        s.set(kx + 7, 1, kz + 7, "minecraft:cartography_table");
        s.set(kx + 8, 1, kz + 7, "minecraft:lectern[facing=west,has_book=false,powered=false]");
        Decor.chair(s, kx + 10, 1, kz + 7, "dark_oak", "west");
        s.chest(kx + 11, 1, kz + 3, "west", null);
        s.set(kx + 6, 5, kz + 7, "minecraft:lantern[hanging=true]");
        for (int[] b : new int[][]{{kx - 1, kz + 4}, {kx - 1, kz + 10}}) {
            s.fill(b[0], 1, b[1], b[0], 4, b[1], "minecraft:dark_oak_fence");
            s.set(b[0], 5, b[1], "minecraft:blue_banner[rotation=12]");
        }
        // namioty żołnierzy w dwóch rzędach
        String[] colors = {"minecraft:green_wool", "minecraft:brown_wool", "minecraft:white_wool", "minecraft:lime_wool"};
        int[][] tents = {{4, 4}, {12, 4}, {20, 4}, {36, 4}, {4, 34}, {12, 34}, {20, 34}, {36, 34}};
        for (int i = 0; i < tents.length; i++) StructureGen.tent(s, tents[i][0], tents[i][1], 6, colors[i % colors.length]);
        // namiot Willa (mały, przy ognisku zwiadowców)
        StructureGen.tent(s, 40, 38, 5, "minecraft:green_wool");
        // kuźnia obozowa: kowadło, szlifierka, palenisko
        s.fill(20, 0, 14, 26, 0, 19, "minecraft:cobblestone");
        s.set(21, 1, 15, "minecraft:anvil[facing=east]");
        s.set(23, 1, 15, "minecraft:grindstone[face=floor,facing=north]");
        s.set(25, 1, 15, "minecraft:blast_furnace[facing=south,lit=true]");
        s.set(25, 2, 15, "minecraft:cobblestone_wall");
        s.set(21, 1, 18, "minecraft:smithing_table");
        s.barrel(24, 1, 18, null);
        for (int[] c : new int[][]{{20, 14}, {26, 14}, {20, 19}, {26, 19}}) s.fill(c[0], 1, c[1], c[0], 3, c[1], "minecraft:spruce_fence");
        s.fill(20, 4, 14, 26, 4, 19, "minecraft:spruce_slab[type=bottom,waterlogged=false]");
        // ognisko zwiadowców i ławy
        Tom2Structures.scoutFire(s, 38, 30);
        for (int x = 36; x <= 40; x++) s.set(x, 1, 33, "minecraft:spruce_log[axis=x]");
        // koniowiąz i siano
        for (int z = 6; z <= 14; z++) s.set(60, 1, z, "minecraft:spruce_fence");
        s.set(61, 1, 8, "minecraft:hay_block");
        s.set(61, 1, 12, "minecraft:hay_block");
        // tarcze łucznicze i stojaki na broń
        for (int x = 4; x <= 24; x += 5) {
            s.set(x, 1, 44, "zwiadowcy:tarcza_lucznicza");
            s.set(x, 2, 44, "zwiadowcy:tarcza_lucznicza");
        }
        // kotwice
        s.anchor(2, 1, 23, "brama");
        s.anchor(5, 1, 21, "straznik");
        s.anchor(43, 1, 23, "namiot_krola");
        s.anchor(kx + 9, 1, kz + 7, "krol");
        s.anchor(kx + 5, 1, kz + 9, "crowley");
        s.anchor(kx + 5, 1, kz + 5, "evanlyn_konie");
        s.anchor(36, 1, 27, "halt");
        s.anchor(28, 1, 10, "rodney");
        s.anchor(22, 1, 17, "kowal");
        s.anchor(38, 1, 28, "ognisko");
        s.anchor(35, 1, 30, "gilan");
        s.anchor(41, 1, 30, "evanlyn");
        s.anchor(39, 1, 34, "horace");
        s.anchor(42, 1, 40, "namiot_willa");
        s.anchor(58, 1, 10, "konie");
        for (int i = 0; i < 4; i++) s.anchor(8 + i * 8, 1, 30, "zolnierz_" + (i + 1));
        s.npc(9, 1, 12, "zolnierz", 90f);
        s.npc(17, 1, 12, "zolnierz", -90f);
        s.npc(10, 1, 41, "lucznik_krolewski", 180f);
        s.npc(18, 1, 41, "lucznik_krolewski", 180f);
        s.npc(23, 1, 17, "kowal_obozowy", 0f);
        s.npc(6, 1, 25, "zolnierz", 90f);
        StructureGen.save(s, "oboz_krolewski");
    }

    // =========================================================================== RÓWNINA UTHAL (M14-M15, S5)
    static void rowninaUthal() throws Exception {
        Structure s = new Structure(140, 12, 100, 20202);
        // północ (z mały) - linie Araluenu, południe (z duży) - szyki Morgaratha
        // linia Araluenu: pale, zielone sztandary
        for (int x = 10; x <= 130; x += 6) {
            s.fill(x, 1, 6, x, 2, 6, "minecraft:spruce_fence");
            if (x % 12 == 10) {
                s.fill(x, 3, 6, x, 4, 6, "minecraft:spruce_fence");
                s.set(x, 5, 6, "minecraft:green_banner[rotation=8]");
            }
        }
        for (int i = 0; i < 4; i++) s.anchor(20 + i * 30, 1, 10, "szyk_araluenu_" + (i + 1));
        // wzgórze kuszników (zadanie A): kopiec z palisadą
        int hx = 22, hz = 72;
        for (int x = hx - 10; x <= hx + 10; x++) for (int z = hz - 10; z <= hz + 10; z++) {
            double d = Math.hypot(x - hx, z - hz);
            int h = hillTop(x - hx, z - hz);
            for (int y = 1; y <= h; y++) s.set(x, y, z, y == h ? "minecraft:grass_block" : "minecraft:dirt");
        }
        for (int a = 0; a < 360; a += 20) {
            int x = hx + (int) Math.round(Math.cos(Math.toRadians(a)) * 6), z = hz + (int) Math.round(Math.sin(Math.toRadians(a)) * 6);
            if (a > 60 && a < 120) continue;
            s.set(x, 3, z, "minecraft:spruce_fence");
        }
        for (int i = 0; i < 5; i++) {
            double ang = Math.toRadians(200 + i * 35);
            int kx2 = hx + (int) Math.round(Math.cos(ang) * 3), kz2 = hz + (int) Math.round(Math.sin(ang) * 3);
            s.anchor(kx2, hillTop(kx2 - hx, kz2 - hz) + 1, kz2, "kusznik_" + (i + 1));
        }
        s.anchor(hx, hillTop(0, 0) + 1, hz, "wzgorze_kusznikow");
        s.anchor(hx + 2, 1, hz - 16, "podejscie_wzgorze");
        // otoczony oddział (zadanie B): wóz, porzucone tarcze, sztandar
        int ox = 70, oz = 54;
        s.fill(ox - 2, 1, oz - 1, ox + 2, 1, oz + 1, "minecraft:spruce_planks");
        s.set(ox - 3, 1, oz - 1, "minecraft:spruce_trapdoor[facing=west,half=bottom,open=true,powered=false,waterlogged=false]");
        s.set(ox + 3, 1, oz + 1, "minecraft:spruce_trapdoor[facing=east,half=bottom,open=true,powered=false,waterlogged=false]");
        s.fill(ox, 2, oz, ox, 4, oz, "minecraft:spruce_fence");
        s.set(ox, 5, oz, "minecraft:green_banner[rotation=0]");
        s.anchor(ox, 1, oz + 3, "oddzial");
        for (int i = 0; i < 6; i++) {
            double ang = Math.toRadians(i * 60);
            s.anchor(ox + (int) Math.round(Math.cos(ang) * 4), 1, oz + 3 + (int) Math.round(Math.sin(ang) * 4), "zolnierz_" + (i + 1));
        }
        for (int i = 0; i < 3; i++) {
            double ang = Math.toRadians(30 + i * 120);
            s.anchor(ox + (int) Math.round(Math.cos(ang) * 16), 1, oz + 3 + (int) Math.round(Math.sin(ang) * 16), "fala_" + (i + 1));
        }
        // szyki Morgaratha A-D z doboszami (zadanie C): platformy z bębnem i czarne sztandary
        String[] names = {"A", "B", "C", "D"};
        for (int i = 0; i < 4; i++) {
            int x = 40 + i * 22, z = 90;
            s.fill(x - 1, 1, z - 1, x + 1, 1, z + 1, "minecraft:spruce_planks");
            s.set(x + 1, 2, z + 1, "minecraft:note_block[instrument=basedrum,note=0,powered=false]");
            s.fill(x - 2, 1, z + 2, x - 2, 4, z + 2, "minecraft:dark_oak_fence");
            s.set(x - 2, 5, z + 2, "minecraft:black_banner[rotation=8]");
            s.anchor(x, 2, z, "szyki_" + names[i]);
            s.anchor(x, 1, z - 6, "wargale_" + names[i]);
        }
        // bród (zadanie D): płytka woda w poprzek pola po wschodniej stronie
        for (int z = 0; z < 100; z++) {
            int wx = 120 + (int) Math.round(Math.sin(z / 14.0) * 2);
            for (int x = wx - 3; x <= wx + 3; x++) {
                s.set(x, 0, z, Math.abs(x - wx) <= 2 ? "minecraft:water" : "minecraft:gravel");
            }
            if (z % 3 == 0) s.set(wx - 4, 1, z, "minecraft:grass");
        }
        s.anchor(112, 1, 40, "brod");
        s.anchor(110, 1, 36, "halt_brod");
        s.anchor(110, 1, 44, "gilan_brod");
        for (int i = 0; i < 3; i++) s.anchor(132, 1, 34 + i * 6, "skandianie_" + (i + 1));
        // arena pojedynku (M15): wolny krąg na północy pola, szyki żołnierzy wokół
        int ax = 70, az = 28;
        s.anchor(ax, 1, az, "arena_pojedynku");
        s.anchor(ax, 1, az - 14, "arena_will");
        s.anchor(ax, 1, az + 14, "arena_morgarath");
        s.anchor(ax - 4, 1, az - 12, "arena_horace");
        for (int i = 0; i < 8; i++) {
            double ang = Math.toRadians(i * 45);
            s.anchor(ax + (int) Math.round(Math.cos(ang) * 27), 1, az + (int) Math.round(Math.sin(ang) * 27), "krag_" + (i + 1));
        }
        // koniowiąz wroga (S5): za szykami, w krzakach
        for (int x = 8; x <= 16; x++) s.set(x, 1, 96, "minecraft:dark_oak_fence");
        s.set(12, 1, 97, "minecraft:hay_block");
        for (int[] b : new int[][]{{4, 90}, {6, 93}, {18, 92}, {20, 95}, {3, 96}}) s.bush(b[0], 1, b[1], "dark_oak");
        s.anchor(12, 1, 94, "rumak");
        s.anchor(4, 1, 84, "podejscie_koniowiaz");
        for (int i = 0; i < 4; i++) s.anchor(6 + i * 4, 1, 88 - (i % 2) * 3, "straz_koni_" + (i + 1));
        // przerywnik: punkt widokowy nad polem
        s.anchor(70, 1, 2, "widok");
        // wyrwy w ziemi i porzucona broń
        for (int[] c : new int[][]{{40, 40}, {95, 62}, {55, 76}, {100, 20}, {30, 50}}) {
            s.set(c[0], 0, c[1], "minecraft:coarse_dirt");
            s.set(c[0] + 1, 0, c[1], "minecraft:coarse_dirt");
            s.set(c[0], 0, c[1] + 1, "minecraft:coarse_dirt");
        }
        StructureGen.save(s, "rownina_uthal");
    }

    // =========================================================================== ROZLEWISKO RZEKI (M16)
    static void brzegRzeki() throws Exception {
        // groundOffset 3: y=3 to poziom gruntu, woda y=1..3 na dnie y=0
        Structure s = new Structure(48, 14, 40, 20203);
        for (int x = 0; x < 48; x++) for (int z = 0; z < 40; z++) {
            int shore = 18 + (int) Math.round(Math.sin(x / 7.0) * 2);
            if (z < shore) {
                for (int y = 0; y <= 2; y++) s.set(x, y, z, "minecraft:dirt");
                s.set(x, 3, z, z >= shore - 2 ? "minecraft:sand" : "minecraft:grass_block");
            } else {
                s.set(x, 0, z, s.pick(new String[]{"minecraft:sand", "minecraft:gravel", "minecraft:mud"}, new double[]{0.5, 0.3, 0.2}));
                for (int y = 1; y <= 3; y++) s.set(x, y, z, "minecraft:water");
                if (s.rnd.nextInt(9) == 0 && z < shore + 4) s.set(x, 1, z, "minecraft:seagrass");
            }
        }
        // szałas opatrunkowy na brzegu
        StructureGen.tent(s, 6, 4, 6, "minecraft:white_wool");
        for (int[] c : new int[][]{{14, 6}, {18, 6}, {22, 6}}) s.set(c[0], 4, c[1], "minecraft:white_carpet");
        s.barrel(26, 4, 4, null);
        s.set(27, 4, 4, "minecraft:cauldron");
        Tom2Structures.scoutFire(s, 32, 9);
        // wilczy okręt: długi kadłub z ciemnego dębu, maszt, żagiel w pasy, tarcze na burtach
        int x0 = 10, x1 = 37, zc = 28;
        for (int x = x0; x <= x1; x++) {
            int half = x - x0 < 3 ? 1 + (x - x0) : x1 - x < 3 ? 1 + (x1 - x) : 3;
            for (int z = zc - half; z <= zc + half; z++) {
                s.set(x, 2, z, "minecraft:dark_oak_planks");
                s.set(x, 3, z, Math.abs(z - zc) == half ? "minecraft:dark_oak_planks" : "minecraft:spruce_planks");
            }
            for (int z : new int[]{zc - half, zc + half}) {
                s.set(x, 4, z, "minecraft:dark_oak_fence");
                if (half == 3 && x % 3 == 0) s.set(x, 4, z, z < zc ? "minecraft:red_wool" : "minecraft:yellow_wool");
            }
            for (int z = zc - half + 1; z <= zc + half - 1; z++) s.set(x, 4, z, "minecraft:air");
        }
        // dziób i rufa ze smoczą głową
        s.fill(x0 - 1, 3, zc, x0 - 1, 6, zc, "minecraft:dark_oak_log[axis=y]");
        s.set(x0 - 2, 6, zc, "minecraft:dark_oak_stairs[facing=east,half=top,shape=straight,waterlogged=false]");
        s.fill(x1 + 1, 3, zc, x1 + 1, 6, zc, "minecraft:dark_oak_log[axis=y]");
        // maszt i żagiel
        s.fill(24, 4, zc, 24, 13, zc, "minecraft:stripped_dark_oak_log[axis=y]");
        for (int y = 7; y <= 12; y++) for (int z = zc - 4; z <= zc + 4; z++) {
            s.set(25, y, z, (z - zc + 4) % 3 == 0 ? "minecraft:red_wool" : "minecraft:white_wool");
        }
        s.fill(24, 12, zc - 4, 24, 12, zc + 4, "minecraft:spruce_log[axis=z]");
        // ławy wioślarzy i skrzynia na łupy
        for (int x = 13; x <= 34; x += 4) {
            s.set(x, 4, zc - 1, "minecraft:spruce_slab[type=bottom,waterlogged=false]");
            s.set(x, 4, zc + 1, "minecraft:spruce_slab[type=bottom,waterlogged=false]");
        }
        s.chest(35, 4, zc, "west", null);
        // trap z brzegu na pokład
        for (int z = 19; z <= zc - 3; z++) s.set(22, 3, z, "minecraft:spruce_planks");
        // kotwice
        s.anchor(10, 4, 10, "szalas");
        s.anchor(14, 4, 9, "ranni_1");
        s.anchor(18, 4, 9, "ranni_2");
        s.anchor(22, 4, 9, "ranni_3");
        s.anchor(30, 4, 12, "evanlyn");
        s.anchor(32, 4, 6, "ognisko");
        s.anchor(40, 4, 14, "skandianie");
        s.anchor(44, 4, 8, "skandianie_2");
        s.anchor(22, 4, 18, "okret");
        s.anchor(30, 4, zc, "okret_poklad");
        s.anchor(33, 4, zc, "okret_erak");
        s.anchor(16, 4, zc, "okret_evanlyn");
        s.anchor(18, 4, zc + 1, "okret_will");
        StructureGen.save(s, "brzeg_rzeki");
    }

    /** Wysokość szczytu kopca kuszników w odległości (dx, dz) od środka. */
    static int hillTop(int dx, int dz) {
        return (int) Math.round(4.5 - Math.hypot(dx, dz) * 0.45);
    }

    private Tom2StructuresUthal() {}
}
