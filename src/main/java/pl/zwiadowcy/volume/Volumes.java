package pl.zwiadowcy.volume;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import pl.zwiadowcy.item.QuiverItem;
import pl.zwiadowcy.network.ModNetwork;
import pl.zwiadowcy.network.OpenEndScreenPacket;
import pl.zwiadowcy.quest.QuestCapability;
import pl.zwiadowcy.quest.QuestEngine;
import pl.zwiadowcy.quest.QuestProgress;
import pl.zwiadowcy.registry.ModItems;
import pl.zwiadowcy.story.StoryService;
import pl.zwiadowcy.story.StoryTexts;
import pl.zwiadowcy.world.Araluen;

/**
 * Tomy opowieści w jednym modzie: koniec tomu I i przejście do tomu II („Płonący most”), szybki start od tomu II
 * w nowym świecie oraz koniec tomu II z zapowiedzią części trzeciej.
 */
public final class Volumes {
    public static final String TOM1_LAST = "przed_obliczem_krola";
    public static final String TOM2_FIRST = "wezwanie";
    public static final String TOM2_LAST = "wilcze_okrety";

    public static final String FLAG_END1_SHOWN = "koniec_tomu_1_pokazany";
    public static final String FLAG_END2_SHOWN = "koniec_tomu_2_pokazany";
    /** Nowy świat zaczęty od razu od tomu II (tom I pominięty, zestaw ucznia zwiadowcy w plecaku). */
    public static final String FLAG_QUICK = "tom2_szybki_start";
    public static final String FLAG_TOM1_SKIPPED = "tom1_pominiety";
    /** Tom II w toku - zlecenia Halta z tomu I są wtedy niedostępne (Halt jest przy armii). */
    public static final String FLAG_TOM2_RUNNING = "tom2_w_toku";
    /** Wolna gra po tomie II (poza kanonem). */
    public static final String FLAG_FREE_PLAY = "wolna_gra";

    public static boolean tom1Done(QuestProgress prog) {
        return prog.isCompleted(TOM1_LAST) || prog.hasFlag(FLAG_TOM1_SKIPPED);
    }

    public static boolean tom2Started(QuestProgress prog) {
        return prog.get(TOM2_FIRST) != null;
    }

    public static boolean tom2Done(QuestProgress prog) {
        return prog.isCompleted(TOM2_LAST);
    }

    public static boolean knight(QuestProgress prog) {
        return prog.hasFlag("wybor_rycerz");
    }

    // ------------------------------------------------------------------ ekrany końca tomu

    /** Po zamknięciu epilogu: ekran końca tomu (pierwszy raz automatycznie, potem z dziennika). */
    public static void onStoryFinished(ServerPlayer p, String story) {
        QuestProgress prog = QuestCapability.get(p);
        if ((story.equals(StoryTexts.EPILOG_ZWIADOWCA) || story.equals(StoryTexts.EPILOG_RYCERZ)) && prog.isCompleted(TOM1_LAST)
                && !prog.hasFlag(FLAG_END1_SHOWN)) {
            prog.setFlag(FLAG_END1_SHOWN, true);
            showEndScreen(p, 1);
        } else if (story.equals(StoryTexts.EPILOG_TOM2) && tom2Done(prog) && !prog.hasFlag(FLAG_END2_SHOWN)) {
            prog.setFlag(FLAG_END2_SHOWN, true);
            showEndScreen(p, 2);
        }
    }

    public static void showEndScreen(ServerPlayer p, int volume) {
        QuestProgress prog = QuestCapability.get(p);
        if (volume == 1 && !prog.isCompleted(TOM1_LAST)) return;
        if (volume == 2 && !tom2Done(prog)) return;
        ModNetwork.sendTo(p, new OpenEndScreenPacket(volume, knight(prog), tom2Started(prog), prog.hasFlag(FLAG_FREE_PLAY)));
    }

    // ------------------------------------------------------------------ tom II

    /** Przycisk „Kontynuuj do części drugiej” (ekran końca tomu, dziennik albo komunikat na czacie). */
    public static void continueToVolume2(ServerPlayer p) {
        QuestProgress prog = QuestCapability.get(p);
        if (!tom1Done(prog)) {
            p.sendSystemMessage(Component.literal("Tom II zaczyna się po zakończeniu tomu I.").withStyle(ChatFormatting.GRAY));
            return;
        }
        if (tom2Started(prog)) {
            p.sendSystemMessage(Component.literal("Tom II: Płonący most już trwa - zajrzyj do dziennika (J).").withStyle(ChatFormatting.GRAY));
            return;
        }
        beginVolume2(p, prog);
    }

    private static void beginVolume2(ServerPlayer p, QuestProgress prog) {
        prog.setFlag(FLAG_TOM2_RUNNING, true);
        QuestEngine.awardAdvancement(p, "tom2/root");
        // zwiadowca zostawał przy Halcie przez całą zimę - Gilan zna go lepiej
        if (!knight(prog) && !prog.trustMap().containsKey("gilan")) prog.setTrust("gilan", prog.trust("gilan") + 5);
        p.sendSystemMessage(Component.literal("✦ Tom II: Płonący most").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        StoryService.queue(p, prog.hasFlag(FLAG_QUICK) ? pl.zwiadowcy.story.StoryTextsTom2.PROLOG_SZYBKI
                : knight(prog) ? pl.zwiadowcy.story.StoryTextsTom2.PROLOG_RYCERZ : StoryTexts.PROLOG_TOM2, 10);
        QuestEngine.start(p, TOM2_FIRST);
        if (!Araluen.isAraluen(p.level())) {
            p.sendSystemMessage(Component.literal("Użyj Kroniki Araluenu, aby wrócić do królestwa - Halt czeka w swojej chacie.")
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    /**
     * Szybki start (ekran Kroniki w nowym świecie): tom I zostaje pominięty, Will dostaje zestaw ucznia zwiadowcy,
     * a pierwsza podróż do Araluenu zaczyna od razu tom II.
     */
    public static void quickStart(ServerPlayer p) {
        QuestProgress prog = QuestCapability.get(p);
        if (prog.visitedAraluen || !prog.states().isEmpty()) {
            p.sendSystemMessage(Component.literal("Szybki start działa tylko na początku nowego świata.").withStyle(ChatFormatting.GRAY));
            return;
        }
        prog.setFlag(FLAG_QUICK, true);
        prog.setFlag(FLAG_TOM1_SKIPPED, true);
        prog.setFlag("kampania_ukonczona", true);
        giveStarterKit(p);
        prog.addNote(StoryTexts.TOM1_SUMMARY_TITLE, StoryTexts.TOM1_SUMMARY);
        p.sendSystemMessage(Component.literal("✦ Zaczynasz od tomu II. W plecaku masz zestaw ucznia zwiadowcy. Użyj Kroniki Araluenu.")
                .withStyle(ChatFormatting.GOLD));
        QuestEngine.sync(p);
    }

    /**
     * Kronika zamknięta bez szybkiego startu (wybór „Zacznij od tomu I” albo zwykłe zamknięcie w nowym świecie):
     * krok po kroku, jak zacząć tom I, a Kronika trafia do ręki.
     */
    public static void guideToVolumeOne(ServerPlayer p) {
        QuestProgress prog = QuestCapability.get(p);
        if (prog.hasFlag(FLAG_QUICK) || prog.visitedAraluen || !prog.states().isEmpty()) return;
        var inv = p.getInventory();
        ItemStack chronicle = new ItemStack(ModItems.KRONIKA_ARALUENU.get());
        int slot = inv.findSlotMatchingItem(chronicle);
        if (slot < 0) {
            inv.add(chronicle);
            slot = inv.findSlotMatchingItem(chronicle);
        }
        if (slot >= 0 && slot != inv.selected) {
            if (slot < 9) {
                inv.selected = slot;
            } else {
                ItemStack held = inv.getItem(inv.selected);
                inv.setItem(inv.selected, inv.getItem(slot));
                inv.setItem(slot, held);
            }
            p.connection.send(new net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket(inv.selected));
        }
        QuestEngine.showTitle(p, Component.literal("Tom I: Ruiny Gorlanu").withStyle(ChatFormatting.GOLD),
                Component.literal("Użyj Kroniki Araluenu (PPM), by przenieść się do Redmont").withStyle(ChatFormatting.GRAY));
        p.sendSystemMessage(Component.literal("✦ Tom I: Ruiny Gorlanu - tu zaczyna się twoja przygoda.").withStyle(ChatFormatting.GOLD));
        p.sendSystemMessage(Component.literal("  1. Kronika Araluenu jest w twojej dłoni. Użyj jej (PPM): przeniesiesz się na dziedziniec zamku Redmont.").withStyle(ChatFormatting.GRAY));
        p.sendSystemMessage(Component.literal("  2. Przeczytaj prolog - potem baron Arald sam do ciebie podejdzie.").withStyle(ChatFormatting.GRAY));
        p.sendSystemMessage(Component.literal("  3. Dziennik zwiadowcy (J) pokazuje misje, a strzałka w rogu ekranu prowadzi do celu.").withStyle(ChatFormatting.GRAY));
        p.sendSystemMessage(Component.literal("  Tom II „Płonący most” odblokuje się po epilogu tomu I.").withStyle(ChatFormatting.DARK_GRAY));
    }

    /** Zestaw ucznia zwiadowcy po tomie I (dla szybkiego startu). */
    public static void giveStarterKit(ServerPlayer p) {
        ItemStack quiver = new ItemStack(ModItems.KOLCZAN.get());
        QuiverItem.fill(quiver, new ItemStack(net.minecraft.world.item.Items.ARROW, 48),
                new ItemStack(ModItems.STRZALA_PLONACA.get(), 8), new ItemStack(ModItems.STRZALA_GWIZDZACA.get(), 4),
                new ItemStack(ModItems.STRZALA_SZEROKI_GROT.get(), 4));
        give(p, new ItemStack(ModItems.LUK_ZWIADOWCY.get()));
        give(p, quiver);
        give(p, new ItemStack(ModItems.NOZ_SAKSONSKI.get()));
        give(p, new ItemStack(ModItems.NOZ_DO_RZUCANIA.get(), 6));
        give(p, new ItemStack(ModItems.PLASZCZ_ZWIADOWCY.get()));
        give(p, new ItemStack(ModItems.ZESTAW_TROPICIELA.get()));
        give(p, new ItemStack(ModItems.FLASZA_OLEJU.get(), 3));
        give(p, new ItemStack(ModItems.GWIZDEK.get()));
        give(p, new ItemStack(ModItems.RACJE_PODROZNE.get(), 12));
        give(p, new ItemStack(ModItems.KAWA_ZWIADOWCY.get(), 4));
        give(p, new ItemStack(ModItems.BRAZOWY_LISC_DEBU.get()));
    }

    private static void give(ServerPlayer p, ItemStack stack) {
        if (!p.getInventory().add(stack)) p.drop(stack, false);
    }

    /** Pierwsze wejście do Araluenu po szybkim starcie: koń oswojony, tom II od razu się zaczyna. */
    public static void onQuickStartArrival(ServerPlayer p) {
        QuestProgress prog = QuestCapability.get(p);
        if (!prog.hasFlag(FLAG_QUICK) || tom2Started(prog)) return;
        MinecraftServer server = p.getServer();
        if (server == null) return;
        pl.zwiadowcy.world.Locations.ensure(server, "chata_halta");
        BlockPos tugPos = pl.zwiadowcy.world.Locations.resolve(server, "chata_halta:chata");
        ServerLevel level = Araluen.level(server);
        if (level != null && tugPos != null) pl.zwiadowcy.entity.TugEntity.summonBonded(level, p, tugPos);
        beginVolume2(p, prog);
    }

    // ------------------------------------------------------------------ logowanie

    /** Przy wejściu do gry: ekran końca tomu I (raz, dla zapisów sprzed 2.0) i przypomnienie o tomie II. */
    public static void onLogin(ServerPlayer p) {
        QuestProgress prog = QuestCapability.get(p);
        if (prog.isCompleted(TOM1_LAST) && !prog.hasFlag(FLAG_END1_SHOWN)) {
            prog.setFlag(FLAG_END1_SHOWN, true);
            StoryService.queueEndScreen(p, 1, 80);
            return;
        }
        if (tom1Done(prog) && !tom2Started(prog)) {
            p.sendSystemMessage(Component.literal("📜 Tom II: Płonący most czeka. ").withStyle(ChatFormatting.GOLD)
                    .append(Component.literal("[Kontynuuj]").withStyle(s -> s.withColor(ChatFormatting.YELLOW).withUnderlined(true)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/zwiadowcy_tom kontynuuj"))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    Component.literal("Zacznij drugą część opowieści"))))));
        }
    }

    // ------------------------------------------------------------------ niewola i wolna gra

    /** Niewola (tom II, finał): ekwipunek trafia do wirtualnej skrzyni, Will zostaje w zgrzebnym ubraniu. */
    public static void storeInventory(ServerPlayer p) {
        QuestProgress prog = QuestCapability.get(p);
        if (prog.storedInventory != null) return;
        Inventory inv = p.getInventory();
        prog.storedInventory = inv.save(new net.minecraft.nbt.ListTag());
        inv.clearContent();
        inv.add(new ItemStack(net.minecraft.world.item.Items.LEATHER_CHESTPLATE));
    }

    /** Zwrot ekwipunku po niewoli (wolna gra po tomie II). */
    public static void restoreInventory(ServerPlayer p) {
        QuestProgress prog = QuestCapability.get(p);
        if (prog.storedInventory == null) return;
        Inventory inv = p.getInventory();
        java.util.List<ItemStack> now = new java.util.ArrayList<>();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (!s.isEmpty() && !s.is(net.minecraft.world.item.Items.LEATHER_CHESTPLATE)) now.add(s.copy());
        }
        inv.clearContent();
        inv.load(prog.storedInventory);
        prog.storedInventory = null;
        for (ItemStack s : now) give(p, s);
    }

    /** Przycisk „Wróć do Araluenu” na ekranie końca tomu II: zwrot ekwipunku i powrót do chaty Halta. */
    public static void startFreePlay(ServerPlayer p) {
        QuestProgress prog = QuestCapability.get(p);
        if (!tom2Done(prog)) return;
        prog.setFlag(FLAG_FREE_PLAY, true);
        prog.setFlag(FLAG_TOM2_RUNNING, false);
        restoreInventory(p);
        MinecraftServer server = p.getServer();
        ServerLevel level = server != null ? Araluen.level(server) : null;
        BlockPos home = server != null ? pl.zwiadowcy.world.Locations.resolve(server, "chata_halta:chata") : null;
        if (level != null && home != null) {
            p.teleportTo(level, home.getX() + 0.5, home.getY(), home.getZ() + 0.5, p.getYRot(), 0f);
            p.setRespawnPosition(Araluen.KEY, home, p.getYRot(), true, false);
        }
        StoryService.queue(p, StoryTexts.WOLNA_GRA, 20);
        QuestEngine.sync(p);
    }

    private Volumes() {}
}
