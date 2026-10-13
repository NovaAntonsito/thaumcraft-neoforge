package thaumcraft.common.tiles.essentia;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aura.AuraHelper;
import thaumcraft.common.blocks.essentia.BlockSmelter;
import thaumcraft.common.blocks.essentia.BlockSmelterAux;
import thaumcraft.common.blocks.essentia.BlockSmelterVent;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.tiles.devices.TileBellows;
import thaumcraft.client.fx.ClientFX;
import thaumcraft.common.lib.crafting.ThaumcraftCraftingManager;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.tiles.TileThaumcraft;

/**
 * Port of TileSmelter (essentia smeltery). Burns fuel like a furnace and breaks items down into their aspects
 * (up to 256), losing some as flux. Alembics stacked on top pull the aspects out one point at a time.
 * Slot 0 is the item to smelt, slot 1 the fuel.
 */
public class TileSmelter extends TileThaumcraft implements WorldlyContainer {
    private static final int[] SLOTS_BOTTOM = {1};
    private static final int[] SLOTS_TOP = {};
    private static final int[] SLOTS_SIDES = {0};

    private final NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);
    public AspectList aspects = new AspectList();
    public int vis;
    private final int maxVis = 256;
    public int smeltTime = 100;
    boolean speedBoost;
    public int furnaceBurnTime;
    public int currentItemBurnTime;
    public int furnaceCookTime;
    int count;
    int bellows = -1;

    /** Window properties 0-4 of ContainerSmelter. */
    public final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> furnaceCookTime;
                case 1 -> furnaceBurnTime;
                case 2 -> currentItemBurnTime;
                case 3 -> vis;
                case 4 -> smeltTime;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> furnaceCookTime = value;
                case 1 -> furnaceBurnTime = value;
                case 2 -> currentItemBurnTime = value;
                case 3 -> vis = value;
                case 4 -> smeltTime = value;
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return 5;
        }
    };

    public TileSmelter(BlockPos pos, BlockState state) {
        super(TCBlockEntities.SMELTER.get(), pos, state);
    }

    @Override
    protected void readSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        furnaceBurnTime = tag.getShort("BurnTime");
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putShort("BurnTime", (short) furnaceBurnTime);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items.clear();
        ContainerHelper.loadAllItems(tag, items, registries);
        speedBoost = tag.getBoolean("speedBoost");
        furnaceCookTime = tag.getShort("CookTime");
        currentItemBurnTime = items.get(1).getBurnTime(RecipeType.SMELTING);
        aspects = AspectList.CODEC.parse(NbtOps.INSTANCE, tag.get("Aspects")).result().orElseGet(AspectList::new);
        vis = aspects.visSize();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putBoolean("speedBoost", speedBoost);
        tag.putShort("CookTime", (short) furnaceCookTime);
        AspectList.CODEC.encodeStart(NbtOps.INSTANCE, aspects).result().ifPresent(t -> tag.put("Aspects", t));
    }

    // ---- tick ----

    public static void tick(Level level, BlockPos pos, BlockState state, TileSmelter te) {
        te.update();
    }

    private void update() {
        boolean flag = furnaceBurnTime > 0;
        boolean flag2 = false;
        ++count;
        if (furnaceBurnTime > 0) {
            --furnaceBurnTime;
        }
        if (level != null && !level.isClientSide) {
            if (bellows < 0) {
                checkNeighbours();
            }
            int speed = getSpeed();
            if (speedBoost) {
                // The original had speed *= (int) 0.8, which made it 0 (and count % 0 throws). This is its evident intent.
                speed = Math.max(1, (int) (speed * 0.8f));
            }
            if (count % speed == 0 && aspects.size() > 0) {
                for (Aspect aspect : aspects.getAspects()) {
                    if (aspects.getAmount(aspect) > 0 && TileAlembic.processAlembics(level, worldPosition, aspect)) {
                        takeFromContainer(aspect, 1);
                        break;
                    }
                }
                // Auxiliary slurry pumps on the sides feed the alembics stacked on them.
                Direction front = getBlockState().getValue(BlockSmelter.FACING);
                for (Direction face : Direction.Plane.HORIZONTAL) {
                    if (front == face) {
                        continue;
                    }
                    BlockState aux = level.getBlockState(worldPosition.relative(face));
                    if (aux.is(ConfigBlocks.smelterAux.get()) && aux.getValue(BlockSmelterAux.FACING) == face.getOpposite()) {
                        for (Aspect aspect2 : aspects.getAspects()) {
                            if (aspects.getAmount(aspect2) > 0 && TileAlembic.processAlembics(level, worldPosition.relative(face), aspect2)) {
                                takeFromContainer(aspect2, 1);
                                break;
                            }
                        }
                    }
                }
            }
            if (furnaceBurnTime == 0) {
                if (canSmelt()) {
                    int itemBurnTime = items.get(1).getBurnTime(RecipeType.SMELTING);
                    furnaceBurnTime = itemBurnTime;
                    currentItemBurnTime = itemBurnTime;
                    if (furnaceBurnTime > 0) {
                        BlockSmelter.setFurnaceState(level, worldPosition, true);
                        flag2 = true;
                        speedBoost = false;
                        ItemStack itemstack = items.get(1);
                        if (!itemstack.isEmpty()) {
                            if (itemstack.is(ConfigItems.alumentum.get())) {
                                speedBoost = true;
                            }
                            ItemStack remainder = itemstack.getCraftingRemainingItem();
                            itemstack.shrink(1);
                            if (itemstack.isEmpty()) {
                                items.set(1, remainder);
                            }
                        }
                    } else {
                        BlockSmelter.setFurnaceState(level, worldPosition, false);
                    }
                } else {
                    BlockSmelter.setFurnaceState(level, worldPosition, false);
                }
            }
            if (getBlockState().getValue(BlockSmelter.ENABLED) && canSmelt()) {
                ++furnaceCookTime;
                if (furnaceCookTime >= smeltTime) {
                    furnaceCookTime = 0;
                    smeltItem();
                    flag2 = true;
                }
            } else {
                furnaceCookTime = 0;
            }
            if (flag != furnaceBurnTime > 0) {
                flag2 = true;
            }
        }
        if (flag2) {
            setChanged();
        }
    }

    private boolean canSmelt() {
        if (items.get(0).isEmpty()) {
            return false;
        }
        AspectList al = ThaumcraftCraftingManager.getObjectTags(items.get(0));
        if (al == null || al.size() == 0) {
            return false;
        }
        int vs = al.visSize();
        if (vs > maxVis - vis) {
            return false;
        }
        smeltTime = (int) (vs * 2 * (1.0f - 0.125f * bellows));
        return true;
    }

    /** Counts the bellows blowing into any side but the front. */
    public void checkNeighbours() {
        Direction front = getBlockState().getValue(BlockSmelter.FACING);
        Direction[] faces = Direction.Plane.HORIZONTAL.stream().filter(d -> d != front).toArray(Direction[]::new);
        bellows = TileBellows.getBellows(level, worldPosition, faces);
    }

    /** getType of the original: 0 basic, 1 thaumium, 2 void metal. */
    private int getSmelterType() {
        return getBlockState().getBlock() instanceof BlockSmelter bs ? bs.type : 0;
    }

    private float getEfficiency() {
        float efficiency = 0.8f;
        if (getSmelterType() == 1) {
            efficiency = 0.9f;
        }
        if (getSmelterType() == 2) {
            efficiency = 0.95f;
        }
        return efficiency;
    }

    private int getSpeed() {
        return 20 - (getSmelterType() == 1 ? 10 : 5);
    }

    public void smeltItem() {
        if (canSmelt()) {
            int flux = 0;
            AspectList al = ThaumcraftCraftingManager.getObjectTags(items.get(0)).copy();
            for (Aspect a : al.getAspects()) {
                if (getEfficiency() < 1.0f) {
                    for (int qq = al.getAmount(a), q = 0; q < qq; ++q) {
                        if (level.random.nextFloat() > (a == Aspect.FLUX ? getEfficiency() * 0.66f : getEfficiency())) {
                            al.reduce(a, 1);
                            ++flux;
                        }
                    }
                }
                aspects.add(a, al.getAmount(a));
            }
            if (flux > 0) {
                // Each venting port has a one in three chance to blow a lost point out into the air; the rest goes
                // into the aura. The decompiled loop is garbled here; this is its evident intent.
                Direction front = getBlockState().getValue(BlockSmelter.FACING);
                int pp = 0;
                for (int c = 0; c < flux; ++c) {
                    boolean vented = false;
                    for (Direction face : Direction.Plane.HORIZONTAL) {
                        if (front == face) {
                            continue;
                        }
                        BlockState vent = level.getBlockState(worldPosition.relative(face));
                        if (vent.is(ConfigBlocks.smelterVent.get()) && vent.getValue(BlockSmelterVent.FACING) == face.getOpposite()
                                && level.random.nextFloat() < 0.333f) {
                            level.blockEvent(worldPosition, getBlockState().getBlock(), 1, face.getOpposite().get3DDataValue());
                            vented = true;
                            break;
                        }
                    }
                    if (!vented) {
                        ++pp;
                    }
                }
                if (pp > 0) {
                    AuraHelper.polluteAura(level, worldPosition, (float) pp, true);
                }
            }
            vis = aspects.visSize();
            items.get(0).shrink(1);
        }
    }

    public static boolean isItemFuel(ItemStack stack) {
        return stack.getBurnTime(RecipeType.SMELTING) > 0;
    }

    public boolean takeFromContainer(Aspect tag, int amount) {
        if (aspects != null && aspects.getAmount(tag) >= amount) {
            aspects.remove(tag, amount);
            vis = aspects.visSize();
            setChanged();
            return true;
        }
        return false;
    }

    public int getCookProgressScaled(int par1) {
        if (smeltTime <= 0) {
            smeltTime = 1;
        }
        return furnaceCookTime * par1 / smeltTime;
    }

    public int getVisScaled(int par1) {
        return vis * par1 / maxVis;
    }

    public int getBurnTimeRemainingScaled(int par1) {
        if (currentItemBurnTime == 0) {
            currentItemBurnTime = 200;
        }
        return furnaceBurnTime * par1 / currentItemBurnTime;
    }

    /** Block event 1: a puff of flux out of a venting port on side j. */
    @Override
    public boolean triggerEvent(int i, int j) {
        if (i == 1) {
            if (level.isClientSide) {
                Direction d = Direction.from3DDataValue(j);
                level.playLocalSound(worldPosition.getX() + 0.5 + d.getOpposite().getStepX(), worldPosition.getY() + 0.5,
                        worldPosition.getZ() + 0.5 + d.getOpposite().getStepZ(), SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.25f,
                        2.6f + (level.random.nextFloat() - level.random.nextFloat()) * 0.8f, true);
                Direction o = d.getOpposite();
                for (int a = 0; a < 4; ++a) {
                    float fx = 0.1f - level.random.nextFloat() * 0.2f;
                    float fz = 0.1f - level.random.nextFloat() * 0.2f;
                    float fy = 0.1f - level.random.nextFloat() * 0.2f;
                    float fx2 = 0.1f - level.random.nextFloat() * 0.2f;
                    float fz2 = 0.1f - level.random.nextFloat() * 0.2f;
                    float fy2 = 0.1f - level.random.nextFloat() * 0.2f;
                    ClientFX.drawVentParticles(level, worldPosition.getX() + 0.5f + fx + o.getStepX(), worldPosition.getY() + 0.5f + fy,
                            worldPosition.getZ() + 0.5f + fz + o.getStepZ(), o.getStepX() / 4.0f + fx2, o.getStepY() / 4.0f + fy2,
                            o.getStepZ() / 4.0f + fz2, 0xAAAAAA);
                }
            }
            return true;
        }
        return super.triggerEvent(i, j);
    }

    // ---- inventory (TileThaumcraftInventory / ISidedInventory) ----

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack out = ContainerHelper.removeItem(items, slot, amount);
        if (!out.isEmpty()) setChanged();
        return out;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        stack.limitSize(getMaxStackSize(stack));
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return net.minecraft.world.Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == 0) {
            AspectList al = ThaumcraftCraftingManager.getObjectTags(stack);
            if (al != null && al.size() > 0) {
                return true;
            }
        }
        return slot == 1 && isItemFuel(stack);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return side == Direction.DOWN ? SLOTS_BOTTOM : side == Direction.UP ? SLOTS_TOP : SLOTS_SIDES;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return side != Direction.UP && canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return side != Direction.UP || slot != 1 || stack.is(Items.BUCKET);
    }
}
