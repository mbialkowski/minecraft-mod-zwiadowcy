package pl.zwiadowcy.companion;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import pl.zwiadowcy.difficulty.StoryDifficulty;
import pl.zwiadowcy.entity.CompanionHorseEntity;
import pl.zwiadowcy.entity.NpcService;
import pl.zwiadowcy.entity.StoryNpc;
import pl.zwiadowcy.quest.QuestCapability;
import pl.zwiadowcy.quest.QuestEngine;
import pl.zwiadowcy.registry.ModEntities;
import pl.zwiadowcy.stealth.Alertness;
import pl.zwiadowcy.stealth.Watchful;
import pl.zwiadowcy.world.LocationData;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Drużyna tomu II: dołączanie i odchodzenie towarzyszy, ich konie (jazda w szyku, zsiadanie w strefach „pieszo”),
 * kucanie przy skradaniu, ranni towarzysze i rozkazy z koła (Za mną, Czekaj tu, Ukryj się, Odciągnij uwagę).
 */
public final class Companions {
    /** Kolejność w szyku i maść konia. */
    private static final Map<String, Integer> FORMATION = Map.of("horace", 0, "gilan", 1, "evanlyn", 2, "halt", 1);
    private static final Map<String, String> COAT = Map.of("horace", "chestnut", "gilan", "brown", "evanlyn", "white", "halt", "darkbrown");
    /** Lokacje, w których drużyna idzie pieszo (konie czekają przed wejściem). */
    private static final Set<String> FOOT_LOCATIONS = Set.of("kopalnia", "budowa_mostu", "posterunek_dobosza", "tre_gwyn",
            "oboz_krolewski", "brzeg_rzeki", "przelecz_wiatrow");

    // ------------------------------------------------------------------ dołączanie

    /** Towarzysz dołącza do drużyny (akcja misji "companion_join"). */
    public static void join(ServerPlayer p, String npcId) {
        StoryNpc npc = NpcService.findNearest(p, npcId);
        if (npc == null) {
            MinecraftServer server = p.getServer();
            ServerLevel level = server != null ? pl.zwiadowcy.world.Araluen.level(server) : null;
            if (level == null) return;
            int gen = LocationData.get(server).bumpGeneration(npcId);
            BlockPos at = p.blockPosition().offset(2, 0, 2);
            npc = NpcService.spawn(level, npcId, at, gen, p.getYRot(), null);
            if (npc == null) return;
        }
        npc.setMode(StoryNpc.Mode.COMPANION, p);
        npc.setOrder(StoryNpc.Order.ZA_MNA, null);
        if (npcId.equals("horace")) {
            // Horace w tomie II: kolczuga i tarcza Redmontu
            npc.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
            npc.setItemSlot(net.minecraft.world.entity.EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
        }
        if (npcId.equals("gilan")) {
            npc.setItemSlot(net.minecraft.world.entity.EquipmentSlot.OFFHAND, new ItemStack(pl.zwiadowcy.registry.ModItems.MIECZ_ZWIADOWCY.get()));
        }
        QuestCapability.get(p).setFlag(npcId + "_w_druzynie", true);
        QuestEngine.sync(p);
    }

    /** Towarzysz odchodzi (akcja misji "companion_leave"); koń znika razem z nim. */
    public static void leave(ServerPlayer p, String npcId) {
        StoryNpc npc = NpcService.findNearest(p, npcId);
        if (npc != null) {
            CompanionHorseEntity horse = horseOf(npc);
            if (npc.isPassenger()) npc.stopRiding();
            if (horse != null) horse.discard();
            npc.setHorseUuid(null);
            npc.setMode(StoryNpc.Mode.IDLE, null);
        }
        QuestCapability.get(p).setFlag(npcId + "_w_druzynie", false);
        QuestEngine.sync(p);
    }

    public static List<StoryNpc> of(ServerPlayer p) {
        return p.level().getEntitiesOfClass(StoryNpc.class, p.getBoundingBox().inflate(160),
                n -> n.ownerPlayer() == p && (n.getMode() == StoryNpc.Mode.COMPANION || n.isDowned()));
    }

    /** Przenosi całą drużynę (z końmi) w pobliże punktu, np. za otwartą bramę albo do gracza, gdy utknęła. */
    public static void teleportParty(ServerPlayer p, BlockPos to) {
        int i = 0;
        for (StoryNpc n : of(p)) {
            double ox = (i % 2 == 0 ? 1.5 : -1.5) * (1 + i / 2), oz = 1.5;
            i++;
            CompanionHorseEntity horse = horseOf(n);
            boolean rode = n.isPassenger();
            if (rode) n.stopRiding();
            n.teleportTo(to.getX() + 0.5 + ox, to.getY(), to.getZ() + 0.5 + oz);
            n.getNavigation().stop();
            n.setOrder(StoryNpc.Order.ZA_MNA, null);
            if (horse != null) {
                horse.teleportTo(to.getX() + 0.5 + ox, to.getY(), to.getZ() + 0.5 + oz);
                if (rode) n.startRiding(horse, true);
            }
        }
    }

    // ------------------------------------------------------------------ zadania w pojedynkę

    /** Klucz w danych gracza: "misja|horace,evanlyn" - kto został odesłany na czas zadania w pojedynkę. */
    private static final String KEY_AWAY = "zw_druzyna_odeslana";

    /**
     * Etapy z "solo": true w celu misji (np. trafienia z siodła) gracz wykonuje sam - towarzysze nie idą za nim
     * i nie zabierają mu wrogów. Wołane co 10 ticków z QuestEngine.tick: soloQuest to misja, której bieżący etap jest
     * „solo” (albo null). Gdy takiego etapu już nie ma (zadanie wykonane, misja przerwana), drużyna pojawia się przy graczu.
     */
    public static void tickSolo(ServerPlayer p, @Nullable String soloQuest) {
        var data = p.getPersistentData();
        if (soloQuest != null) {
            if (data.contains(KEY_AWAY)) return;
            List<StoryNpc> team = of(p);
            if (team.isEmpty()) return;
            StringBuilder ids = new StringBuilder();
            for (StoryNpc npc : team) {
                if (ids.indexOf(npc.npcId()) >= 0) continue;
                if (!ids.isEmpty()) ids.append(',');
                ids.append(npc.npcId());
                CompanionHorseEntity horse = horseOf(npc);
                if (npc.isPassenger()) npc.stopRiding();
                if (horse != null) horse.discard();
                npc.discard();
            }
            data.putString(KEY_AWAY, soloQuest + "|" + ids);
            p.displayClientMessage(Component.literal("Drużyna zostaje z tyłu - to zadanie wykonujesz sam.").withStyle(ChatFormatting.GRAY), true);
            return;
        }
        if (!data.contains(KEY_AWAY) || !p.isAlive() || !pl.zwiadowcy.world.Araluen.isAraluen(p.level())) return;
        String[] parts = data.getString(KEY_AWAY).split("\\|", 2);
        data.remove(KEY_AWAY);
        if (parts.length < 2 || parts[1].isEmpty()) return;
        for (String id : parts[1].split(",")) join(p, id);
        teleportParty(p, p.blockPosition());
        p.level().playSound(null, p.blockPosition(), SoundEvents.HORSE_GALLOP, SoundSource.NEUTRAL, 1f, 1f);
        p.displayClientMessage(Component.literal("Drużyna dołącza do ciebie.").withStyle(ChatFormatting.GREEN), true);
    }

    /** Czy drużyna jest teraz odesłana (zadanie w pojedynkę) - do testów i rozmów. */
    public static boolean isAway(ServerPlayer p) {
        return p.getPersistentData().contains(KEY_AWAY);
    }

    // ------------------------------------------------------------------ tick (co 10 ticków na towarzysza)

    public static void tickNpc(StoryNpc npc) {
        ServerPlayer p = npc.ownerPlayer();
        if (p == null || !(npc.level() instanceof ServerLevel level)) return;
        ensureGear(npc);
        if (npc.isDowned()) {
            boolean enemies = !level.getEntitiesOfClass(Mob.class, npc.getBoundingBox().inflate(16),
                    m -> m instanceof Enemy && m.isAlive() && m.getTarget() != null && !(m instanceof Watchful w && w.alertBrain().knockedOut())).isEmpty();
            npc.tickDowned(StoryDifficulty.of(p).companionNeedsTending, enemies, p);
            return;
        }
        // skradanie: towarzysz kuca razem z graczem
        boolean crouch = p.isCrouching() && !npc.isPassenger();
        Pose want = crouch ? Pose.CROUCHING : Pose.STANDING;
        if (npc.getPose() != want && npc.getPose() != Pose.SLEEPING) npc.setPose(want);
        // konie: jazda w szyku, gdy gracz jedzie konno poza strefą „pieszo” i rozkaz to „Za mną”
        boolean wantRide = p.getVehicle() instanceof AbstractHorse && !onFoot(p) && npc.order() == StoryNpc.Order.ZA_MNA
                && FORMATION.containsKey(npc.npcId());
        CompanionHorseEntity horse = horseOf(npc);
        if (wantRide) {
            if (horse == null) horse = spawnHorse(level, npc, p);
            if (horse != null && npc.getVehicle() != horse) {
                if (horse.distanceTo(npc) > 6) horse.teleportTo(npc.getX(), npc.getY(), npc.getZ());
                npc.startRiding(horse, true);
            }
            if (horse != null) horse.setWaiting(false);
        } else if (npc.getVehicle() instanceof CompanionHorseEntity h) {
            npc.stopRiding();
            h.setWaiting(true);
        }
        // odciąganie uwagi: Horace hałasuje przy wrogach co 2 s
        if (npc.order() == StoryNpc.Order.ODCIAGNIJ && npc.orderPos() != null && npc.tickCount % 40 == 0
                && npc.orderPos().distToCenterSqr(npc.position()) < 6 * 6) {
            npc.level().playSound(null, npc.blockPosition(), SoundEvents.VINDICATOR_CELEBRATE, SoundSource.NEUTRAL, 2f, 1.0f);
            Alertness.noise(level, npc.blockPosition(), 24);
            for (Mob m : level.getEntitiesOfClass(Mob.class, npc.getBoundingBox().inflate(10), m -> m instanceof Enemy && m.isAlive())) {
                if (m.getTarget() == null || m.getTarget() == p) m.setTarget(npc);
            }
        }
    }

    /** Wyposażenie towarzyszy tomu II - nowa kopia postaci (po przeniesieniu) dostaje je z powrotem. */
    private static void ensureGear(StoryNpc npc) {
        if (npc.npcId().equals("horace") && npc.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST).isEmpty()) {
            npc.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
            npc.setItemSlot(net.minecraft.world.entity.EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
        }
        if (npc.npcId().equals("gilan") && npc.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.OFFHAND).isEmpty()) {
            npc.setItemSlot(net.minecraft.world.entity.EquipmentSlot.OFFHAND, new ItemStack(pl.zwiadowcy.registry.ModItems.MIECZ_ZWIADOWCY.get()));
        }
    }

    private static boolean onFoot(ServerPlayer p) {
        if (p.getServer() == null) return false;
        LocationData data = LocationData.get(p.getServer());
        for (String id : FOOT_LOCATIONS) {
            LocationData.Placed placed = data.placed(id);
            if (placed != null && placed.center.distSqr(p.blockPosition()) < 44 * 44) return true;
        }
        return false;
    }

    @Nullable
    public static CompanionHorseEntity horseOf(StoryNpc npc) {
        if (npc.horseUuid() == null || !(npc.level() instanceof ServerLevel level)) return null;
        Entity e = level.getEntity(npc.horseUuid());
        return e instanceof CompanionHorseEntity h && h.isAlive() ? h : null;
    }

    @Nullable
    private static CompanionHorseEntity spawnHorse(ServerLevel level, StoryNpc npc, ServerPlayer p) {
        CompanionHorseEntity horse = ModEntities.KON_TOWARZYSZA.get().create(level);
        if (horse == null) return null;
        horse.moveTo(npc.getX(), npc.getY(), npc.getZ(), npc.getYRot(), 0f);
        horse.setup(p, FORMATION.getOrDefault(npc.npcId(), 1), COAT.getOrDefault(npc.npcId(), "brown"));
        level.addFreshEntity(horse);
        npc.setHorseUuid(horse.getUUID());
        return horse;
    }

    // ------------------------------------------------------------------ rozkazy z koła (klawisz G)

    /** Argument: "rozkaz:idBytu" (idBytu -1 = cała drużyna). */
    public static void order(ServerPlayer p, String arg) {
        String[] parts = arg.split(":", 2);
        StoryNpc.Order order;
        try {
            order = StoryNpc.Order.valueOf(parts[0]);
        } catch (IllegalArgumentException e) {
            return;
        }
        int entityId = -1;
        if (parts.length > 1) {
            try {
                entityId = Integer.parseInt(parts[1]);
            } catch (NumberFormatException ignored) {}
        }
        List<StoryNpc> team = of(p);
        if (team.isEmpty()) {
            p.displayClientMessage(Component.literal("Nie masz teraz drużyny.").withStyle(ChatFormatting.GRAY), true);
            return;
        }
        int applied = 0;
        for (StoryNpc npc : team) {
            if (entityId >= 0 && npc.getId() != entityId) continue;
            if (npc.isDowned()) continue;
            if (order == StoryNpc.Order.ODCIAGNIJ && !npc.npcId().equals("horace")) continue;
            BlockPos pos = switch (order) {
                case CZEKAJ -> npc.blockPosition();
                case UKRYJ -> hideSpot(npc);
                case ODCIAGNIJ -> distractSpot(p, npc);
                default -> null;
            };
            if (order == StoryNpc.Order.ODCIAGNIJ && pos == null) {
                p.displayClientMessage(Component.literal("Horace nie widzi w pobliżu wrogów do odciągnięcia.").withStyle(ChatFormatting.GRAY), true);
                continue;
            }
            npc.setOrder(order, pos);
            npc.setTarget(null);
            applied++;
        }
        if (applied == 0) return;
        String label = switch (order) {
            case ZA_MNA -> "Za mną!";
            case CZEKAJ -> "Czekajcie tu.";
            case UKRYJ -> "Ukryjcie się.";
            case ODCIAGNIJ -> "Horace, odciągnij ich uwagę!";
        };
        p.displayClientMessage(Component.literal(label).withStyle(ChatFormatting.GOLD), true);
        String event = switch (order) {
            case ZA_MNA -> "rozkaz_za_mna";
            case CZEKAJ -> "rozkaz_czekaj";
            case UKRYJ -> "rozkaz_ukryj";
            case ODCIAGNIJ -> "rozkaz_odciagnij";
        };
        QuestEngine.fireEvent(p, event, 1);
    }

    /** Najbliższe krzaki, liście albo wysoka trawa (do 8 bloków), inaczej miejsce, w którym stoi. */
    private static BlockPos hideSpot(StoryNpc npc) {
        BlockPos base = npc.blockPosition();
        BlockPos best = null;
        double bestD = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(base.offset(-8, -2, -8), base.offset(8, 2, 8))) {
            var s = npc.level().getBlockState(pos);
            if (!(s.is(BlockTags.LEAVES) || s.is(BlockTags.REPLACEABLE_BY_TREES) || s.is(BlockTags.FLOWERS))) continue;
            if (!npc.level().getBlockState(pos.below()).isSolid()) continue;
            double d = pos.distSqr(base);
            if (d < bestD) {
                bestD = d;
                best = pos.immutable();
            }
        }
        return best != null ? best : base;
    }

    /** Odciąganie uwagi: najbliższy czujny wróg w promieniu 40 bloków (albo miejsce wskazane przez misję). */
    @Nullable
    private static BlockPos distractSpot(ServerPlayer p, StoryNpc npc) {
        String target = QuestCapability.get(p).hasFlag("odciagnij_brama") && p.getServer() != null
                ? "budowa_mostu:brama" : null;
        if (target != null) {
            BlockPos pos = pl.zwiadowcy.world.Locations.peek(p.getServer(), target);
            if (pos != null) return pos;
        }
        Mob best = null;
        double bestD = Double.MAX_VALUE;
        for (Mob m : p.level().getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(40),
                m -> m instanceof Enemy && m.isAlive() && !(m instanceof Watchful w && w.alertBrain().knockedOut()))) {
            double d = m.distanceToSqr(npc);
            if (d < bestD) {
                bestD = d;
                best = m;
            }
        }
        return best == null ? null : best.blockPosition();
    }

    private Companions() {}
}
