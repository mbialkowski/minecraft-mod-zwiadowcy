package pl.zwiadowcy.client.screen;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import pl.zwiadowcy.Zwiadowcy;
import pl.zwiadowcy.client.ClientQuestData;
import pl.zwiadowcy.network.ActionPacket;
import pl.zwiadowcy.network.ModNetwork;
import pl.zwiadowcy.network.QuestSyncPacket;
import pl.zwiadowcy.world.VolumeTwoLayout;

import javax.annotation.Nullable;

/**
 * Mapa Gilana: pergaminowa mapa Araluenu i Celtyki rysowana z układu krain (z obrotem zapisanym w świecie),
 * z odwiedzonymi lokacjami, „?” w miejscu nieodkrytych, ogniskami zwiadowców (kliknięcie = szybka podróż)
 * i strzałką gracza.
 */
public class MapScreen extends Screen {
    private static final int TEX_W = 256, TEX_H = 192;
    private static final ResourceLocation TEX_ID = Zwiadowcy.id("mapa_gilana_dynamiczna");
    private static final int INK = 0xFF3B2A1A, ACCENT = 0xFF8A2B1A;

    private DynamicTexture texture;
    private double minX, minZ, scale;
    private int left, top, drawW, drawH;
    @Nullable private QuestSyncPacket.MapPoint confirm;

    public MapScreen() {
        super(Component.literal("Mapa Gilana"));
    }

    private QuestSyncPacket.Extra extra() {
        return ClientQuestData.sync != null ? ClientQuestData.sync.extra() : QuestSyncPacket.Extra.EMPTY;
    }

    @Override
    protected void init() {
        drawW = Math.min(width - 40, 384);
        drawH = drawW * TEX_H / TEX_W;
        if (drawH > height - 70) {
            drawH = height - 70;
            drawW = drawH * TEX_W / TEX_H;
        }
        left = (width - drawW) / 2;
        top = (height - drawH) / 2 + 6;
        if (texture == null) buildTexture();
        if (confirm != null) {
            QuestSyncPacket.MapPoint target = confirm;
            addRenderableWidget(Button.builder(Component.literal("Jedź: " + target.name()), b -> {
                ModNetwork.sendToServer(new ActionPacket(ActionPacket.Kind.SZYBKA_PODROZ, target.id()));
                onClose();
            }).bounds(width / 2 - 104, top + drawH + 4, 140, 18).build());
            addRenderableWidget(Button.builder(Component.literal("Anuluj"), b -> {
                confirm = null;
                rebuildWidgets();
            }).bounds(width / 2 + 40, top + drawH + 4, 64, 18).build());
        }
    }

    /** Rysuje krainy do tekstury: wrzosowiska Celtyki, Rozpadlina, góry, okolica Redmontu, trakt, Uthal. */
    private void buildTexture() {
        double ang = Math.toRadians(extra().layoutAngle());
        double c = Math.cos(ang), s = Math.sin(ang);
        // obszar mapy w układzie: od Celtyki po Ruiny Gorlanu i Uthal
        double lx0 = -2600, lx1 = 1400, lz0 = -1200, lz1 = 1800;
        double[][] corners = {{lx0, lz0}, {lx1, lz0}, {lx0, lz1}, {lx1, lz1}};
        double wx0 = Double.MAX_VALUE, wx1 = -Double.MAX_VALUE, wz0 = Double.MAX_VALUE, wz1 = -Double.MAX_VALUE;
        for (double[] p : corners) {
            double x = p[0] * c - p[1] * s, z = p[0] * s + p[1] * c;
            wx0 = Math.min(wx0, x); wx1 = Math.max(wx1, x);
            wz0 = Math.min(wz0, z); wz1 = Math.max(wz1, z);
        }
        scale = Math.max((wx1 - wx0) / TEX_W, (wz1 - wz0) / TEX_H);
        minX = (wx0 + wx1) / 2 - scale * TEX_W / 2;
        minZ = (wz0 + wz1) / 2 - scale * TEX_H / 2;
        NativeImage img = new NativeImage(TEX_W, TEX_H, false);
        for (int py = 0; py < TEX_H; py++) {
            for (int px = 0; px < TEX_W; px++) {
                double wx = minX + px * scale, wz = minZ + py * scale;
                double x = wx * c + wz * s, z = -wx * s + wz * c;
                int rgb = terrainColor(x, z, wx, wz);
                int noise = (int) ((pl.zwiadowcy.world.HashNoise.noise2(px * 0.15, py * 0.15, 5) + 1) * 6);
                int r = Math.max(0, ((rgb >> 16) & 255) - noise), gg = Math.max(0, ((rgb >> 8) & 255) - noise), b = Math.max(0, (rgb & 255) - noise);
                img.setPixelRGBA(px, py, 0xFF000000 | (b << 16) | (gg << 8) | r);
            }
        }
        texture = new DynamicTexture(img);
        Minecraft.getInstance().getTextureManager().register(TEX_ID, texture);
    }

    private static boolean inEllipse(double x, double z, double[] e) {
        double a = (x - e[0]) / e[2], b = (z - e[1]) / e[3];
        return a * a + b * b <= 1;
    }

    private static double capsule(double x, double z, double[] k) {
        double dx = k[2] - k[0], dz = k[3] - k[1];
        double t = Math.max(0, Math.min(1, ((x - k[0]) * dx + (z - k[1]) * dz) / (dx * dx + dz * dz)));
        return Math.hypot(x - (k[0] + dx * t), z - (k[1] + dz * t));
    }

    private static int terrainColor(double x, double z, double wx, double wz) {
        if (VolumeTwoLayout.fissureDistance(x, z)[0] < 22) return 0x3a2a1e;
        if (capsule(x, z, VolumeTwoLayout.ROAD) < 10 || capsule(x, z, VolumeTwoLayout.PASS) < 10) return 0x9a7448;
        if (inEllipse(x, z, VolumeTwoLayout.UTHAL)) return 0xb8c890;
        if (inEllipse(x, z, VolumeTwoLayout.CELTICA[0]) || inEllipse(x, z, VolumeTwoLayout.CELTICA[1])) return 0xc8b0b8;
        double r = Math.hypot(wx, wz);
        if (r > 1200 && capsule(x, z, VolumeTwoLayout.PASS) > 60 && capsule(x, z, VolumeTwoLayout.ROAD) > 70) return 0xa89a88;
        if (r < 700) return 0xc8d0a0;
        return 0xdcc8a0;
    }

    private int toScreenX(double wx) {
        return left + (int) ((wx - minX) / scale * drawW / TEX_W);
    }

    private int toScreenY(double wz) {
        return top + (int) ((wz - minZ) / scale * drawH / TEX_H);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        g.fill(left - 6, top - 22, left + drawW + 6, top + drawH + 6, 0xFF4A3018);
        g.fill(left - 4, top - 20, left + drawW + 4, top + drawH + 4, 0xFFEADCB8);
        g.drawString(font, "Mapa Gilana", left, top - 14, ACCENT, false);
        g.blit(TEX_ID, left, top, 0, 0, drawW, drawH, drawW, drawH);
        QuestSyncPacket.MapPoint hover = null;
        for (QuestSyncPacket.MapPoint mp : extra().map()) {
            int x = toScreenX(mp.pos().getX()), y = toScreenY(mp.pos().getZ());
            if (x < left || y < top || x > left + drawW || y > top + drawH) continue;
            switch (mp.kind()) {
                case 0 -> g.drawString(font, "?", x - 2, y - 4, 0xFF6A5A48, false);
                case 1 -> g.fill(x - 2, y - 2, x + 2, y + 2, INK);
                default -> {
                    g.fill(x - 3, y - 3, x + 3, y + 3, 0xFF7A2A10);
                    g.fill(x - 2, y - 2, x + 2, y + 2, 0xFFFF9A2A);
                }
            }
            if (mp.kind() > 0 && Math.abs(mouseX - x) <= 4 && Math.abs(mouseY - y) <= 4) hover = mp;
        }
        var player = Minecraft.getInstance().player;
        if (player != null && pl.zwiadowcy.world.Araluen.isAraluen(player.level())) {
            int px = toScreenX(player.getX()), py = toScreenY(player.getZ());
            g.pose().pushPose();
            g.pose().translate(px, py, 0);
            g.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(player.getYRot() + 180));
            g.fill(-1, -4, 1, 3, 0xFFFFFFFF);
            g.fill(-3, -2, 3, 0, 0xFFFFFFFF);
            g.pose().popPose();
        }
        if (hover != null) {
            String label = hover.name() + (hover.kind() == 2 ? " - kliknij, by tam pojechać" : "");
            g.renderTooltip(font, Component.literal(label), mouseX, mouseY);
        }
        String lock = extra().travelLock();
        if (!lock.isEmpty() && confirm == null) g.drawString(font, "Szybka podróż: " + lock, left, top + drawH + 8, ACCENT, false);
        else if (confirm == null) g.drawString(font, "Ogniska zwiadowców: kliknij, by pojechać (1 racja).", left, top + drawH + 8, INK, false);
        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (confirm == null && extra().travelLock().isEmpty()) {
            for (QuestSyncPacket.MapPoint mp : extra().map()) {
                if (mp.kind() != 2) continue;
                int x = toScreenX(mp.pos().getX()), y = toScreenY(mp.pos().getZ());
                if (Math.abs(mouseX - x) <= 4 && Math.abs(mouseY - y) <= 4) {
                    confirm = mp;
                    rebuildWidgets();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void removed() {
        if (texture != null) {
            Minecraft.getInstance().getTextureManager().release(TEX_ID);
            texture = null;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
