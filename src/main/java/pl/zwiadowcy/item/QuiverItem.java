package pl.zwiadowcy.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Kołczan: 4 przegródki na różne rodzaje strzał. Łuk zwiadowcy bierze strzały z wybranej przegródki
 * (zmiana klawiszem R). Prawy przycisk otwiera kołczan.
 */
public class QuiverItem extends Item {
    public static final int SLOTS = 4;

    public QuiverItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            NetworkHooks.openScreen(sp, new SimpleMenuProvider((id, inv, p) -> new QuiverMenu(id, inv, hand),
                    Component.translatable("item.zwiadowcy.kolczan")), buf -> buf.writeEnum(hand));
            level.playSound(null, player.blockPosition(), SoundEvents.ARMOR_EQUIP_LEATHER, SoundSource.PLAYERS, 0.6f, 1.2f);
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }

    // ------------------------------------------------------------------ zawartość w NBT

    public static NonNullList<ItemStack> load(ItemStack quiver) {
        NonNullList<ItemStack> items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
        CompoundTag tag = quiver.getTag();
        if (tag != null) ContainerHelper.loadAllItems(tag, items);
        return items;
    }

    public static void save(ItemStack quiver, NonNullList<ItemStack> items) {
        CompoundTag tag = quiver.getOrCreateTag();
        ContainerHelper.saveAllItems(tag, items, true);
    }

    public static void saveFrom(ItemStack quiver, Container container) {
        NonNullList<ItemStack> items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
        for (int i = 0; i < SLOTS; i++) items.set(i, container.getItem(i));
        save(quiver, items);
    }

    public static int selectedIndex(ItemStack quiver) {
        CompoundTag tag = quiver.getTag();
        return tag == null ? 0 : Math.floorMod(tag.getInt("Selected"), SLOTS);
    }

    /** Kopia wybranej strzały; jeśli wybrana przegródka jest pusta - pierwsza niepusta. */
    public static ItemStack selectedArrow(ItemStack quiver) {
        NonNullList<ItemStack> items = load(quiver);
        int sel = selectedIndex(quiver);
        if (!items.get(sel).isEmpty()) return items.get(sel).copy();
        for (ItemStack s : items) if (!s.isEmpty()) return s.copy();
        return ItemStack.EMPTY;
    }

    public static void consumeSelected(ItemStack quiver, int amount) {
        NonNullList<ItemStack> items = load(quiver);
        int sel = selectedIndex(quiver);
        int idx = !items.get(sel).isEmpty() ? sel : -1;
        if (idx < 0) for (int i = 0; i < SLOTS; i++) if (!items.get(i).isEmpty()) { idx = i; break; }
        if (idx < 0) return;
        items.get(idx).shrink(amount);
        save(quiver, items);
    }

    public static ItemStack findQuiver(Player player) {
        for (ItemStack s : player.getInventory().offhand) if (s.getItem() instanceof QuiverItem) return s;
        for (ItemStack s : player.getInventory().items) if (s.getItem() instanceof QuiverItem) return s;
        return ItemStack.EMPTY;
    }

    public static void cycleSelected(ServerPlayer player) {
        ItemStack quiver = findQuiver(player);
        if (quiver.isEmpty()) {
            player.displayClientMessage(Component.literal("Nie masz kołczana.").withStyle(ChatFormatting.GRAY), true);
            return;
        }
        NonNullList<ItemStack> items = load(quiver);
        int sel = selectedIndex(quiver);
        for (int step = 1; step <= SLOTS; step++) {
            int next = (sel + step) % SLOTS;
            if (!items.get(next).isEmpty()) {
                quiver.getOrCreateTag().putInt("Selected", next);
                ItemStack s = items.get(next);
                player.displayClientMessage(Component.literal("Strzała: ").withStyle(ChatFormatting.GRAY)
                        .append(s.getHoverName().copy().withStyle(ChatFormatting.WHITE))
                        .append(Component.literal(" ×" + s.getCount()).withStyle(ChatFormatting.GRAY)), true);
                player.level().playSound(null, player.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.4f, 1.6f);
                return;
            }
        }
        player.displayClientMessage(Component.literal("Kołczan jest pusty.").withStyle(ChatFormatting.GRAY), true);
    }

    public static boolean isArrow(ItemStack stack) {
        return stack.is(ItemTags.ARROWS);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        NonNullList<ItemStack> items = load(stack);
        int sel = selectedIndex(stack);
        boolean any = false;
        for (int i = 0; i < SLOTS; i++) {
            ItemStack s = items.get(i);
            if (s.isEmpty()) continue;
            any = true;
            tooltip.add(Component.literal((i == sel ? "▶ " : "  ") + s.getCount() + "× ").withStyle(i == sel ? ChatFormatting.YELLOW : ChatFormatting.GRAY)
                    .append(s.getHoverName().copy().withStyle(i == sel ? ChatFormatting.YELLOW : ChatFormatting.GRAY)));
        }
        if (!any) tooltip.add(Component.translatable("item.zwiadowcy.kolczan.pusty").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("item.zwiadowcy.kolczan.opis1").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }
}
