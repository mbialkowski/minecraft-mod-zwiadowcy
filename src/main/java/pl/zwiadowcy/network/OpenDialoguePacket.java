package pl.zwiadowcy.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import pl.zwiadowcy.client.ClientHandlers;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Serwer → klient: pokaż węzeł rozmowy. */
public record OpenDialoguePacket(int entityId, String speakerId, String speakerName, String text, List<OptionView> options) {

    public record OptionView(String text, boolean ends) {}

    public static void encode(OpenDialoguePacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
        buf.writeUtf(msg.speakerId);
        buf.writeUtf(msg.speakerName);
        buf.writeUtf(msg.text, 8192);
        buf.writeVarInt(msg.options.size());
        for (OptionView o : msg.options) {
            buf.writeUtf(o.text, 1024);
            buf.writeBoolean(o.ends);
        }
    }

    public static OpenDialoguePacket decode(FriendlyByteBuf buf) {
        int entityId = buf.readInt();
        String speakerId = buf.readUtf();
        String speakerName = buf.readUtf();
        String text = buf.readUtf(8192);
        int n = buf.readVarInt();
        List<OptionView> options = new ArrayList<>(n);
        for (int i = 0; i < n; i++) options.add(new OptionView(buf.readUtf(1024), buf.readBoolean()));
        return new OpenDialoguePacket(entityId, speakerId, speakerName, text, options);
    }

    public static void handle(OpenDialoguePacket msg, Supplier<NetworkEvent.Context> ctx) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHandlers.openDialogue(msg));
    }
}
