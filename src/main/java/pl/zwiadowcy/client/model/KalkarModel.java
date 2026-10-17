package pl.zwiadowcy.client.model;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import pl.zwiadowcy.Zwiadowcy;
import pl.zwiadowcy.entity.KalkarEntity;

/**
 * Kalkar: zgarbiona, kudłata bestia wysoka na trzy bloki, z długimi ramionami sięgającymi ziemi.
 * Tekstura 128×64 - układ UV musi zgadzać się z generatorem (TextureGen.kalkar).
 */
public class KalkarModel extends HierarchicalModel<KalkarEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Zwiadowcy.id("kalkar"), "main");

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public KalkarModel(ModelPart root) {
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
                        .texOffs(0, 0).addBox(-8f, -22f, -5f, 16, 22, 10),
                PartPose.offsetAndRotation(0f, 8f, 0f, 0.35f, 0f, 0f));
        body.addOrReplaceChild("mane", CubeListBuilder.create()
                        .texOffs(56, 20).addBox(-9f, -24f, -6f, 18, 8, 12),
                PartPose.ZERO);
        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(56, 0).addBox(-5f, -9f, -7f, 10, 9, 9),
                PartPose.offsetAndRotation(0f, -19f, -4f, -0.35f, 0f, 0f));
        head.addOrReplaceChild("snout", CubeListBuilder.create()
                        .texOffs(96, 0).addBox(-3f, -4f, -10f, 6, 4, 3),
                PartPose.ZERO);
        PartDefinition rightArm = body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                        .texOffs(24, 32).addBox(-3f, -2f, -3f, 6, 24, 6),
                PartPose.offsetAndRotation(-11f, -18f, 0f, -0.35f, 0f, 0f));
        rightArm.addOrReplaceChild("right_claw", CubeListBuilder.create()
                        .texOffs(48, 40).addBox(-3f, 22f, -3f, 6, 4, 6),
                PartPose.ZERO);
        PartDefinition leftArm = body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                        .texOffs(24, 32).mirror().addBox(-3f, -2f, -3f, 6, 24, 6),
                PartPose.offsetAndRotation(11f, -18f, 0f, -0.35f, 0f, 0f));
        leftArm.addOrReplaceChild("left_claw", CubeListBuilder.create()
                        .texOffs(48, 40).mirror().addBox(-3f, 22f, -3f, 6, 4, 6),
                PartPose.ZERO);
        r.addOrReplaceChild("right_leg", CubeListBuilder.create()
                        .texOffs(0, 40).addBox(-3f, 0f, -3f, 6, 16, 6),
                PartPose.offset(-4.5f, 8f, 0f));
        r.addOrReplaceChild("left_leg", CubeListBuilder.create()
                        .texOffs(0, 40).mirror().addBox(-3f, 0f, -3f, 6, 16, 6),
                PartPose.offset(4.5f, 8f, 0f));
        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(KalkarEntity e, float limbSwing, float limbAmount, float age, float yaw, float pitch) {
        float walk = Mth.cos(limbSwing * 0.5f) * 1.1f * limbAmount;
        rightLeg.xRot = walk;
        leftLeg.xRot = -walk;
        head.yRot = yaw * Mth.DEG_TO_RAD * 0.8f;
        head.xRot = -0.35f + pitch * Mth.DEG_TO_RAD * 0.6f;
        body.xRot = e.isCharging() ? 0.65f : 0.35f + Mth.sin(age * 0.08f) * 0.03f;

        float armBase = e.isCharging() ? -1.3f : -0.35f;
        rightArm.xRot = armBase - walk * 0.8f + Mth.sin(age * 0.1f) * 0.05f;
        leftArm.xRot = armBase + walk * 0.8f - Mth.sin(age * 0.1f) * 0.05f;
        rightArm.zRot = 0.08f;
        leftArm.zRot = -0.08f;
        if (attackTime > 0) {
            float swing = Mth.sin(attackTime * Mth.PI);
            rightArm.xRot = -2.4f * swing;
            leftArm.xRot = -2.2f * swing;
        }
    }
}
