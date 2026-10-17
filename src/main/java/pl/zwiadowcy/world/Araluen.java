package pl.zwiadowcy.world;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import pl.zwiadowcy.Zwiadowcy;
import pl.zwiadowcy.quest.QuestCapability;
import pl.zwiadowcy.quest.QuestEngine;
import pl.zwiadowcy.quest.QuestProgress;
import pl.zwiadowcy.registry.ModItems;

import javax.annotation.Nullable;

/** Wymiar Araluen i podróże między nim a zwykłym światem. */
public final class Araluen {
    public static final ResourceKey<Level> KEY = ResourceKey.create(Registries.DIMENSION, Zwiadowcy.id("araluen"));
    public static final String FIRST_QUEST = "dzien_wyboru";

    public static boolean isAraluen(Level level) {
        return level.dimension() == KEY;
    }

    @Nullable
    public static ServerLevel level(MinecraftServer server) {
        return server.getLevel(KEY);
    }

    /** Użycie Kroniki Araluenu: przeniesienie do Redmont albo z powrotem do zwykłego świata. */
    public static void travel(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        QuestProgress progress = QuestCapability.get(player);
        if (isAraluen(player.level())) {
            ServerLevel overworld = server.overworld();
            BlockPos target = progress.overworldReturnPos;
            if (target == null) {
                BlockPos spawn = overworld.getSharedSpawnPos();
                target = new BlockPos(spawn.getX(), StructurePlacer.height(overworld,
                        net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawn.getX(), spawn.getZ()), spawn.getZ());
            }
            player.teleportTo(overworld, target.getX() + 0.5, target.getY(), target.getZ() + 0.5, player.getYRot(), player.getXRot());
            player.displayClientMessage(Component.literal("Strony kroniki szeleszczą… wracasz do znanego świata.").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true);
            return;
        }

        ServerLevel araluen = level(server);
        if (araluen == null) {
            player.sendSystemMessage(Component.literal("Wymiar Araluen nie został wczytany - sprawdź pliki moda.").withStyle(ChatFormatting.RED));
            return;
        }
        if (player.level().dimension() == Level.OVERWORLD) {
            progress.overworldReturnPos = player.blockPosition();
        }
        boolean first = !progress.visitedAraluen;
        if (first) {
            player.displayClientMessage(Component.literal("Kronika otwiera się sama… (pierwsze wejście może chwilę potrwać)").withStyle(ChatFormatting.GOLD), true);
            for (StoryLocation loc : StoryLocation.values()) {
                if (loc.placeOnArrival) Locations.ensure(server, loc.id);
            }
        }
        BlockPos arrival = Locations.resolve(server, "redmont:przybycie");
        if (arrival == null) arrival = araluen.getSharedSpawnPos();
        player.teleportTo(araluen, arrival.getX() + 0.5, arrival.getY(), arrival.getZ() + 0.5, 180f, 0f);

        if (first) {
            progress.visitedAraluen = true;
            player.setRespawnPosition(KEY, arrival, 180f, true, false);
            if (!player.getInventory().contains(new ItemStack(ModItems.DZIENNIK_ZWIADOWCY.get()))) {
                player.getInventory().add(new ItemStack(ModItems.DZIENNIK_ZWIADOWCY.get()));
            }
            QuestEngine.awardAdvancement(player, "misje/araluen");
            progress.addNote(pl.zwiadowcy.story.StoryTexts.PROLOG_NOTE_TITLE, pl.zwiadowcy.story.StoryTexts.PROLOG_NOTE);
            // prolog na ekranie-kronice, gdy zniknie ekran ładowania wymiaru; potem baron podchodzi do gracza
            pl.zwiadowcy.story.StoryService.queue(player, pl.zwiadowcy.story.StoryTexts.PROLOG, 50);
        }
        if (!progress.isActive(FIRST_QUEST) && !progress.isCompleted(FIRST_QUEST)) {
            QuestEngine.start(player, FIRST_QUEST);
        }
        QuestEngine.sync(player);
    }

    private Araluen() {}
}
