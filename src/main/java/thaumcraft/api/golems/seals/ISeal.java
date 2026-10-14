package thaumcraft.api.golems.seals;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import thaumcraft.api.golems.EnumGolemTrait;
import thaumcraft.api.golems.IGolemAPI;
import thaumcraft.api.golems.tasks.Task;

public interface ISeal {
    /**
     * @return A unique string identifier for this seal. A good idea would be to append your modid before the identifier.
     * For example: "thaumcraft:fetch"
     * This will also be used to create the item model for the seal placer so you will have to define a json using
     * the key with "seal_" added to the front of the key.
     * For example: models/item/seal_fetch.json
     */
    String getKey();

    boolean canPlaceAt(Level world, BlockPos pos, Direction side);

    void tickSeal(Level world, ISealEntity seal);

    void onTaskStarted(Level world, IGolemAPI golem, Task task);

    boolean onTaskCompletion(Level world, IGolemAPI golem, Task task);

    void onTaskSuspension(Level world, Task task);

    boolean canGolemPerformTask(IGolemAPI golem, Task task);

    void readCustomNBT(CompoundTag nbt);

    void writeCustomNBT(CompoundTag nbt);

    /**
     * @return icon used to render the seal in world. Usually the same as your seal placer item icon.
     * If it is not the same you will have to manually stitch it into the texture atlas.
     */
    ResourceLocation getSealIcon();

    void onRemoval(Level world, BlockPos pos, Direction side);

    Object returnContainer(Level world, Player player, BlockPos pos, Direction side, ISealEntity seal);

    @OnlyIn(Dist.CLIENT)
    Object returnGui(Level world, Player player, BlockPos pos, Direction side, ISealEntity seal);

    EnumGolemTrait[] getRequiredTags();

    EnumGolemTrait[] getForbiddenTags();
}
