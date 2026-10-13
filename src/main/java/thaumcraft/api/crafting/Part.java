package thaumcraft.api.crafting;

import javax.annotation.Nullable;

/**
 * Port of Part: one position of a multiblock blueprint.
 * <p>
 * source: a Block, a BlockState or a block TagKey (1.12 also took a Material or an ItemStack with metadata).<br>
 * target: null leaves the block as it is; otherwise a Block or a BlockState to place. The original "AIR" string
 * (anything other than an ItemStack) is Blocks.AIR here.
 */
public class Part {
    private Object source;
    @Nullable
    private Object target;
    private boolean opp;
    private int priority;
    private boolean applyPlayerFacing;

    public Part(Object source, @Nullable Object target, boolean opp, int priority) {
        setSource(source);
        setTarget(target);
        setOpp(opp);
        setPriority(priority);
    }

    public Part(Object source, @Nullable Object target, boolean opp) {
        this(source, target, opp, 50);
    }

    public Part(Object source, @Nullable Object target) {
        this(source, target, false, 50);
    }

    public Object getSource() {
        return source;
    }

    public void setSource(Object source) {
        this.source = source;
    }

    @Nullable
    public Object getTarget() {
        return target;
    }

    public void setTarget(@Nullable Object target) {
        this.target = target;
    }

    public boolean isOpp() {
        return opp;
    }

    public void setOpp(boolean opp) {
        this.opp = opp;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean getApplyPlayerFacing() {
        return applyPlayerFacing;
    }

    public Part setApplyPlayerFacing(boolean applyFacing) {
        applyPlayerFacing = applyFacing;
        return this;
    }
}
