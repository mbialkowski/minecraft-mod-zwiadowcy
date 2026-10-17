package pl.zwiadowcy.quest;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Misja wczytana z data/zwiadowcy/quests/&lt;id&gt;.json.
 */
public record QuestDefinition(String id, String title, String act, String description, int order,
                              boolean repeatable, @Nullable String advancement, List<Stage> stages,
                              JsonArray onStart, JsonArray onComplete) {

    public record Objective(String type, JsonObject params) {
        public int count() {
            return GsonHelper.getAsInt(params, "count", 1);
        }

        public String str(String key) {
            return GsonHelper.getAsString(params, key, "");
        }

        public double dbl(String key, double def) {
            return GsonHelper.getAsDouble(params, key, def);
        }

        public boolean bool(String key) {
            return GsonHelper.getAsBoolean(params, key, false);
        }

        public List<String> list(String key) {
            List<String> out = new ArrayList<>();
            if (params.has(key)) {
                JsonElement el = params.get(key);
                if (el.isJsonArray()) el.getAsJsonArray().forEach(e -> out.add(e.getAsString()));
                else out.add(el.getAsString());
            }
            return out;
        }
    }

    public record Stage(Objective objective, String text, @Nullable String location,
                        JsonArray onStart, JsonArray onComplete, Map<String, JsonArray> triggers) {}

    public static QuestDefinition parse(String id, JsonObject json) {
        List<Stage> stages = new ArrayList<>();
        for (JsonElement el : GsonHelper.getAsJsonArray(json, "stages")) {
            JsonObject so = el.getAsJsonObject();
            JsonObject obj = GsonHelper.getAsJsonObject(so, "objective");
            Objective objective = new Objective(GsonHelper.getAsString(obj, "type"), obj);
            Map<String, JsonArray> triggers = new HashMap<>();
            if (so.has("triggers")) {
                JsonObject tr = GsonHelper.getAsJsonObject(so, "triggers");
                for (Map.Entry<String, JsonElement> e : tr.entrySet()) triggers.put(e.getKey(), e.getValue().getAsJsonArray());
            }
            stages.add(new Stage(objective,
                    GsonHelper.getAsString(so, "text", ""),
                    so.has("location") ? GsonHelper.getAsString(so, "location") : null,
                    GsonHelper.getAsJsonArray(so, "on_start", new JsonArray()),
                    GsonHelper.getAsJsonArray(so, "on_complete", new JsonArray()),
                    triggers));
        }
        return new QuestDefinition(id,
                GsonHelper.getAsString(json, "title", id),
                GsonHelper.getAsString(json, "act", ""),
                GsonHelper.getAsString(json, "description", ""),
                GsonHelper.getAsInt(json, "order", 100),
                GsonHelper.getAsBoolean(json, "repeatable", false),
                json.has("advancement") ? GsonHelper.getAsString(json, "advancement") : null,
                Collections.unmodifiableList(stages),
                GsonHelper.getAsJsonArray(json, "on_start", new JsonArray()),
                GsonHelper.getAsJsonArray(json, "on_complete", new JsonArray()));
    }

    @Nullable
    public Stage stage(int index) {
        return index >= 0 && index < stages.size() ? stages.get(index) : null;
    }
}
