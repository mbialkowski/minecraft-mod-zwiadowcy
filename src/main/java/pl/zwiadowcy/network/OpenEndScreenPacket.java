package pl.zwiadowcy.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import pl.zwiadowcy.client.ClientHandlers;

import java.util.function.Supplier;

/** Serwer → klient: ekran końca tomu (napisy końcowe i przejście do kolejnej części). */
public record OpenEndScreenPacket(int volume, boolean knight, boolean nextStarted, boolean freePlay) {

    public static void encode(OpenEndScreenPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.volume);
        buf.writeBoolean(msg.knight);
        buf.writeBoolean(msg.nextStarted);
        buf.writeBoolean(msg.freePlay);
    }

    public static OpenEndScreenPacket decode(FriendlyByteBuf buf) {
        return new OpenEndScreenPacket(buf.readVarInt(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean());
    }

    public static void handle(OpenEndScreenPacket msg, Supplier<NetworkEvent.Context> ctx) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHandlers.openEndScreen(msg));
    }
}
