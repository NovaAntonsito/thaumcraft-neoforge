package thaumcraft.compat.jade;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/** The vis and flux of the chunk, with goggles of revealing. Shown at the bottom. */
public enum AuraComponent implements IBlockComponentProvider {
    INSTANCE;

    @Override
    public ResourceLocation getUid() {
        return AuraData.UID;
    }

    @Override
    public int getDefaultPriority() {
        return 5000;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (!data.contains(AuraData.VIS)) return;
        tooltip.add(Component.translatable("tc.jade.vis", String.format("%.1f", data.getFloat(AuraData.VIS)), data.getInt(AuraData.BASE))
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        float flux = data.getFloat(AuraData.FLUX);
        if (flux > 0.0f) {
            tooltip.add(Component.translatable("tc.jade.flux", String.format("%.1f", flux)).withStyle(ChatFormatting.DARK_PURPLE));
        }
    }
}
