package pl.zwiadowcy.client;

import pl.zwiadowcy.network.QuestSyncPacket;

import javax.annotation.Nullable;

/** Ostatni stan dziennika i kamuflażu otrzymany z serwera. */
public final class ClientQuestData {
    @Nullable public static QuestSyncPacket sync;
    public static int visibility = 100;
    public static int bezruchTicks;
    public static boolean cloak;

    @Nullable
    public static QuestSyncPacket.QuestView tracked() {
        if (sync == null || sync.tracked().isEmpty()) return null;
        for (QuestSyncPacket.QuestView q : sync.quests()) {
            if (q.id().equals(sync.tracked()) && !q.completed()) return q;
        }
        return null;
    }

    private ClientQuestData() {}
}
