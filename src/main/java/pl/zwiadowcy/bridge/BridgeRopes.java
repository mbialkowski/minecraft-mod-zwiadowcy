package pl.zwiadowcy.bridge;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import pl.zwiadowcy.Zwiadowcy;
import pl.zwiadowcy.registry.ModBlocks;
import pl.zwiadowcy.world.LocationData;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Liny wież mostu: z każdej wieży dwie liny nośne do słupków poręczy i dwa odciągi do pali kotwy. Ukośna lina z bloków
 * (dawniej łańcuchy) była schodkami poziomych kresek, które z daleka wyglądały jak niedorysowany most - teraz każda lina
 * to prosty odcinek z kawałków bloku liny (block_display obrócony wzdłuż liny).
 * <p>Liny stawiamy, gdy gracz jest przy moście (encje potrzebują wczytanych chunków), raz na świat; w starszym świecie
 * przy okazji znikają stare łańcuchy. Przy pożarze liny przęsła znikają razem z nim, odciągi - gdy most dogasa.
 */
public final class BridgeRopes {
    static final String TAG = "zw_most_lina";
    private static final String DONE = "liny";
    /** Długość kawałka liny (tekstura nie rozciąga się za bardzo) i grubość (lina z bloku ma 2 piksele - za cienka z daleka). */
    private static final double PIECE = 1.25;
    private static final float WIDTH = 2.2f;

    record Rope(String group, Vec3 from, Vec3 to) {
        double length() {
            return to.distanceTo(from);
        }
    }

    /** Liny wyliczone z danych mostu (ten sam układ dla nowego i starszego świata). */
    static List<Rope> ropes(ServerLevel level, CompoundTag t) {
        BridgeBuilder.Frame f = BridgeBuilder.frame(t);
        int deckY = t.getInt("deckY"), sEnd = t.getInt("sEnd"), nEnd = t.getInt("nEnd");
        int base = deckY + 1;
        List<BlockPos> stubs = BridgeBurn.group(t, BridgeBuilder.STUBS);
        List<Rope> out = new ArrayList<>();
        for (boolean north : new boolean[]{true, false}) {
            int dir = north ? 1 : -1;
            int u = north ? nEnd + 3 : sEnd - 3;
            int target = BridgeBuilder.ropeTarget(sEnd, nEnd, north);
            String span = north ? BridgeBuilder.SPANS[3] : BridgeBuilder.SPANS[0];
            for (int side = -1; side <= 1; side += 2) {
                // lina nośna: od zewnętrznej strony nogi pod szczytem do wierzchu słupka poręczy (bez słupka - do pomostu)
                boolean post = level.getBlockState(f.pos(target, deckY + 1, side * BridgeBuilder.HALF)).is(BlockTags.LOGS);
                out.add(new Rope(span, f.point(u, base + 9.5, side * 1.5),
                        f.point(target, post ? deckY + 2.0 : deckY + 1.0, side * BridgeBuilder.HALF)));
                // odciąg: od wierzchu pala kotwy do tylnej ściany nogi wieży
                int stakeU = u + 5 * dir, top = Integer.MIN_VALUE;
                for (BlockPos p : stubs) {
                    int[] uv = f.uv(p);
                    if (uv[0] == stakeU && uv[1] == side * 4) top = Math.max(top, p.getY());
                }
                if (top != Integer.MIN_VALUE) {
                    out.add(new Rope(BridgeBuilder.STUBS, f.point(stakeU, top + 1.0, side * 4), f.point(u + 0.5 * dir, base + 8.5, side * 2)));
                }
            }
        }
        return out;
    }

    /** Co 2 s z BridgeBurn: liny stają, gdy gracz jest przy całym moście; po pożarze sprzątamy resztki z wczytanych chunków. */
    static void tick(MinecraftServer server, ServerLevel level, CompoundTag t) {
        LocationData.Placed placed = LocationData.get(server).placed(BridgeBuilder.ID);
        if (placed == null) return;
        boolean near = false;
        for (ServerPlayer p : level.players()) if (p.blockPosition().distSqr(placed.center) < 192 * 192) near = true;
        if (!near) return;
        int state = t.getInt("state");
        if (state == BridgeBurn.BURNT) {
            if (level.getGameTime() % 200 == 0) discard(level, null, false);
            return;
        }
        if (state != BridgeBurn.INTACT || t.getBoolean(DONE)) return;
        List<Rope> ropes = ropes(level, t);
        for (Rope r : ropes) {
            for (Vec3 p : new Vec3[]{r.from, r.to, r.from.add(r.to).scale(0.5)}) {
                BlockPos b = BlockPos.containing(p);
                if (!level.areEntitiesLoaded(ChunkPos.asLong(b.getX() >> 4, b.getZ() >> 4))) return;
            }
        }
        // starszy świat: łańcuchy zamiast lin
        int chains = 0;
        for (String g : t.getCompound("groups").getAllKeys()) {
            for (BlockPos p : BridgeBurn.group(t, g)) {
                if (level.getBlockState(p).is(Blocks.CHAIN)) {
                    level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                    chains++;
                }
            }
        }
        discard(level, null, false);
        int pieces = 0;
        for (Rope r : ropes) pieces += spawn(level, r);
        t.putBoolean(DONE, true);
        LocationData.get(server).setDirty();
        Zwiadowcy.LOGGER.info("Most nad Rozpadliną: {} lin ({} odcinków){}", ropes.size(), pieces,
                chains > 0 ? ", usunięto " + chains + " starych łańcuchów" : "");
    }

    /** Lina z kawałków: każdy to blok liny (krzyż z pionowym pasem) obrócony z pionu na kierunek liny. */
    static int spawn(ServerLevel level, Rope r) {
        Vec3 d = r.to.subtract(r.from);
        double len = d.length();
        if (len < 0.2) return 0;
        Vec3 n = d.scale(1 / len);
        int pieces = Math.max(1, (int) Math.ceil(len / PIECE));
        double s = len / pieces;
        Quaternionf q = new Quaternionf().rotationTo(0f, 1f, 0f, (float) n.x, (float) n.y, (float) n.z);
        // środek modelu (0.5, 0, 0.5) po skali i obrocie ma trafić w początek kawałka
        Vector3f center = q.transform(new Vector3f(0.5f * WIDTH, 0f, 0.5f * WIDTH));
        int spawned = 0;
        for (int i = 0; i < pieces; i++) {
            Vec3 a = r.from.add(n.scale(s * i));
            Vec3 mid = a.add(n.scale(s / 2));
            CompoundTag nbt = new CompoundTag();
            nbt.putString("id", "minecraft:block_display");
            nbt.put("block_state", NbtUtils.writeBlockState(ModBlocks.LINA.get().defaultBlockState()));
            nbt.putFloat("view_range", 3f);
            // jak pod gołym niebem: kawałek w cieniu pomostu albo przy skale nie robi się czarny
            CompoundTag light = new CompoundTag();
            light.putInt("sky", 15);
            light.putInt("block", 0);
            nbt.put("brightness", light);
            CompoundTag tr = new CompoundTag();
            tr.put("translation", floats((float) (a.x - mid.x) - center.x, (float) (a.y - mid.y) - center.y, (float) (a.z - mid.z) - center.z));
            tr.put("left_rotation", floats(q.x, q.y, q.z, q.w));
            tr.put("scale", floats(WIDTH, (float) s, WIDTH));
            tr.put("right_rotation", floats(0f, 0f, 0f, 1f));
            nbt.put("transformation", tr);
            ListTag tags = new ListTag();
            tags.add(StringTag.valueOf(TAG));
            nbt.put("Tags", tags);
            Entity e = EntityType.loadEntityRecursive(nbt, level, ent -> {
                ent.moveTo(mid.x, mid.y, mid.z, 0f, 0f);
                return ent;
            });
            if (e == null) continue;
            e.getPersistentData().putString(TAG, r.group);
            if (level.addFreshEntity(e)) spawned++;
        }
        return spawned;
    }

    /** Zdejmuje liny (wszystkie albo z jednej grupy bloków mostu) z wczytanych chunków; flames = iskry w miejscu liny. */
    static void discard(ServerLevel level, @Nullable String group, boolean flames) {
        for (Display.BlockDisplay d : level.getEntities(EntityTypeTest.forClass(Display.BlockDisplay.class),
                d -> d.getTags().contains(TAG) && (group == null || group.equals(d.getPersistentData().getString(TAG))))) {
            if (flames) level.sendParticles(ParticleTypes.FLAME, d.getX(), d.getY(), d.getZ(), 2, 0.2, 0.2, 0.2, 0.01);
            d.discard();
        }
    }

    /** Autotest: liczba lin, długości i czy wieże nie stoją w zboczu (nad pomostem przy wieży nie ma terenu). */
    public static String check(ServerLevel level, CompoundTag t, List<String> problems) {
        List<Rope> ropes = ropes(level, t);
        int deck = 0, guys = 0;
        double shortest = Double.MAX_VALUE;
        for (Rope r : ropes) {
            if (r.group.equals(BridgeBuilder.STUBS)) guys++;
            else deck++;
            shortest = Math.min(shortest, r.length());
        }
        if (deck != 4) problems.add("Most: lin nośnych " + deck + " zamiast 4");
        if (guys != 4) problems.add("Most: odciągów wież " + guys + " zamiast 4");
        if (shortest < 2) problems.add(String.format("Most: za krótka lina (%.1f bl.)", shortest));
        BridgeBuilder.Frame f = BridgeBuilder.frame(t);
        int deckY = t.getInt("deckY");
        int buried = 0;
        for (int u : new int[]{t.getInt("nEnd") + 3, t.getInt("sEnd") - 3}) {
            for (int y = deckY + 1; y <= deckY + 12; y++) {
                for (int v = -5; v <= 5; v++) {
                    var st = level.getBlockState(f.pos(u, y, v));
                    if (!st.isAir() && !st.is(BlockTags.LOGS) && !st.is(Blocks.LANTERN) && !st.is(BlockTags.WOODEN_FENCES)) buried++;
                }
            }
        }
        if (buried > 0) problems.add("Most: wieża linowa tkwi w terenie (" + buried + " bl. obok wież)");
        // próbne postawienie pierwszej liny (gdy encje tego chunka są wczytane) i sprzątnięcie
        String trial = "próba: chunk bez encji";
        if (!ropes.isEmpty()) {
            Rope r = ropes.get(0);
            BlockPos b = BlockPos.containing(r.from.add(r.to).scale(0.5));
            if (level.areEntitiesLoaded(ChunkPos.asLong(b.getX() >> 4, b.getZ() >> 4))) {
                int want = Math.max(1, (int) Math.ceil(r.length() / PIECE)), got = spawn(level, r);
                int found = level.getEntities(EntityTypeTest.forClass(Display.BlockDisplay.class), d -> d.getTags().contains(TAG)).size();
                discard(level, null, false);
                if (got != want || found < want) problems.add("Most: lina z " + got + "/" + want + " odcinków, w świecie " + found);
                trial = "próba: " + got + "/" + want + " odcinków";
            }
        }
        return String.format("Liny: %d nośne, %d odciągi, najkrótsza %.1f bl.; teren przy wieżach: %d bl.; %s", deck, guys, shortest, buried, trial);
    }

    private static ListTag floats(float... values) {
        ListTag l = new ListTag();
        for (float v : values) l.add(FloatTag.valueOf(v));
        return l;
    }

    private BridgeRopes() {}
}
