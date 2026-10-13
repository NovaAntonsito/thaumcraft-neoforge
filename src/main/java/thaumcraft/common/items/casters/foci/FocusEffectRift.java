package thaumcraft.common.items.casters.foci;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.joml.Vector3f;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.casters.FocusEffect;
import thaumcraft.api.casters.NodeSetting;
import thaumcraft.api.casters.Trajectory;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.utils.BlockUtils;
import thaumcraft.common.tiles.misc.TileHole;

/**
 * Rift effect: a temporary tunnel, three blocks wide, as deep as the depth setting times the spell power.
 * 1.21: the Outer Lands are not ported yet, so the check against them uses their future id. Particles are blue dust
 * until the FX stage.
 */
public class FocusEffectRift extends FocusEffect {
    @Override
    public String getResearch() {
        return "FOCUSRIFT";
    }

    @Override
    public String getKey() {
        return "thaumcraft.RIFT";
    }

    @Override
    public Aspect getAspect() {
        return Aspect.ELDRITCH;
    }

    @Override
    public int getComplexity() {
        return 3 + getSettingValue("duration") / 2 + getSettingValue("depth") / 4;
    }

    @Override
    public boolean execute(HitResult target, Trajectory trajectory, float finalPower, int num) {
        if (!(target instanceof BlockHitResult bhr) || target.getType() != HitResult.Type.BLOCK) {
            return false;
        }
        Level world = getPackage().world;
        BlockPos tp = bhr.getBlockPos();
        if (world.dimension().location().equals(Thaumcraft.id("outer_lands"))) {
            world.playSound(null, tp.getX() + 0.5, tp.getY() + 0.5, tp.getZ() + 0.5, SoundsTC.WANDFAIL.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
            return false;
        }
        float maxdis = getSettingValue("depth") * finalPower;
        int dur = 20 * getSettingValue("duration");
        int distance;
        BlockPos pos = tp;
        for (distance = 0; distance < maxdis; ++distance) {
            BlockState bi = world.getBlockState(pos);
            if (BlockUtils.isPortableHoleBlackListed(bi) || bi.getBlock() == Blocks.BEDROCK || bi.getBlock() == ConfigBlocks.hole.get() || bi.isAir()) {
                break;
            }
            if (bi.getDestroySpeed(world, pos) == -1.0f) {
                break;
            }
            pos = pos.relative(bhr.getDirection().getOpposite());
        }
        createHole(world, tp, bhr.getDirection(), (byte) Math.round((float) (distance + 1)), dur);
        return true;
    }

    public static boolean createHole(Level world, BlockPos pos, Direction side, byte count, int max) {
        BlockState bs = world.getBlockState(pos);
        if (!world.isClientSide && world.getBlockEntity(pos) == null && !BlockUtils.isPortableHoleBlackListed(bs) && bs.getBlock() != Blocks.BEDROCK
                && bs.getBlock() != ConfigBlocks.hole.get() && (bs.isAir() || !bs.canBeReplaced()) && bs.getDestroySpeed(world, pos) != -1.0f) {
            if (world.setBlock(pos, ConfigBlocks.hole.get().defaultBlockState(), 3) && world.getBlockEntity(pos) instanceof TileHole ts) {
                ts.oldblock = bs;
                ts.countdownmax = (short) max;
                ts.count = count;
                ts.direction = side;
                ts.setChanged();
            }
            return true;
        }
        return false;
    }

    @Override
    public NodeSetting[] createSettings() {
        int[] depth = {8, 16, 24, 32};
        String[] depthDesc = {"8", "16", "24", "32"};
        return new NodeSetting[] {new NodeSetting("depth", "focus.rift.depth", new NodeSetting.NodeSettingIntList(depth, depthDesc)),
                new NodeSetting("duration", "focus.common.duration", new NodeSetting.NodeSettingIntRange(2, 10))};
    }

    @Override
    public void renderParticleFX(Level world, double posX, double posY, double posZ, double motionX, double motionY, double motionZ) {
        world.addParticle(new DustParticleOptions(new Vector3f(0.25f, 0.25f, 1.0f), Math.max(0.2f, (float) (0.7 + world.random.nextGaussian() * 0.3))),
                posX, posY, posZ, motionX, motionY, motionZ);
    }

    @Override
    public void onCast(Entity caster) {
        caster.level().playSound(null, caster.blockPosition().above(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.2f, 0.7f);
    }
}
