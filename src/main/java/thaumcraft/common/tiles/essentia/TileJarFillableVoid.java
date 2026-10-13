package thaumcraft.common.tiles.essentia;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aura.AuraHelper;
import thaumcraft.common.registry.TCBlockEntities;

/** Port of TileJarFillableVoid: accepts essentia past its capacity and destroys the excess, sometimes as flux. */
public class TileJarFillableVoid extends TileJarFillable {
    public TileJarFillableVoid(BlockPos pos, BlockState state) {
        super(TCBlockEntities.JAR_VOID.get(), pos, state);
    }

    @Override
    public int addToContainer(Aspect tt, int am) {
        boolean up = amount < CAPACITY;
        if (am == 0) {
            return am;
        }
        if (tt == aspect || amount == 0) {
            aspect = tt;
            amount += am;
            am = 0;
            if (amount > CAPACITY) {
                if (level.random.nextInt(CAPACITY) == 0) {
                    AuraHelper.polluteAura(level, worldPosition, 1.0f, true);
                }
                amount = CAPACITY;
            }
        }
        if (up) {
            syncTile(false);
            setChanged();
        }
        return am;
    }

    @Override
    public int getMinimumSuction() {
        return aspectFilter != null ? 48 : 32;
    }

    @Override
    public int getSuctionAmount(Direction loc) {
        if (aspectFilter != null && amount < CAPACITY) {
            return 48;
        }
        return 32;
    }

    @Override
    protected void update() {
        if (!level.isClientSide && ++count % 5 == 0) {
            fillJar();
        }
    }
}
