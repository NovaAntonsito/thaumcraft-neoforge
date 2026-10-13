package thaumcraft.common.items.resources;

import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssentiaContainerItem;
import thaumcraft.common.registry.TCDataComponents;

/**
 * Port of ItemCrystalEssence + ItemGenericEssentiaContainer (vis crystals).
 * The held aspect lives in the {@link TCDataComponents#ASPECTS} component instead of NBT.
 */
public class ItemCrystalEssence extends Item implements IEssentiaContainerItem {
    protected final int base;

    public ItemCrystalEssence(Properties properties) {
        super(properties);
        this.base = 1;
    }

    @Nullable
    @Override
    public AspectList getAspects(ItemStack stack) {
        AspectList aspects = stack.get(TCDataComponents.ASPECTS);
        return aspects != null && aspects.size() > 0 ? aspects : null;
    }

    @Override
    public void setAspects(ItemStack stack, AspectList aspects) {
        stack.set(TCDataComponents.ASPECTS, aspects.copy());
    }

    /** A crystal of the given aspect (ThaumcraftApiHelper.makeCrystal). */
    public ItemStack makeCrystal(Aspect aspect, int count) {
        ItemStack stack = new ItemStack(this, count);
        setAspects(stack, new AspectList().add(aspect, base));
        return stack;
    }

    /** "%s Vis Crystal" filled with the aspect's name. */
    @Override
    public Component getName(ItemStack stack) {
        AspectList aspects = getAspects(stack);
        if (aspects != null) {
            return Component.translatable(getDescriptionId(stack), aspects.getAspects()[0].getName());
        }
        return super.getName(stack);
    }

    /** A crystal without aspects picks a random one, as in the original onUpdate/onCreated. */
    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
        if (!world.isClientSide && getAspects(stack) == null) assignRandom(stack, world);
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level world, Player player) {
        if (!world.isClientSide && getAspects(stack) == null) assignRandom(stack, world);
    }

    private void assignRandom(ItemStack stack, Level world) {
        Aspect[] displayAspects = Aspect.aspects.values().toArray(new Aspect[0]);
        setAspects(stack, new AspectList().add(displayAspects[world.random.nextInt(displayAspects.length)], base));
    }
}
