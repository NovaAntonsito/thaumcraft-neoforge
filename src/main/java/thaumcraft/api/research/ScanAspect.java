package thaumcraft.api.research;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectHelper;
import thaumcraft.api.aspects.AspectList;

/**
 * Unlocks a research key when something with the aspect is scanned. A trigger is added automatically for each
 * aspect in the format "![tag]", for example "!vitium".
 */
public class ScanAspect implements IScanThing {
    String research;
    Aspect aspect;

    public ScanAspect(String research, Aspect aspect) {
        this.research = research;
        this.aspect = aspect;
    }

    @Override
    public boolean checkThing(Player player, Object obj) {
        if (obj == null) return false;
        AspectList al;
        if (obj instanceof Entity e && !(obj instanceof ItemEntity)) {
            al = AspectHelper.getEntityAspects(e);
        } else {
            ItemStack is = ScanningManager.getItemFromParms(player, obj);
            al = is.isEmpty() ? null : AspectHelper.getObjectAspects(is);
        }
        return al != null && al.getAmount(aspect) > 0;
    }

    /** The original also gave a point of observation in Auromancy, Basics and Alchemy; the TC4 notes replace observation. */
    @Override
    public void onSuccess(Player player, Object obj) {
    }

    @Override
    public String getResearchKey(Player player, Object object) {
        return research;
    }
}
