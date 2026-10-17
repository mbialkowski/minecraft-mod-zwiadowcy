package pl.zwiadowcy.quest;

import net.minecraft.ChatFormatting;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import pl.zwiadowcy.Zwiadowcy;
import pl.zwiadowcy.entity.StoryNpc;
import pl.zwiadowcy.entity.TugEntity;
import pl.zwiadowcy.network.ModNetwork;
import pl.zwiadowcy.network.QuestSyncPacket;
import pl.zwiadowcy.stealth.Stealth;
import pl.zwiadowcy.world.Araluen;
import pl.zwiadowcy.world.Locations;

import javax.annotation.Nullable;
import java.util.*;

/** Logika postępu misji po stronie serwera. */
public final class QuestEngine {

    // ------------------------------------------------------------------ cykl życia misji

    public static void start(ServerPlayer p, String id) {
        QuestDefinition def = QuestManager.INSTANCE.get(id);
        if (def == null) {
            Zwiadowcy.LOGGER.warn("Próba rozpoczęcia nieznanej misji: {}", id);
            return;
        }
        QuestProgress prog = QuestCapability.get(p);
        QuestProgress.State old = prog.get(id);
        if (old != null && old.status == QuestProgress.Status.ACTIVE) return;
        if (old != null && old.status == QuestProgress.Status.COMPLETED && !def.repeatable()) return;
        QuestProgress.State st = new QuestProgress.State();
        if (old != null) {
            st.timesCompleted = old.timesCompleted;
            st.completedAt = old.completedAt;
        }
        prog.states().put(id, st);
        prog.setTracked(id);
        showTitle(p, Component.literal(def.title()).withStyle(ChatFormatting.GOLD),
                Component.literal(def.repeatable() ? "Nowe zlecenie" : "Nowa misja").withStyle(ChatFormatting.GRAY));
        p.sendSystemMessage(Component.literal("✦ Nowa misja: ").withStyle(ChatFormatting.GOLD)
                .append(Component.literal(def.title()).withStyle(ChatFormatting.YELLOW)));
        p.level().playSound(null, p.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1f, 1f);
        QuestActions.run(p, def.onStart(), new QuestActions.Ctx(id, null));
        enterStage(p, def, st);
        sync(p);
    }

    private static void enterStage(ServerPlayer p, QuestDefinition def, QuestProgress.State st) {
        QuestDefinition.Stage stage = def.stage(st.stage);
        if (stage == null) return;
        st.progress = 0;
        st.targetPos = null;
        st.firedTriggers.clear();
        String loc = locationOf(stage);
        if (loc != null && p.getServer() != null) Locations.ensure(p.getServer(), loc.split(":", 2)[0]);
        QuestActions.run(p, stage.onStart(), new QuestActions.Ctx(def.id(), null));
        if (!stage.text().isEmpty()) {
            p.sendSystemMessage(Component.literal("  ▶ " + stage.text()).withStyle(ChatFormatting.GRAY));
        }
        if (stage.objective().type().equals("night") && !isNight(p)) {
            long ticks = nextDusk(p.level().getDayTime()) - p.level().getDayTime();
            long min = Math.max(1, (ticks + 1199) / 1200);
            p.sendSystemMessage(Component.literal("  Do zmroku zostało ok. " + min + " min. Nie chcesz czekać? Połóż się "
                    + "do dowolnego łóżka - prześpisz dzień i obudzisz się o zmroku.").withStyle(ChatFormatting.DARK_AQUA));
        }
    }

    /** Kończy bieżący etap misji i przechodzi do następnego. */
    public static void advance(ServerPlayer p, String id) {
        QuestDefinition def = QuestManager.INSTANCE.get(id);
        QuestProgress prog = QuestCapability.get(p);
        QuestProgress.State st = prog.get(id);
        if (def == null || st == null || st.status != QuestProgress.Status.ACTIVE) return;
        int stageIndex = st.stage;
        QuestDefinition.Stage stage = def.stage(stageIndex);
        if (stage != null) QuestActions.run(p, stage.onComplete(), new QuestActions.Ctx(id, null));
        if (st.status != QuestProgress.Status.ACTIVE || st.stage != stageIndex) {
            sync(p);
            return;
        }
        st.stage++;
        if (st.stage >= def.stages().size()) {
            complete(p, def, st);
        } else {
            p.level().playSound(null, p.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.6f, 1.2f);
            enterStage(p, def, st);
        }
        sync(p);
    }

    public static void complete(ServerPlayer p, String id) {
        QuestDefinition def = QuestManager.INSTANCE.get(id);
        QuestProgress.State st = QuestCapability.get(p).get(id);
        if (def != null && st != null && st.status == QuestProgress.Status.ACTIVE) {
            complete(p, def, st);
            sync(p);
        }
    }

    private static void complete(ServerPlayer p, QuestDefinition def, QuestProgress.State st) {
        QuestProgress prog = QuestCapability.get(p);
        st.status = QuestProgress.Status.COMPLETED;
        st.completedAt = gameTime(p);
        st.timesCompleted++;
        st.targetPos = null;
        if (def.advancement() != null) awardAdvancement(p, def.advancement());
        showTitle(p, Component.literal(def.title()).withStyle(ChatFormatting.GOLD),
                Component.literal(def.repeatable() ? "Zlecenie wykonane" : "Misja ukończona").withStyle(ChatFormatting.GREEN));
        p.sendSystemMessage(Component.literal("✔ Ukończono: ").withStyle(ChatFormatting.GREEN)
                .append(Component.literal(def.title()).withStyle(ChatFormatting.YELLOW)));
        p.level().playSound(null, p.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 0.7f, 1f);
        if (def.id().equals(prog.tracked())) prog.setTracked(nextActive(prog, def.id()));
        QuestActions.run(p, def.onComplete(), new QuestActions.Ctx(def.id(), null));
    }

    private static String nextActive(QuestProgress prog, String except) {
        String best = "";
        int bestOrder = Integer.MAX_VALUE;
        for (Map.Entry<String, QuestProgress.State> e : prog.states().entrySet()) {
            if (e.getKey().equals(except) || e.getValue().status != QuestProgress.Status.ACTIVE) continue;
            QuestDefinition d = QuestManager.INSTANCE.get(e.getKey());
            if (d != null && d.order() < bestOrder) {
                bestOrder = d.order();
                best = d.id();
            }
        }
        return best;
    }

    public static void addProgress(ServerPlayer p, String id, int amount) {
        QuestDefinition def = QuestManager.INSTANCE.get(id);
        QuestProgress.State st = QuestCapability.get(p).get(id);
        if (def == null || st == null || st.status != QuestProgress.Status.ACTIVE) return;
        QuestDefinition.Stage stage = def.stage(st.stage);
        if (stage == null) return;
        int required = stage.objective().count();
        st.progress = Math.min(required, st.progress + amount);
        if (st.progress >= required) {
            advance(p, id);
        } else {
            p.displayClientMessage(Component.literal(stage.text() + " (" + st.progress + "/" + required + ")")
                    .withStyle(ChatFormatting.YELLOW), true);
            sync(p);
        }
    }

    // ------------------------------------------------------------------ zdarzenia ze świata

    /** Sprawdzane co 10 ticków: dotarcie na miejsce, zebrane przedmioty, pora nocna. */
    public static void tick(ServerPlayer p) {
        QuestProgress prog = QuestCapability.get(p);
        boolean dirty = false;
        for (Map.Entry<String, QuestProgress.State> e : new ArrayList<>(prog.states().entrySet())) {
            QuestProgress.State st = e.getValue();
            if (st.status != QuestProgress.Status.ACTIVE) continue;
            QuestDefinition def = QuestManager.INSTANCE.get(e.getKey());
            if (def == null) continue;
            if (st.stage >= def.stages().size() && !def.stages().isEmpty()) {
                // misja w zapisie miała więcej etapów niż obecna wersja - wracamy na ostatni istniejący etap
                st.stage = def.stages().size() - 1;
                dirty = true;
            }
            QuestDefinition.Stage stage = def.stage(st.stage);
            if (stage == null) continue;
            QuestDefinition.Objective o = stage.objective();
            switch (o.type()) {
                case "reach", "sneak_reach" -> {
                    if (!Araluen.isAraluen(p.level()) || p.getServer() == null) break;
                    BlockPos pos = Locations.peek(p.getServer(), locationOf(stage));
                    double r = o.dbl("radius", 4.0);
                    if (pos != null && p.position().distanceToSqr(Vec3.atBottomCenterOf(pos)) <= r * r) {
                        String riding = o.str("riding");
                        if (riding.equals("wyrwij") && !(p.getVehicle() instanceof TugEntity)) {
                            p.displayClientMessage(Component.literal("Masz tu przyjechać na grzbiecie Wyrwija!").withStyle(ChatFormatting.YELLOW), true);
                            break;
                        }
                        advance(p, def.id());
                    }
                }
                case "collect" -> {
                    int have = countItems(p, o.str("item"));
                    int required = o.count();
                    int shown = Math.min(have, required);
                    if (shown != st.progress) {
                        st.progress = shown;
                        dirty = true;
                    }
                    if (have >= required) {
                        if (o.bool("consume")) removeItems(p, o.str("item"), required);
                        advance(p, def.id());
                    }
                }
                case "night" -> {
                    if (Araluen.isAraluen(p.level()) && isNight(p)) advance(p, def.id());
                }
                default -> {}
            }
        }
        if (dirty) sync(p);
    }

    /** Sprawdzane co 4 ticki: czy obserwatorzy widzą skradającego się gracza. */
    public static void fastTick(ServerPlayer p) {
        QuestProgress prog = QuestCapability.get(p);
        for (Map.Entry<String, QuestProgress.State> e : prog.states().entrySet()) {
            QuestProgress.State st = e.getValue();
            if (st.status != QuestProgress.Status.ACTIVE) continue;
            QuestDefinition def = QuestManager.INSTANCE.get(e.getKey());
            if (def == null) continue;
            QuestDefinition.Stage stage = def.stage(st.stage);
            if (stage == null || !stage.objective().type().equals("sneak_reach")) continue;
            QuestDefinition.Objective o = stage.objective();
            List<String> observers = o.list("observers");
            double range = o.dbl("detect_range", 14.0) * Stealth.detectionFactor(p);
            List<LivingEntity> candidates = p.level().getEntitiesOfClass(LivingEntity.class,
                    p.getBoundingBox().inflate(o.dbl("detect_range", 14.0) + 2), ent -> isObserver(ent, observers));
            for (LivingEntity obs : candidates) {
                if (Stealth.canSee(obs, p, range)) {
                    onDetected(p, def, stage, obs);
                    return;
                }
            }
        }
    }

    private static boolean isObserver(LivingEntity e, List<String> observers) {
        if (!e.isAlive()) return false;
        if (e instanceof StoryNpc npc) return observers.contains(npc.npcId());
        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(e.getType());
        return key != null && observers.contains(key.toString());
    }

    private static void onDetected(ServerPlayer p, QuestDefinition def, QuestDefinition.Stage stage, LivingEntity observer) {
        long now = gameTime(p);
        if (now - p.getPersistentData().getLong("zw_detect") < 60) return;
        p.getPersistentData().putLong("zw_detect", now);
        p.sendSystemMessage(Component.literal(observer.getName().getString() + " cię zauważył! ").withStyle(ChatFormatting.RED)
                .append(Component.literal("Wycofaj się i spróbuj ponownie.").withStyle(ChatFormatting.GRAY)));
        showTitle(p, Component.literal("Zauważony!").withStyle(ChatFormatting.RED),
                Component.literal("Zwiadowca musi być cierpliwy").withStyle(ChatFormatting.GRAY));
        p.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0, false, false));
        String checkpoint = stage.objective().str("checkpoint");
        if (!checkpoint.isEmpty() && p.getServer() != null) {
            BlockPos cp = Locations.resolve(p.getServer(), checkpoint);
            if (cp != null && Araluen.isAraluen(p.level())) {
                p.teleportTo(cp.getX() + 0.5, cp.getY(), cp.getZ() + 0.5);
            }
        }
        observer.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, p.getEyePosition());
    }

    public static void onKill(ServerPlayer p, LivingEntity victim, @Nullable String questTag) {
        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(victim.getType());
        if (key == null) return;
        QuestProgress prog = QuestCapability.get(p);
        for (Map.Entry<String, QuestProgress.State> e : new ArrayList<>(prog.states().entrySet())) {
            if (e.getValue().status != QuestProgress.Status.ACTIVE) continue;
            QuestDefinition def = QuestManager.INSTANCE.get(e.getKey());
            if (def == null) continue;
            QuestDefinition.Stage stage = def.stage(e.getValue().stage);
            if (stage == null || !stage.objective().type().equals("kill")) continue;
            QuestDefinition.Objective o = stage.objective();
            if (!o.str("entity").equals(key.toString())) continue;
            if (o.bool("tagged") && !def.id().equals(questTag)) continue;
            addProgress(p, def.id(), 1);
        }
    }

    public static void onTargetHit(ServerPlayer p, String kind, double distance) {
        QuestProgress prog = QuestCapability.get(p);
        for (Map.Entry<String, QuestProgress.State> e : new ArrayList<>(prog.states().entrySet())) {
            if (e.getValue().status != QuestProgress.Status.ACTIVE) continue;
            QuestDefinition def = QuestManager.INSTANCE.get(e.getKey());
            if (def == null) continue;
            QuestDefinition.Stage stage = def.stage(e.getValue().stage);
            if (stage == null || !stage.objective().type().equals("target_hits")) continue;
            QuestDefinition.Objective o = stage.objective();
            String projectile = o.str("projectile").isEmpty() ? "arrow" : o.str("projectile");
            if (!projectile.equals(kind)) continue;
            double min = o.dbl("min_distance", 0);
            if (distance + 0.5 < min) {
                p.displayClientMessage(Component.literal(String.format("Za blisko! Odsuń się na co najmniej %.0f kratek (teraz %.0f).", min, distance))
                        .withStyle(ChatFormatting.RED), true);
                continue;
            }
            addProgress(p, def.id(), 1);
        }
    }

    /** Zdarzenie nazwane: liczniki typu "event" oraz wyzwalacze etapu ("triggers"). */
    public static void fireEvent(ServerPlayer p, String eventId, int amount) {
        QuestProgress prog = QuestCapability.get(p);
        for (Map.Entry<String, QuestProgress.State> e : new ArrayList<>(prog.states().entrySet())) {
            QuestProgress.State st = e.getValue();
            if (st.status != QuestProgress.Status.ACTIVE) continue;
            QuestDefinition def = QuestManager.INSTANCE.get(e.getKey());
            if (def == null) continue;
            QuestDefinition.Stage stage = def.stage(st.stage);
            if (stage == null) continue;
            var trigger = stage.triggers().get(eventId);
            if (trigger != null && st.firedTriggers.add(eventId)) {
                QuestActions.run(p, trigger, new QuestActions.Ctx(def.id(), null));
            }
            QuestDefinition.Objective o = stage.objective();
            if (o.type().equals("event") && o.str("id").equals(eventId)) addProgress(p, def.id(), amount);
        }
    }

    // ------------------------------------------------------------------ narzędzia

    @Nullable
    public static String locationOf(QuestDefinition.Stage stage) {
        String fromObjective = stage.objective().str("location");
        return !fromObjective.isEmpty() ? fromObjective : stage.location();
    }

    public static boolean isNight(ServerPlayer p) {
        long t = p.level().getDayTime() % 24000L;
        return t >= 13000 && t <= 23000;
    }

    /** Najbliższy zmrok (13000. tick doby), od którego cel "night" jest spełniony. */
    public static long nextDusk(long dayTime) {
        long t = Math.floorMod(dayTime, 24000L);
        long dayStart = dayTime - t;
        return t < 13000 ? dayStart + 13000 : dayStart + 24000 + 13000;
    }

    /** Czy bieżący etap którejś aktywnej misji to czekanie na zmrok. */
    public static boolean awaitsNight(ServerPlayer p) {
        return currentStageMatches(p, 0, "night");
    }

    /** Czy bieżący etap następuje zaraz po czekaniu na zmrok - wtedy noc jest potrzebna i nie wolno jej przespać. */
    public static boolean needsDarkness(ServerPlayer p) {
        return currentStageMatches(p, -1, "night");
    }

    private static boolean currentStageMatches(ServerPlayer p, int offset, String type) {
        for (Map.Entry<String, QuestProgress.State> e : QuestCapability.get(p).states().entrySet()) {
            QuestProgress.State st = e.getValue();
            if (st.status != QuestProgress.Status.ACTIVE) continue;
            QuestDefinition def = QuestManager.INSTANCE.get(e.getKey());
            QuestDefinition.Stage stage = def != null && st.stage + offset >= 0 ? def.stage(st.stage + offset) : null;
            if (stage != null && stage.objective().type().equals(type)) return true;
        }
        return false;
    }

    public static long gameTime(ServerPlayer p) {
        MinecraftServer s = p.getServer();
        return s != null ? s.overworld().getGameTime() : p.level().getGameTime();
    }

    public static int countItems(ServerPlayer p, String spec) {
        int n = 0;
        for (ItemStack s : p.getInventory().items) if (matches(s, spec)) n += s.getCount();
        for (ItemStack s : p.getInventory().offhand) if (matches(s, spec)) n += s.getCount();
        return n;
    }

    public static void removeItems(ServerPlayer p, String spec, int count) {
        for (ItemStack s : p.getInventory().items) {
            if (count <= 0) break;
            if (matches(s, spec)) {
                int take = Math.min(count, s.getCount());
                s.shrink(take);
                count -= take;
            }
        }
    }

    public static boolean matches(ItemStack stack, String spec) {
        if (stack.isEmpty() || spec.isEmpty()) return false;
        if (spec.startsWith("#")) {
            TagKey<Item> tag = TagKey.create(net.minecraft.core.registries.Registries.ITEM, ResourceLocation.parse(spec.substring(1)));
            return stack.is(tag);
        }
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return key != null && key.toString().equals(spec);
    }

    public static void awardAdvancement(ServerPlayer p, String path) {
        if (p.getServer() == null) return;
        Advancement adv = p.getServer().getAdvancements().getAdvancement(Zwiadowcy.id(path));
        if (adv == null) return;
        AdvancementProgress progress = p.getAdvancements().getOrStartProgress(adv);
        for (String criterion : progress.getRemainingCriteria()) p.getAdvancements().award(adv, criterion);
    }

    public static void showTitle(ServerPlayer p, Component title, @Nullable Component subtitle) {
        p.connection.send(new ClientboundSetTitlesAnimationPacket(10, 50, 20));
        if (subtitle != null) p.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
        p.connection.send(new ClientboundSetTitleTextPacket(title));
    }

    public static void sync(ServerPlayer p) {
        ModNetwork.sendTo(p, QuestSyncPacket.build(p));
    }

    private QuestEngine() {}
}
