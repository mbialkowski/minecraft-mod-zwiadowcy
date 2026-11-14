package pl.zwiadowcy.companion;

import net.minecraft.server.level.ServerPlayer;
import pl.zwiadowcy.quest.QuestCapability;
import pl.zwiadowcy.quest.QuestProgress;
import pl.zwiadowcy.volume.Volumes;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Ukryte zaufanie towarzyszy tomu II (0-100). Zmieniają je wybory w dialogach i zachowanie w walce.
 * Próg ≥ 70: dodatkowe kwestie, prezenty i pomoc w trudnym momencie; ≤ 30: chłodne kwestie. Fabuła się nie zmienia.
 */
public final class Trust {
    public static final int HIGH = 70, LOW = 30;

    public static int get(ServerPlayer p, String npc) {
        return QuestCapability.get(p).trust(npc);
    }

    public static void change(ServerPlayer p, String npc, int delta) {
        QuestProgress prog = QuestCapability.get(p);
        prog.setTrust(npc, prog.trust(npc) + delta);
    }

    public static boolean high(ServerPlayer p, String npc) {
        return get(p, npc) >= HIGH;
    }

    /** Zaufanie towarzyszy, których gracz już poznał w tomie II (do zakładki „Drużyna” w dzienniku). */
    public static Map<String, Integer> visible(QuestProgress prog) {
        Map<String, Integer> out = new LinkedHashMap<>();
        if (!Volumes.tom2Started(prog)) return out;
        out.put("horace", prog.trust("horace"));
        out.put("gilan", prog.trust("gilan"));
        if (prog.hasFlag("evanlyn_w_druzynie")) out.put("evanlyn", prog.trust("evanlyn"));
        return out;
    }

    /** Opis bez liczb, jak w dzienniku. */
    public static String hint(String npc, int value) {
        String name = switch (npc) {
            case "horace" -> "Horace";
            case "gilan" -> "Gilan";
            case "evanlyn" -> "Evanlyn";
            default -> npc;
        };
        if (value >= HIGH) return name + " polega na tobie bez wahania.";
        if (value >= 50) return name + " ci ufa.";
        if (value > LOW) return name + " wciąż cię poznaje.";
        return name + " nie do końca ci ufa.";
    }

    private Trust() {}
}
