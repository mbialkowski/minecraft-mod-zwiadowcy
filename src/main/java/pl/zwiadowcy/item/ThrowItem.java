package pl.zwiadowcy.item;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.function.BiFunction;
import java.util.function.Supplier;

/** Przedmiot rzucany (kamyk, garnek z dymem): PPM wyrzuca pocisk i zużywa sztukę. */
public class ThrowItem extends TooltipItem {
    private final BiFunction<Level, Player, ThrowableItemProjectile> projectile;
    private final Supplier<SoundEvent> sound;
    private final float velocity;
    private final int cooldown;

    public ThrowItem(Properties props, int tooltipLines, BiFunction<Level, Player, ThrowableItemProjectile> projectile,
                     Supplier<SoundEvent> sound, float velocity, int cooldown) {
        super(props, tooltipLines);
        this.projectile = projectile;
        this.sound = sound;
        this.velocity = velocity;
        this.cooldown = cooldown;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), sound.get(), SoundSource.PLAYERS, 0.6f, 0.9f);
        if (!level.isClientSide) {
            ThrowableItemProjectile p = projectile.apply(level, player);
            p.setItem(stack.copyWithCount(1));
            p.shootFromRotation(player, player.getXRot(), player.getYRot(), 0f, velocity, 1.0f);
            level.addFreshEntity(p);
        }
        player.getCooldowns().addCooldown(this, cooldown);
        if (!player.getAbilities().instabuild) stack.shrink(1);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
