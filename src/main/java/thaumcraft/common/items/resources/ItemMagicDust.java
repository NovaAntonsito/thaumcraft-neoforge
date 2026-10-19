package thaumcraft.common.items.resources;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import thaumcraft.api.crafting.IDustTrigger;
import thaumcraft.common.lib.SoundsTC;

/** Port of ItemMagicDust (salis mundus). */
public class ItemMagicDust extends Item {
    public ItemMagicDust(Properties properties) {
        super(properties.rarity(Rarity.UNCOMMON));
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (player == null || !player.mayUseItemAt(pos, context.getClickedFace(), stack)) {
            return InteractionResult.FAIL;
        }
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        for (IDustTrigger trigger : IDustTrigger.triggers) {
            IDustTrigger.Placement place = trigger.getValidFace(world, player, pos, context.getClickedFace());
            if (place != null) {
                player.swing(context.getHand());
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                trigger.execute(world, player, pos, place, context.getClickedFace());
                if (world.isClientSide) {
                    doSparkles(player, world, pos, context.getClickLocation(), context.getHand(), trigger, place);
                }
                return InteractionResult.sidedSuccess(world.isClientSide);
            }
        }
        return InteractionResult.PASS;
    }

    /** Golden sparkles thrown from the hand at the block, then over each block the trigger changes. */
    private static void doSparkles(Player player, Level world, BlockPos pos, net.minecraft.world.phys.Vec3 hit, net.minecraft.world.InteractionHand hand,
            IDustTrigger trigger, IDustTrigger.Placement place) {
        net.minecraft.world.phys.Vec3 v1 = thaumcraft.common.lib.utils.EntityUtils.posToHand(player, hand);
        net.minecraft.world.phys.Vec3 v2 = net.minecraft.world.phys.Vec3.atCenterOf(pos).subtract(v1);
        for (int cnt = 50, a = 0; a < cnt; ++a) {
            boolean floaty = a < cnt / 3;
            float r = net.minecraft.util.Mth.nextInt(world.random, 255, 255) / 255.0f;
            float g = net.minecraft.util.Mth.nextInt(world.random, 189, 255) / 255.0f;
            float b = net.minecraft.util.Mth.nextInt(world.random, 64, 255) / 255.0f;
            thaumcraft.client.fx.FXDispatcher.drawSimpleSparkle(world.random, v1.x, v1.y, v1.z, v2.x / 6.0 + world.random.nextGaussian() * 0.05,
                    v2.y / 6.0 + world.random.nextGaussian() * 0.05 + (floaty ? 0.05 : 0.15), v2.z / 6.0 + world.random.nextGaussian() * 0.05, 0.5f, r, g, b,
                    world.random.nextInt(5), floaty ? 0.3f + world.random.nextFloat() * 0.5f : 0.85f, floaty ? 0.2f : 0.5f, 16);
        }
        world.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), SoundsTC.DUST.get(), SoundSource.PLAYERS, 0.33f, 1.0f + (float) world.random.nextGaussian() * 0.05f,
                false);
        java.util.List<BlockPos> sparkles = trigger.sparkle(world, player, pos, place);
        if (sparkles != null) {
            for (BlockPos p : sparkles) {
                thaumcraft.client.fx.FXDispatcher.drawBlockSparkles(p, hit);
            }
        }
    }
}
