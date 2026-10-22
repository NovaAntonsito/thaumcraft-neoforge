package thaumcraft.compat.jade;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import thaumcraft.Thaumcraft;

/**
 * Jade: what Waila showed for Thaumcraft 4. The essentia in jars, tubes and other containers (and the suction of a
 * tube when looking closer), and, with goggles of revealing, the vis and flux of the chunk and the aspects of a
 * block already scanned. Not in Thaumcraft 6.
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
    }
}
