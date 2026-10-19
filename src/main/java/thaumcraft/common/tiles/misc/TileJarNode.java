package thaumcraft.common.tiles.misc;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.nodes.INode;
import thaumcraft.api.nodes.NodeData;
import thaumcraft.api.nodes.NodeModifier;
import thaumcraft.api.nodes.NodeType;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.registry.TCDataComponents;
import thaumcraft.common.tiles.TileThaumcraft;

/**
 * Port of the Thaumcraft 4 TileJarNode: an aura node trapped in a jar. It is frozen as it was caught: it does not
 * regenerate, drain or act. A wand opens it back into a node; broken, the jar keeps the node as an item.
 */
public class TileJarNode extends TileThaumcraft implements INode, thaumcraft.api.items.IGogglesDisplayExtended {
    private AspectList aspects = new AspectList();
    private AspectList aspectsBase = new AspectList();
    private NodeType nodeType = NodeType.NORMAL;
    @Nullable
    private NodeModifier nodeModifier = null;
    private String id = "";
    /** Client side: when the capture sparkle ends (System.currentTimeMillis). */
    public long animate = 0L;

    public TileJarNode(BlockPos pos, BlockState state) {
        super(TCBlockEntities.JAR_NODE.get(), pos, state);
    }

    @Override
    protected void readSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        aspects = AspectList.CODEC.parse(NbtOps.INSTANCE, tag.get("Aspects")).result().orElseGet(AspectList::new);
        aspectsBase = AspectList.CODEC.parse(NbtOps.INSTANCE, tag.get("AspectsBase")).result().orElseGet(AspectList::new);
        id = tag.getString("nodeId");
        nodeType = NodeType.byId(tag.getByte("type"));
        nodeModifier = NodeModifier.byId(tag.getByte("modifier"));
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        AspectList.CODEC.encodeStart(NbtOps.INSTANCE, aspects).result().ifPresent(t -> tag.put("Aspects", t));
        AspectList.CODEC.encodeStart(NbtOps.INSTANCE, aspectsBase).result().ifPresent(t -> tag.put("AspectsBase", t));
        tag.putString("nodeId", id);
        tag.putByte("type", (byte) nodeType.ordinal());
        tag.putByte("modifier", (byte) (nodeModifier == null ? -1 : nodeModifier.ordinal()));
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput input) {
        super.applyImplicitComponents(input);
        NodeData data = input.get(TCDataComponents.NODE_DATA.get());
        if (data != null) {
            aspects = data.aspects().copy();
            aspectsBase = data.base().copy();
            nodeType = data.nodeType();
            nodeModifier = data.nodeModifier();
            id = data.id();
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (aspects.size() > 0) {
            components.set(TCDataComponents.NODE_DATA.get(), NodeData.of(this));
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void removeComponentsFromTag(CompoundTag tag) {
        tag.remove("Aspects");
        tag.remove("AspectsBase");
        tag.remove("nodeId");
        tag.remove("type");
        tag.remove("modifier");
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public AspectList getAspects() {
        return aspects;
    }

    @Override
    public AspectList getAspectsBase() {
        return aspectsBase;
    }

    @Override
    public void setAspects(AspectList aspects) {
        this.aspects = aspects.copy();
        this.aspectsBase = aspects.copy();
    }

    @Override
    public int addToContainer(Aspect tt, int am) {
        int out = 0;
        if (aspects.getAmount(tt) + am > aspectsBase.getAmount(tt)) {
            out = aspects.getAmount(tt) + am - aspectsBase.getAmount(tt);
        }
        aspects.add(tt, am - out);
        setChanged();
        syncTile(false);
        return out;
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
    public NodeType getNodeType() {
        return nodeType;
    }

    @Override
    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    @Override
    public void setNodeModifier(@Nullable NodeModifier nodeModifier) {
        this.nodeModifier = nodeModifier;
    }

    @Nullable
    @Override
    public NodeModifier getNodeModifier() {
        return nodeModifier;
    }

    @Override
    public int getNodeVisBase(Aspect aspect) {
        return aspectsBase.getAmount(aspect);
    }

    @Override
    public void setNodeVisBase(Aspect aspect, short nodeVisBase) {
        if (aspectsBase.getAmount(aspect) < nodeVisBase) {
            aspectsBase.merge(aspect, nodeVisBase);
        } else {
            aspectsBase.reduce(aspect, aspectsBase.getAmount(aspect) - nodeVisBase);
        }
    }

    /** receiveClientEvent 9: the jar was just made around the node. */
    @Override
    public boolean triggerEvent(int id, int type) {
        if (id == 9) {
            if (level != null && level.isClientSide) animate = System.currentTimeMillis() + 1000L;
            return true;
        }
        return super.triggerEvent(id, type);
    }

    @Override
    public java.util.List<net.minecraft.network.chat.Component> getIGogglesText() {
        return TileAuraNode.describe(nodeType, nodeModifier);
    }
}
