package pl.zwiadowcy.quest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import pl.zwiadowcy.world.Araluen;

import javax.annotation.Nullable;

/**
 * Warunki w dialogach i akcjach. Lista "when" to koniunkcja obiektów, np.
 * {"quest_stage": "cieciwa", "stage": 2} albo {"flag": "horace_przyjaciel"}.
 */
public final class Conditions {

    public static boolean test(ServerPlayer p, @Nullable JsonElement when) {
        if (when == null || when.isJsonNull()) return true;
        if (when.isJsonObject()) return testOne(p, when.getAsJsonObject());
        for (JsonElement el : when.getAsJsonArray()) {
            if (!testOne(p, el.getAsJsonObject())) return false;
        }
        return true;
    }

    private static boolean testOne(ServerPlayer p, JsonObject c) {
        QuestProgress prog = QuestCapability.get(p);
        if (c.has("quest_active") && !prog.isActive(GsonHelper.getAsString(c, "quest_active"))) return false;
        if (c.has("quest_stage")) {
            QuestProgress.State st = prog.get(GsonHelper.getAsString(c, "quest_stage"));
            if (st == null || st.status != QuestProgress.Status.ACTIVE) return false;
            if (c.has("stage") && st.stage != GsonHelper.getAsInt(c, "stage")) return false;
            if (c.has("stage_min") && st.stage < GsonHelper.getAsInt(c, "stage_min")) return false;
        }
        if (c.has("quest_completed") && !prog.isCompleted(GsonHelper.getAsString(c, "quest_completed"))) return false;
        if (c.has("quest_not_completed") && prog.isCompleted(GsonHelper.getAsString(c, "quest_not_completed"))) return false;
        if (c.has("quest_not_started") && prog.get(GsonHelper.getAsString(c, "quest_not_started")) != null) return false;
        if (c.has("quest_ready") && !questReady(p, prog, GsonHelper.getAsString(c, "quest_ready"))) return false;
        if (c.has("flag") && !prog.hasFlag(GsonHelper.getAsString(c, "flag"))) return false;
        if (c.has("not_flag") && prog.hasFlag(GsonHelper.getAsString(c, "not_flag"))) return false;
        if (c.has("has_item")) {
            int need = GsonHelper.getAsInt(c, "count", 1);
            if (QuestEngine.countItems(p, GsonHelper.getAsString(c, "has_item")) < need) return false;
        }
        if (c.has("not_has_item") && QuestEngine.countItems(p, GsonHelper.getAsString(c, "not_has_item")) > 0) return false;
        if (c.has("cooldown_ready")) {
            long ticks = GsonHelper.getAsLong(c, "ticks", 24000L);
            if (QuestEngine.gameTime(p) - prog.cooldown(GsonHelper.getAsString(c, "cooldown_ready")) < ticks) return false;
        }
        if (c.has("cooldown_active")) {
            long ticks = GsonHelper.getAsLong(c, "ticks", 24000L);
            if (QuestEngine.gameTime(p) - prog.cooldown(GsonHelper.getAsString(c, "cooldown_active")) >= ticks) return false;
        }
        if (c.has("night") && GsonHelper.getAsBoolean(c, "night") != QuestEngine.isNight(p)) return false;
        if (c.has("in_araluen") && GsonHelper.getAsBoolean(c, "in_araluen") != Araluen.isAraluen(p.level())) return false;
        return true;
    }

    /** Zlecenie powtarzalne: nieaktywne i ostatnio wykonane co najmniej dobę (24000 ticków) temu. */
    private static boolean questReady(ServerPlayer p, QuestProgress prog, String id) {
        QuestProgress.State st = prog.get(id);
        if (st == null) return true;
        if (st.status == QuestProgress.Status.ACTIVE) return false;
        return QuestEngine.gameTime(p) - st.completedAt >= 24000L;
    }

    private Conditions() {}
}
