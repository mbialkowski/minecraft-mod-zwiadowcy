package pl.zwiadowcy.item;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import pl.zwiadowcy.registry.ModEffects;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Łuk zwiadowcy: napina się szybciej niż zwykły łuk (14 ticków zamiast 20, z Czujnością 10),
 * strzela mocniej i korzysta ze strzał z kołczana (wybór klawiszem R).
 */
public class RangerBowItem extends BowItem {
    public static final float DRAW_TICKS = 14f;
    public static final float DRAW_TICKS_VIGILANT = 10f;

    public RangerBowItem(Properties props) {
        super(props);
    }

    public static float drawTicks(@Nullable LivingEntity entity) {
        return entity != null && entity.hasEffect(ModEffects.CZUJNOSC.get()) ? DRAW_TICKS_VIGILANT : DRAW_TICKS;
    }

    public static float power(int charge, @Nullable LivingEntity entity) {
        float f = charge / drawTicks(entity);
        f = (f * f + f * 2.0f) / 3.0f;
        return Math.min(f, 1.0f);
    }

    /** Amunicja: najpierw wybrana strzała z kołczana, potem zwykłe szukanie w ekwipunku. */
    private record Ammo(ItemStack stack, @Nullable ItemStack quiver) {}

    private Ammo findAmmo(Player player, ItemStack bow) {
        ItemStack quiver = QuiverItem.findQuiver(player);
        if (!quiver.isEmpty()) {
            ItemStack sel = QuiverItem.selectedArrow(quiver);
            if (!sel.isEmpty()) return new Ammo(sel, quiver);
        }
        return new Ammo(player.getProjectile(bow), null);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack bow = player.getItemInHand(hand);
        boolean has = !findAmmo(player, bow).stack().isEmpty();
        InteractionResultHolder<ItemStack> ret = net.minecraftforge.event.ForgeEventFactory.onArrowNock(bow, level, player, hand, has);
        if (ret != null) return ret;
        if (!player.getAbilities().instabuild && !has) return InteractionResultHolder.fail(bow);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(bow);
    }

    @Override
    public void releaseUsing(ItemStack bow, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) return;
        boolean infinite = player.getAbilities().instabuild
                || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, bow) > 0;
        Ammo ammo = findAmmo(player, bow);
        ItemStack arrowStack = ammo.stack();
        int charge = getUseDuration(bow) - timeLeft;
        charge = net.minecraftforge.event.ForgeEventFactory.onArrowLoose(bow, level, player, charge, !arrowStack.isEmpty() || infinite);
        if (charge < 0) return;
        if (arrowStack.isEmpty() && !infinite) return;
        if (arrowStack.isEmpty()) arrowStack = new ItemStack(Items.ARROW);

        float f = power(charge, player);
        if (f < 0.1f) return;
        boolean noConsume = player.getAbilities().instabuild
                || (arrowStack.getItem() instanceof ArrowItem ai && ai.isInfinite(arrowStack, bow, player));
        if (!level.isClientSide) {
            ArrowItem arrowItem = (ArrowItem) (arrowStack.getItem() instanceof ArrowItem ? arrowStack.getItem() : Items.ARROW);
            AbstractArrow arrow = customArrow(arrowItem.createArrow(level, arrowStack, player));
            arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, f * 3.6f, 0.6f);
            arrow.setBaseDamage(arrow.getBaseDamage() + 0.5);
            if (f == 1.0f) arrow.setCritArrow(true);
            int powerLvl = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, bow);
            if (powerLvl > 0) arrow.setBaseDamage(arrow.getBaseDamage() + powerLvl * 0.5 + 0.5);
            int punch = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, bow);
            if (punch > 0) arrow.setKnockback(punch);
            if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, bow) > 0) arrow.setSecondsOnFire(100);
            bow.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(player.getUsedItemHand()));
            if (noConsume || player.getAbilities().instabuild && (arrowStack.is(Items.SPECTRAL_ARROW) || arrowStack.is(Items.TIPPED_ARROW))) {
                arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            }
            level.addFreshEntity(arrow);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS,
                1.0f, 1.0f / (level.getRandom().nextFloat() * 0.4f + 1.2f) + f * 0.5f);
        if (!noConsume && !player.getAbilities().instabuild) {
            if (ammo.quiver() != null) {
                if (!level.isClientSide) QuiverItem.consumeSelected(ammo.quiver(), 1);
            } else {
                arrowStack.shrink(1);
                if (arrowStack.isEmpty()) player.getInventory().removeItem(arrowStack);
            }
        }
        player.awardStat(Stats.ITEM_USED.get(this));
    }

    @Override
    public int getEnchantmentValue() {
        return 14;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        TooltipItem.addLines(this, 2, tooltip);
    }
}
