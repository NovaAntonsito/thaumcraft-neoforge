package thaumcraft.client.renderers.entity.mob;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.common.lib.utils.EntityUtils;

/**
 * Port of LayerTainted: taint fibres drawn over the model of a mob with the "tainted" champion modifier, at 66%
 * alpha. In 1.12 ChampionModTainted.preRender added the layer to the renderer of each tainted mob; now every living
 * renderer gets it and it only draws on tainted mobs.
 */
public class LayerTainted<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private static final ResourceLocation TAINT_TEXTURE = ResourceLocation.fromNamespaceAndPath(Thaumcraft.MODID, "textures/models/taint_fibres.png");

    public LayerTainted(RenderLayerParent<T, M> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity, float limbSwing, float limbSwingAmount,
            float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (EntityUtils.getChampionMod(entity) != 13 || entity.isInvisible()) {
            return;
        }
        getParentModel().renderToBuffer(poseStack, buffer.getBuffer(RenderType.entityTranslucent(TAINT_TEXTURE)), packedLight,
                OverlayTexture.NO_OVERLAY, 0xA8FFFFFF);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (EntityType<?> type : event.getEntityTypes()) {
            EntityRenderer<?> r = event.getRenderer(type);
            if (r instanceof LivingEntityRenderer living) {
                living.addLayer(new LayerTainted(living));
            }
        }
    }
}
