package thaumcraft.common.tiles.crafting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.api.golems.IGolemProperties;
import thaumcraft.client.fx.ClientFX;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.container.ContainerGolemBuilder;
import thaumcraft.common.golems.GolemProperties;
import thaumcraft.common.golems.ItemGolemPlacer;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.network.misc.PacketGolemBuilderStuffToClient;
import thaumcraft.common.lib.utils.InventoryUtils;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.registry.TCDataComponents;
import thaumcraft.common.tiles.TileThaumcraft;

/**
 * Port of TileGolemBuilder, the golem press. Starting a golem takes its components from the inventories next to the
 * press (not above) or from the player; then it draws one Machina essentia every 5 ticks per point of cost and puts
 * the finished golem placer in its slot.
 */
public class TileGolemBuilder extends TileThaumcraft implements WorldlyContainer, IEssentiaTransport {
    private static final int[] SLOTS = {0};
    private ItemStack output = ItemStack.EMPTY;
    public long golem = -1L;
    public int cost = 0;
    public int maxCost = 0;
    public boolean[] hasStuff = null;
    boolean bufferedEssentia = false;
    int ticks = 0;
    public int press = 0;
    IGolemProperties props = null;
    ItemStack[] components = null;

    public TileGolemBuilder(BlockPos pos, BlockState state) {
        super(TCBlockEntities.GOLEM_BUILDER.get(), pos, state);
    }

    /** messageFromClient "check": which components are in the adjacent inventories. */
    public void checkCraftFor(long id, ServerPlayer player) {
        hasStuff = checkCraft(id);
        byte[] ba = new byte[hasStuff.length];
        for (int a = 0; a < ba.length; ++a) {
            ba[a] = (byte) (hasStuff[a] ? 1 : 0);
        }
        PacketDistributor.sendToPlayer(player, new PacketGolemBuilderStuffToClient(worldPosition, ba));
    }

    /** messageFromServer "stuff". */
    public void receiveStuff(byte[] ba) {
        hasStuff = null;
        if (ba != null && ba.length > 0) {
            hasStuff = new boolean[ba.length];
            for (int a = 0; a < ba.length; ++a) {
                hasStuff[a] = ba[a] == 1;
            }
        }
        ContainerGolemBuilder.redo = true;
    }

    @Override
    protected void readSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        output = tag.contains("Output") ? ItemStack.parseOptional(registries, tag.getCompound("Output")) : ItemStack.EMPTY;
        golem = tag.getLong("golem");
        cost = tag.getInt("cost");
        maxCost = tag.getInt("mcost");
        if (golem >= 0L) {
            try {
                props = GolemProperties.fromLong(golem);
                components = props.generateComponents();
            } catch (Exception e) {
                props = null;
                components = null;
                cost = 0;
                golem = -1L;
            }
        }
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        if (!output.isEmpty()) tag.put("Output", output.save(registries));
        tag.putLong("golem", golem);
        tag.putInt("cost", cost);
        tag.putInt("mcost", maxCost);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TileGolemBuilder tile) {
        tile.update();
    }

    private ItemStack makePlacer(long id) {
        return ItemGolemPlacer.withProps(ConfigItems.golemPlacer.get(), id);
    }

    private void update() {
        boolean complete = false;
        if (!level.isClientSide) {
            ++ticks;
            if (ticks % 5 == 0 && cost > 0 && golem >= 0L) {
                if (bufferedEssentia || drawEssentia()) {
                    bufferedEssentia = false;
                    --cost;
                    setChanged();
                }
                if (cost <= 0) {
                    ItemStack placer = makePlacer(golem);
                    if (output.isEmpty() || (output.getCount() < output.getMaxStackSize() && ItemStack.isSameItemSameComponents(output, placer))) {
                        if (output.isEmpty()) {
                            output = placer.copy();
                        } else {
                            output.grow(1);
                        }
                        complete = true;
                        level.playSound(null, worldPosition, SoundsTC.WAND.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
                    }
                }
            }
        } else {
            double x = worldPosition.getX() + 0.5;
            double y = worldPosition.getY() + 1;
            double z = worldPosition.getZ() + 0.5;
            if (press < 90 && cost > 0 && golem > 0L) {
                press += 6;
                if (press >= 60) {
                    level.playLocalSound(x, worldPosition.getY() + 0.5, z, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.66f,
                            1.0f + level.random.nextFloat() * 0.1f, false);
                    for (int a = 0; a < 16; ++a) {
                        ClientFX.drawVentParticles(level, x, y, z, level.random.nextGaussian() * 0.1, 0.0, level.random.nextGaussian() * 0.1, 11184810);
                    }
                }
            }
            if (press >= 90 && level.random.nextInt(8) == 0) {
                ClientFX.drawVentParticles(level, x, y, z, level.random.nextGaussian() * 0.1, 0.0, level.random.nextGaussian() * 0.1, 11184810);
                level.playLocalSound(x, worldPosition.getY() + 0.5, z, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.1f,
                        1.0f + level.random.nextFloat() * 0.1f, false);
            }
            if (press > 0 && (cost <= 0 || golem == -1L)) {
                if (press >= 90) {
                    for (int a = 0; a < 10; ++a) {
                        ClientFX.drawVentParticles(level, x, y, z, level.random.nextGaussian() * 0.1, 0.0, level.random.nextGaussian() * 0.1, 11184810);
                    }
                }
                press -= 3;
            }
        }
        if (complete) {
            cost = 0;
            golem = -1L;
            syncTile(false);
            setChanged();
        }
    }

    public boolean[] checkCraft(long id) {
        IGolemProperties props = GolemProperties.fromLong(id);
        ItemStack[] cc = props.generateComponents();
        boolean[] ret = new boolean[cc.length];
        int a = 0;
        for (ItemStack stack : cc) {
            ret[a] = InventoryUtils.checkAdjacentChests(level, worldPosition, stack);
            ++a;
        }
        return ret;
    }

    public boolean startCraft(long id, Player p) {
        ItemStack placer = makePlacer(id);
        if (!output.isEmpty() && (output.getCount() >= output.getMaxStackSize() || !ItemStack.isSameItemSameComponents(output, placer))) {
            cost = 0;
            props = null;
            components = null;
            golem = -1L;
            return false;
        }
        golem = id;
        props = GolemProperties.fromLong(golem);
        components = props.generateComponents();
        if (!InventoryUtils.consumeItemsFromAdjacentInventoryOrPlayer(level, worldPosition, p, true, components)) {
            cost = 0;
            props = null;
            components = null;
            golem = -1L;
            return false;
        }
        cost = props.getTraits().size() * 2;
        for (ItemStack stack : components) {
            cost += stack.getCount();
        }
        InventoryUtils.consumeItemsFromAdjacentInventoryOrPlayer(level, worldPosition, p, false, components);
        maxCost = cost;
        setChanged();
        syncTile(false);
        level.playSound(null, worldPosition, SoundsTC.WAND.get(), SoundSource.BLOCKS, 0.25f, 1.0f);
        return true;
    }

    boolean drawEssentia() {
        for (Direction face : Direction.values()) {
            BlockEntity te = ThaumcraftApiHelper.getConnectableTile(level, worldPosition, face);
            if (te instanceof IEssentiaTransport ic) {
                if (!ic.canOutputTo(face.getOpposite())) {
                    return false;
                }
                if (ic.getSuctionAmount(face.getOpposite()) < getSuctionAmount(face) && ic.takeEssentia(Aspect.MECHANISM, 1, face.getOpposite()) == 1) {
                    return true;
                }
            }
        }
        return false;
    }

    // ---- IEssentiaTransport ----

    @Override
    public boolean isConnectable(Direction face) {
        return face.getAxis().isHorizontal() || face == Direction.DOWN;
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return isConnectable(face);
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return false;
    }

    @Override
    public void setSuction(Aspect aspect, int amount) {
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    public Aspect getSuctionType(Direction face) {
        return Aspect.MECHANISM;
    }

    @Override
    public int getSuctionAmount(Direction face) {
        return cost > 0 && golem >= 0L ? 128 : 0;
    }

    @Override
    public Aspect getEssentiaType(Direction loc) {
        return null;
    }

    @Override
    public int getEssentiaAmount(Direction loc) {
        return 0;
    }

    @Override
    public int takeEssentia(Aspect aspect, int amount, Direction facing) {
        return 0;
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, Direction facing) {
        if (!bufferedEssentia && cost > 0 && golem >= 0L && aspect == Aspect.MECHANISM) {
            bufferedEssentia = true;
            return 1;
        }
        return 0;
    }

    // ---- inventory (TileThaumcraftInventory with one slot) ----

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return output.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot == 0 ? output : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (slot != 0 || output.isEmpty()) return ItemStack.EMPTY;
        ItemStack out = output.split(amount);
        setChanged();
        syncTile(false);
        return out;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack out = output;
        output = ItemStack.EMPTY;
        return out;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot == 0) {
            output = stack;
            setChanged();
            syncTile(false);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        output = ItemStack.EMPTY;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ItemGolemPlacer;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) {
        return canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return true;
    }
}
