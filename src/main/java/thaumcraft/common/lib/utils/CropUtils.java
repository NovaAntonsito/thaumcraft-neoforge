package thaumcraft.common.lib.utils;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import thaumcraft.api.ThaumcraftApi;

/**
 * Port of CropUtils. The original kept crop lists keyed by block name and metadata (filled by other mods through
 * IMC); here they are keyed by block state, and a "metadata" is the value of the block's age property.
 */
public final class CropUtils {
    private CropUtils() {
    }

    public static final Set<BlockState> clickableCrops = new HashSet<>();
    public static final Set<BlockState> standardCrops = new HashSet<>();
    public static final Set<BlockState> stackedCrops = new HashSet<>();
    public static final Set<BlockState> lampBlacklist = new HashSet<>();

    /** The states a 1.12 block + metadata pair stood for: every state for 32767, otherwise the ones with that age. */
    private static Set<BlockState> states(Block block, int grownMeta) {
        Set<BlockState> out = new HashSet<>();
        if (grownMeta == 32767) {
            out.addAll(block.getStateDefinition().getPossibleStates());
            return out;
        }
        Property<?> age = block.getStateDefinition().getProperty("age");
        for (BlockState bs : block.getStateDefinition().getPossibleStates()) {
            if (age instanceof IntegerProperty ip ? bs.getValue(ip) == grownMeta : grownMeta == 0) {
                out.add(bs);
            }
        }
        // BlockCrops in 1.12 was grown at meta 7: the fully grown state
        if (block instanceof CropBlock crop && grownMeta != crop.getMaxAge()) {
            out.add(crop.getStateForAge(crop.getMaxAge()));
        }
        return out;
    }

    public static void addStandardCrop(Block block, int grownMeta) {
        standardCrops.addAll(states(block, grownMeta));
    }

    public static void addClickableCrop(Block block, int grownMeta) {
        clickableCrops.addAll(states(block, grownMeta));
    }

    public static void addStackedCrop(Block block, int grownMeta) {
        stackedCrops.addAll(states(block, grownMeta));
    }

    public static boolean isGrownCrop(Level world, BlockPos pos) {
        if (world.isEmptyBlock(pos)) {
            return false;
        }
        BlockState bs = world.getBlockState(pos);
        Block bi = bs.getBlock();
        boolean found = standardCrops.contains(bs) || clickableCrops.contains(bs) || stackedCrops.contains(bs);
        Block biB = world.getBlockState(pos.below()).getBlock();
        return (bi instanceof BonemealableBlock g && !g.isValidBonemealTarget(world, pos, bs) && !(bi instanceof StemBlock))
                || (bi instanceof CropBlock crop && crop.isMaxAge(bs) && !found)
                || standardCrops.contains(bs)
                || clickableCrops.contains(bs)
                || (stackedCrops.contains(bs) && biB == bi);
    }

    /** Adds every state of the block when allStates is true, as metadata 32767 did. */
    public static void blacklistLamp(Block block, boolean allStates) {
        if (allStates) {
            lampBlacklist.addAll(block.getStateDefinition().getPossibleStates());
        } else {
            lampBlacklist.add(block.defaultBlockState());
        }
    }

    public static boolean doesLampGrow(Level world, BlockPos pos) {
        return !lampBlacklist.contains(world.getBlockState(pos));
    }

    /**
     * The crop part of ModConfig.postInitMisc. 1.12 registered the seed of every IPlantable item; in 1.21 those are
     * the items named apart from the plant block they place (wheat seeds, carrots, nether wart and so on).
     */
    public static void registerDefaults() {
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof ItemNameBlockItem bi && bi.getBlock() instanceof BushBlock) {
                ThaumcraftApi.registerSeed(bi.getBlock(), new ItemStack(item));
            }
        }
        addStandardCrop(Blocks.MELON, 32767);
        addStandardCrop(Blocks.PUMPKIN, 32767);
        addStackedCrop(Blocks.SUGAR_CANE, 32767);
        addStackedCrop(Blocks.CACTUS, 32767);
        addStandardCrop(Blocks.NETHER_WART, 3);
        ThaumcraftApi.registerSeed(Blocks.COCOA, new ItemStack(Items.COCOA_BEANS));
    }
}
