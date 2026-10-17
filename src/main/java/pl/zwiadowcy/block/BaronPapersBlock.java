package pl.zwiadowcy.block;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import pl.zwiadowcy.quest.QuestCapability;
import pl.zwiadowcy.quest.QuestEngine;
import pl.zwiadowcy.quest.QuestProgress;
import pl.zwiadowcy.registry.ModItems;

/** Papiery na biurku barona Aralda - w misji "Nocna wspinaczka" można zabrać z nich dokument. */
public class BaronPapersBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(1, 0, 2, 15, 1, 14);
    public static final String QUEST = "nocna_wspinaczka";

    public BaronPapersBlock(Properties props) {
        super(props);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer sp)) return InteractionResult.PASS;
        QuestProgress.State st = QuestCapability.get(sp).get(QUEST);
        boolean wanted = st != null && st.status == QuestProgress.Status.ACTIVE && st.stage >= 1;
        if (wanted && QuestEngine.countItems(sp, "zwiadowcy:dokument_barona") == 0) {
            sp.getInventory().add(new ItemStack(ModItems.DOKUMENT_BARONA.get()));
            level.playSound(null, pos, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1f, 1.2f);
            sp.displayClientMessage(Component.literal("Na wierzchu leży kartka z twoim imieniem…").withStyle(ChatFormatting.GOLD), true);
        } else {
            sp.displayClientMessage(Component.literal("Rachunki, listy, mapy lenna Redmont… nic dla ciebie.").withStyle(ChatFormatting.GRAY), true);
        }
        return InteractionResult.CONSUME;
    }
}
