package pl.zwiadowcy.stealth;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pl.zwiadowcy.Zwiadowcy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Chmury dymu z garnków: gracz w środku jest całkowicie ukryty, wrogowie w dymie i patrzący przez dym tracą cel,
 * a pasek podejrzenia zamarza.
 */
@Mod.EventBusSubscriber(modid = Zwiadowcy.MODID)
public final class SmokeClouds {
    private record Cloud(ServerLevel level, Vec3 center, double radius, long until) {}

    private static final List<Cloud> CLOUDS = new ArrayList<>();

    public static void add(ServerLevel level, Vec3 center, double radius, int ticks) {
        CLOUDS.add(new Cloud(level, center, radius, level.getGameTime() + ticks));
        for (Mob m : level.getEntitiesOfClass(Mob.class, new net.minecraft.world.phys.AABB(center, center).inflate(radius + 1))) {
            if (m.getTarget() instanceof Player) {
                m.setTarget(null);
                m.getNavigation().stop();
            }
        }
    }

    /** Czy punkt leży w dymie albo odcinek wzroku przechodzi przez dym. */
    public static boolean blocksSight(ServerLevel level, Vec3 eye, Vec3 target) {
        long now = level.getGameTime();
        for (Cloud c : CLOUDS) {
            if (c.level != level || c.until < now) continue;
            if (distanceToSegment(c.center, eye, target) <= c.radius) return true;
        }
        return false;
    }

    public static boolean inSmoke(ServerLevel level, Vec3 pos) {
        long now = level.getGameTime();
        for (Cloud c : CLOUDS) {
            if (c.level == level && c.until >= now && c.center.distanceToSqr(pos) <= c.radius * c.radius) return true;
        }
        return false;
    }

    private static double distanceToSegment(Vec3 p, Vec3 a, Vec3 b) {
        Vec3 ab = b.subtract(a);
        double len2 = ab.lengthSqr();
        double t = len2 < 1e-6 ? 0 : Math.max(0, Math.min(1, p.subtract(a).dot(ab) / len2));
        return p.distanceTo(a.add(ab.scale(t)));
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || CLOUDS.isEmpty()) return;
        for (Iterator<Cloud> it = CLOUDS.iterator(); it.hasNext(); ) {
            Cloud c = it.next();
            long now = c.level.getGameTime();
            if (c.until < now) {
                it.remove();
                continue;
            }
            if (now % 4 == 0) {
                c.level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, c.center.x, c.center.y + 1, c.center.z, 6,
                        c.radius * 0.5, 1.0, c.radius * 0.5, 0.01);
                c.level.sendParticles(ParticleTypes.LARGE_SMOKE, c.center.x, c.center.y + 0.8, c.center.z, 10,
                        c.radius * 0.6, 0.8, c.radius * 0.6, 0.01);
            }
            // wrogowie w dymie tracą cel
            if (now % 10 == 0) {
                for (Mob m : c.level.getEntitiesOfClass(Mob.class, new net.minecraft.world.phys.AABB(c.center, c.center).inflate(c.radius))) {
                    if (m.getTarget() instanceof Player) m.setTarget(null);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onStopped(ServerStoppedEvent event) {
        CLOUDS.clear();
    }

    private SmokeClouds() {}
}
