package pl.zwiadowcy.registry;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import pl.zwiadowcy.Zwiadowcy;

public final class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Zwiadowcy.MODID);

    /** Czujność: szybsze napinanie łuku zwiadowcy i lepsze odczytywanie śladów. */
    public static final RegistryObject<MobEffect> CZUJNOSC = EFFECTS.register("czujnosc",
            () -> new SimpleEffect(MobEffectCategory.BENEFICIAL, 0x8a5a2b)
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED, "6a1d3c5e-2f4b-4a51-9c39-1f6f3a0b7d11", 0.05, AttributeModifier.Operation.MULTIPLY_TOTAL));

    /** Groza: spojrzenie Kalkara. Poziom I spowalnia, poziom II paraliżuje. */
    public static final RegistryObject<MobEffect> GROZA = EFFECTS.register("groza",
            () -> new SimpleEffect(MobEffectCategory.HARMFUL, 0x3b0d0d)
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED, "1f0c6a92-77d3-4e2b-a1c8-5b9e8f3d2c44", -0.55, AttributeModifier.Operation.MULTIPLY_TOTAL)
                    .addAttributeModifier(Attributes.ATTACK_SPEED, "9b2e4f10-3c6d-4d8a-b7e5-0a1f2c3d4e55", -0.3, AttributeModifier.Operation.MULTIPLY_TOTAL));

    /** Oblany olejem: obrażenia od ognia są podwojone. */
    public static final RegistryObject<MobEffect> OBLANY_OLEJEM = EFFECTS.register("oblany_olejem",
            () -> new SimpleEffect(MobEffectCategory.HARMFUL, 0x4d4416));

    public static class SimpleEffect extends MobEffect {
        public SimpleEffect(MobEffectCategory category, int color) {
            super(category, color);
        }
    }

    private ModEffects() {}
}
