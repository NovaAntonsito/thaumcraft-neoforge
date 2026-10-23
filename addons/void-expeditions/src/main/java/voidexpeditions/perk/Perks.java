package voidexpeditions.perk;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import thaumcraft.api.casters.ICasterModifier;
import voidexpeditions.VoidExpeditions;
import voidexpeditions.registry.VEAttachments;
import voidexpeditions.run.RunState;

/** Every perk, and how a run's picks act on the player. Also the caster modifier behind the caster bonuses. */
public final class Perks implements ICasterModifier {
    public static final Perks CASTER_MODIFIER = new Perks();

    private static final Map<String, Perk> ALL = new LinkedHashMap<>();

    // Player
    public static final Perk VITALITY = add(Perk.builder("vitality", Perk.Category.PLAYER).add(Attributes.MAX_HEALTH, 4.0));
    public static final Perk BULWARK = add(Perk.builder("bulwark", Perk.Category.PLAYER).add(Attributes.ARMOR, 3.0));
    public static final Perk SWIFTNESS = add(Perk.builder("swiftness", Perk.Category.PLAYER)
            .boost(Attributes.MOVEMENT_SPEED, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, 0.12));
    public static final Perk MIGHT = add(Perk.builder("might", Perk.Category.PLAYER).add(Attributes.ATTACK_DAMAGE, 2.0));
    public static final Perk STEADFAST = add(Perk.builder("steadfast", Perk.Category.PLAYER).add(Attributes.KNOCKBACK_RESISTANCE, 0.25));
    public static final Perk FORTUNE = add(Perk.builder("fortune", Perk.Category.PLAYER).add(Attributes.LUCK, 1.0));
    // Caster
    public static final Perk VIS_THRIFT = add(Perk.builder("vis_thrift", Perk.Category.CASTER).visDiscount(15));
    public static final Perk POTENCY = add(Perk.builder("potency", Perk.Category.CASTER).power(0.25f));
    public static final Perk QUICKCAST = add(Perk.builder("quickcast", Perk.Category.CASTER).cooldown(0.2f));
    // Aura
    public static final Perk VIS_SIPHON = add(Perk.builder("vis_siphon", Perk.Category.AURA).unique());
    public static final Perk FLUX_WARD = add(Perk.builder("flux_ward", Perk.Category.AURA).unique());
    // Warp
    public static final Perk WARP_WARD = add(Perk.builder("warp_ward", Perk.Category.WARP).unique());
    public static final Perk CLEAR_MIND = add(Perk.builder("clear_mind", Perk.Category.WARP).unique());
    // Utility
    public static final Perk NIGHT_EYE = add(Perk.builder("night_eye", Perk.Category.UTILITY).unique());
    public static final Perk SIGIL_SENSE = add(Perk.builder("sigil_sense", Perk.Category.UTILITY).unique());
    public static final Perk VOID_COMPASS = add(Perk.builder("void_compass", Perk.Category.UTILITY).unique());
    // Cursed
    public static final Perk BLOOD_PACT = add(Perk.builder("blood_pact", Perk.Category.CURSED).power(0.5f)
            .add(Attributes.MAX_HEALTH, -4.0));
    public static final Perk GLASS_CANNON = add(Perk.builder("glass_cannon", Perk.Category.CURSED)
            .add(Attributes.ATTACK_DAMAGE, 4.0).add(Attributes.ARMOR, -4.0));
    public static final Perk FRENZY = add(Perk.builder("frenzy", Perk.Category.CURSED).cooldown(0.35f).visDiscount(-20));

    private Perks() {
    }

    private static Perk add(Perk.Builder builder) {
        Perk p = builder.build();
        ALL.put(p.id(), p);
        return p;
    }

    @Nullable
    public static Perk get(String id) {
        return ALL.get(id);
    }

    public static Collection<Perk> all() {
        return Collections.unmodifiableCollection(ALL.values());
    }

    /**
     * Three different perks at random, to choose from at a portal: no unique perk already held, and at most one
     * cursed perk.
     */
    public static List<String> offer(RandomSource random, RunState run) {
        List<Perk> pool = new ArrayList<>();
        for (Perk p : ALL.values()) {
            if (!p.unique() || run.count(p.id()) == 0) pool.add(p);
        }
        Collections.shuffle(pool, new Random(random.nextLong()));
        List<String> out = new ArrayList<>();
        boolean cursed = false;
        for (Perk p : pool) {
            if (out.size() == 3) break;
            if (p.category() == Perk.Category.CURSED) {
                if (cursed) continue;
                cursed = true;
            }
            out.add(p.id());
        }
        return out;
    }

    private static ResourceLocation modifierId(Perk perk, int boost) {
        return VoidExpeditions.id("perk/" + perk.id() + "/" + boost);
    }

    /** Sets the attribute modifiers of the perks to the run's picks: none once the run is over. */
    public static void refresh(ServerPlayer player) {
        RunState run = player.getData(VEAttachments.RUN);
        for (Perk perk : ALL.values()) {
            int n = run.active ? run.count(perk.id()) : 0;
            for (int i = 0; i < perk.boosts().size(); i++) {
                Perk.Boost b = perk.boosts().get(i);
                AttributeInstance inst = player.getAttribute(b.attribute());
                if (inst == null) continue;
                inst.removeModifier(modifierId(perk, i));
                if (n > 0) inst.addPermanentModifier(new AttributeModifier(modifierId(perk, i), b.amount() * n, b.operation()));
            }
        }
        if (player.getHealth() > player.getMaxHealth()) player.setHealth(player.getMaxHealth());
    }

    /** Whether the player is on a run and holds this perk. */
    public static boolean has(Player player, Perk perk) {
        RunState run = player.getData(VEAttachments.RUN);
        return run.active && run.count(perk.id()) > 0;
    }

    @Nullable
    private static RunState activeRun(LivingEntity caster) {
        if (!(caster instanceof Player p)) return null;
        RunState run = p.getData(VEAttachments.RUN);
        return run.active ? run : null;
    }

    @Override
    public int getVisDiscount(Player player) {
        RunState run = activeRun(player);
        if (run == null) return 0;
        int total = 0;
        for (String id : run.perks) {
            Perk p = ALL.get(id);
            if (p != null) total += p.visDiscount();
        }
        return total;
    }

    @Override
    public float getPowerMultiplier(LivingEntity caster) {
        RunState run = activeRun(caster);
        if (run == null) return 1.0f;
        float total = 1.0f;
        for (String id : run.perks) {
            Perk p = ALL.get(id);
            if (p != null) total += p.power();
        }
        return total;
    }

    @Override
    public float getCooldownMultiplier(LivingEntity caster) {
        RunState run = activeRun(caster);
        if (run == null) return 1.0f;
        float total = 1.0f;
        for (String id : run.perks) {
            Perk p = ALL.get(id);
            if (p != null) total -= p.cooldown();
        }
        return Math.max(0.2f, total);
    }
}
