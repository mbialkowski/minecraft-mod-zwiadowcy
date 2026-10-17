package pl.zwiadowcy.assetgen;

/**
 * Tekstury mobów i płaszcza. Układy UV MUSZĄ odpowiadać modelom w pl.zwiadowcy.client.model
 * (KalkarModel, WargalModel, BoarModel, RangerCloakModel) oraz vanilla HorseModel.
 */
final class MobGen {

    static void all() throws Exception {
        kalkar();
        wargal("wargal", 0x5b4a3c, 0x7a6a58, 0x3a2e24, Extra.BELT);
        wargal("wargal_zwiadowca", 0x7a6e5e, 0x9a8e7a, 0x4a4238, Extra.NONE);
        wargal("wargal_oszczepnik", 0x4a3a2c, 0x6a5a48, 0x2a2018, Extra.HARNESS);
        wargal("wargal_herszt", 0x262019, 0x3a322a, 0x141010, Extra.WARPAINT);
        wargalEyes();
        boar();
        bear();
        horse();
        cloak();
    }

    enum Extra { NONE, BELT, HARNESS, WARPAINT }

    // ------------------------------------------------------------------ Niedźwiedź (PolarBearModel) 128×64
    static void bear() throws Exception {
        Px px = new Px(128, 64, 1001);
        int fur = Px.rgb(0x5a3a22), dark = Px.rgb(0x3a2414), muzzle = Px.rgb(0x8a6a4a);
        Px.Face furFace = (p, f, x, y, w, h) -> p.fur(x, y, w, h, fur, 0.18);
        px.box(0, 19, 14, 14, 11, furFace);                     // tułów
        px.box(39, 0, 12, 12, 10, (p, f, x, y, w, h) -> p.fur(x, y, w, h, Px.shade(fur, 1.08), 0.18)); // kark
        px.box(50, 22, 4, 10, 8, (p, f, x, y, w, h) -> {        // tylne łapy
            p.fur(x, y, w, h, fur, 0.18);
            if (!f.equals("top")) p.noisy(x, y + h - 2, w, 2, dark, 0.1);
        });
        px.box(50, 40, 4, 10, 6, (p, f, x, y, w, h) -> {        // przednie łapy
            p.fur(x, y, w, h, fur, 0.18);
            if (!f.equals("top")) p.noisy(x, y + h - 2, w, 2, dark, 0.1);
        });
        px.box(0, 0, 7, 7, 7, (p, f, x, y, w, h) -> {           // głowa
            p.fur(x, y, w, h, fur, 0.14);
            if (f.equals("front")) {
                p.set(x + 1, y + 2, Px.rgb(0x0a0606)); p.set(x + 5, y + 2, Px.rgb(0x0a0606));
                p.set(x + 1, y + 1, dark); p.set(x + 5, y + 1, dark);
            }
        });
        px.box(0, 44, 5, 3, 3, (p, f, x, y, w, h) -> {          // pysk
            p.noisy(x, y, w, h, muzzle, 0.08);
            if (f.equals("front")) { p.rect(x + 1, y, 3, 1, Px.rgb(0x1a1210)); p.set(x + 2, y + 2, Px.rgb(0x3a2a24)); }
        });
        px.box(26, 0, 2, 2, 1, (p, f, x, y, w, h) -> p.noisy(x, y, w, h, dark, 0.1)); // uszy
        px.save(TextureGen.file("entity/niedzwiedz"));
    }

    // ------------------------------------------------------------------ Kalkar 128×64
    static void kalkar() throws Exception {
        Px px = new Px(128, 64, 501);
        int fur = Px.rgb(0x2a221c), furDark = Px.rgb(0x1a1410), face = Px.rgb(0x3e3a36), claw = Px.rgb(0xc8b890);
        Px.Face furFace = (p, f, x, y, w, h) -> p.fur(x, y, w, h, f.equals("front") ? Px.shade(fur, 1.1) : fur, 0.18);
        px.box(0, 0, 16, 22, 10, furFace);                      // tułów
        px.box(56, 20, 18, 8, 12, (p, f, x, y, w, h) -> p.fur(x, y, w, h, furDark, 0.2)); // grzywa
        px.box(24, 32, 6, 24, 6, furFace);                      // ramię
        px.box(0, 40, 6, 16, 6, furFace);                       // noga
        px.box(48, 40, 6, 4, 6, (p, f, x, y, w, h) -> {         // pazury
            p.noisy(x, y, w, h, Px.shade(fur, 0.9), 0.1);
            if (f.equals("front") || f.equals("bottom")) for (int i = 0; i < w; i += 2) p.rect(x + i, y + (f.equals("front") ? 2 : 0), 1, 2, claw);
        });
        px.box(56, 0, 10, 9, 9, (p, f, x, y, w, h) -> {         // głowa
            p.fur(x, y, w, h, fur, 0.15);
            if (f.equals("front")) {
                p.noisy(x + 1, y + 2, w - 2, h - 2, face, 0.08);
                p.rect(x, y + 2, w, 1, furDark);               // wał brwiowy
                p.rect(x + 2, y + 3, 2, 1, Px.rgb(0x5a0a0a));
                p.rect(x + 6, y + 3, 2, 1, Px.rgb(0x5a0a0a));
            }
        });
        px.box(96, 0, 6, 4, 3, (p, f, x, y, w, h) -> {          // pysk
            p.noisy(x, y, w, h, face, 0.08);
            if (f.equals("front")) {
                p.set(x + 1, y, Px.rgb(0x0e0a08)); p.set(x + 4, y, Px.rgb(0x0e0a08));
                for (int i = 0; i < w; i += 2) p.set(x + i, y + h - 1, claw);
            }
        });
        px.save(TextureGen.file("entity/kalkar"));

        // oczy świecące (warstwa emisyjna) - przód głowy: u = 56+9 = 65, v = 9; oczy w wierszu 3
        Px eyes = new Px(128, 64, 502);
        int glow = Px.rgb(0xff3a1a), core = Px.rgb(0xffd060);
        eyes.set(65 + 2, 9 + 3, glow); eyes.set(65 + 3, 9 + 3, core);
        eyes.set(65 + 6, 9 + 3, core); eyes.set(65 + 7, 9 + 3, glow);
        eyes.save(TextureGen.file("entity/kalkar_oczy"));
    }

    // ------------------------------------------------------------------ Wargal 64×64 (odmiany)
    static void wargal(String name, int furRgb, int bellyRgb, int darkRgb, Extra extra) throws Exception {
        Px px = new Px(64, 64, name.hashCode());
        int fur = Px.rgb(furRgb), belly = Px.rgb(bellyRgb), dark = Px.rgb(darkRgb), teeth = Px.rgb(0xe8dcb0);
        int leather = Px.rgb(0x6a3a1e), paint = Px.rgb(0xa01818), iron = Px.rgb(0x8a8e94), bone = Px.rgb(0xd8ccb0);
        px.box(16, 16, 9, 12, 5, (p, f, x, y, w, h) -> {
            p.fur(x, y, w, h, fur, 0.16);
            if (f.equals("front")) {
                p.noisy(x + 2, y + 3, w - 4, h - 3, belly, 0.1);
                switch (extra) {
                    case BELT -> { p.rect(x, y + 1, w, 1, Px.rgb(0x6a2a1a)); p.rect(x, y + 8, w, 1, Px.rgb(0x3a2a1a)); }
                    case HARNESS -> {
                        p.line(x, y, x + w - 1, y + h - 1, leather);
                        p.line(x + w - 1, y, x, y + h - 1, leather);
                        for (int i = 1; i < w; i += 2) p.set(x + i, y + 1, bone);
                    }
                    case WARPAINT -> {
                        p.rect(x + 1, y + 2, 2, 6, paint);
                        p.rect(x + w - 3, y + 2, 2, 6, paint);
                        p.rect(x, y + 8, w, 2, Px.rgb(0x3a2a1a));
                        p.set(x + w / 2, y + 8, iron);
                    }
                    default -> {}
                }
            }
            if (f.equals("back")) {
                p.rect(x + 3, y, 3, h, dark);
                if (extra == Extra.HARNESS) { p.line(x, y, x + w - 1, y + h - 1, leather); p.line(x + w - 1, y, x, y + h - 1, leather); }
                if (extra == Extra.WARPAINT) p.rect(x + 2, y + 2, 5, 1, paint);
            }
            if (extra == Extra.WARPAINT && f.equals("top")) p.noisy(x, y, w, h, iron, 0.1);
        });
        px.box(0, 0, 8, 7, 7, (p, f, x, y, w, h) -> {
            p.fur(x, y, w, h, fur, 0.14);
            if (f.equals("front")) {
                p.rect(x + 1, y + 1, 2, 1, Px.rgb(0x1a0a06));
                p.rect(x + 5, y + 1, 2, 1, Px.rgb(0x1a0a06));
                if (extra == Extra.WARPAINT) { p.rect(x + 1, y + 3, 2, 1, paint); p.rect(x + 5, y + 3, 2, 1, paint); p.rect(x + 3, y, 2, 1, paint); }
            }
            if (extra == Extra.WARPAINT && (f.equals("right") || f.equals("left"))) p.rect(x + 1, y + 2, w - 2, 1, paint);
        });
        px.box(32, 0, 4, 3, 4, (p, f, x, y, w, h) -> {
            p.noisy(x, y, w, h, dark, 0.1);
            if (f.equals("front")) { p.set(x + 1, y, Px.rgb(0x0a0606)); p.set(x + 2, y, Px.rgb(0x0a0606)); p.rect(x, y + 2, w, 1, teeth); }
            if (f.equals("right") || f.equals("left")) p.rect(x, y + 2, w, 1, teeth);
        });
        px.box(48, 0, 2, 2, 1, (p, f, x, y, w, h) -> p.noisy(x, y, w, h, dark, 0.1));
        px.box(44, 16, 4, 13, 4, (p, f, x, y, w, h) -> {
            p.fur(x, y, w, h, fur, 0.16);
            if (!f.equals("top")) p.noisy(x, y + h - 2, w, 2, dark, 0.1);
            if (extra == Extra.WARPAINT && (f.equals("top") || f.equals("right") || f.equals("left") || f.equals("front") || f.equals("back"))) {
                p.noisy(x, y, w, f.equals("top") ? h : 3, iron, 0.12);
                if (!f.equals("top")) p.set(x + 1, y + 1, bone);
            }
            if (extra == Extra.HARNESS && !f.equals("top")) p.rect(x, y + 6, w, 1, leather);
        });
        px.box(0, 16, 4, 12, 4, (p, f, x, y, w, h) -> {
            p.fur(x, y, w, h, Px.shade(fur, 0.9), 0.16);
            if (!f.equals("top")) p.noisy(x, y + h - 3, w, 3, Px.rgb(0x2e2014), 0.08);
            if (extra == Extra.WARPAINT && f.equals("front")) p.rect(x + 1, y + 2, 2, 3, paint);
        });
        px.save(TextureGen.file("entity/" + name));
    }

    static void wargalEyes() throws Exception {
        Px eyes = new Px(64, 64, 602);
        int glow = Px.rgb(0xffb020);
        eyes.set(7 + 1, 7 + 1, glow); eyes.set(7 + 2, 7 + 1, Px.rgb(0xff5010));
        eyes.set(7 + 5, 7 + 1, Px.rgb(0xff5010)); eyes.set(7 + 6, 7 + 1, glow);
        eyes.save(TextureGen.file("entity/wargal_oczy"));
    }

    // ------------------------------------------------------------------ Dzik 64×64
    static void boar() throws Exception {
        Px px = new Px(64, 64, 701);
        int fur = Px.rgb(0x4a3426), dark = Px.rgb(0x2e2018), snout = Px.rgb(0x7a5a4a), tusk = Px.rgb(0xe8dcc0);
        px.box(0, 0, 10, 9, 18, (p, f, x, y, w, h) -> {
            p.fur(x, y, w, h, fur, 0.2);
            if (f.equals("bottom")) p.noisy(x, y, w, h, Px.rgb(0x5a4434), 0.1);
        });
        px.box(0, 44, 3, 2, 16, (p, f, x, y, w, h) -> p.fur(x, y, w, h, dark, 0.2));
        px.box(0, 28, 8, 8, 7, (p, f, x, y, w, h) -> {
            p.fur(x, y, w, h, fur, 0.16);
            if (f.equals("front")) {
                p.set(x + 1, y + 2, Px.rgb(0x0a0606)); p.set(x + 6, y + 2, Px.rgb(0x0a0606));
                p.set(x + 1, y + 1, dark); p.set(x + 6, y + 1, dark);
            }
        });
        px.box(30, 28, 5, 4, 3, (p, f, x, y, w, h) -> {
            p.noisy(x, y, w, h, snout, 0.08);
            if (f.equals("front")) {
                p.rect(x + 1, y + 1, 3, 2, Px.rgb(0x5a3a32));
                p.set(x + 1, y + 2, Px.rgb(0x1a0e0a)); p.set(x + 3, y + 2, Px.rgb(0x1a0e0a));
            }
        });
        px.box(46, 28, 1, 3, 1, (p, f, x, y, w, h) -> p.noisy(x, y, w, h, tusk, 0.04));
        px.box(52, 28, 2, 2, 1, (p, f, x, y, w, h) -> p.noisy(x, y, w, h, dark, 0.1));
        px.box(40, 44, 3, 7, 3, (p, f, x, y, w, h) -> {
            p.fur(x, y, w, h, Px.shade(fur, 0.9), 0.15);
            if (!f.equals("top")) p.rect(x, y + h - 1, w, 1, Px.rgb(0x1a1410));
            if (f.equals("bottom")) p.rect(x, y, w, h, Px.rgb(0x1a1410));
        });
        px.save(TextureGen.file("entity/dzik"));
    }

    // ------------------------------------------------------------------ Wyrwij (HorseModel) 64×64
    static void horse() throws Exception {
        Px px = new Px(64, 64, 801);
        int coat = Px.rgb(0x7a5a3a), mane = Px.rgb(0x2e221a), muzzle = Px.rgb(0x4a3a2e), hoof = Px.rgb(0x2a2420);
        int leather = Px.rgb(0x5a3a22), cloth = Px.rgb(0x4f5a3a), metal = Px.rgb(0xb0b0a8);
        Px.Face coatFace = (p, f, x, y, w, h) -> p.fur(x, y, w, h, coat, 0.12);
        px.box(0, 32, 10, 10, 22, coatFace);                    // tułów
        px.box(0, 35, 4, 12, 7, coatFace);                      // szyja
        px.box(0, 13, 6, 5, 7, (p, f, x, y, w, h) -> {          // głowa
            p.fur(x, y, w, h, coat, 0.1);
            if (f.equals("right") || f.equals("left")) p.set(x + 2, y + 1, Px.rgb(0x0e0a08));
        });
        px.box(0, 25, 4, 5, 5, (p, f, x, y, w, h) -> {          // pysk
            p.noisy(x, y, w, h, muzzle, 0.08);
            if (f.equals("front")) { p.set(x + 1, y + 1, Px.rgb(0x1a1410)); p.set(x + 2, y + 1, Px.rgb(0x1a1410)); }
        });
        px.box(19, 16, 2, 3, 1, (p, f, x, y, w, h) -> p.noisy(x, y, w, h, Px.shade(coat, 0.85), 0.08)); // uszy
        px.box(56, 36, 2, 16, 2, (p, f, x, y, w, h) -> p.fur(x, y, w, h, mane, 0.2));     // grzywa
        px.box(42, 36, 3, 14, 4, (p, f, x, y, w, h) -> p.fur(x, y, w, h, mane, 0.2));     // ogon
        px.box(48, 21, 4, 11, 4, (p, f, x, y, w, h) -> {        // nogi
            p.fur(x, y, w, h, coat, 0.12);
            if (!f.equals("top")) p.noisy(x, y + h - 4, w, 4, Px.shade(coat, 0.75), 0.1);
            if (!f.equals("top")) p.rect(x, y + h - 1, w, 1, hoof);
            if (f.equals("bottom")) p.rect(x, y, w, h, hoof);
        });
        // siodło i uprząż
        px.box(26, 0, 10, 9, 9, (p, f, x, y, w, h) -> {
            p.noisy(x, y, w, h, leather, 0.08);
            if (f.equals("right") || f.equals("left")) p.noisy(x, y + 3, w, h - 3, cloth, 0.1);
        });
        px.box(29, 5, 1, 2, 2, (p, f, x, y, w, h) -> p.rect(x, y, w, h, metal));
        px.box(32, 2, 0, 3, 16, (p, f, x, y, w, h) -> p.rect(x, y, w, h, leather));
        px.box(1, 1, 6, 5, 6, (p, f, x, y, w, h) -> {
            if (f.equals("right") || f.equals("left") || f.equals("front")) p.rect(x, y + 1, w, 1, leather);
        });
        px.box(19, 0, 4, 5, 2, (p, f, x, y, w, h) -> p.rect(x, y + 1, w, 1, leather));
        px.save(TextureGen.file("entity/wyrwij"));
    }

    // ------------------------------------------------------------------ Płaszcz (zbroja) 64×64
    static void cloak() throws Exception {
        Px px = new Px(64, 64, 901);
        int[] camo = SkinGen.CAMO;
        int hem = Px.rgb(0x2e3424), clasp = Px.rgb(0xb8b8a8);
        // kaptur (0,0) 8×8×8 - przód z wycięciem na twarz
        px.box(0, 0, 8, 8, 8, (p, f, x, y, w, h) -> {
            switch (f) {
                case "front" -> {
                    p.camo(x, y, w, 2, camo);
                    p.camo(x, y + 2, 1, h - 2, camo);
                    p.camo(x + w - 1, y + 2, 1, h - 2, camo);
                    p.rect(x, y + 1, w, 1, hem);
                }
                case "bottom" -> {}
                default -> p.camo(x, y, w, h, camo);
            }
        });
        // tułów (16,16) 8×12×4
        px.box(16, 16, 8, 12, 4, (p, f, x, y, w, h) -> {
            p.camo(x, y, w, h, camo);
            if (f.equals("front")) {
                p.rect(x + 3, y, 2, h, Px.shade(camo[1], 0.85));
                p.set(x + 3, y + 1, clasp);
                p.set(x + 4, y + 1, clasp);
            }
            if (!f.equals("top") && !f.equals("bottom")) p.rect(x, y + h - 1, w, 1, hem);
        });
        // poła (0,32) 10×22×1
        px.box(0, 32, 10, 22, 1, (p, f, x, y, w, h) -> {
            p.camo(x, y, w, h, camo);
            if (f.equals("front") || f.equals("back")) {
                p.rect(x, y + h - 1, w, 1, hem);
                for (int i = 0; i < w; i += 3) p.set(x + i, y + h - 2, hem);
            }
        });
        // rękawy (40,16) 4×12×4
        px.box(40, 16, 4, 12, 4, (p, f, x, y, w, h) -> {
            if (f.equals("bottom")) return;
            p.camo(x, y, w, h, camo);
            if (!f.equals("top")) p.rect(x, y + h - 1, w, 1, hem);
        });
        px.save(TextureGen.file("models/armor/plaszcz_zwiadowcy"));
    }

    private MobGen() {}
}
