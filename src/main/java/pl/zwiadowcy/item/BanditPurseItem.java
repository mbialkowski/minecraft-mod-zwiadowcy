package pl.zwiadowcy.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import pl.zwiadowcy.registry.ModItems;

import java.util.ArrayList;
import java.util.List;

/** Sakiewka bandyty - PPM wysypuje zrabowane drobiazgi. */
public class BanditPurseItem extends TooltipItem {
    public BanditPurseItem(Properties props) {
        super(props, 1);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            RandomSource r = level.random;
            List<ItemStack> loot = new ArrayList<>();
            loot.add(new ItemStack(Items.GOLD_NUGGET, 2 + r.nextInt(6)));
            if (r.nextFloat() < 0.6f) loot.add(new ItemStack(Items.IRON_NUGGET, 1 + r.nextInt(5)));
            if (r.nextFloat() < 0.2f) loot.add(new ItemStack(Items.EMERALD));
            if (r.nextFloat() < 0.15f) loot.add(new ItemStack(Items.ARROW, 4 + r.nextInt(6)));
            if (r.nextFloat() < 0.1f) loot.add(new ItemStack(ModItems.KAWA_ZWIADOWCY.get()));
            if (r.nextFloat() < 0.08f) loot.add(new ItemStack(Items.GOLD_INGOT));
            for (ItemStack s : loot) if (!player.getInventory().add(s)) player.drop(s, false);
            level.playSound(null, player.blockPosition(), SoundEvents.ARMOR_EQUIP_GOLD, SoundSource.PLAYERS, 1f, 1.3f);
            player.displayClientMessage(Component.literal("Wysypujesz zawartość sakiewki.").withStyle(ChatFormatting.GOLD), true);
            if (!player.getAbilities().instabuild) stack.shrink(1);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
