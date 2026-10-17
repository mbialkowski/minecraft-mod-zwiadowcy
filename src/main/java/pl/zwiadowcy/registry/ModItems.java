package pl.zwiadowcy.registry;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import pl.zwiadowcy.Zwiadowcy;
import pl.zwiadowcy.entity.projectile.RangerArrowType;
import pl.zwiadowcy.item.*;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Zwiadowcy.MODID);

    // --- Fabuła ---
    public static final RegistryObject<Item> KRONIKA_ARALUENU = ITEMS.register("kronika_araluenu",
            () -> new ChronicleItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> DZIENNIK_ZWIADOWCY = ITEMS.register("dziennik_zwiadowcy",
            () -> new JournalItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> BRAZOWY_LISC_DEBU = ITEMS.register("brazowy_lisc_debu",
            () -> new TooltipItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON), 2));
    public static final RegistryObject<Item> DOKUMENT_BARONA = ITEMS.register("dokument_barona",
            () -> new TooltipItem(new Item.Properties().stacksTo(1), 2));
    public static final RegistryObject<Item> ZNAK_MORGARATHA = ITEMS.register("znak_morgaratha",
            () -> new TooltipItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON), 2));
    public static final RegistryObject<Item> FRAGMENT_Z_GORLANU = ITEMS.register("fragment_z_gorlanu",
            () -> new LoreItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> STRZALA_ZLOTU = ITEMS.register("strzala_zlotu",
            () -> new TooltipItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE), 2));
    public static final RegistryObject<Item> LIST_KROLEWSKI = ITEMS.register("list_krolewski",
            () -> new TooltipItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), 2));
    public static final RegistryObject<Item> ZAPIECZETOWANY_LIST = ITEMS.register("zapieczetowany_list",
            () -> new TooltipItem(new Item.Properties().stacksTo(1), 1));
    public static final RegistryObject<Item> KIEL_DZIKA = ITEMS.register("kiel_dzika",
            () -> new TooltipItem(new Item.Properties(), 1));
    public static final RegistryObject<Item> BELA_SUKNA = ITEMS.register("bela_sukna",
            () -> new TooltipItem(new Item.Properties().stacksTo(16), 1));

    // --- Wyposażenie zwiadowcy ---
    public static final RegistryObject<Item> LUK_ZWIADOWCY = ITEMS.register("luk_zwiadowcy",
            () -> new RangerBowItem(new Item.Properties().durability(640)));
    public static final RegistryObject<Item> KOLCZAN = ITEMS.register("kolczan",
            () -> new QuiverItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> STRZALA_PLONACA = ITEMS.register("strzala_plonaca",
            () -> new RangerArrowItem(RangerArrowType.PLONACA, new Item.Properties()));
    public static final RegistryObject<Item> STRZALA_SZEROKI_GROT = ITEMS.register("strzala_szeroki_grot",
            () -> new RangerArrowItem(RangerArrowType.SZEROKI_GROT, new Item.Properties()));
    public static final RegistryObject<Item> STRZALA_GWIZDZACA = ITEMS.register("strzala_gwizdzaca",
            () -> new RangerArrowItem(RangerArrowType.GWIZDZACA, new Item.Properties()));
    public static final RegistryObject<Item> STRZALA_Z_LINA = ITEMS.register("strzala_z_lina",
            () -> new RangerArrowItem(RangerArrowType.Z_LINA, new Item.Properties()));
    public static final RegistryObject<Item> NOZ_SAKSONSKI = ITEMS.register("noz_saksonski",
            () -> new SaxeKnifeItem(new Item.Properties()));
    public static final RegistryObject<Item> NOZ_DO_RZUCANIA = ITEMS.register("noz_do_rzucania",
            () -> new ThrowingKnifeItem(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> PLASZCZ_ZWIADOWCY = ITEMS.register("plaszcz_zwiadowcy",
            () -> new RangerCloakItem(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> ZESTAW_TROPICIELA = ITEMS.register("zestaw_tropiciela",
            () -> new TrackerKitItem(new Item.Properties().durability(48)));
    public static final RegistryObject<Item> GWIZDEK = ITEMS.register("gwizdek",
            () -> new WhistleItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> FLASZA_OLEJU = ITEMS.register("flasza_oleju",
            () -> new OilFlaskItem(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> OSZCZEP = ITEMS.register("oszczep",
            () -> new JavelinItem(new Item.Properties().stacksTo(8)));
    public static final RegistryObject<Item> TOPOR_HERSZTA = ITEMS.register("topor_herszta",
            () -> new ChieftainAxeItem(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> SAKIEWKA_BANDYTY = ITEMS.register("sakiewka_bandyty",
            () -> new BanditPurseItem(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> ZAPISKI = ITEMS.register("zapiski",
            () -> new LoreNoteItem(new Item.Properties().stacksTo(1)));

    // --- Prowiant ---
    public static final RegistryObject<Item> RACJE_PODROZNE = ITEMS.register("racje_podrozne",
            () -> new TooltipItem(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(8).saturationMod(0.9f)
                    .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 100, 0), 1.0f)
                    .build()), 1));
    public static final RegistryObject<Item> KAWA_ZWIADOWCY = ITEMS.register("kawa_zwiadowcy",
            () -> new CoffeeItem(new Item.Properties().stacksTo(16).food(new FoodProperties.Builder()
                    .nutrition(1).saturationMod(0.2f).alwaysEat().build())));
    public static final RegistryObject<Item> PALONE_ZIARNA_KAWY = ITEMS.register("palone_ziarna_kawy",
            () -> new Item(new Item.Properties()));

    // --- Bloki ---
    public static final RegistryObject<Item> TARCZA_LUCZNICZA = ITEMS.register("tarcza_lucznicza",
            () -> new BlockItem(ModBlocks.TARCZA_LUCZNICZA.get(), new Item.Properties()));
    public static final RegistryObject<Item> SLADY = ITEMS.register("slady",
            () -> new BlockItem(ModBlocks.SLADY.get(), new Item.Properties()));
    public static final RegistryObject<Item> LINA = ITEMS.register("lina",
            () -> new BlockItem(ModBlocks.LINA.get(), new Item.Properties()));
    public static final RegistryObject<Item> PAPIERY_BARONA = ITEMS.register("papiery_barona",
            () -> new BlockItem(ModBlocks.PAPIERY_BARONA.get(), new Item.Properties()));

    // --- Jaja przywołania ---
    public static final RegistryObject<Item> DZIK_SPAWN_EGG = ITEMS.register("dzik_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.DZIK, 0x4a3426, 0xd8cbb0, new Item.Properties()));
    public static final RegistryObject<Item> WARGAL_SPAWN_EGG = ITEMS.register("wargal_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.WARGAL, 0x3b3129, 0x9c2a1d, new Item.Properties()));
    public static final RegistryObject<Item> KALKAR_SPAWN_EGG = ITEMS.register("kalkar_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.KALKAR, 0x1d1714, 0xd11f1f, new Item.Properties()));
    public static final RegistryObject<Item> WARGAL_ZWIADOWCA_SPAWN_EGG = ITEMS.register("wargal_zwiadowca_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.WARGAL_ZWIADOWCA, 0x6a5e50, 0xc8a050, new Item.Properties()));
    public static final RegistryObject<Item> WARGAL_OSZCZEPNIK_SPAWN_EGG = ITEMS.register("wargal_oszczepnik_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.WARGAL_OSZCZEPNIK, 0x4a3e32, 0x8a8a90, new Item.Properties()));
    public static final RegistryObject<Item> WARGAL_HERSZT_SPAWN_EGG = ITEMS.register("wargal_herszt_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.WARGAL_HERSZT, 0x241c16, 0xb01818, new Item.Properties()));
    public static final RegistryObject<Item> BANDYTA_SPAWN_EGG = ITEMS.register("bandyta_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.BANDYTA, 0x6a4a2a, 0xa02020, new Item.Properties()));
    public static final RegistryObject<Item> LUCZNIK_BANDYTOW_SPAWN_EGG = ITEMS.register("lucznik_bandytow_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.LUCZNIK_BANDYTOW, 0x5a5a3a, 0xa02020, new Item.Properties()));
    public static final RegistryObject<Item> HERSZT_BANDYTOW_SPAWN_EGG = ITEMS.register("herszt_bandytow_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.HERSZT_BANDYTOW, 0x3a2a1a, 0xd4a640, new Item.Properties()));
    public static final RegistryObject<Item> NIEDZWIEDZ_SPAWN_EGG = ITEMS.register("niedzwiedz_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.NIEDZWIEDZ, 0x5a3a22, 0x3a2414, new Item.Properties()));

    private ModItems() {}
}
