package thaumcraft.common.blocks.misc;

import org.joml.Vector3f;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec3;
import thaumcraft.api.capabilities.PlayerWarp;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.common.registry.TCFluids;
import thaumcraft.common.registry.TCMobEffects;

/**
 * Port of BlockFluidPure. A player standing in a source block gets Warp Ward, shorter the more permanent warp they
 * have, and the source is used up. Anything moving through it is slowed.
 */
public class BlockFluidPure extends LiquidBlock {
    public BlockFluidPure() {
        super(TCFluids.PURIFYING.get(), BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).replaceable().noCollission()
                .strength(100.0f).pushReaction(PushReaction.DESTROY).noLootTable().liquid().lightLevel(s -> 5));
    }

    @Override
    protected void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
        FluidState fluid = state.getFluidState();
        // getQuantaPercentage: how full the block is.
        float q = fluid.getAmount() / 8.0f;
        Vec3 m = entity.getDeltaMovement();
        entity.setDeltaMovement(m.x * (1.0f - q / 2.0f), m.y, m.z * (1.0f - q / 2.0f));
        if (!world.isClientSide && fluid.isSource() && entity instanceof Player player && !player.hasEffect(TCMobEffects.WARP_WARD)) {
            int warp = ThaumcraftCapabilities.getWarp(player).get(PlayerWarp.EnumWarpType.PERMANENT);
            int div = 1;
            if (warp > 0) {
                div = (int) Math.sqrt(warp);
                if (div < 1) {
                    div = 1;
                }
            }
            player.addEffect(new MobEffectInstance(TCMobEffects.WARP_WARD, Math.min(32000, 200000 / div), 0, true, true));
            world.removeBlock(pos, false);
        }
    }

    /** TODO: FXGeneric motes (stage 10); for now faint white dust drifting up. */
    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource rand) {
        if (rand.nextInt(10) == 0) {
            float height = state.getFluidState().getHeight(world, pos);
            world.addParticle(new DustParticleOptions(new Vector3f(1.0f, 1.0f, 1.0f), rand.nextFloat() * 0.3f + 0.3f),
                    pos.getX() + rand.nextFloat(), pos.getY() + height, pos.getZ() + rand.nextFloat(), 0.0, 0.01, 0.0);
        }
        if (rand.nextInt(50) == 0) {
            world.playLocalSound(pos.getX() + rand.nextFloat(), pos.getY() + 0.5, pos.getZ() + rand.nextFloat(), SoundEvents.LAVA_POP,
                    SoundSource.BLOCKS, 0.1f + rand.nextFloat() * 0.1f, 0.9f + rand.nextFloat() * 0.15f, false);
        }
    }
}
