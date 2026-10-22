package thaumcraft.compat.jade;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.api.research.ScanningManager;
import thaumcraft.common.lib.research.ScanManager;
import thaumcraft.common.lib.utils.EntityUtils;

/** The aspects of a block already scanned, with goggles of revealing. Containers show their essentia instead. */
public enum BlockAspectsComponent implements IBlockComponentProvider {
    INSTANCE;

    private static final ResourceLocation UID = Thaumcraft.id("block_aspects");

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        Player player = accessor.getPlayer();
        if (!EntityUtils.hasGoggles(player) || accessor.getBlockEntity() instanceof IAspectContainer
                || accessor.getBlockEntity() instanceof IEssentiaTransport
                || ScanningManager.isThingStillScannable(player, accessor.getPosition())) {
            return;
        }
        AspectList tags = ScanManager.objScanAspects(player, accessor.getPosition());
        if (tags != null && !tags.isEmpty()) {
            tooltip.add(new AspectsElement(tags));
        }
    }
}
