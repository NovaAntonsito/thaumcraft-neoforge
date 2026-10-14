package thaumcraft.api.golems;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public enum EnumGolemTrait {
    SMART(icon("smart")),
    DEFT(icon("deft")),
    CLUMSY(icon("clumsy")),
    FIGHTER(icon("fighter")),
    WHEELED(icon("wheeled")),
    FLYER(icon("flyer")),
    CLIMBER(icon("climber")),
    HEAVY(icon("heavy")),
    LIGHT(icon("light")),
    FRAGILE(icon("fragile")),
    REPAIR(icon("repair")),
    SCOUT(icon("scout")),
    ARMORED(icon("armored")),
    BRUTAL(icon("brutal")),
    FIREPROOF(icon("fireproof")),
    BREAKER(icon("breaker")),
    HAULER(icon("hauler")),
    RANGED(icon("ranged")),
    BLASTPROOF(icon("blastproof"));

    static {
        CLUMSY.opposite = DEFT;
        DEFT.opposite = CLUMSY;
        HEAVY.opposite = LIGHT;
        LIGHT.opposite = HEAVY;
        FRAGILE.opposite = ARMORED;
        ARMORED.opposite = FRAGILE;
    }

    public ResourceLocation icon;
    public EnumGolemTrait opposite;

    EnumGolemTrait(ResourceLocation icon) {
        this.icon = icon;
    }

    private static ResourceLocation icon(String name) {
        return ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/golem/tag_" + name + ".png");
    }

    public Component getLocalizedName() {
        return Component.translatable("golem.trait." + name().toLowerCase());
    }

    public Component getLocalizedDescription() {
        return Component.translatable("golem.trait.text." + name().toLowerCase());
    }
}
