package thaumcraft.common.lib.crafting;

import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.EventHooks;
import net.minecraft.world.SimpleContainer;
import thaumcraft.api.crafting.IDustTrigger;
import thaumcraft.api.crafting.Part;
import thaumcraft.common.lib.utils.BlockUtils;

/**
 * Port of DustTriggerMultiblock: salis mundus on a multiblock built from a blueprint swaps its blocks.
 * The blueprint is [layer top to bottom][x][z] and may be found in any of the four horizontal rotations.
 * <p>
 * TODO: the original swaps each block through ServerEvents.addSwapper with a delay by priority and sparkle
 * effects; here the swap is immediate.
 */
public class DustTriggerMultiblock implements IDustTrigger {
    final Part[][][] blueprint;
    final String research;
    final int ySize;
    final int xSize;
    final int zSize;

    public DustTriggerMultiblock(String research, Part[][][] blueprint) {
        this.blueprint = blueprint;
        this.research = research;
        ySize = blueprint.length;
        xSize = blueprint[0].length;
        zSize = blueprint[0][0].length;
    }

    @Nullable
    @Override
    public Placement getValidFace(Level world, Player player, BlockPos pos, Direction face) {
        if (research != null && !ThaumcraftCapabilities.getKnowledge(player).isResearchKnown(research)) {
            return null;
        }
        for (int yy = -ySize; yy <= 0; ++yy) {
            for (int xx = -xSize; xx <= 0; ++xx) {
                for (int zz = -zSize; zz <= 0; ++zz) {
                    Direction f = fitMultiblock(world, pos.offset(xx, yy, zz));
                    if (f != null) {
                        return new Placement(xx, yy, zz, f);
                    }
                }
            }
        }
        return null;
    }

    /** EnumFacing.HORIZONTALS order: south, west, north, east. */
    private static Direction horizontal(int i) {
        return Direction.from2DDataValue(i);
    }

    private static boolean matches(Object source, BlockState state) {
        if (source instanceof Block block) return state.is(block);
        if (source instanceof BlockState bs) return state == bs;
        // Material.LAVA in the original: any lava, still or flowing.
        if (source instanceof Fluid fluid) return state.getFluidState().getType().isSame(fluid);
        if (source instanceof TagKey<?> tag) {
            @SuppressWarnings("unchecked")
            TagKey<Block> blockTag = (TagKey<Block>) tag;
            return state.is(blockTag);
        }
        return true;
    }

    @Nullable
    private Direction fitMultiblock(Level world, BlockPos pos) {
        faces:
        for (int i = 0; i < 4; ++i) {
            Direction face = horizontal(i);
            for (int y = 0; y < ySize; ++y) {
                Matrix matrix = new Matrix(blueprint[y]);
                matrix.Rotate90DegRight(3 - face.get2DDataValue());
                for (int x = 0; x < matrix.rows; ++x) {
                    for (int z = 0; z < matrix.cols; ++z) {
                        Part part = matrix.matrix[x][z];
                        if (part != null && !matches(part.getSource(), world.getBlockState(pos.offset(x, -y + (ySize - 1), z)))) {
                            continue faces;
                        }
                    }
                }
            }
            return face;
        }
        return null;
    }

    @Override
    public List<BlockPos> sparkle(Level world, Player player, BlockPos pos, Placement placement) {
        BlockPos p2 = pos.offset(placement.xOffset(), placement.yOffset(), placement.zOffset());
        List<BlockPos> list = new ArrayList<>();
        for (int y = 0; y < ySize; ++y) {
            Matrix matrix = new Matrix(blueprint[y]);
            matrix.Rotate90DegRight(3 - placement.facing().get2DDataValue());
            for (int x = 0; x < matrix.rows; ++x) {
                for (int z = 0; z < matrix.cols; ++z) {
                    if (matrix.matrix[x][z] != null) {
                        BlockPos p3 = p2.offset(x, -y + (ySize - 1), z);
                        if (matrix.matrix[x][z].getSource() != null && BlockUtils.isBlockExposed(world, p3)) {
                            list.add(p3);
                        }
                    }
                }
            }
        }
        return list;
    }

    @Override
    public void execute(Level world, Player player, BlockPos pos, Placement placement, Direction side) {
        if (world.isClientSide) {
            return;
        }
        // The original fires the crafting event with an infernal furnace stack whatever the multiblock is.
        EventHooks.firePlayerCraftingEvent(player, ItemStack.EMPTY, new SimpleContainer(1));
        BlockPos p2 = pos.offset(placement.xOffset(), placement.yOffset(), placement.zOffset());
        for (int y = 0; y < ySize; ++y) {
            Matrix matrix = new Matrix(blueprint[y]);
            matrix.Rotate90DegRight(3 - placement.facing().get2DDataValue());
            for (int x = 0; x < matrix.rows; ++x) {
                for (int z = 0; z < matrix.cols; ++z) {
                    Part part = matrix.matrix[x][z];
                    if (part == null || part.getTarget() == null) continue;
                    BlockState target = null;
                    if (part.getTarget() instanceof Block block) {
                        target = block.defaultBlockState();
                        if (target.hasProperty(HorizontalDirectionalBlock.FACING)) {
                            Direction side2 = side;
                            if (side2.getAxis().isVertical()) {
                                side2 = player.getDirection().getOpposite();
                            }
                            target = target.setValue(HorizontalDirectionalBlock.FACING, part.getApplyPlayerFacing() ? side2
                                    : part.isOpp() ? placement.facing().getOpposite() : placement.facing());
                        }
                    } else if (part.getTarget() instanceof BlockState bs) {
                        target = bs;
                    } else if (part.getTarget() instanceof ItemStack stack && stack.getItem() instanceof BlockItem bi) {
                        target = bi.getBlock().defaultBlockState();
                    } else if ("AIR".equals(part.getTarget())) {
                        target = Blocks.AIR.defaultBlockState();
                    }
                    if (target != null) {
                        world.setBlockAndUpdate(p2.offset(x, -y + (ySize - 1), z), target);
                    }
                }
            }
        }
    }
}
