package pl.zwiadowcy.quest;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.Blocks;
import pl.zwiadowcy.util.Delayed;

import javax.annotation.Nullable;

/**
 * Zawał w kopalni (akcja misji "zawal"): przez kilka sekund ze stropu sypie się żwir wokół wskazanego miejsca -
 * lekkie obrażenia i dudnienie, a gracz ucieka do bezpiecznej komory.
 */
public final class Rockfall {

    public static void start(ServerPlayer p, @Nullable BlockPos at, int radius, int seconds) {
        if (at == null || !(p.level() instanceof ServerLevel level)) return;
        level.playSound(null, at, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 1.5f, 0.5f);
        for (int t = 0; t < seconds * 4; t++) {
            Delayed.run(t * 5, () -> drop(level, at, radius));
        }
    }

    private static void drop(ServerLevel level, BlockPos at, int radius) {
        for (int i = 0; i < 2; i++) {
            int dx = level.random.nextInt(radius * 2 + 1) - radius, dz = level.random.nextInt(radius * 2 + 1) - radius;
            BlockPos base = at.offset(dx, 0, dz);
            // nie zasypujemy postaci (Dafydd w swojej kryjówce, towarzysze)
            if (!level.getEntitiesOfClass(pl.zwiadowcy.entity.StoryNpc.class, new net.minecraft.world.phys.AABB(base).inflate(3, 4, 3)).isEmpty()) continue;
            // szukamy stropu nad chodnikiem (do 4 bloków w górę)
            BlockPos ceiling = null;
            for (int up = 1; up <= 4; up++) {
                if (!level.getBlockState(base.above(up)).isAir()) {
                    ceiling = base.above(up - 1);
                    break;
                }
            }
            if (ceiling == null || !level.getBlockState(ceiling).isAir()) continue;
            FallingBlockEntity falling = FallingBlockEntity.fall(level, ceiling, Blocks.GRAVEL.defaultBlockState());
            falling.setHurtsEntities(1.0f, 3);
        }
        if (level.random.nextInt(4) == 0) level.playSound(null, at, SoundEvents.GRAVEL_BREAK, SoundSource.BLOCKS, 1.2f, 0.6f);
    }

    private Rockfall() {}
}
