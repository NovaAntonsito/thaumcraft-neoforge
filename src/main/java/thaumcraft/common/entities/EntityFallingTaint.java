package thaumcraft.common.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import thaumcraft.client.fx.ClientFX;
import thaumcraft.common.blocks.world.taint.BlockTaint;
import thaumcraft.common.blocks.world.taint.TaintHelper;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.registry.TCEntities;

/**
 * Port of EntityFallingTaint: a lump of crusted taint that came loose. It lifts the block on its first tick, falls,
 * and sets the block down where it lands, unless it can keep falling from there.
 * The 1.12 code also meant to land on flux goo, but compared a block state with a block, so that never happened;
 * here it does not either.
 */
public class EntityFallingTaint extends Entity implements IEntityWithComplexSpawn {
    public BlockState fallTile = Blocks.AIR.defaultBlockState();
    BlockPos oldPos = BlockPos.ZERO;
    public int fallTime = 0;
    private int fallHurtMax = 40;
    private float fallHurtAmount = 2.0f;

    public EntityFallingTaint(EntityType<? extends EntityFallingTaint> type, Level world) {
        super(type, world);
    }

    public EntityFallingTaint(Level world, double x, double y, double z, BlockState state, BlockPos o) {
        this(TCEntities.FALLING_TAINT.get(), world);
        fallTile = state;
        blocksBuilding = true;
        setPos(x, y, z);
        setDeltaMovement(0.0, 0.0, 0.0);
        xo = x;
        yo = y;
        zo = z;
        oldPos = o;
    }

    public BlockState getBlock() {
        return fallTile;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected MovementEmission getMovementEmission() {
        return MovementEmission.NONE;
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf data) {
        data.writeVarInt(Block.getId(fallTile));
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf data) {
        fallTile = Block.stateById(data.readVarInt());
    }

    @Override
    public boolean isPickable() {
        return !isRemoved();
    }

    @Override
    public void tick() {
        if (fallTile == null || fallTile.isAir()) {
            discard();
            return;
        }
        xo = getX();
        yo = getY();
        zo = getZ();
        ++fallTime;
        setDeltaMovement(getDeltaMovement().add(0.0, -0.04, 0.0));
        move(MoverType.SELF, getDeltaMovement());
        setDeltaMovement(getDeltaMovement().scale(0.98));
        BlockPos bp = blockPosition();
        Level world = level();
        if (!world.isClientSide) {
            if (fallTime == 1) {
                if (world.getBlockState(oldPos) != fallTile) {
                    discard();
                    return;
                }
                world.removeBlock(oldPos, false);
            }
            if (onGround()) {
                setDeltaMovement(getDeltaMovement().multiply(0.7, -0.5, 0.7));
                BlockState at = world.getBlockState(bp);
                if (!at.is(Blocks.PISTON) && !at.is(Blocks.MOVING_PISTON) && !at.is(Blocks.PISTON_HEAD)) {
                    playSound(SoundsTC.GORE.get(), 0.5f, ((random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f) * 0.8f);
                    discard();
                    if (canPlace(bp) && !BlockTaint.canFallBelow(world, bp.below())) {
                        world.setBlockAndUpdate(bp, fallTile);
                    }
                }
            } else if ((fallTime > 100 && (bp.getY() < world.getMinBuildHeight() + 1 || bp.getY() > world.getMaxBuildHeight())) || fallTime > 600) {
                discard();
            }
        } else if (onGround() || fallTime == 1) {
            for (int j = 0; j < 10; ++j) {
                thaumcraft.client.fx.FXDispatcher.taintLandFX(this);
            }
        }
    }

    private boolean canPlace(BlockPos pos) {
        BlockState bs = level().getBlockState(pos);
        return bs.is(ConfigBlocks.taintFibre.get()) || bs.is(ConfigBlocks.fluxGoo.get())
                || (TaintHelper.isReplaceable(bs) && fallTile.canSurvive(level(), pos));
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, net.minecraft.world.damagesource.DamageSource source) {
        return false;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        nbt.put("BlockState", NbtUtils.writeBlockState(fallTile));
        nbt.putByte("Time", (byte) fallTime);
        nbt.putFloat("FallHurtAmount", fallHurtAmount);
        nbt.putInt("FallHurtMax", fallHurtMax);
        nbt.putLong("Old", oldPos.asLong());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        fallTile = NbtUtils.readBlockState(level().holderLookup(Registries.BLOCK), nbt.getCompound("BlockState"));
        fallTime = nbt.getByte("Time") & 0xFF;
        oldPos = BlockPos.of(nbt.getLong("Old"));
        if (nbt.contains("FallHurtAmount")) {
            fallHurtAmount = nbt.getFloat("FallHurtAmount");
            fallHurtMax = nbt.getInt("FallHurtMax");
        }
        if (fallTile.isAir()) {
            fallTile = Blocks.SAND.defaultBlockState();
        }
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.BLOCKS;
    }

    @Override
    public boolean displayFireAnimation() {
        return false;
    }
}
