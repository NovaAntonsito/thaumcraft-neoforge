package voidexpeditions.perk;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * A boon that lasts one expedition. Every pick adds its attribute boosts and caster bonuses again; a unique perk
 * (one that does something rather than adding numbers, see {@link PerkEffects}) is only offered while not held.
 * Cursed perks pay for a big bonus with a drawback.
 *
 * @param visDiscount percentage points off the vis cost of casting (negative costs more)
 * @param power       added to the power multiplier of foci
 * @param cooldown    taken off the cooldown multiplier of casting (negative is slower)
 */
public record Perk(String id, Category category, List<Boost> boosts, int visDiscount, float power, float cooldown,
                   boolean unique) {

    public enum Category {
        PLAYER(ChatFormatting.GREEN),
        CASTER(ChatFormatting.AQUA),
        AURA(ChatFormatting.LIGHT_PURPLE),
        WARP(ChatFormatting.DARK_PURPLE),
        UTILITY(ChatFormatting.YELLOW),
        CURSED(ChatFormatting.RED);

        public final ChatFormatting color;

        Category(ChatFormatting color) {
            this.color = color;
        }
    }

    /** One attribute modifier per pick. */
    public record Boost(Holder<Attribute> attribute, AttributeModifier.Operation operation, double amount) {
    }

    public Component displayName() {
        return Component.translatable("voidexpeditions.perk." + id).withStyle(category.color);
    }

    public Component description() {
        return Component.translatable("voidexpeditions.perk." + id + ".desc");
    }

    public static Builder builder(String id, Category category) {
        return new Builder(id, category);
    }

    public static final class Builder {
        private final String id;
        private final Category category;
        private final List<Boost> boosts = new ArrayList<>();
        private int visDiscount;
        private float power;
        private float cooldown;
        private boolean unique;

        private Builder(String id, Category category) {
            this.id = id;
            this.category = category;
        }

        public Builder boost(Holder<Attribute> attribute, AttributeModifier.Operation operation, double amount) {
            boosts.add(new Boost(attribute, operation, amount));
            return this;
        }

        public Builder add(Holder<Attribute> attribute, double amount) {
            return boost(attribute, AttributeModifier.Operation.ADD_VALUE, amount);
        }

        public Builder visDiscount(int points) {
            this.visDiscount = points;
            return this;
        }

        public Builder power(float power) {
            this.power = power;
            return this;
        }

        public Builder cooldown(float cooldown) {
            this.cooldown = cooldown;
            return this;
        }

        public Builder unique() {
            this.unique = true;
            return this;
        }

        public Perk build() {
            return new Perk(id, category, List.copyOf(boosts), visDiscount, power, cooldown, unique);
        }
    }
}
