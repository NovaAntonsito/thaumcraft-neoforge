package voidexpeditions.registry;

import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;
import voidexpeditions.VoidExpeditions;
import voidexpeditions.block.ExpeditionTerminalBlockEntity;

public final class VEBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, VoidExpeditions.MODID);

    public static final Supplier<BlockEntityType<ExpeditionTerminalBlockEntity>> TERMINAL = BLOCK_ENTITIES.register("terminal",
            () -> BlockEntityType.Builder.of(ExpeditionTerminalBlockEntity::new, VEBlocks.EXPEDITION_TERMINAL.get()).build(null));

    private VEBlockEntities() {
    }
}
