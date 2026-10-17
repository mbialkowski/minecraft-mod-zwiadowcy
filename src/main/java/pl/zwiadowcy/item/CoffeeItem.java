package pl.zwiadowcy.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import pl.zwiadowcy.quest.QuestEngine;
import pl.zwiadowcy.registry.ModEffects;

/** Kawa zwiadowcy z miodem: Czujność na 3 minuty, zostaje pusta butelka. */
public class CoffeeItem extends TooltipItem {
    public CoffeeItem(Properties props) {
        super(props, 2);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide) {
            entity.addEffect(new MobEffectInstance(ModEffects.CZUJNOSC.get(), 3600, 0));
            if (entity instanceof ServerPlayer sp) QuestEngine.fireEvent(sp, "use:kawa_zwiadowcy", 1);
        }
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (entity instanceof Player p && !p.getAbilities().instabuild) {
            if (result.isEmpty()) return new ItemStack(Items.GLASS_BOTTLE);
            if (!p.getInventory().add(new ItemStack(Items.GLASS_BOTTLE))) p.drop(new ItemStack(Items.GLASS_BOTTLE), false);
        }
        return result;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 32;
    }

    @Override
    public SoundEvent getDrinkingSound() {
        return SoundEvents.GENERIC_DRINK;
    }

    @Override
    public SoundEvent getEatingSound() {
        return SoundEvents.GENERIC_DRINK;
    }
}
