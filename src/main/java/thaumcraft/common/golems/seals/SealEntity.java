package thaumcraft.common.golems.seals;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import thaumcraft.api.golems.seals.ISeal;
import thaumcraft.api.golems.seals.ISealConfigArea;
import thaumcraft.api.golems.seals.ISealConfigToggles;
import thaumcraft.api.golems.seals.ISealEntity;
import thaumcraft.api.golems.seals.SealPos;
import thaumcraft.api.golems.tasks.Task;
import thaumcraft.common.golems.tasks.TaskHandler;

/** Port of SealEntity: one placed seal and its settings. */
public class SealEntity implements ISealEntity {
    SealPos sealPos;
    ISeal seal;
    byte priority = 0;
    byte color = 0;
    boolean locked = false;
    boolean redstone = false;
    String owner = "";
    boolean stopped = false;
    private BlockPos area = new BlockPos(1, 1, 1);

    public SealEntity() {
    }

    public SealEntity(Level world, SealPos sealPos, ISeal seal) {
        this.sealPos = sealPos;
        this.seal = seal;
        if (seal instanceof ISealConfigArea) {
            int x = sealPos.face.getStepX() == 0 ? 3 : 1;
            int y = sealPos.face.getStepY() == 0 ? 3 : 1;
            int z = sealPos.face.getStepZ() == 0 ? 3 : 1;
            area = new BlockPos(x, y, z);
        }
    }

    @Override
    public void tickSealEntity(Level world) {
        if (seal != null) {
            if (isStoppedByRedstone(world)) {
                if (!stopped) {
                    for (Task t : TaskHandler.getTasks(world.dimension()).values()) {
                        if (t.getSealPos() != null && t.getSealPos().equals(sealPos)) {
                            t.setSuspended(true);
                        }
                    }
                }
                stopped = true;
                return;
            }
            stopped = false;
            seal.tickSeal(world, this);
        }
    }

    /** isBlockPowered in 1.12: a direct signal into the block. */
    @Override
    public boolean isStoppedByRedstone(Level world) {
        return isRedstoneSensitive()
                && (world.getDirectSignalTo(getSealPos().pos) > 0 || world.getDirectSignalTo(getSealPos().pos.relative(getSealPos().face)) > 0);
    }

    @Override
    public ISeal getSeal() {
        return seal;
    }

    @Override
    public SealPos getSealPos() {
        return sealPos;
    }

    @Override
    public byte getPriority() {
        return priority;
    }

    @Override
    public void setPriority(byte priority) {
        this.priority = priority;
    }

    @Override
    public byte getColor() {
        return color;
    }

    @Override
    public void setColor(byte color) {
        this.color = color;
    }

    @Override
    public String getOwner() {
        return owner;
    }

    @Override
    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public boolean isLocked() {
        return locked;
    }

    @Override
    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    @Override
    public boolean isRedstoneSensitive() {
        return redstone;
    }

    @Override
    public void setRedstoneSensitive(boolean redstone) {
        this.redstone = redstone;
    }

    @Override
    public void readNBT(CompoundTag nbt) {
        BlockPos p = BlockPos.of(nbt.getLong("pos"));
        Direction face = Direction.from3DDataValue(nbt.getByte("face"));
        sealPos = new SealPos(p, face);
        setPriority(nbt.getByte("priority"));
        setColor(nbt.getByte("color"));
        setLocked(nbt.getBoolean("locked"));
        setRedstoneSensitive(nbt.getBoolean("redstone"));
        setOwner(nbt.getString("owner"));
        try {
            seal = SealHandler.getSeal(nbt.getString("type")).getClass().getDeclaredConstructor().newInstance();
        } catch (Exception ex) {
        }
        if (seal != null) {
            seal.readCustomNBT(nbt);
            if (seal instanceof ISealConfigArea) {
                area = BlockPos.of(nbt.getLong("area"));
            }
            if (seal instanceof ISealConfigToggles toggles) {
                for (ISealConfigToggles.SealToggle prop : toggles.getToggles()) {
                    if (nbt.contains(prop.getKey())) {
                        prop.setValue(nbt.getBoolean(prop.getKey()));
                    }
                }
            }
        }
    }

    @Override
    public CompoundTag writeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putLong("pos", sealPos.pos.asLong());
        nbt.putByte("face", (byte) sealPos.face.ordinal());
        nbt.putString("type", seal.getKey());
        nbt.putByte("priority", getPriority());
        nbt.putByte("color", getColor());
        nbt.putBoolean("locked", isLocked());
        nbt.putBoolean("redstone", isRedstoneSensitive());
        nbt.putString("owner", getOwner());
        if (seal != null) {
            seal.writeCustomNBT(nbt);
            if (seal instanceof ISealConfigArea) {
                nbt.putLong("area", area.asLong());
            }
            if (seal instanceof ISealConfigToggles toggles) {
                for (ISealConfigToggles.SealToggle prop : toggles.getToggles()) {
                    nbt.putBoolean(prop.getKey(), prop.getValue());
                }
            }
        }
        return nbt;
    }

    /** TODO (stage 8b): PacketSealToClient. */
    @Override
    public void syncToClient(Level world) {
    }

    @Override
    public BlockPos getArea() {
        return area;
    }

    @Override
    public void setArea(BlockPos v) {
        area = v;
    }
}
