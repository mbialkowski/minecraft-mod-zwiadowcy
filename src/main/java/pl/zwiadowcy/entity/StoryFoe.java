package pl.zwiadowcy.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import pl.zwiadowcy.difficulty.StoryDifficulty;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Przeciwnik z zapowiadanymi ciosami (tom II: Jeździec Morgaratha, Skandianie, Morgarath). Przed ciosem widać zamach,
 * błysk na broni i słychać świst - tyle trwa okno na unik (zejście z linii ciosu albo odskok).
 * Po ciosie, który chybił albo trafił w tarczę Horace'a, na chwilę odsłania się słaby punkt: trafienie w tym czasie
 * zadaje potrójne obrażenia (na poziomach Opowieść i Zwiadowca słaby punkt świeci).
 */
public abstract class StoryFoe extends Monster {
    private static final EntityDataAccessor<Integer> WINDUP = SynchedEntityData.defineId(StoryFoe.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> WEAK = SynchedEntityData.defineId(StoryFoe.class, EntityDataSerializers.INT);

    /** Zapowiadany cios: zamach (tiki przy domyślnej trudności), obrażenia, zasięg, dodatkowy efekt po trafieniu. */
    public record Swing(String name, int windup, float damage, double reach, double arcCos, @Nullable Consumer<LivingEntity> onHit) {}

    @Nullable private Swing pending;
    @Nullable private LivingEntity pendingTarget;
    protected int swingCooldown;
    /** Szarża konna: czy kopia już trafiła w tym przejeździe. */
    protected boolean chargeHit;

    protected StoryFoe(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(WINDUP, 0);
        entityData.define(WEAK, 0);
    }

    public boolean hitThisCharge() { return chargeHit; }
    public void resetCharge() { chargeHit = false; }

    public boolean winding() { return entityData.get(WINDUP) > 0; }
    public boolean weakOpen() { return entityData.get(WEAK) > 0; }

    /** Poziom trudności najbliższego gracza (długość zapowiedzi, świecenie słabego punktu). */
    protected StoryDifficulty difficulty() {
        Player p = level().getNearestPlayer(this, 48);
        return StoryDifficulty.of(p);
    }

    protected boolean busy() {
        return pending != null || swingCooldown > 0;
    }

    /** Zaczyna zapowiadany cios w cel. */
    protected void startSwing(Swing s, LivingEntity target) {
        if (pending != null) return;
        pending = s;
        pendingTarget = target;
        int ticks = Math.max(8, Math.round(s.windup() * difficulty().telegraphTicks / 16f));
        entityData.set(WINDUP, ticks);
        playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.2f, 0.55f);
        getNavigation().stop();
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;
        if (swingCooldown > 0) swingCooldown--;
        int weak = entityData.get(WEAK);
        if (weak > 0) {
            entityData.set(WEAK, weak - 1);
            if (weak % 4 == 0 && difficulty().weakPointGlow && level() instanceof ServerLevel sl) {
                Vec3 side = Vec3.directionFromRotation(0, yBodyRot + 90).scale(0.45);
                sl.sendParticles(ParticleTypes.GLOW, getX() + side.x, getY() + getBbHeight() * 0.6, getZ() + side.z, 2, 0.05, 0.1, 0.05, 0);
            }
        }
        int w = entityData.get(WINDUP);
        if (pending == null || w <= 0) return;
        if (pendingTarget != null) getLookControl().setLookAt(pendingTarget, 30f, 30f);
        if (w % 4 == 0 && level() instanceof ServerLevel sl) {
            Vec3 hand = position().add(Vec3.directionFromRotation(0, yBodyRot).scale(0.6)).add(0, getBbHeight() * 0.8, 0);
            sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, hand.x, hand.y, hand.z, 3, 0.15, 0.15, 0.15, 0.02);
        }
        entityData.set(WINDUP, w - 1);
        if (w - 1 > 0) return;
        Swing s = pending;
        LivingEntity t = pendingTarget;
        pending = null;
        pendingTarget = null;
        swing(InteractionHand.MAIN_HAND);
        swingCooldown = 20;
        boolean hit = false;
        if (t != null && t.isAlive() && distanceTo(t) <= s.reach() + t.getBbWidth() / 2) {
            Vec3 look = Vec3.directionFromRotation(0, yBodyRot);
            Vec3 to = t.position().subtract(position()).multiply(1, 0, 1).normalize();
            if (look.dot(to) >= s.arcCos()) {
                float before = t.getHealth();
                float dmg = adjustSwingDamage(t, s);
                hit = dmg > 0 && t.hurt(damageSources().mobAttack(this), dmg) && t.getHealth() < before;
                if (hit && s.onHit() != null) s.onHit().accept(t);
            }
        }
        // chybiony albo zablokowany cios (unik, tarcza Horace'a): słaby punkt odsłonięty
        if (!hit || (t instanceof StoryNpc n && n.npcId().equals("horace"))) entityData.set(WEAK, 30);
        onSwingFinished(s, hit, t);
    }

    /** Obrażenia ciosu tuż przed trafieniem (Morgarath: przyspieszenie w szale, Horace przyjmuje śmiertelny cios). */
    protected float adjustSwingDamage(LivingEntity target, Swing s) {
        return s.damage();
    }

    /** Wywoływane po każdym zapowiadanym ciosie. */
    protected void onSwingFinished(Swing s, boolean hit, @Nullable LivingEntity target) {}

    /** Mnożnik obrażeń od broni (zbroja) - Morgarath: 0.5 dla zwykłych strzał i noży. */
    protected float armorFactor(DamageSource source) {
        return 1f;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        float a = amount * armorFactor(source);
        if (weakOpen() && (source.getEntity() instanceof ServerPlayer || source.getDirectEntity() instanceof Projectile)) {
            a *= 3f;
            entityData.set(WEAK, 0);
            playSound(SoundEvents.PLAYER_ATTACK_CRIT, 1.2f, 0.8f);
        }
        // trafienie przerywa zamach tylko u słabszych przeciwników
        return super.hurt(source, a);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
    }
}
