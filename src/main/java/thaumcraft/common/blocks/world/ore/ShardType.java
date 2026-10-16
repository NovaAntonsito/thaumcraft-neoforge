package thaumcraft.common.blocks.world.ore;

import java.util.function.Supplier;

import net.minecraft.world.level.block.Block;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.config.ConfigBlocks;

/** Port of ShardType: the seven crystal clusters, by their old metadata order. */
public enum ShardType {
    AIR(0, "air", Aspect.AIR, () -> ConfigBlocks.crystalAir.get()),
    FIRE(1, "fire", Aspect.FIRE, () -> ConfigBlocks.crystalFire.get()),
    WATER(2, "water", Aspect.WATER, () -> ConfigBlocks.crystalWater.get()),
    EARTH(3, "earth", Aspect.EARTH, () -> ConfigBlocks.crystalEarth.get()),
    ORDER(4, "order", Aspect.ORDER, () -> ConfigBlocks.crystalOrder.get()),
    ENTROPY(5, "entropy", Aspect.ENTROPY, () -> ConfigBlocks.crystalEntropy.get()),
    FLUX(6, "flux", Aspect.FLUX, () -> ConfigBlocks.crystalTaint.get());

    private static final ShardType[] METADATA_LOOKUP = new ShardType[values().length];
    private final int metadata;
    private final String name;
    private final Aspect aspect;
    private final Supplier<Block> ore;

    ShardType(int metadata, String name, Aspect aspect, Supplier<Block> ore) {
        this.metadata = metadata;
        this.name = name;
        this.aspect = aspect;
        this.ore = ore;
    }

    public int getMetadata() {
        return metadata;
    }

    public Aspect getAspect() {
        return aspect;
    }

    public Block getOre() {
        return ore.get();
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static int getMetaByAspect(Aspect a) {
        ShardType[] values = values();
        for (int i = 0; i < values.length; ++i) {
            if (values[i].getAspect() == a) {
                return i;
            }
        }
        return -1;
    }

    public static ShardType byMetadata(int metadata) {
        if (metadata < 0 || metadata >= METADATA_LOOKUP.length) {
            metadata = 0;
        }
        return METADATA_LOOKUP[metadata];
    }

    static {
        for (ShardType type : values()) {
            METADATA_LOOKUP[type.getMetadata()] = type;
        }
    }
}
