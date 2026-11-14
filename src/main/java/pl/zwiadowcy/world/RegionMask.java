package pl.zwiadowcy.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;

import java.util.List;

/**
 * Funkcja gęstości "zwiadowcy:region": maska 0-1 krainy tomu II (1 w środku, płynnie do 0 na szerokości "edge").
 * Kształt "elipsa" (x, z, rx, rz) albo "kapsula" (x1, z1, x2, z2, szerokość). Współrzędne są w układzie tomu II,
 * więc maska obraca się razem z całym układem ({@link VolumeTwoLayout}).
 */
public record RegionMask(String shape, List<Double> a, double edge) implements DensityFunction.SimpleFunction {
    public static final MapCodec<RegionMask> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.STRING.fieldOf("shape").forGetter(RegionMask::shape),
            Codec.DOUBLE.listOf().fieldOf("a").forGetter(RegionMask::a),
            Codec.DOUBLE.fieldOf("edge").forGetter(RegionMask::edge)
    ).apply(i, RegionMask::new));
    public static final KeyDispatchDataCodec<RegionMask> CODEC = KeyDispatchDataCodec.of(MAP_CODEC);

    @Override
    public double compute(FunctionContext ctx) {
        return value(shape, a, edge, ctx.blockX(), ctx.blockZ());
    }

    /** Wartość maski w punkcie świata (używane też przez autotest i mapę). */
    public static double value(String shape, List<Double> a, double edge, double wx, double wz) {
        double x = VolumeTwoLayout.layoutX(wx, wz), z = VolumeTwoLayout.layoutZ(wx, wz);
        double outside;
        if (shape.equals("kapsula")) {
            double ax = a.get(0), az = a.get(1), bx = a.get(2), bz = a.get(3), half = a.get(4) / 2;
            double dx = bx - ax, dz = bz - az;
            double t = Mth.clamp(((x - ax) * dx + (z - az) * dz) / (dx * dx + dz * dz), 0, 1);
            outside = Math.hypot(x - (ax + dx * t), z - (az + dz * t)) - half;
        } else {
            double rx = a.get(2), rz = a.get(3);
            double q = Math.sqrt(sq((x - a.get(0)) / rx) + sq((z - a.get(1)) / rz));
            outside = (q - 1) * Math.min(rx, rz);
        }
        if (outside <= 0) return 1.0;
        double t = Mth.clamp(outside / edge, 0, 1);
        return 1 - t * t * (3 - 2 * t);
    }

    private static double sq(double v) {
        return v * v;
    }

    @Override
    public double minValue() {
        return 0.0;
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
