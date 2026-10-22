package thaumcraft.compat.jade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.common.tiles.essentia.TileJarFillable;

/** Sends the essentia a container holds, a jar's label and, when looking closer, a tube's suction. */
public enum EssentiaData implements IServerDataProvider<BlockAccessor> {
    INSTANCE;

    public static final ResourceLocation UID = Thaumcraft.id("essentia");
    static final String ESSENTIA = "tc_essentia";
    static final String FILTER = "tc_filter";
    static final String SUCTION = "tc_suction";
    static final String SUCTION_TYPE = "tc_suction_type";

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        BlockEntity be = accessor.getBlockEntity();
        AspectList essentia = new AspectList();
        if (be instanceof IAspectContainer container && container.getAspects() != null) {
            essentia.add(container.getAspects());
        }
        if (be instanceof IEssentiaTransport transport) {
            Aspect type = transport.getEssentiaType(null);
            int amount = transport.getEssentiaAmount(null);
            if (essentia.isEmpty() && type != null && amount > 0) {
                essentia.add(type, amount);
            }
            if (accessor.showDetails() && transport.getSuctionAmount(null) > 0) {
                data.putInt(SUCTION, transport.getSuctionAmount(null));
                Aspect st = transport.getSuctionType(null);
                if (st != null) data.putString(SUCTION_TYPE, st.getTag());
            }
        }
        if (!essentia.isEmpty()) {
            AspectList.CODEC.encodeStart(NbtOps.INSTANCE, essentia).result().ifPresent(t -> data.put(ESSENTIA, t));
        }
        if (be instanceof TileJarFillable jar && jar.aspectFilter != null) {
            data.putString(FILTER, jar.aspectFilter.getTag());
        }
    }

    /** Only for blocks that hold or move essentia. */
    @Override
    public boolean shouldRequestData(BlockAccessor accessor) {
        BlockEntity be = accessor.getBlockEntity();
        return be instanceof IAspectContainer || be instanceof IEssentiaTransport;
    }

}
