package pl.zwiadowcy.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pl.zwiadowcy.Zwiadowcy;
import pl.zwiadowcy.client.screen.JournalScreen;
import pl.zwiadowcy.client.screen.StoryScreen;
import pl.zwiadowcy.dialogue.DialogueService;
import pl.zwiadowcy.entity.NpcService;
import pl.zwiadowcy.entity.StoryNpc;
import pl.zwiadowcy.world.Araluen;
import pl.zwiadowcy.world.LocationData;
import pl.zwiadowcy.world.Locations;
import pl.zwiadowcy.world.StoryLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Sesja zdjęciowa do README (tylko dla twórców: gradlew runClient -Pzdjecia=true).
 * Tworzy świat testowy, przenosi się do Araluenu i odwiedza kolejne kadry: ustawia porę dnia, stawia postacie,
 * ukrywa HUD, czeka na wczytanie terenu i robi zrzut (run/screenshots/zwiadowcy_*.png). Na koniec zamyka grę.
 * Kadry są opisane we współrzędnych szablonów lokacji (jak w StructureGen), więc trafiają w te same miejsca w każdym świecie.
 */
@Mod.EventBusSubscriber(modid = Zwiadowcy.MODID, value = Dist.CLIENT)
public final class PhotoTour {
    private static final boolean ENABLED = Boolean.getBoolean("zwiadowcy.zdjecia");

    /** Kadr: miejsce kamery i punkt, na który patrzy (współrzędne szablonu lokacji), pora dnia, przygotowanie sceny. */
    private record Shot(String name, String loc, double cx, double cy, double cz, double lx, double ly, double lz, long time,
                        Consumer<ServerPlayer> setup, Mode mode) {}

    private enum Mode { SCENE, DIALOGUE, JOURNAL, STORY }

    private enum Phase { TITLE, LOADING, ARRIVAL, PROLOG, SHOTS, DONE }

    private static Phase phase = Phase.TITLE;
    private static int wait;
    /** -Pod=12 w gradlew runClient -Pzdjecia=true: zacznij od wskazanego kadru (np. tylko tom II). */
    private static int index = Integer.getInteger("zwiadowcy.zdjecia.od", 0);
    private static int step;
    private static CompletableFuture<?> pending;
    /** Miejsce kamery bieżącego kadru: x, y stóp, z, yaw, pitch. */
    private static volatile double[] target;
    private static final List<Shot> SHOTS = new ArrayList<>();
    /** Kadry tomu II (od 13.) czekają dłużej: teren generuje się na nowo, daleko od Redmontu. */
    private static final int TOM2_FIRST_SHOT = 12, TOM2_WAIT = 260;

    static {
        SHOTS.add(new Shot("01_redmont", "redmont", 30.5, 8, 50.5, 30.5, 4, 20, 2000, p -> {}, Mode.SCENE));
        SHOTS.add(new Shot("02_wielka_sala", "redmont", 22.5, 3.4, 17.3, 22.5, 2.6, 9.5, 6000, p -> {}, Mode.SCENE));
        SHOTS.add(new Shot("03_baron", "redmont", 31.2, 2.7, 28.2, 34.5, 2.4, 25.5, 3000,
                p -> moveNpc(p, "baron_arald", "redmont:baron_ceremonia", "redmont:ceremonia"), Mode.SCENE));
        SHOTS.add(new Shot("04_halt", "chata_halta", 15.5, 2.7, 29.0, 12.5, 2.3, 26.5, 2500,
                p -> moveNpc(p, "halt", "chata_halta:halt", "chata_halta:linia_strzalu"), Mode.SCENE));
        SHOTS.add(new Shot("05_rozmowa", "chata_halta", 14.8, 2.6, 28.2, 12.5, 2.4, 26.5, 2500,
                p -> openDialogue(p, "halt"), Mode.DIALOGUE));
        SHOTS.add(new Shot("06_strzelnica", "chata_halta", 20.5, 2.8, 30.5, 40.5, 2.0, 30.5, 2500, p -> {}, Mode.SCENE));
        SHOTS.add(new Shot("07_farma_boba", "stajnia_boba", 27.5, 4.5, 23.5, 19.5, 2.0, 8.5, 4000, p -> {}, Mode.SCENE));
        SHOTS.add(new Shot("08_oboz_zlotu", "oboz_zlotu", 30.5, 5.0, 32.0, 29.5, 2.5, 10.0, 3000, p -> {}, Mode.SCENE));
        SHOTS.add(new Shot("09_wensley", "wensley", 24.5, 5.0, 38.0, 21.5, 3.0, 8.0, 5000, p -> {}, Mode.SCENE));
        SHOTS.add(new Shot("10_zasadzka", "spalona_osada", 21.5, 3.0, 31.0, 20.0, 2.0, 17.0, 11800, p -> {
            moveNpc(p, "halt", "spalona_osada:halt", "spalona_osada:osada");
            moveNpc(p, "gilan", "spalona_osada:gilan", "spalona_osada:osada");
            posed(p, "zwiadowcy:wargal", "spalona_osada", 17, 1, 15, 0f);
            posed(p, "zwiadowcy:wargal_oszczepnik", "spalona_osada", 22, 1, 14, 20f);
            posed(p, "zwiadowcy:wargal_zwiadowca", "spalona_osada", 20, 1, 12, -10f);
        }, Mode.SCENE));
        SHOTS.add(new Shot("11_kalkar", "ruiny_gorlanu", 27.5, 3.2, 25.0, 27.5, 2.6, 34.5, 12400,
                p -> posed(p, "zwiadowcy:kalkar", "ruiny_gorlanu", 27, 1, 34, 180f), Mode.SCENE));
        SHOTS.add(new Shot("12_dziennik", "redmont", 30.5, 3.0, 40.5, 30.5, 3.0, 28.5, 3000, p -> {}, Mode.JOURNAL));
        // tom II (plan 16.3): Celtyka, Evanlyn, obóz króla, Morgarath, wilczy okręt
        SHOTS.add(new Shot("13_tre_gwyn", "tre_gwyn", 28.5, 7.0, 53.0, 28.5, 3.0, 24.0, 5000, p -> {}, Mode.SCENE));
        SHOTS.add(new Shot("14_dolina_mgiel", "dolina_mgiel", 14.5, 8.0, 31.0, 26.0, 2.0, 15.0, 1500,
                p -> posed(p, "zwiadowcy:wargal", "dolina_mgiel", 22, 1, 17, 160f), Mode.SCENE));
        SHOTS.add(new Shot("15_oboz_krolewski", "oboz_krolewski", 26.5, 13.0, 52.0, 44.0, 2.0, 22.0, 12500, p -> clearAnimals(p, "oboz_krolewski"), Mode.SCENE));
        SHOTS.add(new Shot("16_morgarath", "rownina_uthal", 66.0, 2.4, 22.0, 70.5, 3.3, 28.5, 6000,
                PhotoTour::morgarathMounted, Mode.SCENE));
        SHOTS.add(new Shot("17_okret", "brzeg_rzeki", 24.5, 9.0, 8.0, 24.5, 5.0, 28.0, 13000, p -> clearAnimals(p, "brzeg_rzeki"), Mode.SCENE));
        // płonący most: współrzędne w układzie mostu (x wzdłuż kanionu, y nad pomostem, z na północ od środka)
        SHOTS.add(new Shot("18_plonacy_most", "most", -34, 5, 8, 0, -2, 4, 23300, PhotoTour::burningBridge, Mode.SCENE));
        SHOTS.add(new Shot("19_bitwa", "rownina_uthal", 16.0, 7.0, 43.0, 90.0, 1.5, 43.0, 4500, PhotoTour::battleLines, Mode.SCENE));
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (!ENABLED || event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (wait > 0) {
            wait--;
            return;
        }
        switch (phase) {
            case TITLE -> {
                if (!(mc.screen instanceof TitleScreen)) return;
                GameRules rules = new GameRules();
                rules.getRule(GameRules.RULE_DAYLIGHT).set(false, null);
                rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false, null);
                rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false, null);
                LevelSettings settings = new LevelSettings("zwiadowcy_zdjecia_" + System.currentTimeMillis(), GameType.CREATIVE, false,
                        Difficulty.NORMAL, true, rules, WorldDataConfiguration.DEFAULT);
                mc.createWorldOpenFlows().createFreshLevel(settings.levelName(), settings, new WorldOptions(20261017L, true, false),
                        WorldPresets::createNormalWorldDimensions);
                phase = Phase.LOADING;
            }
            case LOADING -> {
                if (mc.player == null || mc.level == null || mc.getSingleplayerServer() == null) return;
                if (mc.screen != null) mc.setScreen(null);
                server(p -> Araluen.travel(p));
                phase = Phase.ARRIVAL;
                wait = 40;
            }
            case ARRIVAL -> {
                // prolog pojawia się chwilę po przybyciu do Araluenu
                if (mc.screen instanceof StoryScreen) {
                    wait = 40;
                    phase = Phase.PROLOG;
                } else if (++step > 400) {
                    step = 0;
                    phase = Phase.SHOTS;
                }
            }
            case PROLOG -> {
                mc.options.hideGui = false;
                grab(mc, "00_prolog");
                mc.setScreen(null);
                phase = Phase.SHOTS;
                step = 0;
                wait = 40;
            }
            case SHOTS -> shots(mc);
            case DONE -> {}
        }
    }

    private static void shots(Minecraft mc) {
        if (index >= SHOTS.size()) {
            phase = Phase.DONE;
            Zwiadowcy.LOGGER.info("Sesja zdjęciowa zakończona: {} kadrów w run/screenshots", SHOTS.size() + 1);
            mc.stop();
            return;
        }
        Shot shot = SHOTS.get(index);
        switch (step) {
            case 0 -> {
                if (mc.screen != null) mc.setScreen(null);
                mc.options.hideGui = true;
                pending = server(p -> {
                    p.getAbilities().flying = true;
                    p.onUpdateAbilities();
                    LocationData.Placed placed = Locations.ensure(p.getServer(), shot.loc());
                    ServerLevel araluen = Araluen.level(p.getServer());
                    if (placed == null || araluen == null) return;
                    p.getServer().overworld().setDayTime(shot.time());
                    shot.setup().accept(p);
                    Vec3 eye = local(p, shot.loc(), shot.cx(), shot.cy(), shot.cz());
                    Vec3 look = local(p, shot.loc(), shot.lx(), shot.ly(), shot.lz());
                    double dx = look.x - eye.x, dy = look.y - eye.y, dz = look.z - eye.z;
                    float yaw = (float) (Mth.atan2(dz, dx) * (180 / Math.PI)) - 90f;
                    float pitch = (float) -(Mth.atan2(dy, Math.sqrt(dx * dx + dz * dz)) * (180 / Math.PI));
                    p.teleportTo(araluen, eye.x, eye.y - p.getEyeHeight(), eye.z, yaw, pitch);
                    target = new double[]{eye.x, eye.y - p.getEyeHeight(), eye.z, yaw, pitch};
                });
                step = 1;
            }
            case 1 -> {
                if (pending != null && !pending.isDone()) return;
                // wczytanie i zbudowanie chunków wokół kadru (krainy tomu II leżą daleko - świeży teren)
                wait = index >= TOM2_FIRST_SHOT ? TOM2_WAIT : 140;
                step = 2;
            }
            case 2 -> {
                // gracz mógł opaść podczas czekania na teren - ustaw kamerę jeszcze raz tuż przed zrzutem
                double[] t = target;
                if (t != null) server(p -> {
                    ServerLevel araluen = Araluen.level(p.getServer());
                    if (araluen == null) return;
                    p.getAbilities().flying = true;
                    p.onUpdateAbilities();
                    p.setDeltaMovement(Vec3.ZERO);
                    p.teleportTo(araluen, t[0], t[1], t[2], (float) t[3], (float) t[4]);
                });
                if (shot.mode() == Mode.JOURNAL) mc.setScreen(new JournalScreen());
                mc.options.hideGui = shot.mode() == Mode.SCENE;
                wait = 15;
                step = 3;
            }
            case 3 -> {
                grab(mc, shot.name());
                if (mc.screen != null) mc.setScreen(null);
                index++;
                step = 0;
                wait = 5;
            }
            default -> step = 0;
        }
    }

    private static void grab(Minecraft mc, String name) {
        Screenshot.grab(mc.gameDirectory, "zwiadowcy_" + name + ".png", mc.getMainRenderTarget(),
                msg -> Zwiadowcy.LOGGER.info("Zrzut: {}", msg.getString()));
    }

    private static CompletableFuture<?> server(Consumer<ServerPlayer> action) {
        IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server == null) return CompletableFuture.completedFuture(null);
        return server.submit(() -> {
            ServerPlayer p = server.getPlayerList().getPlayers().isEmpty() ? null : server.getPlayerList().getPlayers().get(0);
            if (p != null) action.accept(p);
        });
    }

    /** Punkt we współrzędnych szablonu lokacji (y=1 to poziom gruntu) → współrzędne świata. */
    private static Vec3 local(ServerPlayer p, String loc, double x, double y, double z) {
        LocationData.Placed placed = LocationData.get(p.getServer()).placed(loc);
        if (loc.equals(pl.zwiadowcy.bridge.BridgeBuilder.ID)) {
            // most: osie samego mostu (v - wzdłuż kanionu, u - na północ), punkt zero na środku pomostu
            double[] v = pl.zwiadowcy.bridge.BridgeBuilder.frame(LocationData.get(p.getServer()).extra(pl.zwiadowcy.bridge.BridgeBuilder.ID)).vectors();
            BlockPos c = placed.center;
            return new Vec3(c.getX() + 0.5 + x * v[0] + z * v[2], c.getY() + 1 + y, c.getZ() + 0.5 + x * v[1] + z * v[3]);
        }
        StoryLocation sl = StoryLocation.byId(loc);
        int g = sl != null ? sl.groundOffset : 0;
        BlockPos o = placed.origin;
        return new Vec3(o.getX() + x, o.getY() + g + y, o.getZ() + z);
    }

    private static void moveNpc(ServerPlayer p, String npc, String to, String face) {
        StoryNpc moved = NpcService.move(p.getServer(), npc, to, StoryNpc.Mode.IDLE, face, null);
        if (moved != null) moved.setNoAi(true);
    }

    /** Postać pozująca do zdjęcia: bez AI, zwrócona w zadanym kierunku. */
    private static void posed(ServerPlayer p, String type, String loc, int x, int y, int z, float yaw) {
        ServerLevel araluen = Araluen.level(p.getServer());
        EntityType<?> et = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getValue(net.minecraft.resources.ResourceLocation.parse(type));
        if (araluen == null || et == null) return;
        Entity e = et.create(araluen);
        if (e == null) return;
        Vec3 pos = local(p, loc, x + 0.5, y, z + 0.5);
        e.moveTo(pos.x, pos.y, pos.z, yaw, 0f);
        e.setYHeadRot(yaw);
        if (e instanceof Mob mob) {
            mob.setNoAi(true);
            mob.setPersistenceRequired();
            mob.yBodyRot = yaw;
        }
        araluen.addFreshEntity(e);
    }

    /** Most w ogniu: podpory polane i zapalone, skrypt rusza tak, żeby w chwili zrzutu płonął cały pomost, a przęsła jeszcze stały. */
    private static void burningBridge(ServerPlayer p) {
        var server = p.getServer();
        LocationData.Placed most = LocationData.get(server).placed(pl.zwiadowcy.bridge.BridgeBuilder.ID);
        if (most == null) return;
        // Wargale stłoczeni na pomoście
        for (String a : new String[]{"kolumna_2", "kolumna_3", "kolumna_4", "przeslo_2", "przeslo_3"}) {
            BlockPos at = most.anchors.get(a);
            if (at != null) posedAt(p, "zwiadowcy:wargal", at, 0f);
        }
        int burnAge = 140;
        pl.zwiadowcy.util.Delayed.run(TOM2_WAIT + 15 - burnAge, () -> pl.zwiadowcy.bridge.BridgeBurn.startForPhoto(server));
    }

    /** Morgarath na czarnym rumaku, oba bez AI, przodem do kamery. */
    private static void morgarathMounted(ServerPlayer p) {
        ServerLevel araluen = Araluen.level(p.getServer());
        if (araluen == null) return;
        Vec3 pos = local(p, "rownina_uthal", 70.5, 1, 28.5);
        var steed = pl.zwiadowcy.registry.ModEntities.CZARNY_RUMAK.get().create(araluen);
        var lord = pl.zwiadowcy.registry.ModEntities.MORGARATH.get().create(araluen);
        if (steed == null || lord == null) return;
        clearAnimals(p, "rownina_uthal");
        for (var m : new Mob[]{steed, lord}) {
            m.moveTo(pos.x, pos.y, pos.z, 180f, 0f);
            m.setNoAi(true);
            m.setPersistenceRequired();
            m.setYBodyRot(180f);
            m.setYHeadRot(180f);
        }
        araluen.addFreshEntity(steed);
        araluen.addFreshEntity(lord);
        lord.mountForPhoto(steed);
        // krąg żołnierzy za plecami Morgaratha
        crowdAt(p, "zolnierz", 64.5, 44.5, 180f, 12, 6, "minecraft:iron_sword", "minecraft:green_banner", true);
        crowdAt(p, "lucznik", 78.5, 44.5, 180f, 12, 6, "minecraft:bow", "", false);
    }

    /** Szyki na Równinie Uthal tuż przed starciem: Araluen (północ) naprzeciw Wargali (południe), kadr z boku. */
    private static void battleLines(ServerPlayer p) {
        clearAnimals(p, "rownina_uthal");
        double[] xs = {34, 56, 78, 100};
        for (int i = 0; i < xs.length; i++) {
            boolean archers = i % 2 == 1;
            crowdAt(p, archers ? "lucznik" : "zolnierz", xs[i], 35.5, 0f, 14, 7, archers ? "minecraft:bow" : "minecraft:iron_sword",
                    archers ? "" : "minecraft:green_banner", !archers);
            crowdAt(p, i == 2 ? "wargal_kusznik" : "wargal", xs[i], 51.5, 180f, 14, 7, i == 2 ? "minecraft:crossbow" : "minecraft:iron_axe",
                    i == 2 ? "" : "minecraft:black_banner", false);
        }
    }

    /** Grupa manekinów w szyku (współrzędne szablonu lokacji), zwrócona w kierunku {@code yaw}; rzędy cofają się od frontu. */
    private static void crowdAt(ServerPlayer p, String skin, double x, double z, float yaw, int count, int width,
                                String item, String banner, boolean cheer) {
        ServerLevel araluen = Araluen.level(p.getServer());
        if (araluen == null) return;
        var bannerItem = banner.isEmpty() ? null : net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(net.minecraft.resources.ResourceLocation.parse(banner));
        var weapon = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(net.minecraft.resources.ResourceLocation.parse(item));
        double back = yaw == 0f ? -1 : 1;
        for (int i = 0; i < count; i++) {
            int row = i / width, col = i % width;
            Vec3 pos = local(p, "rownina_uthal", x + (col - (width - 1) / 2.0) * 1.4, 1, z + back * row * 1.5);
            araluen.getChunkAt(BlockPos.containing(pos));
            int y = araluen.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mth.floor(pos.x), Mth.floor(pos.z));
            var d = pl.zwiadowcy.registry.ModEntities.TLUM.get().create(araluen);
            if (d == null) continue;
            d.moveTo(pos.x, y, pos.z, yaw, 0f);
            d.setYBodyRot(yaw);
            d.setYHeadRot(yaw);
            d.setNoAi(true);
            d.setSkin(pl.zwiadowcy.entity.CrowdDummyEntity.Skin.valueOf(skin.toUpperCase(java.util.Locale.ROOT)));
            d.setCheering(cheer);
            boolean bearer = bannerItem != null && row == 0 && col == width / 2;
            if (bearer) d.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, new net.minecraft.world.item.ItemStack(bannerItem));
            else if (weapon != null) d.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, new net.minecraft.world.item.ItemStack(weapon));
            araluen.addFreshEntity(d);
        }
    }

    /** Zwierzęta z generatora (świnie na pokładzie okrętu, krowy na polu bitwy) psułyby kadr. */
    private static void clearAnimals(ServerPlayer p, String loc) {
        // po teleporcie i wczytaniu chunków wokół kadru (zwierzęta pojawiają się razem z chunkami), przed zrzutem
        for (int delay : new int[]{60, 150, 230}) {
            pl.zwiadowcy.util.Delayed.run(delay, () -> {
                ServerLevel araluen = Araluen.level(p.getServer());
                LocationData.Placed placed = LocationData.get(p.getServer()).placed(loc);
                if (araluen == null || placed == null) return;
                for (Entity e : araluen.getEntitiesOfClass(net.minecraft.world.entity.animal.Animal.class,
                        new net.minecraft.world.phys.AABB(placed.center).inflate(96, 40, 96))) {
                    if (e instanceof pl.zwiadowcy.entity.BlackSteedEntity || (e instanceof Mob m && m.isNoAi())) continue;
                    e.discard();
                }
            });
        }
    }

    private static void posedAt(ServerPlayer p, String type, BlockPos at, float yaw) {
        ServerLevel araluen = Araluen.level(p.getServer());
        EntityType<?> et = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getValue(net.minecraft.resources.ResourceLocation.parse(type));
        if (araluen == null || et == null) return;
        Entity e = et.create(araluen);
        if (e == null) return;
        e.moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, yaw, 0f);
        if (e instanceof Mob mob) {
            mob.setNoAi(true);
            mob.setPersistenceRequired();
        }
        araluen.addFreshEntity(e);
    }

    private static void openDialogue(ServerPlayer p, String npc) {
        StoryNpc halt = NpcService.findNearest(p, npc);
        DialogueService.open(p, npc, halt);
    }

    private PhotoTour() {}
}
