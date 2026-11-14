package pl.zwiadowcy.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import pl.zwiadowcy.quest.QuestActions;

import java.util.EnumSet;

/**
 * Marsz po kolejnych punktach zapisanych w danych bytu ({@link QuestActions#setMarch}): kolumna Wargali wchodząca
 * na most, szyki w bitwie. Walka ma pierwszeństwo - gdy byt ma cel ataku, marsz czeka.
 */
public class MarchGoal extends Goal {
    private final PathfinderMob mob;
    private final double speed;
    /** Punkt, na którym marsz się kończy (np. stłoczona kolumna stoi przed końcem mostu); -1 = do końca trasy. */
    private final int stopBefore;

    public MarchGoal(PathfinderMob mob, double speed) {
        this(mob, speed, -1);
    }

    public MarchGoal(PathfinderMob mob, double speed, int stopBefore) {
        this.mob = mob;
        this.speed = speed;
        this.stopBefore = stopBefore;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    private long[] route() {
        return mob.getPersistentData().getLongArray(QuestActions.TAG_MARCH);
    }

    private int index() {
        return mob.getPersistentData().getInt(QuestActions.TAG_MARCH_I);
    }

    private int limit() {
        long[] r = route();
        int stop = mob.getPersistentData().contains("zw_marsz_stop") ? mob.getPersistentData().getInt("zw_marsz_stop") : stopBefore;
        return stop >= 0 ? Math.min(r.length, stop) : r.length;
    }

    @Override
    public boolean canUse() {
        return mob.getTarget() == null && mob.getPersistentData().contains(QuestActions.TAG_MARCH) && index() < limit();
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void tick() {
        long[] r = route();
        int i = index();
        if (i >= r.length) return;
        BlockPos wp = BlockPos.of(r[i]);
        Vec3 target = Vec3.atBottomCenterOf(wp).add(mob.getPersistentData().getDouble("zw_marsz_dx"), 0, mob.getPersistentData().getDouble("zw_marsz_dz"));
        if (mob.position().distanceToSqr(target.x, mob.getY(), target.z) < 2.0 * 2.0) {
            mob.getPersistentData().putInt(QuestActions.TAG_MARCH_I, i + 1);
            return;
        }
        if (mob.getNavigation().isDone() || mob.tickCount % 40 == 0) mob.getNavigation().moveTo(target.x, target.y, target.z, speed);
    }

    @Override
    public void stop() {
        mob.getNavigation().stop();
    }
}
