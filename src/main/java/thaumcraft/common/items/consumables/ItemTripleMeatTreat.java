package thaumcraft.common.items.consumables;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

/**
 * Port of ItemTripleMeatTreat: 6 hunger, 0.8 saturation, always edible, and a 66% chance of 5 seconds of
 * Regeneration. Wolves eat it too (minecraft:wolf_food tag).
 */
public class ItemTripleMeatTreat extends Item {
    public static final FoodProperties FOOD = new FoodProperties.Builder().nutrition(6).saturationModifier(0.8f).alwaysEdible()
            .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 100, 0), 0.66f).build();

    public ItemTripleMeatTreat(Item.Properties properties) {
        super(properties.food(FOOD));
    }
}
