package pl.zwiadowcy.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import pl.zwiadowcy.client.ClientHandlers;
import pl.zwiadowcy.quest.QuestCapability;
import pl.zwiadowcy.quest.QuestEngine;
import pl.zwiadowcy.story.LoreNotes;

import javax.annotation.Nullable;
import java.util.List;

/** Zapiski znalezione w świecie. PPM - przeczytaj (tekst trafia też do dziennika). */
public class LoreNoteItem extends Item {
    public static final String TAG = "notatka";

    public LoreNoteItem(Properties props) {
        super(props);
    }

    @Nullable
    public static LoreNotes.Note note(ItemStack stack) {
        return stack.hasTag() ? LoreNotes.get(stack.getTag().getString(TAG)) : null;
    }

    public static ItemStack of(net.minecraft.world.item.Item item, String id) {
        ItemStack s = new ItemStack(item);
        s.getOrCreateTag().putString(TAG, id);
        return s;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        LoreNotes.Note note = note(stack);
        if (note == null) return InteractionResultHolder.pass(stack);
        if (level.isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHandlers.openNote(note));
        } else if (player instanceof ServerPlayer sp) {
            level.playSound(null, player.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1f, 1f);
            QuestCapability.get(sp).addNote(note.title(), note.text());
            QuestEngine.fireEvent(sp, "notatka:" + stack.getTag().getString(TAG), 1);
            QuestEngine.sync(sp);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        LoreNotes.Note note = note(stack);
        if (note != null) tooltip.add(Component.literal(note.title()).withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("item.zwiadowcy.zapiski.opis1").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }
}
