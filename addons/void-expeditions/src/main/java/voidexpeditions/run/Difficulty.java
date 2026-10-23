package voidexpeditions.run;

import java.util.Locale;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/** How hard an expedition is: more guardians and champions in each key room, and more loot and shards. */
public enum Difficulty {
    RESTLESS(0, 0, 0, 1, ChatFormatting.LIGHT_PURPLE),
    ABYSSAL(1, 1, 1, 2, ChatFormatting.DARK_PURPLE),
    PRIMORDIAL(2, 3, 2, 4, ChatFormatting.DARK_RED);

    /** Guardians added to every key room. */
    public final int extraGuardians;
    /** How many of a key room's guardians are champions. */
    public final int champions;
    /** Encounters added to the depth that picks the loot of each key room's chest. */
    public final int lootBonus;
    /** Void shards per encounter of a finished run. */
    public final int shardsPerEncounter;
    public final ChatFormatting color;

    Difficulty(int extraGuardians, int champions, int lootBonus, int shardsPerEncounter, ChatFormatting color) {
        this.extraGuardians = extraGuardians;
        this.champions = champions;
        this.lootBonus = lootBonus;
        this.shardsPerEncounter = shardsPerEncounter;
        this.color = color;
    }

    public Component displayName() {
        return Component.translatable("voidexpeditions.difficulty." + name().toLowerCase(Locale.ROOT)).withStyle(color);
    }

    public static Difficulty byIndex(int i) {
        Difficulty[] all = values();
        return all[Math.floorMod(i, all.length)];
    }
}
