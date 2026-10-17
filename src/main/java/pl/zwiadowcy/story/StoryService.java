package pl.zwiadowcy.story;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import pl.zwiadowcy.entity.NpcService;
import pl.zwiadowcy.entity.StoryNpc;
import pl.zwiadowcy.network.ModNetwork;
import pl.zwiadowcy.network.OpenStoryPacket;
import pl.zwiadowcy.network.QuestSyncPacket;
import pl.zwiadowcy.quest.QuestCapability;
import pl.zwiadowcy.quest.QuestProgress;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Wprowadzenie fabularne: opóźnione otwieranie ekranu-kroniki (po załadowaniu świata u klienta)
 * oraz scena powitania przez barona Aralda po przeczytaniu prologu.
 */
public final class StoryService {
    private record Pending(String story, int sendAt) {}

    private static final Map<UUID, Pending> PENDING = new HashMap<>();

    /** Pokaż opowieść za {@code delay} ticków (ekran ładowania świata zdąży zniknąć). */
    public static void queue(ServerPlayer p, String story, int delay) {
        PENDING.put(p.getUUID(), new Pending(story, p.tickCount + delay));
    }

    public static void tick(ServerPlayer p) {
        Pending pending = PENDING.get(p.getUUID());
        if (pending != null && p.tickCount >= pending.sendAt()) {
            PENDING.remove(p.getUUID());
            ModNetwork.sendTo(p, new OpenStoryPacket(pending.story()));
        }
    }

    public static void onFinished(ServerPlayer p, String story) {
        if (!StoryTexts.PROLOG.equals(story)) return;
        QuestProgress prog = QuestCapability.get(p);
        if (prog.hasFlag("prolog_przeczytany")) return;
        prog.setFlag("prolog_przeczytany", true);
        StoryNpc baron = NpcService.findNearest(p, "baron_arald");
        if (baron != null && baron.distanceTo(p) < 64) {
            baron.greet(p);
            p.displayClientMessage(Component.literal("Baron Arald dostrzega cię z drugiego końca dziedzińca i macha ręką.")
                    .withStyle(ChatFormatting.GOLD), true);
        } else {
            p.sendSystemMessage(Component.literal("Baron Arald czeka na ciebie na dziedzińcu zamku. Kierunek wskazuje strzałka w rogu ekranu.")
                    .withStyle(ChatFormatting.GOLD));
        }
    }

    /** Przypomnienie przy powrocie do gry: kim jesteś i co robisz. */
    public static void remindOnLogin(ServerPlayer p) {
        QuestProgress prog = QuestCapability.get(p);
        if (!prog.visitedAraluen) return;
        QuestSyncPacket sync = QuestSyncPacket.build(p);
        for (QuestSyncPacket.QuestView q : sync.quests()) {
            if (q.id().equals(sync.tracked()) && !q.completed()) {
                p.sendSystemMessage(Component.literal("✦ Wracasz do opowieści. Śledzona misja: ").withStyle(ChatFormatting.GOLD)
                        .append(Component.literal(q.title()).withStyle(ChatFormatting.YELLOW)));
                p.sendSystemMessage(Component.literal("  ▶ " + q.currentText()).withStyle(ChatFormatting.GRAY));
                return;
            }
        }
    }

    public static void forget(ServerPlayer p) {
        PENDING.remove(p.getUUID());
    }

    private StoryService() {}
}
