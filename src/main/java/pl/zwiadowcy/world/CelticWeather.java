package pl.zwiadowcy.world;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import pl.zwiadowcy.quest.QuestCapability;
import pl.zwiadowcy.volume.Volumes;

/**
 * Deszczowa Celtyka: pogoda jest wspólna dla całego wymiaru, więc gdy gracz (gra jednoosobowa) przebywa w biomach
 * Celtyki, częściej pada - ok. 60% czasu. Po wyjeździe z Celtyki „celtycki” deszcz przestaje padać.
 */
public final class CelticWeather {
    private static boolean ours;

    /** Co 20 sekund dla każdego gracza. */
    public static void tick(ServerPlayer p) {
        if (p.tickCount % 400 != 0 || !(p.level() instanceof ServerLevel level) || !Araluen.isAraluen(level)) return;
        if (!Volumes.tom2Started(QuestCapability.get(p))) return;
        String biome = level.getBiome(p.blockPosition()).unwrapKey().map(k -> k.location().getPath()).orElse("");
        boolean celtic = biome.equals("wrzosowiska_celtyki") || biome.equals("mgliste_doliny") || biome.equals("lupkowe_wzgorza");
        if (celtic) {
            if (!level.isRaining() && level.random.nextFloat() < 0.35f) {
                level.setWeatherParameters(0, 2400 + level.random.nextInt(2400), true, false);
                ours = true;
            } else if (level.isRaining() && ours && level.random.nextFloat() < 0.12f) {
                level.setWeatherParameters(1200 + level.random.nextInt(1200), 0, false, false);
            }
        } else if (ours && level.isRaining()) {
            level.setWeatherParameters(6000, 0, false, false);
            ours = false;
        }
    }

    private CelticWeather() {}
}
