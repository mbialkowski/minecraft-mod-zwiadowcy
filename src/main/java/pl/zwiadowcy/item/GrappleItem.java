package pl.zwiadowcy.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pl.zwiadowcy.Zwiadowcy;
import pl.zwiadowcy.entity.projectile.GrappleHookEntity;
import pl.zwiadowcy.quest.QuestEngine;
import pl.zwiadowcy.skill.Skills;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Linka z kotwiczką (tom II): PPM rzuca hak na krawędź do 20 bloków i podciąga gracza w ok. sekundę.
 * Odnowienie 8 s (5 s z „Górniczą zręcznością”), nie działa w walce (3 s od zadania lub otrzymania ciosu).
 */
@Mod.EventBusSubscriber(modid = Zwiadowcy.MODID)
public class GrappleItem extends TooltipItem {
    public static final double RANGE = 20;
    public static final TagKey<Block> NO_GRAPPLE = TagKey.create(net.minecraft.core.registries.Registries.BLOCK, Zwiadowcy.id("bez_kotwiczki"));
    private static final int COMBAT_TICKS = 60;

    private record Pull(Vec3 target, int ticksLeft) {}

    private static final Map<UUID, Pull> PULLS = new HashMap<>();

    public GrappleItem(Properties props) {
        super(props, 2);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            int since = Math.min(sp.tickCount - sp.getLastHurtByMobTimestamp(), sp.tickCount - sp.getLastHurtMobTimestamp());
            if (since < COMBAT_TICKS && (sp.getLastHurtByMobTimestamp() > 0 || sp.getLastHurtMobTimestamp() > 0)) {
                sp.displayClientMessage(Component.literal("Nie w trakcie walki.").withStyle(ChatFormatting.GRAY), true);
                return InteractionResultHolder.fail(stack);
            }
            GrappleHookEntity hook = new GrappleHookEntity(level, player);
            hook.setItem(new ItemStack(this));
            hook.shootFromRotation(player, player.getXRot(), player.getYRot(), 0f, 2.2f, 0.3f);
            level.addFreshEntity(hook);
            level.playSound(null, player.blockPosition(), SoundEvents.FISHING_BOBBER_THROW, SoundSource.PLAYERS, 1f, 0.7f);
            player.getCooldowns().addCooldown(this, Skills.has(player, Skills.GORNICZA_ZRECZNOSC) ? 100 : 160);
            stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    /** Podciąganie: gracz leci prosto do krawędzi przez ok. 1 s, bez obrażeń od upadku. */
    public static void pull(ServerPlayer p, BlockPos ledge) {
        Vec3 target = Vec3.atBottomCenterOf(ledge).add(0, 0.1, 0);
        int ticks = (int) Math.max(8, Math.min(30, p.position().distanceTo(target) / 0.9));
        PULLS.put(p.getUUID(), new Pull(target, ticks + 10));
        QuestEngine.fireEvent(p, "kotwiczka", 1);
    }

    public static void missed(ServerPlayer p, boolean forbidden) {
        p.displayClientMessage(Component.literal(forbidden ? "Hak nie trzyma się tej ściany." : "Hak nie znalazł zaczepu.")
                .withStyle(ChatFormatting.GRAY), true);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer p)) return;
        Pull pull = PULLS.get(p.getUUID());
        if (pull == null) return;
        Vec3 to = pull.target.subtract(p.position());
        if (to.length() < 0.8 || pull.ticksLeft <= 0) {
            PULLS.remove(p.getUUID());
            p.setDeltaMovement(Vec3.ZERO);
            p.hurtMarked = true;
            p.fallDistance = 0;
            return;
        }
        Vec3 vel = to.normalize().scale(Math.min(1.0, to.length()));
        p.setDeltaMovement(vel.x, vel.y + 0.08, vel.z);
        p.hurtMarked = true;
        p.fallDistance = 0;
        PULLS.put(p.getUUID(), new Pull(pull.target, pull.ticksLeft - 1));
        if (p.level() instanceof ServerLevel sl && p.tickCount % 2 == 0) {
            sl.sendParticles(ParticleTypes.CRIT, p.getX(), p.getY() + 1, p.getZ(), 1, 0.1, 0.1, 0.1, 0);
        }
    }
}
