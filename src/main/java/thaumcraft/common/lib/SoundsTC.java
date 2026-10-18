package thaumcraft.common.lib;

import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.SoundType;
import net.neoforged.neoforge.common.util.DeferredSoundType;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.Thaumcraft;

/** Port of SoundsTC. Only the sounds used so far; the rest come with their features. */
public final class SoundsTC {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, Thaumcraft.MODID);

    public static final Supplier<SoundEvent> BUBBLE = register("bubble");
    public static final Supplier<SoundEvent> SPILL = register("spill");
    public static final Supplier<SoundEvent> DUST = register("dust");
    public static final Supplier<SoundEvent> JAR = register("jar");
    public static final Supplier<SoundEvent> PAGE = register("page");
    public static final Supplier<SoundEvent> KEY = register("key");
    public static final Supplier<SoundEvent> WRITE = register("write");
    public static final Supplier<SoundEvent> CLACK = register("clack");
    public static final Supplier<SoundEvent> POOF = register("poof");
    public static final Supplier<SoundEvent> CRAFTSTART = register("craftstart");
    public static final Supplier<SoundEvent> CRAFTFAIL = register("craftfail");
    public static final Supplier<SoundEvent> INFUSER = register("infuser");
    public static final Supplier<SoundEvent> INFUSERSTART = register("infuserstart");
    public static final Supplier<SoundEvent> WAND = register("wand");
    public static final Supplier<SoundEvent> WANDFAIL = register("wandfail");
    public static final Supplier<SoundEvent> ZAP = register("zap");
    public static final Supplier<SoundEvent> WIND = register("wind");
    public static final Supplier<SoundEvent> HHON = register("hhon");
    public static final Supplier<SoundEvent> HHOFF = register("hhoff");
    public static final Supplier<SoundEvent> ERASE = register("erase");
    public static final Supplier<SoundEvent> LEARN = register("learn");
    public static final Supplier<SoundEvent> SCAN = register("scan");
    public static final Supplier<SoundEvent> TICKS = register("ticks");
    public static final Supplier<SoundEvent> CREAK = register("creak");
    public static final Supplier<SoundEvent> COINS = register("coins");
    public static final Supplier<SoundEvent> SQUEEK = register("squeek");
    public static final Supplier<SoundEvent> TOOL = register("tool");
    public static final Supplier<SoundEvent> PUMP = register("pump");
    public static final Supplier<SoundEvent> EVILPORTAL = register("evilportal");
    public static final Supplier<SoundEvent> BRAIN = register("brain");
    public static final Supplier<SoundEvent> CRYSTAL = register("crystal");
    public static final Supplier<SoundEvent> GORE = register("gore");
    public static final Supplier<SoundEvent> WISPLIVE = register("wisplive");
    public static final Supplier<SoundEvent> WISPDEAD = register("wispdead");
    public static final Supplier<SoundEvent> PECH_IDLE = register("pech_idle");
    public static final Supplier<SoundEvent> PECH_TRADE = register("pech_trade");
    public static final Supplier<SoundEvent> PECH_DICE = register("pech_dice");
    public static final Supplier<SoundEvent> PECH_HIT = register("pech_hit");
    public static final Supplier<SoundEvent> PECH_DEATH = register("pech_death");
    public static final Supplier<SoundEvent> PECH_CHARGE = register("pech_charge");
    public static final Supplier<SoundEvent> TENTACLE = register("tentacle");
    public static final Supplier<SoundEvent> SWARM = register("swarm");
    public static final Supplier<SoundEvent> SWARMATTACK = register("swarmattack");
    public static final Supplier<SoundEvent> FLY = register("fly");
    public static final Supplier<SoundEvent> SHOCK = register("shock");
    public static final Supplier<SoundEvent> MONOLITH = register("monolith");
    public static final Supplier<SoundEvent> EGATTACK = register("egattack");
    public static final Supplier<SoundEvent> CHANT = register("chant");
    public static final Supplier<SoundEvent> CRABCLAW = register("crabclaw");
    public static final Supplier<SoundEvent> CRABDEATH = register("crabdeath");
    public static final Supplier<SoundEvent> CRABTALK = register("crabtalk");
    public static final Supplier<SoundEvent> EGIDLE = register("egidle");
    public static final Supplier<SoundEvent> EGDEATH = register("egdeath");
    public static final Supplier<SoundEvent> EGSCREECH = register("egscreech");
    public static final Supplier<SoundEvent> HEARTBEAT = register("heartbeat");
    public static final Supplier<SoundEvent> URNBREAK = register("urnbreak");
    public static final Supplier<SoundEvent> JACOBS = register("jacobs");
    public static final Supplier<SoundEvent> WHISPERS = register("whispers");

    /** SoundsTC.JAR of the original: the block sound type of jars. */
    public static final SoundType SOUND_TYPE_JAR = new DeferredSoundType(0.5f, 1.0f, JAR, JAR, JAR, JAR, JAR);
    /** SoundsTC.CRYSTAL of the original: the block sound type of crystal clusters. */
    public static final SoundType SOUND_TYPE_CRYSTAL = new DeferredSoundType(0.5f, 1.0f, CRYSTAL, CRYSTAL, CRYSTAL, CRYSTAL, CRYSTAL);

    /** SoundsTC.URN of the original: the block sound type of the loot urns. */
    public static final SoundType SOUND_TYPE_URN = new DeferredSoundType(0.5f, 1.5f, URNBREAK, URNBREAK, URNBREAK, URNBREAK, URNBREAK);

    /** SoundsTC.GORE of the original: the block sound type of taint. */
    public static final SoundType SOUND_TYPE_GORE = new DeferredSoundType(0.5f, 1.0f, GORE, GORE, GORE, GORE, GORE);

    private SoundsTC() {
    }

    private static Supplier<SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Thaumcraft.MODID, name)));
    }
}
