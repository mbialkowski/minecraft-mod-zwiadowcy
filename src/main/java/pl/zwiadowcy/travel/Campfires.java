package pl.zwiadowcy.travel;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import pl.zwiadowcy.network.QuestSyncPacket;
import pl.zwiadowcy.quest.QuestCapability;
import pl.zwiadowcy.quest.QuestProgress;
import pl.zwiadowcy.volume.Volumes;
import pl.zwiadowcy.world.Araluen;
import pl.zwiadowcy.world.LocationData;
import pl.zwiadowcy.world.StoryLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Ogniska zwiadowców - punkty szybkiej podróży. Ognisko odkrywa się, przechodząc obok niego (do 8 bloków);
 * ogniska z tomu I (Redmont, chata Halta, obóz Zlotu) odblokowują się na starcie tomu II.
 */
public final class Campfires {
    public record Def(String id, String location, String anchor, String name, boolean volumeOne) {}

    public static final List<Def> ALL = List.of(
            new Def("redmont", "redmont", "przybycie", "Redmont", true),
            new Def("chata_halta", "chata_halta", "chata", "Chata Halta", true),
            new Def("oboz_zlotu", "oboz_zlotu", "oboz", "Obóz Zlotu", true),
            new Def("oboz_na_trakcie", "oboz_na_trakcie", "ognisko", "Obozowisko na trakcie", false),
            new Def("przelecz_wiatrow", "przelecz_wiatrow", "zejscie", "Przełęcz Wiatrów", false),
            new Def("tre_gwyn", "tre_gwyn", "ognisko", "Tre-Gwyn", false),
            new Def("dolina_mgiel", "dolina_mgiel", "ognisko", "Dolina Mgieł", false),
            new Def("szalas_pasterski", "szalas_pasterski", "ognisko", "Szałas pasterski", false),
            new Def("krawedz", "krawedz", "ognisko", "Krawędź Rozpadliny", false),
            new Def("oboz_krolewski", "oboz_krolewski", "ognisko", "Obóz królewski", false));

    /** Ogniska, które odkrywa się wyłącznie przez misję (szałas pasterki po misji pobocznej). */
    private static final Set<String> QUEST_ONLY = Set.of("szalas_pasterski");

    @Nullable
    public static Def byId(String id) {
        for (Def d : ALL) if (d.id.equals(id)) return d;
        return null;
    }

    @Nullable
    public static BlockPos pos(MinecraftServer server, Def def) {
        LocationData.Placed placed = LocationData.get(server).placed(def.location);
        return placed == null ? null : placed.anchors.getOrDefault(def.anchor, placed.center);
    }

    /** Sprawdzane co 10 ticków: odkrywanie ognisk i odwiedzonych lokacji (Mapa Gilana). Zwraca, czy coś się zmieniło. */
    public static boolean tick(ServerPlayer p) {
        MinecraftServer server = p.getServer();
        if (server == null || !Araluen.isAraluen(p.level())) return false;
        QuestProgress prog = QuestCapability.get(p);
        boolean dirty = false;
        LocationData data = LocationData.get(server);
        for (StoryLocation loc : StoryLocation.values()) {
            LocationData.Placed placed = data.placed(loc.id);
            String flag = "odwiedzono:" + loc.id;
            if (placed == null || prog.hasFlag(flag)) continue;
            if (placed.center.distSqr(p.blockPosition()) < 48 * 48) {
                prog.setFlag(flag, true);
                dirty = true;
            }
        }
        if (!Volumes.tom2Started(prog)) return dirty;
        for (Def def : ALL) {
            if (prog.knowsCampfire(def.id) || QUEST_ONLY.contains(def.id)) continue;
            BlockPos pos = pos(server, def);
            if (pos == null) continue;
            if (def.volumeOne || pos.distSqr(p.blockPosition()) < 8 * 8) {
                discover(p, def);
                dirty = true;
            }
        }
        return dirty;
    }

    public static void discover(ServerPlayer p, Def def) {
        if (!QuestCapability.get(p).addCampfire(def.id) || def.volumeOne) return;
        p.sendSystemMessage(Component.literal("🔥 Ognisko zwiadowców: ").withStyle(ChatFormatting.GOLD)
                .append(Component.literal(def.name).withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(" - możesz tu wrócić przez Mapę Gilana.").withStyle(ChatFormatting.GRAY)));
    }

    public static List<QuestSyncPacket.MapPoint> mapPoints(ServerPlayer p) {
        List<QuestSyncPacket.MapPoint> out = new ArrayList<>();
        MinecraftServer server = p.getServer();
        if (server == null) return out;
        QuestProgress prog = QuestCapability.get(p);
        LocationData data = LocationData.get(server);
        for (StoryLocation loc : StoryLocation.values()) {
            LocationData.Placed placed = data.placed(loc.id);
            if (placed == null) continue;
            boolean visited = prog.hasFlag("odwiedzono:" + loc.id);
            out.add(new QuestSyncPacket.MapPoint(loc.id, visited ? loc.displayName : "?", placed.center, visited ? 1 : 0));
        }
        for (Def def : ALL) {
            if (!prog.knowsCampfire(def.id)) continue;
            BlockPos pos = pos(server, def);
            if (pos != null) out.add(new QuestSyncPacket.MapPoint(def.id, def.name, pos, 2));
        }
        return out;
    }

    private Campfires() {}
}
