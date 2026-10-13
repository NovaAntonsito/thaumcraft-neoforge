package thaumcraft.common.lib.research;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import thaumcraft.Config;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectHelper;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.IScanThing;
import thaumcraft.api.research.ScanningManager;

/**
 * Anything with aspects. The first scan of each thing gives aspect points: the original gave observation knowledge
 * per category, Reborn's hookScanAspectGeneric gives each aspect times aspectObtainMultiplier.
 */
public class ScanGeneric implements IScanThing {

    @Override
    public boolean checkThing(Player player, Object obj) {
        AspectList al = ScanManager.objScanAspects(player, obj);
        return al != null && al.size() > 0;
    }

    @Override
    public void onSuccess(Player player, Object obj) {
        if (obj == null || !(player instanceof ServerPlayer sp)) return;
        AspectList al = ScanManager.objScanAspects(player, obj);
        if (al != null) {
            double mult = Config.ASPECT_OBTAIN_MULTIPLIER.get();
            for (Aspect aspect : al.getAspects()) {
                ResearchNoteManager.checkAndSyncAspectKnowledge(sp, aspect, (int) Math.round(al.getAmount(aspect) * mult));
            }
        }
    }

    /** "!" and the registry name; the 1.12 key added the metadata of undamageable items. */
    @Override
    public String getResearchKey(Player player, Object obj) {
        if (obj instanceof Entity e && !(obj instanceof ItemEntity)) {
            return "!" + BuiltInRegistries.ENTITY_TYPE.getKey(e.getType());
        }
        ItemStack is = ScanningManager.getItemFromParms(player, obj);
        if (!is.isEmpty()) {
            return "!" + BuiltInRegistries.ITEM.getKey(is.getItem());
        }
        return null;
    }
}
