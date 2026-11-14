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
    /** Ucieczka (np. uwolnieni jeńcy): cel i pozostały czas; nieunikalne postacie znikają po dotarciu. */
    @Nullable private BlockPos fleeTo;
    private int fleeTicks;
    /** Ogłuszenie (Horace w pojedynku z Morgarathem: przyklęka, nie walczy). */
    private int stunTicks;
    /** Tag żołnierzy w bitwie: bronią swojej pozycji i walczą z wrogami bez gracza-dowódcy. */
    public static final String TAG_ALLY = "zw_sojusznik";
    private int generation = -1;
    @Nullable private UUID owner;
    private float baseYaw;
    private int rangedCooldown;
    private boolean initialized;
    @Nullable private UUID greetTarget;
    private int greetTicks;
    // ---- towarzysz tomu II
    public enum Order { ZA_MNA, CZEKAJ, UKRYJ, ODCIAGNIJ }
    private Order order = Order.ZA_MNA;
    @Nullable private BlockPos orderPos;
    @Nullable private UUID horse;
    /** Towarzysz padł ranny w walce (tryb WOUNDED z właścicielem) - wstanie po walce albo po opatrzeniu. */
    private boolean downed;
    private int calmTicks;
    private int specialCooldown;
    private boolean abandonedCounted;

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
        goalSelector.addGoal(1, new StoneGoal());
        goalSelector.addGoal(2, new MeleeGoal());
        goalSelector.addGoal(3, new FollowOwnerGoal());
        goalSelector.addGoal(4, new ReturnHomeGoal());
        goalSelector.addGoal(6, new IdleLookAtPlayerGoal());
        goalSelector.addGoal(7, new IdleRandomLookGoal());
        targetSelector.addGoal(1, new DuelTargetGoal());
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Mob.class, 10, true, false,
                e -> (getMode() == Mode.COMPANION || isBattleAlly()) && !downed && stunTicks <= 0 && combatAllowed() && isFoe(e)));
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
    public void stun(int ticks) {
        stunTicks = Math.max(stunTicks, ticks);
    }

    public boolean stunned() { return stunTicks > 0; }

    public void recoverFromStun() {
        stunTicks = 0;
        setPose(Pose.STANDING);
    }

    public boolean isBattleAlly() { return getTags().contains(TAG_ALLY); }

    public void flee(BlockPos to, int ticks) {
        fleeTo = to;
        fleeTicks = ticks;
        home = null;
        if (getMode() == Mode.SENTRY || getMode() == Mode.WOUNDED) setMode(Mode.IDLE, null);
        setPose(Pose.STANDING);
    }

    /** Zwraca true, gdy postać zniknęła. */
    private boolean tickFlee() {
        Vec3 target = Vec3.atBottomCenterOf(fleeTo);
        if (--fleeTicks <= 0 || position().distanceToSqr(target) < 6 * 6) {
            NpcDefs.NpcDef d = def();
            if (d == null || !d.unique()) {
                discard();
                return true;
            }
            home = fleeTo;
            fleeTo = null;
            return false;
        }
        if (tickCount % 20 == 0) {
            Vec3 dir = target.subtract(position());
            double len = dir.horizontalDistance();
            Vec3 step = len > 12 ? position().add(dir.x * 12 / len, 0, dir.z * 12 / len) : target;
            int y = level().getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mth.floor(step.x), Mth.floor(step.z));
            getNavigation().moveTo(step.x, y, step.z, 1.15);
        }
        return false;
    }

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
        if (e instanceof pl.zwiadowcy.stealth.Watchful w && w.alertBrain().knockedOut()) return false;
        if (e instanceof KalkarEntity || e instanceof WargalEntity || e instanceof StoryFoe) return true;
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
        if (fleeTo != null && tickFlee()) return;
        if (stunTicks > 0) {
            stunTicks--;
            getNavigation().stop();
            setTarget(null);
            if (getPose() != Pose.CROUCHING && stunTicks > 0) setPose(Pose.CROUCHING);
            if (stunTicks == 0) setPose(Pose.STANDING);
        }
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
        } else if (mode == Mode.COMPANION && isShieldBearer() && specialCooldown <= 0 && getTarget() != null) {
            // Horace: co 15 s ściąga na siebie wrogów, którzy atakują gracza
            ServerPlayer p = ownerPlayer();
            if (p != null) {
                for (Mob m : level().getEntitiesOfClass(Mob.class, getBoundingBox().inflate(8), m -> m.getTarget() == p)) m.setTarget(this);
                playSound(net.minecraft.sounds.SoundEvents.VINDICATOR_CELEBRATE, 1f, 1.2f);
                specialCooldown = 300;
            }
        }
        if (rangedCooldown > 0) rangedCooldown--;
        if (specialCooldown > 0) specialCooldown--;
        if ((mode == Mode.COMPANION || downed) && tickCount % 10 == 0) pl.zwiadowcy.companion.Companions.tickNpc(this);
    }

    private void checkGeneration() {
        NpcDefs.NpcDef def = def();
        if (def == null || !def.unique() || generation < 0 || level().getServer() == null) return;
        if (LocationData.get(level().getServer()).generation(npcId()) != generation) {
            discard();
        }
    }

    // ------------------------------------------------------------------ towarzysz (tom II)

    public Order order() { return order; }
    @Nullable public BlockPos orderPos() { return orderPos; }
    public boolean isDowned() { return downed; }
    @Nullable public UUID horseUuid() { return horse; }
    public void setHorseUuid(@Nullable UUID uuid) { this.horse = uuid; }

    public void setOrder(Order order, @Nullable BlockPos pos) {
        this.order = order;
        this.orderPos = pos;
        getNavigation().stop();
    }

    /** Gdy gracz się skrada, towarzysz nie zaczyna walki sam z siebie (chyba że jest alarm albo go zaatakowano). */
    private boolean combatAllowed() {
        ServerPlayer p = ownerPlayer();
        if (p == null || !p.isCrouching()) return true;
        return getLastHurtByMob() != null || pl.zwiadowcy.stealth.Alertness.alarmNear(p);
    }

    private boolean isShieldBearer() {
        return npcId().equals("horace");
    }

    /** Towarzysz pada ranny zamiast zginąć; wstanie po walce (albo po opatrzeniu na poziomie Weteran). */
    private void goDown() {
        downed = true;
        calmTicks = 0;
        abandonedCounted = false;
        setHealth(1f);
        if (isPassenger()) stopRiding();
        setMode(Mode.WOUNDED, ownerPlayer());
        ServerPlayer p = ownerPlayer();
        if (p != null) {
            p.displayClientMessage(Component.literal(getName().getString() + " pada ranny!").withStyle(ChatFormatting.RED), true);
            pl.zwiadowcy.quest.QuestEngine.fireEvent(p, "towarzysz_ranny:" + npcId(), 1);
        }
    }

    /** Powrót do drużyny po walce albo po opatrzeniu. */
    public void recover() {
        downed = false;
        setHealth(getMaxHealth() * 0.5f);
        ServerPlayer p = ownerPlayer();
        setMode(Mode.COMPANION, p);
        if (p != null) p.displayClientMessage(Component.literal(getName().getString() + " podnosi się i wraca do drużyny.").withStyle(ChatFormatting.GREEN), true);
    }

    /** Wywoływane co 10 ticków przez {@link pl.zwiadowcy.companion.Companions}: liczenie spokoju po walce, porzucenie. */
    public void tickDowned(boolean needsTending, boolean enemiesNear, @Nullable ServerPlayer owner) {
        if (enemiesNear) calmTicks = 0;
        else calmTicks += 10;
        if (!needsTending && calmTicks >= 100) {
            recover();
            return;
        }
        if (owner != null && !abandonedCounted && enemiesNear && owner.distanceTo(this) > 80) {
            abandonedCounted = true;
            pl.zwiadowcy.companion.Trust.change(owner, npcId(), -5);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return super.hurt(source, amount);
        if (getMode() == Mode.COMPANION && !level().isClientSide && !(source.getEntity() instanceof Player)) {
            float a = amount;
            // Horace przyjmuje ciosy z przodu na tarczę
            if (isShieldBearer() && source.getSourcePosition() != null) {
                Vec3 look = Vec3.directionFromRotation(0, yBodyRot);
                Vec3 to = source.getSourcePosition().subtract(position()).multiply(1, 0, 1).normalize();
                if (look.dot(to) > 0.3) {
                    a *= 0.3f;
                    playSound(net.minecraft.sounds.SoundEvents.SHIELD_BLOCK, 1f, 1f);
                }
            }
            if (getHealth() - a <= 1f) {
                goDown();
                return true;
            }
            return super.hurt(source, a);
        }
        if (getMode() == Mode.DUEL && source.getEntity() instanceof Player p && p.getUUID().equals(owner)) {
            float capped = Math.min(amount, getHealth() - 1f);
            boolean result = super.hurt(source, capped);
            if (getHealth() <= getMaxHealth() * 0.3f) surrender(p);
            return result;
        }
        // żołnierze w bitwie giną od ciosów wrogów (obrona oddziału ma sens)
        if (isBattleAlly() && !(source.getEntity() instanceof Player)) return super.hurt(source, amount);
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
        if (downed) {
            // opatrzenie rannego towarzysza racją podróżną
            if (!level().isClientSide && player instanceof ServerPlayer sp) {
                if (pl.zwiadowcy.quest.QuestEngine.countItems(sp, "zwiadowcy:racje_podrozne") > 0) {
                    pl.zwiadowcy.quest.QuestEngine.removeItems(sp, "zwiadowcy:racje_podrozne", 1);
                    recover();
                } else {
                    sp.displayClientMessage(Component.literal("Do opatrzenia potrzebujesz racji podróżnej.").withStyle(ChatFormatting.GRAY), true);
                }
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        if (!level().isClientSide && player instanceof ServerPlayer sp0 && getMode() == Mode.WOUNDED
                && pl.zwiadowcy.quest.QuestTom2.onUseNpc(sp0, this)) {
            return InteractionResult.SUCCESS;
        }
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
        arrow.setBaseDamage(npcId().equals("halt") ? 5.0 : 3.0);
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
        tag.putString("Order", order.name());
        if (orderPos != null) tag.put("OrderPos", NbtUtils.writeBlockPos(orderPos));
        if (horse != null) tag.putUUID("Horse", horse);
        tag.putBoolean("Downed", downed);
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
        try {
            order = Order.valueOf(tag.getString("Order"));
        } catch (IllegalArgumentException e) {
            order = Order.ZA_MNA;
        }
        orderPos = tag.contains("OrderPos") ? NbtUtils.readBlockPos(tag.getCompound("OrderPos")) : null;
        horse = tag.hasUUID("Horse") ? tag.getUUID("Horse") : null;
        downed = tag.getBoolean("Downed");
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
                    && t != null && t.isAlive() && !downed
                    && !(npcId().equals("gilan") && distanceToSqr(t) < 16);
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
                rangedCooldown = npcId().equals("halt") ? 18 : 28;
            }
        }
    }

    private class MeleeGoal extends MeleeAttackGoal {
        MeleeGoal() { super(StoryNpc.this, 1.25, true); }

        @Override
        public boolean canUse() {
            NpcDefs.NpcDef d = def();
            Mode m = getMode();
            LivingEntity t = getTarget();
            boolean swordsman = npcId().equals("gilan") && t != null && distanceToSqr(t) < 16;
            boolean melee = m == Mode.DUEL || (m == Mode.COMPANION && !downed && d != null && (d.combat() == NpcDefs.Combat.MELEE || swordsman));
            return melee && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            Mode m = getMode();
            return (m == Mode.DUEL || m == Mode.COMPANION) && super.canContinueToUse();
        }

        /** Pojedynek (sparing): każdy cios jest zapowiadany - 0,8 s zamachu z błyskiem i świstem, potem uderzenie. */
        private int windup;

        @Override
        protected void checkAndPerformAttack(LivingEntity enemy, double distSqr) {
            if (getMode() != Mode.DUEL) {
                super.checkAndPerformAttack(enemy, distSqr);
                return;
            }
            double reach = getAttackReachSqr(enemy);
            if (windup > 0) {
                windup--;
                if (windup % 4 == 0 && level() instanceof net.minecraft.server.level.ServerLevel sl) {
                    sl.sendParticles(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK, getX(), getEyeY(), getZ(), 3, 0.2, 0.2, 0.2, 0.02);
                }
                if (windup == 0) {
                    swing(InteractionHand.MAIN_HAND);
                    if (distSqr <= reach * 1.4) doHurtTarget(enemy);
                    resetAttackCooldown();
                }
                return;
            }
            if (distSqr <= reach && getTicksUntilNextAttack() <= 0) {
                windup = 16;
                playSound(net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_SWEEP, 1f, 0.55f);
            }
        }
    }

    /**
     * Ruch towarzysza: za graczem (przy skradaniu kuca i trzyma się 4 bloki z tyłu), czekanie, ukrycie w krzakach
     * albo odciąganie uwagi wrogów (Horace). Gdy jedzie konno, prowadzi go koń ({@link CompanionHorseEntity}).
     */
    private class FollowOwnerGoal extends Goal {
        FollowOwnerGoal() { setFlags(EnumSet.of(Flag.MOVE)); }

        @Override
        public boolean canUse() {
            if (getMode() != Mode.COMPANION || getTarget() != null || isPassenger()) return false;
            ServerPlayer p = ownerPlayer();
            if (p == null) return false;
            if (order != Order.ZA_MNA) return orderPos != null && orderPos.distToCenterSqr(position()) > 1.5 * 1.5;
            return distanceToSqr(p) > (p.isCrouching() ? 4 * 4 : 5 * 5);
        }

        @Override
        public boolean canContinueToUse() {
            return canUse();
        }

        @Override
        public void tick() {
            ServerPlayer p = ownerPlayer();
            if (p == null) return;
            if (order != Order.ZA_MNA && orderPos != null) {
                getNavigation().moveTo(orderPos.getX() + 0.5, orderPos.getY(), orderPos.getZ() + 0.5, order == Order.ODCIAGNIJ ? 1.3 : 1.0);
                return;
            }
            getLookControl().setLookAt(p, 10f, getMaxHeadXRot());
            boolean tom2 = pl.zwiadowcy.volume.Volumes.tom2Started(pl.zwiadowcy.quest.QuestCapability.get(p));
            double far = tom2 ? 64 : 28;
            if (distanceToSqr(p) > far * far && (!tom2 || !p.hasLineOfSight(StoryNpc.this))) {
                Vec3 behind = p.position().subtract(p.getLookAngle().multiply(2, 0, 2));
                teleportTo(behind.x, p.getY(), behind.z);
                getNavigation().stop();
            } else {
                getNavigation().moveTo(p, p.isCrouching() ? 0.8 : p.isSprinting() ? 1.5 : 1.15);
            }
        }
    }

    /** Evanlyn: nie walczy wręcz, ale rzuca kamieniami (ogłuszenie na sekundę), trzymając się z tyłu. */
    private class StoneGoal extends Goal {
        StoneGoal() { setFlags(EnumSet.of(Flag.LOOK)); }

        @Override
        public boolean canUse() {
            return npcId().equals("evanlyn") && getMode() == Mode.COMPANION && !downed && getTarget() != null
                    && getTarget().isAlive() && distanceToSqr(getTarget()) < 14 * 14;
        }

        @Override
        public void tick() {
            LivingEntity t = getTarget();
            if (t == null) return;
            getLookControl().setLookAt(t, 30f, 30f);
            if (distanceToSqr(t) < 6 * 6) getNavigation().moveTo(getX() + (getX() - t.getX()), getY(), getZ() + (getZ() - t.getZ()), 1.2);
            if (specialCooldown <= 0 && getSensing().hasLineOfSight(t)) {
                specialCooldown = 160;
                swing(InteractionHand.MAIN_HAND);
                net.minecraft.world.entity.projectile.Snowball stone = new net.minecraft.world.entity.projectile.Snowball(level(), StoryNpc.this);
                stone.setItem(new ItemStack(pl.zwiadowcy.registry.ModItems.KAMYK.get()));
                double dx = t.getX() - getX(), dy = t.getEyeY() - 0.4 - stone.getY(), dz = t.getZ() - getZ();
                stone.shoot(dx, dy + Math.sqrt(dx * dx + dz * dz) * 0.15, dz, 1.3f, 4f);
                stone.getPersistentData().putBoolean("zw_kamien_evanlyn", true);
                level().addFreshEntity(stone);
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
