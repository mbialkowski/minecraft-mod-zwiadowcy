package pl.zwiadowcy.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import pl.zwiadowcy.quest.QuestCapability;
import pl.zwiadowcy.quest.QuestEngine;

/** Fragment z Gorlanu - odczytany dopisuje notatkę do dziennika. */
public class LoreItem extends TooltipItem {
    public static final String TITLE = "Napis z Gorlanu";
    public static final String TEXT = "Na odłamku kamienia zachował się herb, niemal starty przez lata deszczu. "
            + "Pod nim resztki napisu: „…Gorlan… lenno barona Morgaratha… wierny Koronie…”. Ktoś później wydrapał słowo „wierny”. "
            + "Halt mówił, że Morgarath był kiedyś baronem tych ziem, zanim zdradził króla i uciekł w Góry Deszczu i Nocy. "
            + "To tutaj wszystko się zaczęło.";

    public LoreItem(Properties props) {
        super(props, 2);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            sp.sendSystemMessage(Component.literal(TITLE).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
            sp.sendSystemMessage(Component.literal(TEXT).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            QuestCapability.get(sp).addNote(TITLE, TEXT);
            QuestEngine.fireEvent(sp, "przeczytano_fragment", 1);
            QuestEngine.sync(sp);
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }
}
