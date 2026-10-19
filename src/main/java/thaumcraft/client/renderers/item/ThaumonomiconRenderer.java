package thaumcraft.client.renderers.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import thaumcraft.Thaumcraft;
import thaumcraft.client.lib.ThaumonomiconAnimation;
import thaumcraft.common.items.curios.ItemThaumonomicon;

/**
 * The Thaumonomicon in the hand as a 3D book: the enchanting table's book model with the Thaumonomicon's cover.
 * Closed it shows its cover; as it opens in first person it turns its pages to the player, rises a little and its
 * pages flutter. The inventory, the ground and item frames keep the flat icon (the item model's perspectives).
 */
public class ThaumonomiconRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation BOOK = Thaumcraft.id("textures/models/thaumonomicon_book.png");
    private static final ResourceLocation BOOK_CHEAT = Thaumcraft.id("textures/models/thaumonomicon_cheat_book.png");
    private BookModel model;

    public ThaumonomiconRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        model = new BookModel(BookModel.createBodyLayer().bakeRoot());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext ctx, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        if (model == null) {
            onResourceManagerReload(null);
        }
        Minecraft mc = Minecraft.getInstance();
        float partial = mc.getTimer().getGameTimeDeltaPartialTick(false);
        float open = ctx.firstPerson() ? ThaumonomiconAnimation.open(stack, partial) : 0.0f;
        float ease = open * open * (3.0f - 2.0f * open);
        float time = (mc.player != null ? mc.player.tickCount : 0) + partial;

        pose.pushPose();
        pose.translate(0.5, 0.5, 0.5);
        if (ctx.firstPerson()) {
            float side = ctx == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND ? -1.0f : 1.0f;
            pose.translate(side * 0.2f * ease, 0.15f * ease, 0.0f);
        }
        // Closed, the cover faces out; open, the pages face the player.
        pose.mulPose(Axis.YP.rotationDegrees(-90.0f * ease));
        pose.scale(1.4f, 1.4f, 1.4f);

        float flip = time * 0.05f * open;
        float flip1 = Mth.clamp(Mth.frac(flip + 0.25f) * 1.6f - 0.3f, 0.0f, 1.0f);
        float flip2 = Mth.clamp(Mth.frac(flip + 0.75f) * 1.6f - 0.3f, 0.0f, 1.0f);
        model.setupAnim(time, flip1, flip2, ease);
        boolean cheat = stack.getItem() instanceof ItemThaumonomicon book && book.isCheat();
        model.renderToBuffer(pose, buffers.getBuffer(model.renderType(cheat ? BOOK_CHEAT : BOOK)), light, overlay, -1);
        pose.popPose();
    }
}
