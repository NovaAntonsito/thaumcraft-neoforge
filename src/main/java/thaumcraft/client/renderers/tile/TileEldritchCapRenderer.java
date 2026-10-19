package thaumcraft.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import thaumcraft.Thaumcraft;
import thaumcraft.client.lib.obj.ObjModel;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.tiles.eldritch.TileEldritchAltar;

/**
 * Port of TC4's TileEldritchCapRenderer: the capstone model, with the altar texture on the keystone and the darker
 * texture in the Outer Lands. On the keystone it also draws the Eldritch Eyes placed in it, one on each side.
 */
public class TileEldritchCapRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {
    static final ResourceLocation CAP = Thaumcraft.id("models/obj/obelisk_cap.obj");
    static final ResourceLocation OUTER = Thaumcraft.id("outer_lands");
    private final ResourceLocation tex;
    private final ResourceLocation tex2 = Thaumcraft.id("textures/models/obelisk_cap_2.png");
    private final ItemRenderer itemRenderer;
    private ItemStack eye = ItemStack.EMPTY;

    public TileEldritchCapRenderer(BlockEntityRendererProvider.Context context, String texture) {
        this.tex = Thaumcraft.id(texture);
        this.itemRenderer = context.getItemRenderer();
    }

    public static <T extends BlockEntity> BlockEntityRendererProvider<T> altar() {
        return c -> new TileEldritchCapRenderer<>(c, "textures/models/obelisk_cap_altar.png");
    }

    public static <T extends BlockEntity> BlockEntityRendererProvider<T> cap() {
        return c -> new TileEldritchCapRenderer<>(c, "textures/models/obelisk_cap.png");
    }

    @Override
    public void render(T te, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        ResourceLocation t = tex;
        if (te.getLevel() != null && te.getLevel().dimension().location().equals(OUTER)) {
            t = tex2;
        }
        pose.pushPose();
        pose.translate(0.5, 0.0, 0.5);
        pose.mulPose(Axis.XN.rotationDegrees(90.0f));
        ObjModel.load(CAP, true).renderPart("Cap", pose, buffers.getBuffer(RenderType.entityTranslucent(t)), light, overlay, 0xFFFFFFFF);
        pose.popPose();

        if (te.getLevel() != null && te instanceof TileEldritchAltar altar && altar.getEyes() > 0) {
            if (eye.isEmpty()) {
                eye = new ItemStack(ConfigItems.eldritchEye.get());
            }
            pose.pushPose();
            pose.translate(0.5, 0.0, 0.5);
            for (int a = 0; a < altar.getEyes(); ++a) {
                pose.pushPose();
                pose.mulPose(Axis.YP.rotationDegrees(a * 90));
                pose.translate(0.46f, 0.2f, 0.0f);
                pose.mulPose(Axis.YP.rotationDegrees(90.0f));
                pose.mulPose(Axis.XN.rotationDegrees(18.0f));
                // RenderItem.renderInFrame: the flat item, about half a block wide.
                pose.scale(0.5f, 0.5f, 0.5f);
                itemRenderer.renderStatic(eye, ItemDisplayContext.FIXED, light, overlay, pose, buffers, te.getLevel(), a);
                pose.popPose();
            }
            pose.popPose();
        }
    }

    @Override
    public AABB getRenderBoundingBox(T te) {
        return new AABB(te.getBlockPos());
    }

    @Override
    public int getViewDistance() {
        return 96;
    }
}
