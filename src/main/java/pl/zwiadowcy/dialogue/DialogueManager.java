package pl.zwiadowcy.dialogue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import pl.zwiadowcy.Zwiadowcy;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/** Wczytuje dialogi z data/&lt;namespace&gt;/dialogues/*.json. */
public class DialogueManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().create();
    public static final DialogueManager INSTANCE = new DialogueManager();

    private Map<String, DialogueDefinition> dialogues = Map.of();

    private DialogueManager() {
        super(GSON, "dialogues");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager manager, ProfilerFiller profiler) {
        Map<String, DialogueDefinition> loaded = new HashMap<>();
        files.forEach((rl, json) -> {
            try {
                DialogueDefinition def = DialogueDefinition.parse(rl.getPath(), json.getAsJsonObject());
                loaded.put(def.npc(), def);
            } catch (Exception e) {
                Zwiadowcy.LOGGER.error("Błąd w pliku dialogu {}: {}", rl, e.getMessage());
            }
        });
        dialogues = loaded;
        Zwiadowcy.LOGGER.info("Wczytano dialogi dla {} postaci", dialogues.size());
    }

    @Nullable
    public DialogueDefinition get(String npc) {
        return dialogues.get(npc);
    }
}
