package pl.zwiadowcy.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import pl.zwiadowcy.entity.BoarEntity;
import pl.zwiadowcy.quest.QuestEngine;
import pl.zwiadowcy.registry.ModEffects;
import pl.zwiadowcy.stealth.Tracking;

/**
 * Zestaw tropiciela: przez 30 s podświetla ślady w okolicy, wskazuje kierunek celu misji
 * i ujawnia ukryte w pobliżu bestie.
 */
public class TrackerKitItem extends TooltipItem {
    public TrackerKitItem(Properties props) {
        super(props, 2);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            int duration = sp.hasEffect(ModEffects.CZUJNOSC.get()) ? 900 : 600;
            Tracking.start(sp, duration);
            int found = 0;
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(40),
                    e -> e instanceof Enemy || (e instanceof BoarEntity b && b.isEnraged()))) {
                e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0, false, false));
                found++;
            }
            sp.displayClientMessage(Component.literal(found > 0
                    ? "Odczytujesz ślady… W pobliżu czai się " + found + " " + (found == 1 ? "istota." : "istot.")
                    : "Odczytujesz ślady: złamane gałązki, odciski w błocie…").withStyle(ChatFormatting.DARK_GREEN), true);
            level.playSound(null, player.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 0.8f, 1.4f);
            stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
            player.getCooldowns().addCooldown(this, 200);
            QuestEngine.fireEvent(sp, "use:zestaw_tropiciela", 1);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
