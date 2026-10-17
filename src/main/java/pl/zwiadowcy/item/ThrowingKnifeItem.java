package pl.zwiadowcy.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import pl.zwiadowcy.entity.projectile.ThrowingKnifeEntity;

/** Nóż do rzucania - wbija się w cel, można go podnieść z powrotem. */
public class ThrowingKnifeItem extends TooltipItem {
    public ThrowingKnifeItem(Properties props) {
        super(props, 1);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 0.6f, 1.6f);
        if (!level.isClientSide) {
            ThrowingKnifeEntity knife = new ThrowingKnifeEntity(level, player, stack.copyWithCount(1));
            knife.shootFromRotation(player, player.getXRot(), player.getYRot(), 0f, 2.2f, 0.8f);
            if (player.getAbilities().instabuild) knife.pickup = net.minecraft.world.entity.projectile.AbstractArrow.Pickup.CREATIVE_ONLY;
            level.addFreshEntity(knife);
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        player.getCooldowns().addCooldown(this, 8);
        if (!player.getAbilities().instabuild) stack.shrink(1);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
