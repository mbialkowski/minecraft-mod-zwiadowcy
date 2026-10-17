package pl.zwiadowcy.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import pl.zwiadowcy.entity.BanditEntity;

/** Model gracza z pozą łucznika (jak szkielet), gdy bandyta celuje z łuku. */
public class BanditModel extends PlayerModel<BanditEntity> {
    public BanditModel(ModelPart root) {
        super(root, false);
    }

    @Override
    public void prepareMobModel(BanditEntity e, float limbSwing, float limbAmount, float partialTick) {
        rightArmPose = HumanoidModel.ArmPose.EMPTY;
        leftArmPose = HumanoidModel.ArmPose.EMPTY;
        ItemStack held = e.getItemInHand(InteractionHand.MAIN_HAND);
        if (held.getItem() instanceof BowItem && e.isAggressive()) {
            if (e.getMainArm() == HumanoidArm.RIGHT) rightArmPose = HumanoidModel.ArmPose.BOW_AND_ARROW;
            else leftArmPose = HumanoidModel.ArmPose.BOW_AND_ARROW;
        } else if (!held.isEmpty()) {
            if (e.getMainArm() == HumanoidArm.RIGHT) rightArmPose = HumanoidModel.ArmPose.ITEM;
            else leftArmPose = HumanoidModel.ArmPose.ITEM;
        }
        if (!e.getOffhandItem().isEmpty()) {
            if (e.getMainArm() == HumanoidArm.RIGHT) leftArmPose = HumanoidModel.ArmPose.ITEM;
            else rightArmPose = HumanoidModel.ArmPose.ITEM;
        }
        super.prepareMobModel(e, limbSwing, limbAmount, partialTick);
    }
}
