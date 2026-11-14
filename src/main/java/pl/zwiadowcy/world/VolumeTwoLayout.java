package pl.zwiadowcy.world;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pl.zwiadowcy.Zwiadowcy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Układ krain tomu II w Araluenie: Celtyka na zachodzie, Przełęcz Wiatrów, Rozpadlina na południowym zachodzie,
 * trakt na wschód i Równina Uthal. Współrzędne „układu” są obracane wokół środka świata o kąt zapisany w świecie:
 * w nowych światach 0°, a w starych - tyle, żeby kanion i Celtyka trafiły na jeszcze nieodkryty teren
 * (funkcje gęstości liczą się z położenia, więc w odkrytych chunkach kanion by się urwał).
 */
@Mod.EventBusSubscriber(modid = Zwiadowcy.MODID)
public final class VolumeTwoLayout {
    /** Łamana Rozpadliny (współrzędne układu, x - wschód, z - południe). */
    public static final double[][] FISSURE = {
            {-1650, 1000}, {-1530, 1050}, {-1420, 1110}, {-1300, 1150}, {-1180, 1190}, {-1060, 1250}, {-950, 1300}};
    /** Punkt mostu na Rozpadlinie i kierunek na północny brzeg (strona Celtyki). */
    public static final double[] BRIDGE = {-1300, 1150};
    /** Elipsy Celtyki: rdzeń (wrzosowiska, doliny) i pogranicze aż do krawędzi Rozpadliny: x, z, rx, rz. */
    public static final double[][] CELTICA = {{-1900, 400, 600, 450}, {-1400, 1010, 520, 300}};
    /** Korytarz Przełęczy Wiatrów przez pierścień gór: x1, z1, x2, z2, szerokość. */
    public static final double[] PASS = {-950, 110, -1500, 230, 100};
    /** Trakt wschodni znad Rozpadliny do obozu królewskiego: x1, z1, x2, z2, szerokość. */
    public static final double[] ROAD = {-1150, 1060, 320, 1170, 120};
    /** Równina Uthal (bitwa): x, z, rx, rz. */
    public static final double[] UTHAL = {450, 1330, 320, 230};

    private static double angle;
    private static double cos = 1, sin = 0;

    public static double angleDegrees() {
        return Math.toDegrees(angle);
    }

    public static void setAngleDegrees(double deg) {
        angle = Math.toRadians(deg);
        cos = Math.cos(angle);
        sin = Math.sin(angle);
    }

    /** Punkt układu → współrzędne świata. */
    public static double[] toWorld(double x, double z) {
        return new double[]{x * cos - z * sin, x * sin + z * cos};
    }

    /** Współrzędne świata → punkt układu (odwrotny obrót). */
    public static double layoutX(double x, double z) {
        return x * cos + z * sin;
    }

    public static double layoutZ(double x, double z) {
        return -x * sin + z * cos;
    }

    public static BlockPos worldPos(double x, double z) {
        double[] w = toWorld(x, z);
        return new BlockPos((int) Math.round(w[0]), 0, (int) Math.round(w[1]));
    }

    // ------------------------------------------------------------------ wybór kąta dla świata

    /**
     * Przed pierwszym chunkiem Araluenu: kąt z zapisu albo nowy wybór (0° w nowym świecie, kolejne obroty o 30°,
     * jeśli w miejscu Rozpadliny lub Celtyki są już wygenerowane chunki ze starszej wersji moda).
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onServerStarting(ServerStartingEvent event) {
        MinecraftServer server = event.getServer();
        LocationData data = LocationData.get(server);
        if (data.layoutAngle() != null) {
            setAngleDegrees(data.layoutAngle());
            return;
        }
        double chosen = 0;
        ServerLevel araluen = Araluen.level(server);
        if (araluen != null) {
            for (int step = 0; step < 12; step++) {
                double deg = step * 30.0;
                if (isFree(araluen, deg)) {
                    chosen = deg;
                    break;
                }
                Zwiadowcy.LOGGER.info("Układ tomu II przy obrocie {}° trafia na odkryty teren - próbuję dalej", deg);
                chosen = -1;
            }
        }
        if (chosen < 0) {
            Zwiadowcy.LOGGER.warn("Nie znaleziono nieodkrytego miejsca na krainy tomu II - zostaje układ domyślny. "
                    + "Najlepiej zagrać tom II w nowym świecie.");
            chosen = 0;
        }
        setAngleDegrees(chosen);
        data.setLayoutAngle((float) chosen);
        Zwiadowcy.LOGGER.info("Układ krain tomu II: obrót {}°", chosen);
    }

    private static boolean isFree(ServerLevel level, double deg) {
        double c = Math.cos(Math.toRadians(deg)), s = Math.sin(Math.toRadians(deg));
        for (double[] p : samplePoints()) {
            int x = (int) Math.round(p[0] * c - p[1] * s), z = (int) Math.round(p[0] * s + p[1] * c);
            ChunkPos cp = new ChunkPos(x >> 4, z >> 4);
            try {
                Optional<CompoundTag> tag = level.getChunkSource().chunkMap.read(cp).join();
                if (tag.isPresent()) return false;
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    /** Punkty, w których teren musi być jeszcze niewygenerowany (kanion, Celtyka, przełęcz, trakt, Uthal). */
    static List<double[]> samplePoints() {
        List<double[]> pts = new ArrayList<>();
        for (int i = 0; i + 1 < FISSURE.length; i++) {
            for (double t = 0; t < 1; t += 0.25) {
                pts.add(new double[]{FISSURE[i][0] + (FISSURE[i + 1][0] - FISSURE[i][0]) * t,
                        FISSURE[i][1] + (FISSURE[i + 1][1] - FISSURE[i][1]) * t});
            }
        }
        for (double[] e : CELTICA) {
            pts.add(new double[]{e[0], e[1]});
            pts.add(new double[]{e[0] - e[2] * 0.7, e[1]});
            pts.add(new double[]{e[0] + e[2] * 0.7, e[1]});
            pts.add(new double[]{e[0], e[1] - e[3] * 0.7});
            pts.add(new double[]{e[0], e[1] + e[3] * 0.7});
        }
        for (double t = 0; t <= 1; t += 0.25) {
            pts.add(new double[]{PASS[0] + (PASS[2] - PASS[0]) * t, PASS[1] + (PASS[3] - PASS[1]) * t});
            pts.add(new double[]{ROAD[0] + (ROAD[2] - ROAD[0]) * t, ROAD[1] + (ROAD[3] - ROAD[1]) * t});
        }
        pts.add(new double[]{UTHAL[0], UTHAL[1]});
        return pts;
    }

    // ------------------------------------------------------------------ geometria Rozpadliny

    /** Odległość punktu układu od łamanej Rozpadliny i parametr wzdłuż niej (0..1). */
    public static double[] fissureDistance(double x, double z) {
        double best = Double.MAX_VALUE, along = 0, total = 0, acc = 0;
        for (int i = 0; i + 1 < FISSURE.length; i++) total += Math.hypot(FISSURE[i + 1][0] - FISSURE[i][0], FISSURE[i + 1][1] - FISSURE[i][1]);
        for (int i = 0; i + 1 < FISSURE.length; i++) {
            double ax = FISSURE[i][0], az = FISSURE[i][1], bx = FISSURE[i + 1][0], bz = FISSURE[i + 1][1];
            double dx = bx - ax, dz = bz - az;
            double len2 = dx * dx + dz * dz, len = Math.sqrt(len2);
            double t = Math.max(0, Math.min(1, ((x - ax) * dx + (z - az) * dz) / len2));
            double px = ax + dx * t, pz = az + dz * t;
            double d = Math.hypot(x - px, z - pz);
            if (d < best) {
                best = d;
                along = (acc + t * len) / total;
            }
            acc += len;
        }
        return new double[]{best, along};
    }

    /** Jednostkowy kierunek Rozpadliny w punkcie mostu (w układzie): od zachodu na wschód. */
    public static double[] bridgeDirection() {
        double dx = FISSURE[4][0] - FISSURE[2][0], dz = FISSURE[4][1] - FISSURE[2][1];
        double len = Math.hypot(dx, dz);
        return new double[]{dx / len, dz / len};
    }

    private VolumeTwoLayout() {}
}
