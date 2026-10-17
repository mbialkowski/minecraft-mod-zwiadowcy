package pl.zwiadowcy.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/** Przedmiot z opisem z pliku językowego: klucze &lt;id&gt;.opis1, .opis2… */
public class TooltipItem extends Item {
    private final int lines;

    public TooltipItem(Properties props, int lines) {
        super(props);
        this.lines = lines;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        addLines(this, lines, tooltip);
    }

    public static void addLines(Item item, int lines, List<Component> tooltip) {
        for (int i = 1; i <= lines; i++) {
            tooltip.add(Component.translatable(item.getDescriptionId() + ".opis" + i).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
    }
}
