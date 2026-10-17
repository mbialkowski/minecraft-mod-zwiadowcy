package pl.zwiadowcy.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import pl.zwiadowcy.Zwiadowcy;
import pl.zwiadowcy.entity.NpcDefs;
import pl.zwiadowcy.entity.StoryNpc;

import java.util.HashMap;
import java.util.Map;

/** NPC renderowani modelem gracza (szeroki lub smukły) ze skórką assets/zwiadowcy/textures/entity/npc/&lt;id&gt;.png. */
public class StoryNpcRenderer extends HumanoidMobRenderer<StoryNpc, PlayerModel<StoryNpc>> {
    private final PlayerModel<StoryNpc> wide;
    private final PlayerModel<StoryNpc> slim;
    private final Map<String, ResourceLocation> textures = new HashMap<>();

    public StoryNpcRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new PlayerModel<>(ctx.bakeLayer(ModelLayers.PLAYER), false), 0.5f);
        this.wide = getModel();
        this.slim = new PlayerModel<>(ctx.bakeLayer(ModelLayers.PLAYER_SLIM), true);
        addLayer(new HumanoidArmorLayer<>(this,
                new HumanoidArmorModel<>(ctx.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidArmorModel<>(ctx.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                ctx.getModelManager()));
    }

    @Override
    public void render(StoryNpc npc, float yaw, float partialTicks, PoseStack pose, MultiBufferSource buffers, int light) {
        NpcDefs.NpcDef def = npc.def();
        this.model = def != null && def.slim() ? slim : wide;
        super.render(npc, yaw, partialTicks, pose, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(StoryNpc npc) {
        return textures.computeIfAbsent(npc.npcId(), id -> Zwiadowcy.id("textures/entity/npc/" + id + ".png"));
    }
}
