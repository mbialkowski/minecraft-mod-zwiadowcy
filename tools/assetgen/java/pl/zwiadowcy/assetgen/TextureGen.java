package pl.zwiadowcy.assetgen;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Generator tekstur moda. Uruchamianie: gradlew generateTextures
 * Wszystkie tekstury powstają z kodu (siatki znaków + procedury), więc można je odtworzyć lub podmienić 1:1.
 */
public final class TextureGen {
    static File root;

    public static void main(String[] args) throws Exception {
        root = new File(args.length > 0 ? args[0] : "src/main/resources/assets/zwiadowcy/textures");
        Items.all();
        Blocks.all();
        Projectiles.all();
        Effects.all();
        SkinGen.all();
        MobGen.all();
        System.out.println("Tekstury wygenerowane w: " + root.getAbsolutePath());
    }

    static File file(String path) {
        return new File(root, path + ".png");
    }

    /** Pomocnik do budowy palet: pal('k', 0x2a1a10, 'B', 0x5a3418, ...) */
    static Map<Character, int[]> pal(Object... kv) {
        Map<Character, int[]> m = new HashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            char c = (Character) kv[i];
            Object v = kv[i + 1];
            if (v instanceof int[] arr) {
                int[] out = new int[arr.length];
                for (int j = 0; j < arr.length; j++) out[j] = Px.rgb(arr[j]);
                m.put(c, out);
            } else {
                m.put(c, new int[]{Px.rgb((Integer) v)});
            }
        }
        return m;
    }

    static int[] c(int... v) {
        return v;
    }

    // =====================================================================================
    static final class Items {
        static final int K = 0x2a1a10;

        static void sprite(String name, String[] rows, Map<Character, int[]> palette, boolean shade) throws Exception {
            Px px = new Px(16, 16, name.hashCode());
            px.grid(rows, palette);
            if (shade) px.autoShade(Px.rgb(palette.containsKey('k') ? palette.get('k')[0] & 0xFFFFFF : K));
            px.save(file("item/" + name));
        }

        static void all() throws Exception {
            sprite("kronika_araluenu", new String[]{
                    "................",
                    "..kkkkkkkkkkkk..",
                    ".kBBBBBBBBBBBBk.",
                    ".kBbbbbbbbbbbBkp",
                    ".kBbbbbGbbbbbBkp",
                    ".kBbbbGGGbbbbBkp",
                    ".kBbbGGGGGbbbBkp",
                    ".kBbbbGGGbbbbBkp",
                    ".kBbbbbGbbbbbBkp",
                    ".kBbbbbGbbbbbBkp",
                    ".kBbbbbbbbbbbBkp",
                    ".kBbbbbbbbbbbBkp",
                    ".kBBBBBBBBBBBBkp",
                    ".kkkkkkkkkkkkkkp",
                    "..pppppppppppppp",
                    "................"},
                    pal('k', K, 'B', 0x5a3418, 'b', c(0x7a4a26, 0x744624, 0x80502a), 'G', 0xe0b04a, 'p', 0xe6dcc0), true);

            sprite("dziennik_zwiadowcy", new String[]{
                    "................",
                    "...kkkkkkkkkk...",
                    "..kGGGGGGGGGGk..",
                    "..kGggggggggGkp.",
                    "..kGgLLLLLLgGkp.",
                    "..kGgLllllLgGkp.",
                    "..kGgLLLLLLgGkp.",
                    "..kGggggggggGkp.",
                    "..kSSSSSSSSSSSSk",
                    "..kGggggggggGkSk",
                    "..kGggggggggGkp.",
                    "..kGggggggggGkp.",
                    "..kGGGGGGGGGGkp.",
                    "..kkkkkkkkkkkkp.",
                    "...pppppppppppp.",
                    "................"},
                    pal('k', K, 'G', 0x3e4a2a, 'g', c(0x56643a, 0x5a683e, 0x525f37), 'L', 0xc9b27a, 'l', 0x7a6a4a,
                            'S', 0x6a4a2a, 'p', 0xe6dcc0), true);

            sprite("brazowy_lisc_debu", new String[]{
                    "................",
                    ".......kk.......",
                    "......kOOk......",
                    "...kk.kOOk.kk...",
                    "..kOOkkODkkOOk..",
                    "..kOOOOODOOOOk..",
                    "...kOOOODOOOk...",
                    ".kkkOOOODOOOkkk.",
                    "kOOOkOOODOOkOOOk",
                    "kOOOOOOODOOOOOOk",
                    ".kOOOOOODOOOOOk.",
                    "..kkkOOODOOkkk..",
                    "....kOOODOOk....",
                    ".....kOODOk.....",
                    "......kkDk......",
                    "........k......."},
                    pal('k', 0x4a2a12, 'O', c(0xc07a3a, 0xc8823e, 0xb87434), 'D', 0x8a5226), true);

            sprite("dokument_barona", new String[]{
                    "................",
                    "..kkkkkkkkkkk...",
                    "..kWWWWWWWWWk...",
                    "..kWlllllllWk...",
                    "..kWWWWWWWWWk...",
                    "..kWlllllWWWk...",
                    "..kWWWWWWWWWk...",
                    "..kWllllllWWk...",
                    "..kWWWWWWWWWk...",
                    "..kWlllllllWk...",
                    "..kWWWWWWWWWk...",
                    "..kWllllWWRRk...",
                    "..kWWWWWWRRRRk..",
                    "..kkkkkkkkRRk...",
                    "................",
                    "................"},
                    pal('k', 0x5a4a3a, 'W', c(0xeee2c4, 0xeadcbc), 'l', 0x7a6a52, 'R', 0xb02020), false);

            sprite("znak_morgaratha", new String[]{
                    "................",
                    ".....kkkkkk.....",
                    "...kkIIIIIIkk...",
                    "..kIIIIIIIIIIk..",
                    "..kIIIrrrrIIIk..",
                    ".kIIIrrIIrrIIIk.",
                    ".kIIrrIIIIrrIIk.",
                    ".kIIrIIrrIIrIIk.",
                    ".kIIrIIrrIIrIIk.",
                    ".kIIrrIIIIrrIIk.",
                    ".kIIIrrIIrrIIIk.",
                    "..kIIIrrrrIIIk..",
                    "..kIIIIIIIIIIk..",
                    "...kkIIIIIIkk...",
                    ".....kkkkkk.....",
                    "................"},
                    pal('k', 0x151515, 'I', c(0x4a4a50, 0x505056, 0x44444a), 'r', 0x9c1a1a), true);

            sprite("fragment_z_gorlanu", new String[]{
                    "................",
                    "................",
                    "......kkk.......",
                    "....kkSSSkk.....",
                    "...kSSSsSSSk....",
                    "..kSSssSSsSSk...",
                    "..kSSSssssSSSk..",
                    ".kSSsSSSSSsSSk..",
                    ".kSSSSsssSSSSSk.",
                    ".kSSSsSSSsSSSk..",
                    "..kSSSSSSSSMk...",
                    "...kkSSSMMkk....",
                    ".....kkkkk......",
                    "................",
                    "................",
                    "................"},
                    pal('k', 0x3a3a3a, 'S', c(0x8a8a8a, 0x929292, 0x828282), 's', 0x5a5a5a, 'M', 0x5a7a3a), true);

            sprite("list_krolewski", new String[]{
                    "................",
                    "...kk......kk...",
                    "..kWWkkkkkkWWk..",
                    "..kWWWWWWWWWWk..",
                    "..kwWWlllllWwk..",
                    "..kwWWWWWWWWwk..",
                    "..kwWlllllllwk..",
                    "..kwWWWWWWWWwk..",
                    "..kwYYYYYYYYwk..",
                    "..kwWWWRRWWWwk..",
                    "..kwWWRRRRWWwk..",
                    "..kwWWWRRWWWwk..",
                    "..kWWWWYYWWWWk..",
                    "..kWWkkYYkkWWk..",
                    "...kk..YY..kk...",
                    "................"},
                    pal('k', 0x5a4a32, 'W', 0xf0e6c8, 'w', 0xd8c8a0, 'l', 0x6a5a42, 'Y', 0xe0b04a, 'R', 0xb02020), false);

            sprite("zapieczetowany_list", new String[]{
                    "................",
                    "................",
                    "................",
                    ".kkkkkkkkkkkkkk.",
                    ".kWWWWWWWWWWWWk.",
                    ".kwWWWWWWWWWWwk.",
                    ".kWwWWWWWWWWwWk.",
                    ".kWWwWWWWWWwWWk.",
                    ".kWWWwWWWWwWWWk.",
                    ".kWWWWwRRwWWWWk.",
                    ".kWWWWWRRWWWWWk.",
                    ".kWWWWWWWWWWWWk.",
                    ".kWWWWWWWWWWWWk.",
                    ".kkkkkkkkkkkkkk.",
                    "................",
                    "................"},
                    pal('k', 0x5a4a32, 'W', 0xeee2c4, 'w', 0xc8b896, 'R', 0xa81c1c), false);

            sprite("kiel_dzika", new String[]{
                    "................",
                    "................",
                    "...........kk...",
                    "..........kIIk..",
                    "..........kIIk..",
                    "..........kIIk..",
                    ".........kIIIk..",
                    "........kIIIk...",
                    ".......kIIIk....",
                    "......kIIIk.....",
                    "....kkIIIk......",
                    "...kIIIIk.......",
                    "..kIIIkk........",
                    "..kBBk..........",
                    "...kk...........",
                    "................"},
                    pal('k', 0x5a4a3a, 'I', c(0xe8dcc0, 0xe0d2b4), 'B', 0x8a6a4a), true);

            sprite("kolczan", new String[]{
                    "................",
                    "...........W.W..",
                    "..........WFWF..",
                    "..........kFkW..",
                    ".........kSkS...",
                    "........kLLLLk..",
                    ".......kLLlLk...",
                    "......kLLlLLk...",
                    ".....kLLlLLk....",
                    "....kBBlBBk.....",
                    "...kLLlLLk......",
                    "..kLLlLLk.......",
                    "..kLLLLk........",
                    "..kBBBk.........",
                    "...kkk..........",
                    "................"},
                    pal('k', 0x2a1a10, 'L', c(0x8a5a32, 0x925f35, 0x82552f), 'l', 0x5a3a1e, 'B', 0x4a3020,
                            'S', 0x6a4a2a, 'W', 0xe8e8e8, 'F', 0xb83a2a), true);

            sprite("noz_do_rzucania", new String[]{
                    "................",
                    "................",
                    "................",
                    "............kk..",
                    "...........kSSk.",
                    "..........kSsk..",
                    ".........kSsk...",
                    "........kSsk....",
                    ".......kSsk.....",
                    "......kkkk......",
                    ".....kGGk.......",
                    "....kGGk........",
                    "...kGGk.........",
                    "...kkk..........",
                    "................",
                    "................"},
                    pal('k', 0x1e1e22, 'S', 0xd0d4d8, 's', 0x9098a0, 'G', 0x4a3424), false);

            sprite("noz_saksonski", new String[]{
                    "................",
                    "..............kk",
                    ".............kSk",
                    "............kSsk",
                    "...........kSsk.",
                    "..........kSsk..",
                    ".........kSsk...",
                    "........kSsk....",
                    ".......kSsk.....",
                    "...kk.kSsk......",
                    "...kGkSsk.......",
                    "....kGGk........",
                    "....kLLGk.......",
                    "...kLLk.kk......",
                    "..kLLk..........",
                    "..kkk..........."},
                    pal('k', 0x1e1e22, 'S', 0xd8dce0, 's', 0x8a929a, 'G', 0x8a8a90, 'L', 0x6a4026), false);

            sprite("plaszcz_zwiadowcy", new String[]{
                    "................",
                    "......kkkk......",
                    ".....kAAAAk.....",
                    "....kAkkkkAk....",
                    "....kAk..kAk....",
                    "...kAAkkkkAAk...",
                    "..kAABAAAABAAk..",
                    "..kABAAAAAAABk..",
                    ".kAAAAAAAAAAAAk.",
                    ".kABAAAAAAACAAk.",
                    ".kAAACAAAABAAAk.",
                    "kAAAAAAAAAAAAAAk",
                    "kABAAAACAAAABAAk",
                    "kAAAAAAAAAAAAAAk",
                    "kkkkkkkkkkkkkkkk",
                    "................"},
                    pal('k', 0x1e2216, 'A', c(0x4f5a3a, 0x56603e, 0x48523a, 0x5a5e48), 'B', 0x3a4630, 'C', 0x6a6a52), true);

            sprite("zestaw_tropiciela", new String[]{
                    "................",
                    "..........W.....",
                    ".........WW.....",
                    "........WW......",
                    "...kkkkWkkkk....",
                    "..kLLLLWLLLLk...",
                    "..kLDDDDDDDLk...",
                    "..kLDDDDDDDLk...",
                    "..kLLLLGLLLLk...",
                    "..kLLLLLLLLLk...",
                    "..kLlLLLLLlLk...",
                    "..kLLLLLLLLLk...",
                    "..kLLLLLLLLLk...",
                    "...kkkkkkkkk....",
                    "................",
                    "................"},
                    pal('k', 0x2a1a10, 'L', c(0x8a5a32, 0x925f35), 'D', 0x6a4224, 'G', 0xd4a640, 'l', 0x5a3a1e, 'W', 0xe8e8e8), true);

            sprite("gwizdek", new String[]{
                    "................",
                    "................",
                    "..ccc...........",
                    ".c...c..........",
                    ".c....c.........",
                    "..c....c........",
                    "...c....kkkkk...",
                    "....c..kWWWWWk..",
                    "......kWkWWWWWk.",
                    "......kWWWWWWWk.",
                    ".......kWWWWWk..",
                    "........kkkkk...",
                    "................",
                    "................",
                    "................",
                    "................"},
                    pal('k', 0x4a3a2a, 'W', c(0xc8a878, 0xd0b080), 'c', 0x6a4a2a), true);

            sprite("flasza_oleju", new String[]{
                    "................",
                    "......R..R......",
                    ".......RR.......",
                    ".......CC.......",
                    "......kGGk......",
                    ".....kGGGGk.....",
                    "....kGOOOOGk....",
                    "...kGOoOOOOGk...",
                    "...kGOoOOOOGk...",
                    "...kGOOOOOOGk...",
                    "...kGOOOOOOGk...",
                    "....kGOOOOGk....",
                    ".....kkkkkk.....",
                    "................",
                    "................",
                    "................"},
                    pal('k', 0x3a4a52, 'R', 0xd8d0b8, 'C', 0xa8804a, 'G', 0xc8e0e8, 'O', c(0xb88a2a, 0xb08428), 'o', 0xe8c060), false);

            sprite("racje_podrozne", new String[]{
                    "................",
                    "................",
                    "................",
                    "....kkkkkkkk....",
                    "...kCCCCCCCCk...",
                    "..kCCCSCCCCCCk..",
                    "..kCCCSCCCCCCk..",
                    "..kSSSSSSSSSSk..",
                    "..kCCCSCCCCCCk..",
                    "..kCCCSCCCCCCk..",
                    "..kCCCSCCCCCCk..",
                    "...kCCSCCCCCk...",
                    "....kkkkkkkk....",
                    "................",
                    "................",
                    "................"},
                    pal('k', 0x4a3a28, 'C', c(0xc8b48a, 0xc0ac82, 0xd0bc92), 'S', 0x6a4a2a), true);

            sprite("kawa_zwiadowcy", new String[]{
                    "................",
                    "........s.......",
                    ".......CC.s.....",
                    "......kGGk......",
                    "......kGGk......",
                    ".....kGGGGk.....",
                    "....kGBBBBGk....",
                    "...kGBBBBBBGk...",
                    "...kGBbBBBBGk...",
                    "...kGBbBBBBGk...",
                    "...kGBBBBBBGk...",
                    "...kGBBBBBBGk...",
                    "....kGBBBBGk....",
                    ".....kkkkkk.....",
                    "................",
                    "................"},
                    pal('k', 0x3a4a52, 'G', 0xd0e4ea, 'B', 0x4a2a14, 'b', 0x7a4a26, 'C', 0xa8804a, 's', 0xdddddd), false);

            sprite("palone_ziarna_kawy", new String[]{
                    "................",
                    "................",
                    "................",
                    ".......kkk......",
                    "......kBbBk.....",
                    "......kBlBk.....",
                    "...kkk.kBk......",
                    "..kBbBk.k.kkk...",
                    "..kBlBk..kBbBk..",
                    "...kBk...kBlBk..",
                    "....k.kkk.kBk...",
                    ".....kBbBk.k....",
                    ".....kBlBk......",
                    "......kBk.......",
                    ".......k........",
                    "................"},
                    pal('k', 0x1e120a, 'B', 0x4a2a18, 'b', 0x7a4a2a, 'l', 0x2a1810), false);

            sprite("oszczep", new String[]{
                    "..............kk",
                    ".............kSk",
                    "............kSsk",
                    "...........kSsk.",
                    "..........kRkk..",
                    ".........kWk....",
                    "........kWk.....",
                    ".......kWk......",
                    "......kWk.......",
                    ".....kWk........",
                    "....kWk.........",
                    "...kWk..........",
                    "..kWk...........",
                    ".kWk............",
                    "kWk.............",
                    "kk.............."},
                    pal('k', 0x2a1a10, 'S', 0xb8bcc0, 's', 0x7a8088, 'W', c(0x8a6a44, 0x7a5c3a), 'R', 0x6a3a1e), false);

            sprite("topor_herszta", new String[]{
                    "................",
                    "......kkkk......",
                    ".....kSSSSk.....",
                    "....kSssSSSk....",
                    "....kSkkSSSSk...",
                    "....kk.kkSSSk...",
                    ".......kWkSk....",
                    "......kWk.k.....",
                    ".....kWk........",
                    "....kWk.........",
                    "...kRk..........",
                    "..kWk...........",
                    ".kWk............",
                    ".kRk............",
                    "..k.............",
                    "................"},
                    pal('k', 0x1a1410, 'S', 0x9a9ea4, 's', 0x5a5e64, 'W', c(0x5a3a22, 0x4a3018), 'R', 0x8a1a1a), true);

            sprite("sakiewka_bandyty", new String[]{
                    "................",
                    "................",
                    "......k..k......",
                    ".......kk.......",
                    "......kSSk......",
                    ".....kLLLLk.....",
                    "....kLLLLLLk....",
                    "...kLLlLLLLLk...",
                    "...kLLLLLLlLk...",
                    "...kLLLLLLLLk...",
                    "...kLlLLLLLLkG..",
                    "....kLLLLLLkGYG.",
                    ".....kkkkkk.GG..",
                    "................",
                    "................",
                    "................"},
                    pal('k', 0x2a1a10, 'L', c(0x7a5230, 0x84593a), 'l', 0x5a3a1e, 'S', 0xa02020, 'G', 0xd4a640, 'Y', 0xf0d070), true);

            sprite("zapiski", new String[]{
                    "................",
                    "................",
                    "...kkkkkkkkkk...",
                    "..kWWWWWWWWWWk..",
                    "..kWllllllWWWk..",
                    "..kWWWWWWWWWWk..",
                    "..kWlllllllWWk..",
                    "..kWWWWWWWWWWk..",
                    "..kSSSSSSSSSSk..",
                    "..kWllllllWWWk..",
                    "..kWWWWWWWWWWk..",
                    "..kWlllllWWWWk..",
                    "..kwWWWWWWWWwk..",
                    "...kkkkkkkkkk...",
                    "................",
                    "................"},
                    pal('k', 0x5a4a32, 'W', c(0xe2d4ae, 0xdccca4), 'w', 0xc0ae84, 'l', 0x5a4a3a, 'S', 0x8a5a2a), false);

            bow();
            arrowIcons();
        }

        // ---------------------------------------------------------------- łuk zwiadowcy
        static void bow() throws Exception {
            for (int stage = -1; stage <= 2; stage++) {
                Px px = new Px(16, 16, 77 + stage);
                int wood = Px.rgb(0x6b4524), woodLight = Px.rgb(0x8a5c32), grip = Px.rgb(0x3a2a1c), string = Px.rgb(0xd8d8d0);
                double sx = 1.5, sy = 1.5, ex = 14.5, ey = 14.5, cx = 15.5, cy = 0.5;
                int pull = stage + 1;
                double px0 = 8 - pull * 1.2, py0 = 8 + pull * 1.2;
                // cięciwa
                if (stage < 0) {
                    px.line(2, 2, 14, 14, string);
                } else {
                    px.line(2, 2, (int) Math.round(px0), (int) Math.round(py0), string);
                    px.line((int) Math.round(px0), (int) Math.round(py0), 14, 14, string);
                }
                // łęczysko (krzywa Béziera) - refleksyjne końce
                for (int i = 0; i <= 80; i++) {
                    double t = i / 80.0;
                    double x = (1 - t) * (1 - t) * sx + 2 * (1 - t) * t * cx + t * t * ex;
                    double y = (1 - t) * (1 - t) * sy + 2 * (1 - t) * t * cy + t * t * ey;
                    int col = (t > 0.42 && t < 0.58) ? grip : (i % 7 < 3 ? woodLight : wood);
                    px.set((int) x, (int) y, col);
                    if (t > 0.15 && t < 0.85) px.set((int) x + 1, (int) y, col == grip ? grip : wood);
                }
                px.set(1, 1, woodLight);
                px.set(14, 14, woodLight);
                // strzała podczas napinania
                if (stage >= 0) {
                    int ax = (int) Math.round(px0), ay = (int) Math.round(py0);
                    int len = 6 + pull;
                    int shaft = Px.rgb(0x8a6a44), head = Px.rgb(0xb8bcc0), feather = Px.rgb(0xe8e8e8);
                    for (int i = 1; i < len; i++) px.set(ax + i, ay - i, shaft);
                    px.set(ax + len, ay - len, head);
                    px.set(ax + len - 1, ay - len, head);
                    px.set(ax + len, ay - len + 1, head);
                    px.set(ax - 1, ay, feather);
                    px.set(ax, ay + 1, feather);
                }
                px.save(file(stage < 0 ? "item/luk_zwiadowcy" : "item/luk_zwiadowcy_pulling_" + stage));
            }
        }

        // ---------------------------------------------------------------- ikony strzał
        static void arrowIcons() throws Exception {
            arrowIcon("strzala_plonaca", 0x8a6a44, 0xe0602a, 0xe8e8e8, "fire");
            arrowIcon("strzala_szeroki_grot", 0x8a6a44, 0xa8acb0, 0xe8e8e8, "broad");
            arrowIcon("strzala_gwizdzaca", 0x8a6a44, 0xa8acb0, 0x9ac0e0, "whistle");
            arrowIcon("strzala_z_lina", 0x8a6a44, 0xa8acb0, 0xe8e8e8, "rope");
            arrowIcon("strzala_zlotu", 0xd4a640, 0xf0d070, 0xffffff, "gold");
        }

        static void arrowIcon(String name, int shaftRgb, int headRgb, int featherRgb, String kind) throws Exception {
            Px px = new Px(16, 16, name.hashCode());
            int shaft = Px.rgb(shaftRgb), head = Px.rgb(headRgb), feather = Px.rgb(featherRgb), dark = Px.rgb(0x3a3a3a);
            for (int i = 0; i <= 9; i++) px.set(3 + i, 12 - i, shaft);
            int[][] headPx = kind.equals("broad")
                    ? new int[][]{{14, 1}, {13, 1}, {12, 1}, {11, 1}, {14, 2}, {14, 3}, {14, 4}, {13, 2}, {12, 2}, {13, 3}, {12, 3}}
                    : new int[][]{{14, 1}, {13, 1}, {12, 1}, {14, 2}, {14, 3}, {13, 2}};
            for (int[] p : headPx) px.set(p[0], p[1], head);
            px.set(12, 3, Px.shade(head, 0.7));
            int[][] fl = {{1, 12}, {2, 12}, {0, 13}, {1, 13}, {2, 14}, {3, 14}, {3, 15}, {2, 13}};
            for (int[] p : fl) px.set(p[0], p[1], feather);
            px.set(1, 14, Px.shade(feather, 0.8));
            switch (kind) {
                case "fire" -> {
                    int f1 = Px.rgb(0xffb020), f2 = Px.rgb(0xff6010), f3 = Px.rgb(0xfff060);
                    px.set(11, 4, f1); px.set(10, 4, f2); px.set(11, 5, f2); px.set(12, 5, f1);
                    px.set(10, 3, f3); px.set(9, 4, f1); px.set(12, 4, f3);
                }
                case "whistle" -> {
                    int bone = Px.rgb(0xe8dcc0);
                    px.set(10, 4, bone); px.set(11, 4, bone); px.set(10, 5, bone); px.set(11, 3, bone); px.set(9, 4, dark);
                }
                case "rope" -> {
                    int r1 = Px.rgb(0xc8a46a), r2 = Px.rgb(0x8a6a3a);
                    int[][] coil = {{6, 7}, {7, 7}, {8, 7}, {6, 8}, {8, 8}, {6, 9}, {7, 9}, {8, 9}, {5, 8}, {9, 8}};
                    for (int[] p : coil) px.set(p[0], p[1], (p[0] + p[1]) % 2 == 0 ? r1 : r2);
                }
                case "gold" -> {
                    px.set(13, 2, Px.rgb(0xffffff));
                    px.set(7, 8, Px.rgb(0xfff4c0));
                }
                default -> {}
            }
            px.save(file("item/" + name));
        }
    }

    // =====================================================================================
    static final class Blocks {
        static void all() throws Exception {
            // tarcza - pierścienie na słomie
            Px side = new Px(16, 16, 11);
            side.fur(0, 0, 16, 16, Px.rgb(0xc9a94a), 0.12);
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    double d = Math.hypot(x - 7.5, y - 7.5);
                    int c;
                    if (d < 1.6) c = 0xd8b020;
                    else if (d < 3.1) c = 0xc02a20;
                    else if (d < 4.6) c = 0xeeeeee;
                    else if (d < 6.1) c = 0xc02a20;
                    else if (d < 7.4) c = 0xeeeeee;
                    else continue;
                    side.set(x, y, Px.shade(Px.rgb(c), 0.95 + side.rnd.nextDouble() * 0.08));
                }
            }
            side.save(file("block/tarcza_lucznicza"));

            Px top = new Px(16, 16, 12);
            top.fur(0, 0, 16, 16, Px.rgb(0xc9a94a), 0.15);
            for (int x = 0; x < 16; x++) {
                top.set(x, 4, Px.rgb(0x6a4a2a));
                top.set(x, 11, Px.rgb(0x6a4a2a));
            }
            top.save(file("block/tarcza_lucznicza_gora"));

            // ślady - dwa odciski butów, półprzezroczyste
            Px tracks = new Px(16, 16, 13);
            int print = Px.argb(150, 0x3a2614);
            int printLight = Px.argb(110, 0x4a3420);
            int[][] boot = {{1, 0}, {2, 0}, {0, 1}, {1, 1}, {2, 1}, {3, 1}, {0, 2}, {1, 2}, {2, 2}, {3, 2},
                    {0, 3}, {1, 3}, {2, 3}, {3, 3}, {1, 4}, {2, 4}, {1, 5}, {2, 5}, {1, 6}, {2, 6}, {0, 7}, {1, 7}, {2, 7}, {3, 7}};
            for (int[] p : boot) {
                tracks.set(3 + p[0], 1 + p[1], (p[1] % 3 == 0) ? printLight : print);
                tracks.set(9 + p[0], 7 + p[1], (p[1] % 3 == 0) ? printLight : print);
            }
            tracks.save(file("block/slady"));

            // lina - skręcona, pionowo w środku
            Px rope = new Px(16, 16, 14);
            for (int y = 0; y < 16; y++) {
                boolean a = (y % 4) < 2;
                rope.set(7, y, Px.rgb(a ? 0xc8a46a : 0x8a6a3a));
                rope.set(8, y, Px.rgb(a ? 0x8a6a3a : 0xc8a46a));
            }
            rope.save(file("block/lina"));

            // papiery barona
            Px papers = new Px(16, 16, 15);
            papers.noisy(1, 2, 14, 12, Px.rgb(0xeee2c4), 0.03);
            papers.noisy(3, 1, 10, 3, Px.rgb(0xe2d4b0), 0.03);
            for (int y = 4; y < 13; y += 2) papers.line(3, y, 3 + 6 + (y % 3) * 2, y, Px.rgb(0x7a6a52));
            papers.rect(11, 10, 2, 2, Px.rgb(0xb02020));
            papers.save(file("block/papiery_barona"));
            Px pside = new Px(16, 16, 16);
            pside.noisy(0, 0, 16, 16, Px.rgb(0xd8ccae), 0.04);
            pside.save(file("block/papiery_barona_bok"));
        }
    }

    // =====================================================================================
    static final class Projectiles {
        static void all() throws Exception {
            arrow("strzala_plonaca", 0x8a6a44, 0xe0602a, 0xe8e8e8, true);
            arrow("strzala_szeroki_grot", 0x8a6a44, 0xa8acb0, 0xe8e8e8, false);
            arrow("strzala_gwizdzaca", 0x8a6a44, 0xa8acb0, 0x9ac0e0, false);
            arrow("strzala_z_lina", 0x8a6a44, 0xa8acb0, 0xc8a46a, false);
        }

        /** Układ jak w vanilla arrow.png (32×32): drzewce w (0..16, 0..5), tył w (0..5, 5..10). Grot po prawej. */
        static void arrow(String name, int shaftRgb, int headRgb, int featherRgb, boolean fire) throws Exception {
            Px px = new Px(32, 32, name.hashCode());
            int shaft = Px.rgb(shaftRgb), head = Px.rgb(headRgb), feather = Px.rgb(featherRgb);
            for (int x = 1; x < 14; x++) px.set(x, 2, shaft);
            px.set(13, 1, head); px.set(14, 1, head); px.set(13, 3, head); px.set(14, 3, head);
            px.set(14, 2, head); px.set(15, 2, head); px.set(12, 2, Px.shade(head, 0.8));
            for (int x = 0; x < 4; x++) {
                px.set(x, 1, feather);
                px.set(x, 3, feather);
            }
            px.set(0, 0, feather); px.set(1, 0, feather); px.set(0, 4, feather); px.set(1, 4, feather);
            if (fire) {
                px.set(11, 1, Px.rgb(0xffb020)); px.set(10, 3, Px.rgb(0xff6010)); px.set(11, 3, Px.rgb(0xfff060));
            }
            // tył (pióra w krzyż)
            px.set(2, 6, feather); px.set(2, 8, feather); px.set(1, 7, feather); px.set(3, 7, feather);
            px.set(2, 7, shaft); px.set(1, 6, Px.shade(feather, 0.8)); px.set(3, 8, Px.shade(feather, 0.8));
            px.save(file("entity/projectiles/" + name));
        }
    }

    // =====================================================================================
    static final class Effects {
        static void all() throws Exception {
            Px vig = new Px(18, 18, 21);
            vig.grid(new String[]{
                    "..................",
                    "......s...s.......",
                    ".......s...s......",
                    "......s...s.......",
                    "..................",
                    "...kkkkkkkkkk.....",
                    "...kWWWWWWWWkkk...",
                    "...kWBBBBBBWk.k...",
                    "...kWBBBBBBWk.k...",
                    "...kWBBBBBBWk.k...",
                    "...kWBBBBBBWkkk...",
                    "...kWWWWWWWWk.....",
                    "....kWWWWWWk......",
                    ".....kkkkkk.......",
                    "..................",
                    "..................",
                    "..................",
                    ".................."},
                    pal('k', 0x3a2a1a, 'W', 0xd8c8a8, 'B', 0x5a3418, 's', 0xe0e0e0));
            vig.save(file("mob_effect/czujnosc"));

            Px terror = new Px(18, 18, 22);
            terror.grid(new String[]{
                    "..................",
                    "..................",
                    "..................",
                    "..................",
                    ".....kkkkkkkk.....",
                    "...kkRRRRRRRRkk...",
                    "..kRRRRYYYYRRRRk..",
                    ".kRRRRYYkkYYRRRRk.",
                    ".kRRRRYkkkkYRRRRk.",
                    ".kRRRRYYkkYYRRRRk.",
                    "..kRRRRYYYYRRRRk..",
                    "...kkRRRRRRRRkk...",
                    ".....kkkkkkkk.....",
                    "..................",
                    "..................",
                    "..................",
                    "..................",
                    ".................."},
                    pal('k', 0x120808, 'R', 0x8a1010, 'Y', 0xe08a20));
            terror.save(file("mob_effect/groza"));

            Px oil = new Px(18, 18, 23);
            oil.grid(new String[]{
                    "..................",
                    "........k.........",
                    ".......kOk........",
                    ".......kOk........",
                    "......kOOOk.......",
                    "......kOOOk.......",
                    ".....kOOoOOk......",
                    ".....kOoOOOk......",
                    "....kOOoOOOOk.....",
                    "....kOOOOOOOk.....",
                    "....kOOOOOOOk.....",
                    "....kOOOOOOOk.....",
                    ".....kOOOOOk......",
                    "......kkkkk.......",
                    "..................",
                    "..................",
                    "..................",
                    ".................."},
                    pal('k', 0x3a2a08, 'O', 0xb88a2a, 'o', 0xf0d080));
            oil.save(file("mob_effect/oblany_olejem"));
        }
    }

    private TextureGen() {}
}
