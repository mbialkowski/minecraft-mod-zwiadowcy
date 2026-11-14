package pl.zwiadowcy.world;

/** Prosty, deterministyczny szum wartości (bez zależności od ziarna świata) - kształty kanionu i ścian. */
public final class HashNoise {

    private static double hash(int x, int y, int z, int seed) {
        long h = x * 374761393L + y * 668265263L + z * 2147483647L + seed * 144665L;
        h = (h ^ (h >>> 13)) * 1274126177L;
        h ^= h >>> 16;
        return ((h & 0xFFFFFF) / (double) 0xFFFFFF) * 2 - 1;
    }

    private static double smooth(double t) {
        return t * t * (3 - 2 * t);
    }

    /** Szum 2D w zakresie [-1, 1]. */
    public static double noise2(double x, double z, int seed) {
        return noise3(x, 0, z, seed);
    }

    /** Szum 3D w zakresie [-1, 1]. */
    public static double noise3(double x, double y, double z, int seed) {
        int x0 = (int) Math.floor(x), y0 = (int) Math.floor(y), z0 = (int) Math.floor(z);
        double fx = smooth(x - x0), fy = smooth(y - y0), fz = smooth(z - z0);
        double c000 = hash(x0, y0, z0, seed), c100 = hash(x0 + 1, y0, z0, seed);
        double c010 = hash(x0, y0 + 1, z0, seed), c110 = hash(x0 + 1, y0 + 1, z0, seed);
        double c001 = hash(x0, y0, z0 + 1, seed), c101 = hash(x0 + 1, y0, z0 + 1, seed);
        double c011 = hash(x0, y0 + 1, z0 + 1, seed), c111 = hash(x0 + 1, y0 + 1, z0 + 1, seed);
        double x00 = c000 + (c100 - c000) * fx, x10 = c010 + (c110 - c010) * fx;
        double x01 = c001 + (c101 - c001) * fx, x11 = c011 + (c111 - c011) * fx;
        double y0v = x00 + (x10 - x00) * fy, y1v = x01 + (x11 - x01) * fy;
        return y0v + (y1v - y0v) * fz;
    }

    private HashNoise() {}
}
