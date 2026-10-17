package pl.zwiadowcy.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import pl.zwiadowcy.client.ClientHandlers;

import java.util.function.Supplier;

/** Serwer → klient: poziom widoczności gracza w płaszczu (0-100) i pozostały czas Bezruchu. */
public record StealthPacket(int visibility, int bezruchTicks, boolean cloak) {

    public static void encode(StealthPacket msg, FriendlyByteBuf buf) {
        buf.writeByte(msg.visibility);
        buf.writeVarInt(msg.bezruchTicks);
        buf.writeBoolean(msg.cloak);
    }

    public static StealthPacket decode(FriendlyByteBuf buf) {
        return new StealthPacket(buf.readByte(), buf.readVarInt(), buf.readBoolean());
    }

    public static void handle(StealthPacket msg, Supplier<NetworkEvent.Context> ctx) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHandlers.stealth(msg));
    }
}
