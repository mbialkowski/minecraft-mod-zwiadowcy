package pl.zwiadowcy.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import pl.zwiadowcy.dialogue.DialogueService;
import pl.zwiadowcy.quest.QuestEngine;
import pl.zwiadowcy.world.LocationData;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.UUID;

/**
 * Postać fabularna (Halt, baron Arald, Horace…). Nieśmiertelna poza trybem pojedynku,
 * stoi na posterunku, w wybranych misjach staje się towarzyszem gracza.
 */
public class StoryNpc extends PathfinderMob implements RangedAttackMob {

    public enum Mode { IDLE, COMPANION, SENTRY, DUEL, WOUNDED }

    private static final EntityDataAccessor<String> NPC_ID = SynchedEntityData.defineId(StoryNpc.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> MODE = SynchedEntityData.defineId(StoryNpc.class, EntityDataSerializers.INT);

    @Nullable private BlockPos home;
    private int generation = -1;
    @Nullable private UUID owner;
    private float baseYaw;
    private int rangedCooldown;
    private boolean initialized;
    @Nullable private UUID greetTarget;
    private int greetTicks;

    public StoryNpc(EntityType<? extends StoryNpc> type, Level level) {
        super(type, level);
        setPersistenceRequired();
        setCanPickUpLoot(false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.ATTACK_DAMAGE, 4.0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(NPC_ID, "wiesniak");
        entityData.define(MODE, Mode.IDLE.ordinal());
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new GreetGoal());
        goalSelector.addGoal(1, new RangedGoal());
        goalSelector.addGoal(2, new MeleeGoal());
        goalSelector.addGoal(3, new FollowOwnerGoal());
        goalSelector.addGoal(4, new ReturnHomeGoal());
        goalSelector.addGoal(6, new IdleLookAtPlayerGoal());
        goalSelector.addGoal(7, new IdleRandomLookGoal());
        targetSelector.addGoal(1, new DuelTargetGoal());
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Mob.class, 10, true, false,
                e -> getMode() == Mode.COMPANION && isFoe(e)));
    }

    // ------------------------------------------------------------------ dane

    public String npcId() { return entityData.get(NPC_ID); }

    @Nullable
    public NpcDefs.NpcDef def() { return NpcDefs.get(npcId()); }

    public Mode getMode() {
        int i = entityData.get(MODE);
        return i >= 0 && i < Mode.values().length ? Mode.values()[i] : Mode.IDLE;
    }

    public void setMode(Mode mode, @Nullable Player ownerPlayer) {
        Mode old = getMode();
        entityData.set(MODE, mode.ordinal());
        owner = ownerPlayer != null ? ownerPlayer.getUUID() : (mode == Mode.COMPANION || mode == Mode.DUEL ? owner : null);
        if (mode == Mode.WOUNDED) {
            setPose(Pose.SLEEPING);
            getNavigation().stop();
            setTarget(null);
        } else if (old == Mode.WOUNDED) {
            setPose(Pose.STANDING);
        }
        if (mode != Mode.DUEL && mode != Mode.COMPANION) setTarget(null);
        if (mode == Mode.SENTRY) baseYaw = getYRot();
    }

    /** Postać podchodzi do gracza i sama zaczyna rozmowę (np. baron po prologu). */
    public void greet(ServerPlayer player) {
        greetTarget = player.getUUID();
        greetTicks = 400;
    }

    public void setup(String npcId, BlockPos home, int generation, float yaw) {
        initialized = true;
        entityData.set(NPC_ID, npcId);
        this.home = home;
        this.generation = generation;
        this.baseYaw = yaw;
        setYRot(yaw);
        yBodyRot = yaw;
        yHeadRot = yaw;
        NpcDefs.NpcDef def = def();
        if (def != null) {
            setCustomName(Component.literal(def.name()));
            setItemSlot(EquipmentSlot.MAINHAND, def.mainHand().get());
            setDropChance(EquipmentSlot.MAINHAND, 0f);
            setMode(def.defaultMode(), null);
        }
    }

    public void setBaseYaw(float yaw) { this.baseYaw = yaw; }
    @Nullable public BlockPos home() { return home; }

    /** Przesuwa postać i jej miejsce domowe (przeniesienie lokacji komendą). */
    public void shiftHome(BlockPos shift) {
        if (home != null) home = home.offset(shift);
        teleportTo(getX() + shift.getX(), getY() + shift.getY(), getZ() + shift.getZ());
    }

    @Nullable
    public ServerPlayer ownerPlayer() {
        if (owner == null || level().isClientSide || level().getServer() == null) return null;
        ServerPlayer p = level().getServer().getPlayerList().getPlayer(owner);
        return p != null && p.level() == level() ? p : null;
    }

    private boolean isFoe(LivingEntity e) {
        if (e instanceof StoryNpc || e instanceof TugEntity) return false;
        if (e instanceof KalkarEntity || e instanceof WargalEntity) return true;
        if (e instanceof BoarEntity boar) return boar.isEnraged();
        if (e instanceof Mob mob && e instanceof Enemy) {
            ServerPlayer p = ownerPlayer();
            return p != null && mob.getTarget() == p;
        }
        return false;
    }

    // ------------------------------------------------------------------ zachowanie

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;
        if (!initialized) {
            // NPC z losowej struktury (zapisany w pliku .nbt) - uzupełnij imię, ekwipunek i dom
            setup(npcId(), blockPosition(), -1, getYRot());
        }
        if (tickCount % 40 == 0) checkGeneration();
        Mode mode = getMode();
        if (mode == Mode.SENTRY) {
            float yaw = baseYaw + 70f * Mth.sin(tickCount / 45f);
            setYRot(yaw);
            yBodyRot = yaw;
            yHeadRot = yaw;
        } else if (mode == Mode.WOUNDED && getPose() != Pose.SLEEPING) {
            setPose(Pose.SLEEPING);
        } else if ((mode == Mode.COMPANION || mode == Mode.DUEL) && tickCount % 20 == 0 && ownerPlayer() == null) {
            setMode(Mode.IDLE, null);
        }
        if (rangedCooldown > 0) rangedCooldown--;
    }

    private void checkGeneration() {
        NpcDefs.NpcDef def = def();
        if (def == null || !def.unique() || generation < 0 || level().getServer() == null) return;
        if (LocationData.get(level().getServer()).generation(npcId()) != generation) {
            discard();
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return super.hurt(source, amount);
        if (getMode() == Mode.DUEL && source.getEntity() instanceof Player p && p.getUUID().equals(owner)) {
            float capped = Math.min(amount, getHealth() - 1f);
            boolean result = super.hurt(source, capped);
            if (getHealth() <= getMaxHealth() * 0.3f) surrender(p);
            return result;
        }
        return false;
    }

    private void surrender(Player p) {
        setMode(Mode.IDLE, null);
        setHealth(getMaxHealth());
        setTarget(null);
        getNavigation().stop();
        p.sendSystemMessage(Component.literal(getName().getString() + ": ").withStyle(ChatFormatting.GOLD)
                .append(Component.literal("Dość! Już dość… poddaję się!").withStyle(ChatFormatting.WHITE)));
        if (p instanceof ServerPlayer sp) {
            QuestEngine.fireEvent(sp, "npc_pokonany", 1);
            QuestEngine.fireEvent(sp, "npc_pokonany:" + npcId(), 1);
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (getMode() == Mode.DUEL) {
            swing(InteractionHand.MAIN_HAND);
            return target.hurt(damageSources().mobAttack(this), 2.0f);
        }
        return super.doHurtTarget(target);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (getMode() == Mode.DUEL) return InteractionResult.PASS;
        if (!level().isClientSide && player instanceof ServerPlayer sp) {
            getLookControl().setLookAt(player, 30f, 30f);
            DialogueService.open(sp, npcId(), this);
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        AbstractArrow arrow = ProjectileUtil.getMobArrow(this, new ItemStack(Items.ARROW), power);
        double dx = target.getX() - getX();
        double dy = target.getY(0.3333) - arrow.getY();
        double dz = target.getZ() - getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        arrow.shoot(dx, dy + dist * 0.2, dz, 1.8f, 2.0f);
        arrow.setBaseDamage(3.0);
        arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
        playSound(net.minecraft.sounds.SoundEvents.ARROW_SHOOT, 1.0f, 1.0f / (getRandom().nextFloat() * 0.4f + 0.8f));
        level().addFreshEntity(arrow);
    }

    @Override
    public boolean removeWhenFarAway(double distance) { return false; }

    @Override
    public boolean isPushable() { return getMode() != Mode.WOUNDED && super.isPushable(); }

    @Override
    protected boolean shouldDespawnInPeaceful() { return false; }

    @Override
    public boolean canBeLeashed(Player player) { return false; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("NpcId", npcId());
        tag.putInt("Mode", getMode().ordinal());
        tag.putInt("Generation", generation);
        tag.putFloat("BaseYaw", baseYaw);
        tag.putBoolean("Initialized", initialized);
        if (home != null) tag.put("Home", NbtUtils.writeBlockPos(home));
        if (owner != null) tag.putUUID("Owner", owner);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(NPC_ID, tag.getString("NpcId"));
        generation = tag.getInt("Generation");
        baseYaw = tag.getFloat("BaseYaw");
        initialized = tag.getBoolean("Initialized") || tag.contains("Home");
        home = tag.contains("Home") ? NbtUtils.readBlockPos(tag.getCompound("Home")) : null;
        owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        int m = tag.getInt("Mode");
        entityData.set(MODE, m >= 0 && m < Mode.values().length ? m : 0);
        if (getMode() == Mode.WOUNDED) setPose(Pose.SLEEPING);
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return pose == Pose.SLEEPING ? EntityDimensions.fixed(0.6f, 0.4f) : super.getDimensions(pose);
    }

    // ------------------------------------------------------------------ cele AI

    private boolean active() {
        Mode m = getMode();
        return m != Mode.WOUNDED;
    }

    private class GreetGoal extends Goal {
        GreetGoal() { setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }

        @Nullable
        private ServerPlayer target() {
            if (greetTarget == null || level().getServer() == null) return null;
            ServerPlayer p = level().getServer().getPlayerList().getPlayer(greetTarget);
            return p != null && p.level() == level() ? p : null;
        }

        @Override
        public boolean canUse() { return greetTarget != null && getMode() == Mode.IDLE; }

        @Override
        public void tick() {
            ServerPlayer p = target();
            if (p == null || --greetTicks <= 0 && distanceTo(p) > 40) {
                greetTarget = null;
                return;
            }
            getLookControl().setLookAt(p, 30f, 30f);
            if (distanceTo(p) < 3.0 || greetTicks <= 0) {
                getNavigation().stop();
                greetTarget = null;
                DialogueService.open(p, npcId(), StoryNpc.this);
            } else {
                getNavigation().moveTo(p, 1.0);
            }
        }

        @Override
        public boolean canContinueToUse() { return greetTarget != null; }
    }

    private class RangedGoal extends Goal {
        RangedGoal() { setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }

        @Override
        public boolean canUse() {
            NpcDefs.NpcDef d = def();
            LivingEntity t = getTarget();
            return d != null && d.combat() == NpcDefs.Combat.ARCHER && getMode() == Mode.COMPANION
                    && t != null && t.isAlive();
        }

        @Override
        public void tick() {
            LivingEntity t = getTarget();
            if (t == null) return;
            getLookControl().setLookAt(t, 30f, 30f);
            double dist = distanceToSqr(t);
            boolean sees = getSensing().hasLineOfSight(t);
            if (dist > 20 * 20 || !sees) getNavigation().moveTo(t, 1.1);
            else if (dist < 6 * 6) getNavigation().moveTo(getX() + (getX() - t.getX()), getY(), getZ() + (getZ() - t.getZ()), 1.2);
            else getNavigation().stop();
            if (sees && rangedCooldown <= 0 && dist < 26 * 26) {
                performRangedAttack(t, 1.0f);
                rangedCooldown = 28;
            }
        }
    }

    private class MeleeGoal extends MeleeAttackGoal {
        MeleeGoal() { super(StoryNpc.this, 1.25, true); }

        @Override
        public boolean canUse() {
            NpcDefs.NpcDef d = def();
            Mode m = getMode();
            boolean melee = m == Mode.DUEL || (m == Mode.COMPANION && d != null && d.combat() == NpcDefs.Combat.MELEE);
            return melee && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            Mode m = getMode();
            return (m == Mode.DUEL || m == Mode.COMPANION) && super.canContinueToUse();
        }
    }

    private class FollowOwnerGoal extends Goal {
        FollowOwnerGoal() { setFlags(EnumSet.of(Flag.MOVE)); }

        @Override
        public boolean canUse() {
            if (getMode() != Mode.COMPANION || getTarget() != null) return false;
            ServerPlayer p = ownerPlayer();
            return p != null && distanceToSqr(p) > 5 * 5;
        }

        @Override
        public void tick() {
            ServerPlayer p = ownerPlayer();
            if (p == null) return;
            getLookControl().setLookAt(p, 10f, getMaxHeadXRot());
            if (distanceToSqr(p) > 28 * 28) {
                Vec3 behind = p.position().subtract(p.getLookAngle().multiply(2, 0, 2));
                teleportTo(behind.x, p.getY(), behind.z);
                getNavigation().stop();
            } else {
                getNavigation().moveTo(p, p.isSprinting() ? 1.5 : 1.15);
            }
        }
    }

    private class ReturnHomeGoal extends Goal {
        ReturnHomeGoal() { setFlags(EnumSet.of(Flag.MOVE)); }

        @Override
        public boolean canUse() {
            Mode m = getMode();
            return (m == Mode.IDLE || m == Mode.SENTRY) && home != null
                    && home.distToCenterSqr(position()) > 2.5 * 2.5;
        }

        @Override
        public void start() {
            if (home == null) return;
            if (home.distToCenterSqr(position()) > 40 * 40) {
                teleportTo(home.getX() + 0.5, home.getY(), home.getZ() + 0.5);
            } else {
                getNavigation().moveTo(home.getX() + 0.5, home.getY(), home.getZ() + 0.5, 0.9);
            }
        }

        @Override
        public boolean canContinueToUse() {
            return canUse() && !getNavigation().isDone();
        }

        @Override
        public void stop() {
            if (getMode() == Mode.SENTRY) {
                setYRot(baseYaw);
                yBodyRot = baseYaw;
            }
        }
    }

    private class IdleLookAtPlayerGoal extends LookAtPlayerGoal {
        IdleLookAtPlayerGoal() { super(StoryNpc.this, Player.class, 8f); }

        @Override
        public boolean canUse() { return getMode() == Mode.IDLE && super.canUse(); }
    }

    private class IdleRandomLookGoal extends RandomLookAroundGoal {
        IdleRandomLookGoal() { super(StoryNpc.this); }

        @Override
        public boolean canUse() { return getMode() == Mode.IDLE && super.canUse(); }
    }

    private class DuelTargetGoal extends Goal {
        @Override
        public boolean canUse() {
            return getMode() == Mode.DUEL && ownerPlayer() != null && getTarget() != ownerPlayer();
        }

        @Override
        public void start() { setTarget(ownerPlayer()); }
    }

    public boolean isActiveNpc() { return active(); }
}
