package thaumcraft.common.tiles.crafting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.common.container.InventoryArcaneWorkbench;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.world.aura.AuraHandler;

/** Port of TileArcaneWorkbench. Holds the grid and crystals; draws vis from the aura of its chunk. */
public class TileArcaneWorkbench extends BlockEntity {
    public final InventoryArcaneWorkbench inventoryCraft = new InventoryArcaneWorkbench(this);
    public int auraVisServer;
    public int auraVisClient;

    public TileArcaneWorkbench(BlockPos pos, BlockState state) {
        super(TCBlockEntities.ARCANE_WORKBENCH.get(), pos, state);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        NonNullList<ItemStack> stacks = NonNullList.withSize(inventoryCraft.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, stacks, registries);
        for (int a = 0; a < stacks.size(); ++a) {
            inventoryCraft.setItem(a, stacks.get(a));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        NonNullList<ItemStack> stacks = NonNullList.withSize(inventoryCraft.getContainerSize(), ItemStack.EMPTY);
        for (int a = 0; a < stacks.size(); ++a) {
            stacks.set(a, inventoryCraft.getItem(a));
        }
        ContainerHelper.saveAllItems(tag, stacks, registries);
    }

    /**
     * Vis available to the workbench.
     * TODO: with a workbench charger on top it reads the 3x3 chunks around it; the charger is not ported yet.
     */
    public void getAura() {
        if (level != null && !level.isClientSide) {
            auraVisServer = (int) AuraHandler.getVis(level, worldPosition);
        }
    }

    /** TODO: the charger variant drains the 3x3 chunks around it in steps. */
    public void spendAura(int vis) {
        if (level != null && !level.isClientSide) {
            AuraHandler.drainVis(level, worldPosition, vis, false);
        }
    }
}
