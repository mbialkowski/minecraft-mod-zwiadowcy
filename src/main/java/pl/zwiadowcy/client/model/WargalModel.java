package pl.zwiadowcy.client.model;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import pl.zwiadowcy.Zwiadowcy;
import pl.zwiadowcy.entity.WargalEntity;

/** Wargal: zgarbiony, psi łeb na ludzkim tułowiu. Tekstura 64×64 (TextureGen.wargal). */
public class WargalModel extends HierarchicalModel<WargalEntity> implements net.minecraft.client.model.ArmedModel {

    @Override
    public void translateToHand(net.minecraft.world.entity.HumanoidArm arm, com.mojang.blaze3d.vertex.PoseStack pose) {
        root.translateAndRotate(pose);
        body.translateAndRotate(pose);
        (arm == net.minecraft.world.entity.HumanoidArm.RIGHT ? rightArm : leftArm).translateAndRotate(pose);
        pose.translate(0, 0.25f, 0);
    }
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Zwiadowcy.id("wargal"), "main");

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public WargalModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.head = body.getChild("head");
        this.rightArm = body.getChild("right_arm");
        this.leftArm = body.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition r = mesh.getRoot();
        PartDefinition body = r.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(16, 16).addBox(-4.5f, -12f, -2.5f, 9, 12, 5),
                PartPose.offsetAndRotation(0f, 12f, 0f, 0.3f, 0f, 0f));
        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4f, -7f, -5f, 8, 7, 7),
                PartPose.offsetAndRotation(0f, -11f, -1f, -0.3f, 0f, 0f));
        head.addOrReplaceChild("snout", CubeListBuilder.create()
                        .texOffs(32, 0).addBox(-2f, -4f, -9f, 4, 3, 4), PartPose.ZERO);
        head.addOrReplaceChild("right_ear", CubeListBuilder.create()
                        .texOffs(48, 0).addBox(-4f, -9f, -1f, 2, 2, 1), PartPose.ZERO);
        head.addOrReplaceChild("left_ear", CubeListBuilder.create()
                        .texOffs(48, 0).mirror().addBox(2f, -9f, -1f, 2, 2, 1), PartPose.ZERO);
        body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                        .texOffs(44, 16).addBox(-2f, -1f, -2f, 4, 13, 4),
                PartPose.offset(-6.5f, -10f, 0f));
        body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                        .texOffs(44, 16).mirror().addBox(-2f, -1f, -2f, 4, 13, 4),
                PartPose.offset(6.5f, -10f, 0f));
        r.addOrReplaceChild("right_leg", CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-2f, 0f, -2f, 4, 12, 4), PartPose.offset(-2.5f, 12f, 0f));
        r.addOrReplaceChild("left_leg", CubeListBuilder.create()
                        .texOffs(0, 16).mirror().addBox(-2f, 0f, -2f, 4, 12, 4), PartPose.offset(2.5f, 12f, 0f));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(WargalEntity e, float limbSwing, float limbAmount, float age, float yaw, float pitch) {
        float walk = Mth.cos(limbSwing * 0.6662f) * 1.3f * limbAmount;
        rightLeg.xRot = walk;
        leftLeg.xRot = -walk;
        rightArm.xRot = -walk * 0.8f - 0.2f;
        leftArm.xRot = walk * 0.8f - 0.2f;
        head.yRot = yaw * Mth.DEG_TO_RAD;
        head.xRot = -0.3f + pitch * Mth.DEG_TO_RAD;
        if (attackTime > 0) {
            float s = Mth.sin(attackTime * Mth.PI);
            rightArm.xRot = -2.0f * s;
            leftArm.xRot = -1.6f * s;
        }
        if (e.isSentry()) {
            rightArm.xRot = -0.1f;
            leftArm.xRot = -0.1f;
        }
    }
}
