package thaumcraft.compat.jade;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ScanningManager;
import thaumcraft.common.lib.research.ScanManager;

/** The aspects of a mob, or of a dropped item, already scanned. */
public enum EntityAspectsComponent implements IEntityComponentProvider {
    INSTANCE;

    private static final ResourceLocation UID = Thaumcraft.id("entity_aspects");

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        Player player = accessor.getPlayer();
        Entity entity = accessor.getEntity();
        if (ScanningManager.isThingStillScannable(player, entity)) return;
        AspectList tags = ScanManager.objScanAspects(player, entity);
        if (tags != null && !tags.isEmpty()) {
            tooltip.add(new AspectsElement(tags));
        }
    }
}
