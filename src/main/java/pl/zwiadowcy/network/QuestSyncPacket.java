package pl.zwiadowcy.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import pl.zwiadowcy.client.ClientHandlers;
import pl.zwiadowcy.quest.*;
import pl.zwiadowcy.world.Locations;
import pl.zwiadowcy.world.StoryLocation;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;

/** Serwer → klient: pełny stan dziennika (misje, notatki, cel śledzonej misji). */
public record QuestSyncPacket(String tracked, List<QuestView> quests, List<QuestProgress.Note> notes,
                              @Nullable BlockPos target, String targetName) {

    public record QuestView(String id, String title, String act, String description, boolean completed,
                            boolean repeatable, int order, int stage, int stageCount, String currentText,
                            int progress, int required, List<String> doneStages) {}

    public static QuestSyncPacket build(ServerPlayer p) {
        QuestProgress prog = QuestCapability.get(p);
        List<QuestView> views = new ArrayList<>();
        BlockPos target = null;
        String targetName = "";
        for (Map.Entry<String, QuestProgress.State> e : prog.states().entrySet()) {
            QuestDefinition def = QuestManager.INSTANCE.get(e.getKey());
            if (def == null) continue;
            QuestProgress.State st = e.getValue();
            boolean completed = st.status == QuestProgress.Status.COMPLETED;
            QuestDefinition.Stage stage = completed ? null : def.stage(st.stage);
            List<String> done = new ArrayList<>();
            int doneCount = completed ? def.stages().size() : st.stage;
            for (int i = 0; i < doneCount && i < def.stages().size(); i++) done.add(def.stages().get(i).text());
            views.add(new QuestView(def.id(), def.title(), def.act(), def.description(), completed, def.repeatable(),
                    def.order(), st.stage, def.stages().size(), stage != null ? stage.text() : "",
                    st.progress, stage != null ? stage.objective().count() : 0, done));
            if (!completed && def.id().equals(prog.tracked()) && stage != null && p.getServer() != null) {
                if (st.targetPos != null) {
                    target = st.targetPos;
                    targetName = "cel zlecenia";
                } else {
                    String loc = QuestEngine.locationOf(stage);
                    target = Locations.peek(p.getServer(), loc);
                    StoryLocation sl = loc != null ? StoryLocation.byId(loc.split(":", 2)[0]) : null;
                    targetName = sl != null ? sl.displayName : "";
                }
            }
        }
        views.sort(Comparator.comparingInt(QuestView::order));
        return new QuestSyncPacket(prog.tracked(), views, new ArrayList<>(prog.notes()), target, targetName);
    }

    public static void encode(QuestSyncPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.tracked);
        buf.writeVarInt(msg.quests.size());
        for (QuestView q : msg.quests) {
            buf.writeUtf(q.id);
            buf.writeUtf(q.title);
            buf.writeUtf(q.act);
            buf.writeUtf(q.description, 4096);
            buf.writeBoolean(q.completed);
            buf.writeBoolean(q.repeatable);
            buf.writeVarInt(q.order);
            buf.writeVarInt(q.stage);
            buf.writeVarInt(q.stageCount);
            buf.writeUtf(q.currentText, 1024);
            buf.writeVarInt(q.progress);
            buf.writeVarInt(q.required);
            buf.writeVarInt(q.doneStages.size());
            for (String s : q.doneStages) buf.writeUtf(s, 1024);
        }
        buf.writeVarInt(msg.notes.size());
        for (QuestProgress.Note n : msg.notes) {
            buf.writeUtf(n.title());
            buf.writeUtf(n.text(), 4096);
        }
        buf.writeBoolean(msg.target != null);
        if (msg.target != null) buf.writeBlockPos(msg.target);
        buf.writeUtf(msg.targetName);
    }

    public static QuestSyncPacket decode(FriendlyByteBuf buf) {
        String tracked = buf.readUtf();
        int n = buf.readVarInt();
        List<QuestView> quests = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            String id = buf.readUtf();
            String title = buf.readUtf();
            String act = buf.readUtf();
            String desc = buf.readUtf(4096);
            boolean completed = buf.readBoolean();
            boolean repeatable = buf.readBoolean();
            int order = buf.readVarInt();
            int stage = buf.readVarInt();
            int stageCount = buf.readVarInt();
            String current = buf.readUtf(1024);
            int progress = buf.readVarInt();
            int required = buf.readVarInt();
            int dn = buf.readVarInt();
            List<String> done = new ArrayList<>(dn);
            for (int j = 0; j < dn; j++) done.add(buf.readUtf(1024));
            quests.add(new QuestView(id, title, act, desc, completed, repeatable, order, stage, stageCount, current, progress, required, done));
        }
        int nn = buf.readVarInt();
        List<QuestProgress.Note> notes = new ArrayList<>(nn);
        for (int i = 0; i < nn; i++) notes.add(new QuestProgress.Note(buf.readUtf(), buf.readUtf(4096)));
        BlockPos target = buf.readBoolean() ? buf.readBlockPos() : null;
        String targetName = buf.readUtf();
        return new QuestSyncPacket(tracked, quests, notes, target, targetName);
    }

    public static void handle(QuestSyncPacket msg, Supplier<NetworkEvent.Context> ctx) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHandlers.questSync(msg));
    }
}
