package pl.zwiadowcy.cutscene;

import com.google.gson.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import pl.zwiadowcy.Zwiadowcy;
import pl.zwiadowcy.network.ModNetwork;
import pl.zwiadowcy.network.PlayCutscenePacket;
import pl.zwiadowcy.quest.QuestActions;
import pl.zwiadowcy.util.Delayed;
import pl.zwiadowcy.world.Locations;

import java.util.*;

/**
 * Przerywniki z kamerą (data/zwiadowcy/cutscenes/*.json). Klatki kluczowe są względne wobec lokacji
 * ("location": środek lub kotwica), a dla mostu - w układzie mostu ("frame": "most": x wzdłuż kanionu, z w poprzek).
 * Serwer wysyła klientowi gotowe współrzędne i sam wykonuje zdarzenia z osi czasu (także po pominięciu przez Esc).
 * W trakcie gracz jest nietykalny, a wrogowie w promieniu 32 bloków zamierają.
 */
public final class Cutscenes extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().create();
    public static final Cutscenes INSTANCE = new Cutscenes();

    private Map<String, JsonObject> defs = Map.of();
    private static final Map<UUID, Long> PLAYING_UNTIL = new HashMap<>();

    private Cutscenes() {
        super(GSON, "cutscenes");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager manager, ProfilerFiller profiler) {
        Map<String, JsonObject> loaded = new HashMap<>();
        files.forEach((rl, json) -> loaded.put(rl.getPath(), json.getAsJsonObject()));
        defs = loaded;
        Zwiadowcy.LOGGER.info("Wczytano {} przerywników", defs.size());
    }

    public Set<String> ids() {
        return defs.keySet();
    }

    @javax.annotation.Nullable
    public JsonObject def(String id) {
        return defs.get(id);
    }

    public static boolean playing(ServerPlayer p) {
        Long until = PLAYING_UNTIL.get(p.getUUID());
        return until != null && p.level().getGameTime() < until;
    }

    /** Akcja misji "cutscene". */
    public static void play(ServerPlayer p, String id) {
        JsonObject def = INSTANCE.defs.get(id);
        MinecraftServer server = p.getServer();
        if (def == null || server == null) {
            Zwiadowcy.LOGGER.warn("Brak przerywnika {}", id);
            return;
        }
        String location = GsonHelper.getAsString(def, "location");
        BlockPos origin = Locations.resolve(server, location);
        if (origin == null) return;
        double[] frame = frame(server, GsonHelper.getAsString(def, "frame", ""));
        List<PlayCutscenePacket.Key> keys = new ArrayList<>();
        int length = 0;
        for (JsonElement el : GsonHelper.getAsJsonArray(def, "keyframes")) {
            JsonObject k = el.getAsJsonObject();
            int t = GsonHelper.getAsInt(k, "t");
            double[] pos = point(origin, frame, GsonHelper.getAsJsonArray(k, "pos"));
            double[] look = point(origin, frame, GsonHelper.getAsJsonArray(k, "look"));
            keys.add(new PlayCutscenePacket.Key(t, pos[0], pos[1], pos[2], look[0], look[1], look[2], GsonHelper.getAsFloat(k, "fov", 70f)));
            length = Math.max(length, t);
        }
        List<PlayCutscenePacket.Subtitle> subs = new ArrayList<>();
        for (JsonElement el : GsonHelper.getAsJsonArray(def, "subtitles", new JsonArray())) {
            JsonObject s = el.getAsJsonObject();
            subs.add(new PlayCutscenePacket.Subtitle(GsonHelper.getAsInt(s, "t"), GsonHelper.getAsInt(s, "d", 60), GsonHelper.getAsString(s, "text")));
        }
        PLAYING_UNTIL.put(p.getUUID(), p.level().getGameTime() + length);
        p.setInvulnerable(true);
        ModNetwork.sendTo(p, new PlayCutscenePacket(id, keys, subs));
        // zdarzenia z osi czasu: wykonuje je serwer (także gdy gracz pominie scenę)
        for (JsonElement el : GsonHelper.getAsJsonArray(def, "events", new JsonArray())) {
            JsonObject e = el.getAsJsonObject();
            JsonArray actions = new JsonArray();
            actions.add(GsonHelper.getAsJsonObject(e, "action"));
            Delayed.run(GsonHelper.getAsInt(e, "t"), () -> {
                if (!p.isRemoved()) QuestActions.run(p, actions, new QuestActions.Ctx(null, null));
            });
        }
        Delayed.run(length + 2, () -> finish(p));
    }

    private static void finish(ServerPlayer p) {
        PLAYING_UNTIL.remove(p.getUUID());
        if (!p.isCreative()) p.setInvulnerable(false);
    }

    /** Klient pominął scenę (Esc) albo ją skończył - nic poza zapisem; serwer kończy według osi czasu. */
    public static void onClientFinished(ServerPlayer p, String id) {
        // celowo puste: zdarzenia i tak wykonają się według zegara serwera
    }

    /** Wrogowie w pobliżu oglądającego zamierają (wywoływane co 10 ticków). */
    public static void tick(ServerPlayer p) {
        if (!playing(p) || p.tickCount % 10 != 0) return;
        for (Mob m : p.level().getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(32), m -> m instanceof Enemy)) {
            m.setTarget(null);
            m.getNavigation().stop();
            m.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 15, 10, false, false));
        }
    }

    /** Układ współrzędnych sceny: zwykły (x, z) albo mostu (oś wzdłuż kanionu i w poprzek, na północ). */
    private static double[] frame(MinecraftServer server, String frame) {
        if (frame.equals("kanion")) {
            // wzdłuż kanionu w miejscu mostu (x) i w poprzek, na północ (z)
            double[] dir = pl.zwiadowcy.world.VolumeTwoLayout.bridgeDirection();
            double ang = Math.toRadians(pl.zwiadowcy.world.VolumeTwoLayout.angleDegrees());
            double c = Math.cos(ang), s = Math.sin(ang);
            double ax = dir[0] * c - dir[1] * s, az = dir[0] * s + dir[1] * c;
            return new double[]{ax, az, az, -ax};
        }
        if (frame.equals("most")) {
            // most już stoi: osie samego mostu (x wzdłuż kanionu, z na północ)
            var data = pl.zwiadowcy.world.LocationData.get(server);
            if (data.hasExtra(pl.zwiadowcy.bridge.BridgeBuilder.ID) && data.extra(pl.zwiadowcy.bridge.BridgeBuilder.ID).contains("cx")) {
                return pl.zwiadowcy.bridge.BridgeBuilder.frame(data.extra(pl.zwiadowcy.bridge.BridgeBuilder.ID)).vectors();
            }
            double[] dir = pl.zwiadowcy.world.VolumeTwoLayout.bridgeDirection();
            double ang = Math.toRadians(pl.zwiadowcy.world.VolumeTwoLayout.angleDegrees());
            double c = Math.cos(ang), s = Math.sin(ang);
            double ax = dir[0] * c - dir[1] * s, az = dir[0] * s + dir[1] * c;
            // oś „z” sceny = w poprzek kanionu, na północ (do Celtyki)
            return new double[]{ax, az, az, -ax};
        }
        if (frame.equals("uklad")) {
            // kierunki w układzie krain tomu II (obracanym razem ze światem)
            double ang = Math.toRadians(pl.zwiadowcy.world.VolumeTwoLayout.angleDegrees());
            double c = Math.cos(ang), s = Math.sin(ang);
            return new double[]{c, s, -s, c};
        }
        return new double[]{1, 0, 0, 1};
    }

    private static double[] point(BlockPos origin, double[] f, JsonArray rel) {
        double x = rel.get(0).getAsDouble(), y = rel.get(1).getAsDouble(), z = rel.get(2).getAsDouble();
        return new double[]{origin.getX() + 0.5 + x * f[0] + z * f[2], origin.getY() + y, origin.getZ() + 0.5 + x * f[1] + z * f[3]};
    }
}
