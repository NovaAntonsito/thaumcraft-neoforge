package thaumcraft.common.items.casters.wands;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.nodes.NodeModifier;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.tiles.misc.TileAuraNode;
import thaumcraft.common.tiles.misc.TileJarNode;

/**
 * The node jar part of the Thaumcraft 4 WandManager. A 3x3x3 cube of glass with wooden slabs on top and a node in
 * the middle becomes a node jar when struck with a wand holding 70 vis (Thaumcraft 4 asked 70 of each primal).
 * Catching the node is rough on it: three times in four it loses a step of brightness.
 */
public final class NodeJarHelper {
    public static final float VIS_COST = 70.0f;
    /** [layer from the top][x][z]: 1 wooden slab, 2 glass, 3 the node. */
    private static final int[][][] BLUEPRINT = {
            {{1, 1, 1}, {1, 1, 1}, {1, 1, 1}},
            {{2, 2, 2}, {2, 2, 2}, {2, 2, 2}},
            {{2, 2, 2}, {2, 3, 2}, {2, 2, 2}},
            {{2, 2, 2}, {2, 2, 2}, {2, 2, 2}}};

    private NodeJarHelper() {
    }

    /** Whether a click on this block could be part of a node jar. */
    public static boolean isJarPart(BlockState bs) {
        return bs.is(Blocks.GLASS) || bs.is(BlockTags.WOODEN_SLABS);
    }

    public static boolean createNodeJar(ItemStack wand, Player player, Level world, BlockPos pos) {
        for (int xx = pos.getX() - 2; xx <= pos.getX(); ++xx) {
            for (int yy = pos.getY() - 3; yy <= pos.getY(); ++yy) {
                for (int zz = pos.getZ() - 2; zz <= pos.getZ(); ++zz) {
                    if (fitNodeJar(world, xx, yy, zz)) {
                        if (!(wand.getItem() instanceof ItemWand w) || !w.consumeVis(wand, player, VIS_COST, true, world.isClientSide)) {
                            return false;
                        }
                        if (!world.isClientSide) {
                            replaceNodeJar(world, xx, yy, zz);
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean fitNodeJar(Level world, int x, int y, int z) {
        for (int yy = 0; yy < 4; ++yy) {
            for (int xx = 0; xx < 3; ++xx) {
                for (int zz = 0; zz < 3; ++zz) {
                    BlockPos p = new BlockPos(x + xx, y - yy + 2, z + zz);
                    BlockState block = world.getBlockState(p);
                    int part = BLUEPRINT[yy][xx][zz];
                    if (part == 1 && !block.is(BlockTags.WOODEN_SLABS)) return false;
                    if (part == 2 && !block.is(Blocks.GLASS)) return false;
                    if (part == 3 && !(world.getBlockEntity(p) instanceof TileAuraNode node && !node.isEnergized())) return false;
                }
            }
        }
        return true;
    }

    private static void replaceNodeJar(Level world, int x, int y, int z) {
        for (int yy = 0; yy < 4; ++yy) {
            for (int xx = 0; xx < 3; ++xx) {
                for (int zz = 0; zz < 3; ++zz) {
                    BlockPos p = new BlockPos(x + xx, y - yy + 2, z + zz);
                    if (BLUEPRINT[yy][xx][zz] == 3) {
                        TileAuraNode node = (TileAuraNode) world.getBlockEntity(p);
                        AspectList na = node.getAspects().copy();
                        NodeModifier nm = node.getNodeModifier();
                        if (world.random.nextFloat() < 0.75f) {
                            if (nm == null) nm = NodeModifier.PALE;
                            else if (nm == NodeModifier.BRIGHT) nm = null;
                            else if (nm == NodeModifier.PALE) nm = NodeModifier.FADING;
                        }
                        String nid = node.getId();
                        var type = node.getNodeType();
                        world.setBlock(p, ConfigBlocks.jarNode.get().defaultBlockState(), Block.UPDATE_ALL);
                        if (world.getBlockEntity(p) instanceof TileJarNode jar) {
                            jar.setAspects(na);
                            jar.setNodeModifier(nm);
                            jar.setNodeType(type);
                            jar.setId(nid);
                            jar.setChanged();
                            jar.syncTile(false);
                            world.blockEvent(p, ConfigBlocks.jarNode.get(), 9, 0);
                        }
                    } else {
                        world.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
            }
        }
        world.playSound(null, x + 0.5, y + 0.5, z + 0.5, SoundsTC.WAND.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    /** TileJarNode.onWandRightClick: breaks the jar open and the node is back in the world, as it was caught. */
    public static void releaseNode(Level world, BlockPos pos, Player player) {
        if (!world.isClientSide && world.getBlockEntity(pos) instanceof TileJarNode jar) {
            AspectList al = jar.getAspects().copy();
            var type = jar.getNodeType();
            var mod = jar.getNodeModifier();
            world.levelEvent(2001, pos, Block.getId(world.getBlockState(pos)));
            world.setBlock(pos, ConfigBlocks.auraNode.get().defaultBlockState(), Block.UPDATE_ALL);
            if (world.getBlockEntity(pos) instanceof TileAuraNode tn) {
                tn.setAspects(al);
                tn.setNodeModifier(mod);
                tn.setNodeType(type);
                tn.setChanged();
                tn.syncTile(false);
            }
        }
        player.swing(player.getUsedItemHand());
    }
}
