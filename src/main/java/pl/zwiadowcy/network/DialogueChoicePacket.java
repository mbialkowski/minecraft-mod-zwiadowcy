package pl.zwiadowcy.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import pl.zwiadowcy.dialogue.DialogueService;

import java.util.function.Supplier;

/** Klient → serwer: wybrana opcja rozmowy (-1 = zamknięcie). */
public record DialogueChoicePacket(int index) {

    public static void encode(DialogueChoicePacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.index + 1);
    }

    public static DialogueChoicePacket decode(FriendlyByteBuf buf) {
        return new DialogueChoicePacket(buf.readVarInt() - 1);
    }

    public static void handle(DialogueChoicePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        if (player != null) DialogueService.choose(player, msg.index);
    }
}
