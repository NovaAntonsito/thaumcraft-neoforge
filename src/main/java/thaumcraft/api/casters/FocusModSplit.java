package thaumcraft.api.casters;

import java.util.ArrayList;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public abstract class FocusModSplit extends FocusMod {

    private final ArrayList<FocusPackage> packages = new ArrayList<>();

    public ArrayList<FocusPackage> getSplitPackages() {
        return packages;
    }

    public void deserialize(CompoundTag nbt) {
        ListTag nodelist = nbt.getList("packages", Tag.TAG_COMPOUND);
        packages.clear();
        for (int x = 0; x < nodelist.size(); x++) {
            FocusPackage fp = new FocusPackage();
            fp.deserialize(nodelist.getCompound(x));
            packages.add(fp);
        }
    }

    public CompoundTag serialize() {
        CompoundTag nbt = new CompoundTag();
        ListTag nodelist = new ListTag();
        for (FocusPackage node : packages) {
            nodelist.add(node.serialize());
        }
        nbt.put("packages", nodelist);
        return nbt;
    }

    @Override
    public float getPowerMultiplier() {
        return .75f;
    }
}
