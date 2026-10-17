package pl.zwiadowcy.registry;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import pl.zwiadowcy.Zwiadowcy;
import pl.zwiadowcy.item.QuiverMenu;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Zwiadowcy.MODID);

    public static final RegistryObject<MenuType<QuiverMenu>> KOLCZAN = MENUS.register("kolczan",
            () -> IForgeMenuType.create(QuiverMenu::new));

    private ModMenus() {}
}
