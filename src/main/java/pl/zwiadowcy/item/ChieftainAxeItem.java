package pl.zwiadowcy.item;

import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeTier;

import javax.annotation.Nullable;
import java.util.List;

/** Topór herszta Wargalów - ciężki, z ząbkowanym ostrzem. */
public class ChieftainAxeItem extends AxeItem {
    public static final ForgeTier TIER = new ForgeTier(2, 700, 6.0f, 3.0f, 10,
            BlockTags.NEEDS_IRON_TOOL, () -> Ingredient.of(Items.IRON_INGOT));

    public ChieftainAxeItem(Properties props) {
        super(TIER, 6.0f, -3.1f, props);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        TooltipItem.addLines(this, 1, tooltip);
    }
}
