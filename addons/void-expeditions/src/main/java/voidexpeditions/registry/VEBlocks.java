package voidexpeditions.registry;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import voidexpeditions.VoidExpeditions;
import voidexpeditions.block.ExpeditionTerminalBlock;
import voidexpeditions.block.VoidGateRingBlock;
import voidexpeditions.block.VoidHorizonBlock;

public final class VEBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(VoidExpeditions.MODID);

    public static final DeferredBlock<ExpeditionTerminalBlock> EXPEDITION_TERMINAL = BLOCKS.register("expedition_terminal",
            () -> new ExpeditionTerminalBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK)
                    .strength(5.0f, 1200.0f).sound(SoundType.METAL).requiresCorrectToolForDrops().lightLevel(s -> 5)));

    public static final DeferredBlock<VoidGateRingBlock> VOID_GATE_RING = BLOCKS.register("void_gate_ring",
            () -> new VoidGateRingBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK)
                    .strength(25.0f, 1200.0f).sound(SoundType.STONE).requiresCorrectToolForDrops()
                    .lightLevel(s -> s.getValue(VoidGateRingBlock.FORMED) ? 9 : 0).pushReaction(PushReaction.BLOCK)));

    public static final DeferredBlock<VoidHorizonBlock> VOID_HORIZON = BLOCKS.register("void_horizon",
            () -> new VoidHorizonBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE)
                    .strength(-1.0f, 3600000.0f).noLootTable().sound(SoundType.GLASS).noCollission().noOcclusion()
                    .lightLevel(s -> 13).pushReaction(PushReaction.BLOCK)));

    private VEBlocks() {
    }
}
