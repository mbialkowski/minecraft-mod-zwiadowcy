package pl.zwiadowcy.assetgen;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Elementy wystroju wnętrz i obejść: kominki, dywany, stoły, żyrandole, kociołki, obrazy, ramki…
 * Kierunek "facing" oznacza zawsze stronę, w którą element jest zwrócony (do wnętrza pomieszczenia).
 */
final class Decor {

    static int dx(String dir) {
        return switch (dir) { case "east" -> 1; case "west" -> -1; default -> 0; };
    }

    static int dz(String dir) {
        return switch (dir) { case "south" -> 1; case "north" -> -1; default -> 0; };
    }

    static String opposite(String dir) {
        return switch (dir) { case "north" -> "south"; case "south" -> "north"; case "east" -> "west"; default -> "east"; };
    }

    /** Kierunek "w lewo" patrząc w stronę facing (odpowiednik counterClockWise w grze). */
    static String ccw(String dir) {
        return switch (dir) { case "north" -> "west"; case "west" -> "south"; case "south" -> "east"; default -> "north"; };
    }

    /**
     * Kominek z cegły: palenisko (ognisko) w (x,y,z), boki, nadproże, gzyms z odwróconych schodów, świeca i doniczka,
     * komin od nadproża aż do topY (przebija dach).
     */
    static void fireplace(Structure s, int x, int y, int z, String facing, int topY, String brick) {
        int px = dx(ccw(facing)), pz = dz(ccw(facing));
        int fx = dx(facing), fz = dz(facing);
        int bx = -fx, bz = -fz;
        s.set(x, y, z, "minecraft:campfire[lit=true,facing=" + facing + ",signal_fire=false]");
        for (int side : new int[]{-1, 1}) {
            s.set(x + px * side, y, z + pz * side, brick);
            s.set(x + px * side, y + 1, z + pz * side, brick);
        }
        for (int side = -1; side <= 1; side++) {
            s.set(x + px * side, y + 2, z + pz * side, brick);
            s.set(x + px * side, y - 1, z + pz * side, brick);
            s.set(x + px * side + bx, y - 1, z + pz * side + bz, brick);
        }
        s.set(x + fx, y - 1, z + fz, brick);
        for (int yy = y; yy <= topY; yy++) s.set(x + bx, yy, z + bz, brick);
        for (int yy = y + 3; yy <= topY; yy++) s.set(x, yy, z, brick);
        String stairBlock = switch (brick) {
            case "minecraft:bricks" -> "minecraft:brick_stairs";
            case "minecraft:cobblestone" -> "minecraft:cobblestone_stairs";
            case "minecraft:mossy_cobblestone" -> "minecraft:mossy_cobblestone_stairs";
            case "minecraft:deepslate_bricks" -> "minecraft:deepslate_brick_stairs";
            default -> "minecraft:stone_brick_stairs";
        };
        for (int side = -1; side <= 1; side++) {
            s.set(x + px * side + fx, y + 2, z + pz * side + fz, stairBlock + "[facing=" + opposite(facing) + ",half=top]");
        }
        s.set(x + px + fx, y + 3, z + pz + fz, "minecraft:candle[candles=3,lit=true]");
        s.set(x - px + fx, y + 3, z - pz + fz, "minecraft:potted_fern");
        s.set(x + fx * 2, y, z + fz * 2, "minecraft:brown_carpet");
    }

    /** Dywan z obwódką; pomija pola już zajęte (meble). */
    static void rug(Structure s, int x0, int z0, int x1, int z1, int y, String border, String fill) {
        for (int x = Math.min(x0, x1); x <= Math.max(x0, x1); x++) {
            for (int z = Math.min(z0, z1); z <= Math.max(z0, z1); z++) {
                if (s.has(x, y, z)) continue;
                boolean edge = x == Math.min(x0, x1) || x == Math.max(x0, x1) || z == Math.min(z0, z1) || z == Math.max(z0, z1);
                s.set(x, y, z, "minecraft:" + (edge ? border : fill) + "_carpet");
            }
        }
    }

    /** Stół (płot + blat) z opcjonalnym przedmiotem na blacie zamiast blatu. */
    static void table(Structure s, int x, int y, int z, String wood, String onTop) {
        s.set(x, y, z, "minecraft:" + wood + "_fence");
        s.set(x, y + 1, z, onTop != null ? onTop : "minecraft:" + wood + "_pressure_plate");
    }

    /** Krzesło (schody) - siedzący patrzy w kierunku "looks". */
    static void chair(Structure s, int x, int y, int z, String wood, String looks) {
        if (s.has(x, y, z)) return;
        s.set(x, y, z, "minecraft:" + wood + "_stairs[facing=" + opposite(looks) + ",half=bottom]");
    }

    /** Stół z czterema krzesłami dookoła. */
    static void tableSet(Structure s, int x, int y, int z, String wood, String onTop) {
        table(s, x, y, z, wood, onTop);
        chair(s, x + 1, y, z, wood, "west");
        chair(s, x - 1, y, z, wood, "east");
        chair(s, x, y, z + 1, wood, "north");
        chair(s, x, y, z - 1, wood, "south");
    }

    /** Żyrandol: łańcuch od sufitu (yTop) w dół i latarnia na końcu. */
    static void chandelier(Structure s, int x, int z, int yTop, int yLantern, boolean soul) {
        for (int y = yLantern + 1; y <= yTop; y++) s.set(x, y, z, "minecraft:chain[axis=y]");
        s.set(x, yLantern, z, "minecraft:" + (soul ? "soul_" : "") + "lantern[hanging=true]");
    }

    /** Kociołek z wodą zawieszony nad ogniskiem - gotowanie strawy. */
    static void cookingPot(Structure s, int x, int y, int z) {
        s.set(x, y, z, "minecraft:campfire[lit=true,facing=north,signal_fire=false]");
        s.set(x, y + 1, z, "minecraft:water_cauldron[level=3]");
    }

    /** Ognisko z kamiennym kręgiem i ławami z bali. */
    static void campfireRing(Structure s, int x, int y, int z, boolean cooking) {
        if (cooking) cookingPot(s, x, y, z);
        else s.set(x, y, z, "minecraft:campfire[lit=true,facing=north,signal_fire=false]");
        for (int[] d : new int[][]{{-1, -1}, {1, -1}, {-1, 1}, {1, 1}}) s.set(x + d[0], y - 1, z + d[1], "minecraft:cobblestone");
        for (int[] d : new int[][]{{0, -1}, {0, 1}, {-1, 0}, {1, 0}}) s.set(x + d[0], y - 1, z + d[1], "minecraft:gravel");
        s.set(x - 3, y, z, "minecraft:spruce_log[axis=z]");
        s.set(x + 3, y, z, "minecraft:spruce_log[axis=z]");
        s.set(x, y, z + 3, "minecraft:spruce_log[axis=x]");
    }

    /** Beczki ułożone na boku jak kegi (rząd wzdłuż osi). */
    static void kegs(Structure s, int x, int y, int z, int n, boolean alongX, String facing) {
        for (int i = 0; i < n; i++) {
            int kx = alongX ? x + i : x, kz = alongX ? z : z + i;
            s.set(kx, y, kz, "minecraft:barrel[facing=" + facing + ",open=false]");
        }
    }

    static void pot(Structure s, int x, int y, int z, String plant) {
        s.set(x, y, z, "minecraft:potted_" + plant);
    }

    /** Półka z półpłyty przy ścianie, z drobiazgami na wierzchu. */
    static void shelf(Structure s, int x, int y, int z, String wood, String item) {
        s.set(x, y, z, "minecraft:" + wood + "_slab[type=top]");
        if (item != null) s.set(x, y + 1, z, item);
    }

    static void decoratedPot(Structure s, int x, int y, int z) {
        s.set(x, y, z, "minecraft:decorated_pot[facing=north,waterlogged=false]");
    }

    // ------------------------------------------------------------------ byty dekoracyjne

    private static byte facing2D(String dir) {
        return switch (dir) { case "south" -> 0; case "west" -> 1; case "north" -> 2; default -> 3; };
    }

    private static byte facing3D(String dir) {
        return switch (dir) { case "north" -> 2; case "south" -> 3; case "west" -> 4; default -> 5; };
    }

    /**
     * Obraz w bloku (x,y,z) (powietrze przed ścianą), zwrócony w stronę facing.
     * Obrazy 2 wysokie zajmują też blok nad, 2 szerokie - blok "po lewej" (ccw(facing)).
     */
    static void painting(Structure s, int x, int y, int z, String facing, String variant) {
        Map<String, Object> nbt = new LinkedHashMap<>();
        nbt.put("id", "minecraft:painting");
        nbt.put("facing", facing2D(facing));
        nbt.put("variant", "minecraft:" + variant);
        s.entity(x + 0.5, y + 0.5, z + 0.5, nbt);
    }

    /** Ramka z przedmiotem na ścianie. */
    static void itemFrame(Structure s, int x, int y, int z, String facing, String item) {
        Map<String, Object> nbt = new LinkedHashMap<>();
        nbt.put("id", "minecraft:item_frame");
        nbt.put("Facing", facing3D(facing));
        Map<String, Object> it = new LinkedHashMap<>();
        it.put("id", item);
        it.put("Count", (byte) 1);
        nbt.put("Item", it);
        nbt.put("Fixed", (byte) 1);
        s.entity(x + 0.5, y + 0.5, z + 0.5, nbt);
    }

    /** Stojak na zbroję (dekoracja) z wybranymi elementami: [buty, nogi, tors, głowa]. */
    static void armorStand(Structure s, int x, int y, int z, float yaw, String feet, String legs, String chest, String head) {
        Map<String, Object> nbt = new LinkedHashMap<>();
        nbt.put("id", "minecraft:armor_stand");
        List<Object> armor = new ArrayList<>();
        for (String item : new String[]{feet, legs, chest, head}) {
            Map<String, Object> it = new LinkedHashMap<>();
            if (item != null) {
                it.put("id", item);
                it.put("Count", (byte) 1);
            }
            armor.add(it);
        }
        nbt.put("ArmorItems", armor);
        nbt.put("ShowArms", (byte) 1);
        nbt.put("Rotation", new ArrayList<Object>(List.of(yaw, 0f)));
        s.entity(x + 0.5, y, z + 0.5, nbt);
    }

    private Decor() {}
}
