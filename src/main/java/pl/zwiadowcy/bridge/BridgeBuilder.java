package pl.zwiadowcy.bridge;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import pl.zwiadowcy.Zwiadowcy;
import pl.zwiadowcy.registry.ModBlocks;
import pl.zwiadowcy.world.LocationData;
import pl.zwiadowcy.world.VolumeTwoLayout;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Most Morgaratha nad Rozpadliną (plan 9.12). Nie jest szablonem: stawiamy go w poprzek kanionu w punkcie
 * {@link VolumeTwoLayout#BRIDGE}, dopasowując pomost do brzegów, które wygenerował teren.
 * <p>Most biegnie wzdłuż osi świata najbliższej kierunkowi „w poprzek kanionu” (u: od południa na północ,
 * v: w poprzek pomostu). Pomost ma 5 bloków szerokości i dzieli się na 4 przęsła. Podpory: 2 zastrzały pod pomostem
 * przy północnej ścianie (lina z pomostu do zejścia) i 2 kotwy wieży linowej w kształcie litery A na północnym brzegu.
 * <p>Pozycje bloków zapisujemy w danych świata (kategorie: przęsła, kikuty, grupy oleju), żeby skrypt spalenia
 * działał też po ponownym wczytaniu gry.
 */
public final class BridgeBuilder {
    public static final String ID = "most";
    static final int HALF = 2;

    /** Układ mostu w świecie: środek kanionu, oś i zwrot ku północy. */
    public record Frame(int cx, int cz, boolean axisX, int sign) {
        public BlockPos pos(int u, int y, int v) {
            return axisX ? new BlockPos(cx + sign * u, y, cz + v) : new BlockPos(cx + v, y, cz + sign * u);
        }

        Direction.Axis along() {
            return axisX ? Direction.Axis.X : Direction.Axis.Z;
        }

        Direction.Axis across() {
            return axisX ? Direction.Axis.Z : Direction.Axis.X;
        }

        /** Wektory świata: v (wzdłuż kanionu) i u (na północ, w poprzek kanionu). */
        public double[] vectors() {
            return axisX ? new double[]{0, 1, sign, 0} : new double[]{1, 0, 0, sign};
        }

        static Frame read(CompoundTag t) {
            return new Frame(t.getInt("cx"), t.getInt("cz"), t.getBoolean("axisX"), t.getInt("sign"));
        }

        void write(CompoundTag t) {
            t.putInt("cx", cx);
            t.putInt("cz", cz);
            t.putBoolean("axisX", axisX);
            t.putInt("sign", sign);
        }
    }

    /** Kategorie bloków mostu zapisywane w danych świata. */
    public static final String[] SPANS = {"przeslo_1", "przeslo_2", "przeslo_3", "przeslo_4"};
    public static final String STUBS = "kikuty";
    public static final String[] SUPPORTS = {"podpora_1", "podpora_2", "podpora_3", "podpora_4"};

    private final ServerLevel level;
    private final Frame f;
    private final Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
    private final Map<String, List<BlockPos>> groups = new LinkedHashMap<>();
    private final Map<String, BlockPos> anchors = new LinkedHashMap<>();
    private final Set<BlockPos> clear = new LinkedHashSet<>();
    private int deckY, sEnd, nEnd;

    private BridgeBuilder(ServerLevel level, Frame f) {
        this.level = level;
        this.f = f;
    }

    /** Stawia most (raz na świat). Zwraca lokację „most” z kotwicami. */
    @Nullable
    public static LocationData.Placed build(ServerLevel level, LocationData data) {
        double[] c = VolumeTwoLayout.toWorld(VolumeTwoLayout.BRIDGE[0], VolumeTwoLayout.BRIDGE[1]);
        double[] dir = VolumeTwoLayout.bridgeDirection();
        // „na północ, w poprzek kanionu” w układzie krain, obrócone do świata
        double[] north = VolumeTwoLayout.toWorld(dir[1], -dir[0]);
        boolean axisX = Math.abs(north[0]) >= Math.abs(north[1]);
        int sign = (int) Math.signum(axisX ? north[0] : north[1]);
        if (sign == 0) sign = -1;
        BridgeBuilder b = new BridgeBuilder(level, new Frame((int) Math.round(c[0]), (int) Math.round(c[1]), axisX, sign));
        long t0 = System.currentTimeMillis();
        b.measure();
        b.design();
        b.place();
        LocationData.Placed placed = new LocationData.Placed(b.f.pos((b.sEnd + b.nEnd) / 2, b.deckY, 0), b.f.pos(b.sEnd, b.deckY, 0));
        placed.anchors.putAll(b.anchors);
        CompoundTag tag = data.extra(ID);
        b.f.write(tag);
        tag.putInt("deckY", b.deckY);
        tag.putInt("sEnd", b.sEnd);
        tag.putInt("nEnd", b.nEnd);
        CompoundTag g = new CompoundTag();
        b.groups.forEach((k, list) -> g.put(k, new LongArrayTag(list.stream().mapToLong(BlockPos::asLong).toArray())));
        tag.put("groups", g);
        tag.putInt("state", BridgeBurn.INTACT);
        data.setDirty();
        Zwiadowcy.LOGGER.info("Most nad Rozpadliną: pomost y={}, długość {}, oś {}, {} bloków ({} ms)", b.deckY, b.nEnd - b.sEnd - 1,
                axisX ? "x" : "z", b.blocks.size(), System.currentTimeMillis() - t0);
        return placed;
    }

    public static Frame frame(CompoundTag tag) {
        return Frame.read(tag);
    }

    // ------------------------------------------------------------------ pomiar brzegów

    private final int[] surface = new int[161];

    private int s(int u) {
        return surface[Math.max(0, Math.min(160, u + 80))];
    }

    private int ground(int u, int v) {
        BlockPos p = f.pos(u, 0, v);
        // bez wczytanego chunka getHeight zwraca dno świata - wymuszamy wygenerowanie
        level.getChunk(p.getX() >> 4, p.getZ() >> 4);
        return level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, p.getX(), p.getZ()) - 1;
    }

    private void measure() {
        for (int u = -80; u <= 80; u++) {
            int m = Integer.MAX_VALUE;
            for (int v = -HALF; v <= HALF; v += HALF) m = Math.min(m, ground(u, v));
            surface[u + 80] = m;
        }
        int u0 = 0;
        for (int u = -35; u <= 35; u++) if (s(u) < s(u0)) u0 = u;
        int maxN = s(u0), maxS = s(u0);
        for (int u = u0; u <= u0 + 50; u++) maxN = Math.max(maxN, s(u));
        for (int u = u0; u >= u0 - 50; u--) maxS = Math.max(maxS, s(u));
        deckY = Math.min(maxN, maxS);
        if (deckY - s(u0) < 6) {
            Zwiadowcy.LOGGER.warn("Pod mostem nie ma kanionu (dno y={}, brzegi y={}) - most stanie nad terenem", s(u0), deckY);
            deckY = s(u0) + 6;
        }
        nEnd = u0;
        while (nEnd < u0 + 60 && s(nEnd) < deckY) nEnd++;
        sEnd = u0;
        while (sEnd > u0 - 60 && s(sEnd) < deckY) sEnd--;
    }

    // ------------------------------------------------------------------ projekt

    private static final BlockState PLANKS_A = Blocks.SPRUCE_PLANKS.defaultBlockState();
    private static final BlockState PLANKS_B = Blocks.DARK_OAK_PLANKS.defaultBlockState();
    private static final BlockState FENCE = Blocks.SPRUCE_FENCE.defaultBlockState();

    private static BlockState log(net.minecraft.world.level.block.Block b, Direction.Axis axis) {
        return b.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
    }

    private void put(String group, int u, int y, int v, BlockState state) {
        BlockPos p = f.pos(u, y, v);
        blocks.put(p, state);
        groups.computeIfAbsent(group, k -> new ArrayList<>()).add(p);
    }

    private String spanOf(int u) {
        int len = nEnd - sEnd - 1;
        int i = (int) Math.floor((u - sEnd - 1) * 4.0 / Math.max(1, len));
        return SPANS[Math.max(0, Math.min(3, i))];
    }

    private void design() {
        int strutTop = strutTop();
        int ropeU = strutTop + 4;
        int len = nEnd - sEnd - 1;
        // pomost: od 3 bloków przed południowym brzegiem do 3 bloków za północnym
        for (int u = sEnd - 3; u <= nEnd + 3; u++) {
            boolean span = u > sEnd && u < nEnd;
            String g = span ? spanOf(u) : STUBS;
            boolean cross = Math.floorMod(u, 5) == 0;
            for (int v = -HALF; v <= HALF; v++) {
                BlockState st;
                if (cross) st = log(Blocks.DARK_OAK_LOG, f.across());
                else if (Math.abs(v) == HALF) st = log(Blocks.STRIPPED_DARK_OAK_LOG, f.along());
                else st = Math.floorMod(u, 3) == 0 ? PLANKS_B : PLANKS_A;
                put(g, u, deckY, v, st);
                for (int y = deckY + 1; y <= deckY + 4; y++) clear.add(f.pos(u, y, v));
            }
            if (span) {
                // podłużnice pod pomostem
                for (int v : new int[]{-1, 1}) put(g, u, deckY - 1, v, log(Blocks.SPRUCE_LOG, f.along()));
            }
            // poręcze: płot, co 5 bloków słupek, co 10 latarnia; przy północnym końcu przerwy na zejście po linie
            for (int v : new int[]{-HALF, HALF}) {
                boolean gap = span && (u == ropeU || u == ropeU + 1);
                if (gap) continue;
                if (cross) {
                    put(g, u, deckY + 1, v, log(Blocks.DARK_OAK_LOG, Direction.Axis.Y));
                    if (Math.floorMod(u, 10) == 0) put(g, u, deckY + 2, v, Blocks.LANTERN.defaultBlockState());
                } else {
                    put(g, u, deckY + 1, v, FENCE);
                }
            }
        }
        // zastrzały pod pomostem przy północnej ścianie (podpora_1: strona v<0, podpora_2: v>0) i liny do zejścia
        for (int side = 0; side < 2; side++) {
            int v = side == 0 ? -HALF : HALF;
            String g = SUPPORTS[side];
            int top = strutTop;
            BlockPos mid = null;
            for (int k = 0; k <= 16; k++) {
                int u = top + k, y = deckY - 1 - k;
                if (u > nEnd + 2 || ground(u, v) >= y) break;
                put(g, u, y, v, log(Blocks.DARK_OAK_LOG, Direction.Axis.Y));
                put(spanOf(Math.min(u, nEnd - 1)), u, y, v, log(Blocks.DARK_OAK_LOG, Direction.Axis.Y));
                if (k == 4) mid = f.pos(u, y, v);
            }
            if (mid == null) mid = f.pos(top + 2, deckY - 3, v);
            anchors.put(g, mid);
            int ropeV = side == 0 ? -HALF - 1 : HALF + 1;
            for (int y = deckY; y >= deckY - 7; y--) put(spanOf(ropeU), ropeU, y, ropeV, ModBlocks.LINA.get().defaultBlockState());
        }
        // wieże linowe (A) na obu brzegach; kotwy północnej wieży to podpora_3 (v<0) i podpora_4 (v>0)
        tower(nEnd + 3, true);
        tower(sEnd - 3, false);
        // droga dojazdowa: wcięcie w brzeg i łagodny podjazd
        approach(nEnd, 1);
        approach(sEnd, -1);
        // kotwice
        int mid = (sEnd + nEnd) / 2;
        anchors.put("srodek", f.pos(mid, deckY + 1, 0));
        anchors.put("polnoc", f.pos(nEnd + 6, deckY + 1, 0));
        anchors.put("poludnie", f.pos(sEnd - 6, deckY + 1, 0));
        anchors.put("brzeg_polnocny", f.pos(nEnd + 12, deckY + 1, 0));
        anchors.put("brzeg_poludniowy", f.pos(sEnd - 10, deckY + 1, 0));
        for (int i = 0; i < 4; i++) {
            int u = sEnd + 1 + (int) Math.round((i + 0.5) * len / 4.0);
            anchors.put(SPANS[i], f.pos(u, deckY + 1, 0));
        }
        int[] col = {sEnd - 14, sEnd + 2, mid, nEnd - 6};
        for (int i = 0; i < col.length; i++) anchors.put("kolumna_" + (i + 1), f.pos(col[i], deckY + 1, 0));
        anchors.put("kolumna_5", f.pos(nEnd + 14, deckY + 1, 0));
        anchors.put("patrol_1", f.pos(nEnd - 3, deckY + 1, 1));
        anchors.put("patrol_2", f.pos(mid, deckY + 1, -1));
    }

    /**
     * Początek zastrzałów: belka schodzi pod kątem 45° od pomostu ku północnej ścianie kanionu. Szukamy miejsca, w którym
     * po obu stronach pomostu zastrzał ma co najmniej 6 belek, zanim oprze się o ścianę (ściany bywają łagodne).
     */
    private int strutTop() {
        for (int top = nEnd - 6; top >= Math.max(sEnd + 2, nEnd - 34); top--) {
            if (Math.min(strutLength(top, -HALF), strutLength(top, HALF)) >= 6) return top;
        }
        return nEnd - 9;
    }

    private int strutLength(int top, int v) {
        for (int k = 0; k <= 16; k++) {
            int u = top + k, y = deckY - 1 - k;
            if (u > nEnd + 2 || ground(u, v) >= y) return k;
        }
        return 17;
    }

    private void tower(int u, boolean north) {
        int base = deckY + 1;
        String g = STUBS;
        for (int side = -1; side <= 1; side += 2) {
            // nogi pochylone do środka: 4,4,4,3,3,3,2,2,2,1 i belka na szczycie
            for (int h = 0; h <= 9; h++) {
                int v = side * (h <= 2 ? 4 : h <= 5 ? 3 : h <= 8 ? 2 : 1);
                put(g, u, base + h, v, log(Blocks.DARK_OAK_LOG, Direction.Axis.Y));
                if (north) groups.computeIfAbsent(SUPPORTS[side < 0 ? 2 : 3], k -> new ArrayList<>()).add(f.pos(u, base + h, v));
            }
            // noga sięga gruntu
            for (int y = base - 1; y > ground(u, side * 4) && y > base - 8; y--) put(g, u, y, side * 4, log(Blocks.DARK_OAK_LOG, Direction.Axis.Y));
            // kotwa: pal wbity za wieżą i odciąg z łańcucha do szczytu
            int stakeU = north ? u + 5 : u - 5;
            int gy = ground(stakeU, side * 4);
            for (int y = gy + 1; y <= gy + 2; y++) {
                put(g, stakeU, y, side * 4, log(Blocks.DARK_OAK_LOG, Direction.Axis.Y));
                if (north) groups.computeIfAbsent(SUPPORTS[side < 0 ? 2 : 3], k -> new ArrayList<>()).add(f.pos(stakeU, y, side * 4));
            }
            if (north) anchors.put(SUPPORTS[side < 0 ? 2 : 3], f.pos(stakeU, gy + 1, side * 4));
            chain(g, stakeU, gy + 3, u, base + 9, side * 2);
        }
        for (int v = -1; v <= 1; v++) put(g, u, base + 10, v, log(Blocks.DARK_OAK_LOG, f.across()));
        for (int v = -3; v <= 3; v++) put(g, u, base + 5, v, log(Blocks.SPRUCE_LOG, f.across()));
        // liny nośne ze szczytu do pomostu (nad poręczami)
        int len = nEnd - sEnd - 1;
        int target = north ? nEnd - Math.max(6, len / 3) : sEnd + Math.max(6, len / 3);
        for (int v : new int[]{-HALF, HALF}) chain(north ? SPANS[3] : SPANS[0], u, base + 9, target, deckY + 2, v);
    }

    /** Łańcuch (lina) jako schodkowa linia między dwoma punktami w płaszczyźnie (u, y). */
    private void chain(String g, int u0, int y0, int u1, int y1, int v) {
        int steps = Math.max(Math.abs(u1 - u0), Math.abs(y1 - y0));
        for (int i = 1; i < steps; i++) {
            int u = u0 + Math.round((u1 - u0) * i / (float) steps), y = y0 + Math.round((y1 - y0) * i / (float) steps);
            BlockPos p = f.pos(u, y, v);
            if (!blocks.containsKey(p)) put(g, u, y, v, Blocks.CHAIN.defaultBlockState().setValue(RotatedPillarBlock.AXIS, f.along()));
        }
    }

    /** Droga z pomostu w głąb brzegu: wcięcie na poziomie pomostu, potem podjazd 1:1 do terenu. */
    private void approach(int end, int dir) {
        for (int i = 4; i <= 24; i++) {
            int u = end + dir * i;
            int path = deckY + Math.max(0, (i - 8) / 2);
            boolean higher = false;
            for (int v = -3; v <= 3; v++) {
                int g = ground(u, v);
                if (g > path) higher = true;
                for (int y = path + 1; y <= Math.min(g, path + 5); y++) clear.add(f.pos(u, y, v));
                if (Math.abs(v) <= HALF && g >= path) blocks.putIfAbsent(f.pos(u, path, v), Blocks.DIRT_PATH.defaultBlockState());
                else if (Math.abs(v) <= HALF && g >= path - 3) {
                    for (int y = g + 1; y < path; y++) blocks.putIfAbsent(f.pos(u, y, v), Blocks.DIRT.defaultBlockState());
                    blocks.putIfAbsent(f.pos(u, path, v), Blocks.DIRT_PATH.defaultBlockState());
                }
            }
            if (!higher && i > 8) break;
        }
    }

    // ------------------------------------------------------------------ stawianie

    private void place() {
        for (BlockPos p : clear) {
            if (blocks.containsKey(p)) continue;
            BlockState old = level.getBlockState(p);
            if (!old.isAir()) level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
        }
        for (Map.Entry<BlockPos, BlockState> e : blocks.entrySet()) level.setBlock(e.getKey(), e.getValue(), 2);
        // płoty i łańcuchy łączą się z sąsiadami dopiero po aktualizacji kształtu
        for (BlockPos p : blocks.keySet()) {
            BlockState st = level.getBlockState(p);
            BlockState upd = net.minecraft.world.level.block.Block.updateFromNeighbourShapes(st, level, p);
            if (upd != st) level.setBlock(p, upd, 2);
        }
    }

}
