package pl.zwiadowcy.assetgen;

import static pl.zwiadowcy.assetgen.TextureGen.c;
import static pl.zwiadowcy.assetgen.TextureGen.file;
import static pl.zwiadowcy.assetgen.TextureGen.pal;

/**
 * Tekstury tomu II („Płonący most”): przedmioty, belki mostu, odmiany Wargali i skórki nowych postaci.
 * Wywoływane z {@link TextureGen#main}.
 */
final class Tom2Gen {
    static final int K = 0x2a1a10;

    static void all() throws Exception {
        items();
        blocks();
        mobs();
        skins();
    }

    static void sprite(String name, String[] rows, java.util.Map<Character, int[]> palette, boolean shade) throws Exception {
        TextureGen.Items.sprite(name, rows, palette, shade);
    }

    // =====================================================================================
    static void items() throws Exception {
        String[] sword = {
                "..............kk",
                ".............kSk",
                "............kSsk",
                "...........kSsk.",
                "..........kSsk..",
                ".........kSsk...",
                "........kSsk....",
                ".......kSsk.....",
                "..kk..kSsk......",
                "..kgkkSsk.......",
                "...kgSsk........",
                "....kgg.........",
                "...khkgk........",
                "..khhk.kk.......",
                ".kppk...........",
                ".kkk............"};
        sprite("miecz_zwiadowcy", sword, pal('k', K, 'S', 0xe4e8ec, 's', c(0xa8b0b8, 0x9aa2aa), 'g', 0x5a5a62, 'h', 0x5a3a1e, 'p', 0x8a8e94), true);
        sprite("miecz_cwiczebny", sword, pal('k', 0x3a2410, 'S', 0xc8a070, 's', c(0x9a7444, 0x8a6a3a), 'g', 0x6a4a28, 'h', 0x4a3018, 'p', 0x6a4a28), true);
        sprite("kamyk", new String[]{
                "................", "................", "................", "................",
                "................", "......kkkk......", ".....kSsssk.....", "....kSssssdk....",
                "....kssssddk....", "....ksssdddk....", ".....kdddddk....", "......kkkkk.....",
                "................", "................", "................", "................"},
                pal('k', 0x3a3a3a, 'S', 0xb0b0ac, 's', c(0x8e8e8a, 0x86867f), 'd', 0x6a6a64), false);
        sprite("garnek_z_dymem", new String[]{
                "......g..g......", ".....g..g.......", "......g..g......", ".......cc.......",
                "......kcck......", ".....kkkkkk.....", "....kRrrrrRk....", "...kRrrrrrrRk...",
                "...kRrSSSrrRk...", "...kRrrrrrrRk...", "...kRrrrrrrRk...", "....kRrrrrRk....",
                ".....kkkkkk.....", "................", "................", "................"},
                pal('k', 0x4a2414, 'R', 0xa85a34, 'r', c(0xc06a3c, 0xb8643a), 'S', 0x3a2a20, 'c', 0xc8a87a, 'g', 0x9a9a9a), true);
        sprite("linka_z_kotwiczka", new String[]{
                "..........kkk...", ".........kIIIk..", "........kIk.kIk.", "........kIk..kk.",
                ".........kIk....", "..........kIk...", ".....rr...kIk...", "....rRRr...k....",
                "...rRrrRr..r....", "..rRr..rRrr.....", "..rR....Rr......", "..rRr..rRr......",
                "...rRrrRr.......", "....rRRr........", ".....rr.........", "................"},
                pal('k', 0x2a2a2e, 'I', 0x9aa0a8, 'r', 0x8a6a3a, 'R', 0xc8a46a), false);
        sprite("mapa_gilana", new String[]{
                "................", ".kkkkkkkkkkkkkk.", ".kPPPPPPPPPPPPk.", ".kPgggPPPwwwPPk.",
                ".kPggPPPwwPPPPk.", ".kPPPllllPPPPPk.", ".kPPlPPPPlPPmmk.", ".kPPlPPPPPlmmmk.",
                ".kPgPPPXPPlPmmk.", ".kPggPPPPPPlPPk.", ".kPPPPdddddPlPk.", ".kPPPPPPPPPPPPk.",
                ".kPPPPPPPPPPPPk.", ".kkkkkkkkkkkkkk.", "................", "................"},
                pal('k', 0x5a4a32, 'P', c(0xe6d8b4, 0xe0d0aa), 'g', 0x8a9a5a, 'w', 0x9a8aa0, 'l', 0x8a6a3a, 'X', 0xb02020,
                        'm', 0x8a8478, 'd', 0x3a2a1e), false);
        sprite("klucze_nadzorcy", new String[]{
                "................", ".....kkkk.......", "....k....k......", "...k......k.....",
                "...k......k.....", "....k....k......", ".....kIIk.......", "......Ik........",
                "......Ik........", "......IIk.......", "......Ik........", "......IIIk......",
                "......Ik........", "......IIk.......", "................", "................"},
                pal('k', 0x5a5a62, 'I', 0x9aa0a8), false);
        sprite("rozkazy_morgaratha", new String[]{
                "................", "...kkkkkkkkkk...", "..kPPPPPPPPPPk..", "..kPPllllllPPk..",
                "..kPPPPPPPPPPk..", "..kPPllllllPPk..", "..kPPPPPPPPPPk..", "..kPPlllllPPPk..",
                "..kPPPPPPPPPPk..", "..kPPPPPPBBPPk..", "..kPPPPPBBBBPk..", "..kPPPPPPBBPPk..",
                "...kkkkkkkkkk...", "................", "................", "................"},
                pal('k', 0x4a3a2a, 'P', c(0xd8c8a0, 0xd0c098), 'l', 0x4a3a2a, 'B', 0x1a1418), false);
        sprite("pierscien_evanlyn", new String[]{
                "................", "................", "................", "......kkkk......",
                ".....kRRRRk.....", ".....kRWRRk.....", "......kRRk......", ".....kGkkGk.....",
                "....kGk..kGk....", "....kG....Gk....", "....kG....Gk....", "....kGk..kGk....",
                ".....kGGGGk.....", "......kkkk......", "................", "................"},
                pal('k', 0x5a4212, 'G', 0xe0b040, 'R', 0xb01828, 'W', 0xff8090), false);
        sprite("pas_gilana", new String[]{
                "................", "................", "....s...s...s...", "....sS..sS..sS..",
                "....sS..sS..sS..", "....kk..kk..kk..", "kkkkkkkkkkkkkkkk", "LLLLLLLLLLLLLLLL",
                "lLlLlLGGLlLlLlLl", "LLLLLLGGLLLLLLLL", "kkkkkkkkkkkkkkkk", "................",
                "................", "................", "................", "................"},
                pal('k', 0x2a1a10, 'L', 0x6a4424, 'l', 0x5a3a1e, 'G', 0xc0a050, 's', 0xa8b0b8, 'S', 0xd8dce0), false);
        sprite("bicz", new String[]{
                "..........rrr...", "........rr...r..", ".......r......r.", "......r.......r.",
                ".....r.......r..", ".....r.....rr...", "......r..rr.....", ".......rr.......",
                "......kh........", ".....khk........", "....khk.........", "...khk..........",
                "..khk...........", ".kkk............", "................", "................"},
                pal('k', K, 'h', 0x5a3a1e, 'r', 0x3a2414), false);
        sprite("kopia", new String[]{
                "..............kS", ".............kSk", "............kSk.", "...........kWk..",
                "..........kWk...", ".........kWk....", "........kWk.....", ".......kWk......",
                "......kWk.......", ".....kWk........", "....kHk.........", "...kHHk.........",
                "..kHk...........", ".kWk............", "kWk.............", "kk.............."},
                pal('k', K, 'S', 0xd8dce0, 'W', c(0x6a4424, 0x5e3c20), 'H', 0x2a1a10), false);
        sprite("beben_wargali", new String[]{
                "................", "................", "....kkkkkkkk....", "...kHHHHHHHHk...",
                "..kHhHHHHHHhHk..", "..kkHHHHHHHHkk..", "..kWsWWWWsWWWk..", "..kWWsWWsWWWWk..",
                "..kWWWsWWsWWWk..", "..kWWsWWWWsWWk..", "..kWsWWWWWWsWk..", "..kkWWWWWWWWkk..",
                "...kkkkkkkkkk...", "................", "................", "................"},
                pal('k', 0x2a1a10, 'H', 0xc8b48a, 'h', 0xa8946a, 'W', c(0x6a4424, 0x5e3c20), 's', 0xb02020), true);
    }

    // =====================================================================================
    static void blocks() throws Exception {
        // płonąca belka: zwęglone drewno z żarzącymi się szczelinami
        Px burnSide = new Px(16, 16, 501);
        burnSide.noisy(0, 0, 16, 16, Px.rgb(0x2a1a12), 0.15);
        for (int i = 0; i < 26; i++) {
            int x = burnSide.rnd.nextInt(16), y = burnSide.rnd.nextInt(16);
            burnSide.set(x, y, Px.rgb(burnSide.rnd.nextBoolean() ? 0xff8a20 : 0xe04a10));
        }
        for (int x = 0; x < 16; x += 5) burnSide.line(x, 0, x, 15, Px.rgb(0x1a0e08));
        burnSide.save(file("block/plonaca_belka"));
        Px burnTop = new Px(16, 16, 502);
        burnTop.noisy(0, 0, 16, 16, Px.rgb(0x3a2214), 0.12);
        for (int r = 2; r < 8; r += 2) {
            for (int a = 0; a < 32; a++) {
                double ang = a * Math.PI / 16;
                burnTop.set((int) (7.5 + Math.cos(ang) * r), (int) (7.5 + Math.sin(ang) * r), Px.rgb(r % 4 == 0 ? 0xff7a20 : 0x5a2a12));
            }
        }
        burnTop.save(file("block/plonaca_belka_gora"));
        // zwęglona belka: czarna, popękana
        Px charSide = new Px(16, 16, 503);
        charSide.noisy(0, 0, 16, 16, Px.rgb(0x1e1a18), 0.18);
        for (int y = 1; y < 16; y += 3) for (int x = (y * 3) % 5; x < 16; x += 4) charSide.set(x, y, Px.rgb(0x0e0c0a));
        charSide.save(file("block/zweglona_belka"));
        Px charTop = new Px(16, 16, 504);
        charTop.noisy(0, 0, 16, 16, Px.rgb(0x2a2420), 0.15);
        for (int r = 2; r < 8; r += 2) {
            for (int a = 0; a < 32; a++) {
                double ang = a * Math.PI / 16;
                charTop.set((int) (7.5 + Math.cos(ang) * r), (int) (7.5 + Math.sin(ang) * r), Px.rgb(0x141210));
            }
        }
        charTop.save(file("block/zweglona_belka_gora"));
        // drewno oblane olejem: ciemne, lśniące smugi
        Px oilSide = new Px(16, 16, 505);
        oilSide.noisy(0, 0, 16, 16, Px.rgb(0x4a3018), 0.12);
        for (int x = 0; x < 16; x += 4) oilSide.line(x, 0, x, 15, Px.rgb(0x3a2410));
        for (int i = 0; i < 6; i++) {
            int x = oilSide.rnd.nextInt(14), y = oilSide.rnd.nextInt(10);
            oilSide.line(x, y, x + 1, y + 5, Px.rgb(0x8a6a2a));
        }
        oilSide.save(file("block/oblane_drewno"));
        Px oilTop = new Px(16, 16, 506);
        oilTop.noisy(0, 0, 16, 16, Px.rgb(0x5a3a1c), 0.1);
        oilTop.rect(5, 5, 6, 6, Px.rgb(0x7a5a24));
        oilTop.save(file("block/oblane_drewno_gora"));
    }

    // =====================================================================================
    static void mobs() throws Exception {
        MobGen.wargal("wargal_kusznik", 0x4e4032, 0x6e604e, 0x30261c, MobGen.Extra.QUIVER);
        MobGen.wargal("wargal_nadzorca", 0x2e2620, 0x46392e, 0x1a1410, MobGen.Extra.COLLAR);
        MobGen.wargal("wargal_dobosz", 0x5e4a38, 0x7e6a52, 0x3a2e22, MobGen.Extra.DRUMSTRAP);
    }

    // =====================================================================================
    static void skins() throws Exception {
        SkinGen.Spec[] specs = {
                new SkinGen.Spec("evanlyn").slim().skin(0xf0c8a8).hair(0xd8b070, SkinGen.Hair.LONG).eyes(0x3a6a8a)
                        .tunic(0x5a4a6a, 0xc8a040).pants(0x4a3a2a).boots(0x3a2a1a).belt(0x5a3a20),
                new SkinGen.Spec("erak").skin(0xd8a888).hair(0xa84a1a, SkinGen.Hair.SHAGGY).beard(0xa84a1a).eyes(0x3a5a7a)
                        .tunic(0x5a4a3a, -1).pants(0x3a3028).boots(0x2a1a10).fur().horns(),
                new SkinGen.Spec("dafydd").skin(0xc89070).hair(0xb0b0b0, SkinGen.Hair.BALD_FRINGE).beard(0xb0b0b0)
                        .tunic(0x5a5048, -1).apron().pants(0x3a3a34).boots(0x2a2420),
                new SkinGen.Spec("rhys").skin(0xd8a080).hair(0x1e1814, SkinGen.Hair.SHORT).tunic(0x4a4a42, -1)
                        .pants(0x3a3a34).boots(0x2a2420).scarf(0x6a3a22),
                new SkinGen.Spec("morwenna").slim().skin(0xe8b898).hair(0x1e1a18, SkinGen.Hair.BRAIDS).eyes(0x4a6a3a)
                        .tunic(0x5a6a3a, 0x8a4a2a).dress().scarf(0x8a4a2a),
                new SkinGen.Spec("gornik").skin(0xc8946e).hair(0x4a3020, SkinGen.Hair.SHORT).beard(0x4a3020)
                        .tunic(0x5a5048, -1).pants(0x3a3a34).clothHood(0x4a4238),
                new SkinGen.Spec("uciekinier").skin(0xd09a78).hair(0x6a4a2a, SkinGen.Hair.SHAGGY).tunic(0x6a6a5a, -1)
                        .pants(0x4a4a3a).scarf(0x6a5a3a),
                new SkinGen.Spec("zolnierz").skin(0xc8946e).hair(0x4a3020, SkinGen.Hair.SHORT).mustache(0x4a3020)
                        .mail().surcoat(0x2a4a8a).pants(0x4a3a2a).helmet(),
                new SkinGen.Spec("lucznik_krolewski").skin(0xd8a07c).hair(0x8a6a3a, SkinGen.Hair.SHORT)
                        .tunic(0x3a5a30, 0x2a4a8a).pants(0x4a3a2a).clothHood(0x3a5a30),
                new SkinGen.Spec("ranny_zolnierz").skin(0xc8946e).hair(0x6a4a2a, SkinGen.Hair.SHAGGY)
                        .mail().surcoat(0x2a4a8a).pants(0x4a3a2a).scarf(0xd8d0c0),
                new SkinGen.Spec("kowal_obozowy").skin(0xb8805e).hair(0x5a3a22, SkinGen.Hair.BALD_FRINGE).beard(0x5a3a22)
                        .tunic(0x5a4a3a, -1).sleeves(0xb8805e).apron().pants(0x3a3028).boots(0x1e1610),
                new SkinGen.Spec("wrogowie/jezdziec_morgaratha").skin(0xb8b0a8).hair(0x101010, SkinGen.Hair.SHORT).eyes(0x4a1010)
                        .mail().surcoat(0x141418).pants(0x1a1a1e).boots(0x0e0e10).helmet(),
                new SkinGen.Spec("skandyjski_zwiadowca").skin(0xe0b090).hair(0xd8c070, SkinGen.Hair.SHAGGY).beard(0xd8c070)
                        .tunic(0x4a4a3a, -1).pants(0x3a3028).fur().horns(),
                new SkinGen.Spec("wrogowie/skandyjski_wojownik").skin(0xe0b090).hair(0xc89a50, SkinGen.Hair.SHAGGY).beard(0xc89a50).eyes(0x3a5a7a)
                        .mail().tunic(0x5a4a3a, 0x8a2a1a).pants(0x3a3028).boots(0x2a1a10).belt(0x4a2a10).fur().horns(),
                new SkinGen.Spec("wrogowie/skandyjski_berserker").skin(0xd8a078).hair(0xa83a1a, SkinGen.Hair.SHAGGY).beard(0xa83a1a).eyes(0x6a2a1a)
                        .tunic(0x3a3028, -1).sleeves(0xd8a078).pants(0x2a2420).boots(0x1e1610).fur(),
                new SkinGen.Spec("wrogowie/morgarath").skin(0xe8e2dc).hair(0xd8d8d4, SkinGen.Hair.LONG).eyes(0x2a2a30)
                        .mail().surcoat(0x0e0e12).pants(0x121216).boots(0x08080a).belt(0x3a0a0a)
        };
        for (SkinGen.Spec s : specs) SkinGen.paint(s);
    }

    private Tom2Gen() {}
}
