package pl.zwiadowcy.entity;

import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Pasek życia bossa tylko dla graczy w pobliżu albo walczących z nim. Vanilla pokazuje go każdemu, kto "śledzi" byt,
 * a to bywa ponad 100 bloków - herszt z kryjówki za lasem wisiał wtedy na ekranie np. w obozie Zlotu.
 */
final class BossBarRange {

    static void update(Mob boss, ServerBossEvent bar, double range) {
        if (!(boss.level() instanceof ServerLevel level) || boss.tickCount % 10 != 0) return;
        Set<ServerPlayer> want = new HashSet<>();
        if (boss.isAlive()) {
            for (ServerPlayer p : level.players()) {
                if (!p.isAlive() || p.isSpectator()) continue;
                double d = p.distanceToSqr(boss);
                if (d <= range * range || (boss.getTarget() == p && d <= 64 * 64)) want.add(p);
            }
        }
        for (ServerPlayer p : new ArrayList<>(bar.getPlayers())) if (!want.contains(p)) bar.removePlayer(p);
        for (ServerPlayer p : want) if (!bar.getPlayers().contains(p)) bar.addPlayer(p);
    }

    private BossBarRange() {}
}
