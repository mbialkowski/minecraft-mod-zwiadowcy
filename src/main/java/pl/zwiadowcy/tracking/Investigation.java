package pl.zwiadowcy.tracking;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import pl.zwiadowcy.quest.*;

/**
 * Wniosek ze śledztwa (cel "deduce"): gracz wybiera w dzienniku jedną z odpowiedzi. Pomyłka daje podpowiedź
 * i kolejną próbę; trafienie za pierwszym razem uruchamia akcje "on_first_try" (np. zaufanie Gilana).
 */
public final class Investigation {

    /** Argument: "misja:indeks". */
    public static void answer(ServerPlayer p, String arg) {
        int sep = arg.lastIndexOf(':');
        if (sep < 0) return;
        String quest = arg.substring(0, sep);
        int index;
        try {
            index = Integer.parseInt(arg.substring(sep + 1));
        } catch (NumberFormatException e) {
            return;
        }
        QuestProgress prog = QuestCapability.get(p);
        QuestProgress.State st = prog.get(quest);
        QuestDefinition def = QuestManager.INSTANCE.get(quest);
        if (st == null || def == null || st.status != QuestProgress.Status.ACTIVE) return;
        QuestDefinition.Stage stage = def.stage(st.stage);
        if (stage == null || !stage.objective().type().equals("deduce")) return;
        var o = stage.objective();
        int correct = GsonHelper.getAsInt(o.params(), "correct", 0);
        String wrongFlag = "wniosek_pomylka:" + quest;
        if (index == correct) {
            p.sendSystemMessage(Component.literal("✔ Wniosek: " + o.list("options").get(index)).withStyle(ChatFormatting.GREEN));
            if (!prog.hasFlag(wrongFlag)) {
                QuestActions.run(p, GsonHelper.getAsJsonArray(o.params(), "on_first_try", new com.google.gson.JsonArray()),
                        new QuestActions.Ctx(quest, null));
            }
            QuestEngine.advance(p, quest);
        } else {
            prog.setFlag(wrongFlag, true);
            String hint = o.str("hint");
            p.sendSystemMessage(Component.literal(hint.isEmpty() ? "To się nie zgadza ze śladami. Spróbuj jeszcze raz." : hint)
                    .withStyle(ChatFormatting.GOLD));
            QuestEngine.sync(p);
        }
    }

    private Investigation() {}
}
