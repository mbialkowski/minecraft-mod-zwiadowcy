package pl.zwiadowcy.bridge;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pl.zwiadowcy.Zwiadowcy;
import pl.zwiadowcy.quest.QuestEngine;
import pl.zwiadowcy.quest.QuestTom2;
import pl.zwiadowcy.registry.ModBlocks;
import pl.zwiadowcy.registry.ModItems;
import pl.zwiadowcy.world.Araluen;
import pl.zwiadowcy.world.LocationData;
import pl.zwiadowcy.world.Locations;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Spalenie mostu (plan 9.12): podpory polane olejem zapalają się od płonącej strzały (albo krzesiwa), a potem skrypt
 * prowadzi ogień po pomoście (0-8 s), zrzuca kolejne przęsła spadającymi blokami (8-30 s, maks. 200 bloków na tik)
 * i zostawia zwęglone kikuty oraz dym nad kanionem na 10 minut gry.
 * <p>Stan jest w danych świata. Gdy gra zostanie wczytana w trakcie palenia, skrypt od razu kończy się stanem końcowym.
 */
@Mod.EventBusSubscriber(modid = Zwiadowcy.MODID)
public final class BridgeBurn {
    public static final int INTACT = 0, BURNING = 1, BURNT = 2;
    static final int IGNITE_TICKS = 160, FALL_START = 160, SPAN_GAP = 90, CHAR_AT = 600, END_AT = 700, SMOKE_TICKS = 12000;
    static final int FALL_PER_TICK = 200;
    /** Kolejność przęseł: najpierw północne (przy podporach), potem kolejne ku południu. */
    private static final int[] FALL_ORDER = {3, 2, 1, 0};

    private static volatile boolean burningNow;
    @Nullable private static Runner runner;

    // ------------------------------------------------------------------ stan

    @Nullable
    static CompoundTag tag(MinecraftServer server) {
        LocationData data = LocationData.get(server);
        return data.hasExtra(BridgeBuilder.ID) && data.extra(BridgeBuilder.ID).contains("groups") ? data.extra(BridgeBuilder.ID) : null;
    }

    public static int state(MinecraftServer server) {
        CompoundTag t = tag(server);
        return t == null ? -1 : t.getInt("state");
    }

    /** Czy trwa skrypt palenia (płonące belki poza mostem zwęglają się same przy losowych tikach). */
    public static boolean burningNow() {
        return burningNow;
    }

    private static List<BlockPos> group(CompoundTag t, String name) {
        List<BlockPos> out = new ArrayList<>();
        for (long l : t.getCompound("groups").getLongArray(name)) out.add(BlockPos.of(l));
        return out;
    }

    private static Set<String> names(CompoundTag t, String key) {
        Set<String> out = new LinkedHashSet<>();
        ListTag l = t.getList(key, Tag.TAG_STRING);
        for (int i = 0; i < l.size(); i++) out.add(l.getString(i));
        return out;
    }

    private static void addName(CompoundTag t, String key, String name) {
        ListTag l = t.getList(key, Tag.TAG_STRING);
        for (int i = 0; i < l.size(); i++) if (l.getString(i).equals(name)) return;
        l.add(StringTag.valueOf(name));
        t.put(key, l);
    }

    public static boolean isOiled(MinecraftServer server, String support) {
        CompoundTag t = tag(server);
        return t != null && names(t, "oiled").contains(support);
    }

    // ------------------------------------------------------------------ olej

    /** Polanie podpory olejem (cel misji "oil_points" na kotwicy most:podpora_N). */
    public static void oil(MinecraftServer server, String support) {
        ServerLevel level = Araluen.level(server);
        CompoundTag t = tag(server);
        if (level == null || t == null || t.getInt("state") != INTACT) return;
        addName(t, "oiled", support);
        LocationData.get(server).setDirty();
        for (BlockPos p : group(t, support)) {
            BlockState st = level.getBlockState(p);
            if (st.is(BlockTags.LOGS) || st.is(BlockTags.PLANKS)) {
                level.setBlock(p, withAxis(ModBlocks.OBLANE_DREWNO.get().defaultBlockState(), st), 2);
                level.sendParticles(ParticleTypes.SQUID_INK, p.getX() + 0.5, p.getY() + 0.6, p.getZ() + 0.5, 3, 0.3, 0.3, 0.3, 0.01);
            }
        }
        level.playSound(null, anchor(server, support), SoundEvents.HONEY_BLOCK_SLIDE, SoundSource.PLAYERS, 1f, 0.7f);
    }

    @Nullable
    private static BlockPos anchor(MinecraftServer server, String name) {
        LocationData.Placed p = LocationData.get(server).placed(BridgeBuilder.ID);
        return p == null ? null : p.anchors.get(name);
    }

    // ------------------------------------------------------------------ podpalenie

    /** Płonąca strzała (moda albo zaczarowana Płomieniem) trafia w blok. */
    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (!(event.getProjectile() instanceof AbstractArrow arrow) || arrow.level().isClientSide || !arrow.isOnFire()) return;
        if (!(event.getRayTraceResult() instanceof BlockHitResult hit)) return;
        ServerPlayer owner = arrow.getOwner() instanceof ServerPlayer sp ? sp : null;
        if (arrow.level() instanceof ServerLevel level && level == Araluen.level(level.getServer())) {
            tryIgnite(level.getServer(), hit.getBlockPos(), owner);
        }
    }

    /** PPM na bloku: krzesiwo przy polanej podporze albo beczka smoły w obozie (napełnia flaszę). */
    public static boolean onUseBlock(ServerPlayer p, BlockPos pos, ItemStack held) {
        MinecraftServer server = p.getServer();
        if (server == null) return false;
        if (held.is(Items.FLINT_AND_STEEL) && tryIgnite(server, pos, p)) {
            held.hurtAndBreak(1, p, pl -> pl.broadcastBreakEvent(net.minecraft.world.InteractionHand.MAIN_HAND));
            return true;
        }
        if (p.level().getBlockState(pos).is(Blocks.BARREL) && tarBarrel(p, pos)) return true;
        return false;
    }

    private static boolean tarBarrel(ServerPlayer p, BlockPos pos) {
        MinecraftServer server = p.getServer();
        BlockPos store = server == null ? null : Locations.peek(server, "budowa_mostu:sklad_smoly");
        if (store == null || LocationData.get(server).placed("budowa_mostu") == null || store.distSqr(pos) > 8 * 8) return false;
        if (!QuestTom2.collectingOil(p)) return false;
        int taken = p.getPersistentData().getInt("zw_smola");
        if (taken >= 8) {
            p.displayClientMessage(Component.literal("Beczki są już puste.").withStyle(ChatFormatting.GRAY), true);
            return true;
        }
        p.getPersistentData().putInt("zw_smola", taken + 1);
        ItemStack flask = new ItemStack(ModItems.FLASZA_OLEJU.get());
        if (!p.getInventory().add(flask)) p.drop(flask, false);
        p.level().playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 1f, 0.8f);
        p.displayClientMessage(Component.literal("Napełniasz flaszę gęstą smołą z beczki.").withStyle(ChatFormatting.YELLOW), true);
        return true;
    }

    /** Zapala polaną podporę w pobliżu trafionego bloku. */
    static boolean tryIgnite(MinecraftServer server, BlockPos hit, @Nullable ServerPlayer owner) {
        CompoundTag t = tag(server);
        ServerLevel level = Araluen.level(server);
        if (t == null || level == null || t.getInt("state") != INTACT) return false;
        // podpalić można tylko w etapie misji (inaczej zapalona za wcześnie podpora zablokowałaby misję)
        if (owner == null || !QuestTom2.ignitingBridge(owner)) return false;
        Set<String> oiled = names(t, "oiled"), lit = names(t, "lit");
        for (String support : oiled) {
            if (lit.contains(support)) continue;
            boolean near = false;
            for (BlockPos p : group(t, support)) {
                if (p.distSqr(hit) <= 2.5 * 2.5) {
                    near = true;
                    break;
                }
            }
            if (!near) continue;
            addName(t, "lit", support);
            LocationData.get(server).setDirty();
            for (BlockPos p : group(t, support)) {
                BlockState st = level.getBlockState(p);
                BlockState b = burnt(st);
                if (b != st) level.setBlock(p, b, 2);
            }
            BlockPos a = anchor(server, support);
            if (a != null) {
                level.playSound(null, a, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.5f, 0.8f);
                level.sendParticles(ParticleTypes.FLAME, a.getX() + 0.5, a.getY() + 0.5, a.getZ() + 0.5, 30, 0.6, 1.2, 0.6, 0.02);
            }
            if (owner != null) {
                owner.displayClientMessage(Component.literal("Podpora płonie!").withStyle(ChatFormatting.GOLD), true);
                QuestEngine.fireEvent(owner, "podpalony:most:" + support, 1);
                QuestTom2.onIgnite(owner, "most:" + support);
            }
            return true;
        }
        return false;
    }

    // ------------------------------------------------------------------ skrypt palenia

    /** Akcja misji {"type": "bridge", "op": "burn"}: rusza skrypt (bez skutku, gdy most już płonie lub spłonął). */
    public static void start(MinecraftServer server) {
        CompoundTag t = tag(server);
        ServerLevel level = Araluen.level(server);
        if (t == null || level == null || t.getInt("state") != INTACT) return;
        t.putInt("state", BURNING);
        t.putLong("start", level.getGameTime());
        LocationData.get(server).setDirty();
        runner = new Runner(level, t);
        burningNow = true;
        Zwiadowcy.LOGGER.info("Most nad Rozpadliną płonie");
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        runner = null;
        burningNow = false;
        if (state(event.getServer()) == BURNING) finishInstantly(event.getServer());
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        MinecraftServer server = event.getServer();
        CompoundTag t = tag(server);
        if (t == null) return;
        int st = t.getInt("state");
        ServerLevel level = Araluen.level(server);
        if (level == null) return;
        long age = level.getGameTime() - t.getLong("start");
        if (st == BURNING) {
            if (runner == null) {
                finishInstantly(server);
                return;
            }
            runner.step((int) age);
            if (age >= END_AT) {
                t.putInt("state", BURNT);
                LocationData.get(server).setDirty();
                runner = null;
                burningNow = false;
                for (ServerPlayer p : level.players()) QuestEngine.fireEvent(p, "most_runal", 1);
                Zwiadowcy.LOGGER.info("Most nad Rozpadliną runął");
            }
        } else if (st == BURNT && age < SMOKE_TICKS && age % 10 == 0) {
            smoke(server, level, t);
        }
    }

    /** Stan końcowy bez animacji (wczytanie gry w trakcie palenia, autotest). */
    public static void finishInstantly(MinecraftServer server) {
        CompoundTag t = tag(server);
        ServerLevel level = Araluen.level(server);
        if (t == null || level == null) return;
        for (String span : BridgeBuilder.SPANS) {
            for (BlockPos p : group(t, span)) if (!level.getBlockState(p).isAir()) level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
        }
        for (BlockPos p : group(t, BridgeBuilder.STUBS)) {
            BlockState st = level.getBlockState(p);
            BlockState c = charred(st);
            if (c != st) level.setBlock(p, c, 2);
        }
        if (t.getInt("state") != BURNT) {
            t.putInt("state", BURNT);
            t.putLong("start", level.getGameTime());
        }
        LocationData.get(server).setDirty();
        runner = null;
        burningNow = false;
    }

    private static void smoke(MinecraftServer server, ServerLevel level, CompoundTag t) {
        LocationData.Placed placed = LocationData.get(server).placed(BridgeBuilder.ID);
        if (placed == null) return;
        BlockPos c = placed.center;
        boolean watched = false;
        for (ServerPlayer p : level.players()) if (p.blockPosition().distSqr(c) < 200 * 200) watched = true;
        if (!watched) return;
        List<BlockPos> stubs = group(t, BridgeBuilder.STUBS);
        for (int i = 0; i < 3 && !stubs.isEmpty(); i++) {
            BlockPos p = stubs.get(level.random.nextInt(stubs.size()));
            level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, p.getX() + 0.5, p.getY() + 1, p.getZ() + 0.5, 1, 0.2, 0.2, 0.2, 0.01);
        }
        level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, c.getX() + 0.5, c.getY() - 30, c.getZ() + 0.5, 2, 6, 4, 6, 0.01);
    }

    /** Sesja zdjęciowa: wszystkie podpory polane i zapalone, skrypt rusza od razu (bez misji i gracza). */
    public static void startForPhoto(MinecraftServer server) {
        CompoundTag t = tag(server);
        ServerLevel level = Araluen.level(server);
        if (t == null || level == null || t.getInt("state") != INTACT) return;
        for (String s : BridgeBuilder.SUPPORTS) {
            oil(server, s);
            addName(t, "lit", s);
            for (BlockPos p : group(t, s)) {
                BlockState st = level.getBlockState(p);
                BlockState b = burnt(st);
                if (b != st) level.setBlock(p, b, 2);
            }
        }
        start(server);
    }

    /** Autotest: polanie i podpalenie wszystkich podpór, cały skrypt w jednym przebiegu, stan końcowy. */
    public static String simulateForTest(MinecraftServer server, List<String> problems) {
        CompoundTag t = tag(server);
        ServerLevel level = Araluen.level(server);
        if (t == null || level == null) return "Spalenie: brak danych mostu";
        for (String s : BridgeBuilder.SUPPORTS) oil(server, s);
        int oiled = 0;
        for (String s : BridgeBuilder.SUPPORTS) for (BlockPos p : group(t, s)) if (level.getBlockState(p).is(ModBlocks.OBLANE_DREWNO.get())) oiled++;
        for (String s : names(t, "oiled")) {
            addName(t, "lit", s);
            for (BlockPos p : group(t, s)) {
                BlockState st = level.getBlockState(p);
                BlockState b = burnt(st);
                if (b != st) level.setBlock(p, b, 2);
            }
        }
        int spanBlocks = 0;
        for (String s : BridgeBuilder.SPANS) for (BlockPos p : group(t, s)) if (!level.getBlockState(p).isAir()) spanBlocks++;
        t.putInt("state", BURNING);
        t.putLong("start", level.getGameTime());
        Runner r = new Runner(level, t);
        for (int age = 0; age <= END_AT; age++) r.step(age);
        int left = 0, charred = 0, burning = 0;
        for (String s : BridgeBuilder.SPANS) for (BlockPos p : group(t, s)) if (!level.getBlockState(p).isAir()) left++;
        for (BlockPos p : group(t, BridgeBuilder.STUBS)) {
            BlockState st = level.getBlockState(p);
            if (st.is(ModBlocks.ZWEGLONA_BELKA.get())) charred++;
            if (st.is(ModBlocks.PLONACA_BELKA.get())) burning++;
        }
        t.putInt("state", BURNT);
        LocationData.get(server).setDirty();
        if (oiled == 0) problems.add("Spalenie: polanie olejem nie zmieniło żadnego bloku");
        if (left > 0) problems.add("Spalenie: na przęsłach zostało " + left + " bloków");
        if (r.fallen < spanBlocks / 2) problems.add("Spalenie: spadło tylko " + r.fallen + " z " + spanBlocks + " bloków przęseł");
        if (burning > 0) problems.add("Spalenie: po skrypcie wciąż płonie " + burning + " bloków kikutów");
        if (charred == 0) problems.add("Spalenie: brak zwęglonych kikutów");
        return String.format("Spalenie (symulacja): polane %d bl., przęsła %d bl. → spadło %d, zostało %d; kikuty zwęglone %d, płonące %d",
                oiled, spanBlocks, r.fallen, left, charred, burning);
    }

    // ------------------------------------------------------------------ zamiana bloków

    private static BlockState withAxis(BlockState target, BlockState from) {
        if (target.hasProperty(RotatedPillarBlock.AXIS)) {
            return target.setValue(RotatedPillarBlock.AXIS,
                    from.hasProperty(RotatedPillarBlock.AXIS) ? from.getValue(RotatedPillarBlock.AXIS) : net.minecraft.core.Direction.Axis.Y);
        }
        return target;
    }

    /** Blok po dojściu ognia: drewno → płonąca belka, płoty i liny spłonęły, reszta bez zmian. */
    static BlockState burnt(BlockState st) {
        if (st.is(ModBlocks.OBLANE_DREWNO.get()) || st.is(BlockTags.LOGS) || st.is(BlockTags.PLANKS)) {
            return withAxis(ModBlocks.PLONACA_BELKA.get().defaultBlockState(), st);
        }
        if (st.is(BlockTags.WOODEN_FENCES) || st.is(ModBlocks.LINA.get())) return Blocks.AIR.defaultBlockState();
        return st;
    }

    /** Blok po zgaszeniu: płonąca lub nietknięta belka → zwęglona. */
    static BlockState charred(BlockState st) {
        if (st.is(ModBlocks.PLONACA_BELKA.get()) || st.is(ModBlocks.OBLANE_DREWNO.get()) || st.is(BlockTags.LOGS) || st.is(BlockTags.PLANKS)) {
            return withAxis(ModBlocks.ZWEGLONA_BELKA.get().defaultBlockState(), st);
        }
        if (st.is(BlockTags.WOODEN_FENCES) || st.is(ModBlocks.LINA.get())) return Blocks.AIR.defaultBlockState();
        return st;
    }

    /** Przebieg palenia w pamięci (kolejka zapłonu i kolejki spadających przęseł). */
    private static final class Runner {
        private final ServerLevel level;
        private final CompoundTag t;
        private final List<BlockPos> igniteOrder = new ArrayList<>();
        private int ignited;
        private final ArrayDeque<BlockPos> falling = new ArrayDeque<>();
        private int spansDropped;
        private int fallen;
        private boolean charredDone;

        Runner(ServerLevel level, CompoundTag t) {
            this.level = level;
            this.t = t;
            List<BlockPos> fires = new ArrayList<>();
            for (String s : names(t, "lit")) fires.addAll(group(t, s));
            if (fires.isEmpty()) for (String s : names(t, "oiled")) fires.addAll(group(t, s));
            Set<BlockPos> all = new LinkedHashSet<>();
            for (String s : BridgeBuilder.SPANS) all.addAll(group(t, s));
            all.addAll(group(t, BridgeBuilder.STUBS));
            Map<BlockPos, Double> dist = new HashMap<>();
            for (BlockPos p : all) {
                double best = Double.MAX_VALUE;
                for (BlockPos f : fires) best = Math.min(best, f.distSqr(p));
                dist.put(p, best);
            }
            igniteOrder.addAll(all);
            igniteOrder.sort(Comparator.comparingDouble(dist::get));
        }

        void step(int age) {
            // 0-8 s: ogień biegnie od podpór po pomoście
            int target = Math.min(igniteOrder.size(), (int) ((long) igniteOrder.size() * Math.min(age, IGNITE_TICKS) / IGNITE_TICKS));
            while (ignited < target) {
                BlockPos p = igniteOrder.get(ignited++);
                BlockState st = level.getBlockState(p);
                BlockState b = burnt(st);
                if (b != st) {
                    level.setBlock(p, b, 2);
                    if (level.random.nextInt(4) == 0) {
                        level.sendParticles(ParticleTypes.FLAME, p.getX() + 0.5, p.getY() + 1, p.getZ() + 0.5, 2, 0.3, 0.2, 0.3, 0.01);
                    }
                }
            }
            if (age % 20 == 0 && age < CHAR_AT) {
                BlockPos c = anchor(level.getServer(), "srodek");
                if (c != null) level.playSound(null, c, SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS, 3f, 0.6f);
            }
            // 8-30 s: przęsła odpadają kolejno
            while (spansDropped < FALL_ORDER.length && age >= FALL_START + spansDropped * SPAN_GAP) {
                dropSpan(BridgeBuilder.SPANS[FALL_ORDER[spansDropped]]);
                spansDropped++;
            }
            for (int i = 0; i < FALL_PER_TICK && !falling.isEmpty(); i++) {
                BlockPos p = falling.poll();
                BlockState st = level.getBlockState(p);
                if (st.isAir()) continue;
                FallingBlockEntity fb = FallingBlockEntity.fall(level, p, st);
                fallen++;
                fb.dropItem = false;
                if (!st.is(ModBlocks.PLONACA_BELKA.get())) fb.disableDrop();
            }
            // 30 s: zostają zwęglone kikuty
            if (!charredDone && age >= CHAR_AT) {
                charredDone = true;
                for (BlockPos p : group(t, BridgeBuilder.STUBS)) {
                    BlockState st = level.getBlockState(p);
                    BlockState c = charred(st);
                    if (c != st) level.setBlock(p, c, 2);
                }
                for (String s : BridgeBuilder.SPANS) {
                    for (BlockPos p : group(t, s)) {
                        if (!level.getBlockState(p).isAir()) level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }

        private void dropSpan(String span) {
            List<BlockPos> blocks = group(t, span);
            if (blocks.isEmpty()) return;
            // od góry, żeby poręcze i latarnie spadały razem z pomostem
            blocks.sort((x, y) -> Integer.compare(y.getY(), x.getY()));
            falling.addAll(blocks);
            AABB box = null;
            for (BlockPos p : blocks) {
                AABB b = new AABB(p).expandTowards(0, 3, 0);
                box = box == null ? b : box.minmax(b);
            }
            if (box == null) return;
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box)) {
                if (e instanceof Player pl && (pl.isCreative() || pl.isSpectator())) continue;
                e.setDeltaMovement((level.random.nextDouble() - 0.5) * 0.5, 0.25, (level.random.nextDouble() - 0.5) * 0.5);
                e.hurtMarked = true;
            }
            BlockPos c = blocks.get(blocks.size() / 2);
            level.playSound(null, c, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 2.5f, 0.6f);
            level.playSound(null, c, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 3f, 0.5f);
            level.sendParticles(ParticleTypes.LARGE_SMOKE, c.getX() + 0.5, c.getY() + 1, c.getZ() + 0.5, 40, 3, 1, 3, 0.05);
        }
    }

    private BridgeBurn() {}
}
