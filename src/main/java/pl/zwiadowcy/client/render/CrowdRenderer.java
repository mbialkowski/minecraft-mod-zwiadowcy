package pl.zwiadowcy.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import pl.zwiadowcy.Zwiadowcy;
import pl.zwiadowcy.client.model.WargalModel;
import pl.zwiadowcy.entity.CrowdDummyEntity;

import java.util.EnumMap;
import java.util.Map;

/** Manekiny tłumu: model Wargala albo człowieka (zależnie od skórki), lekkie kołysanie i uniesiona broń. */
public class CrowdRenderer extends MobRenderer<CrowdDummyEntity, EntityModel<CrowdDummyEntity>> {
    private final EntityModel<CrowdDummyEntity> human;
    private final EntityModel<CrowdDummyEntity> wargal;
    private final Map<CrowdDummyEntity.Skin, ResourceLocation> textures = new EnumMap<>(CrowdDummyEntity.Skin.class);

    public CrowdRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new HumanCrowd(ctx.bakeLayer(ModelLayers.PLAYER)), 0.45f);
        this.human = getModel();
        this.wargal = new WargalCrowd(ctx.bakeLayer(WargalModel.LAYER));
        addLayer(new HeldItem(this, ctx.getItemInHandRenderer()));
        for (CrowdDummyEntity.Skin s : CrowdDummyEntity.Skin.values()) textures.put(s, Zwiadowcy.id("textures/entity/" + s.texture + ".png"));
    }

    @Override
    public void render(CrowdDummyEntity e, float yaw, float pt, PoseStack pose, MultiBufferSource buf, int light) {
        model = e.skin().wargal ? wargal : human;
        super.render(e, yaw, pt, pose, buf, light);
    }

    @Override
    protected void setupRotations(CrowdDummyEntity e, PoseStack pose, float bob, float bodyYaw, float pt) {
        super.setupRotations(e, pose, bob, bodyYaw, pt);
        // kołysanie w miejscu, każdy w innym rytmie
        pose.mulPose(Axis.ZP.rotationDegrees(Mth.sin((e.tickCount + pt + e.getId() * 13) * 0.07f) * 2.5f));
    }

    @Override
    public ResourceLocation getTextureLocation(CrowdDummyEntity e) {
        return textures.get(e.skin());
    }

    private static float cheer(CrowdDummyEntity e, float age) {
        return e.cheering() ? -2.4f + Mth.sin(age * 0.25f + e.getId()) * 0.35f : Float.NaN;
    }

    static final class HumanCrowd extends PlayerModel<CrowdDummyEntity> {
        HumanCrowd(ModelPart root) {
            super(root, false);
        }

        @Override
        public void setupAnim(CrowdDummyEntity e, float limbSwing, float limbAmount, float age, float yaw, float pitch) {
            super.setupAnim(e, limbSwing, limbAmount, age, yaw, pitch);
            float c = cheer(e, age);
            if (!Float.isNaN(c)) {
                rightArm.xRot = c;
                rightSleeve.copyFrom(rightArm);
            }
        }
    }

    static final class WargalCrowd extends WargalModel<CrowdDummyEntity> {
        WargalCrowd(ModelPart root) {
            super(root);
        }

        @Override
        public void setupAnim(CrowdDummyEntity e, float limbSwing, float limbAmount, float age, float yaw, float pitch) {
            super.setupAnim(e, limbSwing, limbAmount, age, yaw, pitch);
            float c = cheer(e, age);
            if (!Float.isNaN(c)) raiseRightArm(c);
        }
    }

    /** Broń w prawej dłoni (jak warstwa przedmiotów vanilla, ale dla obu modeli). */
    static final class HeldItem extends RenderLayer<CrowdDummyEntity, EntityModel<CrowdDummyEntity>> {
        private final ItemInHandRenderer items;

        HeldItem(RenderLayerParent<CrowdDummyEntity, EntityModel<CrowdDummyEntity>> parent, ItemInHandRenderer items) {
            super(parent);
            this.items = items;
        }

        @Override
        public void render(PoseStack pose, MultiBufferSource buf, int light, CrowdDummyEntity e, float limbSwing, float limbAmount,
                           float pt, float age, float yaw, float pitch) {
            ItemStack stack = e.getMainHandItem();
            if (stack.isEmpty() || !(getParentModel() instanceof ArmedModel armed)) return;
            pose.pushPose();
            armed.translateToHand(HumanoidArm.RIGHT, pose);
            pose.mulPose(Axis.XP.rotationDegrees(-90f));
            pose.mulPose(Axis.YP.rotationDegrees(180f));
            pose.translate(1f / 16f, 0.125f, -0.625f);
            items.renderItem(e, stack, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, false, pose, buf, light);
            pose.popPose();
        }
    }
}
