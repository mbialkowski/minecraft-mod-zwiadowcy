package pl.zwiadowcy.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Marker;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pl.zwiadowcy.Zwiadowcy;
import pl.zwiadowcy.network.ActionPacket;
import pl.zwiadowcy.network.ModNetwork;
import pl.zwiadowcy.network.PlayCutscenePacket;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Odtwarzanie przerywnika u klienta: kamera (niewidoczny znacznik) leci po klatkach kluczowych krzywą Catmulla-Roma,
 * na ekranie są pasy kinowe i napisy, a Esc pomija scenę. HUD i sterowanie są wyłączone.
 */
@Mod.EventBusSubscriber(modid = Zwiadowcy.MODID, value = Dist.CLIENT)
public final class CutscenePlayer {
    @Nullable private static PlayCutscenePacket scene;
    @Nullable private static Marker camera;
    private static long startTick;
    private static float fov = 70f;

    public static boolean active() {
        return scene != null;
    }

    public static void start(PlayCutscenePacket msg) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || msg.keys().isEmpty()) return;
        scene = msg;
        startTick = mc.level.getGameTime();
        camera = new Marker(EntityType.MARKER, mc.level);
        apply(0f);
        mc.setCameraEntity(camera);
        if (mc.screen != null) mc.setScreen(null);
    }

    public static void stop(boolean skipped) {
        Minecraft mc = Minecraft.getInstance();
        if (scene != null && skipped) ModNetwork.sendToServer(new ActionPacket(ActionPacket.Kind.KONIEC_PRZERYWNIKA, scene.id()));
        scene = null;
        camera = null;
        if (mc.player != null) mc.setCameraEntity(mc.player);
    }

    private static float time(float partial) {
        Minecraft mc = Minecraft.getInstance();
        return mc.level == null ? 0 : (mc.level.getGameTime() - startTick) + partial;
    }

    /** Ustawia kamerę dla chwili t (w tikach od startu). */
    private static void apply(float partial) {
        if (scene == null || camera == null) return;
        float t = time(partial);
        List<PlayCutscenePacket.Key> keys = scene.keys();
        int i = 0;
        while (i + 1 < keys.size() && keys.get(i + 1).t() <= t) i++;
        PlayCutscenePacket.Key k1 = keys.get(i);
        PlayCutscenePacket.Key k2 = keys.get(Math.min(i + 1, keys.size() - 1));
        PlayCutscenePacket.Key k0 = keys.get(Math.max(i - 1, 0));
        PlayCutscenePacket.Key k3 = keys.get(Math.min(i + 2, keys.size() - 1));
        float u = k2.t() == k1.t() ? 0f : Mth.clamp((t - k1.t()) / (float) (k2.t() - k1.t()), 0f, 1f);
        double x = cr(k0.x(), k1.x(), k2.x(), k3.x(), u), y = cr(k0.y(), k1.y(), k2.y(), k3.y(), u), z = cr(k0.z(), k1.z(), k2.z(), k3.z(), u);
        double lx = cr(k0.lx(), k1.lx(), k2.lx(), k3.lx(), u), ly = cr(k0.ly(), k1.ly(), k2.ly(), k3.ly(), u), lz = cr(k0.lz(), k1.lz(), k2.lz(), k3.lz(), u);
        fov = Mth.lerp(u, k1.fov(), k2.fov());
        double dx = lx - x, dy = ly - y, dz = lz - z;
        float yaw = (float) (Mth.atan2(dz, dx) * (180 / Math.PI)) - 90f;
        float pitch = (float) -(Mth.atan2(dy, Math.sqrt(dx * dx + dz * dz)) * (180 / Math.PI));
        camera.setPos(x, y, z);
        camera.xo = x;
        camera.yo = y;
        camera.zo = z;
        camera.xOld = x;
        camera.yOld = y;
        camera.zOld = z;
        camera.setYRot(yaw);
        camera.setXRot(pitch);
        camera.yRotO = yaw;
        camera.xRotO = pitch;
    }

    private static double cr(double p0, double p1, double p2, double p3, double t) {
        double t2 = t * t, t3 = t2 * t;
        return 0.5 * ((2 * p1) + (-p0 + p2) * t + (2 * p0 - 5 * p1 + 4 * p2 - p3) * t2 + (-p0 + 3 * p1 - 3 * p2 + p3) * t3);
    }

    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.START || scene == null) return;
        apply(event.renderTickTime);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || scene == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            stop(false);
            return;
        }
        if (mc.screen instanceof PauseScreen) {
            // Esc pomija scenę zamiast otwierać menu
            mc.setScreen(null);
            stop(true);
            return;
        }
        int last = scene.keys().get(scene.keys().size() - 1).t();
        if (time(0) > last) stop(false);
        else if (mc.getCameraEntity() != camera && camera != null) mc.setCameraEntity(camera);
    }

    @SubscribeEvent
    public static void onFov(ViewportEvent.ComputeFov event) {
        if (scene != null) event.setFOV(fov);
    }

    @SubscribeEvent
    public static void onInput(MovementInputUpdateEvent event) {
        if (scene == null) return;
        var in = event.getInput();
        in.forwardImpulse = 0;
        in.leftImpulse = 0;
        in.jumping = false;
        in.shiftKeyDown = false;
        in.up = in.down = in.left = in.right = false;
    }

    @SubscribeEvent
    public static void onOverlay(RenderGuiOverlayEvent.Pre event) {
        if (scene != null) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onGui(RenderGuiEvent.Post event) {
        if (scene == null) return;
        Minecraft mc = Minecraft.getInstance();
        GuiGraphics g = event.getGuiGraphics();
        int w = mc.getWindow().getGuiScaledWidth(), h = mc.getWindow().getGuiScaledHeight();
        int bar = h / 9;
        g.fill(0, 0, w, bar, 0xFF000000);
        g.fill(0, h - bar, w, h, 0xFF000000);
        float t = time(event.getPartialTick());
        int y = h - bar + 4;
        for (PlayCutscenePacket.Subtitle s : scene.subtitles()) {
            if (t < s.t() || t > s.t() + s.duration()) continue;
            for (FormattedCharSequence line : mc.font.split(Component.literal(s.text()), w - 40)) {
                g.drawCenteredString(mc.font, line, w / 2, y, 0xFFEFE6D0);
                y += 10;
            }
        }
        g.drawString(mc.font, "Esc - pomiń", w - mc.font.width("Esc - pomiń") - 6, 4, 0xFF888888, false);
    }

    private CutscenePlayer() {}
}
