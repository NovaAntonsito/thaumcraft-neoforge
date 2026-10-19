package thaumcraft.common.tiles.eldritch;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.entities.IEldritchMob;
import thaumcraft.common.lib.utils.EntityUtils;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.tiles.TileThaumcraft;

/**
 * Port of TileEldritchObelisk: eldritch mobs within 6 blocks get Strength and Regeneration, and wisps run from the
 * obelisk to them.
 */
public class TileEldritchObelisk extends TileThaumcraft {
    private int counter = 0;

    public TileEldritchObelisk(BlockPos pos, BlockState state) {
        super(TCBlockEntities.ELDRITCH_OBELISK.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TileEldritchObelisk tile) {
        double x = pos.getX() + 0.5;
        double z = pos.getZ() + 0.5;
        // The original never increased counter, so the check below passes every tick.
        if (!level.isClientSide && tile.counter % 20 == 0) {
            for (LivingEntity e : EntityUtils.<LivingEntity>getEntitiesInRange(level, x, pos.getY(), z, null, LivingEntity.class, 6.0)) {
                if (e instanceof IEldritchMob && !e.hasEffect(MobEffects.REGENERATION)) {
                    e.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 40, 0, true, true));
                    e.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, true, true));
                }
            }
        }
        if (level.isClientSide) {
            for (LivingEntity e : EntityUtils.<LivingEntity>getEntitiesInRange(level, x, pos.getY(), z, null, LivingEntity.class, 6.0)) {
                if (e instanceof IEldritchMob) {
                    thaumcraft.client.fx.FXDispatcher.wispFXEG(x, pos.getY() + 1 + level.random.nextFloat() * 3.0f, z, e);
                }
            }
        }
    }
}
