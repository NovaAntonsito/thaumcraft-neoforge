package thaumcraft.common.container;

import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import javax.annotation.Nullable;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.crafting.ArcaneCraftingInput;
import thaumcraft.api.crafting.IArcaneRecipe;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.container.slot.SlotCraftingArcaneWorkbench;
import thaumcraft.common.container.slot.SlotCrystal;
import thaumcraft.common.lib.crafting.ThaumcraftCraftingManager;
import thaumcraft.common.registry.TCMenus;
import thaumcraft.common.tiles.crafting.TileArcaneWorkbench;

/**
 * Port of ContainerArcaneWorkbench. Slot order as in the original: 0 result, 1-9 grid, 10-15 crystals,
 * 16-42 player inventory, 43-51 hotbar. The available vis is synced as data slot 0.
 */
public class ContainerArcaneWorkbench extends AbstractContainerMenu implements ContainerListener {
    /** Crystal slot positions, in ShardType order: air, fire, water, earth, order, entropy. */
    public static final int[] xx = {64, 17, 112, 17, 112, 64};
    public static final int[] yy = {13, 35, 35, 93, 93, 115};
    public static final Aspect[] CRYSTALS = {Aspect.AIR, Aspect.FIRE, Aspect.WATER, Aspect.EARTH, Aspect.ORDER, Aspect.ENTROPY};

    private final TileArcaneWorkbench tileEntity;
    private final Player player;
    public final ResultContainer craftResult = new ResultContainer();
    private int lastVis = -1;
    private long lastCheck;

    /** Client side: the workbench position comes from the open packet. */
    public ContainerArcaneWorkbench(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(id, inventory, (TileArcaneWorkbench) inventory.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public ContainerArcaneWorkbench(int id, Inventory inventory, TileArcaneWorkbench e) {
        super(TCMenus.ARCANE_WORKBENCH.get(), id);
        tileEntity = e;
        player = inventory.player;
        tileEntity.inventoryCraft.addListener(this);
        e.getAura();
        addSlot(new SlotCraftingArcaneWorkbench(tileEntity, player, craftResult, 0, 160, 64));
        for (int var6 = 0; var6 < 3; ++var6) {
            for (int var7 = 0; var7 < 3; ++var7) {
                addSlot(new Slot(tileEntity.inventoryCraft, var7 + var6 * 3, 40 + var7 * 24, 40 + var6 * 24));
            }
        }
        for (int st = 0; st < 6; ++st) {
            addSlot(new SlotCrystal(CRYSTALS[st], tileEntity.inventoryCraft, st + 9, xx[st], yy[st]));
        }
        for (int var6 = 0; var6 < 3; ++var6) {
            for (int var7 = 0; var7 < 9; ++var7) {
                addSlot(new Slot(inventory, var7 + var6 * 9 + 9, 16 + var7 * 18, 151 + var6 * 18));
            }
        }
        for (int var6 = 0; var6 < 9; ++var6) {
            addSlot(new Slot(inventory, var6, 16 + var6 * 18, 209));
        }
        // updateProgressBar(0, vis): the server value is read, the client one written.
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return tileEntity.auraVisServer;
            }

            @Override
            public void set(int value) {
                tileEntity.auraVisClient = value;
            }
        });
        slotsChanged(tileEntity.inventoryCraft);
    }

    public TileArcaneWorkbench getTile() {
        return tileEntity;
    }

    @Override
    public void containerChanged(Container container) {
        slotsChanged(container);
    }

    @Override
    public void broadcastChanges() {
        long t = System.currentTimeMillis();
        if (t > lastCheck) {
            lastCheck = t + 500L;
            tileEntity.getAura();
        }
        if (lastVis != tileEntity.auraVisServer) {
            lastVis = tileEntity.auraVisServer;
            slotsChanged(tileEntity.inventoryCraft);
        }
        super.broadcastChanges();
    }

    @Override
    public void slotsChanged(Container container) {
        Level level = tileEntity.getLevel();
        if (level == null || level.isClientSide || !(player instanceof ServerPlayer serverPlayer)) return;
        ArcaneCraftingInput input = tileEntity.inventoryCraft.asInput();
        ItemStack itemstack = ItemStack.EMPTY;
        RecipeHolder<IArcaneRecipe> arecipe = ThaumcraftCraftingManager.findMatchingArcaneRecipe(level, player, input);
        if (arecipe != null && ThaumcraftCapabilities.getKnowledge(player).isResearchKnown(arecipe.value().getResearch())) {
            // The crystals are checked by matches(); here the research and the vis. TODO: vis discount.
            tileEntity.getAura();
            if (tileEntity.auraVisServer >= thaumcraft.common.anomaly.AnomalyModifiers.arcaneVis(level, arecipe.value().getVis())) {
                craftResult.setRecipeUsed(arecipe);
                itemstack = arecipe.value().assemble(input, level.registryAccess());
            }
        } else {
            CraftingInput grid = input.grid().input();
            RecipeHolder<CraftingRecipe> irecipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, grid, level).orElse(null);
            if (irecipe != null && craftResult.setRecipeUsed(level, serverPlayer, irecipe)) {
                itemstack = irecipe.value().assemble(grid, level.registryAccess());
            }
        }
        craftResult.setItem(0, itemstack);
        setRemoteSlot(0, itemstack);
        serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(containerId, incrementStateId(), 0, itemstack));
    }

    /** The arcane recipe in the grid right now (client side too, for the screen), or null. */
    @Nullable
    public IArcaneRecipe currentArcaneRecipe() {
        Level level = tileEntity.getLevel();
        if (level == null) return null;
        RecipeHolder<IArcaneRecipe> holder = ThaumcraftCraftingManager.findMatchingArcaneRecipe(level, player, tileEntity.inventoryCraft.asInput());
        return holder != null ? holder.value() : null;
    }

    @Override
    public void removed(Player p) {
        super.removed(p);
        tileEntity.inventoryCraft.removeListener(this);
    }

    @Override
    public boolean stillValid(Player p) {
        Level level = tileEntity.getLevel();
        return level != null && stillValid(ContainerLevelAccess.create(level, tileEntity.getBlockPos()), p, ConfigBlocks.arcaneWorkbench.get());
    }

    @Override
    public ItemStack quickMoveStack(Player p, int par1) {
        ItemStack var2 = ItemStack.EMPTY;
        Slot var3 = slots.get(par1);
        if (var3.hasItem()) {
            ItemStack var4 = var3.getItem();
            var2 = var4.copy();
            if (par1 == 0) {
                if (!moveItemStackTo(var4, 16, 52, true)) {
                    return ItemStack.EMPTY;
                }
                var3.onQuickCraft(var4, var2);
            } else if (par1 >= 16 && par1 < 52) {
                for (int st = 0; st < 6; ++st) {
                    if (SlotCrystal.isValidCrystal(var4, CRYSTALS[st])) {
                        if (!moveItemStackTo(var4, 10 + st, 11 + st, false)) {
                            return ItemStack.EMPTY;
                        }
                        if (var4.getCount() == 0) {
                            break;
                        }
                    }
                }
                if (var4.getCount() != 0) {
                    if (par1 < 43) {
                        if (!moveItemStackTo(var4, 43, 52, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (!moveItemStackTo(var4, 16, 43, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (!moveItemStackTo(var4, 16, 52, false)) {
                return ItemStack.EMPTY;
            }
            if (var4.isEmpty()) {
                var3.setByPlayer(ItemStack.EMPTY);
            } else {
                var3.setChanged();
            }
            if (var4.getCount() == var2.getCount()) {
                return ItemStack.EMPTY;
            }
            var3.onTake(p, var4);
        }
        return var2;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot.container != craftResult && super.canTakeItemForPickAll(stack, slot);
    }
}
