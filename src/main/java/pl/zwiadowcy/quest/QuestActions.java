package pl.zwiadowcy.quest;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.registries.ForgeRegistries;
import pl.zwiadowcy.Zwiadowcy;
import pl.zwiadowcy.entity.*;
import pl.zwiadowcy.world.Araluen;
import pl.zwiadowcy.world.Locations;

import javax.annotation.Nullable;
import java.util.Locale;

/**
 * Akcje wywoływane przez misje i dialogi. Każda akcja to obiekt z polem "type"
 * i opcjonalną listą warunków "when".
 */
public final class QuestActions {

    /** Kontekst: misja, w której wywołano akcję, i ewentualny rozmówca (NPC / Wyrwij). */
    public record Ctx(@Nullable String quest, @Nullable Entity source) {}

    public static final String TAG_QUEST = "zw_quest";
    public static final String TAG_OWNER = "zw_owner";
    public static final String TAG_DROP = "zw_drop";
    /** Przedmiot upuszczony przez moba z misji (HUD wskazuje go na etapie "zbierz"). */
    public static final String TAG_QUEST_DROP = "zw_quest_drop";
    /** "Dom" moba z misji: środek i promień, których się trzyma (np. Kalkarowie na arenie Gorlanu). */
    public static final String TAG_HOME = "zw_home", TAG_HOME_R = "zw_home_r";
    public static final String TAG_SCRIPT = "zw_script";

    public static void run(ServerPlayer p, @Nullable JsonArray actions, Ctx ctx) {
        if (actions == null) return;
        for (JsonElement el : actions) {
            JsonObject a = el.getAsJsonObject();
            if (a.has("when") && !Conditions.test(p, a.get("when"))) continue;
            try {
                runOne(p, a, ctx);
            } catch (Exception e) {
                Zwiadowcy.LOGGER.error("Błąd akcji {}: {}", a, e.toString());
            }
        }
    }

    private static void runOne(ServerPlayer p, JsonObject a, Ctx ctx) {
        MinecraftServer server = p.getServer();
        if (server == null) return;
        String type = GsonHelper.getAsString(a, "type");
        switch (type) {
            case "start_quest" -> QuestEngine.start(p, str(a, "quest"), GsonHelper.getAsBoolean(a, "quiet", false));
            case "story" -> {
                String story = str(a, "story");
                int delay = GsonHelper.getAsInt(a, "delay", 40);
                if (story.equals("epilog")) pl.zwiadowcy.story.StoryService.showEpilog(p, delay);
                else pl.zwiadowcy.story.StoryService.queue(p, story, delay);
            }
            case "cancel_quest" -> QuestEngine.cancel(p, str(a, "quest"), GsonHelper.getAsInt(a, "max_stage", 0));
            case "advance" -> {
                String quest = a.has("quest") ? str(a, "quest") : ctx.quest();
                if (quest == null) return;
                QuestProgress.State st = QuestCapability.get(p).get(quest);
                if (st == null || st.status != QuestProgress.Status.ACTIVE) return;
                if (a.has("stage") && st.stage != GsonHelper.getAsInt(a, "stage")) return;
                QuestEngine.advance(p, quest);
            }
            case "complete_quest" -> QuestEngine.complete(p, str(a, "quest"));
            case "track" -> {
                QuestCapability.get(p).setTracked(str(a, "quest"));
                QuestEngine.sync(p);
            }
            case "give_item" -> giveItem(p, a);
            case "take_item" -> QuestEngine.removeItems(p, str(a, "item"), GsonHelper.getAsInt(a, "count", 1));
            case "set_flag" -> QuestCapability.get(p).setFlag(str(a, "flag"), true);
            case "clear_flag" -> QuestCapability.get(p).setFlag(str(a, "flag"), false);
            case "move_npc" -> NpcService.move(server, str(a, "npc"), str(a, "to"), mode(a),
                    a.has("face") ? str(a, "face") : null, p);
            case "despawn_npc" -> NpcService.despawn(server, str(a, "npc"));
            case "npc_mode" -> {
                StoryNpc npc = NpcService.findNearest(p, str(a, "npc"));
                StoryNpc.Mode m = mode(a);
                if (npc != null && m != null) npc.setMode(m, p);
            }
            case "spawn" -> spawn(p, a, ctx, Locations.resolve(server, str(a, "at")));
            case "spawn_random" -> spawnRandom(p, a, ctx);
            case "message" -> {
                Component msg = Component.literal(str(a, "text")).withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY);
                if (a.has("speaker")) {
                    msg = Component.literal(str(a, "speaker") + ": ").withStyle(ChatFormatting.GOLD)
                            .append(Component.literal(str(a, "text")).withStyle(ChatFormatting.WHITE));
                }
                p.sendSystemMessage(msg);
            }
            case "actionbar" -> p.displayClientMessage(Component.literal(str(a, "text")).withStyle(ChatFormatting.YELLOW), true);
            case "title" -> QuestEngine.showTitle(p, Component.literal(str(a, "title")).withStyle(ChatFormatting.GOLD),
                    a.has("subtitle") ? Component.literal(str(a, "subtitle")).withStyle(ChatFormatting.GRAY) : null);
            case "teleport" -> {
                BlockPos pos = Locations.resolve(server, str(a, "to"));
                ServerLevel level = Araluen.level(server);
                if (pos != null && level != null) {
                    p.teleportTo(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, p.getYRot(), p.getXRot());
                }
            }
            case "note" -> {
                QuestCapability.get(p).addNote(str(a, "title"), str(a, "text"));
                p.displayClientMessage(Component.literal("Nowa notatka w dzienniku: " + str(a, "title")).withStyle(ChatFormatting.AQUA), true);
            }
            case "xp" -> p.giveExperiencePoints(GsonHelper.getAsInt(a, "amount", 10));
            case "sound" -> {
                SoundEvent ev = ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation.parse(str(a, "sound")));
                if (ev != null) p.level().playSound(null, p.blockPosition(), ev, SoundSource.PLAYERS,
                        GsonHelper.getAsFloat(a, "volume", 1f), GsonHelper.getAsFloat(a, "pitch", 1f));
            }
            case "bond_tug" -> {
                if (ctx.source() instanceof TugEntity tug) tug.bondTo(p);
            }
            case "fire_event" -> QuestEngine.fireEvent(p, str(a, "id"), 1);
            case "set_cooldown" -> QuestCapability.get(p).setCooldown(str(a, "id"), QuestEngine.gameTime(p));
            case "repair_held" -> {
                ItemStack held = p.getMainHandItem();
                if (held.isDamageableItem() && held.isDamaged()) {
                    held.setDamageValue(0);
                    p.displayClientMessage(Component.literal("Kowal oddaje ci naprawiony przedmiot.").withStyle(ChatFormatting.GREEN), true);
                } else {
                    p.displayClientMessage(Component.literal("Nie trzymasz w dłoni niczego, co wymaga naprawy.").withStyle(ChatFormatting.GRAY), true);
                }
            }
            case "heal" -> {
                p.setHealth(p.getMaxHealth());
                p.getFoodData().eat(20, 1.0f);
                p.removeAllEffects();
                p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION, 200, 0));
            }
            default -> Zwiadowcy.LOGGER.warn("Nieznany typ akcji: {}", type);
        }
    }

    private static String str(JsonObject a, String key) {
        return GsonHelper.getAsString(a, key, "");
    }

    @Nullable
    private static StoryNpc.Mode mode(JsonObject a) {
        if (!a.has("mode")) return null;
        try {
            return StoryNpc.Mode.valueOf(str(a, "mode").toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static void giveItem(ServerPlayer p, JsonObject a) {
        Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(str(a, "item")));
        if (item == null) return;
        ItemStack stack = new ItemStack(item, GsonHelper.getAsInt(a, "count", 1));
        if (a.has("name")) stack.setHoverName(Component.literal(str(a, "name")).withStyle(ChatFormatting.GOLD));
        Component name = stack.getHoverName();
        int count = stack.getCount();
        if (!p.getInventory().add(stack)) p.drop(stack, false);
        p.displayClientMessage(Component.literal("Otrzymano: ").withStyle(ChatFormatting.GREEN)
                .append(name.copy().withStyle(ChatFormatting.WHITE))
                .append(Component.literal(count > 1 ? " ×" + count : "").withStyle(ChatFormatting.WHITE)), true);
    }

    private static void spawn(ServerPlayer p, JsonObject a, Ctx ctx, @Nullable BlockPos at) {
        ServerLevel level = Araluen.level(p.getServer());
        if (level == null || at == null) return;
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.parse(str(a, "entity")));
        if (type == null) return;
        int count = GsonHelper.getAsInt(a, "count", 1);
        String quest = a.has("quest") ? str(a, "quest") : ctx.quest();
        BlockPos face = a.has("face") ? Locations.resolve(p.getServer(), str(a, "face")) : null;
        for (int i = 0; i < count; i++) {
            Entity e = type.create(level);
            if (e == null) continue;
            double ox = count > 1 ? (level.random.nextDouble() - 0.5) * 3 : 0;
            double oz = count > 1 ? (level.random.nextDouble() - 0.5) * 3 : 0;
            float yaw = face != null ? NpcService.yawTowards(at, face) : level.random.nextFloat() * 360f;
            e.moveTo(at.getX() + 0.5 + ox, at.getY(), at.getZ() + 0.5 + oz, yaw, 0f);
            e.setYHeadRot(yaw);
            if (e instanceof Mob mob) mob.setPersistenceRequired();
            if (quest != null) e.getPersistentData().putString(TAG_QUEST, quest);
            e.getPersistentData().putUUID(TAG_OWNER, p.getUUID());
            if (i == 0 && a.has("drop")) e.getPersistentData().putString(TAG_DROP, str(a, "drop"));
            if (a.has("script")) e.getPersistentData().putString(TAG_SCRIPT, str(a, "script"));
            if (i == 0 && a.has("name")) e.setCustomName(Component.literal(str(a, "name")));
            if (e instanceof BoarEntity boar && GsonHelper.getAsBoolean(a, "enraged", false)) boar.setEnraged(true);
            if (e instanceof WargalEntity w && GsonHelper.getAsBoolean(a, "sentry", false)) w.setSentry(true, yaw);
            if (a.has("home")) {
                BlockPos home = Locations.resolve(p.getServer(), str(a, "home"));
                int r = GsonHelper.getAsInt(a, "home_radius", 12);
                if (home != null) {
                    e.getPersistentData().putLong(TAG_HOME, home.asLong());
                    e.getPersistentData().putInt(TAG_HOME_R, r);
                    if (e instanceof net.minecraft.world.entity.PathfinderMob pm) pm.restrictTo(home, r);
                }
            }
            level.addFreshEntity(e);
        }
    }

    private static void spawnRandom(ServerPlayer p, JsonObject a, Ctx ctx) {
        ServerLevel level = Araluen.level(p.getServer());
        BlockPos center = Locations.resolve(p.getServer(), str(a, "center"));
        if (level == null || center == null) return;
        int min = GsonHelper.getAsInt(a, "min", 80), max = GsonHelper.getAsInt(a, "max", 160);
        double angle = level.random.nextDouble() * Math.PI * 2;
        int dist = Mth.nextInt(level.random, min, max);
        int x = center.getX() + (int) (Math.cos(angle) * dist);
        int z = center.getZ() + (int) (Math.sin(angle) * dist);
        BlockPos at = new BlockPos(x, pl.zwiadowcy.world.StructurePlacer.height(level, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), z);
        spawn(p, a, ctx, at);
        if (ctx.quest() != null) {
            QuestProgress.State st = QuestCapability.get(p).get(ctx.quest());
            if (st != null) st.targetPos = at;
        }
    }

    private QuestActions() {}
}
