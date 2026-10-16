package thaumcraft.common.blocks.world.plants;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import thaumcraft.common.world.aura.AuraHandler;

/**
 * Port of BlockLeavesTC. DECAYABLE is the inverse of 1.21's PERSISTENT; placing leaves by hand already makes them
 * persistent. Drops (saplings, and quicksilver nuggets from silverwood) are in the loot tables.
 */
public class BlockLeavesTC extends LeavesBlock {
    public static final MapCodec<BlockLeavesTC> CODEC = simpleCodec(p -> new BlockLeavesTC(false));
    private final boolean silverwood;

    public BlockLeavesTC(boolean silverwood) {
        super(BlockBehaviour.Properties.of().mapColor(silverwood ? MapColor.COLOR_LIGHT_BLUE : MapColor.PLANT).strength(0.2f)
                .randomTicks().sound(SoundType.GRASS).noOcclusion().isValidSpawn(BlockLeavesTC::ocelotOrParrot)
                .isSuffocating((s, l, p) -> false).isViewBlocking((s, l, p) -> false).ignitedByLava()
                .pushReaction(PushReaction.DESTROY).isRedstoneConductor((s, l, p) -> false));
        this.silverwood = silverwood;
    }

    private static Boolean ocelotOrParrot(BlockState state, BlockGetter level, BlockPos pos, EntityType<?> type) {
        return type == EntityType.OCELOT || type == EntityType.PARROT;
    }

    @Override
    public MapCodec<? extends LeavesBlock> codec() {
        return CODEC;
    }

    public boolean isSilverwood() {
        return silverwood;
    }

    /** Vanilla only ticks leaves that are about to decay; living silverwood leaves also tick, to feed the aura. */
    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return (silverwood && !state.getValue(PERSISTENT)) || super.isRandomlyTicking(state);
    }

    /** updateTick: natural silverwood leaves slowly refill the aura up to its base. */
    @Override
    protected void randomTick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource rand) {
        if (silverwood && !state.getValue(PERSISTENT) && AuraHandler.getVis(worldIn, pos) < AuraHandler.getAuraBase(worldIn, pos)) {
            AuraHandler.addVis(worldIn, pos, 0.01f);
        }
        super.randomTick(state, worldIn, pos, rand);
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 60;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 30;
    }
}
