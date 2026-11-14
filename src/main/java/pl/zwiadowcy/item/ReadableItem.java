package pl.zwiadowcy.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import pl.zwiadowcy.quest.QuestEngine;
import pl.zwiadowcy.story.StoryService;

/** Przedmiot fabularny do przeczytania (np. Rozkazy Morgaratha): PPM otwiera pergamin i zgłasza zdarzenie misji. */
public class ReadableItem extends TooltipItem {
    private final String story;
    private final String event;

    public ReadableItem(Properties props, int tooltipLines, String story, String event) {
        super(props, tooltipLines);
        this.story = story;
        this.event = event;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            level.playSound(null, player.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1f, 0.9f);
            StoryService.queue(sp, story, 1);
            QuestEngine.fireEvent(sp, event, 1);
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }
}
