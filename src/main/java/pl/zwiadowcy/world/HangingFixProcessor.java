package pl.zwiadowcy.world;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import pl.zwiadowcy.Zwiadowcy;

/**
 * Obrazy i ramki zapisane w szablonach struktur potrzebują współrzędnych zawieszenia (TileX/Y/Z) w świecie.
 * Generator nie zna ich z góry, więc procesor uzupełnia je w chwili stawiania struktury.
 */
public class HangingFixProcessor extends StructureProcessor {
    public static final HangingFixProcessor INSTANCE = new HangingFixProcessor();
    public static final Codec<HangingFixProcessor> CODEC = Codec.unit(() -> INSTANCE);

    public static final DeferredRegister<StructureProcessorType<?>> PROCESSORS =
            DeferredRegister.create(Registries.STRUCTURE_PROCESSOR, Zwiadowcy.MODID);
    public static final RegistryObject<StructureProcessorType<HangingFixProcessor>> TYPE =
            PROCESSORS.register("hanging_fix", () -> () -> CODEC);

    @Override
    protected StructureProcessorType<?> getType() {
        return TYPE.get();
    }

    @Override
    public StructureTemplate.StructureEntityInfo processEntity(LevelReader world, BlockPos seedPos,
                                                               StructureTemplate.StructureEntityInfo rawEntityInfo,
                                                               StructureTemplate.StructureEntityInfo entityInfo,
                                                               StructurePlaceSettings settings, StructureTemplate template) {
        String id = entityInfo.nbt.getString("id");
        if (!id.equals("minecraft:painting") && !id.equals("minecraft:item_frame") && !id.equals("minecraft:glow_item_frame")) {
            return entityInfo;
        }
        CompoundTag nbt = entityInfo.nbt.copy();
        BlockPos pos = BlockPos.containing(entityInfo.pos);
        nbt.putInt("TileX", pos.getX());
        nbt.putInt("TileY", pos.getY());
        nbt.putInt("TileZ", pos.getZ());
        return new StructureTemplate.StructureEntityInfo(entityInfo.pos, entityInfo.blockPos, nbt);
    }
}
