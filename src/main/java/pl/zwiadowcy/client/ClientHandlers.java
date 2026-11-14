package pl.zwiadowcy.client;

import net.minecraft.client.Minecraft;
import pl.zwiadowcy.client.screen.DialogueScreen;
import pl.zwiadowcy.client.screen.JournalScreen;
import pl.zwiadowcy.network.OpenDialoguePacket;
import pl.zwiadowcy.network.QuestSyncPacket;
import pl.zwiadowcy.network.StealthPacket;

/** Obsługa pakietów po stronie klienta (ładowane tylko na kliencie). */
public final class ClientHandlers {

    public static void openDialogue(OpenDialoguePacket msg) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof DialogueScreen ds) {
            ds.show(msg);
        } else {
            mc.setScreen(new DialogueScreen(msg));
        }
    }

    public static void questSync(QuestSyncPacket msg) {
        ClientQuestData.sync = msg;
        pl.zwiadowcy.skill.Skills.clientSkills = java.util.Set.copyOf(msg.extra().skills());
        if (Minecraft.getInstance().screen instanceof JournalScreen js) js.refresh();
    }

    public static void stealth(StealthPacket msg) {
        ClientQuestData.visibility = msg.visibility();
        ClientQuestData.bezruchTicks = msg.bezruchTicks();
        ClientQuestData.cloak = msg.cloak();
    }

    public static void openEndScreen(pl.zwiadowcy.network.OpenEndScreenPacket msg) {
        Minecraft.getInstance().setScreen(new pl.zwiadowcy.client.screen.EndOfVolumeScreen(msg));
    }

    public static void playCutscene(pl.zwiadowcy.network.PlayCutscenePacket msg) {
        pl.zwiadowcy.client.CutscenePlayer.start(msg);
    }

    public static void openMap() {
        Minecraft.getInstance().setScreen(new pl.zwiadowcy.client.screen.MapScreen());
    }

    public static void openScreen(String screen) {
        if (screen.equals("mapa")) openMap();
    }

    public static void openJournal() {
        Minecraft.getInstance().setScreen(new JournalScreen());
    }

    public static void openStory(String story) {
        Minecraft.getInstance().setScreen(new pl.zwiadowcy.client.screen.StoryScreen(story,
                pl.zwiadowcy.story.StoryTexts.get(story), true));
    }

    public static void openNote(pl.zwiadowcy.story.LoreNotes.Note note) {
        Minecraft.getInstance().setScreen(new pl.zwiadowcy.client.screen.StoryScreen("notatka",
                java.util.List.of(new pl.zwiadowcy.story.StoryTexts.Page(note.title(), note.text(), "zwiadowcy:zapiski")), false));
    }

    private ClientHandlers() {}
}
