package thaumcraft.api.golems;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Contains functions to allow addon devs to access golem internals
 */
public interface IGolemAPI {
    LivingEntity getGolemEntity();

    IGolemProperties getProperties();

    void setProperties(IGolemProperties prop);

    Level getGolemWorld();

    /**
     * Causes the golem to hold the itemstack supplied.
     * @return anything left over that the golem could not hold. If the golem picked up the entire stack this will be an empty stack.
     */
    ItemStack holdItem(ItemStack stack);

    /**
     * Causes the golem to remove an itemstack it is holding. It does not actually drop the item in the
     * world or place it anywhere - that is up to whatever is calling this method.
     * @param stack the itemstack that the golem will drop. If empty is supplied the golem will drop whatever it is holding
     * @return the stack it 'dropped'
     */
    ItemStack dropItem(ItemStack stack);

    /**
     * Checks if the golem has carrying capacity for the given stack
     * @param stack the stack the golem has room for - can be empty
     * @param partial does the golem only need to have room for part of the stack?
     */
    boolean canCarry(ItemStack stack, boolean partial);

    /**
     * Checks how much carrying capacity the golem has for the given stack
     * @param stack the stack the golem has room for - can be empty
     */
    int canCarryAmount(ItemStack stack);

    boolean isCarrying(ItemStack stack);

    NonNullList<ItemStack> getCarrying();

    /**
     * Gives the golem xp towards increasing its rank rating. Default is usually 1 for completing a task.
     */
    void addRankXp(int xp);

    byte getGolemColor();

    /**
     * Plays arm swinging animated for attacks and such
     */
    void swingArm();

    boolean isInCombat();
}
