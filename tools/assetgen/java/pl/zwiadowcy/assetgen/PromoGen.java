package pl.zwiadowcy.assetgen;

import javax.imageio.ImageIO;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;

/**
 * Grafiki do publikacji moda (pixel-art, powiększany bez wygładzania):
 * ikona projektu (brązowy liść dębu na tle strzały), baner z tytułem i logo na liście modów Forge.
 */
public final class PromoGen {
    private static final int INK = 0xFF1A1208;
    private static final int OUTLINE = 0xFF3A200C, BRONZE = 0xFFC07A3A, RIB = 0xFF7E4A20, VEIN = 0xFF9A5E2C;

    public static void main(String[] args) throws Exception {
        File pub = new File(args.length > 0 ? args[0] : "publikacja");
        File res = new File(args.length > 1 ? args[1] : "src/main/resources");
        pub.mkdirs();
        save(scale(icon(true), 8), new File(pub, "ikona.png"));
        BufferedImage banner = banner();
        save(scale(banner, 4), new File(pub, "baner.png"));
        save(scale(banner, 2), new File(res, "logo.png"));
        System.out.println("Grafiki publikacji: " + pub.getAbsolutePath() + " oraz " + new File(res, "logo.png").getAbsolutePath());
    }

    // ------------------------------------------------------------------ ikona 64x64

    static BufferedImage icon(boolean background) {
        Px p = new Px(64, 64, 1221);
        if (background) {
            for (int y = 0; y < 64; y++) for (int x = 0; x < 64; x++) {
                int c = Px.mix(0xFF22402E, 0xFF0D1C15, y / 63.0);
                p.set(x, y, Px.shade(c, 0.96 + p.rnd.nextDouble() * 0.08));
            }
            for (int[] s : new int[][]{{4, 5}, {9, 2}, {57, 4}, {60, 9}, {3, 12}}) p.set(s[0], s[1], 0xFFCFD8C0);
        }
        // odznaka: zielone pole w brązowym pierścieniu
        double cx = 32, cy = 32;
        for (int y = 0; y < 64; y++) for (int x = 0; x < 64; x++) {
            double dx = x + 0.5 - cx, dy = y + 0.5 - cy, d = Math.hypot(dx, dy);
            if (d <= 26) {
                p.set(x, y, Px.shade(Px.mix(0xFF35634A, 0xFF1A3426, d / 26), 0.97 + p.rnd.nextDouble() * 0.06));
            } else if (d <= 27) {
                p.set(x, y, INK);
            } else if (d <= 29.5) {
                double light = -(dx + dy) / d;            // światło z lewej góry
                p.set(x, y, Px.shade(0xFF9A6A34, 1.0 + light * 0.22));
            } else if (d <= 30.6) {
                p.set(x, y, INK);
            }
        }
        arrow(p, 49.5, 49.5, 13.5, 13.5);
        leaf(p, 32.5, 33, Math.toRadians(-52), 46);
        return p.img;
    }

    /** Strzała z grotem w (hx,hy) i lotkami przy (tx,ty). */
    static void arrow(Px p, double tx, double ty, double hx, double hy) {
        double len = Math.hypot(hx - tx, hy - ty);
        double ax = (hx - tx) / len, ay = (hy - ty) / len, nx = -ay, ny = ax;
        for (int y = 0; y < p.h; y++) for (int x = 0; x < p.w; x++) {
            double rx = x + 0.5 - tx, ry = y + 0.5 - ty;
            double along = rx * ax + ry * ay, across = rx * nx + ry * ny;
            // drzewce
            if (along >= 2 && along <= len - 6 && Math.abs(across) <= 1.0) {
                p.set(x, y, across < 0 ? 0xFF9A6634 : 0xFF6A4220);
            }
            // lotki
            if (along >= 0 && along <= 9 && Math.abs(across) > 0.8 && Math.abs(across) <= 3.6 - along * 0.18) {
                boolean edge = Math.abs(across) > 2.7 - along * 0.18 || along < 1;
                p.set(x, y, edge ? 0xFF2E4426 : ((int) along % 3 == 0 ? 0xFFD8D8C8 : 0xFF6E8F55));
            }
            // grot
            double g = along - (len - 8);
            if (g >= 0 && g <= 8 && Math.abs(across) <= 3.4 * (1 - g / 8.5)) {
                boolean edge = Math.abs(across) > 3.4 * (1 - g / 8.5) - 0.9;
                p.set(x, y, edge ? 0xFF2E343C : (across < 0 ? 0xFFDCE2E8 : 0xFF9AA2AC));
            }
        }
    }

    /** Liść dębu: ogonek w (bx,by) - po przesunięciu o pół długości w tył - oś pod kątem angle, płaty po obu stronach. */
    static void leaf(Px p, double cx, double cy, double angle, double len) {
        double ax = Math.cos(angle), ay = Math.sin(angle);
        double bx = cx - ax * len / 2, by = cy - ay * len / 2;
        boolean[][] in = new boolean[p.w][p.h];
        double[][] side = new double[p.w][p.h], along = new double[p.w][p.h];
        for (int y = 0; y < p.h; y++) for (int x = 0; x < p.w; x++) {
            double rx = x + 0.5 - bx, ry = y + 0.5 - by;
            double u = (rx * ax + ry * ay) / len, v = (-rx * ay + ry * ax) / len;
            along[x][y] = u;
            side[x][y] = v;
            if (u >= 0.1 && u <= 1.0) {
                double t = (u - 0.1) / 0.9;
                double env = 0.30 * Math.pow(Math.sin(Math.PI * t), 0.6) * (0.72 + 0.38 * t);
                double phase = v > 0 ? 0.07 : 0.0;
                double lobe = 0.48 + 0.52 * Math.pow(Math.abs(Math.cos(Math.PI * (t + phase) * 3.5)), 0.5);
                in[x][y] = Math.abs(v) <= env * lobe;
            }
        }
        // ogonek
        for (int i = 0; i <= 14; i++) {
            double u = -0.1 + i * 0.016;
            int x = (int) Math.floor(bx + ax * u * len), y = (int) Math.floor(by + ay * u * len);
            p.set(x, y, RIB);
            p.set(x + 1, y, OUTLINE);
        }
        for (int y = 0; y < p.h; y++) for (int x = 0; x < p.w; x++) {
            if (!in[x][y]) continue;
            boolean edge = !inside(in, x - 1, y) || !inside(in, x + 1, y) || !inside(in, x, y - 1) || !inside(in, x, y + 1);
            int c;
            if (edge) {
                c = OUTLINE;
            } else {
                c = Px.shade(BRONZE, (side[x][y] > 0 ? 1.08 : 0.9) + along[x][y] * 0.08 + (p.rnd.nextDouble() - 0.5) * 0.06);
                if (!inside(in, x - 1, y - 1) || !inside(in, x, y - 2)) c = Px.shade(c, 1.18);   // połysk krawędzi
                if (Math.abs(side[x][y]) * len < 0.75 && along[x][y] < 0.97) c = RIB;           // nerw główny
            }
            p.set(x, y, c);
        }
        // nerwy boczne do szczytów płatów
        for (int k = 1; k <= 3; k++) {
            for (int s : new int[]{-1, 1}) {
                double tPeak = k / 3.5 - (s > 0 ? 0.07 : 0.0);
                double u0 = 0.1 + 0.9 * (tPeak - 0.09), u1 = 0.1 + 0.9 * tPeak;
                double t = tPeak;
                double env = 0.30 * Math.pow(Math.sin(Math.PI * t), 0.6) * (0.72 + 0.38 * t) * 0.8;
                for (int i = 0; i <= 20; i++) {
                    double f = i / 20.0;
                    double u = u0 + (u1 - u0) * f, v = s * env * f;
                    int x = (int) Math.floor(bx + (ax * u - ay * v) * len), y = (int) Math.floor(by + (ay * u + ax * v) * len);
                    if (x < 0 || y < 0 || x >= p.w || y >= p.h || !in[x][y]) continue;
                    if (p.get(x, y) != OUTLINE) p.set(x, y, VEIN);
                }
            }
        }
    }

    private static boolean inside(boolean[][] in, int x, int y) {
        return x >= 0 && y >= 0 && x < in.length && y < in[0].length && in[x][y];
    }

    // ------------------------------------------------------------------ baner 320x100

    static BufferedImage banner() {
        Px p = new Px(320, 100, 2442);
        Random r = p.rnd;
        for (int y = 0; y < 100; y++) for (int x = 0; x < 320; x++) {
            int c = Px.mix(0xFF132629, 0xFF41603F, Math.pow(y / 99.0, 1.3));
            p.set(x, y, Px.shade(c, 0.97 + r.nextDouble() * 0.05));
        }
        for (int i = 0; i < 26; i++) p.set(r.nextInt(320), r.nextInt(40), r.nextBoolean() ? 0xFFCFD8C0 : 0xFF8FA090);
        // księżyc
        for (int y = 3; y < 28; y++) for (int x = 290; x < 316; x++) {
            double d = Math.hypot(x + 0.5 - 303, y + 0.5 - 15);
            if (d <= 8) p.set(x, y, Px.shade(0xFFE8E2C8, d > 6.5 ? 0.9 : 1.0));
            else if (d <= 10) p.set(x, y, Px.mix(p.get(x, y), 0xFFE8E2C8, 0.18));
        }
        // dalekie wzgórza i bliski bór
        for (int x = 0; x < 320; x++) {
            int hill = (int) (72 + 5 * Math.sin(x * 0.031) + 3 * Math.sin(x * 0.11 + 1));
            for (int y = hill; y < 100; y++) p.set(x, y, 0xFF22382B);
        }
        for (int i = 0; i < 46; i++) {
            int x = i * 7 + r.nextInt(5) - 2, h = 14 + r.nextInt(18);
            pine(p, x, 100, h, 0xFF101E17);
        }
        for (int x = 0; x < 320; x++) for (int y = 94; y < 100; y++) p.set(x, y, 0xFF0C1712);
        // odznaka z ikony
        BufferedImage badge = icon(false);
        for (int y = 0; y < 64; y++) for (int x = 0; x < 64; x++) {
            int c = badge.getRGB(x, y);
            if ((c >>> 24) > 0) p.set(14 + x, 14 + y, c);
        }
        // napisy
        title(p, "ZWIADOWCY", new Font(Font.SERIF, Font.BOLD, 29), 90, 45, 0xFFF4CC78, 0xFFB0682A);
        title(p, "RUINY GORLANU", new Font(Font.SERIF, Font.BOLD, 15), 92, 66, 0xFFE8E2C8, 0xFFA8A088);
        title(p, "nieoficjalny mod fanowski  •  Forge 1.20.1", new Font(Font.SANS_SERIF, Font.PLAIN, 9), 93, 84, 0xFFCFD8C0, 0xFFA8B4A0);
        return p.img;
    }

    static void pine(Px p, int x, int base, int h, int color) {
        for (int y = base - h; y < base; y++) {
            double t = (y - (base - h)) / (double) h;
            int half = (int) Math.round(1 + t * h * 0.32 * (0.75 + 0.25 * Math.sin(t * 9)));
            for (int dx = -half; dx <= half; dx++) p.set(x + dx, y, color);
        }
    }

    /** Napis pikselowy (bez wygładzania): cień, ciemny kontur i pionowy gradient wypełnienia. */
    static void title(Px p, String text, Font font, int x, int baseline, int top, int bottom) {
        BufferedImage mask = new BufferedImage(p.w, p.h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = mask.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setFont(font);
        g.setColor(java.awt.Color.WHITE);
        g.drawString(text, x, baseline);
        g.dispose();
        int minY = p.h, maxY = 0;
        for (int yy = 0; yy < p.h; yy++) for (int xx = 0; xx < p.w; xx++) if (on(mask, xx, yy)) { minY = Math.min(minY, yy); maxY = Math.max(maxY, yy); }
        boolean[][] outline = new boolean[p.w][p.h];
        for (int yy = 0; yy < p.h; yy++) for (int xx = 0; xx < p.w; xx++) {
            if (on(mask, xx, yy)) continue;
            for (int dy = -1; dy <= 1 && !outline[xx][yy]; dy++) for (int dx = -1; dx <= 1; dx++) if (on(mask, xx + dx, yy + dy)) { outline[xx][yy] = true; break; }
        }
        for (int yy = 0; yy < p.h; yy++) for (int xx = 0; xx < p.w; xx++) {
            if ((outline[xx][yy] || on(mask, xx, yy)) && !on(mask, xx + 1, yy + 1) && !outline[Math.min(p.w - 1, xx + 1)][Math.min(p.h - 1, yy + 1)]) {
                p.set(xx + 1, yy + 1, Px.mix(p.get(xx + 1, yy + 1), 0xFF000000, 0.55));
            }
        }
        for (int yy = 0; yy < p.h; yy++) for (int xx = 0; xx < p.w; xx++) {
            if (outline[xx][yy]) p.set(xx, yy, INK);
            else if (on(mask, xx, yy)) p.set(xx, yy, Px.mix(top, bottom, maxY > minY ? (yy - minY) / (double) (maxY - minY) : 0));
        }
    }

    private static boolean on(BufferedImage img, int x, int y) {
        return x >= 0 && y >= 0 && x < img.getWidth() && y < img.getHeight() && (img.getRGB(x, y) >>> 24) > 0;
    }

    // ------------------------------------------------------------------ zapis

    static BufferedImage scale(BufferedImage src, int k) {
        BufferedImage out = new BufferedImage(src.getWidth() * k, src.getHeight() * k, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < out.getHeight(); y++) for (int x = 0; x < out.getWidth(); x++) out.setRGB(x, y, src.getRGB(x / k, y / k));
        return out;
    }

    static void save(BufferedImage img, File f) throws Exception {
        f.getParentFile().mkdirs();
        ImageIO.write(img, "png", f);
    }

    private PromoGen() {}
}
