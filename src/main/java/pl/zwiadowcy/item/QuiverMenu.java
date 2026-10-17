package pl.zwiadowcy.item;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import pl.zwiadowcy.registry.ModMenus;

/** Okno kołczana: 4 przegródki na strzały (układ jak w lejku). */
public class QuiverMenu extends AbstractContainerMenu {
    private final InteractionHand hand;
    private final ItemStack quiver;
    private final SimpleContainer container;
    private boolean loading;

    public QuiverMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, buf.readEnum(InteractionHand.class));
    }

    public QuiverMenu(int id, Inventory inv, InteractionHand hand) {
        super(ModMenus.KOLCZAN.get(), id);
        this.hand = hand;
        this.quiver = inv.player.getItemInHand(hand);
        this.container = new SimpleContainer(QuiverItem.SLOTS) {
            @Override
            public void setChanged() {
                super.setChanged();
                if (!loading) QuiverItem.saveFrom(quiver, this);
            }
        };
        loading = true;
        var items = QuiverItem.load(quiver);
        for (int i = 0; i < QuiverItem.SLOTS; i++) container.setItem(i, items.get(i));
        loading = false;

        for (int i = 0; i < QuiverItem.SLOTS; i++) {
            addSlot(new Slot(container, i, 44 + i * 18, 20) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return QuiverItem.isArrow(stack);
                }
            });
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 51 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            final boolean locked = hand == InteractionHand.MAIN_HAND && col == inv.selected;
            addSlot(new Slot(inv, col, 8 + col * 18, 109) {
                @Override
                public boolean mayPickup(Player player) {
                    return !locked && super.mayPickup(player);
                }
            });
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return player.getItemInHand(hand).getItem() instanceof QuiverItem;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        int quiverEnd = QuiverItem.SLOTS;
        if (index < quiverEnd) {
            if (!moveItemStackTo(stack, quiverEnd, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            if (!QuiverItem.isArrow(stack) || !moveItemStackTo(stack, 0, quiverEnd, false)) return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        return copy;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide) QuiverItem.saveFrom(quiver, container);
    }
}
