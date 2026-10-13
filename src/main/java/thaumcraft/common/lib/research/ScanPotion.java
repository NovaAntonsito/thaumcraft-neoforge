package thaumcraft.common.lib.research;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import thaumcraft.api.research.IScanThing;
import thaumcraft.api.research.ScanningManager;

/** A creature under the effect, or a potion item carrying it. Key: "!" and the effect id. */
public class ScanPotion implements IScanThing {
    final Holder<MobEffect> potion;

    public ScanPotion(Holder<MobEffect> potion) {
        this.potion = potion;
    }

    @Override
    public boolean checkThing(Player player, Object obj) {
        if (obj == null) return false;
        if (obj instanceof LivingEntity e) {
            return e.hasEffect(potion);
        }
        ItemStack is = ScanningManager.getItemFromParms(player, obj);
        if (!is.isEmpty()) {
            for (MobEffectInstance effect : is.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).getAllEffects()) {
                if (effect.getEffect().equals(potion)) return true;
            }
        }
        return false;
    }

    @Override
    public String getResearchKey(Player player, Object obj) {
        return "!" + BuiltInRegistries.MOB_EFFECT.getKey(potion.value());
    }
}
