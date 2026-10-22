package thaumcraft.api.nodes;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import thaumcraft.api.aspects.AspectList;

/**
 * A node as an item carries it: the "nodetype", "nodemod" and "nodeid" NBT tags and the aspects of the Thaumcraft 4
 * ItemJarNode. modifier is -1 for none.
 */
public record NodeData(int type, int modifier, AspectList aspects, AspectList base, String id) {
    public static final Codec<NodeData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.fieldOf("type").forGetter(NodeData::type),
            Codec.INT.optionalFieldOf("modifier", -1).forGetter(NodeData::modifier),
            AspectList.CODEC.fieldOf("aspects").forGetter(NodeData::aspects),
            AspectList.CODEC.fieldOf("base").forGetter(NodeData::base),
            Codec.STRING.optionalFieldOf("id", "").forGetter(NodeData::id)
    ).apply(i, NodeData::new));
    public static final StreamCodec<ByteBuf, NodeData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, NodeData::type,
            ByteBufCodecs.VAR_INT, NodeData::modifier,
            AspectList.STREAM_CODEC, NodeData::aspects,
            AspectList.STREAM_CODEC, NodeData::base,
            ByteBufCodecs.STRING_UTF8, NodeData::id,
            NodeData::new);

    public NodeType nodeType() {
        return NodeType.byId(type);
    }

    @Nullable
    public NodeModifier nodeModifier() {
        return NodeModifier.byId(modifier);
    }

    public static NodeData of(INode node) {
        return new NodeData(node.getNodeType().ordinal(), node.getNodeModifier() == null ? -1 : node.getNodeModifier().ordinal(),
                node.getAspects().copy(), node.getAspectsBase().copy(), node.getId());
    }

    /** Gives a node the type, brightness and aspects held here. */
    public void applyTo(INode node) {
        node.setNodeType(nodeType());
        node.setNodeModifier(nodeModifier());
        node.setAspects(base.copy());
        for (thaumcraft.api.aspects.Aspect a : base.getAspects()) {
            int diff = base.getAmount(a) - aspects.getAmount(a);
            if (diff > 0) node.takeFromContainer(a, diff);
        }
    }
}
