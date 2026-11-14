package pl.zwiadowcy.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import pl.zwiadowcy.Zwiadowcy;
import pl.zwiadowcy.stealth.Watchful;

/**
 * Wizualia tomu II po stronie klienta: ikony czujności nad wrogami (?, !, zz i pasek podejrzenia) oraz mgła
 * w biomach Celtyki (gęstsza w Mglistych dolinach).
 */
@Mod.EventBusSubscriber(modid = Zwiadowcy.MODID, value = Dist.CLIENT)
public final class ClientTom2Visuals {

    @SubscribeEvent
    public static void onRenderLiving(RenderLivingEvent.Post<?, ?> event) {
        LivingEntity e = event.getEntity();
        if (!(e instanceof Watchful w) || !(e instanceof Mob mob) || !mob.getTags().contains(Watchful.TAG)) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.player.distanceToSqr(e) > 32 * 32 || mc.options.hideGui) return;
        int state = w.syncedAlertState(), meter = w.syncedAlertMeter();
        String icon;
        int color;
        switch (state) {
            case 1 -> {
                icon = "?";
                color = 0xFFF0D040;
            }
            case 2 -> {
                icon = "!";
                color = 0xFFE03030;
            }
            case 3 -> {
                icon = "zz";
                color = 0xFFA0A0A0;
            }
            default -> {
                if (meter <= 0) return;
                icon = "?";
                color = 0x99F0D040;
            }
        }
        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        pose.translate(0, e.getBbHeight() + 0.75, 0);
        pose.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
        pose.scale(-0.04f, -0.04f, 0.04f);
        Matrix4f m = pose.last().pose();
        Font font = mc.font;
        MultiBufferSource buffers = event.getMultiBufferSource();
        font.drawInBatch(icon, -font.width(icon) / 2f, 0, color, false, m, buffers, Font.DisplayMode.SEE_THROUGH, 0, event.getPackedLight());
        if (state <= 1 && meter > 0) {
            // pasek podejrzenia pod znakiem
            String bar = "▮".repeat(Math.max(1, meter / 20));
            font.drawInBatch(bar, -font.width(bar) / 2f, 10, 0xFFF0D040, false, m, buffers, Font.DisplayMode.SEE_THROUGH, 0, event.getPackedLight());
        }
        pose.popPose();
    }

    /** Mgła w Celtyce: widoczność od 20% zasięgu renderowania (Mgliste doliny: od 8%). */
    @SubscribeEvent
    public static void onFog(ViewportEvent.RenderFog event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || event.getMode() != net.minecraft.client.renderer.FogRenderer.FogMode.FOG_TERRAIN) return;
        if (!pl.zwiadowcy.world.Araluen.isAraluen(mc.level)) return;
        Holder<Biome> biome = mc.level.getBiome(mc.player.blockPosition());
        String id = biome.unwrapKey().map(k -> k.location().getPath()).orElse("");
        float start;
        if (id.equals("mgliste_doliny")) start = 0.08f;
        else if (id.equals("wrzosowiska_celtyki") || id.equals("lupkowe_wzgorza")) start = 0.2f;
        else return;
        float far = event.getFarPlaneDistance();
        event.setNearPlaneDistance(far * start);
        event.setFarPlaneDistance(far * (start + 0.55f));
        event.setCanceled(true);
    }

    private ClientTom2Visuals() {}
}
