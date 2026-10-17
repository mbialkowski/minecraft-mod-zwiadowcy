package pl.zwiadowcy.world;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Zapis świata: gdzie stoją struktury fabularne, ich punkty kotwiczne oraz rejestr "pokoleń" NPC
 * (przeniesienie NPC = nowe pokolenie; stara kopia sama się usuwa po załadowaniu).
 */
public class LocationData extends SavedData {
    private static final String NAME = "zwiadowcy_lokacje";

    public static class Placed {
        public final BlockPos center;
        public final BlockPos origin;
        public final Map<String, BlockPos> anchors = new HashMap<>();

        public Placed(BlockPos center, BlockPos origin) {
            this.center = center;
            this.origin = origin;
        }
    }

    private final Map<String, Placed> placed = new HashMap<>();
    private final Map<String, Integer> npcGenerations = new HashMap<>();
    private final Set<String> spawnedNpcs = new HashSet<>();
    private boolean tugSpawned;

    public static LocationData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(LocationData::load, LocationData::new, NAME);
    }

    @Nullable public Placed placed(String id) { return placed.get(id); }
    public void putPlaced(String id, Placed p) { placed.put(id, p); setDirty(); }
    public Map<String, Placed> allPlaced() { return placed; }

    public int generation(String npc) { return npcGenerations.getOrDefault(npc, 0); }

    public int bumpGeneration(String npc) {
        int g = generation(npc) + 1;
        npcGenerations.put(npc, g);
        spawnedNpcs.add(npc);
        setDirty();
        return g;
    }

    public boolean wasSpawned(String npc) { return spawnedNpcs.contains(npc); }
    public void markSpawned(String npc) { spawnedNpcs.add(npc); setDirty(); }

    public boolean tugSpawned() { return tugSpawned; }
    public void setTugSpawned() { tugSpawned = true; setDirty(); }

    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag pl = new CompoundTag();
        placed.forEach((id, p) -> {
            CompoundTag t = new CompoundTag();
            t.put("center", NbtUtils.writeBlockPos(p.center));
            t.put("origin", NbtUtils.writeBlockPos(p.origin));
            CompoundTag an = new CompoundTag();
            p.anchors.forEach((k, v) -> an.put(k, NbtUtils.writeBlockPos(v)));
            t.put("anchors", an);
            pl.put(id, t);
        });
        tag.put("placed", pl);
        CompoundTag gens = new CompoundTag();
        npcGenerations.forEach(gens::putInt);
        tag.put("generations", gens);
        ListTag sp = new ListTag();
        spawnedNpcs.forEach(s -> sp.add(StringTag.valueOf(s)));
        tag.put("spawned", sp);
        tag.putBoolean("tugSpawned", tugSpawned);
        return tag;
    }

    public static LocationData load(CompoundTag tag) {
        LocationData d = new LocationData();
        CompoundTag pl = tag.getCompound("placed");
        for (String id : pl.getAllKeys()) {
            CompoundTag t = pl.getCompound(id);
            Placed p = new Placed(NbtUtils.readBlockPos(t.getCompound("center")), NbtUtils.readBlockPos(t.getCompound("origin")));
            CompoundTag an = t.getCompound("anchors");
            for (String k : an.getAllKeys()) p.anchors.put(k, NbtUtils.readBlockPos(an.getCompound(k)));
            d.placed.put(id, p);
        }
        CompoundTag gens = tag.getCompound("generations");
        for (String k : gens.getAllKeys()) d.npcGenerations.put(k, gens.getInt(k));
        ListTag sp = tag.getList("spawned", Tag.TAG_STRING);
        for (int i = 0; i < sp.size(); i++) d.spawnedNpcs.add(sp.getString(i));
        d.tugSpawned = tag.getBoolean("tugSpawned");
        return d;
    }
}
