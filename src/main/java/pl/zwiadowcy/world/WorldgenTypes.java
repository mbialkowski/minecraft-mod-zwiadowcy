package pl.zwiadowcy.world;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import pl.zwiadowcy.Zwiadowcy;

/** Własne typy funkcji gęstości generatora Araluenu. */
public final class WorldgenTypes {
    public static final DeferredRegister<Codec<? extends DensityFunction>> TYPES =
            DeferredRegister.create(Registries.DENSITY_FUNCTION_TYPE, Zwiadowcy.MODID);

    public static final RegistryObject<Codec<? extends DensityFunction>> ODLEGLOSC_OD_SRODKA =
            TYPES.register("odleglosc_od_srodka", DistanceGradient.MAP_CODEC::codec);
    public static final RegistryObject<Codec<? extends DensityFunction>> REGION =
            TYPES.register("region", RegionMask.MAP_CODEC::codec);
    public static final RegistryObject<Codec<? extends DensityFunction>> ROZPADLINA =
            TYPES.register("rozpadlina", FissureDensity.MAP_CODEC::codec);

    private WorldgenTypes() {}
}
