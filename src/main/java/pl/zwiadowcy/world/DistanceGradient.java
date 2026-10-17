package pl.zwiadowcy.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import pl.zwiadowcy.Zwiadowcy;

/**
 * Funkcja gęstości "zwiadowcy:odleglosc_od_srodka": 0 w promieniu {@code inner} od środka świata (okolice Redmont),
 * 1 dalej niż {@code outer}, płynnie pomiędzy. Generator Araluenu mnoży nią rozrzut kontynentalności - blisko domu
 * teren jest łagodny (lasy, łąki, pagórki), a wysokie góry rosną dopiero w dalekich krainach trzeciego aktu.
 */
public record DistanceGradient(double inner, double outer) implements DensityFunction.SimpleFunction {
    public static final MapCodec<DistanceGradient> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.DOUBLE.fieldOf("inner").forGetter(DistanceGradient::inner),
            Codec.DOUBLE.fieldOf("outer").forGetter(DistanceGradient::outer)
    ).apply(i, DistanceGradient::new));
    public static final KeyDispatchDataCodec<DistanceGradient> CODEC = KeyDispatchDataCodec.of(MAP_CODEC);

    public static final DeferredRegister<Codec<? extends DensityFunction>> TYPES =
            DeferredRegister.create(Registries.DENSITY_FUNCTION_TYPE, Zwiadowcy.MODID);
    public static final RegistryObject<Codec<? extends DensityFunction>> TYPE = TYPES.register("odleglosc_od_srodka", MAP_CODEC::codec);

    @Override
    public double compute(FunctionContext ctx) {
        double d = Math.sqrt((double) ctx.blockX() * ctx.blockX() + (double) ctx.blockZ() * ctx.blockZ());
        double t = Mth.clamp((d - inner) / (outer - inner), 0.0, 1.0);
        return t * t * (3 - 2 * t);
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
