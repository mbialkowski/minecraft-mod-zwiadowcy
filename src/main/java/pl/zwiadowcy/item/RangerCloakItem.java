package pl.zwiadowcy.item;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

/**
 * Płaszcz zwiadowcy (slot napierśnika). Kaptur i długa poła renderowane własnym modelem.
 * Kamuflaż obsługuje {@link pl.zwiadowcy.stealth.Stealth}; Bezruch - klawisz V.
 */
public class RangerCloakItem extends ArmorItem {
    public static final String TEXTURE = "zwiadowcy:textures/models/armor/plaszcz_zwiadowcy.png";

    public static final ArmorMaterial MATERIAL = new ArmorMaterial() {
        @Override public int getDurabilityForType(Type type) { return 320; }
        @Override public int getDefenseForType(Type type) { return 3; }
        @Override public int getEnchantmentValue() { return 15; }
        @Override public SoundEvent getEquipSound() { return SoundEvents.ARMOR_EQUIP_LEATHER; }
        @Override public Ingredient getRepairIngredient() { return Ingredient.of(Items.GREEN_WOOL, Items.GRAY_WOOL); }
        @Override public String getName() { return "zwiadowcy:plaszcz_zwiadowcy"; }
        @Override public float getToughness() { return 0f; }
        @Override public float getKnockbackResistance() { return 0f; }
    };

    public RangerCloakItem(Properties props) {
        super(MATERIAL, Type.CHESTPLATE, props);
    }

    @Override
    public @Nullable String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return TEXTURE;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(pl.zwiadowcy.client.CloakClientExtensions.INSTANCE);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        TooltipItem.addLines(this, 3, tooltip);
    }
}
