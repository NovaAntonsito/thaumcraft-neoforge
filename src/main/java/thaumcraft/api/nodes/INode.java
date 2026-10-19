package thaumcraft.api.nodes;

import javax.annotation.Nullable;

import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.aspects.Aspect;

/** Port of the Thaumcraft 4 INode: an aura node, wild or in a jar. */
public interface INode extends IAspectContainer {
    /** A unique id for the node: dimension:x:y:z. */
    String getId();

    AspectList getAspectsBase();

    NodeType getNodeType();

    void setNodeType(NodeType nodeType);

    void setNodeModifier(@Nullable NodeModifier nodeModifier);

    @Nullable
    NodeModifier getNodeModifier();

    /** The amount of the aspect the node regenerates up to. */
    int getNodeVisBase(Aspect aspect);

    void setNodeVisBase(Aspect aspect, short nodeVisBase);
}
