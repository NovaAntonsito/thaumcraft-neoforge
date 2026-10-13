package thaumcraft.common.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import thaumcraft.Thaumcraft;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigItems;

/**
 * Fluids (ConfigBlocks.FluidPure). The purifying fluid flows like the BlockFluidClassic of the original and does not
 * make new sources. Its look (water textures, color 0x77FFCCAA) is set on the client in ThaumcraftClient.
 */
public final class TCFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, Thaumcraft.MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, Thaumcraft.MODID);

    /** FluidPure: luminosity 5, rarity RARE. */
    public static final DeferredHolder<FluidType, FluidType> PURIFYING_TYPE = FLUID_TYPES.register("purifying_fluid",
            () -> new FluidType(FluidType.Properties.create().descriptionId("fluid.thaumcraft.purifying_fluid").lightLevel(5)
                    .rarity(Rarity.RARE).canSwim(true).canDrown(true).canExtinguish(true).supportsBoating(true)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL).sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)));

    public static final DeferredHolder<Fluid, FlowingFluid> PURIFYING = FLUIDS.register("purifying_fluid",
            () -> new BaseFlowingFluid.Source(purifyingProps()));
    public static final DeferredHolder<Fluid, FlowingFluid> PURIFYING_FLOWING = FLUIDS.register("flowing_purifying_fluid",
            () -> new BaseFlowingFluid.Flowing(purifyingProps()));

    private static BaseFlowingFluid.Properties purifyingProps() {
        return new BaseFlowingFluid.Properties(PURIFYING_TYPE, PURIFYING, PURIFYING_FLOWING)
                .block(ConfigBlocks.purifyingFluid).bucket(ConfigItems.purifyingFluidBucket);
    }

    private TCFluids() {
    }
}
