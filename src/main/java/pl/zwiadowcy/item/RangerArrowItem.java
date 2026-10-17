package pl.zwiadowcy.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import pl.zwiadowcy.entity.projectile.RangerArrowEntity;
import pl.zwiadowcy.entity.projectile.RangerArrowType;

import javax.annotation.Nullable;
import java.util.List;

/** Strzały specjalne: płonąca, z szerokim grotem, gwiżdżąca, z liną. */
public class RangerArrowItem extends ArrowItem {
    private final RangerArrowType type;

    public RangerArrowItem(RangerArrowType type, Properties props) {
        super(props);
        this.type = type;
    }

    public RangerArrowType type() {
        return type;
    }

    @Override
    public AbstractArrow createArrow(Level level, ItemStack stack, LivingEntity shooter) {
        return new RangerArrowEntity(level, shooter, type);
    }

    @Override
    public boolean isInfinite(ItemStack stack, ItemStack bow, Player player) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        TooltipItem.addLines(this, 1, tooltip);
    }
}
