package pl.zwiadowcy.tracking;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import pl.zwiadowcy.quest.*;
import pl.zwiadowcy.registry.ModItems;
import pl.zwiadowcy.skill.Skills;
import pl.zwiadowcy.world.Locations;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Oczy tropiciela (tom II): przytrzymany klawisz z zestawem tropiciela w plecaku podświetla wskazówki śledztwa,
 * ślady stóp i wózków (trop misji) oraz świeże, czerwone ślady wrogów. Gracz porusza się wtedy wolniej.
 * PPM na wskazówce zapisuje ją w dzienniku (zakładka „Wnioski”).
 */
public final class TrackerSense {
    private static final Set<UUID> ACTIVE = new HashSet<>();
    private static final Map<UUID, Deque<long[]>> ENEMY_TRACKS = new HashMap<>();
    private static final DustParticleOptions FOOTPRINT = new DustParticleOptions(new Vector3f(0.45f, 0.32f, 0.16f), 1.0f);
    private static final DustParticleOptions FRESH = new DustParticleOptions(new Vector3f(0.8f, 0.12f, 0.1f), 1.1f);
    private static final int FRESH_TICKS = 1200;

    public static void set(ServerPlayer p, boolean on) {
        if (on) {
            if (QuestEngine.countItems(p, "zwiadowcy:zestaw_tropiciela") < 1) {
                p.displayClientMessage(Component.literal("Oczy tropiciela wymagają zestawu tropiciela w plecaku.").withStyle(ChatFormatting.GRAY), true);
                return;
            }
            if (ACTIVE.add(p.getUUID())) {
                p.level().playSound(null, p.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 0.5f, 1.6f);
                QuestEngine.fireEvent(p, "tropiciel_aktywny", 1);
            }
        } else {
            ACTIVE.remove(p.getUUID());
        }
    }

    public static boolean active(ServerPlayer p) {
        return ACTIVE.contains(p.getUUID());
    }

    public static int range(ServerPlayer p) {
        return Skills.has(p, Skills.OKO_TROPICIELA) ? 24 : 16;
    }

    public static void tick(ServerPlayer p) {
        if (p.tickCount % 40 == 0) rememberEnemies(p);
        if (!ACTIVE.contains(p.getUUID())) return;
        if (!(p.level() instanceof ServerLevel level)) return;
        // wolniej, jak przy skradaniu
        if (p.tickCount % 20 == 0) p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 1, false, false, false));
        if (p.tickCount % 10 != 0) return;
        int range = range(p);
        for (Clue c : activeClues(p)) {
            if (c.pos.distSqr(p.blockPosition()) > range * range) continue;
            boolean found = Clues.found(QuestCapability.get(p), c.id);
            level.sendParticles(p, found ? ParticleTypes.HAPPY_VILLAGER : ParticleTypes.END_ROD, true,
                    c.pos.getX() + 0.5, c.pos.getY() + 0.6, c.pos.getZ() + 0.5, found ? 1 : 3, 0.25, 0.3, 0.25, 0.01);
        }
        List<BlockPos> trail = activeTrail(p);
        for (int i = 0; i + 1 < trail.size(); i++) {
            Vec3 a = Vec3.atBottomCenterOf(trail.get(i)), b = Vec3.atBottomCenterOf(trail.get(i + 1));
            double len = a.distanceTo(b);
            for (double d = 0; d < len; d += 1.5) {
                Vec3 pt = a.add(b.subtract(a).scale(d / len));
                if (pt.distanceToSqr(p.position()) > range * range) continue;
                level.sendParticles(p, FOOTPRINT, true, pt.x, pt.y + 0.1, pt.z, 1, 0.15, 0.02, 0.15, 0);
            }
        }
        Deque<long[]> tracks = ENEMY_TRACKS.get(p.getUUID());
        if (tracks != null) {
            long now = level.getGameTime();
            for (long[] t : tracks) {
                if (now - t[1] > FRESH_TICKS) continue;
                BlockPos pos = BlockPos.of(t[0]);
                if (pos.distSqr(p.blockPosition()) > range * range) continue;
                level.sendParticles(p, FRESH, true, pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5, 1, 0.2, 0.02, 0.2, 0);
            }
        }
    }

    /** Co 2 s zapamiętujemy, gdzie stoją wrogowie w pobliżu - to ich „świeże ślady” (znikają po minucie). */
    private static void rememberEnemies(ServerPlayer p) {
        Deque<long[]> tracks = ENEMY_TRACKS.computeIfAbsent(p.getUUID(), u -> new ArrayDeque<>());
        long now = p.level().getGameTime();
        while (!tracks.isEmpty() && now - tracks.peekFirst()[1] > FRESH_TICKS) tracks.pollFirst();
        for (Mob m : p.level().getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(32), e -> e instanceof Enemy && e.isAlive())) {
            if (tracks.size() > 400) tracks.pollFirst();
            tracks.addLast(new long[]{m.blockPosition().asLong(), now});
        }
    }

    // ------------------------------------------------------------------ wskazówki i trop z bieżących etapów misji

    public record Clue(String id, BlockPos pos, String title, String text, String quest) {}

    public static List<Clue> activeClues(ServerPlayer p) {
        List<Clue> out = new ArrayList<>();
        if (p.getServer() == null) return out;
        QuestProgress prog = QuestCapability.get(p);
        for (var e : prog.states().entrySet()) {
            if (e.getValue().status != QuestProgress.Status.ACTIVE) continue;
            QuestDefinition def = QuestManager.INSTANCE.get(e.getKey());
            QuestDefinition.Stage stage = def != null ? def.stage(e.getValue().stage) : null;
            if (stage == null || !stage.objective().type().equals("clues")) continue;
            for (JsonElement el : GsonHelper.getAsJsonArray(stage.objective().params(), "clues")) {
                JsonObject c = el.getAsJsonObject();
                String at = GsonHelper.getAsString(c, "at");
                BlockPos pos = Locations.peek(p.getServer(), at);
                if (pos != null) out.add(new Clue(at, pos, GsonHelper.getAsString(c, "title", "Wskazówka"),
                        GsonHelper.getAsString(c, "text", ""), def.id()));
            }
        }
        return out;
    }

    private static List<BlockPos> activeTrail(ServerPlayer p) {
        List<BlockPos> out = new ArrayList<>();
        if (p.getServer() == null) return out;
        QuestProgress prog = QuestCapability.get(p);
        for (var e : prog.states().entrySet()) {
            if (e.getValue().status != QuestProgress.Status.ACTIVE) continue;
            QuestDefinition def = QuestManager.INSTANCE.get(e.getKey());
            QuestDefinition.Stage stage = def != null ? def.stage(e.getValue().stage) : null;
            if (stage == null || !stage.objective().type().equals("follow_trail")) continue;
            for (String a : stage.objective().list("anchors")) {
                BlockPos pos = Locations.peek(p.getServer(), a);
                if (pos != null) out.add(pos);
            }
        }
        return out;
    }

    /** PPM na bloku: czy to wskazówka z bieżącego śledztwa (do 2 bloków od kotwicy). */
    @Nullable
    public static Clue clueAt(ServerPlayer p, BlockPos clicked) {
        for (Clue c : activeClues(p)) {
            if (c.pos.distSqr(clicked) <= 2.5 * 2.5 || c.pos.above().distSqr(clicked) <= 2.5 * 2.5) return c;
        }
        return null;
    }

    public static void onClueFound(ServerPlayer p, Clue c) {
        QuestProgress prog = QuestCapability.get(p);
        if (Clues.found(prog, c.id)) return;
        prog.setFlag(Clues.PREFIX + c.id, true);
        prog.addNote("Wskazówka: " + c.title, c.text);
        p.level().playSound(null, p.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 0.8f, 1.2f);
        p.displayClientMessage(Component.literal("Wskazówka: " + c.title + " (zapisana w dzienniku)").withStyle(ChatFormatting.AQUA), true);
        QuestEngine.addProgress(p, c.quest, 1);
        QuestEngine.fireEvent(p, "wskazowka:" + c.id, 1);
        QuestEngine.sync(p);
    }

    public static boolean hasKit(ServerPlayer p) {
        return p.getInventory().contains(new net.minecraft.world.item.ItemStack(ModItems.ZESTAW_TROPICIELA.get()));
    }

    public static void forget(ServerPlayer p) {
        ACTIVE.remove(p.getUUID());
        ENEMY_TRACKS.remove(p.getUUID());
    }

    private TrackerSense() {}
}
