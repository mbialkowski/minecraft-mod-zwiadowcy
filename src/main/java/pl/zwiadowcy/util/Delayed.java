package pl.zwiadowcy.util;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pl.zwiadowcy.Zwiadowcy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Proste zadania odłożone o kilka ticków serwera (podróż przez ognisko, skrypty scen). */
@Mod.EventBusSubscriber(modid = Zwiadowcy.MODID)
public final class Delayed {
    private record Task(long at, Runnable action) {}

    private static final List<Task> TASKS = new ArrayList<>();
    private static long now;

    public static void run(int ticks, Runnable action) {
        TASKS.add(new Task(now + Math.max(1, ticks), action));
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        now++;
        if (TASKS.isEmpty()) return;
        List<Task> due = new ArrayList<>();
        for (Iterator<Task> it = TASKS.iterator(); it.hasNext(); ) {
            Task t = it.next();
            if (t.at <= now) {
                due.add(t);
                it.remove();
            }
        }
        for (Task t : due) {
            try {
                t.action.run();
            } catch (Exception e) {
                Zwiadowcy.LOGGER.error("Błąd zadania odłożonego", e);
            }
        }
    }

    @SubscribeEvent
    public static void onStopped(ServerStoppedEvent event) {
        TASKS.clear();
    }

    private Delayed() {}
}
