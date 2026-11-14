package pl.zwiadowcy.tracking;

import pl.zwiadowcy.quest.QuestProgress;

import java.util.ArrayList;
import java.util.List;

/** Znalezione wskazówki śledztw (Oczy tropiciela) - zapisywane jako flagi "wskazowka:lokacja:kotwica". */
public final class Clues {
    public static final String PREFIX = "wskazowka:";

    public static List<String> foundList(QuestProgress prog) {
        List<String> out = new ArrayList<>();
        for (String f : prog.flags()) if (f.startsWith(PREFIX)) out.add(f.substring(PREFIX.length()));
        out.sort(String::compareTo);
        return out;
    }

    /**
     * Pytanie z bieżącego etapu typu "deduce" (wniosek ze śledztwa) dla dziennika: [misja, pytanie, odpowiedzi…],
     * albo pusta lista.
     */
    public static List<String> activeDeduction(QuestProgress prog) {
        for (var e : prog.states().entrySet()) {
            if (e.getValue().status != QuestProgress.Status.ACTIVE) continue;
            var def = pl.zwiadowcy.quest.QuestManager.INSTANCE.get(e.getKey());
            var stage = def != null ? def.stage(e.getValue().stage) : null;
            if (stage == null || !stage.objective().type().equals("deduce")) continue;
            List<String> out = new ArrayList<>();
            out.add(def.id());
            out.add(stage.objective().str("question"));
            out.addAll(stage.objective().list("options"));
            return out;
        }
        return List.of();
    }

    public static boolean found(QuestProgress prog, String clue) {
        return prog.hasFlag(PREFIX + clue);
    }

    private Clues() {}
}
