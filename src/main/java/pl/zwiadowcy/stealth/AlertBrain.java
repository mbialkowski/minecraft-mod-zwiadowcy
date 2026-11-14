package pl.zwiadowcy.stealth;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import pl.zwiadowcy.difficulty.StoryDifficulty;
import pl.zwiadowcy.skill.Skills;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * „Mózg” czujności wroga w skradaniu 2.0: patrol po trasie, stożek widzenia zależny od pory dnia i światła,
 * pasek podejrzenia, sprawdzanie ostatniego miejsca, alarm (krzyk albo bęben dobosza), powrót do spokoju
 * i ogłuszenie od tyłu.
 */
public class AlertBrain {
    public static final int CALM = 0, SUSPICIOUS = 1, ALARM = 2, KNOCKED_OUT = 3;
    /** Po tylu tikach bez kontaktu alarm opada do podejrzenia (90 s). */
    private static final int ALARM_COOLDOWN = 1800;
    /** Szukanie w ostatnim znanym miejscu (20 s). */
    private static final int SEARCH_TICKS = 400;
    private static final int DRUM_WINDUP = 60;
    private static final int SHOUT_RADIUS = 12, DRUM_RADIUS = 40;

    private final Mob mob;
    private int state = CALM;
    private float meter;
    @Nullable private BlockPos lastKnown;
    private int searchTicks;
    private int alarmTicks;
    private int drumTicks = -1;
    private final List<BlockPos> route = new ArrayList<>();
    private int routeIndex;
    private int waitTicks;
    @Nullable private ServerPlayer suspect;

    public AlertBrain(Mob mob) {
        this.mob = mob;
    }

    public int state() { return state; }
    public float meter() { return meter; }
    public boolean knockedOut() { return state == KNOCKED_OUT; }
    public boolean alarmed() { return state == ALARM; }

    public void setRoute(List<BlockPos> points) {
        route.clear();
        route.addAll(points);
        routeIndex = 0;
    }

    /** Czy mózg kieruje ruchem (patrol, sprawdzanie) - wtedy zwykłe łażenie moba jest wyłączone. */
    public boolean controlsMovement() {
        return state == SUSPICIOUS || state == KNOCKED_OUT || (state == CALM && !route.isEmpty()) || drumTicks >= 0;
    }

    /** Czy mob może sam z siebie atakować gracza (tylko po alarmie). */
    public boolean mayTarget() {
        return state == ALARM && drumTicks < 0;
    }

    // ------------------------------------------------------------------ tick

    public void tick() {
        if (!(mob.level() instanceof ServerLevel level) || !Watchful.active(mob)) return;
        if (state == KNOCKED_OUT) {
            if (mob.getPose() != Pose.SLEEPING) mob.setPose(Pose.SLEEPING);
            mob.getNavigation().stop();
            mob.setTarget(null);
            return;
        }
        if (drumTicks >= 0) {
            tickDrum(level);
            return;
        }
        if (mob.tickCount % 4 == 0) sense(level);
        switch (state) {
            case CALM -> patrol();
            case SUSPICIOUS -> investigate();
            case ALARM -> {
                ServerPlayer target = mob.getTarget() instanceof ServerPlayer sp ? sp : suspect;
                if (target != null && target.isAlive() && mob.hasLineOfSight(target) && mob.distanceTo(target) < 32) {
                    alarmTicks = 0;
                    lastKnown = target.blockPosition();
                    if (mob.getTarget() == null) mob.setTarget(target);
                } else if (++alarmTicks >= ALARM_COOLDOWN) {
                    state = SUSPICIOUS;
                    meter = 0.6f;
                    mob.setTarget(null);
                    searchTicks = 0;
                }
            }
            default -> {}
        }
        sync();
    }

    /** Wzrok i słuch: gracze w pobliżu, ogłuszeni towarzysze w zasięgu wzroku. */
    private void sense(ServerLevel level) {
        if (state == ALARM) return;
        // wartownik w dymie nic nie widzi, a jego podejrzenie zamarza
        if (SmokeClouds.inSmoke(level, mob.getEyePosition())) return;
        boolean noticed = false;
        boolean night = isNight(level);
        for (ServerPlayer p : level.players()) {
            if (p.isSpectator() || p.isCreative() || p.distanceToSqr(mob) > 40 * 40) continue;
            double dist = p.distanceTo(mob);
            if (dist < 1.6) {
                // wpadł prosto na wartownika
                suspect = p;
                raiseAlarm(level, p);
                return;
            }
            double vis = Stealth.visibility(p);
            double range = night ? 12 : 24;
            if (night && level.getBrightness(LightLayer.BLOCK, p.blockPosition()) >= 8) range += 8;
            double eff = range * (0.3 + 0.7 * vis);
            double half = Math.toRadians(night ? 40 : 55);
            double gain = 0;
            boolean smoke = SmokeClouds.inSmoke(level, p.position()) || SmokeClouds.blocksSight(level, mob.getEyePosition(), p.getEyePosition());
            if (smoke) continue;
            if (vis > 0 && dist <= eff && inCone(p.getEyePosition(), half) && mob.hasLineOfSight(p)) {
                gain = (1.6 - 1.2 * dist / eff) * 0.2;
            }
            // słuch: bieg w pobliżu i kroki po głośnym podłożu (bez „Cichego kroku”)
            if (p.isSprinting() && dist < 12) gain += 0.08;
            else if (!p.isCrouching() && dist < 5 && Stealth.noisyGround(p) && !Skills.has(p, Skills.CICHY_KROK)) gain += 0.04;
            if (gain <= 0) continue;
            gain *= StoryDifficulty.of(p).suspicion;
            if (Skills.has(p, Skills.ZIMNA_KREW)) gain *= 0.75;
            meter = Math.min(1f, meter + (float) gain);
            lastKnown = p.blockPosition();
            suspect = p;
            noticed = true;
            if (meter >= 1f) {
                raiseAlarm(level, p);
                return;
            }
        }
        // ogłuszony kompan w zasięgu wzroku: wartownik od razu staje się podejrzliwy
        if (!noticed) {
            for (Mob other : level.getEntitiesOfClass(Mob.class, mob.getBoundingBox().inflate(16),
                    m -> m != mob && m instanceof Watchful w && w.alertBrain().knockedOut())) {
                if (inCone(other.position().add(0, 0.3, 0), Math.toRadians(55)) && mob.hasLineOfSight(other)) {
                    meter = Math.max(meter, 0.9f);
                    lastKnown = other.blockPosition();
                    noticed = true;
                    break;
                }
            }
        }
        if (noticed && state == CALM && meter > 0.15f) {
            state = SUSPICIOUS;
            searchTicks = 0;
            if (((Watchful) mob).shoutsOnSuspicion() && lastKnown != null) {
                mob.playSound(SoundEvents.VINDICATOR_AMBIENT, 1.5f, 0.6f);
                for (Mob other : level.getEntitiesOfClass(Mob.class, mob.getBoundingBox().inflate(SHOUT_RADIUS),
                        m -> m != mob && Watchful.active(m))) ((Watchful) other).alertBrain().hearNoise(lastKnown);
            }
        } else if (!noticed && meter > 0) {
            meter = Math.max(0f, meter - 0.02f);
        }
    }

    private boolean inCone(Vec3 point, double halfAngle) {
        Vec3 eye = mob.getEyePosition();
        Vec3 diff = point.subtract(eye);
        Vec3 look = Vec3.directionFromRotation(0, mob.getYHeadRot());
        Vec3 flat = new Vec3(diff.x, 0, diff.z);
        if (flat.lengthSqr() < 1e-4) return true;
        return look.dot(flat.normalize()) >= Math.cos(halfAngle);
    }

    private static boolean isNight(ServerLevel level) {
        long t = Math.floorMod(level.getDayTime(), 24000L);
        return t >= 13000 && t <= 23000;
    }

    private void patrol() {
        if (route.isEmpty()) return;
        if (waitTicks > 0) {
            waitTicks--;
            if (waitTicks % 20 == 0) mob.setYHeadRot(mob.getYHeadRot() + (mob.getRandom().nextFloat() - 0.5f) * 90f);
            return;
        }
        BlockPos target = route.get(routeIndex % route.size());
        if (target.distToCenterSqr(mob.position()) < 2.5 * 2.5) {
            routeIndex = (routeIndex + 1) % route.size();
            waitTicks = 40 + mob.getRandom().nextInt(41);
            mob.getNavigation().stop();
        } else if (mob.getNavigation().isDone()) {
            mob.getNavigation().moveTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5, 0.75);
        }
    }

    private void investigate() {
        if (lastKnown == null) {
            calmDown();
            return;
        }
        if (searchTicks > 0) {
            if (--searchTicks == 0) calmDown();
            else if (searchTicks % 30 == 0) mob.setYHeadRot(mob.getYHeadRot() + (mob.getRandom().nextFloat() - 0.5f) * 140f);
            return;
        }
        if (lastKnown.distToCenterSqr(mob.position()) < 2.5 * 2.5) {
            searchTicks = SEARCH_TICKS;
            mob.getNavigation().stop();
        } else if (mob.getNavigation().isDone()) {
            mob.getNavigation().moveTo(lastKnown.getX() + 0.5, lastKnown.getY(), lastKnown.getZ() + 0.5, 0.9);
            mob.getLookControl().setLookAt(lastKnown.getX() + 0.5, lastKnown.getY() + 1, lastKnown.getZ() + 0.5);
        }
    }

    private void calmDown() {
        state = CALM;
        meter = 0;
        lastKnown = null;
        searchTicks = 0;
        suspect = null;
    }

    // ------------------------------------------------------------------ alarm

    public void raiseAlarm(ServerLevel level, ServerPlayer p) {
        if (state == ALARM || state == KNOCKED_OUT) return;
        suspect = p;
        lastKnown = p.blockPosition();
        meter = 1f;
        if (((Watchful) mob).isDrummer()) {
            // dobosz najpierw biegnie do bębna: 3 s rozbiegu, trafienie przerywa
            drumTicks = DRUM_WINDUP;
            mob.getNavigation().stop();
            mob.playSound(SoundEvents.WOLF_GROWL, 1.5f, 0.6f);
            sync();
            return;
        }
        becomeAlarmed(p);
        mob.playSound(SoundEvents.WOLF_HOWL, 2.0f, 0.75f);
        broadcast(level, p, SHOUT_RADIUS);
        Alertness.onAlarm(mob, p);
    }

    private void tickDrum(ServerLevel level) {
        mob.getNavigation().stop();
        if (suspect != null) mob.getLookControl().setLookAt(suspect, 30f, 30f);
        if (drumTicks % 10 == 0) level.sendParticles(ParticleTypes.NOTE, mob.getX(), mob.getEyeY() + 0.6, mob.getZ(), 1, 0.2, 0.1, 0.2, 0);
        if (--drumTicks <= 0) {
            drumTicks = -1;
            ServerPlayer p = suspect;
            for (int i = 0; i < 3; i++) {
                level.playSound(null, mob.blockPosition(), SoundEvents.NOTE_BLOCK_BASEDRUM.value(), SoundSource.HOSTILE, 3f, 0.6f + i * 0.05f);
            }
            if (p != null) {
                becomeAlarmed(p);
                broadcast(level, p, DRUM_RADIUS);
                Alertness.onAlarm(mob, p);
            } else {
                calmDown();
            }
        }
        sync();
    }

    /** Trafienie przerywa rozbieg dobosza (zaczyna od nowa po chwili). */
    public void onHurt() {
        if (drumTicks >= 0) {
            drumTicks = DRUM_WINDUP;
        }
    }

    private void becomeAlarmed(ServerPlayer p) {
        state = ALARM;
        alarmTicks = 0;
        mob.setTarget(p);
    }

    private void broadcast(ServerLevel level, ServerPlayer p, int radius) {
        for (Mob other : level.getEntitiesOfClass(Mob.class, mob.getBoundingBox().inflate(radius),
                m -> m != mob && Watchful.active(m))) {
            AlertBrain b = ((Watchful) other).alertBrain();
            if (b.state == KNOCKED_OUT || b.state == ALARM) continue;
            b.suspect = p;
            b.lastKnown = p.blockPosition();
            b.meter = 1f;
            b.drumTicks = -1;
            b.becomeAlarmed(p);
            b.sync();
        }
    }

    /** Hałas (kamyk, strzała gwiżdżąca): spokojny albo podejrzliwy wróg idzie sprawdzić miejsce. */
    public void hearNoise(BlockPos pos) {
        if (state == ALARM || state == KNOCKED_OUT) return;
        state = SUSPICIOUS;
        meter = Math.max(meter, 0.35f);
        lastKnown = pos;
        searchTicks = 0;
        mob.getNavigation().stop();
        sync();
    }

    /** Ogłuszenie od tyłu: wróg leży do końca misji, a ogłuszonego może zauważyć inny wartownik. */
    public void knockOut() {
        state = KNOCKED_OUT;
        meter = 0;
        drumTicks = -1;
        mob.setTarget(null);
        mob.getNavigation().stop();
        mob.setPose(Pose.SLEEPING);
        mob.setSilent(true);
        sync();
    }

    public void sync() {
        ((Watchful) mob).setSyncedAlert(drumTicks >= 0 ? ALARM : state, Mth.clamp(Math.round(meter * 100), 0, 100));
    }

    // ------------------------------------------------------------------ zapis

    public void save(CompoundTag tag) {
        CompoundTag t = new CompoundTag();
        t.putInt("State", state == ALARM ? SUSPICIOUS : state);
        t.putFloat("Meter", state == ALARM ? 0.6f : meter);
        ListTag r = new ListTag();
        for (BlockPos p : route) r.add(NbtUtils.writeBlockPos(p));
        t.put("Route", r);
        t.putInt("RouteIndex", routeIndex);
        tag.put("Czujnosc", t);
    }

    public void load(CompoundTag tag) {
        if (!tag.contains("Czujnosc")) return;
        CompoundTag t = tag.getCompound("Czujnosc");
        state = t.getInt("State");
        meter = t.getFloat("Meter");
        route.clear();
        ListTag r = t.getList("Route", Tag.TAG_COMPOUND);
        for (int i = 0; i < r.size(); i++) route.add(NbtUtils.readBlockPos(r.getCompound(i)));
        routeIndex = t.getInt("RouteIndex");
        if (state == KNOCKED_OUT) mob.setPose(Pose.SLEEPING);
    }

    /** Pomocnicze dla zadań misji: czy wróg jest wyeliminowany (zabity albo ogłuszony). */
    public static boolean eliminated(Mob mob) {
        return !mob.isAlive() || (mob instanceof Watchful w && w.alertBrain().knockedOut());
    }
}
