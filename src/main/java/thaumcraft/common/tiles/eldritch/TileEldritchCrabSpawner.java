package thaumcraft.common.tiles.eldritch;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import thaumcraft.client.fx.FXDispatcher;
import thaumcraft.common.entities.monster.EntityEldritchCrab;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.registry.TCEntities;

/**
 * Port of TC4's TileEldritchCrabSpawner. While a player is within 16 blocks and there are no more than five eldritch
 * crabs within 32, it vents and then pushes out a crab without a helmet through its facing side, every 150 to 250
 * ticks. The client draws the vent's steam. The facing is the block's FACING state.
 */
public class TileEldritchCrabSpawner extends BlockEntity {
    public int count = 150;
    public int ticks = 0;
    int venting = 0;

    public TileEldritchCrabSpawner(BlockPos pos, BlockState state) {
        super(TCBlockEntities.ELDRITCH_CRAB_SPAWNER.get(), pos, state);
    }

    public Direction getFacing() {
        return getBlockState().getValue(BlockStateProperties.FACING);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TileEldritchCrabSpawner tile) {
        if (tile.ticks == 0) {
            tile.ticks = level.random.nextInt(500);
        }

        ++tile.ticks;
        if (!level.isClientSide) {
            --tile.count;
            if (tile.count < 0) {
                tile.count = 50 + level.random.nextInt(50);
            } else {
                if (tile.count == 15 && tile.isActivated() && !tile.maxEntitiesReached()) {
                    level.blockEvent(pos, state.getBlock(), 1, 0);
                    level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 1.0F);
                }

                if (tile.count <= 0 && tile.isActivated() && !tile.maxEntitiesReached()) {
                    tile.count = 150 + level.random.nextInt(100);
                    tile.spawnCrab();
                    level.playSound(null, pos, SoundsTC.GORE.get(), SoundSource.BLOCKS, 0.5F, 1.0F);
                }
            }
        } else if (tile.venting > 0) {
            --tile.venting;

            for (int a = 0; a < 3; ++a) {
                tile.drawVent();
            }
        } else if (level.random.nextInt(20) == 0) {
            tile.drawVent();
        }
    }

    void drawVent() {
        Direction dir = getFacing();
        var rand = level.random;
        float fx = 0.15F - rand.nextFloat() * 0.3F;
        float fz = 0.15F - rand.nextFloat() * 0.3F;
        float fy = 0.15F - rand.nextFloat() * 0.3F;
        float fx2 = 0.1F - rand.nextFloat() * 0.2F;
        float fz2 = 0.1F - rand.nextFloat() * 0.2F;
        float fy2 = 0.1F - rand.nextFloat() * 0.2F;
        BlockPos p = getBlockPos();
        FXDispatcher.drawVentParticles(p.getX() + 0.5F + fx + dir.getStepX() / 2.1F, p.getY() + 0.5F + fy + dir.getStepY() / 2.1F,
                p.getZ() + 0.5F + fz + dir.getStepZ() / 2.1F, dir.getStepX() / 3.0F + fx2, dir.getStepY() / 3.0F + fy2,
                dir.getStepZ() / 3.0F + fz2, 10061994, 2.0F);
    }

    @Override
    public boolean triggerEvent(int i, int j) {
        if (i == 1) {
            this.venting = 20;
            return true;
        }
        return super.triggerEvent(i, j);
    }

    private boolean maxEntitiesReached() {
        return level.getEntitiesOfClass(EntityEldritchCrab.class, new AABB(getBlockPos()).inflate(32.0)).size() > 5;
    }

    public boolean isActivated() {
        BlockPos p = getBlockPos();
        return level.getNearestPlayer(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, 16.0, false) != null;
    }

    private void spawnCrab() {
        if (!(level instanceof ServerLevel world)) {
            return;
        }
        Direction dir = getFacing();
        EntityEldritchCrab crab = TCEntities.ELDRITCH_CRAB.get().create(world);
        if (crab == null) {
            return;
        }
        BlockPos p = getBlockPos().relative(dir);
        crab.moveTo(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, 0.0F, 0.0F);
        crab.finalizeSpawn(world, world.getCurrentDifficultyAt(p), MobSpawnType.SPAWNER, null);
        crab.setHelm(false);
        crab.setDeltaMovement(dir.getStepX() * 0.2F, dir.getStepY() * 0.2F, dir.getStepZ() * 0.2F);
        world.addFreshEntity(crab);
    }
}
