package pl.zwiadowcy.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import pl.zwiadowcy.entity.projectile.JavelinEntity;

/** Oszczep - ciężka broń miotana (zdobyczna od Wargalów-oszczepników). */
public class JavelinItem extends TooltipItem {
    public JavelinItem(Properties props) {
        super(props, 1);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1f, 0.8f);
        if (!level.isClientSide) {
            JavelinEntity javelin = new JavelinEntity(level, player, stack.copyWithCount(1));
            javelin.shootFromRotation(player, player.getXRot(), player.getYRot(), 0f, 1.9f, 0.6f);
            if (player.getAbilities().instabuild) javelin.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            level.addFreshEntity(javelin);
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        player.getCooldowns().addCooldown(this, 20);
        if (!player.getAbilities().instabuild) stack.shrink(1);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
