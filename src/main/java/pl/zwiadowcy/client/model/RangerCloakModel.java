package pl.zwiadowcy.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.LivingEntity;
import pl.zwiadowcy.Zwiadowcy;

/**
 * Płaszcz zwiadowcy: kaptur na głowie, płaszcz na tułowiu, rękawy i długa poła z tyłu do kolan.
 * Tekstura 64×64 (TextureGen.cloakArmor).
 */
public class RangerCloakModel<T extends LivingEntity> extends HumanoidModel<T> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Zwiadowcy.id("plaszcz_zwiadowcy"), "main");

    public RangerCloakModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0f);
        PartDefinition r = mesh.getRoot();
        r.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4f, -8f, -4f, 8, 8, 8, new CubeDeformation(0.75f)), PartPose.ZERO);
        r.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition body = r.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(16, 16).addBox(-4f, 0f, -2f, 8, 12, 4, new CubeDeformation(0.45f)), PartPose.ZERO);
        body.addOrReplaceChild("cape", CubeListBuilder.create()
                        .texOffs(0, 32).addBox(-5f, 0f, 0f, 10, 22, 1),
                PartPose.offsetAndRotation(0f, -0.2f, 2.55f, 0.06f, 0f, 0f));
        r.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(40, 16).addBox(-3f, -2f, -2f, 4, 12, 4, new CubeDeformation(0.4f)), PartPose.offset(-5f, 2f, 0f));
        r.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(40, 16).mirror().addBox(-1f, -2f, -2f, 4, 12, 4, new CubeDeformation(0.4f)), PartPose.offset(5f, 2f, 0f));
        r.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9f, 12f, 0f));
        r.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9f, 12f, 0f));
        return LayerDefinition.create(mesh, 64, 64);
    }
}
