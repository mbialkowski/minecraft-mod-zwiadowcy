package pl.zwiadowcy.quest;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Postęp fabularny jednego gracza: stany misji, flagi, notatki w dzienniku i dane pomocnicze
 * (Wyrwij, punkt powrotu do zwykłego świata).
 */
public class QuestProgress {
    public enum Status { ACTIVE, COMPLETED }

    public static class State {
        public Status status = Status.ACTIVE;
        public int stage;
        public int progress;
        public long completedAt = -1;
        public int timesCompleted;
        public final Set<String> firedTriggers = new HashSet<>();
        @Nullable public BlockPos targetPos;

        CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putString("status", status.name());
            tag.putInt("stage", stage);
            tag.putInt("progress", progress);
            tag.putLong("completedAt", completedAt);
            tag.putInt("times", timesCompleted);
            ListTag fired = new ListTag();
            firedTriggers.forEach(t -> fired.add(StringTag.valueOf(t)));
            tag.put("fired", fired);
            if (targetPos != null) tag.put("target", NbtUtils.writeBlockPos(targetPos));
            return tag;
        }

        static State load(CompoundTag tag) {
            State s = new State();
            try {
                s.status = Status.valueOf(tag.getString("status"));
            } catch (IllegalArgumentException e) {
                s.status = Status.ACTIVE;
            }
            s.stage = tag.getInt("stage");
            s.progress = tag.getInt("progress");
            s.completedAt = tag.getLong("completedAt");
            s.timesCompleted = tag.getInt("times");
            ListTag fired = tag.getList("fired", Tag.TAG_STRING);
            for (int i = 0; i < fired.size(); i++) s.firedTriggers.add(fired.getString(i));
            if (tag.contains("target")) s.targetPos = NbtUtils.readBlockPos(tag.getCompound("target"));
            return s;
        }
    }

    public record Note(String title, String text) {}

    private final Map<String, State> states = new LinkedHashMap<>();
    private final Set<String> flags = new HashSet<>();
    private final List<Note> notes = new ArrayList<>();
    private final Map<String, Long> cooldowns = new HashMap<>();
    private String tracked = "";

    public boolean chronicleGiven;
    public boolean visitedAraluen;
    @Nullable public UUID tugUuid;
    public int tugGeneration;
    @Nullable public BlockPos overworldReturnPos;

    public Map<String, State> states() { return states; }
    @Nullable public State get(String quest) { return states.get(quest); }

    public boolean isActive(String quest) {
        State s = states.get(quest);
        return s != null && s.status == Status.ACTIVE;
    }

    public boolean isCompleted(String quest) {
        State s = states.get(quest);
        return s != null && (s.status == Status.COMPLETED || s.timesCompleted > 0);
    }

    public boolean hasFlag(String flag) { return flags.contains(flag); }
    public void setFlag(String flag, boolean value) { if (value) flags.add(flag); else flags.remove(flag); }
    public Set<String> flags() { return flags; }

    public List<Note> notes() { return notes; }
    public void addNote(String title, String text) {
        for (Note n : notes) if (n.title().equals(title)) return;
        notes.add(new Note(title, text));
    }

    /** Znaczniki czasu dla akcji "raz na jakiś czas" (np. odpoczynek w zajeździe). */
    public long cooldown(String id) { return cooldowns.getOrDefault(id, Long.MIN_VALUE / 2); }
    public void setCooldown(String id, long gameTime) { cooldowns.put(id, gameTime); }

    public String tracked() { return tracked; }
    public void setTracked(String quest) { this.tracked = quest == null ? "" : quest; }

    public void reset() {
        states.clear();
        flags.clear();
        notes.clear();
        tracked = "";
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        CompoundTag st = new CompoundTag();
        states.forEach((k, v) -> st.put(k, v.save()));
        tag.put("states", st);
        ListTag fl = new ListTag();
        flags.forEach(f -> fl.add(StringTag.valueOf(f)));
        tag.put("flags", fl);
        ListTag nl = new ListTag();
        for (Note n : notes) {
            CompoundTag nt = new CompoundTag();
            nt.putString("title", n.title());
            nt.putString("text", n.text());
            nl.add(nt);
        }
        tag.put("notes", nl);
        CompoundTag cd = new CompoundTag();
        cooldowns.forEach(cd::putLong);
        tag.put("cooldowns", cd);
        tag.putString("tracked", tracked);
        tag.putBoolean("chronicleGiven", chronicleGiven);
        tag.putBoolean("visitedAraluen", visitedAraluen);
        if (tugUuid != null) tag.putUUID("tug", tugUuid);
        tag.putInt("tugGen", tugGeneration);
        if (overworldReturnPos != null) tag.put("owReturn", NbtUtils.writeBlockPos(overworldReturnPos));
        return tag;
    }

    public void load(CompoundTag tag) {
        states.clear();
        CompoundTag st = tag.getCompound("states");
        for (String k : st.getAllKeys()) states.put(k, State.load(st.getCompound(k)));
        flags.clear();
        ListTag fl = tag.getList("flags", Tag.TAG_STRING);
        for (int i = 0; i < fl.size(); i++) flags.add(fl.getString(i));
        notes.clear();
        ListTag nl = tag.getList("notes", Tag.TAG_COMPOUND);
        for (int i = 0; i < nl.size(); i++) {
            CompoundTag nt = nl.getCompound(i);
            notes.add(new Note(nt.getString("title"), nt.getString("text")));
        }
        cooldowns.clear();
        CompoundTag cd = tag.getCompound("cooldowns");
        for (String k : cd.getAllKeys()) cooldowns.put(k, cd.getLong(k));
        tracked = tag.getString("tracked");
        chronicleGiven = tag.getBoolean("chronicleGiven");
        visitedAraluen = tag.getBoolean("visitedAraluen");
        tugUuid = tag.hasUUID("tug") ? tag.getUUID("tug") : null;
        tugGeneration = tag.getInt("tugGen");
        overworldReturnPos = tag.contains("owReturn") ? NbtUtils.readBlockPos(tag.getCompound("owReturn")) : null;
    }
}
