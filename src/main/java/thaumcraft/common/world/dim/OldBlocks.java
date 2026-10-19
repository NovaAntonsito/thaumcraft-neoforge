package thaumcraft.common.world.dim;

/**
 * The 1.7 blocks the maze generators place, by their old names, so the generators read as the original. With a meta
 * they become 1.21 blocks in {@link GenWorld#state}.
 */
enum OldBlocks {
    air,
    bedrock,
    web,
    mob_spawner,
    blockCosmeticSolid,
    blockEldritch,
    blockEldritchNothing,
    blockStairsEldritch,
    blockSlabStone,
    blockAiry,
    blockLootUrn,
    blockLootCrate,
    blockCrystal,
    blockTaintFibres,
    blockEldritchPortal,
    /** Anything else. */
    other
}
