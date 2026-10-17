package pl.zwiadowcy.dialogue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Dialogi jednej postaci (data/zwiadowcy/dialogues/&lt;npc&gt;.json).
 * "entries" - lista {when, start} sprawdzana od góry; pierwsza pasująca wybiera węzeł startowy.
 */
public record DialogueDefinition(String npc, List<Entry> entries, Map<String, Node> nodes) {

    public record Entry(@Nullable JsonElement when, String start) {}

    public record Option(String text, @Nullable JsonElement when, JsonArray actions, @Nullable String next) {}

    public record Node(@Nullable String speaker, String text, JsonArray actions, List<Option> options) {}

    public static DialogueDefinition parse(String id, JsonObject json) {
        List<Entry> entries = new ArrayList<>();
        for (JsonElement el : GsonHelper.getAsJsonArray(json, "entries")) {
            JsonObject e = el.getAsJsonObject();
            entries.add(new Entry(e.get("when"), GsonHelper.getAsString(e, "start")));
        }
        Map<String, Node> nodes = new HashMap<>();
        JsonObject nodesJson = GsonHelper.getAsJsonObject(json, "nodes");
        for (Map.Entry<String, JsonElement> e : nodesJson.entrySet()) {
            JsonObject n = e.getValue().getAsJsonObject();
            List<Option> options = new ArrayList<>();
            for (JsonElement oe : GsonHelper.getAsJsonArray(n, "options", new JsonArray())) {
                JsonObject o = oe.getAsJsonObject();
                options.add(new Option(
                        GsonHelper.getAsString(o, "text"),
                        o.get("when"),
                        GsonHelper.getAsJsonArray(o, "actions", new JsonArray()),
                        o.has("next") && !o.get("next").isJsonNull() ? GsonHelper.getAsString(o, "next") : null));
            }
            String text;
            JsonElement t = n.get("text");
            if (t != null && t.isJsonArray()) {
                StringBuilder sb = new StringBuilder();
                for (JsonElement line : t.getAsJsonArray()) {
                    if (!sb.isEmpty()) sb.append('\n');
                    sb.append(line.getAsString());
                }
                text = sb.toString();
            } else {
                text = GsonHelper.getAsString(n, "text", "");
            }
            nodes.put(e.getKey(), new Node(
                    n.has("speaker") ? GsonHelper.getAsString(n, "speaker") : null,
                    text,
                    GsonHelper.getAsJsonArray(n, "actions", new JsonArray()),
                    options));
        }
        return new DialogueDefinition(GsonHelper.getAsString(json, "npc", id), entries, nodes);
    }
}
