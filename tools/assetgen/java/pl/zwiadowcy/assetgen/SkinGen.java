package pl.zwiadowcy.assetgen;

/**
 * Skórki NPC (format gracza 64×64). Każda postać opisana parametrami: cera, włosy, broda, strój, płaszcz…
 */
final class SkinGen {

    enum Hair { SHORT, SHAGGY, LONG, BALD_FRINGE, BRAIDS, RECEDING }

    static final class Spec {
        String id;
        boolean slim;
        int skin = 0xd8a07c, hair = 0x4a3020, eyes = 0x3a2a1a;
        Hair hairStyle = Hair.SHORT;
        boolean beard, mustache;
        int beardColor = -1;
        int tunic = 0x6a4a2a, trim = -1, sleeves = -1, pants = 0x4a3a2a, boots = 0x2e2014, belt = 0x2a1a10;
        boolean rangerCloak, hood, crown, chefHat, strawHat, helmet, apron, robe, dress, chainmail, fur;
        int surcoat = -1;
        int scarf = -1, clothHood = -1;

        Spec(String id) { this.id = id; }
        Spec scarf(int c) { scarf = c; return this; }
        Spec clothHood(int c) { clothHood = c; return this; }
        Spec slim() { slim = true; return this; }
        Spec skin(int c) { skin = c; return this; }
        Spec hair(int c, Hair s) { hair = c; hairStyle = s; return this; }
        Spec beard(int c) { beard = true; beardColor = c; return this; }
        Spec mustache(int c) { mustache = true; beardColor = c; return this; }
        Spec eyes(int c) { eyes = c; return this; }
        Spec tunic(int c, int trimC) { tunic = c; trim = trimC; return this; }
        Spec sleeves(int c) { sleeves = c; return this; }
        Spec pants(int c) { pants = c; return this; }
        Spec boots(int c) { boots = c; return this; }
        Spec belt(int c) { belt = c; return this; }
        Spec ranger(boolean withHood) { rangerCloak = true; hood = withHood; return this; }
        Spec crown() { crown = true; return this; }
        Spec chef() { chefHat = true; apron = true; return this; }
        Spec straw() { strawHat = true; return this; }
        Spec helmet() { helmet = true; return this; }
        Spec apron() { apron = true; return this; }
        Spec robe() { robe = true; return this; }
        Spec dress() { dress = true; return this; }
        Spec mail() { chainmail = true; return this; }
        Spec surcoat(int c) { surcoat = c; return this; }
        Spec fur() { fur = true; return this; }
    }

    static final int[] CAMO = {Px.rgb(0x4f5a3a), Px.rgb(0x3e4a30), Px.rgb(0x5f6a48), Px.rgb(0x5a5a4a), Px.rgb(0x4a4232)};

    static void all() throws Exception {
        Spec[] specs = {
                new Spec("halt").skin(0xc4967a).hair(0x4a4642, Hair.SHAGGY).beard(0x3e3a36).eyes(0x2a2018)
                        .tunic(0x5b5a3a, -1).pants(0x4b3a2a).ranger(true),
                new Spec("gilan").skin(0xd9a986).hair(0xb08a4a, Hair.SHORT).eyes(0x3a5a7a)
                        .tunic(0x5d6b3a, -1).pants(0x4b3a2a).ranger(true),
                new Spec("crowley").skin(0xd4a07a).hair(0xa8673a, Hair.RECEDING).eyes(0x3a5a3a)
                        .tunic(0x5a5a3a, -1).pants(0x4b3a2a).ranger(false),
                new Spec("zwiadowca").skin(0xc8946e).hair(0x5a3a22, Hair.SHORT).beard(0x5a3a22)
                        .tunic(0x55583a, -1).pants(0x4b3a2a).ranger(true),
                new Spec("baron_arald").skin(0xd8a07c).hair(0x3d2a1a, Hair.SHORT).beard(0x3d2a1a)
                        .tunic(0x7a1e1e, 0xd4a640).sleeves(0x6a1818).pants(0x3a2a20).belt(0x1a1410).fur(),
                new Spec("sir_rodney").skin(0xc98f6a).hair(0x8a8a8a, Hair.SHORT).mustache(0x8a8a8a)
                        .mail().surcoat(0x8a2020).pants(0x5a5a60),
                new Spec("mistrz_nigel").skin(0xe0b898).hair(0x9a9a9a, Hair.RECEDING).eyes(0x4a4a6a)
                        .tunic(0x2e3a5a, 0x8a8aa0).robe().belt(0x8a7a5a),
                new Spec("mistrz_chubb").skin(0xe4a888).hair(0x6a4a2a, Hair.BALD_FRINGE)
                        .tunic(0x6a4a2a, -1).chef().pants(0x4a3a2a),
                new Spec("lady_pauline").slim().skin(0xe8c0a0).hair(0xd8d0b8, Hair.LONG).eyes(0x3a5a7a)
                        .tunic(0x5a6a8a, 0xc8c8d8).dress(),
                new Spec("horace").skin(0xd8a07c).hair(0x8a6a3a, Hair.SHORT).eyes(0x3a5a7a)
                        .tunic(0x4a5a7a, 0x2a3448).pants(0x3a3a48).boots(0x3a2a1e),
                new Spec("alyss").slim().skin(0xf0c8a8).hair(0xe0c070, Hair.LONG).eyes(0x3a6a8a)
                        .tunic(0xd8d8e0, 0x4a6aa0).dress(),
                new Spec("jenny").slim().skin(0xf0b898).hair(0xd8b050, Hair.BRAIDS).eyes(0x4a6a3a)
                        .tunic(0x5a7a3a, -1).dress().apron(),
                new Spec("stary_bob").skin(0xc08a6a).hair(0xdcdcdc, Hair.SHAGGY).beard(0xdcdcdc)
                        .tunic(0x6a4a2a, -1).pants(0x5a4a3a).straw(),
                new Spec("krol_duncan").skin(0xd4a080).hair(0x5a4a3a, Hair.SHORT).beard(0x7a6a5a)
                        .tunic(0x24346a, 0xf0f0f0).sleeves(0x1e2a58).pants(0x1e2a48).belt(0xd4a640).crown().fur(),
                new Spec("alda").skin(0xd09070).hair(0x2a1a10, Hair.SHORT).tunic(0x3e4a62, 0x2a3448).pants(0x30303a),
                new Spec("bryn").skin(0xe0b090).hair(0xa84a1a, Hair.SHAGGY).tunic(0x3e4a62, 0x2a3448).pants(0x30303a),
                new Spec("jerome").skin(0xd8a888).hair(0xd8c070, Hair.SHORT).tunic(0x3e4a62, 0x2a3448).pants(0x30303a),
                new Spec("straznik").skin(0xc8946e).hair(0x4a3020, Hair.SHORT).mustache(0x4a3020)
                        .mail().surcoat(0x8a2020).pants(0x4a3a2a).helmet(),
                new Spec("wiesniak").skin(0xc8946e).hair(0x6a4a2a, Hair.SHAGGY).tunic(0x8a6a4a, -1)
                        .pants(0xb8a882).straw(),
                new Spec("karczmarz").skin(0xe0a888).hair(0x5a3a22, Hair.BALD_FRINGE).mustache(0x5a3a22)
                        .tunic(0x8a5a3a, -1).apron().pants(0x4a3a2a),
                new Spec("kowal").skin(0xb8805e).hair(0x2a1a10, Hair.SHORT).beard(0x2a1a10)
                        .tunic(0x5a4a3a, -1).sleeves(0xb8805e).apron().pants(0x3a3028).boots(0x1e1610),
                new Spec("chlopka").slim().skin(0xe8b898).hair(0x8a5a2a, Hair.BRAIDS).tunic(0x9a7a4a, -1).dress().apron(),
                // bandyci (losowo wybierana skórka)
                new Spec("bandyci/bandyta_0").skin(0xc8946e).hair(0x3a2a1a, Hair.SHAGGY).beard(0x3a2a1a)
                        .tunic(0x6a4a2a, -1).pants(0x4a3a2a).scarf(0xa02020),
                new Spec("bandyci/bandyta_1").skin(0xd8a888).hair(0x8a4a1a, Hair.SHORT).tunic(0x4a4a3a, -1)
                        .pants(0x3a3a30).scarf(0x8a1a1a).clothHood(0x3a3a32),
                new Spec("bandyci/bandyta_2").skin(0xb07a58).hair(0x1a1410, Hair.SHORT).mustache(0x1a1410)
                        .tunic(0x7a2a1a, -1).pants(0x4a3a2a),
                new Spec("bandyci/bandyta_3").skin(0xe0b090).hair(0xc0a060, Hair.SHAGGY).tunic(0x5a5a42, -1)
                        .pants(0x4a3a2a).scarf(0xb02a20),
                new Spec("bandyci/bandyta_4").skin(0xc08060).hair(0x4a3020, Hair.RECEDING).beard(0x5a4030)
                        .tunic(0x3a3a3a, -1).pants(0x2e2e2e).clothHood(0x2a2a26),
                new Spec("bandyci/bandyta_5").skin(0xd09a78).hair(0x5a3a22, Hair.LONG).tunic(0x6a5a3a, -1)
                        .pants(0x4a3a2a).scarf(0x9a1a1a).clothHood(0x4a3a2a),
                new Spec("bandyci/herszt_bandytow").skin(0xc08a68).hair(0x1e1410, Hair.SHAGGY).beard(0x1e1410)
                        .tunic(0x5a1414, 0xd4a640).pants(0x2a2420).boots(0x1a120c).scarf(0xc02020)
        };
        for (Spec s : specs) paint(s);
    }

    static void paint(Spec s) throws Exception {
        Px px = new Px(64, 64, s.id.hashCode());
        int skin = Px.rgb(s.skin), hair = Px.rgb(s.hair);
        int beard = Px.rgb(s.beardColor >= 0 ? s.beardColor : s.hair);
        int tunic = Px.rgb(s.tunic);
        int sleeve = Px.rgb(s.sleeves >= 0 ? s.sleeves : s.tunic);
        int pants = Px.rgb(s.dress || s.robe ? s.tunic : s.pants);
        int boots = Px.rgb(s.boots), belt = Px.rgb(s.belt);
        int mail = Px.rgb(0x8d9096);

        // ---------------- głowa (0,0)
        px.box(0, 0, 8, 8, 8, (p, f, x, y, w, h) -> {
            p.noisy(x, y, w, h, skin, 0.03);
            switch (f) {
                case "top" -> {
                    if (s.hairStyle == Hair.BALD_FRINGE || s.hairStyle == Hair.RECEDING) {
                        p.noisy(x, y, w, h, skin, 0.03);
                        p.noisy(x, y + h - 2, w, 2, hair, 0.08);
                    } else p.noisy(x, y, w, h, hair, 0.08);
                }
                case "back" -> {
                    int rows = s.hairStyle == Hair.BALD_FRINGE ? 5 : (s.hairStyle == Hair.LONG || s.hairStyle == Hair.BRAIDS ? 8 : 6);
                    p.noisy(x, y + (s.hairStyle == Hair.BALD_FRINGE ? 2 : 0), w, rows, hair, 0.08);
                }
                case "right", "left" -> {
                    int rows = switch (s.hairStyle) {
                        case LONG, BRAIDS -> 8;
                        case SHAGGY -> 5;
                        case BALD_FRINGE -> 4;
                        default -> 3;
                    };
                    int start = s.hairStyle == Hair.BALD_FRINGE ? 2 : 0;
                    p.noisy(x, y + start, w, rows - start, hair, 0.08);
                    if (s.hairStyle == Hair.LONG || s.hairStyle == Hair.BRAIDS) p.noisy(x, y, 2, h, hair, 0.08);
                    if (s.beard) p.noisy(x, y + 5, w, 3, beard, 0.08);
                }
                case "front" -> {
                    if (s.hairStyle == Hair.SHAGGY) {
                        p.noisy(x, y, w, 2, hair, 0.08);
                        p.set(x + 1, y + 2, hair); p.set(x + 5, y + 2, hair);
                    } else if (s.hairStyle == Hair.SHORT || s.hairStyle == Hair.LONG || s.hairStyle == Hair.BRAIDS) {
                        p.noisy(x, y, w, 1, hair, 0.08);
                        p.set(x, y + 1, hair); p.set(x + 7, y + 1, hair);
                    }
                    if (s.hairStyle == Hair.LONG || s.hairStyle == Hair.BRAIDS) {
                        p.noisy(x, y + 1, 1, 7, hair, 0.08);
                        p.noisy(x + 7, y + 1, 1, 7, hair, 0.08);
                    }
                    int brow = Px.shade(hair, 0.8);
                    p.set(x + 1, y + 3, brow); p.set(x + 2, y + 3, brow);
                    p.set(x + 5, y + 3, brow); p.set(x + 6, y + 3, brow);
                    p.set(x + 1, y + 4, Px.rgb(0xf0f0f0)); p.set(x + 2, y + 4, Px.rgb(s.eyes));
                    p.set(x + 5, y + 4, Px.rgb(s.eyes)); p.set(x + 6, y + 4, Px.rgb(0xf0f0f0));
                    p.set(x + 3, y + 5, Px.shade(skin, 0.88)); p.set(x + 4, y + 5, Px.shade(skin, 0.88));
                    p.set(x + 3, y + 6, Px.shade(skin, 0.7)); p.set(x + 4, y + 6, Px.shade(skin, 0.7));
                    if (s.beard) {
                        p.noisy(x, y + 5, 3, 3, beard, 0.08);
                        p.noisy(x + 5, y + 5, 3, 3, beard, 0.08);
                        p.noisy(x + 2, y + 7, 4, 1, beard, 0.08);
                        p.set(x + 3, y + 6, Px.shade(skin, 0.55)); p.set(x + 4, y + 6, Px.shade(skin, 0.55));
                        p.set(x + 2, y + 5, beard); p.set(x + 5, y + 5, beard);
                    } else if (s.mustache) {
                        p.set(x + 2, y + 5, beard); p.set(x + 3, y + 5, beard); p.set(x + 4, y + 5, beard); p.set(x + 5, y + 5, beard);
                    }
                }
                case "bottom" -> { if (s.beard) p.noisy(x, y, w, h, beard, 0.08); }
                default -> {}
            }
        });

        // ---------------- warstwa kapelusza (32,0)
        if (s.hood) {
            px.box(32, 0, 8, 8, 8, (p, f, x, y, w, h) -> {
                if (f.equals("front")) {
                    p.camo(x, y, w, 2, CAMO);
                    p.camo(x, y + 2, 1, h - 2, CAMO);
                    p.camo(x + w - 1, y + 2, 1, h - 2, CAMO);
                } else if (!f.equals("bottom")) {
                    p.camo(x, y, w, h, CAMO);
                }
            });
        } else if (s.crown) {
            int gold = Px.rgb(0xd4a640), gem = Px.rgb(0xb02020);
            px.box(32, 0, 8, 8, 8, (p, f, x, y, w, h) -> {
                if (f.equals("top") || f.equals("bottom")) return;
                p.rect(x, y, w, 2, gold);
                for (int i = 0; i < w; i += 2) p.set(x + i, y - 0, Px.shade(gold, 1.15));
                if (f.equals("front")) p.set(x + 3, y + 1, gem);
            });
        } else if (s.chefHat) {
            px.box(32, 0, 8, 8, 8, (p, f, x, y, w, h) -> {
                if (f.equals("bottom")) return;
                p.noisy(x, y, w, f.equals("top") ? h : 3, Px.rgb(0xf0f0f0), 0.03);
            });
        } else if (s.strawHat) {
            int straw = Px.rgb(0xd4b860);
            px.box(32, 0, 8, 8, 8, (p, f, x, y, w, h) -> {
                if (f.equals("bottom")) return;
                p.noisy(x, y, w, f.equals("top") ? h : 2, straw, 0.1);
                if (!f.equals("top")) p.rect(x, y + 2, w, 1, Px.rgb(0x8a6a3a));
            });
        } else if (s.helmet) {
            int iron = Px.rgb(0x9a9ea4);
            px.box(32, 0, 8, 8, 8, (p, f, x, y, w, h) -> {
                if (f.equals("bottom")) return;
                p.noisy(x, y, w, f.equals("top") ? h : 3, iron, 0.06);
                if (f.equals("front")) { p.rect(x + 3, y + 3, 2, 3, iron); }
            });
        }

        // ---------------- tułów (16,16)
        px.box(16, 16, 8, 12, 4, (p, f, x, y, w, h) -> {
            if (s.chainmail) p.noisy(x, y, w, h, mail, 0.12);
            else p.noisy(x, y, w, h, tunic, 0.05);
            if (s.surcoat >= 0 && (f.equals("front") || f.equals("back"))) {
                p.noisy(x + 1, y, w - 2, h, Px.rgb(s.surcoat), 0.05);
                if (f.equals("front")) { p.rect(x + 3, y + 2, 2, 6, Px.rgb(0xd8d8d8)); p.rect(x + 2, y + 4, 4, 2, Px.rgb(0xd8d8d8)); }
            }
            if (f.equals("front") || f.equals("back") || f.equals("left") || f.equals("right")) {
                if (!s.robe && !s.dress) p.rect(x, y + 8, w, 1, belt);
                if (s.robe || s.dress) p.rect(x, y + 7, w, 1, belt);
            }
            if (f.equals("front")) {
                if (s.trim >= 0) {
                    p.rect(x + 2, y, 4, 1, Px.rgb(s.trim));
                    if (s.fur || s.dress) p.rect(x, y, w, 1, Px.rgb(s.trim));
                }
                if (!s.robe && !s.dress) p.set(x + 3, y + 8, Px.rgb(0xc8a040));
                if (s.apron) p.noisy(x + 1, y + 3, w - 2, h - 3, Px.rgb(0xeeeeee), 0.03);
            }
            if (f.equals("top") && s.fur) p.noisy(x, y, w, h, Px.rgb(0xeeeeee), 0.05);
        });

        // ---------------- ręce
        int armW = s.slim ? 3 : 4;
        Px.Face arm = (p, f, x, y, w, h) -> {
            int c = s.chainmail ? mail : sleeve;
            p.noisy(x, y, w, h, c, 0.06);
            if (!f.equals("top")) p.noisy(x, y + h - 3, w, 3, skin, 0.03);
            if (f.equals("bottom")) p.noisy(x, y, w, h, skin, 0.03);
            if (s.fur && !f.equals("top") && !f.equals("bottom")) p.rect(x, y + h - 4, w, 1, Px.rgb(0xeeeeee));
        };
        px.box(40, 16, armW, 12, 4, arm);
        px.box(32, 48, armW, 12, 4, arm);

        // ---------------- nogi
        Px.Face leg = (p, f, x, y, w, h) -> {
            p.noisy(x, y, w, h, pants, 0.05);
            if (!s.dress && !s.robe) {
                if (!f.equals("top")) p.noisy(x, y + h - 3, w, 3, boots, 0.05);
                if (f.equals("bottom")) p.noisy(x, y, w, h, boots, 0.05);
            } else {
                if (!f.equals("top")) p.noisy(x, y + h - 1, w, 1, boots, 0.05);
                if (f.equals("bottom")) p.noisy(x, y, w, h, boots, 0.05);
            }
            if (s.apron && f.equals("front")) p.noisy(x, y, w, 5, Px.rgb(0xeeeeee), 0.03);
        };
        px.box(0, 16, 4, 12, 4, leg);
        px.box(16, 48, 4, 12, 4, leg);

        // ---------------- płaszcz zwiadowcy na warstwie kurtki i rękawów
        if (s.rangerCloak) {
            px.box(16, 32, 8, 12, 4, (p, f, x, y, w, h) -> {
                switch (f) {
                    case "front" -> {
                        p.camo(x, y, 2, h, CAMO);
                        p.camo(x + w - 2, y, 2, h, CAMO);
                        p.rect(x + 2, y, 4, 1, Px.rgb(0x3a3a2a));
                        p.set(x + 3, y, Px.rgb(0xc0c0b0));
                    }
                    case "back", "left", "right", "top" -> p.camo(x, y, w, h, CAMO);
                    default -> {}
                }
            });
            Px.Face cloakSleeve = (p, f, x, y, w, h) -> {
                if (f.equals("bottom")) return;
                p.camo(x, y, w, f.equals("top") ? h : 8, CAMO);
            };
            px.box(40, 32, armW, 12, 4, cloakSleeve);
            px.box(48, 48, armW, 12, 4, cloakSleeve);
            // poła płaszcza na nogawkach (od zewnątrz i z tyłu)
            Px.Face cloakLeg = (p, f, x, y, w, h) -> {
                if (f.equals("back") || f.equals("right") || f.equals("left")) p.camo(x, y, w, 7, CAMO);
            };
            px.box(0, 32, 4, 12, 4, cloakLeg);
            px.box(0, 48, 4, 12, 4, cloakLeg);
        }
        if (s.scarf >= 0) {
            // chusta zasłaniająca dół twarzy (warstwa kapelusza) - znak rozpoznawczy bandytów
            int sc = Px.rgb(s.scarf);
            px.box(32, 0, 8, 8, 8, (p, f, x, y, w, h) -> {
                switch (f) {
                    case "front" -> { p.noisy(x, y + 5, w, 3, sc, 0.08); p.set(x + 3, y + 5, Px.shade(sc, 0.7)); }
                    case "right", "left", "back" -> p.noisy(x, y + 5, w, 2, sc, 0.08);
                    default -> {}
                }
            });
        }
        if (s.clothHood >= 0) {
            int hc = Px.rgb(s.clothHood);
            px.box(32, 0, 8, 8, 8, (p, f, x, y, w, h) -> {
                switch (f) {
                    case "front" -> { p.noisy(x, y, w, 2, hc, 0.06); p.noisy(x, y + 2, 1, 3, hc, 0.06); p.noisy(x + w - 1, y + 2, 1, 3, hc, 0.06); }
                    case "top", "back" -> p.noisy(x, y, w, h, hc, 0.06);
                    case "right", "left" -> p.noisy(x, y, w, 6, hc, 0.06);
                    default -> {}
                }
            });
        }
        if (s.hairStyle == Hair.LONG || s.hairStyle == Hair.BRAIDS) {
            // włosy opadające na plecy (warstwa kurtki, tył)
            px.noisy(32, 36, 8, s.hairStyle == Hair.LONG ? 5 : 3, hair, 0.08);
        }
        px.save(TextureGen.file(s.id.contains("/") ? "entity/" + s.id : "entity/npc/" + s.id));
    }

    private SkinGen() {}
}
