package pl.zwiadowcy.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;

/**
 * Funkcja gęstości "zwiadowcy:rozpadlina" - wielki kanion tomu II wzdłuż łamanej {@link VolumeTwoLayout#FISSURE}.
 * <ul>
 *   <li>tryb "teren": wartość &le; 0 wycina powietrze (łączona przez minimum z gęstością terenu); poza kanionem 1.0,</li>
 *   <li>tryb "dno": 0.5 tuż pod dnem (łączona przez maksimum), żeby jaskinie nie otwierały dziur w dnie,</li>
 *   <li>tryb "woda": -1 w pasie kanionu (z zapasem), 1 poza nim - łączona przez minimum z „zalaniem” warstw
 *       wodonośnych, żeby kanion nie wypełniał się wodą ani lawą.</li>
 * </ul>
 * Dno leży nad poziomem wody Araluenu (50), ściany poszerzają się ku górze i są poszarpane szumem.
 */
public record FissureDensity(String mode, double halfMin, double halfMax, double floor, double slope) implements DensityFunction {
    public static final MapCodec<FissureDensity> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.STRING.fieldOf("mode").forGetter(FissureDensity::mode),
            Codec.DOUBLE.fieldOf("half_min").forGetter(FissureDensity::halfMin),
            Codec.DOUBLE.fieldOf("half_max").forGetter(FissureDensity::halfMax),
            Codec.DOUBLE.fieldOf("floor").forGetter(FissureDensity::floor),
            Codec.DOUBLE.fieldOf("slope").forGetter(FissureDensity::slope)
    ).apply(i, FissureDensity::new));
    public static final KeyDispatchDataCodec<FissureDensity> CODEC = KeyDispatchDataCodec.of(MAP_CODEC);

    private static final double MIN_X, MAX_X, MIN_Z, MAX_Z;

    static {
        double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE, minZ = Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;
        for (double[] p : VolumeTwoLayout.FISSURE) {
            minX = Math.min(minX, p[0]);
            maxX = Math.max(maxX, p[0]);
            minZ = Math.min(minZ, p[1]);
            maxZ = Math.max(maxZ, p[1]);
        }
        MIN_X = minX - 90;
        MAX_X = maxX + 90;
        MIN_Z = minZ - 90;
        MAX_Z = maxZ + 90;
    }

    @Override
    public double compute(FunctionContext ctx) {
        return value(ctx.blockX(), ctx.blockY(), ctx.blockZ());
    }

    public double value(int wx, int y, int wz) {
        double x = VolumeTwoLayout.layoutX(wx, wz), z = VolumeTwoLayout.layoutZ(wx, wz);
        double neutral = mode.equals("dno") ? -1.0 : 1.0;
        if (x < MIN_X || x > MAX_X || z < MIN_Z || z > MAX_Z) return neutral;
        double[] da = VolumeTwoLayout.fissureDistance(x, z);
        double d = da[0], along = da[1];
        double taper = smooth(Mth.clamp(along / 0.06, 0, 1)) * smooth(Mth.clamp((1 - along) / 0.06, 0, 1));
        double base = halfMin + (halfMax - halfMin) * (0.5 + 0.5 * HashNoise.noise2(along * 14, 0.5, 7));
        if (mode.equals("woda")) {
            return d < base * taper + 24 && y > floor - 24 ? -1.0 : 1.0;
        }
        double fl = floor + HashNoise.noise2(wx * 0.05, wz * 0.05, 11) * 2.5;
        if (mode.equals("dno")) {
            // zatyczka: 5 bloków pod dnem zawsze litych (łączona przez maksimum) - jaskinie nie dziurawią dna kanionu
            return d < base * taper + 4 && y < fl && y >= fl - 5 ? 0.5 : -1.0;
        }
        double hw = base * taper + HashNoise.noise3(wx * 0.07, y * 0.09, wz * 0.07, 13) * 3.5 + Math.max(0, y - fl) * slope;
        double v = (d - hw) / 5.0;
        double vf = (fl - y) / 5.0;
        return Mth.clamp(Math.max(v, vf), -1.0, 1.0);
    }

    private static double smooth(double t) {
        return t * t * (3 - 2 * t);
    }

    @Override
    public void fillArray(double[] values, ContextProvider provider) {
        provider.fillAllDirectly(values, this);
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(this);
    }

    @Override
    public double minValue() {
        return -1.0;
    }

    @Override
    public double maxValue() {
        return 1.0;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }
}
