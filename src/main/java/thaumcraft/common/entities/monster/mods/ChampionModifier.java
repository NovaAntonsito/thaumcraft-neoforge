package thaumcraft.common.entities.monster.mods;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import thaumcraft.Thaumcraft;

public class ChampionModifier {
    String name;
    public int id;
    /** -1 never runs, 0 every tick, 1 when the champion hurts something, 2 when something hurts the champion. */
    public int type;
    public IChampionModifierEffect effect;
    public AttributeModifier attributeMod;
    public static final ChampionModifier[] mods;
    public static final AttributeModifier ATTRIBUTE_MOD_NONE;
    public static final AttributeModifier ATTRIBUTE_MINUS_ONE;

    public ChampionModifier(int id, String name, int type, IChampionModifierEffect effect) {
        this.name = name;
        this.id = id;
        this.type = type;
        this.effect = effect;
        attributeMod = new AttributeModifier(rl("champion_" + name), id + 2, AttributeModifier.Operation.ADD_VALUE);
    }

    public Component getModNameLocalized() {
        return Component.translatable("champion.mod." + name);
    }

    private static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(Thaumcraft.MODID, path);
    }

    static {
        mods = new ChampionModifier[] {
                new ChampionModifier(0, "bold", -1, new ChampionModBold()),
                new ChampionModifier(1, "spine", 2, new ChampionModSpined()),
                new ChampionModifier(2, "armor", 2, new ChampionModArmored()),
                new ChampionModifier(3, "mighty", -1, new ChampionModMighty()),
                new ChampionModifier(4, "grim", 1, new ChampionModGrim()),
                new ChampionModifier(5, "warded", 0, new ChampionModWarded()),
                new ChampionModifier(6, "warp", 1, new ChampionModWarp()),
                new ChampionModifier(7, "undying", 0, new ChampionModUndying()),
                new ChampionModifier(8, "fiery", 1, new ChampionModFire()),
                new ChampionModifier(9, "sickly", 1, new ChampionModSickly()),
                new ChampionModifier(10, "venomous", 1, new ChampionModPoison()),
                new ChampionModifier(11, "vampiric", 1, new ChampionModVampire()),
                new ChampionModifier(12, "infested", 2, new ChampionModInfested()),
                new ChampionModifier(13, "tainted", 0, new ChampionModTainted()) };
        ATTRIBUTE_MOD_NONE = new AttributeModifier(rl("champion_normal"), 1.0, AttributeModifier.Operation.ADD_VALUE);
        ATTRIBUTE_MINUS_ONE = new AttributeModifier(rl("champion_minus1"), -1.0, AttributeModifier.Operation.ADD_VALUE);
    }
}
