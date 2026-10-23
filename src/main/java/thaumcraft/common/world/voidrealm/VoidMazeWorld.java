package thaumcraft.common.world.voidrealm;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import thaumcraft.Thaumcraft;
import thaumcraft.api.maze.IMazeWorld;
import thaumcraft.common.entities.EntityPermanentItem;
import thaumcraft.common.items.consumables.ItemVoidJournalPage;

/**
 * Dream branch: the void's labyrinths. The key room also holds the encounter's journal page and a chest, and more
 * guardians watch it the further the encounters go; the portal leads back to the rift.
 */
public final class VoidMazeWorld implements IMazeWorld {
    public static final VoidMazeWorld INSTANCE = new VoidMazeWorld();

    private VoidMazeWorld() {
    }

    @Override
    public boolean ownsChunk(int cx, int cz) {
        return VoidDim.isVoidChunk(cx);
    }

    @Override
    public void decorateKeyRoom(WorldGenLevel level, RandomSource random, BlockPos center, int cx, int cz) {
        int encounter = VoidDim.encounterAt(cz);
        EntityPermanentItem page = new EntityPermanentItem(level.getLevel(), center.getX() + 0.5, center.getY() + 1.5,
                center.getZ() + 1.5, ItemVoidJournalPage.page(encounter));
        page.setDeltaMovement(0.0, 0.0, 0.0);
        level.addFreshEntity(page);
        BlockPos chest = center.south(2);
        level.setBlock(chest, Blocks.CHEST.defaultBlockState(), 2);
        RandomizableContainer.setBlockEntityLootTable(level, random, chest,
                ResourceKey.create(Registries.LOOT_TABLE, Thaumcraft.id("chests/void_encounter_" + encounter)));
    }

    @Override
    public int keyRoomGuardians(int base, int cx, int cz) {
        return base + VoidDim.encounterAt(cz) - 1;
    }

    @Override
    public boolean keyRoomChampion(int index, int cx, int cz) {
        return index < VoidDim.encounterAt(cz) - 2;
    }

    @Override
    public void onPortal(ServerPlayer player, ServerLevel level, BlockPos portal) {
        VoidEncounters.leave(player);
    }
}
