package thaumcraft.common.golems.parts;

import net.minecraft.world.entity.LivingEntity;
import thaumcraft.api.golems.IGolemAPI;
import thaumcraft.api.golems.parts.GolemLeg;
import thaumcraft.client.fx.ClientFX;

public class GolemLegLevitator implements GolemLeg.ILegFunction {
    @Override
    public void onUpdateTick(IGolemAPI golem) {
        LivingEntity g = golem.getGolemEntity();
        if (golem.getGolemWorld().isClientSide && (!g.onGround() || g.tickCount % 5 == 0)) {
            thaumcraft.client.fx.FXDispatcher.drawGolemFlyParticles(g.getX(), g.getY() + 0.1, g.getZ(),
                    golem.getGolemWorld().random.nextGaussian() / 100.0, -0.1, golem.getGolemWorld().random.nextGaussian() / 100.0);
        }
    }
}
