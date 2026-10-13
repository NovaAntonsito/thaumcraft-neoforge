package thaumcraft.common.lib.events;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.item.ItemExpireEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.items.consumables.ItemBathSalts;

/** Partial port of EntityEvents: bath salts that dissolve in a still water source turn it into purifying fluid. */
@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class EntityEvents {
    private EntityEvents() {
    }

    @SubscribeEvent
    public static void itemExpire(ItemExpireEvent event) {
        ItemEntity item = event.getEntity();
        if (item.getItem().getItem() instanceof ItemBathSalts) {
            Level level = item.level();
            BlockPos bp = item.blockPosition();
            BlockState bs = level.getBlockState(bp);
            if (bs.is(Blocks.WATER) && bs.getFluidState().isSource()) {
                level.setBlockAndUpdate(bp, ConfigBlocks.purifyingFluid.get().defaultBlockState());
            }
        }
    }
}
