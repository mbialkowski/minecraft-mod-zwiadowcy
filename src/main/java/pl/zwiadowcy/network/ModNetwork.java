package pl.zwiadowcy.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import pl.zwiadowcy.Zwiadowcy;

public final class ModNetwork {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            Zwiadowcy.id("main"), () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    public static void register() {
        int id = 0;
        CHANNEL.messageBuilder(OpenDialoguePacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(OpenDialoguePacket::encode).decoder(OpenDialoguePacket::decode)
                .consumerMainThread(OpenDialoguePacket::handle).add();
        CHANNEL.messageBuilder(QuestSyncPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(QuestSyncPacket::encode).decoder(QuestSyncPacket::decode)
                .consumerMainThread(QuestSyncPacket::handle).add();
        CHANNEL.messageBuilder(StealthPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(StealthPacket::encode).decoder(StealthPacket::decode)
                .consumerMainThread(StealthPacket::handle).add();
        CHANNEL.messageBuilder(OpenStoryPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(OpenStoryPacket::encode).decoder(OpenStoryPacket::decode)
                .consumerMainThread(OpenStoryPacket::handle).add();
        CHANNEL.messageBuilder(DialogueChoicePacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(DialogueChoicePacket::encode).decoder(DialogueChoicePacket::decode)
                .consumerMainThread(DialogueChoicePacket::handle).add();
        CHANNEL.messageBuilder(ActionPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ActionPacket::encode).decoder(ActionPacket::decode)
                .consumerMainThread(ActionPacket::handle).add();
    }

    public static void sendTo(ServerPlayer player, Object packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendToServer(Object packet) {
        CHANNEL.sendToServer(packet);
    }

    private ModNetwork() {}
}
