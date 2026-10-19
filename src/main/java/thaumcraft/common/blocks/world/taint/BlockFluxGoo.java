package thaumcraft.common.blocks.world.taint;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import thaumcraft.api.aura.AuraHelper;
import thaumcraft.client.fx.ClientFX;
import thaumcraft.common.entities.monster.EntityThaumicSlime;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.registry.TCMobEffects;

/**
 * Port of BlockFluxGoo. In 1.12 it was a Forge finite fluid (BlockFluidFinite): LEVEL 0 to 7 is how full the block
 * is, and the goo falls, then evens out with its neighbours without ever making more of itself. Vanilla fluids are
 * not finite, so this is a block that does the BlockFluidFinite flow itself on its ticks (every 30 ticks, viscosity
 * 6000, and on random ticks).
 * <p>
 * Anything in it is slowed, and living creatures get Flux Flu for 30 seconds, stronger the fuller the goo. Little by
 * little it evaporates into flux, and the last bit may turn into taint fibres. Thaumic slimes crawl out of it and
 * grow by soaking it up.
 */
public class BlockFluxGoo extends Block {
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 7);
    private static final int QUANTA = 8;
    private static final int TICK_RATE = 30;

    public BlockFluxGoo() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).noCollission().strength(100.0f).noLootTable()
                .sound(SoundsTC.SOUND_TYPE_GORE).randomTicks().pushReaction(PushReaction.DESTROY).noOcclusion());
        registerDefaultState(stateDefinition.any().setValue(LEVEL, 7));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL);
    }

    /** getQuantaPercentage. */
    private static float quantaPercentage(BlockState state) {
        return (state.getValue(LEVEL) + 1) / (float) QUANTA;
    }

    @Override
    protected void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
        int md = state.getValue(LEVEL);
        if (entity instanceof EntityThaumicSlime slime) {
            if (slime.getSize() < md && world.random.nextBoolean()) {
                slime.setSize(slime.getSize() + 1, true);
                if (md > 1) {
                    world.setBlock(pos, state.setValue(LEVEL, md - 1), Block.UPDATE_CLIENTS);
                } else {
                    world.removeBlock(pos, false);
                }
            }
            return;
        }
        float q = quantaPercentage(state);
        Vec3 m = entity.getDeltaMovement();
        entity.setDeltaMovement(m.x * (1.0f - q), m.y, m.z * (1.0f - q));
        if (!world.isClientSide && entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(TCMobEffects.VIS_EXHAUST, 600, md / 3, true, true));
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource rand) {
        tick(state, world, pos, rand);
    }

    @Override
    protected void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource rand) {
        int meta = state.getValue(LEVEL);
        if (meta >= 2 && world.isEmptyBlock(pos.above()) && rand.nextInt(50) == 0) {
            world.removeBlock(pos, false);
            EntityThaumicSlime slime = new EntityThaumicSlime(world);
            slime.moveTo(pos.getX() + 0.5f, pos.getY(), pos.getZ() + 0.5f, 0.0f, 0.0f);
            slime.setSize(meta >= 6 ? 2 : 1, true);
            world.addFreshEntity(slime);
            slime.playSound(SoundsTC.GORE.get(), 1.0f, 1.0f);
            return;
        }
        if (rand.nextInt(4) == 0) {
            if (meta == 0) {
                if (rand.nextBoolean()) {
                    AuraHelper.polluteAura(world, pos, 1.0f, true);
                    world.removeBlock(pos, false);
                } else {
                    world.setBlockAndUpdate(pos, thaumcraft.common.config.ConfigBlocks.taintFibre.get().defaultBlockState());
                }
            } else {
                world.setBlock(pos, state.setValue(LEVEL, meta - 1), Block.UPDATE_CLIENTS);
                AuraHelper.polluteAura(world, pos, 1.0f, true);
            }
            return;
        }
        flow(state, world, pos, rand);
    }

    /** BlockFluidFinite.updateTick: fall, then spread out evenly over the neighbours that have less. */
    private void flow(BlockState state, ServerLevel world, BlockPos pos, RandomSource rand) {
        boolean changed = false;
        int quantaRemaining = state.getValue(LEVEL) + 1;
        int prevRemaining = quantaRemaining;
        quantaRemaining = tryToFlowVerticallyInto(world, pos, quantaRemaining);
        if (quantaRemaining < 1) {
            return;
        } else if (quantaRemaining != prevRemaining) {
            changed = true;
            if (quantaRemaining == 1) {
                world.setBlock(pos, state.setValue(LEVEL, quantaRemaining - 1), Block.UPDATE_CLIENTS);
                return;
            }
        } else if (quantaRemaining == 1) {
            return;
        }
        int lowerThan = quantaRemaining - 1;
        int total = quantaRemaining;
        int count = 1;
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockPos off = pos.relative(side);
            if (displaceIfPossible(world, off)) {
                world.removeBlock(off, false);
            }
            int quanta = getQuantaValueBelow(world, off, lowerThan);
            if (quanta >= 0) {
                ++count;
                total += quanta;
            }
        }
        if (count == 1) {
            if (changed) {
                world.setBlock(pos, state.setValue(LEVEL, quantaRemaining - 1), Block.UPDATE_CLIENTS);
            }
            return;
        }
        int each = total / count;
        int rem = total % count;
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockPos off = pos.relative(side);
            int quanta = getQuantaValueBelow(world, off, lowerThan);
            if (quanta >= 0) {
                int newquanta = each;
                if (rem == count || (rem > 1 && rand.nextInt(count - rem) != 0)) {
                    ++newquanta;
                    --rem;
                }
                if (newquanta != quanta) {
                    if (newquanta == 0) {
                        world.removeBlock(off, false);
                    } else {
                        world.setBlock(off, defaultBlockState().setValue(LEVEL, newquanta - 1), Block.UPDATE_ALL);
                    }
                    world.scheduleTick(off, this, TICK_RATE);
                }
                --count;
            }
        }
        if (rem > 0) {
            ++each;
        }
        world.setBlock(pos, state.setValue(LEVEL, each - 1), Block.UPDATE_CLIENTS);
    }

    private int tryToFlowVerticallyInto(ServerLevel world, BlockPos pos, int amtToInput) {
        BlockPos other = pos.below();
        if (other.getY() < world.getMinBuildHeight()) {
            world.removeBlock(pos, false);
            return 0;
        }
        int amt = getQuantaValueBelow(world, other, QUANTA);
        if (amt >= 0) {
            amt += amtToInput;
            if (amt > QUANTA) {
                world.setBlockAndUpdate(other, defaultBlockState().setValue(LEVEL, QUANTA - 1));
                world.scheduleTick(other, this, TICK_RATE);
                return amt - QUANTA;
            } else if (amt > 0) {
                world.setBlockAndUpdate(other, defaultBlockState().setValue(LEVEL, amt - 1));
                world.scheduleTick(other, this, TICK_RATE);
                world.removeBlock(pos, false);
                return 0;
            }
            return amtToInput;
        }
        if (displaceIfPossible(world, other)) {
            world.setBlockAndUpdate(other, world.getBlockState(pos));
            world.scheduleTick(other, this, TICK_RATE);
            world.removeBlock(pos, false);
            return 0;
        }
        return amtToInput;
    }

    /** getQuantaValue: 0 for air, the amount of goo for goo, -1 for anything else. */
    private int getQuantaValueBelow(BlockGetter world, BlockPos pos, int belowThis) {
        BlockState bs = world.getBlockState(pos);
        int quantaRemaining;
        if (bs.isAir()) {
            quantaRemaining = 0;
        } else if (bs.is(this)) {
            quantaRemaining = bs.getValue(LEVEL) + 1;
        } else {
            return -1;
        }
        return quantaRemaining >= belowThis ? -1 : quantaRemaining;
    }

    /**
     * BlockFluidBase.displaceIfPossible: goo pushes aside (and drops) anything that does not block movement, taint
     * fibres included, but not other fluids or portals.
     */
    private boolean displaceIfPossible(ServerLevel world, BlockPos pos) {
        BlockState bs = world.getBlockState(pos);
        if (bs.isAir()) {
            return true;
        }
        if (bs.is(this)) {
            return false;
        }
        if (bs.is(thaumcraft.common.config.ConfigBlocks.taintFibre.get())) {
            return true;
        }
        if (!bs.getFluidState().isEmpty() || bs.blocksMotion() || bs.is(Blocks.NETHER_PORTAL) || bs.is(Blocks.END_PORTAL)
                || bs.is(Blocks.END_GATEWAY) || bs.getDestroySpeed(world, pos) < 0.0f) {
            return false;
        }
        Block.dropResources(bs, world, pos, world.getBlockEntity(pos));
        return true;
    }

    @Override
    protected void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!oldState.is(this)) {
            world.scheduleTick(pos, this, TICK_RATE);
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        world.scheduleTick(pos, this, TICK_RATE);
    }

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return state.getValue(LEVEL) < 4;
    }

    /** Fluids have no outline in 1.12 either: the goo is looked through and cannot be broken by hand. */
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    /** Hides the faces against more goo, like the fluid renderer did. */
    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacent, Direction dir) {
        if (adjacent.is(this)) {
            return dir.getAxis() == Direction.Axis.Y || adjacent.getValue(LEVEL) >= state.getValue(LEVEL);
        }
        return super.skipRendering(state, adjacent, dir);
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource rand) {
        int meta = state.getValue(LEVEL);
        if (rand.nextInt(44) <= meta) {
            thaumcraft.client.fx.FXDispatcher.fluxGooBubble(pos.getX() + rand.nextFloat(), pos.getY() + 0.125f * meta, pos.getZ() + rand.nextFloat());
        }
    }
}
