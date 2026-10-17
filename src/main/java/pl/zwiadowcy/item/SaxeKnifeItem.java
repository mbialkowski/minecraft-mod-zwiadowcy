package pl.zwiadowcy.item;

import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeTier;

import javax.annotation.Nullable;
import java.util.List;

/** Nóż saksoński (saksa): szybki, ciężki nóż do walki wręcz. */
public class SaxeKnifeItem extends SwordItem {
    public static final ForgeTier TIER = new ForgeTier(2, 520, 6.5f, 2.0f, 16,
            BlockTags.NEEDS_IRON_TOOL, () -> Ingredient.of(Items.IRON_INGOT));

    public SaxeKnifeItem(Properties props) {
        super(TIER, 3, -2.0f, props);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        TooltipItem.addLines(this, 1, tooltip);
    }
}
