package pl.zwiadowcy.client;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import pl.zwiadowcy.Zwiadowcy;
import pl.zwiadowcy.client.model.BoarModel;
import pl.zwiadowcy.client.model.KalkarModel;
import pl.zwiadowcy.client.model.RangerCloakModel;
import pl.zwiadowcy.client.model.WargalModel;
import pl.zwiadowcy.client.render.MobRenderers;
import pl.zwiadowcy.client.render.ProjectileRenderers;
import pl.zwiadowcy.client.render.StoryNpcRenderer;
import pl.zwiadowcy.client.screen.QuiverScreen;
import pl.zwiadowcy.item.RangerBowItem;
import pl.zwiadowcy.registry.ModEntities;
import pl.zwiadowcy.registry.ModItems;
import pl.zwiadowcy.registry.ModMenus;

@Mod.EventBusSubscriber(modid = Zwiadowcy.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenus.KOLCZAN.get(), QuiverScreen::new);
            ItemProperties.register(ModItems.LUK_ZWIADOWCY.get(), ResourceLocation.withDefaultNamespace("pull"), (stack, level, entity, seed) -> {
                if (entity == null || entity.getUseItem() != stack) return 0f;
                return (stack.getUseDuration() - entity.getUseItemRemainingTicks()) / RangerBowItem.drawTicks(entity);
            });
            ItemProperties.register(ModItems.LUK_ZWIADOWCY.get(), ResourceLocation.withDefaultNamespace("pulling"),
                    (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1f : 0f);
        });
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.NPC.get(), StoryNpcRenderer::new);
        event.registerEntityRenderer(ModEntities.KALKAR.get(), MobRenderers.Kalkar::new);
        event.registerEntityRenderer(ModEntities.WARGAL.get(), ctx -> new MobRenderers.Wargal(ctx, "wargal", 1.0f));
        event.registerEntityRenderer(ModEntities.WARGAL_ZWIADOWCA.get(), ctx -> new MobRenderers.Wargal(ctx, "wargal_zwiadowca", 0.85f));
        event.registerEntityRenderer(ModEntities.WARGAL_OSZCZEPNIK.get(), ctx -> new MobRenderers.Wargal(ctx, "wargal_oszczepnik", 1.0f));
        event.registerEntityRenderer(ModEntities.WARGAL_HERSZT.get(), ctx -> new MobRenderers.Wargal(ctx, "wargal_herszt", 1.35f));
        event.registerEntityRenderer(ModEntities.BANDYTA.get(), MobRenderers.Bandit::new);
        event.registerEntityRenderer(ModEntities.LUCZNIK_BANDYTOW.get(), MobRenderers.Bandit::new);
        event.registerEntityRenderer(ModEntities.HERSZT_BANDYTOW.get(), MobRenderers.Bandit::new);
        event.registerEntityRenderer(ModEntities.NIEDZWIEDZ.get(), MobRenderers.Bear::new);
        event.registerEntityRenderer(ModEntities.OSZCZEP.get(), ProjectileRenderers.Javelin::new);
        event.registerEntityRenderer(ModEntities.DZIK.get(), MobRenderers.Boar::new);
        event.registerEntityRenderer(ModEntities.WYRWIJ.get(), MobRenderers.Tug::new);
        event.registerEntityRenderer(ModEntities.STRZALA.get(), ProjectileRenderers.RangerArrow::new);
        event.registerEntityRenderer(ModEntities.NOZ.get(), ProjectileRenderers.Knife::new);
        event.registerEntityRenderer(ModEntities.FLASZA.get(), ThrownItemRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(KalkarModel.LAYER, KalkarModel::createLayer);
        event.registerLayerDefinition(WargalModel.LAYER, WargalModel::createLayer);
        event.registerLayerDefinition(BoarModel.LAYER, BoarModel::createLayer);
        event.registerLayerDefinition(RangerCloakModel.LAYER, RangerCloakModel::createLayer);
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(ModKeys.BEZRUCH);
        event.register(ModKeys.STRZALA);
        event.register(ModKeys.DZIENNIK);
    }

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("zwiadowcy_hud", HudOverlay.INSTANCE);
    }

    private ClientSetup() {}
}
