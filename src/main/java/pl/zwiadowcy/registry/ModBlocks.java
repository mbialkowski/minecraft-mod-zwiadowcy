package pl.zwiadowcy.registry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import pl.zwiadowcy.Zwiadowcy;
import pl.zwiadowcy.block.ArcheryTargetBlock;
import pl.zwiadowcy.block.BaronPapersBlock;
import pl.zwiadowcy.block.HearthBlock;
import pl.zwiadowcy.block.RopeBlock;
import pl.zwiadowcy.block.TracksBlock;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Zwiadowcy.MODID);

    public static final RegistryObject<Block> TARCZA_LUCZNICZA = BLOCKS.register("tarcza_lucznicza",
            () -> new ArcheryTargetBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_YELLOW).strength(0.6f).sound(SoundType.GRASS)));

    public static final RegistryObject<Block> SLADY = BLOCKS.register("slady",
            () -> new TracksBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DIRT).noCollission().instabreak().noOcclusion()
                    .sound(SoundType.GRAVEL).pushReaction(PushReaction.DESTROY)));

    public static final RegistryObject<Block> LINA = BLOCKS.register("lina",
            () -> new RopeBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD).noCollission().strength(0.2f).noOcclusion()
                    .sound(SoundType.WOOL).pushReaction(PushReaction.DESTROY)));

    public static final RegistryObject<Block> PAPIERY_BARONA = BLOCKS.register("papiery_barona",
            () -> new BaronPapersBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.SNOW).noCollission().strength(0.1f).noOcclusion()
                    .sound(SoundType.WOOL).pushReaction(PushReaction.DESTROY)));

    /** Palenisko (wystrój struktur): ognisko bez bloku-encji i bez gęstego dymu. */
    public static final RegistryObject<Block> PALENISKO = BLOCKS.register("palenisko",
            () -> new HearthBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PODZOL).strength(2.0f).sound(SoundType.WOOD).lightLevel(s -> 15)
                    .noOcclusion().ignitedByLava(), false));

    public static final RegistryObject<Block> PALENISKO_DUSZ = BLOCKS.register("palenisko_dusz",
            () -> new HearthBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PODZOL).strength(2.0f).sound(SoundType.WOOD).lightLevel(s -> 10)
                    .noOcclusion(), true));

    // --- tom II
    /** Ognisko zwiadowców: punkt szybkiej podróży (PPM - Mapa Gilana). */
    public static final RegistryObject<Block> OGNISKO_ZWIADOWCOW = BLOCKS.register("ognisko_zwiadowcow",
            () -> new pl.zwiadowcy.block.ScoutCampfireBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PODZOL).strength(2.0f).sound(SoundType.WOOD).lightLevel(s -> 15).noOcclusion()));

    /** Belki mostu nad Rozpadliną: płonąca, zwęglona i drewno oblane olejem (skrypt spalenia mostu). */
    public static final RegistryObject<Block> PLONACA_BELKA = BLOCKS.register("plonaca_belka",
            () -> new pl.zwiadowcy.block.BurningBeamBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.FIRE).strength(1.0f).sound(SoundType.WOOD).lightLevel(s -> 13)));
    public static final RegistryObject<Block> ZWEGLONA_BELKA = BLOCKS.register("zweglona_belka",
            () -> new net.minecraft.world.level.block.RotatedPillarBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK).strength(1.0f).sound(SoundType.WOOD)));
    public static final RegistryObject<Block> OBLANE_DREWNO = BLOCKS.register("oblane_drewno",
            () -> new net.minecraft.world.level.block.RotatedPillarBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN).strength(2.0f).sound(SoundType.WOOD).ignitedByLava()));

    private ModBlocks() {}
}
