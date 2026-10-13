package thaumcraft.common.tiles.devices;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.registry.TCDataComponents;
import thaumcraft.common.tiles.TileThaumcraft;

/**
 * Port of TileJarBrain. Pulls experience orbs within 8 blocks and eats those that touch the jar, up to 2000 xp.
 * On the client the brain turns toward the nearest player and sighs now and then.
 * <p>
 * 1.21: only the server eats orbs; the client only moves them.
 */
public class TileJarBrain extends TileThaumcraft {
    public static final int XP_MAX = 2000;
    private static final RandomSource RAND = RandomSource.create();

    public float field_40063_b;
    public float field_40061_d;
    public float field_40059_f;
    public float field_40066_q;
    public float rota;
    public float rotb;
    public int xp = 0;
    public int eatDelay = 0;
    private long lastsigh = System.currentTimeMillis() + 1500L;

    public TileJarBrain(BlockPos pos, BlockState state) {
        super(TCBlockEntities.JAR_BRAIN.get(), pos, state);
    }

    @Override
    protected void readSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        xp = tag.getInt("XP");
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("XP", xp);
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput input) {
        super.applyImplicitComponents(input);
        xp = input.getOrDefault(TCDataComponents.XP.get(), 0);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (xp > 0) {
            components.set(TCDataComponents.XP.get(), xp);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void removeComponentsFromTag(CompoundTag tag) {
        tag.remove("XP");
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TileJarBrain tile) {
        tile.update();
    }

    private void update() {
        Entity entity = null;
        if (xp > XP_MAX) {
            xp = XP_MAX;
        }
        if (xp < XP_MAX) {
            entity = getClosestXPOrb();
            if (entity != null && eatDelay == 0) {
                double var3 = (worldPosition.getX() + 0.5 - entity.getX()) / 25.0;
                double var4 = (worldPosition.getY() + 0.5 - entity.getY()) / 25.0;
                double var5 = (worldPosition.getZ() + 0.5 - entity.getZ()) / 25.0;
                double var6 = Math.sqrt(var3 * var3 + var4 * var4 + var5 * var5);
                double var7 = 1.0 - var6;
                if (var7 > 0.0) {
                    var7 *= var7;
                    entity.setDeltaMovement(entity.getDeltaMovement().add(var3 / var6 * var7 * 0.3, var4 / var6 * var7 * 0.5,
                            var5 / var6 * var7 * 0.3));
                }
            }
        }
        if (level.isClientSide) {
            rotb = rota;
            if (entity == null) {
                entity = level.getNearestPlayer(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, 6.0, false);
                if (entity != null && lastsigh < System.currentTimeMillis()) {
                    level.playLocalSound(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, SoundsTC.BRAIN.get(),
                            SoundSource.AMBIENT, 0.15f, 0.8f + level.random.nextFloat() * 0.4f, false);
                    lastsigh = System.currentTimeMillis() + 5000L + level.random.nextInt(25000);
                }
            }
            if (entity != null) {
                double d = entity.getX() - (worldPosition.getX() + 0.5f);
                double d2 = entity.getZ() - (worldPosition.getZ() + 0.5f);
                field_40066_q = (float) Math.atan2(d2, d);
                field_40059_f += 0.1f;
                if (field_40059_f < 0.5f || RAND.nextInt(40) == 0) {
                    float f3 = field_40061_d;
                    do {
                        field_40061_d += RAND.nextInt(4) - RAND.nextInt(4);
                    } while (f3 == field_40061_d);
                }
            } else {
                field_40066_q += 0.01f;
            }
            while (rota >= 3.141593f) {
                rota -= 6.283185f;
            }
            while (rota < -3.141593f) {
                rota += 6.283185f;
            }
            while (field_40066_q >= 3.141593f) {
                field_40066_q -= 6.283185f;
            }
            while (field_40066_q < -3.141593f) {
                field_40066_q += 6.283185f;
            }
            float f4;
            for (f4 = field_40066_q - rota; f4 >= 3.141593f; f4 -= 6.283185f) {
            }
            while (f4 < -3.141593f) {
                f4 += 6.283185f;
            }
            rota += f4 * 0.04f;
        }
        if (eatDelay > 0) {
            --eatDelay;
        } else if (xp < XP_MAX && !level.isClientSide) {
            List<ExperienceOrb> ents = level.getEntitiesOfClass(ExperienceOrb.class, new AABB(worldPosition.getX() - 0.1, worldPosition.getY() - 0.1,
                    worldPosition.getZ() - 0.1, worldPosition.getX() + 1.1, worldPosition.getY() + 1.1, worldPosition.getZ() + 1.1));
            if (!ents.isEmpty()) {
                for (ExperienceOrb eo : ents) {
                    xp += eo.getValue();
                    eo.playSound(SoundEvents.GENERIC_EAT, 0.1f, (level.random.nextFloat() - level.random.nextFloat()) * 0.2f + 1.0f);
                    eo.discard();
                }
                syncTile(false);
                setChanged();
            }
        }
    }

    public Entity getClosestXPOrb() {
        double cdist = Double.MAX_VALUE;
        Entity orb = null;
        Vec3 c = Vec3.atCenterOf(worldPosition);
        for (ExperienceOrb eo : level.getEntitiesOfClass(ExperienceOrb.class, new AABB(worldPosition).inflate(8.0))) {
            double d = eo.distanceToSqr(c);
            if (d < cdist) {
                orb = eo;
                cdist = d;
            }
        }
        return orb;
    }
}
