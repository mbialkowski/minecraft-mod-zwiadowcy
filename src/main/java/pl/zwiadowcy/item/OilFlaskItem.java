package pl.zwiadowcy.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import pl.zwiadowcy.entity.projectile.OilFlaskEntity;

/** Flasza oleju - rzucona oblewa cele; ogień zadaje im podwójne obrażenia. */
public class OilFlaskItem extends TooltipItem {
    public OilFlaskItem(Properties props) {
        super(props, 2);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SPLASH_POTION_THROW, SoundSource.PLAYERS, 0.6f, 0.8f);
        if (!level.isClientSide) {
            OilFlaskEntity flask = new OilFlaskEntity(level, player);
            flask.setItem(stack.copyWithCount(1));
            flask.shootFromRotation(player, player.getXRot(), player.getYRot(), -15f, 0.9f, 1.0f);
            level.addFreshEntity(flask);
        }
        player.getCooldowns().addCooldown(this, 20);
        if (!player.getAbilities().instabuild) stack.shrink(1);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
