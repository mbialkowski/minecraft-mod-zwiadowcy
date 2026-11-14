package pl.zwiadowcy.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import pl.zwiadowcy.client.ClientHandlers;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Serwer → klient: przerywnik z kamerą (klatki kluczowe w gotowych współrzędnych świata i napisy). */
public record PlayCutscenePacket(String id, List<Key> keys, List<Subtitle> subtitles) {

    public record Key(int t, double x, double y, double z, double lx, double ly, double lz, float fov) {}

    public record Subtitle(int t, int duration, String text) {}

    public static void encode(PlayCutscenePacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.id);
        buf.writeVarInt(msg.keys.size());
        for (Key k : msg.keys) {
            buf.writeVarInt(k.t);
            buf.writeDouble(k.x);
            buf.writeDouble(k.y);
            buf.writeDouble(k.z);
            buf.writeDouble(k.lx);
            buf.writeDouble(k.ly);
            buf.writeDouble(k.lz);
            buf.writeFloat(k.fov);
        }
        buf.writeVarInt(msg.subtitles.size());
        for (Subtitle s : msg.subtitles) {
            buf.writeVarInt(s.t);
            buf.writeVarInt(s.duration);
            buf.writeUtf(s.text, 1024);
        }
    }

    public static PlayCutscenePacket decode(FriendlyByteBuf buf) {
        String id = buf.readUtf();
        int n = buf.readVarInt();
        List<Key> keys = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            keys.add(new Key(buf.readVarInt(), buf.readDouble(), buf.readDouble(), buf.readDouble(),
                    buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readFloat()));
        }
        int m = buf.readVarInt();
        List<Subtitle> subs = new ArrayList<>(m);
        for (int i = 0; i < m; i++) subs.add(new Subtitle(buf.readVarInt(), buf.readVarInt(), buf.readUtf(1024)));
        return new PlayCutscenePacket(id, keys, subs);
    }

    public static void handle(PlayCutscenePacket msg, Supplier<NetworkEvent.Context> ctx) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHandlers.playCutscene(msg));
    }
}
