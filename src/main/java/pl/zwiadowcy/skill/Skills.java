package pl.zwiadowcy.skill;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import pl.zwiadowcy.quest.QuestCapability;
import pl.zwiadowcy.quest.QuestEngine;

import javax.annotation.Nullable;
import java.util.List;

/** Umiejętności-odznaki tomu II: zdobywane w misjach, bez punktów i grindu. Działają też w wolnej grze. */
public final class Skills {
    public static final String LEKCJA_GILANA = "lekcja_gilana";
    public static final String OKO_TROPICIELA = "oko_tropiciela";
    public static final String CICHY_KROK = "cichy_krok";
    public static final String PEWNA_REKA = "pewna_reka";
    public static final String ZIMNA_KREW = "zimna_krew";
    public static final String ROZPALACZ = "rozpalacz";
    public static final String GORNICZA_ZRECZNOSC = "gornicza_zrecznosc";
    public static final String JEZDZIEC = "jezdziec";
    public static final String CZUJNE_UCHO = "czujne_ucho";
    public static final String PRZYJACIEL_KONI = "przyjaciel_koni";
    /** Bonus wyboru rycerskiego z tomu I (nie jest odznaką z misji). */
    public static final String SZKOLNY_FECHTUNEK = "szkolny_fechtunek";

    public record Skill(String id, String name, String effect, String source) {}

    public static final List<Skill> ALL = List.of(
            new Skill(LEKCJA_GILANA, "Lekcja Gilana", "Miecz zwiadowcy zadaje o 2 obrażenia więcej.", "Misja „Miecz zwiadowcy”"),
            new Skill(OKO_TROPICIELA, "Oko tropiciela", "Oczy tropiciela sięgają 24 bloki, ślady widać dłużej.", "Misja „Ciche wzgórza”"),
            new Skill(CICHY_KROK, "Cichy krok", "Kroki po żwirze, kamieniu i deskach nie budzą podejrzeń.", "Misja „Dwie drogi”"),
            new Skill(PEWNA_REKA, "Pewna ręka", "Łuk zwiadowcy napina się o 15% szybciej.", "Misja „Czarny jeździec”"),
            new Skill(ZIMNA_KREW, "Zimna krew", "Podejrzenie wrogów rośnie o 25% wolniej.", "Misja „Łańcuchy”"),
            new Skill(ROZPALACZ, "Rozpalacz", "Olej rozpryskuje się szerzej, płonące strzały palą dłużej.", "Misja „Płonący most”"),
            new Skill(GORNICZA_ZRECZNOSC, "Górnicza zręczność", "Kotwiczka odnawia się w 5 s, po drabinach wspinasz się szybciej.", "Misja „Syn górnika”"),
            new Skill(JEZDZIEC, "Jeździec", "Kara do celności strzałów z siodła o połowę mniejsza.", "Misja „Strzał Gilana”"),
            new Skill(CZUJNE_UCHO, "Czujne ucho", "Po 2 s bezruchu w kucaniu widzisz wrogów w promieniu 12 bloków.", "Misja „Obcy na trakcie”"),
            new Skill(PRZYJACIEL_KONI, "Przyjaciel koni", "Gwizdek sięga 150 bloków, zryw Wyrwija co 20 s.", "Misja „Czarny rumak”"));

    @Nullable
    public static Skill byId(String id) {
        for (Skill s : ALL) if (s.id.equals(id)) return s;
        return null;
    }

    /** Umiejętności gracza po stronie klienta (z synchronizacji dziennika) - np. szybsze napinanie łuku w animacji. */
    public static volatile java.util.Set<String> clientSkills = java.util.Set.of();

    public static boolean has(@Nullable Player p, String id) {
        if (p == null) return false;
        if (p.level().isClientSide) return clientSkills.contains(id);
        return QuestCapability.get(p).hasSkill(id);
    }

    public static void grant(ServerPlayer p, String id) {
        Skill s = byId(id);
        if (!QuestCapability.get(p).addSkill(id)) return;
        if (s != null) {
            QuestEngine.showTitle(p, Component.literal("Nowa umiejętność").withStyle(ChatFormatting.AQUA),
                    Component.literal(s.name).withStyle(ChatFormatting.GOLD));
            p.sendSystemMessage(Component.literal("★ Umiejętność: ").withStyle(ChatFormatting.AQUA)
                    .append(Component.literal(s.name).withStyle(ChatFormatting.GOLD))
                    .append(Component.literal(" - " + s.effect).withStyle(ChatFormatting.GRAY)));
            p.level().playSound(null, p.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.6f, 1.4f);
        }
        QuestEngine.sync(p);
    }

    private Skills() {}
}
