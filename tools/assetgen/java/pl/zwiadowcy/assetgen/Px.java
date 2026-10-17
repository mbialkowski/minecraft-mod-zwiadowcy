package pl.zwiadowcy.assetgen;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Random;

/** Prosty "pędzel" pikselowy na BufferedImage ARGB. */
final class Px {
    final BufferedImage img;
    final int w, h;
    final Random rnd;

    Px(int w, int h, long seed) {
        this.w = w;
        this.h = h;
        this.img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        this.rnd = new Random(seed);
    }

    static int rgb(int rgb) {
        return 0xFF000000 | rgb;
    }

    static int argb(int a, int rgb) {
        return (a << 24) | (rgb & 0xFFFFFF);
    }

    static int shade(int argb, double f) {
        int a = argb >>> 24;
        int r = clamp((int) (((argb >> 16) & 0xFF) * f));
        int g = clamp((int) (((argb >> 8) & 0xFF) * f));
        int b = clamp((int) ((argb & 0xFF) * f));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    static int mix(int c1, int c2, double t) {
        int a = (int) (((c1 >>> 24) * (1 - t)) + ((c2 >>> 24) * t));
        int r = (int) ((((c1 >> 16) & 0xFF) * (1 - t)) + (((c2 >> 16) & 0xFF) * t));
        int g = (int) ((((c1 >> 8) & 0xFF) * (1 - t)) + (((c2 >> 8) & 0xFF) * t));
        int b = (int) (((c1 & 0xFF) * (1 - t)) + ((c2 & 0xFF) * t));
        return (clamp(a) << 24) | (clamp(r) << 16) | (clamp(g) << 8) | clamp(b);
    }

    static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }

    void set(int x, int y, int argb) {
        if (x >= 0 && y >= 0 && x < w && y < h) img.setRGB(x, y, argb);
    }

    int get(int x, int y) {
        return (x >= 0 && y >= 0 && x < w && y < h) ? img.getRGB(x, y) : 0;
    }

    boolean opaque(int x, int y) {
        return (get(x, y) >>> 24) > 0;
    }

    void rect(int x, int y, int rw, int rh, int argb) {
        for (int i = x; i < x + rw; i++) for (int j = y; j < y + rh; j++) set(i, j, argb);
    }

    /** Wypełnienie z losowym rozrzutem jasności (±var). */
    void noisy(int x, int y, int rw, int rh, int argb, double var) {
        for (int i = x; i < x + rw; i++) {
            for (int j = y; j < y + rh; j++) {
                set(i, j, shade(argb, 1.0 + (rnd.nextDouble() * 2 - 1) * var));
            }
        }
    }

    /** Futro: rozrzut jasności + pionowe ciemniejsze pasma. */
    void fur(int x, int y, int rw, int rh, int argb, double var) {
        noisy(x, y, rw, rh, argb, var);
        for (int i = x; i < x + rw; i++) {
            if (rnd.nextDouble() < 0.35) {
                int len = 2 + rnd.nextInt(3);
                int start = y + rnd.nextInt(Math.max(1, rh));
                for (int j = start; j < Math.min(y + rh, start + len); j++) set(i, j, shade(argb, 0.78));
            }
        }
    }

    /** Kamuflaż zwiadowcy: plamy kilku odcieni zieleni, szarości i brązu. */
    void camo(int x, int y, int rw, int rh, int[] palette) {
        for (int i = x; i < x + rw; i++) {
            for (int j = y; j < y + rh; j++) set(i, j, palette[0]);
        }
        int blobs = Math.max(1, rw * rh / 6);
        for (int b = 0; b < blobs; b++) {
            int c = palette[1 + rnd.nextInt(palette.length - 1)];
            int bx = x + rnd.nextInt(Math.max(1, rw)), by = y + rnd.nextInt(Math.max(1, rh));
            int size = 1 + rnd.nextInt(2);
            for (int i = bx; i < Math.min(x + rw, bx + size + 1); i++) {
                for (int j = by; j < Math.min(y + rh, by + size); j++) set(i, j, shade(c, 0.95 + rnd.nextDouble() * 0.1));
            }
        }
    }

    void line(int x0, int y0, int x1, int y1, int argb) {
        int dx = Math.abs(x1 - x0), dy = -Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1, sy = y0 < y1 ? 1 : -1;
        int err = dx + dy;
        while (true) {
            set(x0, y0, argb);
            if (x0 == x1 && y0 == y1) break;
            int e2 = 2 * err;
            if (e2 >= dy) { err += dy; x0 += sx; }
            if (e2 <= dx) { err += dx; y0 += sy; }
        }
    }

    /** Rysunek z siatki znaków; znak '.' = przezroczysty. Paleta może mieć kilka odcieni (losowanych). */
    void grid(String[] rows, Map<Character, int[]> palette) {
        for (int y = 0; y < rows.length && y < h; y++) {
            String row = rows[y];
            for (int x = 0; x < row.length() && x < w; x++) {
                char c = row.charAt(x);
                if (c == '.' || c == ' ') continue;
                int[] cols = palette.get(c);
                if (cols == null) throw new IllegalArgumentException("Brak koloru dla znaku '" + c + "'");
                set(x, y, cols.length == 1 ? cols[0] : cols[rnd.nextInt(cols.length)]);
            }
        }
    }

    /** Proste cieniowanie: jaśniej przy krawędzi od góry/lewej, ciemniej od dołu/prawej. */
    void autoShade(int outline) {
        int[][] src = new int[w][h];
        for (int x = 0; x < w; x++) for (int y = 0; y < h; y++) src[x][y] = get(x, y);
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int c = src[x][y];
                if ((c >>> 24) == 0 || c == outline) continue;
                boolean topEdge = edge(src, x, y - 1, outline) || edge(src, x - 1, y, outline);
                boolean bottomEdge = edge(src, x, y + 1, outline) || edge(src, x + 1, y, outline);
                if (topEdge && !bottomEdge) set(x, y, shade(c, 1.18));
                else if (bottomEdge && !topEdge) set(x, y, shade(c, 0.82));
            }
        }
    }

    private boolean edge(int[][] src, int x, int y, int outline) {
        if (x < 0 || y < 0 || x >= w || y >= h) return true;
        int c = src[x][y];
        return (c >>> 24) == 0 || c == outline;
    }

    /** Ściany prostopadłościanu w układzie UV modelu Minecrafta. */
    interface Face {
        void paint(Px px, String face, int x, int y, int fw, int fh);
    }

    void box(int u, int v, int bw, int bh, int bd, Face f) {
        f.paint(this, "top", u + bd, v, bw, bd);
        f.paint(this, "bottom", u + bd + bw, v, bw, bd);
        f.paint(this, "right", u, v + bd, bd, bh);
        f.paint(this, "front", u + bd, v + bd, bw, bh);
        f.paint(this, "left", u + bd + bw, v + bd, bd, bh);
        f.paint(this, "back", u + bd + bw + bd, v + bd, bw, bh);
    }

    void save(File file) throws IOException {
        file.getParentFile().mkdirs();
        ImageIO.write(img, "png", file);
    }
}
