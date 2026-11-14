package pl.zwiadowcy.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import pl.zwiadowcy.item.QuiverItem;
import pl.zwiadowcy.quest.QuestCapability;
import pl.zwiadowcy.quest.QuestEngine;
import pl.zwiadowcy.stealth.Stealth;

import java.util.function.Supplier;

/** Klient → serwer: akcje z klawiszy (Bezruch, zmiana strzały, śledzenie misji). */
public record ActionPacket(Kind kind, String arg) {

    public enum Kind {
        BEZRUCH, NASTEPNA_STRZALA, SLEDZ_MISJE, KONIEC_OPOWIESCI, POKAZ_EPILOG,
        // tom II
        KONTYNUUJ_TOM2, SZYBKI_START, POKAZ_KONIEC_TOMU, WOLNA_GRA, POKAZ_EPILOG_TOM2, TRUDNOSC,
        TROPICIEL, WNIOSEK, SZYBKA_PODROZ, ROZKAZ, ZRYW, KONIEC_PRZERYWNIKA
    }

    public static void encode(ActionPacket msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.kind);
        buf.writeUtf(msg.arg, 128);
    }

    public static ActionPacket decode(FriendlyByteBuf buf) {
        return new ActionPacket(buf.readEnum(Kind.class), buf.readUtf(128));
    }

    public static void handle(ActionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        if (player == null) return;
        switch (msg.kind) {
            case BEZRUCH -> Stealth.tryActivateBezruch(player);
            case NASTEPNA_STRZALA -> QuiverItem.cycleSelected(player);
            case SLEDZ_MISJE -> {
                if (QuestCapability.get(player).isActive(msg.arg)) {
                    QuestCapability.get(player).setTracked(msg.arg);
                    QuestEngine.sync(player);
                }
            }
            case KONIEC_OPOWIESCI -> pl.zwiadowcy.story.StoryService.onFinished(player, msg.arg);
            case KONTYNUUJ_TOM2 -> pl.zwiadowcy.volume.Volumes.continueToVolume2(player);
            case SZYBKI_START -> pl.zwiadowcy.volume.Volumes.quickStart(player);
            case POKAZ_KONIEC_TOMU -> pl.zwiadowcy.volume.Volumes.showEndScreen(player, msg.arg.equals("2") ? 2 : 1);
            case WOLNA_GRA -> pl.zwiadowcy.volume.Volumes.startFreePlay(player);
            case POKAZ_EPILOG_TOM2 -> {
                if (pl.zwiadowcy.volume.Volumes.tom2Done(QuestCapability.get(player)))
                    pl.zwiadowcy.story.StoryService.queue(player, pl.zwiadowcy.story.StoryTexts.EPILOG_TOM2, 1);
            }
            case TRUDNOSC -> pl.zwiadowcy.difficulty.StoryDifficulty.set(player, msg.arg);
            case TROPICIEL -> pl.zwiadowcy.tracking.TrackerSense.set(player, msg.arg.equals("1"));
            case WNIOSEK -> pl.zwiadowcy.tracking.Investigation.answer(player, msg.arg);
            case SZYBKA_PODROZ -> pl.zwiadowcy.travel.FastTravel.travel(player, msg.arg);
            case ROZKAZ -> pl.zwiadowcy.companion.Companions.order(player, msg.arg);
            case ZRYW -> pl.zwiadowcy.entity.TugEntity.burst(player);
            case KONIEC_PRZERYWNIKA -> pl.zwiadowcy.cutscene.Cutscenes.onClientFinished(player, msg.arg);
            case POKAZ_EPILOG -> {
                var st = pl.zwiadowcy.quest.QuestCapability.get(player).get("przed_obliczem_krola");
                if (st != null && st.status == pl.zwiadowcy.quest.QuestProgress.Status.COMPLETED) pl.zwiadowcy.story.StoryService.showEpilog(player, 1);
            }
        }
    }
}
