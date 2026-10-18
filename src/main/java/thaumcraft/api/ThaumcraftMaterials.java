package thaumcraft.api;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.SimpleTier;
import thaumcraft.common.config.ConfigItems;

/**
 * The tool materials of ThaumcraftMaterials. The armor materials are in TCArmorMaterials, since 1.21 registers them.
 * 1.21: the harvest level is the tag of blocks the tier can't mine (level 3 is diamond, 4 and up netherite).
 */
public final class ThaumcraftMaterials {
    /** TOOLMAT_THAUMIUM: level 3, 500 uses, speed 7, damage 2.5, enchantability 22, repaired with thaumium. */
    public static final Tier TOOLMAT_THAUMIUM = new SimpleTier(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 500, 7.0f, 2.5f, 22,
            () -> Ingredient.of(ConfigItems.ingots.get("thaumium").get()));
    /** TOOLMAT_VOID: level 4, 150 uses, speed 8, damage 3, enchantability 10, repaired with void metal. */
    public static final Tier TOOLMAT_VOID = new SimpleTier(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 150, 8.0f, 3.0f, 10,
            () -> Ingredient.of(ConfigItems.ingots.get("void").get()));
    /** TOOLMAT_ELEMENTAL: level 3, 1500 uses, speed 9, damage 3, enchantability 18, repaired with thaumium. */
    public static final Tier TOOLMAT_ELEMENTAL = new SimpleTier(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1500, 9.0f, 3.0f, 18,
            () -> Ingredient.of(ConfigItems.ingots.get("thaumium").get()));

    private ThaumcraftMaterials() {
    }
}
