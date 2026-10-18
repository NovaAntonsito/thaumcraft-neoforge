package thaumcraft.common.blocks.world;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.utils.BlockUtils;
import thaumcraft.common.lib.utils.Utils;

/**
 * Port of BlockLoot: the loot crates (wood) and urns (stone) of the cultist portal and the barrows. They break at
 * once with anything and drop 1 + rarity + 0 to 2 random treasures, or themselves with silk touch.
 */
public class BlockLoot extends Block {
    private static final VoxelShape URN = box(2, 1, 2, 14, 13, 14);
    private static final VoxelShape CRATE = box(1, 0, 1, 15, 14, 15);

    public final LootType type;
    private final boolean urn;

    public BlockLoot(boolean urn, LootType type) {
        super(BlockBehaviour.Properties.of().mapColor(urn ? MapColor.STONE : MapColor.WOOD).strength(0.15f, 0.0f)
                .sound(urn ? SoundsTC.SOUND_TYPE_URN : SoundType.WOOD).noOcclusion());
        this.urn = urn;
        this.type = type;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return urn ? URN : CRATE;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> ret = new ArrayList<>();
        if (BlockUtils.getSilkTouchLevel(params) > 0) {
            ret.add(new ItemStack(this));
            return ret;
        }
        var rand = params.getLevel().random;
        for (int q = 1 + type.ordinal() + rand.nextInt(3), a = 0; a < q; ++a) {
            ItemStack is = Utils.generateLoot(type.ordinal(), rand, params.getLevel().registryAccess());
            if (!is.isEmpty()) {
                ret.add(is.copy());
            }
        }
        return ret;
    }

    public enum LootType {
        COMMON,
        UNCOMMON,
        RARE
    }
}
