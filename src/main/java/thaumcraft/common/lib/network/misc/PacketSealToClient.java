package thaumcraft.common.lib.network.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thaumcraft.Thaumcraft;
import thaumcraft.api.golems.seals.ISealConfigArea;
import thaumcraft.api.golems.seals.ISealConfigFilter;
import thaumcraft.api.golems.seals.ISealConfigToggles;
import thaumcraft.api.golems.seals.ISealEntity;
import thaumcraft.api.golems.seals.SealPos;
import thaumcraft.common.golems.seals.SealEntity;
import thaumcraft.common.golems.seals.SealHandler;

/** Port of PacketSealToClient: one placed seal and its settings, or "REMOVE" when it is gone. */
public class PacketSealToClient implements CustomPacketPayload {
    public static final Type<PacketSealToClient> TYPE = new Type<>(Thaumcraft.id("seal_to_client"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketSealToClient> STREAM_CODEC =
            StreamCodec.of((buf, msg) -> msg.toBytes(buf), PacketSealToClient::fromBytes);

    BlockPos pos;
    Direction face;
    String type;
    long area;
    boolean[] props = null;
    boolean blacklist;
    byte filtersize;
    NonNullList<ItemStack> filter;
    NonNullList<Integer> filterStackSize;
    byte priority;
    byte color;
    boolean locked;
    boolean redstone;
    String owner;

    private PacketSealToClient() {
    }

    public PacketSealToClient(ISealEntity se) {
        pos = se.getSealPos().pos;
        face = se.getSealPos().face;
        type = se.getSeal() == null ? "REMOVE" : se.getSeal().getKey();
        if (se.getSeal() instanceof ISealConfigArea) {
            area = se.getArea().asLong();
        }
        if (se.getSeal() instanceof ISealConfigToggles cp) {
            props = new boolean[cp.getToggles().length];
            for (int a = 0; a < cp.getToggles().length; ++a) {
                props[a] = cp.getToggles()[a].getValue();
            }
        }
        if (se.getSeal() instanceof ISealConfigFilter cp2) {
            blacklist = cp2.isBlacklist();
            filtersize = (byte) cp2.getFilterSize();
            filter = cp2.getInv();
            filterStackSize = cp2.getSizes();
        }
        priority = se.getPriority();
        color = se.getColor();
        locked = se.isLocked();
        redstone = se.isRedstoneSensitive();
        owner = se.getOwner();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private void toBytes(RegistryFriendlyByteBuf dos) {
        dos.writeLong(pos.asLong());
        dos.writeByte(face.ordinal());
        dos.writeByte(priority);
        dos.writeByte(color);
        dos.writeBoolean(locked);
        dos.writeBoolean(redstone);
        dos.writeUtf(owner == null ? "" : owner);
        dos.writeUtf(type);
        dos.writeBoolean(blacklist);
        dos.writeByte(filtersize);
        for (int a = 0; a < filtersize; ++a) {
            ItemStack.OPTIONAL_STREAM_CODEC.encode(dos, filter.get(a));
            dos.writeShort(filterStackSize.get(a));
        }
        if (area != 0L) {
            dos.writeLong(area);
        }
        if (props != null) {
            for (boolean b : props) {
                dos.writeBoolean(b);
            }
        }
    }

    private static PacketSealToClient fromBytes(RegistryFriendlyByteBuf dat) {
        PacketSealToClient m = new PacketSealToClient();
        m.pos = BlockPos.of(dat.readLong());
        m.face = Direction.values()[dat.readByte()];
        m.priority = dat.readByte();
        m.color = dat.readByte();
        m.locked = dat.readBoolean();
        m.redstone = dat.readBoolean();
        m.owner = dat.readUtf();
        m.type = dat.readUtf();
        m.blacklist = dat.readBoolean();
        m.filtersize = dat.readByte();
        m.filter = NonNullList.withSize(m.filtersize, ItemStack.EMPTY);
        m.filterStackSize = NonNullList.withSize(m.filtersize, 0);
        for (int a = 0; a < m.filtersize; ++a) {
            m.filter.set(a, ItemStack.OPTIONAL_STREAM_CODEC.decode(dat));
            m.filterStackSize.set(a, (int) dat.readShort());
        }
        if (!m.type.equals("REMOVE") && SealHandler.getSeal(m.type) != null) {
            if (SealHandler.getSeal(m.type) instanceof ISealConfigArea && dat.readableBytes() >= 8) {
                m.area = dat.readLong();
            }
            if (SealHandler.getSeal(m.type) instanceof ISealConfigToggles cp) {
                int n = cp.getToggles().length;
                if (dat.readableBytes() >= n) {
                    m.props = new boolean[n];
                    for (int a2 = 0; a2 < n; ++a2) {
                        m.props[a2] = dat.readBoolean();
                    }
                }
            }
        }
        dat.skipBytes(dat.readableBytes());
        return m;
    }

    public static void handle(PacketSealToClient message, IPayloadContext context) {
        Level world = context.player().level();
        if (message.type.equals("REMOVE")) {
            SealHandler.removeSealEntity(world, new SealPos(message.pos, message.face), true);
            return;
        }
        try {
            SealEntity seal = new SealEntity(world, new SealPos(message.pos, message.face),
                    SealHandler.getSeal(message.type).getClass().getDeclaredConstructor().newInstance());
            if (message.area != 0L) {
                seal.setArea(BlockPos.of(message.area));
            }
            if (message.props != null && seal.getSeal() instanceof ISealConfigToggles cp) {
                for (int a = 0; a < message.props.length; ++a) {
                    cp.setToggle(a, message.props[a]);
                }
            }
            if (seal.getSeal() instanceof ISealConfigFilter cp2) {
                cp2.setBlacklist(message.blacklist);
                for (int a = 0; a < message.filtersize; ++a) {
                    cp2.setFilterSlot(a, message.filter.get(a));
                    cp2.setFilterSlotSize(a, message.filterStackSize.get(a));
                }
            }
            seal.setPriority(message.priority);
            seal.setColor(message.color);
            seal.setLocked(message.locked);
            seal.setRedstoneSensitive(message.redstone);
            seal.setOwner(message.owner);
            SealHandler.addSealEntity(world, seal);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
