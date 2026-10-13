package thaumcraft.api.research;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import thaumcraft.api.ThaumcraftApi;

/** Unlocks a research key when an entity of the given class (optionally with some NBT values) is scanned. */
public class ScanEntity implements IScanThing {
    String research;
    Class<?> entityClass;
    ThaumcraftApi.EntityTagsNBT[] NBTData;

    /** false if the specific entity class should be used, or true if anything that inherits from that class is also allowed. */
    boolean inheritedClasses = false;

    public ScanEntity(String research, Class<?> entityClass, boolean inheritedClasses) {
        this.research = research;
        this.entityClass = entityClass;
        this.inheritedClasses = inheritedClasses;
    }

    public ScanEntity(String research, Class<?> entityClass, boolean inheritedClasses, ThaumcraftApi.EntityTagsNBT... nbt) {
        this(research, entityClass, inheritedClasses);
        NBTData = nbt;
    }

    @Override
    public boolean checkThing(Player player, Object obj) {
        if (obj != null && ((!inheritedClasses && entityClass == obj.getClass()) || (inheritedClasses && entityClass.isInstance(obj)))) {
            if (NBTData != null && NBTData.length > 0) {
                CompoundTag tc = ((Entity) obj).saveWithoutId(new CompoundTag());
                for (ThaumcraftApi.EntityTagsNBT nbt : NBTData) {
                    if (!nbt.matches(tc)) return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public String getResearchKey(Player player, Object object) {
        return research;
    }
}
