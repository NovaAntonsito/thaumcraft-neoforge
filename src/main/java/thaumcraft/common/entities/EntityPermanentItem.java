package thaumcraft.common.entities;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import thaumcraft.common.registry.TCEntities;

/**
 * Port of TC4's EntityPermanentItem: a floating item that never despawns, like the Runed Tablet in the key room of
 * the Outer Lands. The original reset its age just before the lifespan ran out; 1.21 has an unlimited lifetime.
 */
public class EntityPermanentItem extends EntitySpecialItem {
    public EntityPermanentItem(EntityType<? extends EntityPermanentItem> type, Level level) {
        super(type, level);
        this.setUnlimitedLifetime();
    }

    public EntityPermanentItem(Level level, double x, double y, double z, ItemStack stack) {
        super(TCEntities.PERMANENT_ITEM.get(), level, x, y, z, stack);
        this.setUnlimitedLifetime();
    }
}
