package thaumcraft.common.tiles.essentia;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.tiles.TileThaumcraft;

/**
 * Port of TileCentrifuge. Draws one compound essentia from below, spins for two seconds and offers one of its two
 * components at the top. A redstone signal stops it.
 */
public class TileCentrifuge extends TileThaumcraft implements IAspectContainer, IEssentiaTransport {
    @Nullable public Aspect aspectOut;
    @Nullable public Aspect aspectIn;
    int count;
    int process;
    float rotationSpeed;
    public float rotation;

    public TileCentrifuge(BlockPos pos, BlockState state) {
        super(TCBlockEntities.CENTRIFUGE.get(), pos, state);
    }

    @Override
    protected void readSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        aspectIn = Aspect.getAspect(tag.getString("aspectIn"));
        aspectOut = Aspect.getAspect(tag.getString("aspectOut"));
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        if (aspectIn != null) {
            tag.putString("aspectIn", aspectIn.getTag());
        }
        if (aspectOut != null) {
            tag.putString("aspectOut", aspectOut.getTag());
        }
    }

    @Override
    public AspectList getAspects() {
        AspectList al = new AspectList();
        if (aspectOut != null) {
            al.add(aspectOut, 1);
        }
        return al;
    }

    @Override
    public void setAspects(AspectList aspects) {
    }

    /** markAndNotifyBlock in the original: save and send the change to clients. */
    private void changed() {
        setChanged();
        syncTile(false);
    }

    @Override
    public int addToContainer(Aspect tt, int am) {
        if (am > 0 && aspectOut == null) {
            aspectOut = tt;
            changed();
            --am;
        }
        return am;
    }

    @Override
    public boolean takeFromContainer(Aspect tt, int am) {
        if (aspectOut != null && tt == aspectOut) {
            aspectOut = null;
            changed();
            return true;
        }
        return false;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect tag, int amt) {
        return amt == 1 && tag == aspectOut;
    }

    @Override
    public int containerContains(Aspect tag) {
        return tag == aspectOut ? 1 : 0;
    }

    @Override
    public boolean doesContainerAccept(Aspect tag) {
        return true;
    }

    @Override
    public boolean isConnectable(@Nullable Direction face) {
        return face == Direction.UP || face == Direction.DOWN;
    }

    @Override
    public boolean canInputFrom(@Nullable Direction face) {
        return face == Direction.DOWN;
    }

    @Override
    public boolean canOutputTo(@Nullable Direction face) {
        return face == Direction.UP;
    }

    @Override
    public void setSuction(@Nullable Aspect aspect, int amount) {
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Nullable
    @Override
    public Aspect getSuctionType(@Nullable Direction face) {
        return null;
    }

    @Override
    public int getSuctionAmount(@Nullable Direction face) {
        return face == Direction.DOWN ? (gettingPower() ? 0 : (aspectIn == null ? 128 : 64)) : 0;
    }

    @Nullable
    @Override
    public Aspect getEssentiaType(@Nullable Direction loc) {
        return aspectOut;
    }

    @Override
    public int getEssentiaAmount(@Nullable Direction loc) {
        return aspectOut != null ? 1 : 0;
    }

    @Override
    public int takeEssentia(Aspect aspect, int amount, Direction face) {
        return canOutputTo(face) && takeFromContainer(aspect, amount) ? amount : 0;
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, Direction face) {
        if (aspectIn == null && !aspect.isPrimal()) {
            aspectIn = aspect;
            process = 39;
            changed();
            return 1;
        }
        return 0;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TileCentrifuge te) {
        te.update();
    }

    private void update() {
        if (!level.isClientSide) {
            if (!gettingPower()) {
                if (aspectOut == null && aspectIn == null && ++count % 5 == 0) {
                    drawEssentia();
                }
                if (process > 0) {
                    --process;
                }
                if (aspectOut == null && aspectIn != null && process == 0) {
                    processEssentia();
                }
            }
        } else {
            if (aspectIn != null && !gettingPower() && rotationSpeed < 20.0f) {
                rotationSpeed += 2.0f;
            }
            if ((aspectIn == null || gettingPower()) && rotationSpeed > 0.0f) {
                rotationSpeed -= 0.5f;
            }
            int pr = (int) rotation;
            rotation += rotationSpeed;
            if (rotation % 180.0f <= 20.0f && pr % 180 >= 160 && rotationSpeed > 0.0f) {
                level.playLocalSound(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, SoundsTC.PUMP.get(),
                        SoundSource.BLOCKS, 1.0f, 1.0f, false);
            }
        }
    }

    void processEssentia() {
        Aspect[] comps = aspectIn.getComponents();
        aspectOut = comps[level.random.nextInt(2)];
        aspectIn = null;
        changed();
    }

    void drawEssentia() {
        BlockEntity te = ThaumcraftApiHelper.getConnectableTile(level, worldPosition, Direction.DOWN);
        if (te != null) {
            IEssentiaTransport ic = (IEssentiaTransport) te;
            if (!ic.canOutputTo(Direction.UP)) {
                return;
            }
            Aspect ta = null;
            if (ic.getEssentiaAmount(Direction.UP) > 0 && ic.getSuctionAmount(Direction.UP) < getSuctionAmount(Direction.DOWN)
                    && getSuctionAmount(Direction.DOWN) >= ic.getMinimumSuction()) {
                ta = ic.getEssentiaType(Direction.UP);
            }
            if (ta != null && !ta.isPrimal() && ic.getSuctionAmount(Direction.UP) < getSuctionAmount(Direction.DOWN)
                    && ic.takeEssentia(ta, 1, Direction.UP) == 1) {
                aspectIn = ta;
                process = 39;
                changed();
            }
        }
    }
}
