package voidexpeditions.run;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import thaumcraft.Thaumcraft;
import thaumcraft.api.maze.IMazeWorld;
import thaumcraft.common.entities.EntityPermanentItem;
import voidexpeditions.registry.VEItems;

/**
 * The expedition labyrinths: their key room holds the void sigil that opens the way on, and a chest of the void
 * encounters' loot, deeper with the encounter and the difficulty. The guardians grow with both.
 */
public final class ExpeditionMazeWorld implements IMazeWorld {
    public static final ExpeditionMazeWorld INSTANCE = new ExpeditionMazeWorld();

    private ExpeditionMazeWorld() {
    }

    @Override
    public boolean ownsChunk(int cx, int cz) {
        return ExpeditionDim.ownsChunk(cx);
    }

    @Override
    public void decorateKeyRoom(WorldGenLevel level, RandomSource random, BlockPos center, int cx, int cz) {
        int encounter = ExpeditionDim.encounterAt(cz);
        Difficulty difficulty = ExpeditionDim.difficultyAt(cz);
        EntityPermanentItem sigil = new EntityPermanentItem(level.getLevel(), center.getX() + 0.5, center.getY() + 1.5,
                center.getZ() + 1.5, new ItemStack(VEItems.VOID_SIGIL.get()));
        sigil.setDeltaMovement(0.0, 0.0, 0.0);
        level.addFreshEntity(sigil);
        BlockPos chest = center.south(2);
        level.setBlock(chest, Blocks.CHEST.defaultBlockState(), 2);
        int depth = Math.min(5, (encounter + 1) / 2 + difficulty.lootBonus);
        RandomizableContainer.setBlockEntityLootTable(level, random, chest,
                ResourceKey.create(Registries.LOOT_TABLE, Thaumcraft.id("chests/void_encounter_" + depth)));
    }

    @Override
    public int keyRoomGuardians(int base, int cx, int cz) {
        return base + ExpeditionDim.difficultyAt(cz).extraGuardians + (ExpeditionDim.encounterAt(cz) - 1) / 2;
    }

    @Override
    public boolean keyRoomChampion(int index, int cx, int cz) {
        return index < ExpeditionDim.difficultyAt(cz).champions;
    }

    @Override
    public void onPortal(ServerPlayer player, ServerLevel level, BlockPos portal) {
        Expeditions.onPortal(player);
    }
}
