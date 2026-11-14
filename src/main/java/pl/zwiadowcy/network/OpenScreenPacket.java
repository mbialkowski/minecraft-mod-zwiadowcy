package pl.zwiadowcy.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import pl.zwiadowcy.client.ClientHandlers;

import java.util.function.Supplier;

/** Serwer → klient: otwórz ekran moda (np. "mapa" po kliknięciu ogniska zwiadowców). */
public record OpenScreenPacket(String screen) {

    public static void encode(OpenScreenPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.screen);
    }

    public static OpenScreenPacket decode(FriendlyByteBuf buf) {
        return new OpenScreenPacket(buf.readUtf());
    }

    public static void handle(OpenScreenPacket msg, Supplier<NetworkEvent.Context> ctx) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHandlers.openScreen(msg.screen()));
    }
}
