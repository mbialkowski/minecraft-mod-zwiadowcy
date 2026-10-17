package pl.zwiadowcy.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import pl.zwiadowcy.Zwiadowcy;
import pl.zwiadowcy.entity.projectile.RangerArrowEntity;
import pl.zwiadowcy.entity.projectile.RangerArrowType;
import pl.zwiadowcy.entity.projectile.ThrowingKnifeEntity;
import pl.zwiadowcy.registry.ModItems;

import java.util.EnumMap;
import java.util.Map;

public final class ProjectileRenderers {

    public static class RangerArrow extends ArrowRenderer<RangerArrowEntity> {
        private static final Map<RangerArrowType, ResourceLocation> TEXTURES = new EnumMap<>(RangerArrowType.class);

        static {
            for (RangerArrowType t : RangerArrowType.values()) {
                TEXTURES.put(t, Zwiadowcy.id("textures/entity/projectiles/strzala_" + t.id + ".png"));
            }
        }

        public RangerArrow(EntityRendererProvider.Context ctx) {
            super(ctx);
        }

        @Override
        public ResourceLocation getTextureLocation(RangerArrowEntity e) {
            return TEXTURES.get(e.arrowType());
        }
    }

    /** Nóż w locie wiruje; wbity - tkwi ostrzem w celu. */
    public static class Knife extends EntityRenderer<ThrowingKnifeEntity> {
        private final ItemRenderer itemRenderer;
        private ItemStack stack;

        public Knife(EntityRendererProvider.Context ctx) {
            super(ctx);
            this.itemRenderer = ctx.getItemRenderer();
        }

        @Override
        public void render(ThrowingKnifeEntity e, float yaw, float partialTicks, PoseStack pose, MultiBufferSource buffers, int light) {
            if (stack == null) stack = new ItemStack(ModItems.NOZ_DO_RZUCANIA.get());
            pose.pushPose();
            pose.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, e.yRotO, e.getYRot()) - 90f));
            pose.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, e.xRotO, e.getXRot())));
            float spin = e.isStuck() ? -45f : -((e.tickCount + partialTicks) * 40f) % 360f;
            pose.mulPose(Axis.ZP.rotationDegrees(spin + 45f));
            pose.scale(0.75f, 0.75f, 0.75f);
            itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY, pose, buffers, e.level(), e.getId());
            pose.popPose();
            super.render(e, yaw, partialTicks, pose, buffers, light);
        }

        @Override
        @SuppressWarnings("deprecation")
        public ResourceLocation getTextureLocation(ThrowingKnifeEntity e) {
            return TextureAtlas.LOCATION_BLOCKS;
        }
    }

    /** Oszczep w locie - przedmiot obrócony wzdłuż kierunku lotu. */
    public static class Javelin extends EntityRenderer<pl.zwiadowcy.entity.projectile.JavelinEntity> {
        private final ItemRenderer itemRenderer;
        private ItemStack stack;

        public Javelin(EntityRendererProvider.Context ctx) {
            super(ctx);
            this.itemRenderer = ctx.getItemRenderer();
        }

        @Override
        public void render(pl.zwiadowcy.entity.projectile.JavelinEntity e, float yaw, float partialTicks, PoseStack pose, MultiBufferSource buffers, int light) {
            if (stack == null) stack = new ItemStack(ModItems.OSZCZEP.get());
            pose.pushPose();
            pose.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, e.yRotO, e.getYRot()) - 90f));
            pose.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, e.xRotO, e.getXRot()) - 45f));
            pose.scale(1.6f, 1.6f, 1.6f);
            itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY, pose, buffers, e.level(), e.getId());
            pose.popPose();
            super.render(e, yaw, partialTicks, pose, buffers, light);
        }

        @Override
        @SuppressWarnings("deprecation")
        public ResourceLocation getTextureLocation(pl.zwiadowcy.entity.projectile.JavelinEntity e) {
            return TextureAtlas.LOCATION_BLOCKS;
        }
    }

    private ProjectileRenderers() {}
}
