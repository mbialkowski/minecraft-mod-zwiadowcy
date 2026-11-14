package pl.zwiadowcy.difficulty;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import pl.zwiadowcy.quest.QuestCapability;
import pl.zwiadowcy.quest.QuestEngine;

import javax.annotation.Nullable;
import java.util.Locale;

/**
 * Poziom trudności fabularnej tomu II, do zmiany w dzienniku w dowolnej chwili.
 * Zwiadowca jest domyślny i wyraźnie trudniejszy od tomu I (kusznicy, skradanie 2.0); tom I nie korzysta z tych mnożników.
 */
public enum StoryDifficulty {
    OPOWIESC("opowiesc", "Opowieść", 0.6, 0.8, 0.7, true, 22, false, true),
    ZWIADOWCA("zwiadowca", "Zwiadowca", 1.0, 1.0, 1.0, true, 16, false, true),
    WETERAN("weteran", "Weteran", 1.4, 1.25, 1.3, false, 12, true, false);

    public final String id;
    public final String label;
    /** Mnożnik obrażeń zadawanych graczowi przez wrogów tomu II. */
    public final double enemyDamage;
    /** Mnożnik zdrowia wrogów tomu II. */
    public final double enemyHealth;
    /** Mnożnik narastania podejrzenia (skradanie 2.0). */
    public final double suspicion;
    /** Czy słabe punkty bossa są podświetlane. */
    public final boolean weakPointGlow;
    /** Długość zapowiedzi ciosu bossa w tikach (bazowo; poszczególne ciosy ją skalują). */
    public final int telegraphTicks;
    /** Ranny towarzysz wstaje dopiero po opatrzeniu (zamiast po walce). */
    public final boolean companionNeedsTending;
    /** Punkt kontrolny po każdej fazie pojedynku (Weteran: tylko przed walką). */
    public final boolean phaseCheckpoints;

    StoryDifficulty(String id, String label, double enemyDamage, double enemyHealth, double suspicion, boolean weakPointGlow,
                    int telegraphTicks, boolean companionNeedsTending, boolean phaseCheckpoints) {
        this.id = id;
        this.label = label;
        this.enemyDamage = enemyDamage;
        this.enemyHealth = enemyHealth;
        this.suspicion = suspicion;
        this.weakPointGlow = weakPointGlow;
        this.telegraphTicks = telegraphTicks;
        this.companionNeedsTending = companionNeedsTending;
        this.phaseCheckpoints = phaseCheckpoints;
    }

    public static StoryDifficulty byId(@Nullable String id) {
        if (id != null) {
            for (StoryDifficulty d : values()) if (d.id.equals(id.toLowerCase(Locale.ROOT))) return d;
        }
        return ZWIADOWCA;
    }

    public static StoryDifficulty of(@Nullable Player p) {
        return p == null ? ZWIADOWCA : byId(QuestCapability.get(p).difficulty);
    }

    public StoryDifficulty next() {
        return values()[(ordinal() + 1) % values().length];
    }

    /** Zmiana w dzienniku; argument "nastepny" przełącza po kolei. */
    public static void set(ServerPlayer p, String arg) {
        StoryDifficulty cur = of(p);
        StoryDifficulty next = arg.equals("nastepny") ? cur.next() : byId(arg);
        QuestCapability.get(p).difficulty = next.id;
        if (next != WETERAN) QuestCapability.get(p).setFlag("weteran_caly_pojedynek", false);
        p.displayClientMessage(Component.literal("Poziom trudności: " + next.label).withStyle(ChatFormatting.GOLD), true);
        QuestEngine.sync(p);
    }
}
