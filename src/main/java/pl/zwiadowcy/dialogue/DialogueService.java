package pl.zwiadowcy.dialogue;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import pl.zwiadowcy.Zwiadowcy;
import pl.zwiadowcy.entity.NpcDefs;
import pl.zwiadowcy.network.ModNetwork;
import pl.zwiadowcy.network.OpenDialoguePacket;
import pl.zwiadowcy.quest.Conditions;
import pl.zwiadowcy.quest.QuestActions;
import pl.zwiadowcy.quest.QuestEngine;

import javax.annotation.Nullable;
import java.util.*;

/** Prowadzi rozmowy po stronie serwera: wybór węzła, filtrowanie opcji, wykonywanie akcji. */
public final class DialogueService {

    private record Session(String npc, String nodeId, int entityId, List<Integer> visibleOptions) {}

    private static final Map<UUID, Session> SESSIONS = new HashMap<>();

    public static void open(ServerPlayer p, String npc, @Nullable Entity source) {
        DialogueDefinition def = DialogueManager.INSTANCE.get(npc);
        if (def == null) {
            p.displayClientMessage(Component.literal("…").withStyle(ChatFormatting.GRAY), true);
            return;
        }
        for (DialogueDefinition.Entry e : def.entries()) {
            if (Conditions.test(p, e.when())) {
                showNode(p, def, e.start(), source);
                return;
            }
        }
    }

    private static void showNode(ServerPlayer p, DialogueDefinition def, String nodeId, @Nullable Entity source) {
        DialogueDefinition.Node node = def.nodes().get(nodeId);
        if (node == null) {
            Zwiadowcy.LOGGER.warn("Brak węzła dialogu '{}' u postaci '{}'", nodeId, def.npc());
            SESSIONS.remove(p.getUUID());
            return;
        }
        QuestActions.run(p, node.actions(), new QuestActions.Ctx(null, source));

        List<Integer> visible = new ArrayList<>();
        List<OpenDialoguePacket.OptionView> views = new ArrayList<>();
        for (int i = 0; i < node.options().size(); i++) {
            DialogueDefinition.Option opt = node.options().get(i);
            if (Conditions.test(p, opt.when())) {
                visible.add(i);
                views.add(new OpenDialoguePacket.OptionView(opt.text(), opt.next() == null));
            }
        }
        String speakerId = node.speaker() != null ? node.speaker() : def.npc();
        String speakerName;
        NpcDefs.NpcDef speakerDef = node.speaker() != null ? NpcDefs.get(node.speaker()) : null;
        if (speakerDef != null) {
            speakerName = speakerDef.name();
        } else if (source != null) {
            speakerName = source.getName().getString();
        } else {
            NpcDefs.NpcDef sd = NpcDefs.get(def.npc());
            speakerName = sd != null ? sd.name() : def.npc();
        }
        int entityId = source != null ? source.getId() : -1;
        SESSIONS.put(p.getUUID(), new Session(def.npc(), nodeId, entityId, visible));
        ModNetwork.sendTo(p, new OpenDialoguePacket(entityId, speakerId, speakerName, node.text(), views));
    }

    /** Wybór opcji przez gracza; indeks -1 oznacza zamknięcie rozmowy. */
    public static void choose(ServerPlayer p, int index) {
        Session s = SESSIONS.remove(p.getUUID());
        if (s == null) return;
        DialogueDefinition def = DialogueManager.INSTANCE.get(s.npc());
        if (def == null) return;
        DialogueDefinition.Node node = def.nodes().get(s.nodeId());
        if (node == null || index < 0 || index >= s.visibleOptions().size()) return;
        DialogueDefinition.Option opt = node.options().get(s.visibleOptions().get(index));
        Entity source = s.entityId() >= 0 ? p.level().getEntity(s.entityId()) : null;
        QuestActions.run(p, opt.actions(), new QuestActions.Ctx(null, source));
        if (opt.next() != null) showNode(p, def, opt.next(), source);
        QuestEngine.sync(p);
    }

    public static void forget(ServerPlayer p) {
        SESSIONS.remove(p.getUUID());
    }

    private DialogueService() {}
}
