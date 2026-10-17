package pl.zwiadowcy.registry;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import pl.zwiadowcy.Zwiadowcy;
import pl.zwiadowcy.entity.*;
import pl.zwiadowcy.entity.projectile.OilFlaskEntity;
import pl.zwiadowcy.entity.projectile.RangerArrowEntity;
import pl.zwiadowcy.entity.projectile.ThrowingKnifeEntity;

@Mod.EventBusSubscriber(modid = Zwiadowcy.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Zwiadowcy.MODID);

    public static final RegistryObject<EntityType<StoryNpc>> NPC = ENTITIES.register("npc",
            () -> EntityType.Builder.<StoryNpc>of(StoryNpc::new, MobCategory.MISC)
                    .sized(0.6f, 1.8f).clientTrackingRange(10).build("npc"));

    public static final RegistryObject<EntityType<KalkarEntity>> KALKAR = ENTITIES.register("kalkar",
            () -> EntityType.Builder.of(KalkarEntity::new, MobCategory.MONSTER)
                    .sized(1.3f, 3.1f).clientTrackingRange(10).build("kalkar"));

    public static final RegistryObject<EntityType<WargalEntity>> WARGAL = ENTITIES.register("wargal",
            () -> EntityType.Builder.<WargalEntity>of((t, l) -> new WargalEntity(t, l, WargalEntity.Variant.WOJOWNIK), MobCategory.MONSTER)
                    .sized(0.8f, 1.7f).clientTrackingRange(8).build("wargal"));

    public static final RegistryObject<EntityType<WargalEntity>> WARGAL_ZWIADOWCA = ENTITIES.register("wargal_zwiadowca",
            () -> EntityType.Builder.<WargalEntity>of((t, l) -> new WargalEntity(t, l, WargalEntity.Variant.ZWIADOWCA), MobCategory.MONSTER)
                    .sized(0.7f, 1.5f).clientTrackingRange(8).build("wargal_zwiadowca"));

    public static final RegistryObject<EntityType<WargalEntity>> WARGAL_OSZCZEPNIK = ENTITIES.register("wargal_oszczepnik",
            () -> EntityType.Builder.<WargalEntity>of((t, l) -> new WargalEntity(t, l, WargalEntity.Variant.OSZCZEPNIK), MobCategory.MONSTER)
                    .sized(0.8f, 1.7f).clientTrackingRange(8).build("wargal_oszczepnik"));

    public static final RegistryObject<EntityType<WargalEntity>> WARGAL_HERSZT = ENTITIES.register("wargal_herszt",
            () -> EntityType.Builder.<WargalEntity>of((t, l) -> new WargalEntity(t, l, WargalEntity.Variant.HERSZT), MobCategory.MONSTER)
                    .sized(1.1f, 2.3f).clientTrackingRange(10).build("wargal_herszt"));

    public static final RegistryObject<EntityType<BanditEntity>> BANDYTA = ENTITIES.register("bandyta",
            () -> EntityType.Builder.<BanditEntity>of((t, l) -> new BanditEntity(t, l, BanditEntity.Variant.BANDYTA), MobCategory.MONSTER)
                    .sized(0.6f, 1.95f).clientTrackingRange(8).build("bandyta"));

    public static final RegistryObject<EntityType<BanditEntity>> LUCZNIK_BANDYTOW = ENTITIES.register("lucznik_bandytow",
            () -> EntityType.Builder.<BanditEntity>of((t, l) -> new BanditEntity(t, l, BanditEntity.Variant.LUCZNIK), MobCategory.MONSTER)
                    .sized(0.6f, 1.95f).clientTrackingRange(8).build("lucznik_bandytow"));

    public static final RegistryObject<EntityType<BanditEntity>> HERSZT_BANDYTOW = ENTITIES.register("herszt_bandytow",
            () -> EntityType.Builder.<BanditEntity>of((t, l) -> new BanditEntity(t, l, BanditEntity.Variant.HERSZT), MobCategory.MONSTER)
                    .sized(0.6f, 1.95f).clientTrackingRange(10).build("herszt_bandytow"));

    public static final RegistryObject<EntityType<BearEntity>> NIEDZWIEDZ = ENTITIES.register("niedzwiedz",
            () -> EntityType.Builder.of(BearEntity::new, MobCategory.CREATURE)
                    .sized(1.4f, 1.4f).clientTrackingRange(10).build("niedzwiedz"));

    public static final RegistryObject<EntityType<pl.zwiadowcy.entity.projectile.JavelinEntity>> OSZCZEP = ENTITIES.register("oszczep",
            () -> EntityType.Builder.<pl.zwiadowcy.entity.projectile.JavelinEntity>of(pl.zwiadowcy.entity.projectile.JavelinEntity::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f).clientTrackingRange(4).updateInterval(20).build("oszczep"));

    public static final RegistryObject<EntityType<BoarEntity>> DZIK = ENTITIES.register("dzik",
            () -> EntityType.Builder.of(BoarEntity::new, MobCategory.CREATURE)
                    .sized(1.1f, 1.0f).clientTrackingRange(8).build("dzik"));

    public static final RegistryObject<EntityType<TugEntity>> WYRWIJ = ENTITIES.register("wyrwij",
            () -> EntityType.Builder.of(TugEntity::new, MobCategory.CREATURE)
                    .sized(1.2f, 1.5f).clientTrackingRange(10).build("wyrwij"));

    public static final RegistryObject<EntityType<RangerArrowEntity>> STRZALA = ENTITIES.register("strzala",
            () -> EntityType.Builder.<RangerArrowEntity>of(RangerArrowEntity::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f).clientTrackingRange(4).updateInterval(20).build("strzala"));

    public static final RegistryObject<EntityType<ThrowingKnifeEntity>> NOZ = ENTITIES.register("noz",
            () -> EntityType.Builder.<ThrowingKnifeEntity>of(ThrowingKnifeEntity::new, MobCategory.MISC)
                    .sized(0.4f, 0.4f).clientTrackingRange(4).updateInterval(20).build("noz"));

    public static final RegistryObject<EntityType<OilFlaskEntity>> FLASZA = ENTITIES.register("flasza",
            () -> EntityType.Builder.<OilFlaskEntity>of(OilFlaskEntity::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f).clientTrackingRange(4).updateInterval(10).build("flasza"));

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(NPC.get(), StoryNpc.createAttributes().build());
        event.put(KALKAR.get(), KalkarEntity.createAttributes().build());
        event.put(WARGAL.get(), WargalEntity.createAttributes().build());
        event.put(DZIK.get(), BoarEntity.createAttributes().build());
        event.put(WYRWIJ.get(), TugEntity.createAttributes().build());
        event.put(WARGAL_ZWIADOWCA.get(), WargalEntity.attributes(WargalEntity.Variant.ZWIADOWCA).build());
        event.put(WARGAL_OSZCZEPNIK.get(), WargalEntity.attributes(WargalEntity.Variant.OSZCZEPNIK).build());
        event.put(WARGAL_HERSZT.get(), WargalEntity.attributes(WargalEntity.Variant.HERSZT).build());
        event.put(BANDYTA.get(), BanditEntity.attributes(BanditEntity.Variant.BANDYTA).build());
        event.put(LUCZNIK_BANDYTOW.get(), BanditEntity.attributes(BanditEntity.Variant.LUCZNIK).build());
        event.put(HERSZT_BANDYTOW.get(), BanditEntity.attributes(BanditEntity.Variant.HERSZT).build());
        event.put(NIEDZWIEDZ.get(), BearEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
        event.register(DZIK.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Animal::checkAnimalSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(NIEDZWIEDZ.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Animal::checkAnimalSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        for (RegistryObject<? extends EntityType<? extends net.minecraft.world.entity.monster.Monster>> type
                : java.util.List.of(WARGAL, WARGAL_ZWIADOWCA, WARGAL_OSZCZEPNIK, WARGAL_HERSZT, BANDYTA, LUCZNIK_BANDYTOW, HERSZT_BANDYTOW)) {
            registerFarMonster(event, type.get());
        }
    }

    private static <T extends net.minecraft.world.entity.monster.Monster> void registerFarMonster(SpawnPlacementRegisterEvent event, EntityType<T> type) {
        event.register(type, SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ModEntities::farFromRedmont, SpawnPlacementRegisterEvent.Operation.REPLACE);
    }

    /** Naturalnie pojawiający się wrogowie: zasady potworów vanilla + nigdy bliżej niż 420 bloków od Redmont. */
    public static <T extends net.minecraft.world.entity.monster.Monster> boolean farFromRedmont(EntityType<T> type,
            net.minecraft.world.level.ServerLevelAccessor level, net.minecraft.world.entity.MobSpawnType reason,
            net.minecraft.core.BlockPos pos, net.minecraft.util.RandomSource random) {
        if (!net.minecraft.world.entity.monster.Monster.checkMonsterSpawnRules(type, level, reason, pos, random)) return false;
        if (reason == net.minecraft.world.entity.MobSpawnType.NATURAL || reason == net.minecraft.world.entity.MobSpawnType.CHUNK_GENERATION) {
            long x = pos.getX(), z = pos.getZ();
            return x * x + z * z > 420L * 420L;
        }
        return true;
    }

    private ModEntities() {}
}
