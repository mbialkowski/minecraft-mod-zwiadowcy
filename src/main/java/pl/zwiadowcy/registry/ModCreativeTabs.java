package pl.zwiadowcy.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import pl.zwiadowcy.Zwiadowcy;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Zwiadowcy.MODID);

    public static final RegistryObject<CreativeModeTab> ZWIADOWCY = TABS.register("zwiadowcy",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.zwiadowcy"))
                    .icon(() -> new ItemStack(ModItems.LUK_ZWIADOWCY.get()))
                    .displayItems((params, output) -> ModItems.ITEMS.getEntries().forEach(item -> {
                        if (item.get() == ModItems.ZAPISKI.get()) {
                            for (String id : pl.zwiadowcy.story.LoreNotes.ids()) {
                                output.accept(pl.zwiadowcy.item.LoreNoteItem.of(item.get(), id));
                            }
                        } else {
                            output.accept(item.get());
                        }
                    }))
                    .build());

    private ModCreativeTabs() {}
}
