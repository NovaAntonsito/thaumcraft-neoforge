package thaumcraft.common.tiles.misc;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import thaumcraft.Config;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectHelper;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aura.AuraHelper;
import thaumcraft.api.nodes.INode;
import thaumcraft.api.nodes.NodeModifier;
import thaumcraft.api.nodes.NodeType;
import thaumcraft.common.blocks.world.taint.TaintHelper;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.items.casters.wands.ItemWand;
import thaumcraft.common.lib.network.fx.PacketFXBlockZap;
import thaumcraft.common.network.TCNetwork;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.registry.TCEntities;
import thaumcraft.common.tiles.TileThaumcraft;
import thaumcraft.common.world.biomes.TCBiomes;

/**
 * Port of the Thaumcraft 4 TileNode: an aura node in the air. It holds aspects that regenerate up to their base
 * amounts, drains weaker nodes nearby and, by its type, changes its surroundings. A wand drains its aspects as vis.
 * Thaumcraft 6 has no nodes; on the dream branch they also feed the chunk aura ({@link #handleAura}).
 */
public class TileAuraNode extends TileThaumcraft implements INode, thaumcraft.api.items.IGogglesDisplayExtended {
    long lastActive = 0L;
    AspectList aspects = new AspectList();
    AspectList aspectsBase = new AspectList();
    private NodeType nodeType = NodeType.NORMAL;
    @Nullable
    private NodeModifier nodeModifier = null;
    int count = 0;
    int regeneration = -1;
    int wait = 0;
    @Nullable
    String id = null;
    byte nodeLock = 0;
    boolean catchUp = false;
    /** Client side: the player draining the node with a wand, for the vis stream. */
    @Nullable
    public Entity drainEntity = null;
    public int drainColor = 0xFFFFFF;

    public TileAuraNode(BlockPos pos, BlockState state) {
        super(TCBlockEntities.AURA_NODE.get(), pos, state);
    }

    @Override
    public String getId() {
        if (id == null) {
            id = level == null ? "?" : level.dimension().location() + ":" + worldPosition.getX() + ":" + worldPosition.getY() + ":" + worldPosition.getZ();
        }
        return id;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TileAuraNode node) {
        node.tick();
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, TileAuraNode node) {
        node.tick();
    }

    private void tick() {
        boolean change = false;
        change = handleHungryNodeFirst(change);
        ++count;
        checkLock();
        if (!level.isClientSide) {
            change = handleDischarge(change);
            change = handleRecharge(change);
            change = handleTaintNode(change);
            change = handleNodeStability(change);
            change = handleDarkNode(change);
            change = handlePureNode(change);
            change = handleHungryNodeSecond(change);
            handleAura();
            if (change && !isRemoved()) {
                setChanged();
                syncTile(false);
            }
        }
    }

    public void nodeChange() {
        regeneration = -1;
        setChanged();
        syncTile(false);
    }

    public double getDistanceTo(double x, double y, double z) {
        double dx = worldPosition.getX() + 0.5 - x;
        double dy = worldPosition.getY() + 0.5 - y;
        double dz = worldPosition.getZ() + 0.5 - z;
        return dx * dx + dy * dy + dz * dz;
    }

    // ---- IAspectContainer and INode ----

    @Override
    public AspectList getAspects() {
        return aspects;
    }

    @Override
    public AspectList getAspectsBase() {
        return aspectsBase;
    }

    @Override
    public void setAspects(AspectList aspects) {
        this.aspects = aspects.copy();
        this.aspectsBase = aspects.copy();
    }

    @Override
    public int addToContainer(Aspect aspect, int amount) {
        aspects.add(aspect, amount);
        return 0;
    }

    @Override
    public boolean takeFromContainer(Aspect aspect, int amount) {
        return aspects.reduce(aspect, amount);
    }

    @Override
    public boolean doesContainerContainAmount(Aspect tag, int amount) {
        return aspects.getAmount(tag) >= amount;
    }

    @Override
    public int containerContains(Aspect tag) {
        return aspects.getAmount(tag);
    }

    @Override
    public boolean doesContainerAccept(Aspect tag) {
        return true;
    }

    /** A random primal aspect of the node, taken from it. */
    @Nullable
    public Aspect takeRandomPrimalFromSource() {
        List<Aspect> primals = new ArrayList<>();
        for (Aspect a : aspects.getAspects()) {
            if (a.isPrimal() && aspects.getAmount(a) > 0) primals.add(a);
        }
        if (primals.isEmpty()) return null;
        Aspect asp = primals.get(level.random.nextInt(primals.size()));
        return takeFromContainer(asp, 1) ? asp : null;
    }

    /** A random aspect of the node that is in the filter; with preserve, only aspects with more than one left. */
    @Nullable
    public Aspect chooseRandomFilteredFromSource(@Nullable AspectList filter, boolean preserve) {
        int min = preserve ? 1 : 0;
        List<Aspect> valid = new ArrayList<>();
        for (Aspect prim : aspects.getAspects()) {
            if ((filter == null || filter.getAmount(prim) > 0) && aspects.getAmount(prim) > min) valid.add(prim);
        }
        return valid.isEmpty() ? null : valid.get(level.random.nextInt(valid.size()));
    }

    @Override
    public NodeType getNodeType() {
        return nodeType;
    }

    @Override
    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    @Override
    public void setNodeModifier(@Nullable NodeModifier nodeModifier) {
        this.nodeModifier = nodeModifier;
    }

    @Nullable
    @Override
    public NodeModifier getNodeModifier() {
        return nodeModifier;
    }

    @Override
    public int getNodeVisBase(Aspect aspect) {
        return aspectsBase.getAmount(aspect);
    }

    @Override
    public void setNodeVisBase(Aspect aspect, short nodeVisBase) {
        if (aspectsBase.getAmount(aspect) < nodeVisBase) {
            aspectsBase.merge(aspect, nodeVisBase);
        } else {
            aspectsBase.reduce(aspect, aspectsBase.getAmount(aspect) - nodeVisBase);
        }
    }

    public byte getLock() {
        return nodeLock;
    }

    /** With goggles: the kind of node, as the Thaumcraft 4 thaumometer named it. */
    @Override
    public List<net.minecraft.network.chat.Component> getIGogglesText() {
        net.minecraft.network.chat.MutableComponent name = net.minecraft.network.chat.Component.translatable("tc.node.name",
                net.minecraft.network.chat.Component.translatable("nodetype." + nodeType.name() + ".name"));
        if (nodeModifier != null) {
            name = net.minecraft.network.chat.Component.translatable("nodemod." + nodeModifier.name() + ".name").append(" ").append(name);
        }
        return List.of(name.withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE));
    }

    // ---- Saving ----

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        lastActive = tag.getLong("lastActive");
        // Back from an unloaded chunk: regenerate for the time it was away, as the original did.
        catchUp = true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("lastActive", lastActive);
    }

    @Override
    protected void readSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        nodeType = NodeType.byId(tag.getByte("type"));
        nodeModifier = NodeModifier.byId(tag.getByte("modifier"));
        aspects = AspectList.CODEC.parse(NbtOps.INSTANCE, tag.get("Aspects")).result().orElseGet(AspectList::new);
        aspectsBase = AspectList.CODEC.parse(NbtOps.INSTANCE, tag.get("AspectsBase")).result().orElseGet(AspectList::new);
        drainColor = tag.getInt("draincolor");
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putByte("type", (byte) nodeType.ordinal());
        tag.putByte("modifier", (byte) (nodeModifier == null ? -1 : nodeModifier.ordinal()));
        AspectList.CODEC.encodeStart(NbtOps.INSTANCE, aspects).result().ifPresent(t -> tag.put("Aspects", t));
        AspectList.CODEC.encodeStart(NbtOps.INSTANCE, aspectsBase).result().ifPresent(t -> tag.put("AspectsBase", t));
        tag.putInt("draincolor", drainColor);
    }

    // ---- Wand tapping ----

    /**
     * onUsingWandTick: every 5 ticks of holding use on the node, 1 to 3 points of a random aspect go into the wand
     * as vis (NODETAPPER1 and NODETAPPER2 add one each). With NODEPRESERVE, and a wand better than wood and iron,
     * the last point of each aspect is left unless sneaking. Returns false when the player stopped looking at it.
     */
    public boolean onUsingWandTick(ItemStack wandstack, Player player, int count) {
        HitResult mop = player.pick(player.blockInteractionRange(), 1.0f, false);
        if (!(mop instanceof BlockHitResult bhr) || mop.getType() != HitResult.Type.BLOCK || !bhr.getBlockPos().equals(worldPosition)) {
            drainEntity = null;
            return false;
        }
        if (count % 5 == 0) {
            int tap = 1;
            if (thaumcraft.api.capabilities.ThaumcraftCapabilities.knowsResearch(player, "NODETAPPER1")) ++tap;
            if (thaumcraft.api.capabilities.ThaumcraftCapabilities.knowsResearch(player, "NODETAPPER2")) ++tap;
            thaumcraft.common.items.casters.wands.WandParts parts = ItemWand.getParts(wandstack);
            boolean preserve = !player.isShiftKeyDown() && thaumcraft.api.capabilities.ThaumcraftCapabilities.knowsResearch(player, "NODEPRESERVE")
                    && !parts.rod().equals("wood") && !parts.cap().equals("iron");
            boolean success = false;
            Aspect aspect = ItemWand.getRoom(wandstack) > 0 ? chooseRandomFilteredFromSource(null, preserve) : null;
            if (aspect != null) {
                int amt = aspects.getAmount(aspect);
                if (tap > amt) tap = amt;
                if (preserve && tap == amt) --tap;
                if (tap > 0) {
                    int rem = ItemWand.addCharge(wandstack, tap, !level.isClientSide);
                    if (rem < tap) {
                        drainColor = aspect.getColor();
                        if (!level.isClientSide) {
                            takeFromContainer(aspect, tap - rem);
                            setChanged();
                            syncTile(false);
                        }
                        success = true;
                    }
                }
            }
            drainEntity = success ? player : null;
            if (success && level.isClientSide) {
                thaumcraft.client.fx.FXDispatcher.visSparkle(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                        (int) Math.floor(player.getX()), (int) Math.floor(player.getEyeY()), (int) Math.floor(player.getZ()), drainColor);
            }
        }
        return true;
    }

    // ---- The original's update steps ----

    private boolean handleHungryNodeFirst(boolean change) {
        if (nodeType != NodeType.HUNGRY || !Config.HARD_NODE.get()) {
            return change;
        }
        List<Entity> ents = level.getEntitiesOfClass(Entity.class, new AABB(worldPosition).inflate(15.0));
        for (Entity eo : ents) {
            if (eo instanceof Player p && (p.getAbilities().invulnerable || p.isSpectator())) continue;
            if (eo.isAlive() && !eo.isInvulnerable()) {
                double d = getDistanceTo(eo.getX(), eo.getY(), eo.getZ());
                if (d < 2.0 && !level.isClientSide) {
                    eo.hurt(level.damageSources().fellOutOfWorld(), 1.0f);
                    if (!eo.isAlive()) {
                        AspectList al = AspectHelper.getEntityAspects(eo);
                        if (al != null && al.size() > 0) {
                            al = AspectHelper.reduceToPrimals(al.copy());
                            if (al.size() > 0) {
                                Aspect a = al.getAspects()[level.random.nextInt(al.size())];
                                if (aspects.getAmount(a) < getNodeVisBase(a)) {
                                    addToContainer(a, 1);
                                    change = true;
                                } else if (level.random.nextInt(1 + getNodeVisBase(a) * 2) < al.getAmount(a)) {
                                    aspectsBase.add(a, 1);
                                    change = true;
                                }
                            }
                        }
                    }
                }
            }
            double var3 = (worldPosition.getX() + 0.5 - eo.getX()) / 15.0;
            double var5 = (worldPosition.getY() + 0.5 - eo.getY()) / 15.0;
            double var7 = (worldPosition.getZ() + 0.5 - eo.getZ()) / 15.0;
            double var9 = Math.sqrt(var3 * var3 + var5 * var5 + var7 * var7);
            double var11 = 1.0 - var9;
            if (var11 > 0.0 && var9 > 0.0) {
                var11 *= var11;
                eo.setDeltaMovement(eo.getDeltaMovement().add(var3 / var9 * var11 * 0.15, var5 / var9 * var11 * 0.25, var7 / var9 * var11 * 0.15));
            }
        }
        return change;
    }

    private boolean handleDischarge(boolean change) {
        if (getBlockState().getBlock() != ConfigBlocks.auraNode.get() || getLock() == 1 || nodeModifier == NodeModifier.FADING) {
            return change;
        }
        boolean shiny = nodeType == NodeType.HUNGRY || nodeModifier == NodeModifier.BRIGHT;
        int inc = nodeModifier == null ? 2 : (shiny ? 1 : (nodeModifier == NodeModifier.PALE ? 3 : 2));
        if (count % inc != 0) {
            return change;
        }
        RandomSource rand = level.random;
        int x = rand.nextInt(5) - rand.nextInt(5);
        int y = rand.nextInt(5) - rand.nextInt(5);
        int z = rand.nextInt(5) - rand.nextInt(5);
        if (nodeModifier == NodeModifier.PALE && rand.nextBoolean()) {
            return change;
        }
        if (x == 0 && y == 0 && z == 0) {
            return change;
        }
        BlockPos op = worldPosition.offset(x, y, z);
        if (!(level.getBlockEntity(op) instanceof TileAuraNode other) || level.getBlockState(op).getBlock() != ConfigBlocks.auraNode.get()) {
            return change;
        }
        if (other.getLock() > 0) {
            return change;
        }
        int ndavg = (other.getAspects().visSize() + other.getAspectsBase().visSize()) / 2;
        int thisavg = (aspects.visSize() + aspectsBase.visSize()) / 2;
        if (ndavg < thisavg && other.getAspects().size() > 0) {
            Aspect a = other.getAspects().getAspects()[rand.nextInt(other.getAspects().size())];
            boolean u = false;
            if (aspects.getAmount(a) < getNodeVisBase(a) && other.takeFromContainer(a, 1)) {
                addToContainer(a, 1);
                u = true;
            } else if (other.takeFromContainer(a, 1)) {
                if (rand.nextInt(1 + (int) (getNodeVisBase(a) / (shiny ? 1.5 : 1.0))) == 0) {
                    aspectsBase.add(a, 1);
                    if (nodeModifier == NodeModifier.PALE && rand.nextInt(100) == 0) {
                        nodeModifier = null;
                        regeneration = -1;
                    }
                    if (rand.nextInt(3) == 0) {
                        other.setNodeVisBase(a, (short) (other.getNodeVisBase(a) - 1));
                    }
                }
                u = true;
            }
            if (u) {
                other.wait = other.regeneration / 2;
                other.setChanged();
                other.syncTile(false);
                change = true;
                TCNetwork.sendToAllAround(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), 32.0,
                        new PacketFXBlockZap(op.getX() + 0.5f, op.getY() + 0.5f, op.getZ() + 0.5f,
                                worldPosition.getX() + 0.5f, worldPosition.getY() + 0.5f, worldPosition.getZ() + 0.5f));
            }
        }
        return change;
    }

    private boolean handleRecharge(boolean change) {
        if (regeneration < 0) {
            regeneration = 600;
            if (nodeModifier != null) {
                switch (nodeModifier) {
                    case BRIGHT -> regeneration = 400;
                    case PALE -> regeneration = 900;
                    case FADING -> regeneration = 0;
                }
            }
            if (getLock() == 1) regeneration *= 2;
            if (getLock() == 2) regeneration *= 20;
        }
        if (catchUp) {
            catchUp = false;
            long ct = System.currentTimeMillis();
            int inc = regeneration * 75;
            int amt = inc > 0 && lastActive > 0 ? (int) ((ct - lastActive) / inc) : 0;
            for (int a = 0; a < Math.min(amt, aspectsBase.visSize()); ++a) {
                regenerateOne();
            }
        }
        if (count % 1200 == 0) {
            for (Aspect aspect : aspects.getAspects()) {
                if (aspects.getAmount(aspect) <= 0) {
                    setNodeVisBase(aspect, (short) (getNodeVisBase(aspect) - 1));
                    if (level.random.nextInt(20) == 0 || getNodeVisBase(aspect) <= 0) {
                        aspects.remove(aspect);
                        if (level.random.nextInt(5) == 0) {
                            if (nodeModifier == NodeModifier.BRIGHT) {
                                nodeModifier = null;
                            } else if (nodeModifier == null) {
                                nodeModifier = NodeModifier.PALE;
                            }
                            if (nodeModifier == NodeModifier.PALE && level.random.nextInt(5) == 0) {
                                nodeModifier = NodeModifier.FADING;
                            }
                        }
                        nodeChange();
                        break;
                    }
                    nodeChange();
                }
            }
            if (aspects.size() <= 0) {
                if (getBlockState().getBlock() == ConfigBlocks.auraNode.get()) {
                    level.removeBlock(worldPosition, false);
                }
                return false;
            }
        }
        if (wait > 0) --wait;
        if (regeneration > 0 && wait == 0 && count % regeneration == 0) {
            lastActive = System.currentTimeMillis();
            if (regenerateOne()) change = true;
        }
        return change;
    }

    /** One point back into a random aspect that is below its base. */
    private boolean regenerateOne() {
        AspectList al = new AspectList();
        for (Aspect aspect : aspects.getAspects()) {
            if (aspects.getAmount(aspect) < getNodeVisBase(aspect)) al.add(aspect, 1);
        }
        if (al.size() > 0) {
            addToContainer(al.getAspects()[level.random.nextInt(al.size())], 1);
            return true;
        }
        return false;
    }

    /**
     * handleTaintNode: a tainted node spreads taint fibres around it. 1.21 has no taint biome, so the original's
     * biome spread is left out, and a node turns tainted when its chunk has more flux than half its aura base.
     */
    private boolean handleTaintNode(boolean change) {
        if (nodeType == NodeType.TAINTED && count % 50 == 0) {
            if (Config.HARD_NODE.get() && level.random.nextBoolean()) {
                BlockPos p = worldPosition.offset(level.random.nextInt(5) - level.random.nextInt(5),
                        level.random.nextInt(5) - level.random.nextInt(5), level.random.nextInt(5) - level.random.nextInt(5));
                TaintHelper.spreadFibres(level, p);
            }
        } else if (nodeType != NodeType.PURE && nodeType != NodeType.TAINTED && count % 100 == 0) {
            if (AuraHelper.getFlux(level, worldPosition) > AuraHelper.getAuraBase(level, worldPosition) / 2.0f && level.random.nextInt(500) == 0) {
                nodeType = NodeType.TAINTED;
                nodeChange();
            }
        }
        return change;
    }

    /**
     * handleNodeStability: an unstable node throws out a point of a primal aspect now and then. The original made an
     * aspect orb of it, which 1.21 does not have; here the point goes into the chunk aura as vis. A lock calms it.
     */
    private boolean handleNodeStability(boolean change) {
        if (count % 100 == 0) {
            if (nodeType == NodeType.UNSTABLE && level.random.nextBoolean()) {
                if (getLock() == 0) {
                    if (takeRandomPrimalFromSource() != null) {
                        AuraHelper.addVis(level, worldPosition, 1.0f);
                        TCNetwork.sendToAllAround(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), 32.0,
                                new PacketFXBlockZap(worldPosition.getX() + 0.5f, worldPosition.getY() + 0.5f, worldPosition.getZ() + 0.5f,
                                        worldPosition.getX() + 0.5f + (level.random.nextFloat() - 0.5f) * 4.0f,
                                        worldPosition.getY() + 0.5f + (level.random.nextFloat() - 0.5f) * 4.0f,
                                        worldPosition.getZ() + 0.5f + (level.random.nextFloat() - 0.5f) * 4.0f));
                        change = true;
                    }
                } else if (level.random.nextInt(10000 / getLock()) == 42) {
                    nodeType = NodeType.NORMAL;
                    change = true;
                }
            }
            if (nodeModifier == NodeModifier.FADING && getLock() > 0 && level.random.nextInt(12500 / getLock()) == 69) {
                nodeModifier = NodeModifier.PALE;
                change = true;
            }
        }
        return change;
    }

    /**
     * handlePureNode: the original turned taint biome back into magical forest, or the land around a silverwood
     * node. Neither exists here (no taint biome, no node in the log), so a pure node cleans the aura instead
     * ({@link #handleAura}).
     */
    private boolean handlePureNode(boolean change) {
        return change;
    }

    /** handleDarkNode: in the overworld, spreads the eerie biome and, with hardNode, calls giant brainy zombies. */
    private boolean handleDarkNode(boolean change) {
        if (nodeType != NodeType.DARK || count % 50 != 0 || level.dimension() != Level.OVERWORLD || !(level instanceof ServerLevel sl)) {
            return change;
        }
        int x = worldPosition.getX() + level.random.nextInt(12) - level.random.nextInt(12);
        int z = worldPosition.getZ() + level.random.nextInt(12) - level.random.nextInt(12);
        thaumcraft.common.lib.utils.Utils.setBiomeAt(sl, new BlockPos(x, worldPosition.getY(), z), TCBiomes.EERIE);
        if (Config.HARD_NODE.get() && level.random.nextBoolean()
                && level.getNearestPlayer(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, 24.0, false) != null) {
            int j = level.getEntitiesOfClass(thaumcraft.common.entities.monster.EntityGiantBrainyZombie.class,
                    new AABB(worldPosition).inflate(10.0, 6.0, 10.0)).size();
            if (j <= 3) {
                thaumcraft.common.entities.monster.EntityGiantBrainyZombie entity = TCEntities.GIANT_BRAINY_ZOMBIE.get().create(level);
                if (entity != null) {
                    double d0 = worldPosition.getX() + (level.random.nextDouble() - level.random.nextDouble()) * 5.0;
                    double d3 = worldPosition.getY() + level.random.nextInt(3) - 1;
                    double d4 = worldPosition.getZ() + (level.random.nextDouble() - level.random.nextDouble()) * 5.0;
                    entity.moveTo(d0, d3, d4, level.random.nextFloat() * 360.0f, 0.0f);
                    if (entity.checkSpawnRules(level, MobSpawnType.SPAWNER) && entity.checkSpawnObstruction(level)) {
                        entity.finalizeSpawn(sl, level.getCurrentDifficultyAt(entity.blockPosition()), MobSpawnType.SPAWNER, null);
                        level.addFreshEntity(entity);
                        level.levelEvent(2004, worldPosition, 0);
                        entity.spawnAnim();
                    }
                }
            }
        }
        return change;
    }

    /** handleHungryNodeSecond: every 50 ticks breaks a soft block in sight within 16 blocks. */
    private boolean handleHungryNodeSecond(boolean change) {
        if (nodeType != NodeType.HUNGRY || count % 50 != 0) {
            return change;
        }
        int tx = worldPosition.getX() + level.random.nextInt(16) - level.random.nextInt(16);
        int ty = worldPosition.getY() + level.random.nextInt(16) - level.random.nextInt(16);
        int tz = worldPosition.getZ() + level.random.nextInt(16) - level.random.nextInt(16);
        int h = level.getHeight(Heightmap.Types.MOTION_BLOCKING, tx, tz);
        if (ty > h) ty = h;
        Vec3 v1 = Vec3.atCenterOf(worldPosition);
        Vec3 v2 = new Vec3(tx + 0.5, ty + 0.5, tz + 0.5);
        BlockHitResult mop = level.clip(new ClipContext(v1, v2, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
        if (mop.getType() == HitResult.Type.BLOCK && mop.getBlockPos().distSqr(worldPosition) < 256.0) {
            BlockPos bp = mop.getBlockPos();
            BlockState bs = level.getBlockState(bp);
            if (!bs.isAir()) {
                float hard = bs.getDestroySpeed(level, bp);
                if (hard >= 0.0f && hard < 5.0f) {
                    level.destroyBlock(bp, true);
                }
            }
        }
        return change;
    }

    /**
     * Not in Thaumcraft 4: every 5 seconds the node feeds the chunk aura with a hundredth of its aspects as vis,
     * up to the aura base plus half its aspects. A hungry node eats vis instead, a tainted node adds flux and a
     * pure node removes it.
     */
    private void handleAura() {
        if ((count + worldPosition.hashCode()) % 100 != 0) {
            return;
        }
        switch (nodeType) {
            case HUNGRY -> AuraHelper.drainVis(level, worldPosition, 1.0f, false);
            case TAINTED -> AuraHelper.polluteAura(level, worldPosition, 0.25f, false);
            case PURE -> AuraHelper.drainFlux(level, worldPosition, 0.25f, false);
            default -> {
            }
        }
        if (nodeType == NodeType.HUNGRY) {
            return;
        }
        int total = aspects.visSize();
        float cap = AuraHelper.getAuraBase(level, worldPosition) + total / 2.0f;
        float vis = AuraHelper.getVis(level, worldPosition);
        if (total > 0 && vis < cap) {
            AuraHelper.addVis(level, worldPosition, Math.min(total / 100.0f, cap - vis));
        }
    }

    /**
     * checkLock: a node stabilizer below the node slows its regeneration and stops it from draining or being
     * drained. The stabilizers come with the node jar; until then nodes are never locked.
     */
    public void checkLock() {
        nodeLock = 0;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        drainEntity = null;
    }

    /** For callers that want the node at a position, if there is one. */
    @Nullable
    public static TileAuraNode at(Level level, BlockPos pos) {
        BlockEntity te = level.getBlockEntity(pos);
        return te instanceof TileAuraNode n ? n : null;
    }
}
