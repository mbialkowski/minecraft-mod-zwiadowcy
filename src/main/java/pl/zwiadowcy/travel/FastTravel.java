package pl.zwiadowcy.travel;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import pl.zwiadowcy.entity.StoryNpc;
import pl.zwiadowcy.entity.TugEntity;
import pl.zwiadowcy.quest.*;
import pl.zwiadowcy.registry.ModItems;
import pl.zwiadowcy.util.Delayed;
import pl.zwiadowcy.world.Araluen;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Szybka podróż między odkrytymi ogniskami zwiadowców (z Mapy Gilana). Kosztuje jedną rację podróżną, przesuwa czas
 * o godzinę na każde 500 bloków i zabiera ze sobą Wyrwija oraz towarzyszy. Zablokowana w walce, przy alarmie
 * i w etapach misji z flagą "no_fast_travel".
 */
public final class FastTravel {
    private static final int COMBAT_TICKS = 160;

    /** Powód blokady albo null, gdy można podróżować. */
    @Nullable
    public static String locked(ServerPlayer p) {
        if (!Araluen.isAraluen(p.level())) return "Szybka podróż działa tylko w Araluenie.";
        if (p.tickCount - p.getLastHurtByMobTimestamp() < COMBAT_TICKS && p.getLastHurtByMobTimestamp() > 0) {
            return "Nie w trakcie walki.";
        }
        if (pl.zwiadowcy.stealth.Alertness.alarmNear(p)) return "Wrogowie w pobliżu podnieśli alarm.";
        QuestProgress prog = QuestCapability.get(p);
        for (Map.Entry<String, QuestProgress.State> e : prog.states().entrySet()) {
            if (e.getValue().status != QuestProgress.Status.ACTIVE) continue;
            QuestDefinition def = QuestManager.INSTANCE.get(e.getKey());
            QuestDefinition.Stage stage = def != null ? def.stage(e.getValue().stage) : null;
            if (stage != null && stage.noFastTravel()) return "Teraz nie ma czasu na skróty - misja „" + def.title() + "” trwa.";
        }
        return null;
    }

    public static void travel(ServerPlayer p, String campfireId) {
        MinecraftServer server = p.getServer();
        Campfires.Def def = Campfires.byId(campfireId);
        if (server == null || def == null || !QuestCapability.get(p).knowsCampfire(campfireId)) return;
        String lock = locked(p);
        if (lock != null) {
            p.displayClientMessage(Component.literal(lock).withStyle(ChatFormatting.RED), true);
            return;
        }
        BlockPos dest = Campfires.pos(server, def);
        ServerLevel level = Araluen.level(server);
        if (dest == null || level == null) return;
        double dist = Math.sqrt(p.blockPosition().distSqr(dest));
        if (dist < 32) {
            p.displayClientMessage(Component.literal("Jesteś już przy tym ognisku.").withStyle(ChatFormatting.GRAY), true);
            return;
        }
        if (!p.getAbilities().instabuild) {
            if (QuestEngine.countItems(p, "zwiadowcy:racje_podrozne") < 1) {
                p.displayClientMessage(Component.literal("Na drogę potrzebujesz racji podróżnej.").withStyle(ChatFormatting.RED), true);
                return;
            }
            QuestEngine.removeItems(p, "zwiadowcy:racje_podrozne", 1);
        }
        QuestEngine.showTitle(p, Component.literal("W drodze…").withStyle(ChatFormatting.GOLD),
                Component.literal(def.name()).withStyle(ChatFormatting.GRAY));
        p.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0, false, false));
        p.level().playSound(null, p.blockPosition(), SoundEvents.HORSE_GALLOP, SoundSource.PLAYERS, 1f, 1f);
        Delayed.run(40, () -> arrive(p, level, dest, dist));
    }

    private static void arrive(ServerPlayer p, ServerLevel level, BlockPos dest, double dist) {
        if (p.isRemoved()) return;
        Entity vehicle = p.getVehicle();
        if (vehicle != null) p.stopRiding();
        double x = dest.getX() + 0.5, y = dest.getY(), z = dest.getZ() + 0.5;
        p.teleportTo(level, x, y, z, p.getYRot(), p.getXRot());
        QuestProgress prog = QuestCapability.get(p);
        if (prog.tugUuid != null && level.getEntity(prog.tugUuid) instanceof TugEntity tug && tug.isAlive()) {
            tug.teleportTo(x + 2, y, z + 1);
            tug.getNavigation().stop();
        }
        for (StoryNpc npc : level.getEntitiesOfClass(StoryNpc.class, p.getBoundingBox().inflate(4096),
                n -> n.getMode() == StoryNpc.Mode.COMPANION && n.ownerPlayer() == p)) {
            npc.teleportTo(x - 2 + level.random.nextInt(3), y, z - 2 + level.random.nextInt(3));
            npc.getNavigation().stop();
        }
        long hours = Math.max(1, Math.round(dist / 500.0));
        level.setDayTime(level.getDayTime() + hours * 1000);
        p.displayClientMessage(Component.literal("Droga zajęła ok. " + hours + (hours == 1 ? " godzinę." : hours < 5 ? " godziny." : " godzin."))
                .withStyle(ChatFormatting.GRAY), true);
        QuestEngine.sync(p);
    }

    private FastTravel() {}
}
