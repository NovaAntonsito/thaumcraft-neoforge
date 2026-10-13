package thaumcraft.common.tiles.crafting;

import java.util.HashMap;

import net.minecraft.nbt.CompoundTag;
import thaumcraft.api.casters.FocusEngine;
import thaumcraft.api.casters.FocusNode;
import thaumcraft.api.casters.IFocusElement;

/** One cell of the focal manipulator grid: its position, links and the focus node it holds. */
public class FocusElementNode {
    public int x;
    public int y;
    public int id;
    public boolean target = false;
    public boolean trajectory = false;
    public int parent = -1;
    public int[] children = new int[0];
    public float complexityMultiplier = 1.0f;
    public FocusNode node = null;

    public float getPower(HashMap<Integer, FocusElementNode> data) {
        if (node == null) {
            return 1.0f;
        }
        float pow = node.getPowerMultiplier();
        FocusElementNode p = data.get(parent);
        if (p != null && p.node != null) {
            pow *= p.getPower(data);
        }
        return pow;
    }

    public void deserialize(CompoundTag nbt) {
        x = nbt.getInt("x");
        y = nbt.getInt("y");
        id = nbt.getInt("id");
        target = nbt.getBoolean("target");
        trajectory = nbt.getBoolean("trajectory");
        parent = nbt.getInt("parent");
        children = nbt.getIntArray("children");
        complexityMultiplier = nbt.getFloat("complexity");
        IFocusElement fe = FocusEngine.getElement(nbt.getString("key"));
        if (fe instanceof FocusNode fn) {
            node = fn;
            fn.initialize();
            if (fn.getSettingList() != null) {
                for (String ns : fn.getSettingList()) {
                    fn.getSetting(ns).setValue(nbt.getInt("setting." + ns));
                }
            }
        }
    }

    public CompoundTag serialize() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("x", x);
        nbt.putInt("y", y);
        nbt.putInt("id", id);
        nbt.putBoolean("target", target);
        nbt.putBoolean("trajectory", trajectory);
        nbt.putInt("parent", parent);
        nbt.putIntArray("children", children);
        nbt.putFloat("complexity", complexityMultiplier);
        if (node != null) {
            nbt.putString("key", node.getKey());
            if (node.getSettingList() != null) {
                for (String ns : node.getSettingList()) {
                    nbt.putInt("setting." + ns, node.getSettingValue(ns));
                }
            }
        }
        return nbt;
    }
}
