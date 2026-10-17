package pl.zwiadowcy.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.AbstractHorseRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import pl.zwiadowcy.Zwiadowcy;
import pl.zwiadowcy.client.model.BoarModel;
import pl.zwiadowcy.client.model.KalkarModel;
import pl.zwiadowcy.client.model.WargalModel;
import pl.zwiadowcy.entity.BoarEntity;
import pl.zwiadowcy.entity.KalkarEntity;
import pl.zwiadowcy.entity.TugEntity;
import pl.zwiadowcy.entity.WargalEntity;

/** Renderery mobów moda. */
public final class MobRenderers {

    public static class Kalkar extends MobRenderer<KalkarEntity, KalkarModel> {
        private static final ResourceLocation TEXTURE = Zwiadowcy.id("textures/entity/kalkar.png");
        private static final RenderType EYES = RenderType.eyes(Zwiadowcy.id("textures/entity/kalkar_oczy.png"));

        public Kalkar(EntityRendererProvider.Context ctx) {
            super(ctx, new KalkarModel(ctx.bakeLayer(KalkarModel.LAYER)), 1.0f);
            addLayer(new EyesLayer<>(this) {
                @Override
                public RenderType renderType() {
                    return EYES;
                }
            });
        }

        @Override
        public ResourceLocation getTextureLocation(KalkarEntity e) {
            return TEXTURE;
        }
    }

    /** Wszystkie odmiany Wargalów: wspólny model, własna tekstura i rozmiar. */
    public static class Wargal extends MobRenderer<WargalEntity, WargalModel> {
        private static final RenderType EYES = RenderType.eyes(Zwiadowcy.id("textures/entity/wargal_oczy.png"));
        private final ResourceLocation texture;
        private final float scale;

        public Wargal(EntityRendererProvider.Context ctx, String texture, float scale) {
            super(ctx, new WargalModel(ctx.bakeLayer(WargalModel.LAYER)), 0.6f * scale);
            this.texture = Zwiadowcy.id("textures/entity/" + texture + ".png");
            this.scale = scale;
            addLayer(new EyesLayer<>(this) {
                @Override
                public RenderType renderType() {
                    return EYES;
                }
            });
            addLayer(new net.minecraft.client.renderer.entity.layers.ItemInHandLayer<>(this, ctx.getItemInHandRenderer()));
        }

        @Override
        protected void scale(WargalEntity e, PoseStack pose, float partialTicks) {
            pose.scale(scale, scale, scale);
        }

        @Override
        public ResourceLocation getTextureLocation(WargalEntity e) {
            return texture;
        }
    }

    /** Bandyci: model gracza, skórka zależna od odmiany i losowego numeru. */
    public static class Bandit extends net.minecraft.client.renderer.entity.HumanoidMobRenderer<pl.zwiadowcy.entity.BanditEntity, pl.zwiadowcy.client.model.BanditModel> {
        private static final ResourceLocation[] SKINS = new ResourceLocation[pl.zwiadowcy.entity.BanditEntity.SKINS];
        private static final ResourceLocation CHIEF = Zwiadowcy.id("textures/entity/bandyci/herszt_bandytow.png");

        static {
            for (int i = 0; i < SKINS.length; i++) SKINS[i] = Zwiadowcy.id("textures/entity/bandyci/bandyta_" + i + ".png");
        }

        public Bandit(EntityRendererProvider.Context ctx) {
            super(ctx, new pl.zwiadowcy.client.model.BanditModel(ctx.bakeLayer(ModelLayers.PLAYER)), 0.5f);
            addLayer(new net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer<>(this,
                    new net.minecraft.client.model.HumanoidArmorModel<>(ctx.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                    new net.minecraft.client.model.HumanoidArmorModel<>(ctx.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                    ctx.getModelManager()));
        }

        @Override
        public ResourceLocation getTextureLocation(pl.zwiadowcy.entity.BanditEntity e) {
            if (e.variant() == pl.zwiadowcy.entity.BanditEntity.Variant.HERSZT) return CHIEF;
            return SKINS[Math.floorMod(e.skin(), SKINS.length)];
        }
    }

    /** Niedźwiedź brunatny - model niedźwiedzia polarnego z własną teksturą. */
    public static class Bear extends MobRenderer<pl.zwiadowcy.entity.BearEntity, net.minecraft.client.model.PolarBearModel<pl.zwiadowcy.entity.BearEntity>> {
        private static final ResourceLocation TEXTURE = Zwiadowcy.id("textures/entity/niedzwiedz.png");

        public Bear(EntityRendererProvider.Context ctx) {
            super(ctx, new net.minecraft.client.model.PolarBearModel<>(ctx.bakeLayer(ModelLayers.POLAR_BEAR)), 0.9f);
        }

        @Override
        protected void scale(pl.zwiadowcy.entity.BearEntity e, PoseStack pose, float partialTicks) {
            pose.scale(1.1f, 1.1f, 1.1f);
        }

        @Override
        public ResourceLocation getTextureLocation(pl.zwiadowcy.entity.BearEntity e) {
            return TEXTURE;
        }
    }

    public static class Boar extends MobRenderer<BoarEntity, BoarModel> {
        private static final ResourceLocation TEXTURE = Zwiadowcy.id("textures/entity/dzik.png");

        public Boar(EntityRendererProvider.Context ctx) {
            super(ctx, new BoarModel(ctx.bakeLayer(BoarModel.LAYER)), 0.7f);
        }

        @Override
        public ResourceLocation getTextureLocation(BoarEntity e) {
            return TEXTURE;
        }
    }

    public static class Tug extends AbstractHorseRenderer<TugEntity, HorseModel<TugEntity>> {
        private static final ResourceLocation TEXTURE = Zwiadowcy.id("textures/entity/wyrwij.png");

        public Tug(EntityRendererProvider.Context ctx) {
            super(ctx, new HorseModel<>(ctx.bakeLayer(ModelLayers.HORSE)), 0.9f);
        }

        @Override
        protected void scale(TugEntity e, PoseStack pose, float partialTicks) {
            pose.scale(0.9f, 0.9f, 0.9f);
        }

        @Override
        public ResourceLocation getTextureLocation(TugEntity e) {
            return TEXTURE;
        }
    }

    private MobRenderers() {}
}
