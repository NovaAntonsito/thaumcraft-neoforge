package thaumcraft.common.tiles.essentia;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.tiles.devices.TileBellows;

/**
 * Port of TileTubeBuffer. Stores up to 10 points of any mix of essentia with the weakest possible suction (1),
 * or 32 per bellows pointed at it. Each side can be choked with a caster: 1 (blue band) or 0 (red band).
 */
public class TileTubeBuffer extends TileTube implements IAspectContainer {
    public AspectList aspects = new AspectList();
    public final int MAXAMOUNT = 10;
    public byte[] chokedSides = {0, 0, 0, 0, 0, 0};
    int bellows = -1;

    public TileTubeBuffer(BlockPos pos, BlockState state) {
        super(TCBlockEntities.TUBE_BUFFER.get(), pos, state);
    }

    @Override
    protected void readSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        aspects = AspectList.CODEC.parse(NbtOps.INSTANCE, tag.get("Aspects")).result().orElseGet(AspectList::new);
        byte[] sides = tag.getByteArray("open");
        if (sides.length == 6) {
            for (int a = 0; a < 6; ++a) {
                openSides[a] = sides[a] == 1;
            }
        }
        chokedSides = tag.getByteArray("choke");
        if (chokedSides.length < 6) {
            chokedSides = new byte[]{0, 0, 0, 0, 0, 0};
        }
        facing = Direction.from3DDataValue(tag.getInt("side"));
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        AspectList.CODEC.encodeStart(NbtOps.INSTANCE, aspects).result().ifPresent(t -> tag.put("Aspects", t));
        byte[] sides = new byte[6];
        for (int a = 0; a < 6; ++a) {
            sides[a] = (byte) (openSides[a] ? 1 : 0);
        }
        tag.putByteArray("open", sides);
        tag.putByteArray("choke", chokedSides);
        tag.putInt("side", facing.get3DDataValue());
    }

    @Override
    public AspectList getAspects() {
        return aspects;
    }

    @Override
    public void setAspects(AspectList aspects) {
    }

    @Override
    public int addToContainer(Aspect tt, int am) {
        if (am != 1) {
            return am;
        }
        if (aspects.visSize() < MAXAMOUNT) {
            aspects.add(tt, am);
            setChanged();
            syncTile(false);
            return 0;
        }
        return am;
    }

    @Override
    public boolean takeFromContainer(Aspect tt, int am) {
        if (aspects.getAmount(tt) >= am) {
            aspects.remove(tt, am);
            setChanged();
            syncTile(false);
            return true;
        }
        return false;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect tag, int amt) {
        return aspects.getAmount(tag) >= amt;
    }

    @Override
    public int containerContains(Aspect tag) {
        return aspects.getAmount(tag);
    }

    @Override
    public boolean doesContainerAccept(Aspect tag) {
        return true;
    }

    @Override
    public boolean isConnectable(@Nullable Direction face) {
        return face != null && openSides[face.get3DDataValue()];
    }

    @Override
    public boolean canInputFrom(@Nullable Direction face) {
        return face != null && openSides[face.get3DDataValue()];
    }

    @Override
    public boolean canOutputTo(@Nullable Direction face) {
        return face != null && openSides[face.get3DDataValue()];
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
    public Aspect getSuctionType(@Nullable Direction loc) {
        return null;
    }

    @Override
    public int getSuctionAmount(@Nullable Direction loc) {
        if (loc == null) {
            return bellows <= 0 ? 1 : bellows * 32;
        }
        int choke = chokedSides[loc.get3DDataValue()];
        return choke == 2 ? 0 : (bellows <= 0 || choke == 1 ? 1 : bellows * 32);
    }

    @Nullable
    @Override
    public Aspect getEssentiaType(@Nullable Direction loc) {
        return aspects.size() > 0 ? aspects.getAspects()[level.random.nextInt(aspects.getAspects().length)] : null;
    }

    @Override
    public int getEssentiaAmount(@Nullable Direction loc) {
        return aspects.visSize();
    }

    @Override
    public int takeEssentia(Aspect aspect, int amount, Direction face) {
        if (!canOutputTo(face)) {
            return 0;
        }
        int suction = 0;
        BlockEntity te = ThaumcraftApiHelper.getConnectableTile(level, worldPosition, face);
        if (te != null) {
            suction = ((IEssentiaTransport) te).getSuctionAmount(face.getOpposite());
        }
        // Don't give it away if another side pulls harder for this aspect.
        for (Direction dir : Direction.values()) {
            if (!canOutputTo(dir) || dir == face) {
                continue;
            }
            te = ThaumcraftApiHelper.getConnectableTile(level, worldPosition, dir);
            if (te != null) {
                IEssentiaTransport ic = (IEssentiaTransport) te;
                int sa = ic.getSuctionAmount(dir.getOpposite());
                Aspect su = ic.getSuctionType(dir.getOpposite());
                if ((su == aspect || su == null) && suction < sa && getSuctionAmount(dir) < sa) {
                    return 0;
                }
            }
        }
        if (amount > aspects.getAmount(aspect)) {
            amount = aspects.getAmount(aspect);
        }
        return takeFromContainer(aspect, amount) ? amount : 0;
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, Direction face) {
        return canInputFrom(face) ? amount - addToContainer(aspect, amount) : 0;
    }

    @Override
    public void update() {
        ++count;
        if (bellows < 0 || count % 20 == 0) {
            getBellows();
        }
        if (!level.isClientSide && count % 5 == 0) {
            if (aspects.visSize() < MAXAMOUNT) {
                fillBuffer();
            }
        }
        if (!level.isClientSide && count % 10 == 0) {
            thaumcraft.common.blocks.essentia.BlockTube.refreshConnections(level, worldPosition);
        }
    }

    void fillBuffer() {
        for (Direction dir : Direction.values()) {
            if (!canInputFrom(dir)) {
                continue;
            }
            BlockEntity te = ThaumcraftApiHelper.getConnectableTile(level, worldPosition, dir);
            if (te != null) {
                IEssentiaTransport ic = (IEssentiaTransport) te;
                if (ic.getEssentiaAmount(dir.getOpposite()) > 0 && ic.getSuctionAmount(dir.getOpposite()) < getSuctionAmount(dir)
                        && getSuctionAmount(dir) >= ic.getMinimumSuction()) {
                    Aspect ta = ic.getEssentiaType(dir.getOpposite());
                    if (ta != null) {
                        addToContainer(ta, ic.takeEssentia(ta, 1, dir.getOpposite()));
                    }
                    return;
                }
            }
        }
    }

    public void getBellows() {
        bellows = TileBellows.getBellows(level, worldPosition, Direction.values());
    }

    @Override
    public boolean onCasterRightClick(Level world, ItemStack casterStack, Player player, BlockPos bp, Direction side, InteractionHand hand) {
        int hit = pickPart(player);
        if (hit >= 0 && hit < 6) {
            player.swing(hand);
            if (player.isShiftKeyDown()) {
                world.playLocalSound(bp.getX() + 0.5, bp.getY() + 0.5, bp.getZ() + 0.5, SoundsTC.SQUEEK.get(), SoundSource.BLOCKS, 0.6f,
                        2.0f + world.random.nextFloat() * 0.2f, false);
                if (!world.isClientSide) {
                    ++chokedSides[hit];
                    if (chokedSides[hit] > 2) {
                        chokedSides[hit] = 0;
                    }
                    setChanged();
                    syncTile(true);
                }
            } else {
                world.playLocalSound(bp.getX() + 0.5, bp.getY() + 0.5, bp.getZ() + 0.5, SoundsTC.TOOL.get(), SoundSource.BLOCKS, 0.5f,
                        0.9f + world.random.nextFloat() * 0.2f, false);
                toggleSide(hit);
            }
            return true;
        }
        return false;
    }

    @Override
    public List<AABB> getTraceableCuboids(BlockGetter world) {
        List<AABB> cuboids = new ArrayList<>(7);
        double min = 0.375;
        double max = 0.625;
        for (int i = 0; i < 6; ++i) {
            cuboids.add(null);
        }
        if (connectsTo(world, Direction.DOWN)) cuboids.set(0, new AABB(min, 0, min, max, 0.5, max));
        if (connectsTo(world, Direction.UP)) cuboids.set(1, new AABB(min, 0.5, min, max, 1, max));
        if (connectsTo(world, Direction.NORTH)) cuboids.set(2, new AABB(min, min, 0, max, max, 0.5));
        if (connectsTo(world, Direction.SOUTH)) cuboids.set(3, new AABB(min, min, 0.5, max, max, 1));
        if (connectsTo(world, Direction.WEST)) cuboids.set(4, new AABB(0, min, min, 0.5, max, max));
        if (connectsTo(world, Direction.EAST)) cuboids.set(5, new AABB(0.5, min, min, 1, max, max));
        cuboids.add(new AABB(0.25, 0.25, 0.25, 0.75, 0.75, 0.75));
        return cuboids;
    }
}
