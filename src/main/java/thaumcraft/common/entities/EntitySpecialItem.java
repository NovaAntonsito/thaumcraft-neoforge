package thaumcraft.common.entities;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Port of TC4's EntitySpecialItem: a dropped item that floats. Its upward motion fades, and each tick it gets back
 * the 0.04 that gravity takes away. Explosions do not destroy it.
 */
public class EntitySpecialItem extends ItemEntity {
    public EntitySpecialItem(EntityType<? extends EntitySpecialItem> type, Level level) {
        super(type, level);
    }

    public EntitySpecialItem(EntityType<? extends EntitySpecialItem> type, Level level, double x, double y, double z, ItemStack stack) {
        super(type, level);
        this.setPos(x, y, z);
        this.setItem(stack);
        this.setYRot((float) (Math.random() * 360.0));
        this.setDeltaMovement(Math.random() * 0.2F - 0.1F, 0.2F, Math.random() * 0.2F - 0.1F);
    }

    @Override
    public void tick() {
        Vec3 m = this.getDeltaMovement();
        double my = m.y;
        if (my > 0.0) {
            my *= 0.9F;
        }
        my += 0.04F;
        this.setDeltaMovement(m.x, my, m.z);
        super.tick();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return !source.is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION) && super.hurt(source, amount);
    }
}
