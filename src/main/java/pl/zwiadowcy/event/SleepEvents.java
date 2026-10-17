package pl.zwiadowcy.event;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.SleepingTimeCheckEvent;
import net.minecraftforge.event.level.SleepFinishedTimeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pl.zwiadowcy.Zwiadowcy;
import pl.zwiadowcy.quest.QuestEngine;
import pl.zwiadowcy.world.Araluen;

/**
 * Sen w Araluenie:
 * - gdy misja każe czekać do zmroku, łóżko działa także w dzień, a sen kończy się o zmroku,
 * - zaraz po zapadnięciu nocy potrzebnej misji (skradanie, polowanie Kalkarów) nie da się jej przespać,
 * - zwykły sen w nocy przewija czas do rana. Araluen dzieli czas ze światem głównym, więc vanilla w ogóle
 *   go tu nie zmieniała - ustawiamy go w świecie głównym.
 */
@Mod.EventBusSubscriber(modid = Zwiadowcy.MODID)
public final class SleepEvents {

    @SubscribeEvent
    public static void onSleepTimeCheck(SleepingTimeCheckEvent event) {
        if (event.getEntity() instanceof ServerPlayer p && Araluen.isAraluen(p.level())
                && QuestEngine.awaitsNight(p) && !QuestEngine.isNight(p)) {
            event.setResult(Event.Result.ALLOW);
        }
    }

    @SubscribeEvent
    public static void onSleepInBed(PlayerSleepInBedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer p) || !Araluen.isAraluen(p.level())) return;
        if (QuestEngine.isNight(p) && (QuestEngine.needsDarkness(p) || QuestEngine.awaitsNight(p))) {
            event.setResult(Player.BedSleepingProblem.OTHER_PROBLEM);
            p.displayClientMessage(Component.literal("Nie pora na sen - ciemność to teraz twój sprzymierzeniec.")
                    .withStyle(ChatFormatting.YELLOW), true);
        } else if (QuestEngine.awaitsNight(p) && !QuestEngine.isNight(p)) {
            p.displayClientMessage(Component.literal("Zamykasz oczy. Obudzisz się o zmroku…")
                    .withStyle(ChatFormatting.GRAY), true);
        }
    }

    @SubscribeEvent
    public static void onSleepFinished(SleepFinishedTimeEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !Araluen.isAraluen(level)) return;
        MinecraftServer server = level.getServer();
        long now = server.overworld().getDayTime();
        boolean toDusk = false, keepNight = false;
        for (ServerPlayer p : level.players()) {
            if (!p.isSleeping()) continue;
            if (QuestEngine.awaitsNight(p) && !QuestEngine.isNight(p)) toDusk = true;
            if (QuestEngine.isNight(p) && (QuestEngine.needsDarkness(p) || QuestEngine.awaitsNight(p))) keepNight = true;
        }
        if (keepNight && !toDusk) {
            // noc jest potrzebna misji - krótka drzemka, ale czas stoi w miejscu
            event.setTimeAddition(now);
            return;
        }
        long target = toDusk ? QuestEngine.nextDusk(now) : event.getNewTime();
        if (target <= now) return;
        event.setTimeAddition(target);
        server.overworld().setDayTime(target);
        if (toDusk) {
            for (ServerPlayer p : level.players()) {
                if (!p.isSleeping()) continue;
                QuestEngine.showTitle(p, Component.literal("Zapada zmrok").withStyle(ChatFormatting.GOLD),
                        Component.literal("Przespałeś dzień. Czas ruszać."));
            }
        }
    }

    private SleepEvents() {}
}
