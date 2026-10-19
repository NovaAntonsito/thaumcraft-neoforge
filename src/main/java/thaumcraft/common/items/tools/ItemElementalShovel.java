package thaumcraft.common.items.tools;

import java.util.ArrayList;
import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import thaumcraft.api.ThaumcraftMaterials;
import thaumcraft.api.items.IArchitect;
import thaumcraft.client.fx.ClientFX;
import thaumcraft.common.items.ISubItems;
import thaumcraft.common.lib.enchantment.EnumInfusionEnchantment;
import thaumcraft.common.lib.utils.InventoryUtils;
import thaumcraft.common.registry.TCDataComponents;

/**
 * The shovel of the earthmover: right-click a block to place a 3x3 of it against the face you clicked, taking the blocks
 * from your inventory (dirt for grass). The G key turns the 3x3 between flat, upright and sideways; sneaking shows where
 * it goes. Made with Destructive.
 * 1.21: the orientation is the thaumcraft:shovel_orientation component (the "or" byte of 1.12).
 */
public class ItemElementalShovel extends ShovelItem implements IArchitect, ISubItems {
    public ItemElementalShovel(Properties properties) {
        super(ThaumcraftMaterials.TOOLMAT_ELEMENTAL, properties.attributes(ShovelItem.createAttributes(ThaumcraftMaterials.TOOLMAT_ELEMENTAL, 1.5f, -3.0f)));
    }

    /** The offset of one of the 3x3 cells, for the orientation and the face clicked. */
    private static int[] offset(int aa, int bb, byte o, Direction side, Player player) {
        int xx = 0;
        int yy = 0;
        int zz = 0;
        if (o == 1) {
            yy = bb;
            if (side.ordinal() <= 1) {
                int l = Mth.floor(player.getYRot() * 4.0f / 360.0f + 0.5) & 0x3;
                if (l == 0 || l == 2) {
                    xx = aa;
                } else {
                    zz = aa;
                }
            } else if (side.ordinal() <= 3) {
                zz = aa;
            } else {
                xx = aa;
            }
        } else if (o == 2) {
            if (side.ordinal() <= 1) {
                int l = Mth.floor(player.getYRot() * 4.0f / 360.0f + 0.5) & 0x3;
                yy = bb;
                if (l == 0 || l == 2) {
                    xx = aa;
                } else {
                    zz = aa;
                }
            } else {
                zz = bb;
                xx = aa;
            }
        } else if (side.ordinal() <= 1) {
            xx = aa;
            zz = bb;
        } else if (side.ordinal() <= 3) {
            xx = aa;
            yy = bb;
        } else {
            zz = aa;
            yy = bb;
        }
        return new int[] {xx, yy, zz};
    }

    /** Block.canPlaceBlockAt of 1.12: the spot can be replaced and the block can stand there. */
    private static boolean canPlaceAt(BlockState bs, Level world, BlockPos p2) {
        return world.getBlockState(p2).canBeReplaced() && bs.canSurvive(world, p2);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.FAIL;
        }
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction side = context.getClickedFace();
        InteractionHand hand = context.getHand();
        BlockState bs = world.getBlockState(pos);
        if (world.getBlockEntity(pos) == null) {
            for (int aa = -1; aa <= 1; ++aa) {
                for (int bb = -1; bb <= 1; ++bb) {
                    int[] o = offset(aa, bb, getOrientation(player.getItemInHand(hand)), side, player);
                    BlockPos p2 = pos.relative(side).offset(o[0], o[1], o[2]);
                    if (canPlaceAt(bs, world, p2)) {
                        if (player.getAbilities().instabuild || InventoryUtils.consumePlayerItem(player, new ItemStack(bs.getBlock().asItem()), false)) {
                            world.playLocalSound(p2.getX(), p2.getY(), p2.getZ(), bs.getSoundType(world, p2, player).getBreakSound(), SoundSource.BLOCKS,
                                    0.6f, 0.9f + world.random.nextFloat() * 0.2f, false);
                            world.setBlock(p2, bs, 3);
                            player.getItemInHand(hand).hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
                            if (world.isClientSide) {
                                thaumcraft.client.fx.FXDispatcher.drawBamf(p2, 8401408, false, false, side);
                            }
                            player.swing(hand);
                        } else if (bs.is(Blocks.GRASS_BLOCK)
                                && (player.getAbilities().instabuild || InventoryUtils.consumePlayerItem(player, new ItemStack(Items.DIRT), false))) {
                            world.playLocalSound(p2.getX(), p2.getY(), p2.getZ(), bs.getSoundType(world, p2, player).getBreakSound(), SoundSource.BLOCKS,
                                    0.6f, 0.9f + world.random.nextFloat() * 0.2f, false);
                            world.setBlock(p2, Blocks.DIRT.defaultBlockState(), 3);
                            player.getItemInHand(hand).hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
                            if (world.isClientSide) {
                                thaumcraft.client.fx.FXDispatcher.drawBamf(p2, 8401408, false, false, side);
                            }
                            player.swing(hand);
                            if (player.getItemInHand(hand).isEmpty()) {
                                break;
                            }
                        }
                    }
                }
            }
        }
        return InteractionResult.FAIL;
    }

    @Override
    public void fillSubItems(Consumer<ItemStack> output) {
        ItemStack w1 = new ItemStack(this);
        EnumInfusionEnchantment.addInfusionEnchantment(w1, EnumInfusionEnchantment.DESTRUCTIVE, 1);
        output.accept(w1);
    }

    @Override
    public ArrayList<BlockPos> getArchitectBlocks(ItemStack focusstack, Level world, BlockPos pos, Direction side, Player player) {
        ArrayList<BlockPos> b = new ArrayList<>();
        if (!player.isShiftKeyDown()) {
            return b;
        }
        BlockState bs = world.getBlockState(pos);
        for (int aa = -1; aa <= 1; ++aa) {
            for (int bb = -1; bb <= 1; ++bb) {
                int[] o = offset(aa, bb, getOrientation(focusstack), side, player);
                BlockPos p2 = pos.relative(side).offset(o[0], o[1], o[2]);
                if (canPlaceAt(bs, world, p2)) {
                    b.add(p2);
                }
            }
        }
        return b;
    }

    @Override
    public boolean showAxis(ItemStack stack, Level world, Player player, Direction side, EnumAxis axis) {
        return false;
    }

    public static byte getOrientation(ItemStack stack) {
        return stack.getOrDefault(TCDataComponents.SHOVEL_ORIENTATION.get(), (byte) 0);
    }

    public static void setOrientation(ItemStack stack, byte o) {
        stack.set(TCDataComponents.SHOVEL_ORIENTATION.get(), (byte) (o % 3));
    }

    @Override
    public HitResult getArchitectMOP(ItemStack stack, Level world, LivingEntity player) {
        return player instanceof Player p ? getPlayerPOVHitResult(world, p, ClipContext.Fluid.NONE) : null;
    }

    @Override
    public boolean useBlockHighlight(ItemStack stack) {
        return true;
    }
}
