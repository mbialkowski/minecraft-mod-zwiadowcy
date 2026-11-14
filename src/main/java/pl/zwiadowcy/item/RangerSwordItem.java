package pl.zwiadowcy.item;

import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeTier;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Miecz zwiadowcy (lekki, szybki, słabszy od rycerskiego) i drewniany miecz ćwiczebny. PPM to garda:
 * cios przyjęty tuż po jej podniesieniu jest parowany (patrz {@link pl.zwiadowcy.combat.Parry}).
 */
public class RangerSwordItem extends SwordItem {
    public static final ForgeTier TIER = new ForgeTier(2, 450, 6.0f, 1.0f, 14,
            BlockTags.NEEDS_IRON_TOOL, () -> Ingredient.of(Items.IRON_INGOT));
    public static final ForgeTier PRACTICE = new ForgeTier(0, 200, 2.0f, 0.0f, 0,
            BlockTags.NEEDS_STONE_TOOL, () -> Ingredient.of(net.minecraft.tags.ItemTags.PLANKS));

    private final int tooltipLines;

    public RangerSwordItem(Tier tier, int damage, float speed, int tooltipLines, Properties props) {
        super(tier, damage, speed, props);
        this.tooltipLines = tooltipLines;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BLOCK;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        TooltipItem.addLines(this, tooltipLines, tooltip);
    }
}
