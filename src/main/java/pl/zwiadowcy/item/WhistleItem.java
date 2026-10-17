package pl.zwiadowcy.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import pl.zwiadowcy.entity.TugEntity;
import pl.zwiadowcy.quest.QuestCapability;
import pl.zwiadowcy.quest.QuestProgress;
import pl.zwiadowcy.registry.ModEntities;

/** Gwizdek - przywołuje Wyrwija, jeśli koń jest związany z graczem. */
public class WhistleItem extends TooltipItem {
    public WhistleItem(Properties props) {
        super(props, 1);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        level.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_FLUTE.value(), SoundSource.PLAYERS, 1.5f, 1.8f);
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            summon(sp);
            player.getCooldowns().addCooldown(this, 100);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    public static void summon(ServerPlayer sp) {
        QuestProgress prog = QuestCapability.get(sp);
        if (prog.tugUuid == null) {
            sp.displayClientMessage(Component.literal("Gwiżdżesz… ale żaden koń jeszcze nie odpowiada na twój gwizd.").withStyle(ChatFormatting.GRAY), true);
            return;
        }
        ServerLevel level = sp.serverLevel();
        Entity existing = level.getEntity(prog.tugUuid);
        if (existing instanceof TugEntity tug && tug.isAlive()) {
            if (tug.isTired()) {
                sp.displayClientMessage(Component.literal("Wyrwij jest zbyt zmęczony, by przybiec.").withStyle(ChatFormatting.GRAY), true);
                return;
            }
            if (tug.distanceTo(sp) > 12) {
                tug.teleportTo(sp.getX() + 2, sp.getY(), sp.getZ() + 2);
            }
            tug.getNavigation().moveTo(sp, 1.4);
        } else {
            // koń jest w niezaładowanym miejscu lub innym wymiarze - przybiega "nowy" (stara kopia zniknie)
            prog.tugGeneration++;
            TugEntity tug = ModEntities.WYRWIJ.get().create(level);
            if (tug == null) return;
            tug.moveTo(sp.getX() + 2, sp.getY(), sp.getZ() + 2, sp.getYRot(), 0f);
            tug.bondTo(sp);
            tug.setGeneration(prog.tugGeneration);
            level.addFreshEntity(tug);
            prog.tugUuid = tug.getUUID();
        }
        sp.displayClientMessage(Component.literal("Wyrwij rży w oddali i przybiega galopem!").withStyle(ChatFormatting.GREEN), true);
    }
}
