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

    public enum Kind { BEZRUCH, NASTEPNA_STRZALA, SLEDZ_MISJE, KONIEC_OPOWIESCI }

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
        }
    }
}
