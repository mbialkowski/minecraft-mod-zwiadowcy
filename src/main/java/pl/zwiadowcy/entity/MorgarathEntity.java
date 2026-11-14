package pl.zwiadowcy.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import pl.zwiadowcy.quest.QuestCapability;
import pl.zwiadowcy.quest.QuestEngine;
import pl.zwiadowcy.registry.ModEntities;

import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * Morgarath, Władca Deszczu i Nocy (plan 10.3) - pojedynek M15.
 * <ul>
 *   <li>Faza 1 (100% → 70%, po S5 → 85%): konno na czarnym rumaku, szarże z zapowiedzią; rumak jest nietykalny.</li>
 *   <li>Faza 2 (→ 30%): pieszo, zapowiadane cięcia; Horace blokuje tarczą (po 6 ciosach przyklęka na 5 s).</li>
 *   <li>Faza 3 (→ 0): szał - ciosy o 30% szybsze, piasek w oczy, udawany upadek; Horace ogłuszony uderzeniem tarczą,
 *       ale przy zaufaniu ≥ 70 raz wstaje i przyjmuje cios, który zabiłby Willa.</li>
 * </ul>
 * Zbroja: zwykłe strzały i noże ×0,5, ogień ×2 (oblany olejem i podpalony wpada na 4 s w gniew). Ostatni cios zawsze
 * zadaje gracz. Przejścia faz wysyłają zdarzenia misji "morgarath_faza_2" i "morgarath_faza_3".
 */
public class MorgarathEntity extends StoryFoe {
    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.literal("Morgarath, Władca Deszczu i Nocy").withStyle(ChatFormatting.DARK_RED),
            BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_10);

    private int phase = 1;
    private boolean mounted;
    private int swingIndex;
    private int enrage;
    private boolean wasOnFire;
    private int horaceBlocks;
    private boolean horaceSaved;
    private int trickCooldown = 120;
    private int sandWindup;
    private int feint;

    public MorgarathEntity(EntityType<? extends MorgarathEntity> type, Level level) {
        super(type, level);
        setPersistenceRequired();
        xpReward = 300;
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_SWORD));
        setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.NETHERITE_CHESTPLATE));
        setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.NETHERITE_LEGGINGS));
        setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.NETHERITE_BOOTS));
        for (EquipmentSlot s : EquipmentSlot.values()) setDropChance(s, 0f);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 400)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 9)
                .add(Attributes.ARMOR, 8)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.85)
                .add(Attributes.FOLLOW_RANGE, 48);
    }

    public int phase() { return phase; }

    /** Akcja spawn z "boss_phase": po śmierci gracza pojedynek zaczyna się od początku bieżącej fazy. */
    public void startAtPhase(int p, @Nullable ServerPlayer player) {
        phase = Math.max(1, Math.min(3, p));
        if (phase >= 2) {
            mounted = true;
            setHealth(getMaxHealth() * (phase == 2 ? phaseOneEnd(player) - 0.005f : 0.3f - 0.005f));
        }
    }

    /** Sesja zdjęciowa: Morgarath w siodle nieruchomego rumaka. */
    public void mountForPhoto(net.minecraft.world.entity.Entity steed) {
        mounted = true;
        startRiding(steed, true);
    }

    /** Koniec fazy konnej: 70%, a gdy gracz poluzował popręg rumaka (S5) - już przy 85%. */
    private float phaseOneEnd(@Nullable Player p) {
        if (p == null) p = level().getNearestPlayer(this, 64);
        return p instanceof ServerPlayer sp && QuestCapability.get(sp).hasFlag("popreg_poluzowany") ? 0.85f : 0.70f;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new DuelGoal());
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide || !(level() instanceof ServerLevel sl)) return;
        bossEvent.setProgress(getHealth() / getMaxHealth());
        BossBarRange.update(this, bossEvent, 64);
        if (phase == 1 && !mounted) {
            BlackSteedEntity steed = ModEntities.CZARNY_RUMAK.get().create(sl);
            if (steed != null) {
                steed.moveTo(getX(), getY(), getZ(), getYRot(), 0f);
                sl.addFreshEntity(steed);
                startRiding(steed, true);
            }
            mounted = true;
        }
        LivingEntity t = getTarget();
        if (getVehicle() instanceof BlackSteedEntity steed && steed.charging() && t != null && !chargeHit && distanceTo(t) < 3.2) {
            chargeHit = true;
            swing(net.minecraft.world.InteractionHand.MAIN_HAND);
            t.hurt(damageSources().mobAttack(this), 10f);
            playSound(SoundEvents.PLAYER_ATTACK_KNOCKBACK, 1.5f, 0.5f);
        }
        if (phase == 1 && getHealth() <= getMaxHealth() * phaseOneEnd(null)) toPhaseTwo();
        if (phase == 2 && getHealth() <= getMaxHealth() * 0.3f) toPhaseThree();
        // olej i płonąca strzała: gniew na 4 s
        boolean fire = isOnFire();
        if (fire && !wasOnFire) {
            enrage = 80;
            announce("Morgarath ryczy z wściekłości - płonie!", ChatFormatting.RED);
        }
        wasOnFire = fire;
        if (enrage > 0) enrage--;
        if (trickCooldown > 0) trickCooldown--;
        tickTricks(sl);
    }

    private void toPhaseTwo() {
        phase = 2;
        if (getVehicle() instanceof BlackSteedEntity steed) steed.flee();
        stopRiding();
        playSound(SoundEvents.HORSE_DEATH, 1.4f, 0.5f);
        announce("Czarny rumak potyka się - Morgarath spada na ziemię i dobywa miecza!", ChatFormatting.GOLD);
        fireEvent("morgarath_faza_2");
    }

    private void toPhaseThree() {
        phase = 3;
        // uderzenie tarczą: Horace ogłuszony
        StoryNpc horace = horace();
        if (horace != null) {
            horace.stun(20 * 14);
            Vec3 push = horace.position().subtract(position()).multiply(1, 0, 1).normalize().scale(1.2);
            horace.push(push.x, 0.4, push.z);
            horace.hurtMarked = true;
        }
        playSound(SoundEvents.SHIELD_BLOCK, 2f, 0.5f);
        playSound(SoundEvents.RAVAGER_ROAR, 1.5f, 0.6f);
        announce("Morgarath powala Horace'a uderzeniem tarczy! Zostałeś sam.", ChatFormatting.DARK_RED);
        fireEvent("morgarath_faza_3");
    }

    @Nullable
    private StoryNpc horace() {
        for (StoryNpc n : level().getEntitiesOfClass(StoryNpc.class, getBoundingBox().inflate(24), n -> n.npcId().equals("horace"))) return n;
        return null;
    }

    /** Podstępy fazy 3: piasek w oczy (zapowiedź - schyla się) i udawany upadek (kontra z ziemi). */
    private void tickTricks(ServerLevel sl) {
        LivingEntity t = getTarget();
        if (sandWindup > 0) {
            getNavigation().stop();
            if (--sandWindup == 0) {
                setPose(Pose.STANDING);
                if (t != null && distanceTo(t) < 6) {
                    t.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));
                    sl.sendParticles(ParticleTypes.ASH, t.getX(), t.getEyeY(), t.getZ(), 30, 0.4, 0.3, 0.4, 0.05);
                    playSound(SoundEvents.SAND_BREAK, 1.5f, 0.8f);
                }
            }
            return;
        }
        if (feint > 0) {
            getNavigation().stop();
            feint--;
            if (t != null && distanceTo(t) < 3.0) {
                // kontra z ziemi - bez zapowiedzi
                feint = 0;
                setPose(Pose.STANDING);
                swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                t.hurt(damageSources().mobAttack(this), adjustSwingDamage(t, new Swing("kontra", 0, 12f, 3, -1, null)));
                announce("To był podstęp! Morgarath kontruje z ziemi.", ChatFormatting.RED);
            } else if (feint == 0) setPose(Pose.STANDING);
            return;
        }
        if (phase < 3 || t == null || busy() || winding() || trickCooldown > 0 || distanceTo(t) > 7) return;
        trickCooldown = 200 + random.nextInt(120);
        if (random.nextBoolean()) {
            sandWindup = 16;
            setPose(Pose.CROUCHING);
            announce("Morgarath schyla się po garść ziemi…", ChatFormatting.GRAY);
        } else {
            feint = 60;
            setPose(Pose.CROUCHING);
            announce("Morgarath zatacza się i pada na kolano…", ChatFormatting.GRAY);
        }
    }

    /** Ciosy pieszo: z góry, poziomy po łuku i pchnięcie; w szale i w gniewie szybsze. */
    private class DuelGoal extends Goal {
        DuelGoal() { setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }

        @Override
        public boolean canUse() {
            return !isPassenger() && getTarget() != null && getTarget().isAlive() && sandWindup == 0 && feint == 0;
        }

        @Override
        public void tick() {
            LivingEntity t = getTarget();
            if (t == null) return;
            // Horace wchodzi między nich: celem jest ten, kto stoi bliżej
            StoryNpc h = horace();
            if (h != null && !h.isDowned() && !h.stunned() && h.distanceTo(MorgarathEntity.this) + 0.8 < distanceTo(t)) t = h;
            getLookControl().setLookAt(t, 30f, 30f);
            if (winding()) return;
            double d = distanceTo(t);
            if (d > 3.0) getNavigation().moveTo(t, phase == 3 ? 1.2 : 1.05);
            else getNavigation().stop();
            if (d <= 3.8 && !busy()) {
                float speed = (phase == 3 ? 0.7f : 1f) * (enrage > 0 ? 0.75f : 1f);
                Swing s = switch (swingIndex++ % 3) {
                    case 0 -> new Swing("cięcie z góry", Math.round(16 * speed), 10f, 3.4, 0.5, null);
                    case 1 -> new Swing("cięcie poziome", Math.round(18 * speed), 9f, 3.9, -0.2, null);
                    default -> new Swing("pchnięcie", Math.round(12 * speed), 11f, 4.3, 0.85, null);
                };
                startSwing(s, t);
            }
        }
    }

    @Override
    protected void onSwingFinished(Swing s, boolean hit, @Nullable LivingEntity target) {
        if (target instanceof StoryNpc n && n.npcId().equals("horace")) {
            if (++horaceBlocks >= 6) {
                horaceBlocks = 0;
                n.stun(100);
                announce("Horace przyklęka pod gradem ciosów - Morgarath odwraca się do ciebie!", ChatFormatting.GOLD);
            }
        }
    }

    @Override
    protected float adjustSwingDamage(LivingEntity target, Swing s) {
        float dmg = s.damage();
        if (target instanceof ServerPlayer p && phase == 3 && !horaceSaved && p.getHealth() - dmg <= 1f
                && pl.zwiadowcy.companion.Trust.get(p, "horace") >= 70) {
            horaceSaved = true;
            StoryNpc h = horace();
            if (h != null) {
                h.recoverFromStun();
                h.teleportTo(p.getX() + 0.5, p.getY(), p.getZ() + 0.5);
            }
            announce("Horace zrywa się z ziemi i przyjmuje cios na tarczę - za ciebie!", ChatFormatting.GREEN);
            playSound(SoundEvents.SHIELD_BLOCK, 2f, 0.7f);
            return 0f;
        }
        return dmg;
    }

    @Override
    protected float armorFactor(DamageSource source) {
        if (source.is(DamageTypeTags.IS_FIRE)) return 2f;
        if (source.getDirectEntity() instanceof Projectile proj && !proj.isOnFire()) return 0.5f;
        return 1f;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // w siodle trafiają tylko gracz i jego pociski (rumak osłania jeźdźca przed resztą)
        if (phase == 1 && isPassenger() && !(source.getEntity() instanceof Player)) return false;
        return super.hurt(source, amount);
    }

    /** Ostatni cios zawsze zadaje gracz: inni (Horace, ogień, upadek) zbijają zdrowie najwyżej do 1. */
    @Override
    protected void actuallyHurt(DamageSource source, float amount) {
        if (!(source.getEntity() instanceof Player)) amount = Math.min(amount, getHealth() - 1f);
        if (amount <= 0) return;
        super.actuallyHurt(source, amount);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (level().isClientSide) return;
        fireEvent("morgarath_pokonany");
        // wrogowie wokół uciekają
        for (Mob m : level().getEntitiesOfClass(Mob.class, getBoundingBox().inflate(64), m -> m instanceof WargalEntity || m instanceof SkandianEntity)) {
            Vec3 away = m.position().subtract(position()).multiply(1, 0, 1).normalize().scale(40).add(m.position());
            m.setTarget(null);
            m.getNavigation().moveTo(away.x, m.getY(), away.z, 1.3);
            m.getPersistentData().putBoolean("zw_ucieka", true);
            pl.zwiadowcy.util.Delayed.run(200 + random.nextInt(100), () -> {
                if (m.isAlive()) m.discard();
            });
        }
    }

    private void announce(String text, ChatFormatting color) {
        for (ServerPlayer p : level().getEntitiesOfClass(ServerPlayer.class, getBoundingBox().inflate(64))) {
            p.displayClientMessage(Component.literal(text).withStyle(color), true);
        }
    }

    private void fireEvent(String id) {
        for (ServerPlayer p : level().getEntitiesOfClass(ServerPlayer.class, getBoundingBox().inflate(96))) QuestEngine.fireEvent(p, id, 1);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossEvent.removePlayer(player);
    }

    @Override
    public void remove(RemovalReason reason) {
        bossEvent.removeAllPlayers();
        super.remove(reason);
    }

    @Override
    public boolean removeWhenFarAway(double distance) { return false; }

    @Override protected SoundEvent getAmbientSound() { return SoundEvents.WITHER_SKELETON_AMBIENT; }
    @Override protected SoundEvent getHurtSound(DamageSource src) { return SoundEvents.WITHER_SKELETON_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.WITHER_SKELETON_DEATH; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Phase", phase);
        tag.putBoolean("Mounted", mounted);
        tag.putBoolean("HoraceSaved", horaceSaved);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        phase = Math.max(1, tag.getInt("Phase"));
        mounted = tag.getBoolean("Mounted");
        horaceSaved = tag.getBoolean("HoraceSaved");
    }
}
