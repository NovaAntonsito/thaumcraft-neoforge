package thaumcraft.common.registry;

import java.util.function.Supplier;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.Thaumcraft;

/**
 * Particle types. Only the sprites are taken from these: the particles themselves are made in ClientFX with the
 * entity they follow, which a particle type cannot carry.
 */
public final class TCParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE, Thaumcraft.MODID);

    /** The eight frames of FXSwarm, cut from textures/misc/particles.png (cells 7 to 14 of the fifth row). */
    public static final Supplier<SimpleParticleType> SWARM = PARTICLE_TYPES.register("swarm", () -> new SimpleParticleType(false));

    private TCParticles() {
    }
}
