package pl.zwiadowcy.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import pl.zwiadowcy.client.ModKeys;
import pl.zwiadowcy.entity.StoryNpc;
import pl.zwiadowcy.network.ActionPacket;
import pl.zwiadowcy.network.ModNetwork;

/**
 * Koło rozkazów dla drużyny (przytrzymaj G): kierunek myszy wybiera rozkaz, puszczenie klawisza go wydaje.
 * Gdy celownik wskazywał towarzysza, rozkaz dotyczy tylko jego.
 */
public class CommandWheelScreen extends Screen {
    private static final String[] ORDERS = {"ZA_MNA", "CZEKAJ", "UKRYJ", "ODCIAGNIJ"};
    private static final String[] LABELS = {"Za mną", "Czekaj tu", "Ukryj się", "Odciągnij uwagę (Horace)"};
    private final int targetId;
    private final String targetName;
    private int selected = -1;

    public CommandWheelScreen() {
        super(Component.literal("Rozkazy"));
        Minecraft mc = Minecraft.getInstance();
        if (mc.crosshairPickEntity instanceof StoryNpc npc) {
            targetId = npc.getId();
            targetName = npc.getName().getString();
        } else {
            targetId = -1;
            targetName = "";
        }
    }

    @Override
    public void tick() {
        Minecraft mc = Minecraft.getInstance();
        long window = mc.getWindow().getWindow();
        boolean down = InputConstants.isKeyDown(window, ModKeys.ROZKAZY.getKey().getValue());
        if (!down) {
            if (selected >= 0) {
                ModNetwork.sendToServer(new ActionPacket(ActionPacket.Kind.ROZKAZ, ORDERS[selected] + ":" + targetId));
            }
            onClose();
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        int cx = width / 2, cy = height / 2;
        double dx = mouseX - cx, dy = mouseY - cy;
        selected = Math.hypot(dx, dy) < 18 ? -1 : sector(dx, dy);
        g.fill(cx - 1, cy - 1, cx + 1, cy + 1, 0xFFFFFFFF);
        String head = targetId >= 0 ? "Rozkaz dla: " + targetName : "Rozkaz dla drużyny";
        g.drawCenteredString(font, head, cx, cy - 78, 0xFFE0B04A);
        int[][] pos = {{0, -52}, {70, 0}, {0, 52}, {-70, 0}};
        for (int i = 0; i < 4; i++) {
            int x = cx + pos[i][0], y = cy + pos[i][1];
            int w = font.width(LABELS[i]) + 12;
            boolean sel = i == selected;
            g.fill(x - w / 2, y - 9, x + w / 2, y + 9, sel ? 0xCC8A2B1A : 0xAA000000);
            g.drawCenteredString(font, LABELS[i], x, y - 4, sel ? 0xFFFFFFFF : 0xFFDDDDDD);
        }
        g.drawCenteredString(font, "Wskaż myszą i puść G", cx, cy + 78, 0xFFAAAAAA);
    }

    /** 0 = góra, 1 = prawo, 2 = dół, 3 = lewo. */
    private static int sector(double dx, double dy) {
        double a = Math.toDegrees(Math.atan2(dy, dx));
        if (a >= -135 && a < -45) return 0;
        if (a >= -45 && a < 45) return 1;
        if (a >= 45 && a < 135) return 2;
        return 3;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
