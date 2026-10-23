package thaumcraft.common.tiles.crafting;

import thaumcraft.client.fx.FXDispatcher;
import net.minecraft.core.Direction;
import net.minecraft.world.SimpleContainer;
import net.neoforged.neoforge.event.EventHooks;
import javax.annotation.Nullable;

import org.joml.Vector3f;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectHelper;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aura.AuraHelper;
import thaumcraft.api.crafting.CrucibleRecipe;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.crafting.ThaumcraftCraftingManager;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.registry.TCTags;

/**
 * Port of TileCrucible. Holds up to 1000 mB of water and the melted aspects.
 * Heat rises while a heat source ({@link TCTags#CRUCIBLE_HEATERS}) is below; above 150 it boils and melts items.
 */
public class TileCrucible extends BlockEntity implements thaumcraft.api.aspects.IAspectContainer {
    public static final String SPECIAL_ITEM_TAG = "thaumcraft.special_item";

    public short heat;
    public AspectList aspects = new AspectList();
    public final int maxTags = 500;
    private long counter = -100L;

    public final FluidTank tank = new FluidTank(1000, fs -> fs.is(Fluids.WATER)) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            syncTile();
        }
    };

    public TileCrucible(BlockPos pos, BlockState state) {
        super(TCBlockEntities.CRUCIBLE.get(), pos, state);
    }

    // ---- NBT and client sync (readSyncNBT / writeSyncNBT) ----

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        heat = tag.getShort("Heat");
        tank.readFromNBT(registries, tag);
        if (tag.contains("Empty")) {
            tank.setFluid(FluidStack.EMPTY);
        }
        aspects = AspectList.CODEC.parse(net.minecraft.nbt.NbtOps.INSTANCE, tag.get("Aspects")).result().orElseGet(AspectList::new);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putShort("Heat", heat);
        tank.writeToNBT(registries, tag);
        AspectList.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, aspects).result().ifPresent(t -> tag.put("Aspects", t));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveCustomOnly(registries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void syncTile() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    // ---- tick ----

    public static void tick(Level level, BlockPos pos, BlockState state, TileCrucible te) {
        te.update();
    }

    private void update() {
        ++counter;
        int prevheat = heat;
        if (!level.isClientSide) {
            if (tank.getFluidAmount() > 0) {
                BlockState below = level.getBlockState(worldPosition.below());
                if (TCTags.isHeating(below)) {
                    if (heat < 200) {
                        ++heat;
                        if (prevheat < 151 && heat >= 151) {
                            setChanged();
                            syncTile();
                        }
                    }
                } else if (heat > 0) {
                    --heat;
                    if (heat == 149) {
                        setChanged();
                        syncTile();
                    }
                }
            } else if (heat > 0) {
                --heat;
            }
            if (aspects.visSize() > maxTags) {
                spillRandom();
            }
            if (counter >= 100L) {
                spillRandom();
                counter = 0L;
            }
        } else if (tank.getFluidAmount() > 0) {
            drawEffects();
        }
        if (level.isClientSide && prevheat < 151 && heat >= 151) {
            ++heat;
        }
    }

    private void drawEffects() {
        var rand = level.random;
        int px = worldPosition.getX(), py = worldPosition.getY(), pz = worldPosition.getZ();
        if (heat > 150) {
            FXDispatcher.crucibleFroth(px + 0.2f + rand.nextFloat() * 0.6f, py + getFluidHeight(), pz + 0.2f + rand.nextFloat() * 0.6f);
            if (aspects.visSize() > maxTags) {
                for (int a = 0; a < 2; ++a) {
                    FXDispatcher.crucibleFrothDown((float) px, (float) (py + 1), pz + rand.nextFloat());
                    FXDispatcher.crucibleFrothDown((float) (px + 1), (float) (py + 1), pz + rand.nextFloat());
                    FXDispatcher.crucibleFrothDown(px + rand.nextFloat(), (float) (py + 1), (float) pz);
                    FXDispatcher.crucibleFrothDown(px + rand.nextFloat(), (float) (py + 1), (float) (pz + 1));
                }
            }
        }
        if (rand.nextInt(6) == 0 && aspects.size() > 0) {
            int color = aspects.getAspects()[rand.nextInt(aspects.size())].getColor();
            int x = 5 + rand.nextInt(22);
            int y = 5 + rand.nextInt(22);
            FXDispatcher.crucibleBubble(px + x / 32.0f + 0.015625f, py + 0.05f + getFluidHeight(), pz + y / 32.0f + 0.015625f,
                    ((color >> 16) & 0xFF) / 255.0f, ((color >> 8) & 0xFF) / 255.0f, (color & 0xFF) / 255.0f);
        }
    }

    // ---- crafting ----

    /** Spits the result out of the crucible as a floating item that cannot be melted again (EntitySpecialItem). */
    public void ejectItem(ItemStack items) {
        boolean first = true;
        do {
            ItemStack spitout = items.copy();
            if (spitout.getCount() > spitout.getMaxStackSize()) {
                spitout.setCount(spitout.getMaxStackSize());
            }
            items.shrink(spitout.getCount());
            ItemEntity entityitem = new ItemEntity(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.71, worldPosition.getZ() + 0.5, spitout);
            entityitem.setNoGravity(true);
            entityitem.addTag(SPECIAL_ITEM_TAG);
            entityitem.setDeltaMovement(first ? 0.0 : (level.random.nextFloat() - level.random.nextFloat()) * 0.01f, 0.075,
                    first ? 0.0 : (level.random.nextFloat() - level.random.nextFloat()) * 0.01f);
            level.addFreshEntity(entityitem);
            first = false;
        } while (items.getCount() > 0);
    }

    /**
     * Melts or crafts with each item of the stack. Returns what is left (empty if all was used).
     * Only call when heat &gt; 150 and there is water.
     */
    public ItemStack attemptSmelt(ItemStack item, @Nullable Player player) {
        boolean bubble = false;
        boolean craftDone = false;
        int stacksize = item.getCount();
        for (int a = 0; a < item.getCount(); ++a) {
            CrucibleRecipe rc = ThaumcraftCraftingManager.findMatchingCrucibleRecipe(level, player, aspects, item);
            if (rc != null && tank.getFluidAmount() > 0) {
                ItemStack out = rc.getRecipeOutput().copy();
                if (player != null) {
                    out.onCraftedBy(level, player, out.getCount());
                    EventHooks.firePlayerCraftingEvent(player, out, new SimpleContainer(item.copyWithCount(1)));
                }
                aspects = rc.removeMatching(aspects, level);
                tank.drain(50, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
                ejectItem(out);
                craftDone = true;
                --stacksize;
                counter = -250L;
            } else {
                AspectList ot = AspectHelper.getObjectAspects(item);
                if (ot.size() != 0) {
                    for (Aspect tag : ot.getAspects()) {
                        aspects.add(tag, ot.getAmount(tag));
                    }
                    bubble = true;
                    --stacksize;
                    counter = -150L;
                }
            }
        }
        if (bubble) {
            level.playSound(null, worldPosition, SoundsTC.BUBBLE.get(), SoundSource.BLOCKS, 0.2f, 1.0f + level.random.nextFloat() * 0.4f);
            syncTile();
            level.blockEvent(worldPosition, getBlockState().getBlock(), 2, 1);
        }
        if (craftDone) {
            syncTile();
            level.blockEvent(worldPosition, getBlockState().getBlock(), 99, 0);
        }
        setChanged();
        if (stacksize <= 0) {
            return ItemStack.EMPTY;
        }
        return item.copyWithCount(stacksize);
    }

    public void attemptSmelt(ItemEntity entity) {
        Entity owner = entity.getOwner();
        ItemStack res = attemptSmelt(entity.getItem(), owner instanceof Player p ? p : null);
        if (res.isEmpty()) {
            entity.discard();
        } else {
            entity.setItem(res);
        }
    }

    public float getFluidHeight() {
        float base = 0.3f + 0.5f * (tank.getFluidAmount() / (float) tank.getCapacity());
        float out = base + aspects.visSize() / (float) maxTags * (1.0f - base);
        if (out > 1.0f) {
            out = 1.001f;
        }
        if (out == 1.0f) {
            out = 0.9999f;
        }
        return out;
    }

    /** Every 5 seconds (and when over 500) one point of a random aspect boils off as flux. */
    public void spillRandom() {
        if (aspects.size() > 0) {
            Aspect tag = aspects.getAspects()[level.random.nextInt(aspects.getAspects().length)];
            aspects.remove(tag, 1);
            AuraHelper.polluteAura(level, worldPosition, tag == Aspect.FLUX ? 1.0f : 0.25f, true);
        }
        setChanged();
        syncTile();
    }

    /** Empties water and aspects; the aspects become flux. */
    public void spillRemnants() {
        int vs = aspects.visSize();
        if (tank.getFluidAmount() > 0 || vs > 0) {
            tank.setFluid(FluidStack.EMPTY);
            AuraHelper.polluteAura(level, worldPosition, vs * 0.25f, true);
            int f = aspects.getAmount(Aspect.FLUX);
            if (f > 0) {
                AuraHelper.polluteAura(level, worldPosition, f * 0.75f, false);
            }
            aspects = new AspectList();
            level.blockEvent(worldPosition, getBlockState().getBlock(), 2, 5);
            setChanged();
            syncTile();
        }
    }

    /** Client effects sent with level.blockEvent. */
    @Override
    public boolean triggerEvent(int i, int j) {
        if (i == 99) {
            if (level.isClientSide) {
                FXDispatcher.drawBamf(worldPosition.getX() + 0.5, worldPosition.getY() + 1.25f, worldPosition.getZ() + 0.5, true, true, Direction.UP);
                level.playLocalSound(worldPosition, SoundsTC.SPILL.get(), SoundSource.BLOCKS, 0.2f, 1.0f, false);
            }
            return true;
        }
        if (i == 1) {
            if (level.isClientSide) {
                FXDispatcher.drawBamf(worldPosition.above(), true, true, Direction.UP);
            }
            return true;
        }
        if (i == 2) {
            level.playLocalSound(worldPosition, SoundsTC.SPILL.get(), SoundSource.BLOCKS, 0.2f, 1.0f, false);
            if (level.isClientSide) {
                for (int q = 0; q < 10; ++q) {
                    FXDispatcher.crucibleBoil(worldPosition, this, j);
                }
            }
            return true;
        }
        return super.triggerEvent(i, j);
    }

    // IAspectContainer: only so goggles of revealing show the melted aspects; nothing can be put in or taken out.

    @Override
    public AspectList getAspects() {
        return aspects;
    }

    @Override
    public void setAspects(AspectList aspects) {
    }

    @Override
    public int addToContainer(Aspect tag, int amount) {
        return 0;
    }

    @Override
    public boolean takeFromContainer(Aspect tag, int amount) {
        return false;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect tag, int amount) {
        return false;
    }

    @Override
    public int containerContains(Aspect tag) {
        return 0;
    }

    @Override
    public boolean doesContainerAccept(Aspect tag) {
        return true;
    }
}
