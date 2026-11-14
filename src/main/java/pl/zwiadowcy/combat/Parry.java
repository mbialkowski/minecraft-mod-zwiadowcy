package pl.zwiadowcy.combat;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pl.zwiadowcy.Zwiadowcy;
import pl.zwiadowcy.item.RangerSwordItem;
import pl.zwiadowcy.item.SaxeKnifeItem;
import pl.zwiadowcy.quest.QuestEngine;
import pl.zwiadowcy.skill.Skills;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Garda i parowanie (tom II): przytrzymany PPM z mieczem zwiadowcy albo saksą to garda. Cios, który trafi w ciągu
 * okna parowania od podniesienia gardy, jest odbity (zero obrażeń, przeciwnik zatacza się na sekundę, a kontra
 * w tym czasie zadaje podwójne obrażenia). Spóźniona garda przyjmuje 60% obrażeń; garda od tyłu nic nie daje.
 */
@Mod.EventBusSubscriber(modid = Zwiadowcy.MODID)
public final class Parry {
    private record Counter(UUID target, long until) {}

    private static final Map<UUID, Counter> COUNTERS = new HashMap<>();

    /** Okno parowania w tikach dla broni w dłoni. */
    public static int window(ServerPlayer p, ItemStack weapon) {
        int w = weapon.getItem() instanceof SaxeKnifeItem ? 4 : 6;
        if (Skills.has(p, Skills.LEKCJA_GILANA)) w += 2;
        if (Skills.has(p, Skills.SZKOLNY_FECHTUNEK)) w += 1;
        return w;
    }

    public static boolean isGuardWeapon(ItemStack stack) {
        return stack.getItem() instanceof RangerSwordItem || stack.getItem() instanceof SaxeKnifeItem;
    }

    private static boolean guarding(ServerPlayer p) {
        return p.isUsingItem() && isGuardWeapon(p.getUseItem());
    }

    private static boolean fromFront(ServerPlayer p, LivingEntity attacker) {
        Vec3 look = Vec3.directionFromRotation(0, p.getYRot());
        Vec3 to = attacker.position().subtract(p.position()).multiply(1, 0, 1);
        return to.lengthSqr() < 1e-4 || look.dot(to.normalize()) > 0.0;
    }

    @SubscribeEvent
    public static void onAttack(LivingAttackEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer p) || !(event.getSource().getDirectEntity() instanceof LivingEntity attacker)) return;
        if (!guarding(p) || !fromFront(p, attacker)) return;
        int held = p.getTicksUsingItem();
        if (held > window(p, p.getUseItem())) return;
        event.setCanceled(true);
        // odbicie: przeciwnik zatacza się, kontra ×2 przez sekundę
        attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 3, false, false));
        attacker.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20, 2, false, false));
        Vec3 push = attacker.position().subtract(p.position()).multiply(1, 0, 1).normalize().scale(0.6);
        attacker.push(push.x, 0.15, push.z);
        p.level().playSound(null, p.blockPosition(), SoundEvents.ANVIL_PLACE, SoundSource.PLAYERS, 0.5f, 1.8f);
        if (p.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.CRIT, attacker.getX(), attacker.getEyeY(), attacker.getZ(), 10, 0.3, 0.3, 0.3, 0.2);
        }
        COUNTERS.put(p.getUUID(), new Counter(attacker.getUUID(), p.level().getGameTime() + 20));
        p.displayClientMessage(Component.literal("Sparowane! Kontruj!").withStyle(ChatFormatting.GOLD), true);
        p.stopUsingItem();
        QuestEngine.fireEvent(p, "parowanie", 1);
    }

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        // spóźniona garda: 60% obrażeń
        if (event.getEntity() instanceof ServerPlayer p && guarding(p)
                && event.getSource().getDirectEntity() instanceof LivingEntity attacker && fromFront(p, attacker)) {
            event.setAmount(event.getAmount() * 0.6f);
        }
        // kontra po udanym parowaniu: ×2
        if (event.getSource().getEntity() instanceof ServerPlayer p) {
            Counter c = COUNTERS.get(p.getUUID());
            if (c != null && c.target.equals(event.getEntity().getUUID()) && p.level().getGameTime() <= c.until
                    && event.getSource().getDirectEntity() == p) {
                COUNTERS.remove(p.getUUID());
                event.setAmount(event.getAmount() * 2f);
                p.level().playSound(null, p.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1f, 0.9f);
                QuestEngine.fireEvent(p, "kontra", 1);
            }
        }
    }

    private Parry() {}
}
