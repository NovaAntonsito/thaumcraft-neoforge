package thaumcraft.client.renderers.item;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import thaumcraft.Thaumcraft;
import thaumcraft.common.items.casters.ItemFocus;
import thaumcraft.common.items.casters.wands.ItemWand;
import thaumcraft.common.items.casters.wands.WandParts;

/**
 * Port of Thaumcraft 4's ModelWand and ItemWandRenderer: the rod, a cap at each end and, with a focus mounted, a
 * glowing cube of its colour on the top cap. The textures are Thaumcraft 4's (textures/models/wand_*.png). Blaze
 * rods glow. Not in Thaumcraft 6.
 */
public class WandRenderer extends BlockEntityWithoutLevelRenderer {
    private static final net.minecraft.resources.ResourceLocation FOCUS = Thaumcraft.id("textures/models/wand.png");
    /** The rod and cap textures by id, so a frame does not build them anew. */
    private static final java.util.Map<String, net.minecraft.resources.ResourceLocation> TEXTURES = new java.util.concurrent.ConcurrentHashMap<>();
    private final ModelPart cap;
    private final ModelPart capBottom;
    private final ModelPart rod;
    private final ModelPart focus;

    public WandRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
        MeshDefinition mesh = new MeshDefinition();
        var root = mesh.getRoot();
        root.addOrReplaceChild("cap", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-1, -1, -1, 2, 2, 2), PartPose.ZERO);
        root.addOrReplaceChild("cap_bottom", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-1, -1, -1, 2, 2, 2), PartPose.offset(0, 20, 0));
        root.addOrReplaceChild("rod", CubeListBuilder.create().texOffs(0, 8).mirror().addBox(-1, -1, -1, 2, 18, 2), PartPose.offset(0, 2, 0));
        root.addOrReplaceChild("focus", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-3, -6, -3, 6, 6, 6), PartPose.ZERO);
        ModelPart baked = LayerDefinition.create(mesh, 32, 32).bakeRoot();
        cap = baked.getChild("cap");
        capBottom = baked.getChild("cap_bottom");
        rod = baked.getChild("rod");
        focus = baked.getChild("focus");
    }

    private static net.minecraft.resources.ResourceLocation texture(String name) {
        return TEXTURES.computeIfAbsent(name, n -> Thaumcraft.id("textures/models/" + n + ".png"));
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        if (!(stack.getItem() instanceof ItemWand wand)) return;
        WandParts parts = ItemWand.getParts(stack);
        pose.pushPose();
        // The model runs from the top cap (y = -1) down to the bottom cap (y = 21), in pixels, y down.
        pose.translate(0.5, 0.5, 0.5);
        pose.scale(1.0f, -1.0f, -1.0f);
        pose.translate(0.0, -10.0 / 16.0, 0.0);

        var rodTex = texture("wand_rod_" + parts.getRod().id());
        int rodLight = parts.getRod().glowing() ? LightTexture.FULL_BRIGHT : light;
        rod.render(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(rodTex)), rodLight, overlay);

        var capTex = texture("wand_cap_" + parts.getCap().id());
        var capVc = buffers.getBuffer(RenderType.entityCutoutNoCull(capTex));
        pose.pushPose();
        pose.scale(1.2f, 1.0f, 1.2f);
        cap.render(pose, capVc, light, overlay);
        capBottom.render(pose, capVc, light, overlay);
        pose.popPose();

        ItemStack focusStack = wand.getFocusStack(stack);
        if (!focusStack.isEmpty() && focusStack.getItem() instanceof ItemFocus f) {
            int c = f.getFocusColor(focusStack);
            float t = Minecraft.getInstance().player != null ? Minecraft.getInstance().player.tickCount : 0;
            // 195 + sin(ticks / 3) * 10 + 10 block light: a slow pulse near full brightness.
            int bright = Mth.clamp((int) (195 + Mth.sin(t / 3.0f) * 10 + 10) / 16, 0, 15);
            pose.pushPose();
            pose.scale(0.5f, 0.5f, 0.5f);
            focus.render(pose, buffers.getBuffer(RenderType.entityTranslucent(FOCUS)), LightTexture.pack(bright, bright), OverlayTexture.NO_OVERLAY,
                    FastColor.ARGB32.color(242, (c >> 16) & 0xFF, (c >> 8) & 0xFF, c & 0xFF));
            pose.popPose();
        }
        pose.popPose();
    }
}
