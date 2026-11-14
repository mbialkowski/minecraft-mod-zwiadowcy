package pl.zwiadowcy.quest;

import com.google.gson.JsonObject;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import pl.zwiadowcy.companion.Companions;
import pl.zwiadowcy.companion.Trust;
import pl.zwiadowcy.entity.NpcService;
import pl.zwiadowcy.entity.StoryNpc;
import pl.zwiadowcy.entity.TugEntity;
import pl.zwiadowcy.skill.Skills;
import pl.zwiadowcy.travel.Campfires;
import pl.zwiadowcy.volume.Volumes;
import pl.zwiadowcy.world.Araluen;
import pl.zwiadowcy.world.Locations;

import java.util.*;

/**
 * Cele i akcje misji tomu II.
 * <p>Cele: follow_trail (trop po kotwicach), ride_with (dotrzyj z towarzyszami), survive (przetrwaj do czasu albo
 * do niskiego zdrowia - bez śmierci), defend (obrona z falami i minimum żywych), mark (oznacz lunetą), free (uwolnij
 * jeńców przy kłódkach), oil_points / ignite_points (most), heal (opatrz rannych), clues i deduce (śledztwo).
 * <p>Akcje: companion_join/leave/order, trust, skill, weather, capture, restore_inventory, end_volume, checkpoint,
 * cutscene, campfire, tug_command, glow.
 */
public final class QuestTom2 {
    /** Czas patrzenia przez lunetę na cel, po którym zostaje oznaczony (ticki). */
    private static final int MARK_TICKS = 20;
    private static final Map<UUID, int[]> MARK_PROGRESS = new HashMap<>();

    // ------------------------------------------------------------------ cele: tick co 10 ticków

    /** Zwraca true, gdy coś się zmieniło (potrzebna synchronizacja). */
    static boolean tick(ServerPlayer p, QuestDefinition def, QuestProgress.State st, QuestDefinition.Stage stage) {
        QuestDefinition.Objective o = stage.objective();
        MinecraftServer server = p.getServer();
        if (server == null) return false;
        if (stage.battle() && p.getRandom().nextInt(5) == 0) battleSound(p);
        if (o.params().has("arena")) arena(p, server, o);
        switch (o.type()) {
            case "follow_trail" -> {
                List<String> anchors = o.list("anchors");
                if (st.progress >= anchors.size()) {
                    QuestEngine.advance(p, def.id());
                    return false;
                }
                BlockPos next = Locations.peek(server, anchors.get(st.progress));
                double r = o.dbl("radius", 5);
                if (next != null && p.position().distanceToSqr(Vec3.atBottomCenterOf(next)) <= r * r) {
                    st.progress++;
                    if (st.progress >= anchors.size()) QuestEngine.advance(p, def.id());
                    else p.displayClientMessage(Component.literal("Trop prowadzi dalej… (" + st.progress + "/" + anchors.size() + ")")
                            .withStyle(ChatFormatting.DARK_GREEN), true);
                    return true;
                }
            }
            case "ride_with" -> {
                if (!Araluen.isAraluen(p.level())) return false;
                BlockPos pos = Locations.peek(server, QuestEngine.locationOf(stage));
                double r = o.dbl("radius", 8);
                if (pos == null || p.position().distanceToSqr(Vec3.atBottomCenterOf(pos)) > r * r) return false;
                double cr = o.dbl("companion_radius", 24);
                for (String npc : o.list("npcs")) {
                    StoryNpc n = NpcService.findNearest(p, npc);
                    if (n == null || n.distanceTo(p) > cr) {
                        // drużyna utknęła (ścieżka, brama, teren) - po 10 s dogania gracza sama
                        String key = "zw_czekam:" + def.id();
                        long now = p.level().getGameTime();
                        if (!p.getPersistentData().contains(key)) p.getPersistentData().putLong(key, now);
                        if (now - p.getPersistentData().getLong(key) > 200) {
                            p.getPersistentData().remove(key);
                            Companions.teleportParty(p, p.blockPosition());
                            p.displayClientMessage(Component.literal("Drużyna dogania cię.").withStyle(ChatFormatting.GREEN), true);
                            return false;
                        }
                        p.displayClientMessage(Component.literal("Zaczekaj na drużynę.").withStyle(ChatFormatting.YELLOW), true);
                        return false;
                    }
                }
                p.getPersistentData().remove("zw_czekam:" + def.id());
                QuestEngine.advance(p, def.id());
            }
            case "survive" -> {
                int seconds = (int) o.dbl("seconds", 30);
                st.progress = Math.min(seconds, st.progress + 1);
                double until = o.dbl("until_health", 0);
                if (st.progress * 10 >= seconds * 20 || (until > 0 && p.getHealth() <= p.getMaxHealth() * until)) {
                    QuestEngine.advance(p, def.id());
                    return false;
                }
                return st.progress % 4 == 0;
            }
            case "defend" -> {
                return tickDefend(p, def, st, o);
            }
            case "kill" -> {
                if (o.bool("tagged")) return tickHunt(p, def, st, stage);
            }
            case "bridge_burnt" -> {
                if (pl.zwiadowcy.bridge.BridgeBurn.state(server) == pl.zwiadowcy.bridge.BridgeBurn.BURNT) {
                    QuestEngine.advance(p, def.id());
                    return false;
                }
            }
            default -> {}
        }
        return false;
    }

    /**
     * Pokonaj oznaczonych wrogów: gdy zostało ich najwyżej 2, świecą (widać ich przez ściany). Gdy w pobliżu miejsca
     * etapu nie ma już żadnego (uciekł, spadł w przepaść, zniknął w niewczytanym terenie), po 15 s etap kończy się sam.
     */
    private static boolean tickHunt(ServerPlayer p, QuestDefinition def, QuestProgress.State st, QuestDefinition.Stage stage) {
        int remaining = stage.objective().count(QuestCapability.get(p)) - st.progress;
        if (remaining <= 0) return false;
        List<Mob> rest = p.level().getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(192),
                m -> m.isAlive() && !(m instanceof StoryNpc) && def.id().equals(m.getPersistentData().getString(QuestActions.TAG_QUEST))
                        && matchesKill(m, stage.objective().str("entity")));
        String key = "zw_bez_celow:" + def.id();
        if (!rest.isEmpty()) {
            p.getPersistentData().remove(key);
            if (rest.size() <= 2) {
                for (Mob m : rest) {
                    if (!m.hasEffect(MobEffects.GLOWING)) m.addEffect(new MobEffectInstance(MobEffects.GLOWING, 400, 0, false, false));
                }
            }
            return false;
        }
        // wrogów nie ma w pobliżu - liczy się tylko, gdy gracz jest na miejscu etapu
        String loc = QuestEngine.locationOf(stage);
        BlockPos at = loc != null ? Locations.peek(p.getServer(), loc) : null;
        if (at == null || p.blockPosition().distSqr(at) > 64 * 64) return false;
        long now = p.level().getGameTime();
        if (!p.getPersistentData().contains(key)) {
            p.getPersistentData().putLong(key, now);
            return false;
        }
        if (now - p.getPersistentData().getLong(key) < 300) return false;
        p.getPersistentData().remove(key);
        p.sendSystemMessage(Component.literal("Reszta wrogów uciekła - droga wolna.").withStyle(ChatFormatting.GRAY));
        QuestEngine.addProgress(p, def.id(), remaining);
        return false;
    }

    private static boolean matchesKill(Mob m, String want) {
        if (want.isEmpty() || want.equals("*")) return true;
        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(m.getType());
        return key != null && key.toString().equals(want);
    }

    /** Obrona: co pół sekundy odliczanie; fale jako zdarzenia "fala_N"; za mało żywych - etap od nowa. */
    private static boolean tickDefend(ServerPlayer p, QuestDefinition def, QuestProgress.State st, QuestDefinition.Objective o) {
        int seconds = (int) o.dbl("seconds", 60);
        int halfSeconds = st.progress + 1;
        st.progress = halfSeconds;
        if (o.params().has("waves")) {
            for (var w : GsonHelper.getAsJsonArray(o.params(), "waves")) {
                JsonObject wave = w.getAsJsonObject();
                if (GsonHelper.getAsInt(wave, "t") * 2 == halfSeconds) QuestEngine.fireEvent(p, GsonHelper.getAsString(wave, "event"), 1);
            }
        }
        List<String> npcs = o.list("npcs");
        int minAlive = (int) o.dbl("min_alive", 0);
        if (!npcs.isEmpty() && minAlive > 0 && halfSeconds > 4) {
            int alive = 0;
            for (StoryNpc n : p.level().getEntitiesOfClass(StoryNpc.class, p.getBoundingBox().inflate(48),
                    n -> npcs.contains(n.npcId()) && n.isAlive() && !n.isDowned() && n.getMode() != StoryNpc.Mode.WOUNDED)) alive++;
            if (alive < minAlive) {
                p.sendSystemMessage(Component.literal("Oddział nie wytrzymał - spróbuj jeszcze raz.").withStyle(ChatFormatting.RED));
                QuestEngine.restartStage(p, def.id());
                return true;
            }
        }
        if (halfSeconds >= seconds * 2) {
            QuestEngine.advance(p, def.id());
            return false;
        }
        if (halfSeconds % 10 == 0) {
            p.displayClientMessage(Component.literal("Wytrzymaj jeszcze " + (seconds - halfSeconds / 2) + " s").withStyle(ChatFormatting.YELLOW), true);
        }
        return halfSeconds % 4 == 0;
    }

    // ------------------------------------------------------------------ oznaczanie lunetą (co 4 ticki)

    static void fastTick(ServerPlayer p) {
        if (!p.isScoping()) {
            MARK_PROGRESS.remove(p.getUUID());
            return;
        }
        for (var e : QuestCapability.get(p).states().entrySet()) {
            if (e.getValue().status != QuestProgress.Status.ACTIVE) continue;
            QuestDefinition def = QuestManager.INSTANCE.get(e.getKey());
            QuestDefinition.Stage stage = def != null ? def.stage(e.getValue().stage) : null;
            if (stage == null || !stage.objective().type().equals("mark")) continue;
            Mob target = lookedAt(p, 160);
            if (target == null || !markable(target, stage.objective(), def.id())) {
                MARK_PROGRESS.remove(p.getUUID());
                return;
            }
            int[] prog = MARK_PROGRESS.computeIfAbsent(p.getUUID(), u -> new int[]{target.getId(), 0});
            if (prog[0] != target.getId()) {
                prog[0] = target.getId();
                prog[1] = 0;
            }
            prog[1] += 4;
            if (prog[1] >= MARK_TICKS) {
                MARK_PROGRESS.remove(p.getUUID());
                target.getPersistentData().putBoolean("zw_oznaczony", true);
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 20 * 60 * 20, 0, false, false));
                p.displayClientMessage(Component.literal("Oznaczono: " + target.getName().getString()).withStyle(ChatFormatting.GOLD), true);
                QuestEngine.fireEvent(p, "oznaczony", 1);
                QuestEngine.addProgress(p, def.id(), 1);
            }
            return;
        }
    }

    private static boolean markable(Mob m, QuestDefinition.Objective o, String quest) {
        if (m.getPersistentData().getBoolean("zw_oznaczony")) return false;
        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(m.getType());
        List<String> types = o.list("entity");
        if (!types.isEmpty() && (key == null || !types.contains(key.toString()))) return false;
        return !o.bool("tagged") || quest.equals(m.getPersistentData().getString(QuestActions.TAG_QUEST));
    }

    private static Mob lookedAt(ServerPlayer p, double range) {
        Vec3 eye = p.getEyePosition();
        Vec3 end = eye.add(p.getLookAngle().scale(range));
        AABB box = p.getBoundingBox().expandTowards(p.getLookAngle().scale(range)).inflate(1);
        EntityHitResult hit = net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(p.level(), p, eye, end, box,
                e -> e instanceof Mob && e.isAlive(), 0.3f);
        return hit != null && hit.getEntity() instanceof Mob m ? m : null;
    }

    // ------------------------------------------------------------------ PPM: kłódki, punkty oleju, ranni

    /** PPM na bloku. Zwraca true, gdy kliknięcie obsłużyła misja. */
    public static boolean onUseBlock(ServerPlayer p, BlockPos pos) {
        MinecraftServer server = p.getServer();
        if (server == null) return false;
        for (var e : QuestCapability.get(p).states().entrySet()) {
            if (e.getValue().status != QuestProgress.Status.ACTIVE) continue;
            QuestDefinition def = QuestManager.INSTANCE.get(e.getKey());
            QuestDefinition.Stage stage = def != null ? def.stage(e.getValue().stage) : null;
            if (stage == null) continue;
            QuestDefinition.Objective o = stage.objective();
            if (!o.type().equals("free") && !o.type().equals("oil_points") && !o.type().equals("use")) continue;
            for (String anchor : o.list("anchors")) {
                BlockPos at = Locations.peek(server, anchor);
                String flag = o.type() + ":" + anchor;
                if (at == null || at.distSqr(pos) > 2.5 * 2.5 || QuestCapability.get(p).hasFlag(flag)) continue;
                String item = o.type().equals("free") ? o.str("item") : o.type().equals("use") ? "" : "zwiadowcy:flasza_oleju";
                if (!item.isEmpty() && QuestEngine.countItems(p, item) < 1) {
                    p.displayClientMessage(Component.literal(o.type().equals("free") ? "Potrzebujesz kluczy nadzorcy."
                            : "Potrzebujesz flaszy oleju.").withStyle(ChatFormatting.GRAY), true);
                    return true;
                }
                if (o.type().equals("oil_points")) QuestEngine.removeItems(p, item, 1);
                QuestCapability.get(p).setFlag(flag, true);
                if (o.type().equals("oil_points") && anchor.startsWith(pl.zwiadowcy.bridge.BridgeBuilder.ID + ":")) {
                    pl.zwiadowcy.bridge.BridgeBurn.oil(server, anchor.substring(pl.zwiadowcy.bridge.BridgeBuilder.ID.length() + 1));
                }
                QuestEngine.fireEvent(p, (o.type().equals("free") ? "uwolniony:" : o.type().equals("use") ? "uzyto:" : "olej:") + anchor, 1);
                QuestEngine.addProgress(p, def.id(), 1);
                return true;
            }
        }
        return false;
    }

    /** Podpora mostu zapłonęła od strzały gracza (cel "ignite_points"). */
    public static void onIgnite(ServerPlayer p, String anchor) {
        for (var e : new ArrayList<>(QuestCapability.get(p).states().entrySet())) {
            if (e.getValue().status != QuestProgress.Status.ACTIVE) continue;
            QuestDefinition def = QuestManager.INSTANCE.get(e.getKey());
            QuestDefinition.Stage stage = def != null ? def.stage(e.getValue().stage) : null;
            if (stage == null || !stage.objective().type().equals("ignite_points") || !stage.objective().list("anchors").contains(anchor)) continue;
            String flag = "ignite_points:" + anchor;
            if (QuestCapability.get(p).hasFlag(flag)) continue;
            QuestCapability.get(p).setFlag(flag, true);
            QuestEngine.addProgress(p, def.id(), 1);
        }
    }

    /** Etapy z "restart_on_death": po śmierci gracza etap zaczyna się od nowa (pojedynek od początku fazy, obrona). */
    public static void onRespawn(ServerPlayer p) {
        for (var e : new ArrayList<>(QuestCapability.get(p).states().entrySet())) {
            if (e.getValue().status != QuestProgress.Status.ACTIVE) continue;
            QuestDefinition def = QuestManager.INSTANCE.get(e.getKey());
            QuestDefinition.Stage stage = def != null ? def.stage(e.getValue().stage) : null;
            if (stage == null || !stage.restartOnDeath()) continue;
            String id = def.id();
            pl.zwiadowcy.util.Delayed.run(40, () -> {
                if (p.isRemoved()) return;
                QuestActions.run(p, stage.onRestart(), new QuestActions.Ctx(id, null));
                p.sendSystemMessage(Component.literal("Spróbuj jeszcze raz - ta część zaczyna się od nowa.").withStyle(ChatFormatting.GRAY));
                QuestEngine.restartStage(p, id);
            });
        }
    }

    /** Krąg pojedynku: szyki żołnierzy odpychają gracza, który próbuje uciec poza arenę. */
    private static void arena(ServerPlayer p, MinecraftServer server, QuestDefinition.Objective o) {
        BlockPos c = Locations.peek(server, o.str("arena"));
        double r = o.dbl("arena_radius", 25);
        if (c == null || p.position().distanceToSqr(c.getX() + 0.5, p.getY(), c.getZ() + 0.5) <= r * r) return;
        Vec3 back = new Vec3(c.getX() + 0.5 - p.getX(), 0, c.getZ() + 0.5 - p.getZ()).normalize().scale(0.9);
        Entity mover = p.getVehicle() != null ? p.getVehicle() : p;
        mover.setDeltaMovement(back.x, 0.25, back.z);
        mover.hurtMarked = true;
        p.displayClientMessage(Component.literal("Żołnierze w kręgu odpychają cię z powrotem - z pojedynku nie ma ucieczki.").withStyle(ChatFormatting.GRAY), true);
    }

    /** Odgłosy bitwy w tle: rogi, bębny, szczęk broni z losowego kierunku. */
    private static void battleSound(ServerPlayer p) {
        var r = p.getRandom();
        double ang = r.nextDouble() * Math.PI * 2, dist = 18 + r.nextDouble() * 14;
        double x = p.getX() + Math.cos(ang) * dist, z = p.getZ() + Math.sin(ang) * dist;
        net.minecraft.sounds.SoundEvent ev = switch (r.nextInt(6)) {
            case 0 -> net.minecraft.sounds.SoundEvents.RAID_HORN.value();
            case 1, 2 -> net.minecraft.sounds.SoundEvents.NOTE_BLOCK_BASEDRUM.value();
            case 3 -> net.minecraft.sounds.SoundEvents.ANVIL_LAND;
            case 4 -> net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_STRONG;
            default -> net.minecraft.sounds.SoundEvents.VINDICATOR_CELEBRATE;
        };
        p.level().playSound(null, x, p.getY(), z, ev, net.minecraft.sounds.SoundSource.HOSTILE, ev == net.minecraft.sounds.SoundEvents.RAID_HORN.value() ? 6f : 1.6f,
                0.7f + r.nextFloat() * 0.4f);
    }

    /** Czy gracz jest w etapie podpalania mostu (cel "ignite_points"). */
    public static boolean ignitingBridge(ServerPlayer p) {
        for (var e : QuestCapability.get(p).states().entrySet()) {
            if (e.getValue().status != QuestProgress.Status.ACTIVE) continue;
            QuestDefinition def = QuestManager.INSTANCE.get(e.getKey());
            QuestDefinition.Stage stage = def != null ? def.stage(e.getValue().stage) : null;
            if (stage != null && stage.objective().type().equals("ignite_points")) return true;
        }
        return false;
    }

    /** Czy gracz zbiera właśnie flasze oleju (beczki smoły w obozie budowy napełniają je tylko wtedy). */
    public static boolean collectingOil(ServerPlayer p) {
        for (var e : QuestCapability.get(p).states().entrySet()) {
            if (e.getValue().status != QuestProgress.Status.ACTIVE) continue;
            QuestDefinition def = QuestManager.INSTANCE.get(e.getKey());
            QuestDefinition.Stage stage = def != null ? def.stage(e.getValue().stage) : null;
            if (stage == null) continue;
            QuestDefinition.Objective o = stage.objective();
            if (o.type().equals("oil_points") || (o.type().equals("collect") && o.str("item").equals("zwiadowcy:flasza_oleju"))) return true;
        }
        return false;
    }

    /** PPM na postaci: opatrywanie rannych (cel "heal"). */
    public static boolean onUseNpc(ServerPlayer p, StoryNpc npc) {
        for (var e : QuestCapability.get(p).states().entrySet()) {
            if (e.getValue().status != QuestProgress.Status.ACTIVE) continue;
            QuestDefinition def = QuestManager.INSTANCE.get(e.getKey());
            QuestDefinition.Stage stage = def != null ? def.stage(e.getValue().stage) : null;
            if (stage == null || !stage.objective().type().equals("heal")) continue;
            if (!stage.objective().list("npcs").contains(npc.npcId()) || npc.getMode() != StoryNpc.Mode.WOUNDED) continue;
            if (QuestEngine.countItems(p, "zwiadowcy:racje_podrozne") < 1) {
                p.displayClientMessage(Component.literal("Do opatrzenia potrzebujesz racji podróżnej.").withStyle(ChatFormatting.GRAY), true);
                return true;
            }
            QuestEngine.removeItems(p, "zwiadowcy:racje_podrozne", 1);
            npc.setMode(StoryNpc.Mode.IDLE, null);
            p.displayClientMessage(Component.literal(npc.getName().getString() + " oddycha spokojniej.").withStyle(ChatFormatting.GREEN), true);
            QuestEngine.addProgress(p, def.id(), 1);
            return true;
        }
        return false;
    }

    /** Fabularne powalenie: w etapie "survive" z flagą no_death gracz nie może zginąć. */
    public static boolean protectsFromDeath(ServerPlayer p) {
        for (var e : QuestCapability.get(p).states().entrySet()) {
            if (e.getValue().status != QuestProgress.Status.ACTIVE) continue;
            QuestDefinition def = QuestManager.INSTANCE.get(e.getKey());
            QuestDefinition.Stage stage = def != null ? def.stage(e.getValue().stage) : null;
            if (stage != null && stage.objective().type().equals("survive") && stage.objective().bool("no_death")) return true;
        }
        return false;
    }

    // ------------------------------------------------------------------ akcje

    /** Zwraca false, gdy typ akcji nie jest znany. */
    static boolean action(ServerPlayer p, JsonObject a, QuestActions.Ctx ctx, String type) {
        MinecraftServer server = p.getServer();
        if (server == null) return true;
        switch (type) {
            case "companion_join" -> Companions.join(p, str(a, "npc"));
            case "companion_leave" -> Companions.leave(p, str(a, "npc"));
            case "companion_order" -> Companions.order(p, str(a, "order") + ":-1");
            case "trust" -> Trust.change(p, str(a, "npc"), GsonHelper.getAsInt(a, "delta", 0));
            case "skill" -> Skills.grant(p, str(a, "id"));
            case "weather" -> {
                ServerLevel level = Araluen.level(server);
                if (level != null) {
                    boolean rain = GsonHelper.getAsBoolean(a, "rain", true);
                    level.setWeatherParameters(rain ? 0 : 12000, rain ? GsonHelper.getAsInt(a, "ticks", 6000) : 0, rain, false);
                }
            }
            case "capture" -> {
                Volumes.storeInventory(p);
                BlockPos to = Locations.resolve(server, str(a, "to"));
                ServerLevel level = Araluen.level(server);
                if (to != null && level != null) p.teleportTo(level, to.getX() + 0.5, to.getY(), to.getZ() + 0.5, p.getYRot(), 0f);
                p.setHealth(p.getMaxHealth());
            }
            case "restore_inventory" -> Volumes.restoreInventory(p);
            case "end_volume" -> pl.zwiadowcy.story.StoryService.queueEndScreen(p, GsonHelper.getAsInt(a, "volume", 2), GsonHelper.getAsInt(a, "delay", 20));
            case "checkpoint" -> {
                BlockPos at = Locations.resolve(server, str(a, "at"));
                ServerLevel level = Araluen.level(server);
                if (at != null && level != null) p.setRespawnPosition(level.dimension(), at, p.getYRot(), true, false);
            }
            case "cutscene" -> pl.zwiadowcy.cutscene.Cutscenes.play(p, str(a, "id"));
            case "campfire" -> {
                Campfires.Def def = Campfires.byId(str(a, "id"));
                if (def != null) Campfires.discover(p, def);
            }
            case "npc_item" -> {
                StoryNpc npc = NpcService.findNearest(p, str(a, "npc"));
                var item = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(str(a, "item")));
                if (npc != null && item != null) {
                    var slot = str(a, "slot").equals("offhand") ? net.minecraft.world.entity.EquipmentSlot.OFFHAND
                            : str(a, "slot").equals("chest") ? net.minecraft.world.entity.EquipmentSlot.CHEST : net.minecraft.world.entity.EquipmentSlot.MAINHAND;
                    npc.setItemSlot(slot, new net.minecraft.world.item.ItemStack(item));
                }
            }
            case "set_block" -> {
                BlockPos at = Locations.resolve(server, str(a, "at"));
                ServerLevel level = Araluen.level(server);
                if (at != null && level != null) {
                    try {
                        var state = net.minecraft.commands.arguments.blocks.BlockStateParser.parseForBlock(
                                level.holderLookup(net.minecraft.core.registries.Registries.BLOCK), str(a, "block"), false).blockState();
                        // "height": kolumna w górę od kotwicy (np. wrota z bali na 3 bloki)
                        for (int dy = 0; dy < Math.max(1, GsonHelper.getAsInt(a, "height", 1)); dy++) level.setBlock(at.above(dy), state, 3);
                    } catch (Exception e) {
                        pl.zwiadowcy.Zwiadowcy.LOGGER.warn("Zły blok w akcji set_block: {}", str(a, "block"));
                    }
                }
            }
            case "zawal" -> pl.zwiadowcy.quest.Rockfall.start(p, Locations.resolve(server, str(a, "at")),
                    GsonHelper.getAsInt(a, "radius", 5), GsonHelper.getAsInt(a, "seconds", 20));
            case "tug_to" -> {
                BlockPos to = Locations.resolve(server, str(a, "at"));
                QuestProgress prog = QuestCapability.get(p);
                ServerLevel level = Araluen.level(server);
                if (to != null && level != null && prog.tugUuid != null && level.getEntity(prog.tugUuid) instanceof TugEntity tug) {
                    tug.teleportTo(to.getX() + 0.5, to.getY(), to.getZ() + 0.5);
                    tug.getNavigation().stop();
                }
            }
            case "tug_command" -> {
                if (ctx.source() instanceof TugEntity tug) tug.command(p, str(a, "cmd"));
            }
            case "glow" -> {
                for (Entity e : p.level().getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(GsonHelper.getAsDouble(a, "radius", 48)),
                        m -> ctx.quest() != null && ctx.quest().equals(m.getPersistentData().getString(QuestActions.TAG_QUEST)))) {
                    ((LivingEntity) e).addEffect(new MobEffectInstance(MobEffects.GLOWING, GsonHelper.getAsInt(a, "ticks", 400), 0, false, false));
                }
            }
            case "bridge" -> {
                switch (str(a, "op")) {
                    case "burn" -> pl.zwiadowcy.bridge.BridgeBurn.start(server);
                    case "finish" -> pl.zwiadowcy.bridge.BridgeBurn.finishInstantly(server);
                    default -> Locations.ensure(server, pl.zwiadowcy.bridge.BridgeBuilder.ID);
                }
            }
            case "npc_flee" -> {
                // postacie (np. uwolnieni jeńcy) uciekają do wskazanego miejsca i znikają po dotarciu
                BlockPos near = Locations.resolve(server, str(a, "near"));
                BlockPos to = Locations.resolve(server, str(a, "to"));
                if (near != null && to != null) {
                    double r = GsonHelper.getAsDouble(a, "radius", 12);
                    String npc = str(a, "npc");
                    for (StoryNpc n : p.level().getEntitiesOfClass(StoryNpc.class, new AABB(near).inflate(r),
                            n -> npc.isEmpty() || n.npcId().equals(npc))) n.flee(to, GsonHelper.getAsInt(a, "ticks", 900));
                }
            }
            case "delay" -> {
                // akcje po czasie: {"type": "delay", "ticks": 100, "actions": [...]}
                com.google.gson.JsonArray later = GsonHelper.getAsJsonArray(a, "actions", new com.google.gson.JsonArray());
                pl.zwiadowcy.util.Delayed.run(GsonHelper.getAsInt(a, "ticks", 20), () -> {
                    if (!p.isRemoved()) QuestActions.run(p, later, ctx);
                });
            }
            case "crowd" -> pl.zwiadowcy.entity.CrowdDummyEntity.spawnGroup(p, a, ctx.quest());
            case "companion_tp" -> {
                // drużyna przenosi się do kotwicy (albo do gracza): {"type": "companion_tp", "to": "lokacja:kotwica"}
                BlockPos to = a.has("to") ? Locations.resolve(server, str(a, "to")) : p.blockPosition();
                if (to != null) Companions.teleportParty(p, to);
            }
            case "odkop" -> {
                // usuwa żwir zawału wokół kotwicy (np. kryjówka Dafydda w kopalni)
                BlockPos at = Locations.resolve(server, str(a, "at"));
                ServerLevel level = Araluen.level(server);
                int r = GsonHelper.getAsInt(a, "radius", 3);
                if (at != null && level != null) {
                    for (BlockPos b : BlockPos.betweenClosed(at.offset(-r, -1, -r), at.offset(r, 3, r))) {
                        if (level.getBlockState(b).is(net.minecraft.world.level.block.Blocks.GRAVEL)) {
                            level.setBlock(b, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
                        }
                    }
                }
            }
            case "advancement" -> QuestEngine.awardAdvancement(p, str(a, "id"));
            case "spawn_npc" -> {
                // postacie nieunikalne (żołnierze, ranni) w kotwicach: {"npc": "zolnierz", "at": ["loc:a", ...], "ally": true, "mode": "idle"}
                ServerLevel level = Araluen.level(server);
                if (level == null) break;
                BlockPos face = a.has("face") ? Locations.resolve(server, str(a, "face")) : null;
                for (String ref : listOf(a, "at")) {
                    BlockPos at = Locations.resolve(server, ref);
                    if (at == null) continue;
                    float yaw = face != null ? NpcService.yawTowards(at, face) : 0f;
                    StoryNpc.Mode mode = a.has("mode") ? StoryNpc.Mode.valueOf(str(a, "mode").toUpperCase(java.util.Locale.ROOT)) : null;
                    StoryNpc n = NpcService.spawn(level, str(a, "npc"), at, -1, yaw, mode);
                    if (n == null) continue;
                    n.setPersistenceRequired();
                    if (GsonHelper.getAsBoolean(a, "ally", false)) n.addTag(StoryNpc.TAG_ALLY);
                    if (GsonHelper.getAsBoolean(a, "follow", false)) n.setMode(StoryNpc.Mode.COMPANION, p);
                    if (ctx.quest() != null) n.getPersistentData().putString(QuestActions.TAG_QUEST, ctx.quest());
                    n.restrictTo(at, GsonHelper.getAsInt(a, "radius", 8));
                }
            }
            case "clear_mobs" -> {
                // usuwa byty misji (np. przed powtórzeniem fazy pojedynku): {"quest": "...", "radius": 160}
                String q = a.has("quest") ? str(a, "quest") : ctx.quest();
                double r = GsonHelper.getAsDouble(a, "radius", 160);
                for (Entity e : p.level().getEntitiesOfClass(Entity.class, p.getBoundingBox().inflate(r),
                        e -> !(e instanceof net.minecraft.world.entity.player.Player) && q != null && q.equals(e.getPersistentData().getString(QuestActions.TAG_QUEST)))) {
                    if (e.getVehicle() != null) e.getVehicle().discard();
                    e.discard();
                }
            }
            case "salwa" -> {
                // salwa łuczników na oznaczonych wrogów (zadanie C w bitwie): deszcz strzał i trafienie po chwili
                double r = GsonHelper.getAsDouble(a, "radius", 160);
                for (Mob m : p.level().getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(r), m -> m.getPersistentData().getBoolean("zw_oznaczony") && m.isAlive())) {
                    ServerLevel sl = (ServerLevel) m.level();
                    for (int i = 0; i < 8; i++) {
                        net.minecraft.world.entity.projectile.Arrow arrow = new net.minecraft.world.entity.projectile.Arrow(sl, m.getX() + (sl.random.nextDouble() - 0.5) * 4, m.getY() + 18, m.getZ() + (sl.random.nextDouble() - 0.5) * 4);
                        arrow.setDeltaMovement(0, -2.2, 0);
                        arrow.pickup = net.minecraft.world.entity.projectile.AbstractArrow.Pickup.DISALLOWED;
                        sl.addFreshEntity(arrow);
                    }
                    pl.zwiadowcy.util.Delayed.run(14, () -> {
                        if (m.isAlive()) m.hurt(p.damageSources().playerAttack(p), 1000f);
                    });
                }
            }
            case "crowd_clear" -> pl.zwiadowcy.entity.CrowdDummyEntity.clear(Araluen.level(server), str(a, "tag"));
            case "time" -> {
                // pora dnia (czas płynie wspólnie z Overworldem)
                ServerLevel ow = server.overworld();
                long target = switch (str(a, "pora")) {
                    case "noc" -> 13000;
                    case "poludnie" -> 6000;
                    default -> 23000;
                };
                long now = ow.getDayTime(), next = now - Math.floorMod(now, 24000) + target;
                if (next <= now) next += 24000;
                ow.setDayTime(next);
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    private static List<String> listOf(JsonObject a, String key) {
        List<String> out = new ArrayList<>();
        if (!a.has(key)) return out;
        if (a.get(key).isJsonArray()) a.getAsJsonArray(key).forEach(x -> out.add(x.getAsString()));
        else out.add(a.get(key).getAsString());
        return out;
    }

    private static String str(JsonObject a, String key) {
        return GsonHelper.getAsString(a, key, "");
    }

    private QuestTom2() {}
}
