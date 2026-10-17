package thaumcraft.common.blocks.basic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.lib.utils.BlockUtils;

/**
 * Port of BlockStonePorous: what tainted rock leaves behind. It breaks into gravel, but now and then (more with
 * fortune) it gives something from the list below instead. Silk touch keeps it.
 * TODO: the ore clusters (iron 20, gold 10, quartz 10, and copper, tin, silver and lead when those exist) are not
 * ported yet, so they are not in the list.
 */
public class BlockStonePorous extends Block {
    static final Random r = new Random(System.currentTimeMillis());
    private static List<Entry> pdrops = null;

    private record Entry(ItemStack item, int weight) {
    }

    public BlockStonePorous() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops()
                .strength(1.0f, 3.0f).sound(SoundType.STONE));
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> ret = new ArrayList<>();
        if (BlockUtils.getSilkTouchLevel(params) > 0) {
            ret.add(new ItemStack(this));
            return ret;
        }
        int rr = r.nextInt(15) + BlockUtils.getFortuneLevel(params);
        if (rr > 13) {
            if (pdrops == null || pdrops.isEmpty()) {
                createDrops();
            }
            ret.add(pick().copy());
        } else {
            ret.add(new ItemStack(Blocks.GRAVEL));
        }
        return ret;
    }

    private static ItemStack pick() {
        int total = 0;
        for (Entry e : pdrops) total += e.weight();
        int roll = r.nextInt(total);
        for (Entry e : pdrops) {
            roll -= e.weight();
            if (roll < 0) {
                return e.item();
            }
        }
        return pdrops.get(pdrops.size() - 1).item();
    }

    /**
     * One crystal of each compound aspect (weight 1 each), then the rest. As in the original, the flux crystal
     * holds 100 flux instead of 1.
     */
    private static void createDrops() {
        List<Entry> drops = new ArrayList<>();
        for (Aspect aspect : Aspect.getCompoundAspects()) {
            ItemStack is = new ItemStack(ConfigItems.crystalEssence.get());
            ConfigItems.crystalEssence.get().setAspects(is, new AspectList().add(aspect, (aspect == Aspect.FLUX) ? 100 : (aspect.isPrimal() ? 20 : 1)));
            drops.add(new Entry(is, 1));
        }
        drops.add(new Entry(new ItemStack(ConfigItems.amber.get()), 20));
        drops.add(new Entry(new ItemStack(Items.DIAMOND), 2));
        drops.add(new Entry(new ItemStack(Items.EMERALD), 4));
        drops.add(new Entry(new ItemStack(Items.REDSTONE), 8));
        drops.add(new Entry(new ItemStack(Items.PRISMARINE_CRYSTALS), 3));
        drops.add(new Entry(new ItemStack(Items.PRISMARINE_SHARD), 3));
        drops.add(new Entry(new ItemStack(Items.CLAY_BALL), 30));
        drops.add(new Entry(new ItemStack(Items.QUARTZ), 15));
        pdrops = drops;
    }
}
