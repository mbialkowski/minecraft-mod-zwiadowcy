package pl.zwiadowcy.assetgen;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Budowniczy szablonu struktury. y=0 to warstwa gruntu (zastępuje darń), y=1 to poziom chodzenia.
 * Stany bloków jako tekst: "minecraft:oak_stairs[facing=north,half=bottom]".
 */
final class Structure {
    final int sx, sy, sz;
    final Random rnd;
    /** Ile warstw szablonu leży pod ziemią: logiczne y=0 (grunt) to wewnętrzne y=groundY. */
    final int groundY;
    private final Map<Long, String> states = new LinkedHashMap<>();
    private final Map<Long, Map<String, Object>> nbts = new HashMap<>();
    private final List<Object> entities = new ArrayList<>();

    Structure(int sx, int sy, int sz, long seed) {
        this(sx, sy, sz, seed, 0);
    }

    Structure(int sx, int sy, int sz, long seed, int groundY) {
        this.sx = sx;
        this.sy = sy;
        this.sz = sz;
        this.rnd = new Random(seed);
        this.groundY = groundY;
    }

    private long key(int x, int y, int z) {
        return ((long) x << 40) | ((long) (y + groundY) << 20) | z;
    }

    boolean inside(int x, int y, int z) {
        int iy = y + groundY;
        return x >= 0 && iy >= 0 && z >= 0 && x < sx && iy < sy && z < sz;
    }

    void set(int x, int y, int z, String state) {
        if (!inside(x, y, z)) return;
        long k = key(x, y, z);
        states.put(k, state);
        nbts.remove(k);
    }

    String get(int x, int y, int z) {
        return states.get(key(x, y, z));
    }

    /** Byt zapisany w szablonie (pojawia się razem ze strukturą). */
    void entity(double x, double y, double z, Map<String, Object> nbt) {
        Map<String, Object> e = new LinkedHashMap<>();
        e.put("pos", new ArrayList<Object>(List.of(x, y + groundY, z)));
        e.put("blockPos", new ArrayList<Object>(List.of((int) Math.floor(x), (int) Math.floor(y) + groundY, (int) Math.floor(z))));
        e.put("nbt", nbt);
        entities.add(e);
    }

    /** Wygodny skrót: mob moda z tagiem "zw_losowe" (pilnowanym w okolicy Redmont). */
    void mob(int x, int y, int z, String id, Object... extra) {
        Map<String, Object> nbt = new LinkedHashMap<>();
        nbt.put("id", id);
        nbt.put("PersistenceRequired", (byte) 1);
        nbt.put("Tags", new ArrayList<Object>(List.of("zw_losowe")));
        for (int i = 0; i + 1 < extra.length; i += 2) nbt.put((String) extra[i], extra[i + 1]);
        entity(x + 0.5, y, z + 0.5, nbt);
    }

    /** NPC moda zapisany w szablonie (imię i ekwipunek uzupełni się przy pierwszym ticku). */
    void npcEntity(int x, int y, int z, String npcId, float yaw) {
        Map<String, Object> nbt = new LinkedHashMap<>();
        nbt.put("id", "zwiadowcy:npc");
        nbt.put("NpcId", npcId);
        nbt.put("PersistenceRequired", (byte) 1);
        nbt.put("Rotation", new ArrayList<Object>(List.of(yaw, 0f)));
        entity(x + 0.5, y, z + 0.5, nbt);
    }

    /** Uzupełnia połączenia płotów, szyb, krat i murków (przy stawianiu struktur losowych kształty się nie aktualizują). */
    void connectAll() {
        Map<Long, String> updated = new HashMap<>();
        for (Map.Entry<Long, String> e : states.entrySet()) {
            String s = e.getValue();
            if (s.contains("[")) continue;
            String family = family(s);
            if (family == null) continue;
            long k = e.getKey();
            int x = (int) (k >> 40), iy = (int) ((k >> 20) & 0xFFFFF), z = (int) (k & 0xFFFFF);
            boolean n = connects(family, states.get(rawKey(x, iy, z - 1)));
            boolean so = connects(family, states.get(rawKey(x, iy, z + 1)));
            boolean ea = connects(family, states.get(rawKey(x + 1, iy, z)));
            boolean we = connects(family, states.get(rawKey(x - 1, iy, z)));
            String props;
            if (family.equals("wall")) {
                props = "[up=true,north=" + (n ? "low" : "none") + ",south=" + (so ? "low" : "none") + ",east=" + (ea ? "low" : "none")
                        + ",west=" + (we ? "low" : "none") + ",waterlogged=false]";
            } else {
                props = "[north=" + n + ",south=" + so + ",east=" + ea + ",west=" + we + ",waterlogged=false]";
            }
            updated.put(k, s + props);
        }
        states.putAll(updated);
    }

    private static long rawKey(int x, int iy, int z) {
        if (x < 0 || iy < 0 || z < 0) return -1;
        return ((long) x << 40) | ((long) iy << 20) | z;
    }

    private static String family(String s) {
        String n = s.contains("[") ? s.substring(0, s.indexOf('[')) : s;
        if (n.endsWith("_fence")) return "fence";
        if (n.endsWith("glass_pane") || n.endsWith("iron_bars")) return "pane";
        if (n.endsWith("_wall") && !n.contains("wall_")) return "wall";
        return null;
    }

    private static boolean connects(String family, String other) {
        if (other == null || other.equals("minecraft:air")) return false;
        String n = other.contains("[") ? other.substring(0, other.indexOf('[')) : other;
        String f = family(other);
        if (family.equals("fence") && (n.endsWith("_fence") || n.endsWith("_fence_gate"))) return true;
        if (family.equals(f)) return true;
        return isFull(n);
    }

    private static final String[] NOT_FULL = {"_stairs", "_slab", "fence", "pane", "bars", "door", "torch", "lantern", "carpet",
            "button", "pressure_plate", "sign", "banner", "_bed", "chest", "campfire", "cauldron", "grass", "fern", "flower",
            "sapling", "vine", "_wall", "skull", "candle", "pot", "anvil", "lectern", "grindstone", "ladder", "rail", "air",
            "water", "lava", "structure_block", "cobweb", "dead_bush", "wheat", "carrots", "potatoes", "farmland", "dirt_path",
            "chain", "bell", "snow", "berry", "poppy", "dandelion", "cornflower", "daisy", "tulip", "beehive", "trapdoor"};

    private static boolean isFull(String name) {
        for (String s : NOT_FULL) if (name.contains(s)) return false;
        return true;
    }

    boolean has(int x, int y, int z) {
        String s = get(x, y, z);
        return s != null && !s.equals("minecraft:air");
    }

    void setNbt(int x, int y, int z, String state, Map<String, Object> nbt) {
        set(x, y, z, state);
        if (inside(x, y, z)) nbts.put(key(x, y, z), nbt);
    }

    void fill(int x0, int y0, int z0, int x1, int y1, int z1, String state) {
        for (int x = Math.min(x0, x1); x <= Math.max(x0, x1); x++)
            for (int y = Math.min(y0, y1); y <= Math.max(y0, y1); y++)
                for (int z = Math.min(z0, z1); z <= Math.max(z0, z1); z++) set(x, y, z, state);
    }

    /** Wypełnienie losowym wariantem z listy (np. kamienne cegły z mchem i pęknięciami). */
    void fillMix(int x0, int y0, int z0, int x1, int y1, int z1, String[] variants, double[] weights) {
        for (int x = Math.min(x0, x1); x <= Math.max(x0, x1); x++)
            for (int y = Math.min(y0, y1); y <= Math.max(y0, y1); y++)
                for (int z = Math.min(z0, z1); z <= Math.max(z0, z1); z++) set(x, y, z, pick(variants, weights));
    }

    String pick(String[] variants, double[] weights) {
        double r = rnd.nextDouble(), acc = 0;
        for (int i = 0; i < variants.length; i++) {
            acc += weights[i];
            if (r < acc) return variants[i];
        }
        return variants[variants.length - 1];
    }

    /** Ściany obwodowe prostopadłościanu (bez dachu i podłogi). */
    void walls(int x0, int y0, int z0, int x1, int y1, int z1, String state) {
        for (int y = y0; y <= y1; y++) {
            for (int x = x0; x <= x1; x++) { set(x, y, z0, state); set(x, y, z1, state); }
            for (int z = z0; z <= z1; z++) { set(x0, y, z, state); set(x1, y, z, state); }
        }
    }

    void wallsMix(int x0, int y0, int z0, int x1, int y1, int z1, String[] v, double[] w) {
        for (int y = y0; y <= y1; y++) {
            for (int x = x0; x <= x1; x++) { set(x, y, z0, pick(v, w)); set(x, y, z1, pick(v, w)); }
            for (int z = z0; z <= z1; z++) { set(x0, y, z, pick(v, w)); set(x1, y, z, pick(v, w)); }
        }
    }

    void clear(int x0, int y0, int z0, int x1, int y1, int z1) {
        fill(x0, y0, z0, x1, y1, z1, "minecraft:air");
    }

    /** Blanki na obwodzie (co drugi blok). */
    void battlements(int x0, int z0, int x1, int z1, int y, String state) {
        for (int x = x0; x <= x1; x++) {
            if ((x - x0) % 2 == 0) { set(x, y, z0, state); set(x, y, z1, state); }
        }
        for (int z = z0; z <= z1; z++) {
            if ((z - z0) % 2 == 0) { set(x0, y, z, state); set(x1, y, z, state); }
        }
    }

    /** Dach dwuspadowy ze schodów; kalenica wzdłuż osi X (alongX) albo Z. */
    void gableRoof(int x0, int z0, int x1, int z1, int y, String material, String planks, boolean alongX) {
        String stairs = "minecraft:" + material + "_stairs";
        if (alongX) {
            int depth = z1 - z0 + 1;
            int layers = (depth + 1) / 2;
            for (int i = 0; i < layers; i++) {
                int zn = z0 + i, zs = z1 - i;
                for (int x = x0 - 1; x <= x1 + 1; x++) {
                    if (zn == zs) set(x, y + i, zn, "minecraft:" + material + "_slab[type=bottom]");
                    else {
                        set(x, y + i, zn, stairs + "[facing=south,half=bottom]");
                        set(x, y + i, zs, stairs + "[facing=north,half=bottom]");
                    }
                }
                // szczyty (ściany szczytowe)
                for (int z = zn + 1; z < zs; z++) {
                    set(x0, y + i, z, planks);
                    set(x1, y + i, z, planks);
                }
            }
        } else {
            int width = x1 - x0 + 1;
            int layers = (width + 1) / 2;
            for (int i = 0; i < layers; i++) {
                int xw = x0 + i, xe = x1 - i;
                for (int z = z0 - 1; z <= z1 + 1; z++) {
                    if (xw == xe) set(xw, y + i, z, "minecraft:" + material + "_slab[type=bottom]");
                    else {
                        set(xw, y + i, z, stairs + "[facing=east,half=bottom]");
                        set(xe, y + i, z, stairs + "[facing=west,half=bottom]");
                    }
                }
                for (int x = xw + 1; x < xe; x++) {
                    set(x, y + i, z0, planks);
                    set(x, y + i, z1, planks);
                }
            }
        }
    }

    /** Dach namiotowy (piramida) ze schodów. */
    void pyramidRoof(int x0, int z0, int x1, int z1, int y, String material) {
        String stairs = "minecraft:" + material + "_stairs";
        int i = 0;
        while (x0 + i <= x1 - i && z0 + i <= z1 - i) {
            int a = x0 + i - 1, b = x1 - i + 1, c = z0 + i - 1, d = z1 - i + 1;
            if (a + 1 >= b - 1 && c + 1 >= d - 1) {
                fill(a + 1, y + i, c + 1, b - 1, y + i, d - 1, "minecraft:" + material + "_slab[type=bottom]");
                break;
            }
            for (int x = a; x <= b; x++) {
                set(x, y + i, c, stairs + "[facing=south,half=bottom]");
                set(x, y + i, d, stairs + "[facing=north,half=bottom]");
            }
            for (int z = c + 1; z < d; z++) {
                set(a, y + i, z, stairs + "[facing=east,half=bottom]");
                set(b, y + i, z, stairs + "[facing=west,half=bottom]");
            }
            i++;
        }
    }

    void door(int x, int y, int z, String facing, String wood) {
        set(x, y, z, "minecraft:" + wood + "_door[facing=" + facing + ",half=lower,hinge=left,open=false]");
        set(x, y + 1, z, "minecraft:" + wood + "_door[facing=" + facing + ",half=upper,hinge=left,open=false]");
    }

    void ironDoor(int x, int y, int z, String facing) {
        set(x, y, z, "minecraft:iron_door[facing=" + facing + ",half=lower,hinge=left,open=false]");
        set(x, y + 1, z, "minecraft:iron_door[facing=" + facing + ",half=upper,hinge=left,open=false]");
    }

    /** Łóżko: (x,z) to nogi, głowa w kierunku facing. */
    void bed(int x, int y, int z, String facing, String color) {
        int dx = 0, dz = 0;
        switch (facing) {
            case "north" -> dz = -1;
            case "south" -> dz = 1;
            case "east" -> dx = 1;
            case "west" -> dx = -1;
        }
        set(x, y, z, "minecraft:" + color + "_bed[facing=" + facing + ",part=foot]");
        set(x + dx, y, z + dz, "minecraft:" + color + "_bed[facing=" + facing + ",part=head]");
    }

    void tallPlant(int x, int y, int z, String plant) {
        set(x, y, z, "minecraft:" + plant + "[half=lower]");
        set(x, y + 1, z, "minecraft:" + plant + "[half=upper]");
    }

    void chest(int x, int y, int z, String facing, String lootTable) {
        Map<String, Object> nbt = new LinkedHashMap<>();
        nbt.put("id", "minecraft:chest");
        nbt.put("LootTable", lootTable);
        setNbt(x, y, z, "minecraft:chest[facing=" + facing + ",type=single]", nbt);
    }

    void barrel(int x, int y, int z, String lootTable) {
        Map<String, Object> nbt = new LinkedHashMap<>();
        nbt.put("id", "minecraft:barrel");
        if (lootTable != null) nbt.put("LootTable", lootTable);
        setNbt(x, y, z, "minecraft:barrel[facing=up,open=false]", nbt);
    }

    /** Znacznik dla moda (blok struktury w trybie DATA; podczas stawiania jest pomijany). */
    void marker(int x, int y, int z, String metadata) {
        Map<String, Object> nbt = new LinkedHashMap<>();
        nbt.put("id", "minecraft:structure_block");
        nbt.put("mode", "DATA");
        nbt.put("metadata", metadata);
        setNbt(x, y, z, "minecraft:structure_block[mode=data]", nbt);
    }

    void anchor(int x, int y, int z, String name) {
        marker(x, y, z, "anchor:" + name);
    }

    void npc(int x, int y, int z, String id, float yaw) {
        marker(x, y, z, "npc:" + id + ";yaw=" + yaw);
    }

    /** Proste drzewo (dąb/świerk) z liśćmi oznaczonymi jako trwałe. */
    void tree(int x, int y, int z, String wood, int height) {
        for (int i = 0; i < height; i++) set(x, y + i, z, "minecraft:" + wood + "_log[axis=y]");
        String leaves = "minecraft:" + wood + "_leaves[persistent=true]";
        int top = y + height;
        for (int dx = -2; dx <= 2; dx++)
            for (int dz = -2; dz <= 2; dz++)
                for (int dy = -2; dy <= 1; dy++) {
                    int r = Math.abs(dx) + Math.abs(dz) + Math.max(0, dy);
                    if (r <= 3 && !(dx == 0 && dz == 0 && dy < 0) && rnd.nextDouble() > 0.08) {
                        if (!has(x + dx, top + dy, z + dz)) set(x + dx, top + dy, z + dz, leaves);
                    }
                }
        set(x, top, z, leaves);
    }

    void bush(int x, int y, int z, String leavesType) {
        String l = "minecraft:" + leavesType + "_leaves[persistent=true]";
        set(x, y, z, l);
        if (rnd.nextBoolean()) set(x + 1, y, z, l);
        if (rnd.nextBoolean()) set(x, y, z + 1, l);
        if (rnd.nextDouble() < 0.4) set(x, y + 1, z, l);
    }

    void save(File file) throws IOException {
        connectAll();
        List<String> palette = new ArrayList<>();
        Map<String, Integer> index = new HashMap<>();
        List<Object> blocks = new ArrayList<>();
        for (Map.Entry<Long, String> e : states.entrySet()) {
            long k = e.getKey();
            int x = (int) (k >> 40), y = (int) ((k >> 20) & 0xFFFFF), z = (int) (k & 0xFFFFF);
            int idx = index.computeIfAbsent(e.getValue(), s -> {
                palette.add(s);
                return palette.size() - 1;
            });
            Map<String, Object> b = new LinkedHashMap<>();
            b.put("state", idx);
            b.put("pos", new ArrayList<Object>(List.of(x, y, z)));
            Map<String, Object> nbt = nbts.get(k);
            if (nbt != null) b.put("nbt", nbt);
            blocks.add(b);
        }
        List<Object> pal = new ArrayList<>();
        for (String s : palette) pal.add(parseState(s));
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("DataVersion", 3465);
        root.put("size", new ArrayList<Object>(List.of(sx, sy, sz)));
        root.put("palette", pal);
        root.put("blocks", blocks);
        root.put("entities", entities);
        Nbt.writeGzip(file, root);
        System.out.printf("  %s: %dx%dx%d, %d blokow, %d bytow, paleta %d%n", file.getName(), sx, sy, sz, blocks.size(), entities.size(), palette.size());
    }

    private static Map<String, Object> parseState(String s) {
        Map<String, Object> m = new LinkedHashMap<>();
        int br = s.indexOf('[');
        m.put("Name", br < 0 ? s : s.substring(0, br));
        if (br >= 0) {
            Map<String, Object> props = new LinkedHashMap<>();
            String inner = s.substring(br + 1, s.length() - 1);
            for (String kv : inner.split(",")) {
                String[] p = kv.split("=");
                props.put(p[0].trim(), p[1].trim());
            }
            m.put("Properties", props);
        }
        return m;
    }
}
