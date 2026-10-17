package pl.zwiadowcy.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pl.zwiadowcy.Zwiadowcy;
import pl.zwiadowcy.network.ActionPacket;
import pl.zwiadowcy.network.ModNetwork;

@Mod.EventBusSubscriber(modid = Zwiadowcy.MODID, value = Dist.CLIENT)
public final class ClientEvents {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;
        while (ModKeys.BEZRUCH.consumeClick()) {
            ModNetwork.sendToServer(new ActionPacket(ActionPacket.Kind.BEZRUCH, ""));
        }
        while (ModKeys.STRZALA.consumeClick()) {
            ModNetwork.sendToServer(new ActionPacket(ActionPacket.Kind.NASTEPNA_STRZALA, ""));
        }
        while (ModKeys.DZIENNIK.consumeClick()) {
            ClientHandlers.openJournal();
        }
    }

    private ClientEvents() {}
}
