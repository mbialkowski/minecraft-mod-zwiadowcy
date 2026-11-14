package pl.zwiadowcy.entity;

import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import pl.zwiadowcy.quest.QuestActions;
import pl.zwiadowcy.registry.ModEntities;
import pl.zwiadowcy.world.Araluen;
import pl.zwiadowcy.world.Locations;

import java.util.List;

/**
 * Manekin tłumu (plan 9.13): lekki byt bez walki i bez celowania - kolumna Wargali w tle na moście, szyki w bitwie.
 * Umie tylko maszerować po kotwicach, kołysać się i unosić broń. Nie da się go trafić ani popchnąć; ginie tylko
 * od upadku, ognia albo przy końcu czasu życia. Na świat przypada maks. {@link #MAX} manekinów.
 */
public class CrowdDummyEntity extends PathfinderMob {
    public static final int MAX = 160;
    public static final String TAG_GROUP = "zw_tlum";

    public enum Skin {
        WARGAL("wargal", true), WARGAL_KUSZNIK("wargal_kusznik", true), ZOLNIERZ("npc/zolnierz", false),
        LUCZNIK("npc/lucznik_krolewski", false), SKANDYJCZYK("npc/skandyjski_zwiadowca", false);

        public final String texture;
        public final boolean wargal;

        Skin(String texture, boolean wargal) {
            this.texture = texture;
            this.wargal = wargal;
        }
    }

    private static final EntityDataAccessor<Integer> SKIN = SynchedEntityData.defineId(CrowdDummyEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> CHEER = SynchedEntityData.defineId(CrowdDummyEntity.class, EntityDataSerializers.BOOLEAN);
    private int lifetime = 6000;

    public CrowdDummyEntity(EntityType<? extends CrowdDummyEntity> type, Level level) {
        super(type, level);
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 20).add(Attributes.MOVEMENT_SPEED, 0.25).add(Attributes.FOLLOW_RANGE, 32);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(SKIN, 0);
        entityData.define(CHEER, false);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new MarchGoal(this, 0.8));
    }

    public Skin skin() {
        int i = entityData.get(SKIN);
        return i >= 0 && i < Skin.values().length ? Skin.values()[i] : Skin.WARGAL;
    }

    public void setSkin(Skin s) {
        entityData.set(SKIN, s.ordinal());
    }

    public boolean cheering() {
        return entityData.get(CHEER);
    }

    public void setCheering(boolean c) {
        entityData.set(CHEER, c);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && --lifetime <= 0) discard();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // tylko upadek, ogień, pustka i /kill - strzały i ciosy przechodzą bokiem
        if (source.is(DamageTypeTags.IS_FALL) || source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypes.FELL_OUT_OF_WORLD)
                || source.is(DamageTypes.GENERIC_KILL) || source.is(DamageTypes.IN_WALL)) {
            return super.hurt(source, amount);
        }
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(net.minecraft.world.entity.Entity e) {}

    @Override
    public boolean removeWhenFarAway(double dist) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Skin", entityData.get(SKIN));
        tag.putInt("Lifetime", lifetime);
        tag.putBoolean("Cheer", cheering());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(SKIN, tag.getInt("Skin"));
        lifetime = tag.contains("Lifetime") ? tag.getInt("Lifetime") : 6000;
        setCheering(tag.getBoolean("Cheer"));
    }

    // ------------------------------------------------------------------ grupy (akcje misji)

    /**
     * Akcja "crowd": {"skin": "wargal", "count": 20, "at": "most:kolumna_1", "march": "most:kolumna", "stop": 4,
     * "width": 4, "spacing": 1.6, "tag": "kolumna", "item": "minecraft:iron_sword", "lifetime": 6000, "cheer": false}.
     * Szyk: rzędy po "width" ustawione za punktem "at", w kierunku pierwszego odcinka trasy.
     */
    public static void spawnGroup(ServerPlayer p, JsonObject a, String quest) {
        ServerLevel level = Araluen.level(p.getServer());
        if (level == null) return;
        BlockPos at = Locations.resolve(p.getServer(), GsonHelper.getAsString(a, "at", ""));
        if (at == null) return;
        int existing = level.getEntities(ModEntities.TLUM.get(), e -> true).size();
        int count = Math.min(GsonHelper.getAsInt(a, "count", 10), MAX - existing);
        if (count <= 0) return;
        List<BlockPos> route = a.has("march") ? QuestActions.route(p.getServer(), GsonHelper.getAsString(a, "march")) : List.of();
        double dx = 0, dz = 1;
        BlockPos face = a.has("face") ? Locations.resolve(p.getServer(), GsonHelper.getAsString(a, "face")) : !route.isEmpty() ? route.get(Math.min(1, route.size() - 1)) : null;
        if (face != null && !face.equals(at)) {
            double len = Math.hypot(face.getX() - at.getX(), face.getZ() - at.getZ());
            dx = (face.getX() - at.getX()) / len;
            dz = (face.getZ() - at.getZ()) / len;
        }
        int width = GsonHelper.getAsInt(a, "width", 4);
        double spacing = GsonHelper.getAsDouble(a, "spacing", 1.6);
        Skin skin;
        try {
            skin = Skin.valueOf(GsonHelper.getAsString(a, "skin", "wargal").toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException e) {
            skin = Skin.WARGAL;
        }
        ItemStack item = ItemStack.EMPTY;
        if (a.has("item")) {
            var it = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(GsonHelper.getAsString(a, "item")));
            if (it != null) item = new ItemStack(it);
        }
        String tag = GsonHelper.getAsString(a, "tag", "tlum");
        // "banner": środkowy z pierwszego rzędu niesie sztandar (plan 9.13)
        ItemStack banner = ItemStack.EMPTY;
        if (a.has("banner")) {
            var bi = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(GsonHelper.getAsString(a, "banner")));
            if (bi != null) banner = new ItemStack(bi);
        }
        // "row_offset": kolejna grupa tej samej kolumny staje za poprzednią (przesunięcie tylko w szyku marszu)
        int rowShift = GsonHelper.getAsInt(a, "row_offset", 0);
        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        for (int i = 0; i < count; i++) {
            CrowdDummyEntity d = ModEntities.TLUM.get().create(level);
            if (d == null) continue;
            int row = i / width, col = i % width;
            double lateral = (col - (width - 1) / 2.0) * spacing;
            double back = row * spacing;
            // przesunięcie w szyku: w bok (prostopadle do marszu) i do tyłu
            double ox = -dz * lateral - dx * back, oz = dx * lateral - dz * back;
            double x = at.getX() + 0.5 + ox, z = at.getZ() + 0.5 + oz;
            int y = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) Math.floor(x), (int) Math.floor(z));
            if (Math.abs(y - at.getY()) > 6) y = at.getY();
            d.moveTo(x, y, z, yaw, 0f);
            d.setYHeadRot(yaw);
            d.setYBodyRot(yaw);
            d.setSkin(skin);
            d.lifetime = GsonHelper.getAsInt(a, "lifetime", 6000);
            d.setCheering(GsonHelper.getAsBoolean(a, "cheer", false));
            if (!banner.isEmpty() && row == 0 && col == width / 2) d.setItemSlot(EquipmentSlot.MAINHAND, banner.copy());
            else if (!item.isEmpty()) d.setItemSlot(EquipmentSlot.MAINHAND, item.copy());
            d.getPersistentData().putString(TAG_GROUP, tag);
            if (quest != null) d.getPersistentData().putString(QuestActions.TAG_QUEST, quest);
            if (!route.isEmpty()) {
                QuestActions.setMarch(d, route);
                double mBack = (row + rowShift) * spacing;
                d.getPersistentData().putDouble("zw_marsz_dx", -dz * lateral - dx * mBack);
                d.getPersistentData().putDouble("zw_marsz_dz", dx * lateral - dz * mBack);
                if (a.has("stop")) d.getPersistentData().putInt("zw_marsz_stop", GsonHelper.getAsInt(a, "stop"));
            }
            level.addFreshEntity(d);
        }
    }

    /** Akcja "crowd_clear": usuwa grupę manekinów (pusty tag = wszystkie). */
    public static void clear(ServerLevel level, String tag) {
        if (level == null) return;
        for (CrowdDummyEntity d : level.getEntities(ModEntities.TLUM.get(), e -> tag.isEmpty() || tag.equals(e.getPersistentData().getString(TAG_GROUP)))) {
            d.discard();
        }
    }
}
