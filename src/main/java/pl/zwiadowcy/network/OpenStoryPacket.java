package pl.zwiadowcy.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import pl.zwiadowcy.client.ClientHandlers;

import java.util.function.Supplier;

/** Serwer → klient: pokaż ekran-kronikę z opowieścią o danym identyfikatorze (kronika, prolog). */
public record OpenStoryPacket(String story) {

    public static void encode(OpenStoryPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.story);
    }

    public static OpenStoryPacket decode(FriendlyByteBuf buf) {
        return new OpenStoryPacket(buf.readUtf());
    }

    public static void handle(OpenStoryPacket msg, Supplier<NetworkEvent.Context> ctx) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHandlers.openStory(msg.story));
    }
}
