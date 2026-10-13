package thaumcraft.api.research;

import net.minecraft.world.entity.player.Player;

/** Something the thaumometer can learn from. Port of thaumcraft.api.research.IScanThing. */
public interface IScanThing {

    /**
     * The passed in obj can either be an Entity, a BlockPos, an ItemStack, or null if nothing was actually clicked on.
     * You could then probably use the player's look vector to do whatever you want.
     */
    boolean checkThing(Player player, Object obj);

    /**
     * The research key that will be unlocked if the object is scanned. This need not be an actual defined research
     * item; "!" is the usual prefix for these keys, which can then be used as research requirements.
     */
    String getResearchKey(Player player, Object object);

    default void onSuccess(Player player, Object object) {
    }
}
