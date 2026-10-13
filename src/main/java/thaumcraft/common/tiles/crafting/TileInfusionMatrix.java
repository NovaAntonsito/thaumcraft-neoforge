package thaumcraft.common.tiles.crafting;

import net.minecraft.server.level.ServerPlayer;
import thaumcraft.common.lib.network.playerdata.PacketSyncKnowledge;
import thaumcraft.api.capabilities.PlayerKnowledge;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.particles.DustParticleOptions;
import org.joml.Vector3f;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.network.PacketDistributor;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.aura.AuraHelper;
import thaumcraft.api.capabilities.PlayerWarp;
import thaumcraft.api.casters.IInteractWithCaster;
import thaumcraft.api.crafting.IInfusionStabiliser;
import thaumcraft.api.crafting.IInfusionStabiliserExt;
import thaumcraft.api.crafting.InfusionRecipe;
import thaumcraft.common.blocks.basic.BlockPillar;
import thaumcraft.common.blocks.devices.BlockPedestal;
import thaumcraft.common.lib.InternalMethodHandler;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.crafting.ThaumcraftCraftingManager;
import thaumcraft.common.lib.events.EssentiaHandler;
import thaumcraft.common.lib.network.fx.PacketFXBlockArc;
import thaumcraft.common.lib.network.fx.PacketFXInfusionSource;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.tiles.TileThaumcraft;
import thaumcraft.common.tiles.devices.TileStabilizer;

/**
 * Port of TileInfusionMatrix, the runic matrix that runs infusion crafting.
 * <p>
 * Right-clicking it with a caster first activates it (the altar must be valid: a pedestal two blocks below and
 * a pillar at each lower corner), then starts the recipe on the central pedestal. Each cycle drains one essentia
 * from nearby jars, then consumes the pedestal items one by one, then finishes. Stability is gained from
 * symmetric stabilizers (candles, skulls) and lost each cycle according to the recipe's instability; while
 * negative, random instability events can fire.
 * <p>
 * TODO: research (!INSTABILITY), flux goo, the flux taint and vis exhaust potions, the eldritch and ancient
 * pillars and pedestals, matrix speed and cost upgrades, the instability stabilizer, XP recipes (recipeType 1),
 * and the FXDispatcher effects (stage 10).
 */
public class TileInfusionMatrix extends TileThaumcraft implements IInteractWithCaster, IAspectContainer {
    private static final DecimalFormat myFormatter = new DecimalFormat("#######.##");

    private final List<BlockPos> pedestals = new ArrayList<>();
    public boolean active = false;
    public boolean crafting = false;
    public boolean checkSurroundings = true;
    public float costMult = 0.0f;
    private int cycleTime = 20;
    public int stabilityCap = 25;
    public float stability = 0.0f;
    public float stabilityReplenish = 0.0f;
    private AspectList recipeEssentia = new AspectList();
    private List<ItemStack> recipeIngredients = new ArrayList<>();
    private ItemStack recipeOutput = ItemStack.EMPTY;
    @Nullable
    private String recipePlayer = null;
    private ItemStack recipeInput = ItemStack.EMPTY;
    private int recipeInstability = 0;
    public final Map<String, SourceFX> sourceFX = new HashMap<>();
    public int count = 0;
    public int craftCount = 0;
    public float startUp;
    private int countDelay = cycleTime / 2;
    private int itemCount = 0;
    private final List<BlockPos> problemBlocks = new ArrayList<>();
    private final Map<Block, Integer> tempBlockCount = new HashMap<>();

    public TileInfusionMatrix(BlockPos pos, BlockState state) {
        super(TCBlockEntities.INFUSION_MATRIX.get(), pos, state);
    }

    @Override
    protected void readSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        active = tag.getBoolean("active");
        crafting = tag.getBoolean("crafting");
        stability = tag.getFloat("stability");
        recipeInstability = tag.getInt("recipeinst");
        recipeEssentia = AspectList.CODEC.parse(NbtOps.INSTANCE, tag.get("Aspects")).result().orElseGet(AspectList::new);
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putBoolean("active", active);
        tag.putBoolean("crafting", crafting);
        tag.putFloat("stability", stability);
        tag.putInt("recipeinst", recipeInstability);
        AspectList.CODEC.encodeStart(NbtOps.INSTANCE, recipeEssentia).result().ifPresent(t -> tag.put("Aspects", t));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        recipeIngredients = new ArrayList<>();
        ListTag list = tag.getList("recipein", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); ++i) {
            ItemStack.parse(registries, list.getCompound(i)).ifPresent(recipeIngredients::add);
        }
        recipeOutput = tag.contains("recipeout") ? ItemStack.parseOptional(registries, tag.getCompound("recipeout")) : ItemStack.EMPTY;
        recipeInput = tag.contains("recipeinput") ? ItemStack.parseOptional(registries, tag.getCompound("recipeinput")) : ItemStack.EMPTY;
        recipePlayer = tag.getString("recipeplayer");
        if (recipePlayer.isEmpty()) {
            recipePlayer = null;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!recipeIngredients.isEmpty()) {
            ListTag list = new ListTag();
            for (ItemStack stack : recipeIngredients) {
                if (!stack.isEmpty()) list.add(stack.save(registries));
            }
            tag.put("recipein", list);
        }
        if (!recipeOutput.isEmpty()) {
            tag.put("recipeout", recipeOutput.save(registries));
        }
        if (!recipeInput.isEmpty()) {
            tag.put("recipeinput", recipeInput.save(registries));
        }
        tag.putString("recipeplayer", recipePlayer == null ? "" : recipePlayer);
    }

    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition).inflate(0.1);
    }

    public enum EnumStability {
        VERY_STABLE, STABLE, UNSTABLE, VERY_UNSTABLE
    }

    private EnumStability getStability() {
        return stability > stabilityCap / 2 ? EnumStability.VERY_STABLE
                : stability >= 0.0f ? EnumStability.STABLE
                : stability > -25.0f ? EnumStability.UNSTABLE : EnumStability.VERY_UNSTABLE;
    }

    private float getModFromCurrentStability() {
        return switch (getStability()) {
            case VERY_STABLE -> 5.0f;
            case STABLE -> 6.0f;
            case UNSTABLE -> 7.0f;
            case VERY_UNSTABLE -> 8.0f;
        };
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TileInfusionMatrix tile) {
        tile.update();
    }

    private void update() {
        ++count;
        if (checkSurroundings) {
            checkSurroundings = false;
            getSurroundings();
        }
        if (level.isClientSide) {
            doEffects();
        } else {
            if (count % (crafting ? 20 : 100) == 0 && !validLocation()) {
                active = false;
                setChanged();
                syncTile(false);
                return;
            }
            if (active && !crafting && stability < stabilityCap && count % Math.max(5, countDelay) == 0) {
                stability += Math.max(0.1f, stabilityReplenish);
                if (stability > stabilityCap) {
                    stability = stabilityCap;
                }
                setChanged();
                syncTile(false);
            }
            if (active && crafting && count % countDelay == 0) {
                craftCycle();
                setChanged();
            }
        }
    }

    public boolean validLocation() {
        return level.getBlockState(worldPosition.offset(0, -2, 0)).getBlock() instanceof BlockPedestal
                && level.getBlockState(worldPosition.offset(1, -2, 1)).getBlock() instanceof BlockPillar
                && level.getBlockState(worldPosition.offset(-1, -2, 1)).getBlock() instanceof BlockPillar
                && level.getBlockState(worldPosition.offset(1, -2, -1)).getBlock() instanceof BlockPillar
                && level.getBlockState(worldPosition.offset(-1, -2, -1)).getBlock() instanceof BlockPillar;
    }

    @Nullable
    private TilePedestal getPedestal(BlockPos p) {
        return level.getBlockEntity(p) instanceof TilePedestal ped ? ped : null;
    }

    public void craftingStart(Player player) {
        if (!validLocation()) {
            active = false;
            setChanged();
            syncTile(false);
            return;
        }
        getSurroundings();
        recipeInput = ItemStack.EMPTY;
        TilePedestal center = getPedestal(worldPosition.below(2));
        if (center != null && !center.getItem(0).isEmpty()) {
            recipeInput = center.getItem(0).copy();
        }
        if (recipeInput.isEmpty()) {
            return;
        }
        List<ItemStack> components = new ArrayList<>();
        for (BlockPos cc : pedestals) {
            TilePedestal ped = getPedestal(cc);
            if (ped != null && !ped.getItem(0).isEmpty()) {
                components.add(ped.getItem(0).copy());
            }
        }
        if (components.isEmpty()) {
            return;
        }
        InfusionRecipe recipe = ThaumcraftCraftingManager.findMatchingInfusionRecipe(level, components, recipeInput, player);
        if (costMult < 0.5) {
            costMult = 0.5f;
        }
        if (recipe != null) {
            recipeIngredients = components;
            recipeOutput = recipe.getRecipeOutput(player, recipeInput, components);
            recipeInstability = recipe.getInstability(player, recipeInput, components);
            AspectList al = recipe.getAspects(player, recipeInput, components);
            AspectList al2 = new AspectList();
            for (Aspect as : al.getAspects()) {
                if ((int) (al.getAmount(as) * costMult) > 0) {
                    al2.add(as, (int) (al.getAmount(as) * costMult));
                }
            }
            recipeEssentia = al2;
            recipePlayer = player.getGameProfile().getName();
            crafting = true;
            level.playSound(null, worldPosition, SoundsTC.CRAFTSTART.get(), SoundSource.BLOCKS, 0.5f, 1.0f);
            syncTile(false);
            setChanged();
        }
    }

    private float getLossPerCycle() {
        return recipeInstability / getModFromCurrentStability();
    }

    /** ThaumcraftInvHelper.areItemStacksEqualForCrafting. */
    private static boolean sameForCrafting(ItemStack a, ItemStack b) {
        return ItemStack.isSameItemSameComponents(a, b);
    }

    public void craftCycle() {
        boolean valid = false;
        float ff = level.random.nextFloat() * getLossPerCycle();
        stability -= ff;
        stability += stabilityReplenish;
        if (stability < -100.0f) {
            stability = -100.0f;
        }
        if (stability > stabilityCap) {
            stability = stabilityCap;
        }
        TilePedestal center = getPedestal(worldPosition.below(2));
        if (center != null && !center.getItem(0).isEmpty() && sameForCrafting(center.getItem(0), recipeInput)) {
            valid = true;
        }
        if (!valid || (stability < 0.0f && level.random.nextInt(1500) <= Math.abs(stability))) {
            switch (level.random.nextInt(24)) {
                case 0, 1, 2, 3 -> inEvEjectItem(0);
                case 4, 5, 6 -> inEvWarp();
                case 7, 8, 9 -> inEvZap(false);
                case 10, 11 -> inEvZap(true);
                case 12, 13 -> inEvEjectItem(1);
                case 14, 15 -> inEvEjectItem(2);
                case 16 -> inEvEjectItem(3);
                case 17 -> inEvEjectItem(4);
                case 18, 19 -> inEvHarm(false);
                case 20, 21 -> inEvEjectItem(5);
                case 22 -> inEvHarm(true);
                case 23 -> level.explode(null, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5,
                        1.5f + level.random.nextFloat(), Level.ExplosionInteraction.NONE);
                default -> {
                }
            }
            stability += 5.0f + level.random.nextFloat() * 5.0f;
            inResAdd();
            if (valid) {
                return;
            }
        }
        if (!valid) {
            crafting = false;
            recipeEssentia = new AspectList();
            recipeInstability = 0;
            syncTile(false);
            level.playSound(null, worldPosition, SoundsTC.CRAFTFAIL.get(), SoundSource.BLOCKS, 1.0f, 0.6f);
            setChanged();
            return;
        }
        if (countDelay < 1) {
            countDelay = 1;
        }
        if (recipeEssentia.visSize() > 0) {
            for (Aspect aspect : recipeEssentia.getAspects()) {
                int na = recipeEssentia.getAmount(aspect);
                if (na > 0) {
                    if (EssentiaHandler.drainEssentia(this, aspect, null, 12, na > 1 ? countDelay : 0)) {
                        recipeEssentia.reduce(aspect, 1);
                        syncTile(false);
                        setChanged();
                        return;
                    }
                    stability -= 0.25f;
                    syncTile(false);
                    setChanged();
                }
            }
            checkSurroundings = true;
            return;
        }
        if (!recipeIngredients.isEmpty()) {
            for (int a = 0; a < recipeIngredients.size(); ++a) {
                for (BlockPos cc : pedestals) {
                    TilePedestal ped = getPedestal(cc);
                    if (ped != null && !ped.getItem(0).isEmpty() && sameForCrafting(ped.getItem(0), recipeIngredients.get(a))) {
                        if (itemCount == 0) {
                            itemCount = 5;
                            sendFX(new PacketFXInfusionSource(worldPosition, cc, 0), worldPosition);
                        } else if (itemCount-- <= 1) {
                            ItemStack is = ped.getItem(0).getCraftingRemainingItem();
                            ped.setItem(0, is.isEmpty() ? ItemStack.EMPTY : is.copy());
                            recipeIngredients.remove(a);
                            setChanged();
                        }
                        return;
                    }
                }
                Aspect[] ingEss = recipeEssentia.getAspects();
                if (ingEss.length > 0 && level.random.nextInt(1 + a) == 0) {
                    Aspect as = ingEss[level.random.nextInt(ingEss.length)];
                    recipeEssentia.add(as, 1);
                    stability -= 0.25f;
                    syncTile(false);
                    setChanged();
                }
            }
            return;
        }
        crafting = false;
        craftingFinish(recipeOutput);
        recipeOutput = ItemStack.EMPTY;
        syncTile(false);
        setChanged();
    }

    private void sendFX(net.minecraft.network.protocol.common.custom.CustomPacketPayload payload, BlockPos at) {
        if (level instanceof ServerLevel server) {
            PacketDistributor.sendToPlayersNear(server, null, at.getX(), at.getY(), at.getZ(), 32.0, payload);
        }
    }

    private AABB area() {
        return new AABB(worldPosition).inflate(10.0);
    }

    private float arcColor() {
        return 0.3f - level.random.nextFloat() * 0.1f;
    }

    private void inEvZap(boolean all) {
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area())) {
            sendFX(new PacketFXBlockArc(worldPosition, target, arcColor(), 0.0f, arcColor()), worldPosition);
            target.hurt(level.damageSources().magic(), 4 + level.random.nextInt(4));
            if (!all) {
                break;
            }
        }
    }

    /** TODO: applies PotionFluxTaint or PotionVisExhaust; potions are not ported yet, so this only zaps lightly. */
    private void inEvHarm(boolean all) {
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area())) {
            sendFX(new PacketFXBlockArc(worldPosition, target, arcColor(), 0.0f, arcColor()), worldPosition);
            target.hurt(level.damageSources().magic(), 1.0f);
            if (!all) {
                break;
            }
        }
    }

    private void inResAdd() {
        for (Player player : level.getEntitiesOfClass(Player.class, area())) {
            PlayerKnowledge knowledge = ThaumcraftCapabilities.getKnowledge(player);
            if (!knowledge.isResearchKnown("!INSTABILITY")) {
                knowledge.addResearch("!INSTABILITY");
                if (player instanceof ServerPlayer sp) PacketSyncKnowledge.sync(sp);
                player.displayClientMessage(Component.translatable("got.instability").withStyle(ChatFormatting.DARK_PURPLE), true);
            }
        }
    }

    private void inEvWarp() {
        List<Player> targets = level.getEntitiesOfClass(Player.class, area());
        if (!targets.isEmpty()) {
            Player target = targets.get(level.random.nextInt(targets.size()));
            if (level.random.nextFloat() < 0.25f) {
                InternalMethodHandler.addWarpToPlayer(target, 1, PlayerWarp.EnumWarpType.NORMAL);
            } else {
                InternalMethodHandler.addWarpToPlayer(target, 2 + level.random.nextInt(4), PlayerWarp.EnumWarpType.TEMPORARY);
            }
        }
    }

    private void inEvEjectItem(int type) {
        for (int retries = 0; retries < 25 && !pedestals.isEmpty(); ++retries) {
            BlockPos cc = pedestals.get(level.random.nextInt(pedestals.size()));
            TilePedestal ped = getPedestal(cc);
            if (ped != null && !ped.getItem(0).isEmpty()) {
                // A charged vis inlay lets an infusion stabilizer absorb the event.
                BlockPos stabPos = ped.findInstabilityMitigator();
                if (stabPos != null && level.getBlockEntity(stabPos) instanceof TileStabilizer tste
                        && tste.mitigate(Mth.nextInt(level.random, 5, 10))) {
                    level.blockEvent(cc, level.getBlockState(cc).getBlock(), 5, 0);
                    sendFX(new PacketFXBlockArc(worldPosition, cc.above(), arcColor(), 0.0f, arcColor()), cc);
                    sendFX(new PacketFXBlockArc(cc.above(), stabPos, arcColor(), 0.0f, arcColor()), stabPos);
                    return;
                }
                if (type <= 3 || type == 5) {
                    Containers.dropContents(level, cc.above(), ped);
                    ped.clearContent();
                } else {
                    ped.setItem(0, ItemStack.EMPTY);
                }
                if (type == 1 || type == 3) {
                    // TODO: places BlocksTC.fluxGoo above the pedestal; flux goo is not ported yet.
                    level.playSound(null, cc, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 0.3f, 1.0f);
                } else if (type == 2 || type == 4) {
                    int a = 5 + level.random.nextInt(5);
                    AuraHelper.polluteAura(level, cc, a, true);
                } else if (type == 5) {
                    level.explode(null, cc.getX() + 0.5, cc.getY() + 0.5, cc.getZ() + 0.5, 1.0f, Level.ExplosionInteraction.NONE);
                }
                level.blockEvent(cc, level.getBlockState(cc).getBlock(), 11, 0);
                sendFX(new PacketFXBlockArc(worldPosition, cc.above(), arcColor(), 0.0f, arcColor()), cc);
                return;
            }
        }
    }

    public void craftingFinish(ItemStack out) {
        TilePedestal ped = getPedestal(worldPosition.below(2));
        if (ped == null) {
            return;
        }
        ItemStack current = ped.getItem(0);
        if (!out.isEmpty()) {
            ItemStack qs = out.copy();
            if (current.isDamageableItem() && current.isDamaged()) {
                float dmg = current.getDamageValue() / (float) current.getMaxDamage();
                if (qs.isDamageableItem() && !qs.isDamaged()) {
                    qs.setDamageValue((int) (qs.getMaxDamage() * dmg));
                }
            }
            ped.setInventorySlotContentsFromInfusion(0, qs);
        }
        if (recipePlayer != null && level.getServer() != null) {
            Player p = level.getServer().getPlayerList().getPlayerByName(recipePlayer);
            if (p != null) {
                EventHooks.firePlayerCraftingEvent(p, ped.getItem(0), new SimpleContainer(recipeIngredients.toArray(new ItemStack[0])));
            }
        }
        recipeEssentia = new AspectList();
        recipeInstability = 0;
        syncTile(false);
        setChanged();
        level.blockEvent(worldPosition.below(2), level.getBlockState(worldPosition.below(2)).getBlock(), 12, 0);
        level.playSound(null, worldPosition, SoundsTC.WAND.get(), SoundSource.BLOCKS, 0.5f, 1.0f);
    }

    private void getSurroundings() {
        Set<Long> stuff = new HashSet<>();
        pedestals.clear();
        tempBlockCount.clear();
        problemBlocks.clear();
        cycleTime = 10;
        stabilityReplenish = 0.0f;
        costMult = 1.0f;
        for (int xx = -8; xx <= 8; ++xx) {
            for (int zz = -8; zz <= 8; ++zz) {
                for (int yy = -3; yy <= 7; ++yy) {
                    if (xx == 0 && zz == 0) continue;
                    BlockPos bp = new BlockPos(worldPosition.getX() + xx, worldPosition.getY() - yy, worldPosition.getZ() + zz);
                    Block bi = level.getBlockState(bp).getBlock();
                    if (bi instanceof BlockPedestal) {
                        pedestals.add(bp);
                    }
                    if (bi instanceof AbstractSkullBlock || (bi instanceof IInfusionStabiliser st && st.canStabaliseInfusion(level, bp))) {
                        stuff.add(bp.asLong());
                    }
                }
            }
        }
        while (!stuff.isEmpty()) {
            long lp = stuff.iterator().next();
            BlockPos c1 = BlockPos.of(lp);
            int x2 = worldPosition.getX() - c1.getX();
            int z2 = worldPosition.getZ() - c1.getZ();
            BlockPos c2 = new BlockPos(worldPosition.getX() + x2, c1.getY(), worldPosition.getZ() + z2);
            Block sb1 = level.getBlockState(c1).getBlock();
            Block sb2 = level.getBlockState(c2).getBlock();
            float amt1 = 0.1f;
            float amt2 = 0.1f;
            if (sb1 instanceof IInfusionStabiliserExt ext) {
                amt1 = ext.getStabilizationAmount(level, c1);
            }
            if (sb2 instanceof IInfusionStabiliserExt ext) {
                amt2 = ext.getStabilizationAmount(level, c2);
            }
            // Skull blocks differ between floor and wall variants; the original had one skull block.
            boolean same = sb1 == sb2 || (sb1 instanceof AbstractSkullBlock s1 && sb2 instanceof AbstractSkullBlock s2 && s1.getType() == s2.getType());
            if (same && amt1 == amt2) {
                if (sb1 instanceof IInfusionStabiliserExt ext && ext.hasSymmetryPenalty(level, c1, c2)) {
                    stabilityReplenish -= ext.getSymmetryPenalty(level, c1);
                    problemBlocks.add(c1);
                } else {
                    stabilityReplenish += calcDeminishingReturns(sb1, amt1);
                }
            } else {
                stabilityReplenish -= Math.max(amt1, amt2);
                problemBlocks.add(c1);
            }
            stuff.remove(c2.asLong());
            stuff.remove(lp);
        }
        // TODO: ancient and eldritch pillars, matrix speed/cost blocks below the corners, ancient and eldritch pedestals.
        countDelay = cycleTime / 2;
    }

    private float calcDeminishingReturns(Block b, float base) {
        float bb = base;
        int c = tempBlockCount.getOrDefault(b, 0);
        if (c > 0) {
            bb *= (float) Math.pow(0.75, c);
        }
        tempBlockCount.put(b, c + 1);
        return bb;
    }

    @Override
    public boolean onCasterRightClick(Level world, ItemStack wandstack, Player player, BlockPos pos, Direction side, InteractionHand hand) {
        if (world.isClientSide && active && !crafting) {
            checkSurroundings = true;
        }
        if (!world.isClientSide && active && !crafting) {
            craftingStart(player);
            return false;
        }
        if (!world.isClientSide && !active && validLocation()) {
            world.playSound(null, pos, SoundsTC.CRAFTSTART.get(), SoundSource.BLOCKS, 0.5f, 1.0f);
            active = true;
            syncTile(false);
            setChanged();
            return false;
        }
        return false;
    }

    /** Client: called from PacketFXInfusionSource. */
    public void addSourceFX(BlockPos loc, int color, int ticks) {
        String key = loc.getX() + ":" + loc.getY() + ":" + loc.getZ() + ":" + color;
        SourceFX sf = sourceFX.get(key);
        if (sf != null) {
            sf.ticks = ticks;
        } else {
            sourceFX.put(key, new SourceFX(loc, ticks, color));
        }
    }

    private void doEffects() {
        if (crafting) {
            if (craftCount == 0) {
                level.playLocalSound(worldPosition, SoundsTC.INFUSERSTART.get(), SoundSource.BLOCKS, 0.5f, 1.0f, false);
            } else if (craftCount % 65 == 0) {
                level.playLocalSound(worldPosition, SoundsTC.INFUSER.get(), SoundSource.BLOCKS, 0.5f, 1.0f, false);
            }
            ++craftCount;
            // TODO: FXDispatcher.blockRunes on the central pedestal (stage 10).
            if (level.random.nextInt(3) == 0) {
                level.addParticle(ParticleTypes.ENCHANT, worldPosition.getX() + level.random.nextFloat(), worldPosition.getY() - 1.0,
                        worldPosition.getZ() + level.random.nextFloat(), 0, -0.5, 0);
            }
        } else if (craftCount > 0) {
            craftCount -= 2;
            if (craftCount < 0) {
                craftCount = 0;
            }
            if (craftCount > 50) {
                craftCount = 50;
            }
        }
        if (active && startUp != 1.0f) {
            if (startUp < 1.0f) {
                startUp += Math.max(startUp / 10.0f, 0.001f);
            }
            if (startUp > 0.999) {
                startUp = 1.0f;
            }
        }
        if (!active && startUp > 0.0f) {
            startUp -= startUp / 10.0f;
            if (startUp < 0.001) {
                startUp = 0.0f;
            }
        }
        double mx = worldPosition.getX() + 0.5, my = worldPosition.getY() + 0.5, mz = worldPosition.getZ() + 0.5;
        for (String fxk : sourceFX.keySet().toArray(new String[0])) {
            SourceFX fx = sourceFX.get(fxk);
            if (fx.ticks <= 0) {
                sourceFX.remove(fxk);
                continue;
            }
            // TODO: drawInfusionParticles1-4 are custom particles (stage 10). Enchant glyphs fly from the source to the matrix.
            if (fx.loc.equals(worldPosition)) {
                Entity player = level.getEntity(fx.color);
                if (player != null) {
                    for (int a = 0; a < 4; ++a) {
                        double px = player.getX() + (level.random.nextFloat() - level.random.nextFloat()) * player.getBbWidth();
                        double py = player.getBoundingBox().minY + level.random.nextFloat() * player.getBbHeight();
                        double pz = player.getZ() + (level.random.nextFloat() - level.random.nextFloat()) * player.getBbWidth();
                        level.addParticle(ParticleTypes.ENCHANT, mx, my, mz, px - mx, py - my, pz - mz);
                    }
                }
            } else if (level.getBlockEntity(fx.loc) instanceof TilePedestal ped) {
                ItemStack is = ped.getItem(0);
                if (!is.isEmpty()) {
                    double px = fx.loc.getX() + 0.5, py = fx.loc.getY() + 1.25, pz = fx.loc.getZ() + 0.5;
                    if (level.random.nextInt(3) == 0) {
                        level.addParticle(ParticleTypes.ENCHANT, mx, my, mz, px + level.random.nextFloat() - 0.5 - mx,
                                py + level.random.nextFloat() - 0.5 - my, pz + level.random.nextFloat() - 0.5 - mz);
                    } else {
                        for (int a = 0; a < 2; ++a) {
                            double vx = (mx - px) * 0.08, vy = (my - py) * 0.08 + 0.1, vz = (mz - pz) * 0.08;
                            if (is.getItem() instanceof BlockItem bi) {
                                level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, bi.getBlock().defaultBlockState()), px, py, pz, vx, vy, vz);
                            } else {
                                level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, is), px, py, pz, vx, vy, vz);
                            }
                        }
                    }
                }
            } else {
                fx.ticks = 0;
            }
            --fx.ticks;
        }
        if (crafting && stability < 0.0f && level.random.nextInt(250) <= Math.abs(stability)) {
            spark(worldPosition.getX() + level.random.nextFloat(), worldPosition.getY() + level.random.nextFloat(),
                    worldPosition.getZ() + level.random.nextFloat(), 0.7f + level.random.nextFloat() * 0.1f, 0.1f, 0.65f + level.random.nextFloat() * 0.1f);
        }
        if (active && !problemBlocks.isEmpty() && level.random.nextInt(25) == 0) {
            BlockPos p = problemBlocks.get(level.random.nextInt(problemBlocks.size()));
            spark(p.getX() + level.random.nextFloat(), p.getY() + level.random.nextFloat(), p.getZ() + level.random.nextFloat(),
                    0.7f + level.random.nextFloat() * 0.1f, 0.1f, 0.65f + level.random.nextFloat() * 0.1f);
        }
    }

    /** FXDispatcher.spark stand-in. */
    private void spark(double x, double y, double z, float r, float g, float b) {
        level.addParticle(new DustParticleOptions(new Vector3f(r, g, b), 1.5f), x, y, z, 0, 0, 0);
        level.addParticle(ParticleTypes.ELECTRIC_SPARK, x, y, z, 0, 0, 0);
    }

    // ---- IAspectContainer: shows the essentia still needed ----------------------------------------

    @Override
    public AspectList getAspects() {
        return recipeEssentia;
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

    /** IGogglesDisplayExtended.getIGogglesText: the stability readout. */
    public List<Component> getIGogglesText() {
        List<Component> out = new ArrayList<>();
        out.add(Component.translatable("stability." + getStability().name()).withStyle(ChatFormatting.BOLD));
        out.add(Component.literal(myFormatter.format(stabilityReplenish) + " ").append(Component.translatable("stability.gain"))
                .withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
        float lpc = getLossPerCycle();
        if (lpc != 0.0f) {
            out.add(Component.translatable("stability.range").withStyle(ChatFormatting.RED)
                    .append(Component.literal(myFormatter.format(lpc) + " ").append(Component.translatable("stability.loss"))
                            .withStyle(ChatFormatting.RED, ChatFormatting.ITALIC)));
        }
        return out;
    }

    public static class SourceFX {
        public BlockPos loc;
        public int ticks;
        public int color;

        public SourceFX(BlockPos loc, int ticks, int color) {
            this.loc = loc;
            this.ticks = ticks;
            this.color = color;
        }
    }
}
