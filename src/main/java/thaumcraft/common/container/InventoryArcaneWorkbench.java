package thaumcraft.common.container;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import thaumcraft.api.crafting.ArcaneCraftingInput;

/** Port of InventoryArcaneWorkbench: 9 grid slots plus 6 crystal slots, saved with the workbench. */
public class InventoryArcaneWorkbench extends SimpleContainer {
    private final BlockEntity workbench;

    public InventoryArcaneWorkbench(BlockEntity tileEntity) {
        super(ArcaneCraftingInput.SIZE);
        workbench = tileEntity;
    }

    public ArcaneCraftingInput asInput() {
        List<ItemStack> items = new ArrayList<>(getContainerSize());
        for (int i = 0; i < getContainerSize(); i++) items.add(getItem(i));
        return new ArcaneCraftingInput(items);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        workbench.setChanged();
    }
}
