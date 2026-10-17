package pl.zwiadowcy.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import pl.zwiadowcy.Zwiadowcy;
import pl.zwiadowcy.entity.NpcService;
import pl.zwiadowcy.entity.TugEntity;
import pl.zwiadowcy.registry.ModEntities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Stawia strukturę fabularną z pliku data/zwiadowcy/structures/&lt;id&gt;.nbt:
 * szuka płaskiego, suchego terenu, wyrównuje go, wstawia szablon i obsługuje znaczniki DATA.
 * Warstwa y=0 szablonu to fundament (zastępuje darń), warstwa y=1 to poziom gruntu.
 */
public final class StructurePlacer {
    private static final int FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    private static final int MARGIN = 7;
    /** Miejsce do ominięcia przy szukaniu terenu (stara pozycja przenoszonej lokacji). */
    @javax.annotation.Nullable static BlockPos avoid;

    public static LocationData.Placed place(ServerLevel level, StoryLocation loc, LocationData data) {
        StructureTemplate template = level.getStructureManager().get(Zwiadowcy.id(loc.id)).orElse(null);
        if (template == null || template.getSize().getX() == 0) {
            Zwiadowcy.LOGGER.error("Brak pliku struktury {}.nbt - stawiam pusty punkt", loc.id);
            BlockPos c = new BlockPos(loc.x, height(level, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, loc.x, loc.z), loc.z);
            return new LocationData.Placed(c, c);
        }
        Vec3i size = template.getSize();
        BlockPos center = findSite(level, loc, size, nominal(data, loc), takenAreas(level, data, loc));
        int baseY = center.getY();
        BlockPos origin = new BlockPos(center.getX() - size.getX() / 2, baseY - 1 - loc.groundOffset, center.getZ() - size.getZ() / 2);

        prepareTerrain(level, origin.above(loc.groundOffset), size, baseY);

        StructurePlaceSettings settings = new StructurePlaceSettings()
                .addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK)
                .addProcessor(HangingFixProcessor.INSTANCE)
                .setIgnoreEntities(false);
        List<StructureTemplate.StructureBlockInfo> markers = template.filterBlocks(origin, settings, Blocks.STRUCTURE_BLOCK);
        template.placeInWorld(level, origin, origin, settings, level.getRandom(), FLAGS);

        if (loc == StoryLocation.RUINY_GORLANU) paintBiome(level, center, 96, "rownina_samotnosci");
        if (loc == StoryLocation.SPALONA_OSADA) paintBiome(level, center, 36, "rownina_samotnosci");

        LocationData.Placed placed = new LocationData.Placed(center, origin);
        for (StructureTemplate.StructureBlockInfo info : markers) {
            CompoundTag nbt = info.nbt();
            if (nbt == null || !"DATA".equals(nbt.getString("mode"))) continue;
            handleMarker(level, loc, data, placed, info.pos(), nbt.getString("metadata"));
        }
        return placed;
    }

    /** Znacznik ma postać "rodzaj:wartość" z opcjonalnym ";yaw=stopnie". */
    private static void handleMarker(ServerLevel level, StoryLocation loc, LocationData data,
                                     LocationData.Placed placed, BlockPos pos, String metadata) {
        float yaw = 0f;
        String main = metadata;
        int semi = metadata.indexOf(';');
        if (semi >= 0) {
            main = metadata.substring(0, semi);
            for (String opt : metadata.substring(semi + 1).split(";")) {
                if (opt.startsWith("yaw=")) {
                    try { yaw = Float.parseFloat(opt.substring(4)); } catch (NumberFormatException ignored) {}
                }
            }
        }
        String[] kv = main.split(":", 2);
        String kind = kv[0];
        String value = kv.length > 1 ? kv[1] : "";
        // znacznik nie jest stawiany (procesor go pomija) - pod ziemią zostałaby w tym miejscu skała
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), FLAGS);
        switch (kind) {
            case "anchor" -> placed.anchors.put(value, pos);
            case "npc" -> {
                placed.anchors.putIfAbsent(value, pos);
                NpcService.spawnFromMarker(level, data, loc, value, pos, yaw);
            }
            case "dummy" -> {
                placed.anchors.put("manekin", pos);
                ArmorStand stand = EntityType.ARMOR_STAND.create(level);
                if (stand != null) {
                    stand.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, yaw, 0f);
                    stand.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.CARVED_PUMPKIN));
                    stand.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.LEATHER_CHESTPLATE));
                    stand.setInvulnerable(true);
                    stand.setCustomName(Component.literal("Manekin treningowy"));
                    stand.addTag("zwiadowcy_manekin");
                    level.addFreshEntity(stand);
                }
            }
            case "tug" -> {
                placed.anchors.put("wyrwij", pos);
                if (!data.tugSpawned()) {
                    TugEntity tug = ModEntities.WYRWIJ.get().create(level);
                    if (tug != null) {
                        tug.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, yaw, 0f);
                        tug.setHomePos(pos);
                        level.addFreshEntity(tug);
                        data.setTugSpawned();
                    }
                }
            }
            default -> Zwiadowcy.LOGGER.warn("Nieznany znacznik '{}' w {}", metadata, loc.id);
        }
    }

    /** Zmienia biom w promieniu wokół lokacji (np. wymarła Równina Samotności wokół Gorlanu) i odsyła go klientom. */
    private static void paintBiome(ServerLevel level, BlockPos center, int radius, String biomeId) {
        var registry = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME);
        var key = net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BIOME, Zwiadowcy.id(biomeId));
        var holder = registry.getHolder(key);
        if (holder.isEmpty()) return;
        var biome = holder.get();
        var sampler = level.getChunkSource().randomState().sampler();
        java.util.List<net.minecraft.world.level.chunk.ChunkAccess> changed = new java.util.ArrayList<>();
        int r2 = radius * radius;
        for (int cx = (center.getX() - radius) >> 4; cx <= (center.getX() + radius) >> 4; cx++) {
            for (int cz = (center.getZ() - radius) >> 4; cz <= (center.getZ() + radius) >> 4; cz++) {
                var chunk = level.getChunk(cx, cz);
                chunk.fillBiomesFromNoise((qx, qy, qz, s) -> {
                    int bx = net.minecraft.core.QuartPos.toBlock(qx) + 2 - center.getX();
                    int bz = net.minecraft.core.QuartPos.toBlock(qz) + 2 - center.getZ();
                    return bx * bx + bz * bz <= r2 ? biome : chunk.getNoiseBiome(qx, qy, qz);
                }, sampler);
                chunk.setUnsaved(true);
                changed.add(chunk);
            }
        }
        level.getChunkSource().chunkMap.resendBiomesForChunks(changed);
    }

    /**
     * Wysokość terenu (pierwszy wolny blok). Level.getHeight zwraca minimum świata dla niezaładowanych chunków,
     * dlatego najpierw wymuszamy wygenerowanie chunka.
     */
    public static int height(ServerLevel level, Heightmap.Types type, int x, int z) {
        return level.getChunk(x >> 4, z >> 4).getHeight(type, x & 15, z & 15) + 1;
    }

    /** Biomy, w których lokacja wygląda naturalnie (np. chata Halta w lesie). Pusty zbiór = bez preferencji. */
    private static Set<String> preferredBiomes(StoryLocation loc) {
        Set<String> forest = Set.of("lasy_redmont", "mroczna_puszcza", "stare_bory", "bory_polnocy", "brzozowy_gaj");
        return switch (loc) {
            case CHATA_HALTA, LEGOWISKO_DZIKA, OBOZ_ZLOTU -> forest;
            case WENSLEY, STAJNIA_BOBA -> Set.of("laki_araluenu", "lasy_redmont", "brzozowy_gaj");
            case SPALONA_OSADA, RUINY_GORLANU -> Set.of("rownina_samotnosci", "laki_araluenu", "pogorze_deszczu_i_nocy");
            case REDMONT -> Set.of();
        };
    }

    /**
     * Punkt, od którego szukamy miejsca: Redmont według współrzędnych świata, pozostałe lokacje względem
     * faktycznego położenia Redmontu (zamek mógł odsunąć się od morza).
     */
    public static BlockPos nominal(LocationData data, StoryLocation loc) {
        LocationData.Placed redmont = loc == StoryLocation.REDMONT ? null : data.placed(StoryLocation.REDMONT.id);
        return redmont == null ? new BlockPos(loc.x, 0, loc.z)
                : new BlockPos(redmont.center.getX() + loc.x, 0, redmont.center.getZ() + loc.z);
    }

    /** Prostokąty (x0, z0, x1, z1) zajęte przez inne postawione lokacje, z zapasem na wyrównany teren. */
    private static List<int[]> takenAreas(ServerLevel level, LocationData data, StoryLocation loc) {
        List<int[]> out = new ArrayList<>();
        for (var e : data.allPlaced().entrySet()) {
            if (e.getKey().equals(loc.id)) continue;
            Vec3i s = level.getStructureManager().get(Zwiadowcy.id(e.getKey())).map(StructureTemplate::getSize).orElse(null);
            if (s == null) continue;
            BlockPos o = e.getValue().origin;
            int pad = MARGIN + 8;
            out.add(new int[]{o.getX() - pad, o.getZ() - pad, o.getX() + s.getX() + pad, o.getZ() + s.getZ() + pad});
        }
        return out;
    }

    private static boolean overlaps(List<int[]> taken, int x0, int z0, int x1, int z1) {
        for (int[] t : taken) if (x0 <= t[2] && x1 >= t[0] && z0 <= t[3] && z1 >= t[1]) return true;
        return false;
    }

    private static boolean isWet(Holder<Biome> biome) {
        return biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_DEEP_OCEAN) || biome.is(BiomeTags.IS_RIVER) || biome.is(BiomeTags.IS_BEACH);
    }

    /**
     * Szuka płaskiego, suchego miejsca coraz dalej od punktu nominalnego.
     * Najpierw tani test biomu (bez generowania chunków): środek i krawędzie nie mogą leżeć w morzu, rzece ani na
     * plaży, a miejsce nie może nachodzić na inną lokację. Dopiero wtedy mierzymy teren: bez liści (las nie jest
     * „górą”), z wodą rozpoznawaną po bloku na powierzchni. Preferowany biom daje przewagę, odległość lekko odejmuje.
     */
    private static BlockPos findSite(ServerLevel level, StoryLocation loc, Vec3i size, BlockPos nominal, List<int[]> taken) {
        int hx = size.getX() / 2, hz = size.getZ() / 2;
        int[][] pts = {{0, 0}, {-hx, -hz}, {hx, -hz}, {-hx, hz}, {hx, hz}, {0, -hz}, {0, hz}, {-hx, 0}, {hx, 0}};
        Set<String> preferred = preferredBiomes(loc);
        int sea = level.getSeaLevel();
        BlockPos best = null;
        int bestScore = Integer.MAX_VALUE, measured = 0;
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (int r = 0; r <= 768; r += 16) {
            // dalej niż 112 bloków szukamy tylko wtedy, gdy w pobliżu nie ma nic sensownego; liczba pomiarów
            // terenu (każdy generuje chunki) jest ograniczona
            if (best != null && r > 112 && (bestScore <= 40 || measured >= 48)) break;
            int steps = r == 0 ? 1 : Math.max(8, r / 8);
            for (int i = 0; i < steps; i++) {
                double a = (Math.PI * 2 * i) / steps;
                int x = nominal.getX() + (int) Math.round(Math.cos(a) * r);
                int z = nominal.getZ() + (int) Math.round(Math.sin(a) * r);
                if (avoid != null && Math.abs(x - avoid.getX()) < size.getX() + 16 && Math.abs(z - avoid.getZ()) < size.getZ() + 16) continue;
                if (overlaps(taken, x - hx - MARGIN, z - hz - MARGIN, x + hx + MARGIN, z + hz + MARGIN)) continue;
                boolean wet = false;
                for (int[] pt : pts) {
                    if (isWet(level.getBiome(m.set(x + pt[0], sea, z + pt[1])))) {
                        wet = true;
                        break;
                    }
                }
                if (wet) continue;
                measured++;
                int[] hs = new int[pts.length];
                int water = 0;
                for (int p = 0; p < pts.length; p++) {
                    int px = x + pts[p][0], pz = z + pts[p][1];
                    hs[p] = height(level, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, px, pz);
                    if (!level.getFluidState(m.set(px, hs[p] - 1, pz)).isEmpty()) water++;
                }
                int[] sorted = hs.clone();
                Arrays.sort(sorted);
                int median = sorted[sorted.length / 2];
                int score = (sorted[sorted.length - 2] - sorted[1]) + water * 60 + r / 32;
                if (median < sea) score += 30;
                Holder<Biome> biome = level.getBiome(m.set(x, median, z));
                if (!preferred.isEmpty() && !biome.unwrapKey().map(k -> preferred.contains(k.location().getPath())).orElse(false)) {
                    score += 25;
                }
                if (score < bestScore) {
                    bestScore = score;
                    best = new BlockPos(x, Math.max(median, sea + 1), z);
                }
                if (score <= 4) return best;
            }
        }
        if (best == null) {
            // dookoła sama woda - zostaje punkt nominalny (teren zostanie wyrównany i usypany)
            Zwiadowcy.LOGGER.warn("Nie znaleziono suchego miejsca dla {} - stawiam w punkcie nominalnym", loc.id);
            int x = nominal.getX(), z = nominal.getZ();
            best = new BlockPos(x, Math.max(height(level, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), sea + 1), z);
        }
        return best;
    }

    private static void prepareTerrain(ServerLevel level, BlockPos origin, Vec3i size, int baseY) {
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState grass = Blocks.GRASS_BLOCK.defaultBlockState();
        BlockState dirt = Blocks.DIRT.defaultBlockState();
        int x0 = origin.getX(), z0 = origin.getZ();
        int x1 = x0 + size.getX() - 1, z1 = z0 + size.getZ() - 1;
        int clearTop = baseY + size.getY() + 12;
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (int x = x0 - MARGIN; x <= x1 + MARGIN; x++) {
            for (int z = z0 - MARGIN; z <= z1 + MARGIN; z++) {
                int dx = Math.max(0, Math.max(x0 - x, x - x1));
                int dz = Math.max(0, Math.max(z0 - z, z - z1));
                int d = Math.max(dx, dz);
                int natural = height(level, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                int target;
                if (d == 0) {
                    target = baseY;
                } else {
                    double t = d / (double) (MARGIN + 1);
                    target = (int) Math.round(baseY * (1 - t) + natural * t);
                }
                // usuń wszystko powyżej docelowego poziomu (wzgórza, drzewa, woda)
                int top = d == 0 ? Math.max(clearTop, height(level, Heightmap.Types.WORLD_SURFACE, x, z))
                        : height(level, Heightmap.Types.WORLD_SURFACE, x, z);
                for (int y = target; y <= top; y++) {
                    m.set(x, y, z);
                    if (!level.getBlockState(m).isAir()) level.setBlock(m, air, FLAGS);
                }
                // dosyp ziemi tam, gdzie teren był niżej
                for (int y = Math.min(natural, target) - 1; y < target; y++) {
                    m.set(x, y, z);
                    BlockState cur = level.getBlockState(m);
                    if (y == target - 1) {
                        if (natural != target || cur.isAir() || !cur.isSolid()) level.setBlock(m, grass, FLAGS);
                    } else if (cur.isAir() || !cur.isSolid()) {
                        level.setBlock(m, dirt, FLAGS);
                    }
                }
            }
        }
    }

    private StructurePlacer() {}
}
