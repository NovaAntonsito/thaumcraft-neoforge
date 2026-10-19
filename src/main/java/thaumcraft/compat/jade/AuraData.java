package thaumcraft.compat.jade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;
import thaumcraft.Thaumcraft;
import thaumcraft.common.lib.utils.EntityUtils;
import thaumcraft.common.world.aura.AuraHandler;

/** Sends the vis, flux and base aura of the chunk, only to a player wearing goggles of revealing. */
public enum AuraData implements IServerDataProvider<BlockAccessor> {
    INSTANCE;

    public static final ResourceLocation UID = Thaumcraft.id("aura");
    static final String VIS = "tc_vis";
    static final String FLUX = "tc_flux";
    static final String BASE = "tc_base";

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (!EntityUtils.hasGoggles(accessor.getPlayer())) return;
        data.putFloat(VIS, AuraHandler.getVis(accessor.getLevel(), accessor.getPosition()));
        data.putFloat(FLUX, AuraHandler.getFlux(accessor.getLevel(), accessor.getPosition()));
        data.putInt(BASE, AuraHandler.getAuraBase(accessor.getLevel(), accessor.getPosition()));
    }

    @Override
    public boolean shouldRequestData(BlockAccessor accessor) {
        return EntityUtils.hasGoggles(accessor.getPlayer());
    }
}
