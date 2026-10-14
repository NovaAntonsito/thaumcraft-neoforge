package thaumcraft.common.entities.projectile;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import thaumcraft.common.registry.TCEntities;

/** Port of EntityGolemDart: the dart golem's arrow. It cannot be picked up (its shooter is no player). */
public class EntityGolemDart extends AbstractArrow {
    public EntityGolemDart(EntityType<? extends EntityGolemDart> type, Level level) {
        super(type, level);
    }

    public EntityGolemDart(Level level, LivingEntity shooter) {
        super(TCEntities.GOLEM_DART.get(), shooter, level, new ItemStack(Items.ARROW), null);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
    }
}
