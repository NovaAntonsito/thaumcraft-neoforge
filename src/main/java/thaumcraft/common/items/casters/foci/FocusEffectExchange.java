package thaumcraft.common.items.casters.foci;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.joml.Vector3f;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.casters.FocusEffect;
import thaumcraft.api.casters.IFocusBlockPicker;
import thaumcraft.api.casters.NodeSetting;
import thaumcraft.api.casters.Trajectory;
import thaumcraft.common.items.casters.ItemCaster;
import thaumcraft.common.lib.events.ServerEvents;

/**
 * Exchange effect: swaps the block hit for the block picked with the caster (sneak + right click), paying with one
 * from your inventory. 1.21: particles are gray dust until the FX stage.
 */
public class FocusEffectExchange extends FocusEffect implements IFocusBlockPicker {
    @Override
    public String getResearch() {
        return "FOCUSEXCHANGE";
    }

    @Override
    public String getKey() {
        return "thaumcraft.EXCHANGE";
    }

    @Override
    public Aspect getAspect() {
        return Aspect.EXCHANGE;
    }

    /** Copied as it is in the original: the comparison is never true, so this is always (fortune + 1) * 3 and silk touch is free. */
    @Override
    public int getComplexity() {
        return (5 + getSettingValue("silk") * 4 + getSettingValue("fortune") == 0) ? 0 : ((getSettingValue("fortune") + 1) * 3);
    }

    @Override
    public boolean execute(HitResult target, Trajectory trajectory, float finalPower, int num) {
        if (!(target instanceof BlockHitResult bhr) || target.getType() != HitResult.Type.BLOCK) {
            return false;
        }
        LivingEntity caster = getPackage().getCaster();
        ItemStack casterStack = ItemStack.EMPTY;
        if (caster.getMainHandItem().getItem() instanceof ItemCaster) {
            casterStack = caster.getMainHandItem();
        } else if (caster.getOffhandItem().getItem() instanceof ItemCaster) {
            casterStack = caster.getOffhandItem();
        }
        if (casterStack.isEmpty()) {
            return false;
        }
        boolean silk = getSettingValue("silk") > 0;
        int fortune = getSettingValue("fortune");
        ItemStack picked = ((ItemCaster) casterStack.getItem()).getPickedBlock(casterStack);
        if (caster instanceof Player player && !picked.isEmpty()) {
            Level world = getPackage().world;
            ServerEvents.addSwapper(world, bhr.getBlockPos(), world.getBlockState(bhr.getBlockPos()), picked, true, 0, player, true, false, 8038177, true,
                    silk, fortune, ServerEvents.DEFAULT_PREDICATE, 0.25f + (silk ? 0.25f : 0.0f) + fortune * 0.1f);
        }
        return true;
    }

    @Override
    public NodeSetting[] createSettings() {
        int[] silk = {0, 1};
        String[] silkDesc = {"focus.common.no", "focus.common.yes"};
        int[] fortune = {0, 1, 2, 3, 4};
        String[] fortuneDesc = {"focus.common.no", "I", "II", "III", "IV"};
        return new NodeSetting[] {new NodeSetting("fortune", "focus.common.fortune", new NodeSetting.NodeSettingIntList(fortune, fortuneDesc)),
                new NodeSetting("silk", "focus.common.silk", new NodeSetting.NodeSettingIntList(silk, silkDesc))};
    }

    @Override
    public void renderParticleFX(Level world, double x, double y, double z, double vx, double vy, double vz) {
        thaumcraft.client.fx.FocusFX.exchange(x, y, z, vx, vy, vz);
    }

    @Override
    public void onCast(Entity caster) {
        caster.level().playSound(null, caster.blockPosition().above(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.2f,
                2.0f + (float) (caster.level().random.nextGaussian() * 0.05));
    }
}
