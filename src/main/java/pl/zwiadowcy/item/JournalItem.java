package pl.zwiadowcy.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import pl.zwiadowcy.client.ClientHandlers;
import pl.zwiadowcy.quest.QuestEngine;

/** Dziennik zwiadowcy - otwiera ekran misji i notatek (także klawisz J). */
public class JournalItem extends TooltipItem {
    public JournalItem(Properties props) {
        super(props, 1);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientHandlers::openJournal);
        } else if (player instanceof ServerPlayer sp) {
            QuestEngine.sync(sp);
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }
}
