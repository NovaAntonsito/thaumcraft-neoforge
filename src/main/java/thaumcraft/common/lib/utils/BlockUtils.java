package thaumcraft.common.lib.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.CommonHooks;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Partial port of BlockUtils. */
public final class BlockUtils {
    private BlockUtils() {
    }

    /** True if any neighbour of the block is not a full opaque block. */
    public static boolean isBlockExposed(Level world, BlockPos pos) {
        for (Direction face : Direction.values()) {
            BlockPos n = pos.relative(face);
            if (!world.getBlockState(n).isSolidRender(world, n)) {
                return true;
            }
        }
        return false;
    }

    /**
     * True if nothing solid lies between the centers of the two blocks. The source block is ignored and hitting the
     * target block itself counts as a clear line (ThaumcraftApiHelper.rayTraceIgnoringSource in the original).
     */
    public static boolean hasLOS(Level world, BlockPos source, BlockPos target) {
        Vec3 from = Vec3.atCenterOf(source);
        Vec3 to = Vec3.atCenterOf(target);
        BlockHitResult hit = BlockGetter.traverseBlocks(from, to, null, (ctx, pos) -> {
            if (pos.equals(source)) return null;
            VoxelShape shape = world.getBlockState(pos).getCollisionShape(world, pos, CollisionContext.empty());
            return shape.isEmpty() ? null : shape.clip(from, to, pos);
        }, ctx -> null);
        return hit == null || hit.getBlockPos().equals(target);
    }
    public static boolean harvestBlockSkipCheck(Level world, Player player, BlockPos pos) {
        return harvestBlock(world, player, pos, false, false, 0, true);
    }

    public static boolean harvestBlock(Level world, Player player, BlockPos pos) {
        return harvestBlock(world, player, pos, false, false, 0, false);
    }

    /**
     * Breaks a block as the player would, optionally always dropping it and with silk touch or fortune added.
     * 1.21: the enchanted_placeholder item is replaced by a stick carrying the enchantments, and the experience
     * comes from getExpDrop instead of the break event.
     */
    public static boolean harvestBlock(Level world, Player p, BlockPos pos, boolean alwaysDrop, boolean silkOverride, int fortuneOverride, boolean skipEvent) {
        if (world.isClientSide || !(p instanceof ServerPlayer player) || !(world instanceof ServerLevel sl)) {
            return false;
        }
        BlockState iblockstate = world.getBlockState(pos);
        if (!skipEvent && CommonHooks.fireBlockBreak(world, player.gameMode.getGameModeForPlayer(), player, pos, iblockstate).isCanceled()) {
            return false;
        }
        BlockEntity tileentity = world.getBlockEntity(pos);
        Block block = iblockstate.getBlock();
        if (block instanceof GameMasterBlock && !player.canUseGameMasterBlocks()) {
            world.sendBlockUpdated(pos, iblockstate, iblockstate, 3);
            return false;
        }
        world.levelEvent(null, 2001, pos, Block.getId(iblockstate));
        boolean flag1;
        ItemStack fakeStack = ItemStack.EMPTY;
        if (player.isCreative()) {
            flag1 = removeBlock(world, player, pos, iblockstate, false);
            player.connection.send(new ClientboundBlockUpdatePacket(world, pos));
        } else {
            ItemStack itemstack1 = player.getMainHandItem();
            boolean flag2 = alwaysDrop || iblockstate.canHarvestBlock(world, pos, player);
            flag1 = removeBlock(world, player, pos, iblockstate, flag2);
            if (flag1 && flag2) {
                fakeStack = itemstack1.copy();
                Holder<Enchantment> fortune = world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE);
                Holder<Enchantment> silk = world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH);
                if (silkOverride || fortuneOverride > EnchantmentHelper.getEnchantmentLevel(fortune, player)) {
                    if (alwaysDrop || fakeStack.isEmpty()) {
                        fakeStack = new ItemStack(Items.STICK);
                        fakeStack.set(net.minecraft.core.component.DataComponents.ENCHANTMENTS, itemstack1.getTagEnchantments());
                    }
                    int fort = Math.max(fortuneOverride, EnchantmentHelper.getItemEnchantmentLevel(fortune, itemstack1));
                    EnchantmentHelper.updateEnchantments(fakeStack, m -> {
                        if (silkOverride) {
                            m.set(silk, 1);
                        }
                        if (fort > 0) {
                            m.set(fortune, fort);
                        }
                    });
                }
                block.playerDestroy(world, player, pos, iblockstate, tileentity, fakeStack);
            }
        }
        if (!player.isCreative() && flag1) {
            int exp = iblockstate.getExpDrop(world, pos, tileentity, player, fakeStack);
            if (exp > 0) {
                block.popExperience(sl, pos, exp);
            }
        }
        return flag1;
    }

    private static boolean removeBlock(Level world, Player player, BlockPos pos, BlockState state, boolean canHarvest) {
        boolean removed = state.onDestroyedByPlayer(world, pos, player, canHarvest, world.getFluidState(pos));
        if (removed) {
            state.getBlock().destroy(world, pos, state);
        }
        return removed;
    }

    /**
     * Blocks the rift focus never opens (ConfigBlocks.initMisc and the IMC "portableHoleBlacklist" messages).
     * A key is "modid:name", optionally followed by ";property=value" parts that must all appear in the state.
     * 1.21: a key starting with # is a block tag (it replaces the ore dictionary names and the old per-color beds and doors).
     */
    public static final java.util.List<String> portableHoleBlackList = new java.util.ArrayList<>(java.util.List.of(
            "#minecraft:beds", "minecraft:piston", "minecraft:piston_head", "minecraft:sticky_piston", "minecraft:moving_piston",
            "#minecraft:doors", "thaumcraft:infernal_furnace"));

    public static boolean isPortableHoleBlackListed(BlockState blockstate) {
        return isBlockListed(blockstate, portableHoleBlackList);
    }

    public static boolean isBlockListed(BlockState blockstate, java.util.List<String> list) {
        String stateString = blockstate.toString();
        for (String key : list) {
            String[] splitString = key.split(";");
            if (splitString[0].startsWith("#")) {
                net.minecraft.resources.ResourceLocation tag = net.minecraft.resources.ResourceLocation.tryParse(splitString[0].substring(1));
                if (tag != null && blockstate.is(net.minecraft.tags.TagKey.create(Registries.BLOCK, tag))) {
                    return true;
                }
                continue;
            }
            if (!net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(blockstate.getBlock()).toString().equals(splitString[0])) {
                continue;
            }
            if (splitString.length <= 1) {
                return true;
            }
            int matches = 0;
            for (int a = 1; a < splitString.length; ++a) {
                if (stateString.contains(splitString[a])) {
                    ++matches;
                }
            }
            if (matches == splitString.length - 1) {
                return true;
            }
        }
        return false;
    }

    static BlockPos lastPos;
    static double lastdistance;

    /** Shows the cracking animation of breaker id at pos to the players within 32 blocks, except breaker id itself. */
    public static void destroyBlockPartially(Level world, int par1, BlockPos pos, int par5) {
        if (!(world instanceof ServerLevel sl)) {
            return;
        }
        for (ServerPlayer player : sl.players()) {
            if (player != null && player.getId() != par1) {
                double d0 = pos.getX() - player.getX();
                double d2 = pos.getY() - player.getY();
                double d3 = pos.getZ() - player.getZ();
                if (d0 * d0 + d2 * d2 + d3 * d3 >= 1024.0) {
                    continue;
                }
                player.connection.send(new net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket(par1, pos, par5));
            }
        }
    }

    /** Walks from lastPos to the furthest connected block of the same kind (24 out, 48 up at most). */
    public static void findBlocks(Level world, BlockPos pos, BlockState block, int reach) {
        for (int xx = -reach; xx <= reach; ++xx) {
            for (int yy = reach; yy >= -reach; --yy) {
                for (int zz = -reach; zz <= reach; ++zz) {
                    if (Math.abs(lastPos.getX() + xx - pos.getX()) > 24) {
                        return;
                    }
                    if (Math.abs(lastPos.getY() + yy - pos.getY()) > 48) {
                        return;
                    }
                    if (Math.abs(lastPos.getZ() + zz - pos.getZ()) > 24) {
                        return;
                    }
                    BlockPos p = lastPos.offset(xx, yy, zz);
                    BlockState bs = world.getBlockState(p);
                    // 1.12 also compared damageDropped; flattened states of one block drop the same item
                    boolean same = bs.getBlock() == block.getBlock();
                    if (same && bs.getDestroySpeed(world, p) >= 0.0f) {
                        double xd = lastPos.getX() + xx - pos.getX();
                        double yd = lastPos.getY() + yy - pos.getY();
                        double zd = lastPos.getZ() + zz - pos.getZ();
                        double d = xd * xd + yd * yd + zd * zd;
                        if (d > lastdistance) {
                            lastdistance = d;
                            lastPos = p;
                            findBlocks(world, pos, block, reach);
                            return;
                        }
                    }
                }
            }
        }
    }

    /** Breaks the furthest block of the same kind connected to pos (a whole tree, one block at a time). */
    public static boolean breakFurthestBlock(Level world, BlockPos pos, BlockState block, Player player) {
        lastPos = pos;
        lastdistance = 0.0;
        int reach = Utils.isWoodLog(world, pos) ? 2 : 1;
        findBlocks(world, pos, block, reach);
        boolean worked = harvestBlockSkipCheck(world, player, lastPos);
        world.sendBlockUpdated(pos, block, block, 3);
        if (worked && Utils.isWoodLog(world, pos)) {
            world.sendBlockUpdated(pos, block, block, 3);
            for (int xx = -3; xx <= 3; ++xx) {
                for (int yy = -3; yy <= 3; ++yy) {
                    for (int zz = -3; zz <= 3; ++zz) {
                        BlockPos p = lastPos.offset(xx, yy, zz);
                        world.scheduleTick(p, world.getBlockState(p).getBlock(), 50 + world.random.nextInt(75));
                    }
                }
            }
        }
        return worked;
    }

    /** Block.getSilkTouchDrop of 1.12: the block's own item. */
    public static ItemStack getSilkTouchDrop(BlockState bs) {
        return new ItemStack(bs.getBlock());
    }

    /** Sorts positions by their distance to a source block. */
    public static class BlockPosComparator implements java.util.Comparator<BlockPos> {
        private final BlockPos source;

        public BlockPosComparator(BlockPos source) {
            this.source = source;
        }

        @Override
        public int compare(BlockPos a, BlockPos b) {
            if (a.equals(b)) {
                return 0;
            }
            double da = source.distSqr(a);
            double db = source.distSqr(b);
            if (da < db) {
                return -1;
            }
            if (da > db) {
                return 1;
            }
            return 0;
        }
    }
}
