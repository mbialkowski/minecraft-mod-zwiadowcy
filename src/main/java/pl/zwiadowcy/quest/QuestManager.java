package pl.zwiadowcy.quest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import pl.zwiadowcy.Zwiadowcy;

import javax.annotation.Nullable;
import java.util.*;

/** Wczytuje misje z data/&lt;namespace&gt;/quests/*.json. */
public class QuestManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().create();
    public static final QuestManager INSTANCE = new QuestManager();

    private Map<String, QuestDefinition> quests = Map.of();

    private QuestManager() {
        super(GSON, "quests");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager manager, ProfilerFiller profiler) {
        Map<String, QuestDefinition> loaded = new LinkedHashMap<>();
        files.forEach((rl, json) -> {
            try {
                loaded.put(rl.getPath(), QuestDefinition.parse(rl.getPath(), json.getAsJsonObject()));
            } catch (Exception e) {
                Zwiadowcy.LOGGER.error("Błąd w pliku misji {}: {}", rl, e.getMessage());
            }
        });
        quests = Collections.unmodifiableMap(loaded);
        Zwiadowcy.LOGGER.info("Wczytano {} misji", quests.size());
    }

    @Nullable
    public QuestDefinition get(String id) {
        return quests.get(id);
    }

    public Collection<QuestDefinition> all() {
        return quests.values();
    }
}
