package thaumcraft.common.entities;

import java.util.UUID;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.registry.TCEntities;
import thaumcraft.common.world.voidrealm.VoidEncounters;

/**
 * Dream branch: a void rift of the Whispers of the Void. It looks like a flux rift, but it is steady: it does not
 * eat blocks, grow, feed on flux or collapse. Walking into it takes a player into the labyrinth of its encounter.
 * It closes once its owner has read that encounter's journal page.
 */
public class EntityVoidRift extends EntityFluxRift {
    private int mazeX;
    private int mazeZ;
    private int encounter = 1;
    private UUID owner;
    private boolean entered;

    public EntityVoidRift(EntityType<? extends EntityVoidRift> type, Level world) {
        super(type, world);
    }

    public EntityVoidRift(Level world) {
        this(TCEntities.VOID_RIFT.get(), world);
    }

    public void setMaze(int x, int z, int encounter, UUID owner) {
        this.mazeX = x;
        this.mazeZ = z;
        this.encounter = encounter;
        this.owner = owner;
    }

    public int getMazeX() {
        return mazeX;
    }

    public int getMazeZ() {
        return mazeZ;
    }

    public boolean wasEntered() {
        return entered;
    }

    public int getEncounter() {
        return encounter;
    }

    @Override
    public void tick() {
        baseTick();
        if (lastSize != getRiftSize()) setSize();
        Level level = level();
        if (level instanceof ServerLevel sl) {
            if (getRiftSeed() == 0) setRiftSeed(random.nextInt());
            if (tickCount % 5 == 0) {
                for (ServerPlayer p : sl.getEntitiesOfClass(ServerPlayer.class, getBoundingBox().inflate(0.5))) {
                    if (p.isPassenger() || p.isVehicle()) continue;
                    if (p.isOnPortalCooldown()) {
                        p.setPortalCooldown(100);
                    } else {
                        entered = true;
                        VoidEncounters.enter(p, this);
                    }
                }
            }
            if (tickCount % 100 == 0 && owner != null && VoidEncounters.isDone(sl, owner, encounter)) {
                sl.sendParticles(ParticleTypes.REVERSE_PORTAL, getX(), getY(), getZ(), 80, 1.0, 1.0, 1.0, 0.3);
                sl.playSound(null, blockPosition(), SoundsTC.CRAFTFAIL.get(), SoundSource.HOSTILE, 1.0f, 0.5f);
                discard();
                return;
            }
            if (tickCount % 300 == 0) {
                playSound(SoundsTC.WHISPERS.get(), 0.4f, 0.6f + random.nextFloat() * 0.2f);
            }
        } else if (points.size() > 2 && random.nextInt(4) == 0) {
            int pi = 1 + random.nextInt(points.size() - 2);
            var v1 = points.get(pi).add(getX(), getY(), getZ());
            thaumcraft.client.fx.FXDispatcher.drawCurlyWisp(v1.x, v1.y, v1.z, 0.0, 0.0, 0.0, 0.1f + pointsWidth.get(pi) * 3.0f,
                    0.4f, 0.1f, 0.6f, 0.4f, null, 1, 0, 0);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("MazeX", mazeX);
        tag.putInt("MazeZ", mazeZ);
        tag.putInt("Encounter", encounter);
        if (owner != null) tag.putUUID("Owner", owner);
        tag.putBoolean("Entered", entered);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        mazeX = tag.getInt("MazeX");
        mazeZ = tag.getInt("MazeZ");
        encounter = Math.max(1, tag.getInt("Encounter"));
        owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        entered = tag.getBoolean("Entered");
    }
}
