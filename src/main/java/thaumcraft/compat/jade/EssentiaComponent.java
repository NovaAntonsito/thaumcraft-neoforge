package thaumcraft.compat.jade;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

/** The essentia of a container, a jar's label and a tube's suction. */
public enum EssentiaComponent implements IBlockComponentProvider {
    INSTANCE;

    @Override
    public ResourceLocation getUid() {
        return EssentiaData.UID;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (data.contains(EssentiaData.ESSENTIA)) {
            AspectList.CODEC.parse(NbtOps.INSTANCE, data.get(EssentiaData.ESSENTIA)).result()
                    .filter(list -> !list.isEmpty()).ifPresent(list -> tooltip.add(new AspectsElement(list)));
        }
        if (data.contains(EssentiaData.FILTER)) {
            Aspect filter = Aspect.getAspect(data.getString(EssentiaData.FILTER));
            if (filter != null) {
                tooltip.add(Component.translatable("tc.jade.label", filter.getDisplayName()).withStyle(ChatFormatting.GRAY));
            }
        }
        if (data.contains(EssentiaData.SUCTION)) {
            Aspect type = Aspect.getAspect(data.getString(EssentiaData.SUCTION_TYPE));
            Component of = type != null ? type.getDisplayName() : Component.translatable("tc.jade.suction.any");
            tooltip.add(Component.translatable("tc.jade.suction", data.getInt(EssentiaData.SUCTION), of).withStyle(ChatFormatting.GRAY));
        }
    }
}
