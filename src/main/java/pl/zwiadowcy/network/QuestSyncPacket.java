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
                              @Nullable BlockPos target, String targetName, Extra extra) {

    /** Bity stanu tomów. */
    public static final int TOM1_DONE = 1, TOM2_STARTED = 2, TOM2_DONE = 4, FREE_PLAY = 8, TOM1_FINISHED_STORY = 16, QUICK_START = 32;

    /** Punkt na Mapie Gilana: 0 = nieodkryta lokacja („?”), 1 = odwiedzona lokacja, 2 = ognisko do szybkiej podróży. */
    public record MapPoint(String id, String name, BlockPos pos, int kind) {}

    /** Stan tomu II widoczny w dzienniku i na mapie: tomy, trudność, umiejętności, zaufanie, ogniska, blokada podróży. */
    public record Extra(int volumes, String difficulty, List<String> skills, Map<String, Integer> trust,
                        List<MapPoint> map, String travelLock, List<String> clues, float layoutAngle, List<String> deduction) {
        public static final Extra EMPTY = new Extra(0, "zwiadowca", List.of(), Map.of(), List.of(), "", List.of(), 0f, List.of());

        public boolean has(int bit) { return (volumes & bit) != 0; }
    }

    public record QuestView(String id, String title, String act, String description, boolean completed,
                            boolean repeatable, int order, int stage, int stageCount, String currentText,
                            int progress, int required, boolean awaitNight, List<String> doneStages, int tom) {}

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
                    st.progress, stage != null ? stage.objective().count(prog) : 0,
                    stage != null && stage.objective().type().equals("night"), done, def.tom()));
            if (!completed && def.id().equals(prog.tracked()) && stage != null && p.getServer() != null) {
                BlockPos drop = stage.objective().type().equals("collect") ? questDrop(p, stage.objective().str("item")) : null;
                if (st.targetPos != null) {
                    target = st.targetPos;
                    targetName = "cel zlecenia";
                } else if (drop != null) {
                    // przedmiot upuszczony przez moba z misji (np. Znak Morgaratha) - strzałka prowadzi prosto do niego
                    target = drop;
                    targetName = "upuszczony przedmiot";
                } else {
                    String loc = QuestEngine.locationOf(stage);
                    target = Locations.peek(p.getServer(), loc);
                    StoryLocation sl = loc != null ? StoryLocation.byId(loc.split(":", 2)[0]) : null;
                    targetName = sl != null ? sl.displayName : "";
                }
            }
        }
        // postoje na szlaku: strzałka prowadzi przez postój, który leży przed graczem na drodze do celu
        // (albo przy którym właśnie trwa zadanie); minięty postój przestaje być wskazywany
        if (target != null && p.getServer() != null) {
            BlockPos mainTarget = target;
            double toTarget = Math.sqrt(p.blockPosition().distSqr(mainTarget));
            double bestD = Double.MAX_VALUE;
            for (Map.Entry<String, QuestProgress.State> e : prog.states().entrySet()) {
                QuestProgress.State st = e.getValue();
                if (!e.getKey().startsWith("szlak_") || e.getKey().equals(prog.tracked()) || st.status != QuestProgress.Status.ACTIVE) continue;
                QuestDefinition def = QuestManager.INSTANCE.get(e.getKey());
                QuestDefinition.Stage stage = def != null ? def.stage(st.stage) : null;
                BlockPos stop = stage != null ? Locations.peek(p.getServer(), QuestEngine.locationOf(stage)) : null;
                if (stop == null) continue;
                double d = p.blockPosition().distSqr(stop);
                boolean ahead = Math.sqrt(stop.distSqr(mainTarget)) < toTarget - 16;
                boolean busy = st.stage > 0 && d < 64 * 64;
                if ((ahead || busy) && d < bestD) {
                    bestD = d;
                    target = stop;
                    targetName = def.title();
                }
            }
        }
        views.sort(Comparator.comparingInt(QuestView::order));
        return new QuestSyncPacket(prog.tracked(), views, new ArrayList<>(prog.notes()), target, targetName, extra(p, prog));
    }

    private static Extra extra(ServerPlayer p, QuestProgress prog) {
        int v = 0;
        if (pl.zwiadowcy.volume.Volumes.tom1Done(prog)) v |= TOM1_DONE;
        if (pl.zwiadowcy.volume.Volumes.tom2Started(prog)) v |= TOM2_STARTED;
        if (pl.zwiadowcy.volume.Volumes.tom2Done(prog)) v |= TOM2_DONE;
        if (prog.hasFlag(pl.zwiadowcy.volume.Volumes.FLAG_FREE_PLAY)) v |= FREE_PLAY;
        if (prog.isCompleted(pl.zwiadowcy.volume.Volumes.TOM1_LAST)) v |= TOM1_FINISHED_STORY;
        if (prog.hasFlag(pl.zwiadowcy.volume.Volumes.FLAG_QUICK)) v |= QUICK_START;
        return new Extra(v, prog.difficulty, new ArrayList<>(prog.skills()), pl.zwiadowcy.companion.Trust.visible(prog),
                pl.zwiadowcy.travel.Campfires.mapPoints(p), java.util.Objects.requireNonNullElse(pl.zwiadowcy.travel.FastTravel.locked(p), ""),
                pl.zwiadowcy.tracking.Clues.foundList(prog), (float) pl.zwiadowcy.world.VolumeTwoLayout.angleDegrees(),
                pl.zwiadowcy.tracking.Clues.activeDeduction(prog));
    }

    /** Najbliższy leżący przedmiot z misji (oznaczony przy upuszczeniu) pasujący do celu "zbierz". */
    @Nullable
    private static BlockPos questDrop(ServerPlayer p, String itemSpec) {
        if (itemSpec.isEmpty()) return null;
        net.minecraft.world.entity.item.ItemEntity best = null;
        for (var e : p.level().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, p.getBoundingBox().inflate(128),
                it -> it.getPersistentData().getBoolean(pl.zwiadowcy.quest.QuestActions.TAG_QUEST_DROP)
                        && QuestEngine.matches(it.getItem(), itemSpec))) {
            if (best == null || e.distanceToSqr(p) < best.distanceToSqr(p)) best = e;
        }
        return best == null ? null : best.blockPosition();
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
            buf.writeBoolean(q.awaitNight);
            buf.writeVarInt(q.doneStages.size());
            for (String s : q.doneStages) buf.writeUtf(s, 1024);
            buf.writeVarInt(q.tom);
        }
        buf.writeVarInt(msg.notes.size());
        for (QuestProgress.Note n : msg.notes) {
            buf.writeUtf(n.title());
            buf.writeUtf(n.text(), 4096);
        }
        buf.writeBoolean(msg.target != null);
        if (msg.target != null) buf.writeBlockPos(msg.target);
        buf.writeUtf(msg.targetName);
        Extra e = msg.extra;
        buf.writeVarInt(e.volumes());
        buf.writeUtf(e.difficulty());
        buf.writeVarInt(e.skills().size());
        for (String s : e.skills()) buf.writeUtf(s);
        buf.writeVarInt(e.trust().size());
        e.trust().forEach((k, val) -> {
            buf.writeUtf(k);
            buf.writeVarInt(val);
        });
        buf.writeVarInt(e.map().size());
        for (MapPoint c : e.map()) {
            buf.writeUtf(c.id());
            buf.writeUtf(c.name());
            buf.writeBlockPos(c.pos());
            buf.writeByte(c.kind());
        }
        buf.writeUtf(e.travelLock());
        buf.writeVarInt(e.clues().size());
        for (String s : e.clues()) buf.writeUtf(s);
        buf.writeFloat(e.layoutAngle());
        buf.writeVarInt(e.deduction().size());
        for (String s : e.deduction()) buf.writeUtf(s, 1024);
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
            boolean awaitNight = buf.readBoolean();
            int dn = buf.readVarInt();
            List<String> done = new ArrayList<>(dn);
            for (int j = 0; j < dn; j++) done.add(buf.readUtf(1024));
            int tom = buf.readVarInt();
            quests.add(new QuestView(id, title, act, desc, completed, repeatable, order, stage, stageCount, current, progress, required, awaitNight, done, tom));
        }
        int nn = buf.readVarInt();
        List<QuestProgress.Note> notes = new ArrayList<>(nn);
        for (int i = 0; i < nn; i++) notes.add(new QuestProgress.Note(buf.readUtf(), buf.readUtf(4096)));
        BlockPos target = buf.readBoolean() ? buf.readBlockPos() : null;
        String targetName = buf.readUtf();
        int volumes = buf.readVarInt();
        String difficulty = buf.readUtf();
        int sn = buf.readVarInt();
        List<String> skills = new ArrayList<>(sn);
        for (int i = 0; i < sn; i++) skills.add(buf.readUtf());
        int tn = buf.readVarInt();
        Map<String, Integer> trust = new HashMap<>();
        for (int i = 0; i < tn; i++) trust.put(buf.readUtf(), buf.readVarInt());
        int cn = buf.readVarInt();
        List<MapPoint> camps = new ArrayList<>(cn);
        for (int i = 0; i < cn; i++) camps.add(new MapPoint(buf.readUtf(), buf.readUtf(), buf.readBlockPos(), buf.readByte()));
        String locked = buf.readUtf();
        int kn = buf.readVarInt();
        List<String> clues = new ArrayList<>(kn);
        for (int i = 0; i < kn; i++) clues.add(buf.readUtf());
        float angle = buf.readFloat();
        int dn = buf.readVarInt();
        List<String> deduction = new ArrayList<>(dn);
        for (int i = 0; i < dn; i++) deduction.add(buf.readUtf(1024));
        return new QuestSyncPacket(tracked, quests, notes, target, targetName,
                new Extra(volumes, difficulty, skills, trust, camps, locked, clues, angle, deduction));
    }

    public static void handle(QuestSyncPacket msg, Supplier<NetworkEvent.Context> ctx) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHandlers.questSync(msg));
    }
}
