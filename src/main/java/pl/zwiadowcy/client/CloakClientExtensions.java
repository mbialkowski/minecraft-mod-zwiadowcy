package pl.zwiadowcy.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import pl.zwiadowcy.client.model.RangerCloakModel;

/** Podmienia model zbroi na płaszcz z kapturem (kaptur widoczny mimo slotu napierśnika). */
public final class CloakClientExtensions implements IClientItemExtensions {
    public static final CloakClientExtensions INSTANCE = new CloakClientExtensions();
    private RangerCloakModel<LivingEntity> model;

    private CloakClientExtensions() {}

    private RangerCloakModel<LivingEntity> model() {
        if (model == null) {
            model = new RangerCloakModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(RangerCloakModel.LAYER));
        }
        return model;
    }

    @Override
    public @NotNull Model getGenericArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
        RangerCloakModel<LivingEntity> m = model();
        ForgeHooksClient.copyModelProperties(original, m);
        m.head.visible = true;
        m.hat.visible = false;
        m.body.visible = true;
        m.rightArm.visible = true;
        m.leftArm.visible = true;
        m.rightLeg.visible = false;
        m.leftLeg.visible = false;
        return m;
    }
}
