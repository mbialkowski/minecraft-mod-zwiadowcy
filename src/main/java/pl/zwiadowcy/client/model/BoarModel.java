package pl.zwiadowcy.client.model;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import pl.zwiadowcy.Zwiadowcy;
import pl.zwiadowcy.entity.BoarEntity;

/** Dzik: masywny tułów, szczecina na grzbiecie, kły. Tekstura 64×64 (TextureGen.boar). */
public class BoarModel extends HierarchicalModel<BoarEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Zwiadowcy.id("dzik"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart rightFront;
    private final ModelPart leftFront;
    private final ModelPart rightHind;
    private final ModelPart leftHind;

    public BoarModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.rightFront = root.getChild("right_front_leg");
        this.leftFront = root.getChild("left_front_leg");
        this.rightHind = root.getChild("right_hind_leg");
        this.leftHind = root.getChild("left_hind_leg");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition r = mesh.getRoot();
        PartDefinition body = r.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-5f, -5f, -9f, 10, 9, 18), PartPose.offset(0f, 13f, 0f));
        body.addOrReplaceChild("bristles", CubeListBuilder.create()
                .texOffs(0, 44).addBox(-1.5f, -7f, -9f, 3, 2, 16), PartPose.ZERO);
        PartDefinition head = r.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 28).addBox(-4f, -4f, -7f, 8, 8, 7), PartPose.offset(0f, 13f, -9f));
        head.addOrReplaceChild("snout", CubeListBuilder.create()
                .texOffs(30, 28).addBox(-2.5f, 0f, -10f, 5, 4, 3), PartPose.ZERO);
        head.addOrReplaceChild("right_tusk", CubeListBuilder.create()
                .texOffs(46, 28).addBox(-3.5f, -1f, -9f, 1, 3, 1), PartPose.ZERO);
        head.addOrReplaceChild("left_tusk", CubeListBuilder.create()
                .texOffs(46, 28).addBox(2.5f, -1f, -9f, 1, 3, 1), PartPose.ZERO);
        head.addOrReplaceChild("right_ear", CubeListBuilder.create()
                .texOffs(52, 28).addBox(-4f, -6f, -2f, 2, 2, 1), PartPose.ZERO);
        head.addOrReplaceChild("left_ear", CubeListBuilder.create()
                .texOffs(52, 28).addBox(2f, -6f, -2f, 2, 2, 1), PartPose.ZERO);
        CubeListBuilder leg = CubeListBuilder.create().texOffs(40, 44).addBox(-1.5f, 0f, -1.5f, 3, 7, 3);
        r.addOrReplaceChild("right_front_leg", leg, PartPose.offset(-3f, 17f, -6f));
        r.addOrReplaceChild("left_front_leg", leg, PartPose.offset(3f, 17f, -6f));
        r.addOrReplaceChild("right_hind_leg", leg, PartPose.offset(-3f, 17f, 6f));
        r.addOrReplaceChild("left_hind_leg", leg, PartPose.offset(3f, 17f, 6f));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(BoarEntity e, float limbSwing, float limbAmount, float age, float yaw, float pitch) {
        float speed = e.isCharging() ? 1.0f : 0.6662f;
        float walk = Mth.cos(limbSwing * speed) * 1.4f * limbAmount;
        rightFront.xRot = walk;
        leftHind.xRot = walk;
        leftFront.xRot = -walk;
        rightHind.xRot = -walk;
        head.yRot = yaw * Mth.DEG_TO_RAD;
        head.xRot = e.isCharging() ? 0.45f : pitch * Mth.DEG_TO_RAD;
    }
}
