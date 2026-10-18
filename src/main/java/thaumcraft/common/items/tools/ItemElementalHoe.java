package thaumcraft.common.items.tools;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import thaumcraft.api.ThaumcraftMaterials;
import thaumcraft.client.fx.ClientFX;

/**
 * The hoe of growth: tills 3x3 at once (just one block while sneaking). Where nothing can be tilled it works as bone
 * meal, for 3 durability.
 * As a 1.12 hoe it adds no damage and its attack speed is the material damage minus 3.
 */
public class ItemElementalHoe extends HoeItem {
    public ItemElementalHoe(Properties properties) {
        super(ThaumcraftMaterials.TOOLMAT_ELEMENTAL, properties.attributes(HoeItem.createAttributes(ThaumcraftMaterials.TOOLMAT_ELEMENTAL, -3.0f, 0.0f)));
    }

    @Override
    public int getEnchantmentValue() {
        return 5;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (player != null && player.isShiftKeyDown()) {
            return super.useOn(context);
        }
        boolean did = false;
        for (int xx = -1; xx <= 1; ++xx) {
            for (int zz = -1; zz <= 1; ++zz) {
                BlockPos pp = pos.offset(xx, 0, zz);
                UseOnContext ctx = new UseOnContext(world, player, context.getHand(), context.getItemInHand(),
                        new BlockHitResult(Vec3.atCenterOf(pp), context.getClickedFace(), pp, false));
                if (super.useOn(ctx).consumesAction()) {
                    if (world.isClientSide) {
                        ClientFX.drawBamf(world, pp.getX() + 0.5, pp.getY() + 1.01, pp.getZ() + 0.5, 0x4C1F19, xx == 0 && zz == 0, false);
                    }
                    did = true;
                }
            }
        }
        if (!did) {
            did = BoneMealItem.applyBonemeal(new ItemStack(Items.BONE_MEAL), world, pos, player);
            if (did) {
                if (player != null) {
                    context.getItemInHand().hurtAndBreak(3, player, LivingEntity.getSlotForHand(context.getHand()));
                }
                if (!world.isClientSide) {
                    world.levelEvent(1505, pos, 15);
                } else {
                    ClientFX.blockMist(pos, 4259648);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }
}
