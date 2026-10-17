package thaumcraft.common.blocks.world.taint;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootParams;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aura.AuraHelper;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.entities.EntityFallingTaint;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.utils.BlockUtils;
import thaumcraft.common.lib.utils.Utils;
import thaumcraft.common.registry.TCMobEffects;

/**
 * Port of BlockTaint: crusted taint, tainted soil, tainted rock and the taint geyser. Away from a taint seed they
 * wither (rock to porous stone, soil to dirt, crust and geyser to flux goo). Tainted rock spreads fibres, loose
 * crust falls, and the geyser pollutes the aura.
 * TODO: the geyser's taint swarm (stage 9b-2).
 */
public class BlockTaint extends Block implements ITaintBlock {
    static final Random r = new Random(System.currentTimeMillis());

    public BlockTaint() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(10.0f, 60.0f).sound(SoundsTC.SOUND_TYPE_GORE)
                .randomTicks().pushReaction(PushReaction.DESTROY));
    }

    @Override
    public void die(Level world, BlockPos pos, BlockState state) {
        if (state.is(ConfigBlocks.taintRock.get())) {
            world.setBlockAndUpdate(pos, ConfigBlocks.stonePorous.get().defaultBlockState());
        } else if (state.is(ConfigBlocks.taintSoil.get())) {
            world.setBlockAndUpdate(pos, Blocks.DIRT.defaultBlockState());
        } else if (state.is(ConfigBlocks.taintCrust.get()) || state.is(ConfigBlocks.taintGeyser.get())) {
            world.setBlockAndUpdate(pos, ConfigBlocks.fluxGoo.get().defaultBlockState());
        } else {
            world.removeBlock(pos, false);
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (!TaintHelper.isNearTaintSeed(world, pos) && random.nextInt(10) == 0) {
            die(world, pos, state);
            return;
        }
        if (state.is(ConfigBlocks.taintRock.get())) {
            TaintHelper.spreadFibres(world, pos);
        }
        if (state.is(ConfigBlocks.taintCrust.get())) {
            if (tryToFall(world, pos, pos)) {
                return;
            }
            if (world.isEmptyBlock(pos.above())) {
                boolean doIt = true;
                Direction dir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
                for (int a = 1; a < 4; ++a) {
                    if (!world.isEmptyBlock(pos.relative(dir).below(a))) {
                        doIt = false;
                        break;
                    }
                    if (!world.getBlockState(pos.below(a)).is(this)) {
                        doIt = false;
                        break;
                    }
                }
                if (doIt && tryToFall(world, pos, pos.relative(dir))) {
                    return;
                }
            }
        } else if (state.is(ConfigBlocks.taintGeyser.get())) {
            // TODO stage 9b-2: with 20% chance, a player within 32 blocks and no taint swarm within 32, a swarm
            // spawns 1.25 blocks above instead of polluting.
            if (AuraHelper.getFlux(world, pos) < 2.0f) {
                AuraHelper.polluteAura(world, pos, 0.25f, true);
            }
        }
    }

    @Override
    public void stepOn(Level world, BlockPos pos, BlockState state, Entity entity) {
        if (!world.isClientSide && entity instanceof LivingEntity living && !living.isInvertedHealAndHarm() && world.random.nextInt(250) == 0) {
            living.addEffect(new MobEffectInstance(TCMobEffects.FLUX_TAINT, 200, 0, false, true));
        }
        super.stepOn(world, pos, state, entity);
    }

    /** Event 1, sent when taint takes over a block. */
    @Override
    protected boolean triggerEvent(BlockState state, Level world, BlockPos pos, int id, int param) {
        if (id == 1) {
            if (world.isClientSide) {
                world.playLocalSound(pos, SoundEvents.CHORUS_FLOWER_DEATH, SoundSource.BLOCKS, 0.1f, 0.9f + world.random.nextFloat() * 0.2f, false);
            }
            return true;
        }
        return super.triggerEvent(state, world, pos, id, param);
    }

    /** True if falling taint can drop into pos: air, fire, fibres, replaceable blocks, water and lava, but not next to wood. */
    public static boolean canFallBelow(Level world, BlockPos pos) {
        BlockState bs = world.getBlockState(pos);
        for (int xx = -1; xx <= 1; ++xx) {
            for (int zz = -1; zz <= 1; ++zz) {
                for (int yy = -1; yy <= 1; ++yy) {
                    if (Utils.isWoodLog(world, pos.offset(xx, yy, zz))) {
                        return false;
                    }
                }
            }
        }
        if (bs.isAir()) {
            return true;
        }
        if (bs.is(ConfigBlocks.fluxGoo.get()) && bs.getValue(BlockFluxGoo.LEVEL) >= 4) {
            return false;
        }
        return bs.is(BlockTags.FIRE) || bs.is(ConfigBlocks.taintFibre.get()) || TaintHelper.isReplaceable(bs)
                || bs.getFluidState().is(FluidTags.WATER) || bs.getFluidState().is(FluidTags.LAVA);
    }

    private boolean tryToFall(Level world, BlockPos pos, BlockPos pos2) {
        if (!BlockTaintFibre.isOnlyAdjacentToTaint(world, pos)) {
            return false;
        }
        if (canFallBelow(world, pos2.below()) && pos2.getY() >= world.getMinBuildHeight()) {
            int b0 = 32;
            if (world.hasChunksAt(pos2.offset(-b0, -b0, -b0), pos2.offset(b0, b0, b0))) {
                if (!world.isClientSide) {
                    EntityFallingTaint falling = new EntityFallingTaint(world, pos2.getX() + 0.5f, pos2.getY() + 0.5f, pos2.getZ() + 0.5f,
                            world.getBlockState(pos), pos);
                    world.addFreshEntity(falling);
                    return true;
                }
            } else {
                world.removeBlock(pos, false);
                BlockPos p2;
                for (p2 = pos2; canFallBelow(world, p2.below()) && p2.getY() > world.getMinBuildHeight(); p2 = p2.below()) {
                }
                if (p2.getY() > world.getMinBuildHeight()) {
                    world.setBlockAndUpdate(p2, ConfigBlocks.taintCrust.get().defaultBlockState());
                }
            }
        }
        return false;
    }

    /** Silk touch keeps the block. Otherwise only tainted rock drops something: a flux crystal, rarely. */
    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> ret = new ArrayList<>();
        if (BlockUtils.getSilkTouchLevel(params) > 0) {
            ret.add(new ItemStack(this));
            return ret;
        }
        if (state.is(ConfigBlocks.taintRock.get())) {
            int rr = r.nextInt(15) + BlockUtils.getFortuneLevel(params);
            if (rr > 13) {
                ret.add(ConfigItems.crystalEssence.get().makeCrystal(Aspect.FLUX, 1));
            }
        }
        return ret;
    }
}
