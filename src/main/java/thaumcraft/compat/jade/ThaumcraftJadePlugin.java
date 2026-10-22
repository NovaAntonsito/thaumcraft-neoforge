package thaumcraft.compat.jade;

import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import thaumcraft.Thaumcraft;
import thaumcraft.client.gui.AnomalyHud;

/**
 * Jade: what Waila showed for Thaumcraft 4. The essentia in jars, tubes and other containers (and the suction of a
 * tube when looking closer), and, with goggles of revealing, the vis and flux of the chunk and the aspects of a
 * block, mob or dropped item already scanned. Not in Thaumcraft 6.
 */
@WailaPlugin(Thaumcraft.MODID)
public class ThaumcraftJadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(EssentiaData.INSTANCE, BlockEntity.class);
        registration.registerBlockDataProvider(AuraData.INSTANCE, Block.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(EssentiaComponent.INSTANCE, Block.class);
        registration.registerBlockComponent(AuraComponent.INSTANCE, Block.class);
        registration.registerBlockComponent(BlockAspectsComponent.INSTANCE, Block.class);
        registration.registerEntityComponent(EntityAspectsComponent.INSTANCE, Entity.class);
        // Jade's tooltip sits at the top middle, where the anomaly HUD is: move it below the panel.
        registration.addBeforeRenderCallback((box, rect, g, accessor) -> {
            Rect2i r = rect.rect;
            int width = Math.round(r.getWidth() * Math.max(1.0f, rect.scale));
            int bottom = AnomalyHud.bottomOver(r.getX(), r.getX() + width);
            if (bottom > 0 && r.getY() < bottom + 2) {
                r.setY(bottom + 2);
            }
            return false;
        });
    }
}
