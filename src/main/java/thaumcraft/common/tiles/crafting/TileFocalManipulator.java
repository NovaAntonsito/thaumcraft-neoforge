package thaumcraft.common.tiles.crafting;

import java.util.HashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.api.casters.FocusModSplit;
import thaumcraft.api.casters.FocusPackage;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.items.casters.ItemFocus;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.utils.InventoryUtils;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.registry.TCDataComponents;
import thaumcraft.common.tiles.TileThaumcraft;
import thaumcraft.common.world.aura.AuraHandler;

/**
 * The focal manipulator (wand_workbench). The GUI sends the node grid; starting the craft takes the crystals and
 * experience, then the vis is drained from the aura (up to 20 a second) and the package is written on the focus.
 * TODO: the arcane workbench charger on top (draw from the 3x3 chunks around) is not ported yet.
 */
public class TileFocalManipulator extends TileThaumcraft implements Container {
    public float vis = 0.0f;
    public HashMap<Integer, FocusElementNode> data = new HashMap<>();
    public String focusName = "";
    int ticks = 0;
    public float visCost = 0.0f;
    public int xpCost = 0;
    private AspectList crystals = new AspectList();
    public AspectList crystalsSync = new AspectList();
    public boolean doGuiReset = false;
    private ItemStack focus = ItemStack.EMPTY;

    public TileFocalManipulator(BlockPos pos, BlockState state) {
        super(TCBlockEntities.FOCAL_MANIPULATOR.get(), pos, state);
    }

    @Override
    protected void readSyncNBT(CompoundTag nbt, HolderLookup.Provider registries) {
        vis = nbt.getFloat("vis");
        focusName = nbt.getString("focusName");
        crystalsSync = AspectList.CODEC.parse(NbtOps.INSTANCE, nbt.get("crystals")).result().orElseGet(AspectList::new);
        ListTag nodelist = nbt.getList("nodes", Tag.TAG_COMPOUND);
        data.clear();
        for (int x = 0; x < nodelist.size(); ++x) {
            FocusElementNode node = new FocusElementNode();
            node.deserialize(nodelist.getCompound(x));
            data.put(node.id, node);
        }
        // The GUI reset on a focus change is driven by the container slot (setItem), as in the original.
        focus = nbt.contains("Item") ? ItemStack.parseOptional(registries, nbt.getCompound("Item")) : ItemStack.EMPTY;
    }

    @Override
    protected void writeSyncNBT(CompoundTag nbt, HolderLookup.Provider registries) {
        nbt.putFloat("vis", vis);
        nbt.putString("focusName", focusName);
        AspectList.CODEC.encodeStart(NbtOps.INSTANCE, crystalsSync).result().ifPresent(t -> nbt.put("crystals", t));
        ListTag nodelist = new ListTag();
        for (FocusElementNode node : data.values()) {
            nodelist.add(node.serialize());
        }
        nbt.put("nodes", nodelist);
        if (!focus.isEmpty()) {
            nbt.put("Item", focus.save(registries));
        }
    }

    public float spendAura(float vis) {
        return AuraHandler.drainVis(level, worldPosition, vis, false);
    }

    public static void tick(Level world, BlockPos pos, BlockState state, TileFocalManipulator tile) {
        boolean complete = false;
        ++tile.ticks;
        if (!world.isClientSide) {
            if (tile.ticks % 20 == 0) {
                if (tile.vis > 0.0f && !(tile.focus.getItem() instanceof ItemFocus)) {
                    complete = true;
                    tile.vis = 0.0f;
                    world.playSound(null, pos, SoundsTC.WANDFAIL.get(), SoundSource.BLOCKS, 0.33f, 1.0f);
                }
                if (!complete && tile.vis > 0.0f) {
                    float amt = tile.spendAura(Math.min(20.0f, tile.vis));
                    if (amt > 0.0f) {
                        world.blockEvent(pos, state.getBlock(), 5, 1);
                        tile.vis -= amt;
                        tile.syncTile(false);
                        tile.setChanged();
                    }
                    if (tile.vis <= 0.0f && tile.focus.getItem() instanceof ItemFocus) {
                        complete = true;
                        tile.endCraft();
                    }
                }
            }
        } else if (tile.vis > 0.0f) {
            var r = world.random;
            thaumcraft.client.fx.FXDispatcher.drawGenericParticles(pos.getX() + 0.5 + (r.nextFloat() - r.nextFloat()) * 0.3f,
                    pos.getY() + 1.4 + (r.nextFloat() - r.nextFloat()) * 0.3f, pos.getZ() + 0.5 + (r.nextFloat() - r.nextFloat()) * 0.3f, 0.0, 0.0, 0.0,
                    0.5f + r.nextFloat() * 0.4f, 1.0f - r.nextFloat() * 0.4f, 1.0f - r.nextFloat() * 0.4f, 0.8f, false, 448, 9, 1, 6 + r.nextInt(5), 0,
                    0.3f + r.nextFloat() * 0.3f, 0.0f, 0);
        }
        if (complete) {
            tile.vis = 0.0f;
            tile.syncTile(false);
            tile.setChanged();
        }
    }

    private FocusPackage generateFocus() {
        if (data != null && !data.isEmpty()) {
            FocusPackage core = new FocusPackage();
            int totalComplexity = 0;
            HashMap<String, Integer> compCount = new HashMap<>();
            for (FocusElementNode node : data.values()) {
                if (node.node != null) {
                    int a = compCount.getOrDefault(node.node.getKey(), 0);
                    ++a;
                    node.complexityMultiplier = 0.5f * (a + 1);
                    compCount.put(node.node.getKey(), a);
                    totalComplexity += (int) (node.node.getComplexity() * node.complexityMultiplier);
                }
            }
            core.setComplexity(totalComplexity);
            FocusElementNode root = data.get(0);
            traverseChildren(core, root);
            return core;
        }
        return null;
    }

    private void traverseChildren(FocusPackage currentPackage, FocusElementNode currentNode) {
        if (currentPackage == null || currentNode == null) {
            return;
        }
        currentPackage.addNode(currentNode.node);
        if (currentNode.children == null || currentNode.children.length == 0) {
            return;
        }
        if (currentNode.children.length == 1) {
            traverseChildren(currentPackage, data.get(currentNode.children[0]));
        } else {
            FocusModSplit splitNode = (FocusModSplit) currentNode.node;
            splitNode.getSplitPackages().clear();
            for (int c : currentNode.children) {
                FocusPackage splitPackage = new FocusPackage();
                traverseChildren(splitPackage, data.get(c));
                splitNode.getSplitPackages().add(splitPackage);
            }
        }
    }

    public void endCraft() {
        vis = 0.0f;
        if (focus.getItem() instanceof ItemFocus) {
            FocusPackage core = generateFocus();
            if (core != null) {
                level.playSound(null, worldPosition, SoundsTC.WAND.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
                ItemStack f = focus.copy();
                f.remove(TCDataComponents.FOCUS_COLOR.get());
                f.set(DataComponents.CUSTOM_NAME, Component.literal(focusName));
                ItemFocus.setPackage(f, core);
                setItem(0, f);
                crystalsSync = new AspectList();
                data.clear();
                syncTile(false);
                setChanged();
            }
        }
    }

    public boolean startCraft(int id, Player p) {
        if (data == null || data.isEmpty() || vis > 0.0f || !(focus.getItem() instanceof ItemFocus itemFocus)) {
            return false;
        }
        int maxComplexity = itemFocus.getMaxComplexity();
        int totalComplexity = 0;
        crystals = new AspectList();
        HashMap<String, Integer> compCount = new HashMap<>();
        for (FocusElementNode node : data.values()) {
            if (node.node == null) {
                return false;
            }
            if (!ThaumcraftCapabilities.knowsResearchStrict(p, node.node.getResearch())) {
                return false;
            }
            int a = compCount.getOrDefault(node.node.getKey(), 0);
            ++a;
            node.complexityMultiplier = 0.5f * (a + 1);
            compCount.put(node.node.getKey(), a);
            totalComplexity += (int) (node.node.getComplexity() * node.complexityMultiplier);
            if (node.node.getAspect() == null) {
                continue;
            }
            crystals.add(node.node.getAspect(), 1);
        }
        vis = (float) (totalComplexity * 10 + maxComplexity / 5);
        xpCost = (int) Math.max(1L, Math.round(Math.sqrt(totalComplexity)));
        if (!p.getAbilities().instabuild && p.experienceLevel < xpCost) {
            vis = 0.0f;
            return false;
        }
        if (!p.getAbilities().instabuild) {
            p.giveExperienceLevels(-xpCost);
        }
        if (crystals.getAspects().length > 0) {
            ItemStack[] components = new ItemStack[crystals.getAspects().length];
            int r = 0;
            for (Aspect as : crystals.getAspects()) {
                components[r] = ConfigItems.crystalEssence.get().makeCrystal(as, crystals.getAmount(as));
                ++r;
            }
            for (ItemStack component : components) {
                if (!InventoryUtils.isPlayerCarryingAmount(p, component)) {
                    vis = 0.0f;
                    return false;
                }
            }
            for (ItemStack component : components) {
                InventoryUtils.consumePlayerItem(p, component, true);
            }
            crystalsSync = crystals.copy();
            setChanged();
            syncTile(false);
            level.playSound(null, worldPosition, SoundsTC.CRAFTSTART.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
            return true;
        }
        vis = 0.0f;
        return false;
    }

    @Override
    public boolean triggerEvent(int i, int j) {
        if (i == 1) {
            doGuiReset = true;
            return true;
        }
        if (i == 5) {
            if (level != null && level.isClientSide) {
                var r = level.random;
                thaumcraft.client.fx.FXDispatcher.visSparkle(worldPosition.getX() + r.nextInt(3) - r.nextInt(3), worldPosition.getY() + r.nextInt(3),
                        worldPosition.getZ() + r.nextInt(3) - r.nextInt(3), worldPosition.getX(), worldPosition.getY() + 1, worldPosition.getZ(), j);
            }
            return true;
        }
        return super.triggerEvent(i, j);
    }

    // ---- inventory (one focus slot) ----

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return focus.isEmpty();
    }

    @Override
    public ItemStack getItem(int index) {
        return index == 0 ? focus : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        if (index != 0 || focus.isEmpty()) return ItemStack.EMPTY;
        ItemStack out = focus.split(count);
        setItem(0, focus);
        return out;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        if (index != 0) return ItemStack.EMPTY;
        ItemStack ret = focus;
        focus = ItemStack.EMPTY;
        return ret;
    }

    /** setInventorySlotContents: a new or removed focus resets the craft (and the GUI on the client). */
    @Override
    public void setItem(int index, ItemStack stack) {
        if (index != 0) return;
        ItemStack prev = focus;
        focus = stack;
        if (stack.isEmpty() || !ItemStack.matches(stack, prev)) {
            if (level != null && level.isClientSide) {
                data.clear();
                doGuiReset = true;
            } else {
                vis = 0.0f;
                crystalsSync = new AspectList();
                setChanged();
                syncTile(false);
            }
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return stack.getItem() instanceof ItemFocus;
    }

    @Override
    public void clearContent() {
        focus = ItemStack.EMPTY;
    }
}
