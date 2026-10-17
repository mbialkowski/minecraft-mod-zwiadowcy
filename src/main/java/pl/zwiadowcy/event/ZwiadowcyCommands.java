package pl.zwiadowcy.event;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import pl.zwiadowcy.quest.*;
import pl.zwiadowcy.world.Araluen;
import pl.zwiadowcy.world.LocationData;
import pl.zwiadowcy.world.Locations;
import pl.zwiadowcy.world.StoryLocation;

import java.util.Arrays;

/**
 * Komendy pomocnicze (poziom uprawnień 2):
 * /zwiadowcy misja start|dalej|ukoncz &lt;id&gt;, /zwiadowcy misja reset, /zwiadowcy tp &lt;lokacja[:kotwica]&gt;,
 * /zwiadowcy podroz, /zwiadowcy flaga &lt;nazwa&gt;, /zwiadowcy kotwice &lt;lokacja&gt;.
 */
public final class ZwiadowcyCommands {

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("zwiadowcy").requires(s -> s.hasPermission(2))
                .then(Commands.literal("misja")
                        .then(Commands.literal("start").then(questArg().executes(c -> {
                            QuestEngine.start(c.getSource().getPlayerOrException(), StringArgumentType.getString(c, "id"));
                            return 1;
                        })))
                        .then(Commands.literal("dalej").then(questArg().executes(c -> {
                            QuestEngine.advance(c.getSource().getPlayerOrException(), StringArgumentType.getString(c, "id"));
                            return 1;
                        })))
                        .then(Commands.literal("ukoncz").then(questArg().executes(c -> {
                            ServerPlayer p = c.getSource().getPlayerOrException();
                            String id = StringArgumentType.getString(c, "id");
                            QuestEngine.complete(p, id);
                            return 1;
                        })))
                        .then(Commands.literal("reset").executes(c -> {
                            ServerPlayer p = c.getSource().getPlayerOrException();
                            QuestCapability.get(p).reset();
                            QuestEngine.sync(p);
                            c.getSource().sendSuccess(() -> Component.literal("Postęp fabularny wyczyszczony."), false);
                            return 1;
                        })))
                .then(Commands.literal("tp").then(Commands.argument("cel", StringArgumentType.string())
                        .suggests((c, b) -> SharedSuggestionProvider.suggest(Arrays.stream(StoryLocation.values()).map(l -> l.id), b))
                        .executes(ZwiadowcyCommands::teleport)))
                .then(Commands.literal("kotwice").then(Commands.argument("lokacja", StringArgumentType.word())
                        .suggests((c, b) -> SharedSuggestionProvider.suggest(Arrays.stream(StoryLocation.values()).map(l -> l.id), b))
                        .executes(c -> {
                            String id = StringArgumentType.getString(c, "lokacja");
                            LocationData.Placed placed = Locations.ensure(c.getSource().getServer(), id);
                            if (placed == null) return 0;
                            c.getSource().sendSuccess(() -> Component.literal(id + " @ " + placed.center.toShortString()
                                    + " - kotwice: " + String.join(", ", placed.anchors.keySet())), false);
                            return 1;
                        })))
                .then(Commands.literal("przestaw").then(Commands.argument("lokacja", StringArgumentType.word())
                        .suggests((c, b) -> SharedSuggestionProvider.suggest(Arrays.stream(StoryLocation.values())
                                .filter(l -> l != StoryLocation.REDMONT).map(l -> l.id), b))
                        .executes(c -> {
                            String id = StringArgumentType.getString(c, "lokacja");
                            c.getSource().sendSuccess(() -> Component.literal("Szukam nowego miejsca dla " + id + "…"), false);
                            LocationData.Placed placed = Locations.relocate(c.getSource().getServer(), id);
                            if (placed == null) {
                                c.getSource().sendFailure(Component.literal("Nie mogę przestawić lokacji " + id + " (Redmontu nie przenoszę)."));
                                return 0;
                            }
                            c.getSource().sendSuccess(() -> Component.literal(id + " stoi teraz @ " + placed.center.toShortString()
                                    + ". Postacie i koń przejdą tam w ciągu kilkunastu sekund. Dojazd: /zwiadowcy tp " + id), true);
                            return 1;
                        })))
                .then(Commands.literal("podroz").executes(c -> {
                    Araluen.travel(c.getSource().getPlayerOrException());
                    return 1;
                }))
                .then(Commands.literal("flaga").then(Commands.argument("nazwa", StringArgumentType.word()).executes(c -> {
                    ServerPlayer p = c.getSource().getPlayerOrException();
                    String f = StringArgumentType.getString(c, "nazwa");
                    QuestCapability.get(p).setFlag(f, !QuestCapability.get(p).hasFlag(f));
                    c.getSource().sendSuccess(() -> Component.literal("Flaga " + f + " = " + QuestCapability.get(p).hasFlag(f)), false);
                    return 1;
                }))));
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, String> questArg() {
        return Commands.argument("id", StringArgumentType.word())
                .suggests((c, b) -> SharedSuggestionProvider.suggest(QuestManager.INSTANCE.all().stream().map(QuestDefinition::id), b));
    }

    private static int teleport(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        String target = StringArgumentType.getString(c, "cel");
        BlockPos pos = Locations.resolve(c.getSource().getServer(), target);
        ServerLevel level = Araluen.level(c.getSource().getServer());
        if (pos == null || level == null) {
            c.getSource().sendFailure(Component.literal("Nie znam miejsca: " + target));
            return 0;
        }
        p.teleportTo(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, p.getYRot(), p.getXRot());
        return 1;
    }

    private ZwiadowcyCommands() {}
}
