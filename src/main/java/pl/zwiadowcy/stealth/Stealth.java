package pl.zwiadowcy.stealth;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import pl.zwiadowcy.entity.KalkarEntity;
import pl.zwiadowcy.item.RangerCloakItem;
import pl.zwiadowcy.network.ModNetwork;
import pl.zwiadowcy.network.StealthPacket;
import pl.zwiadowcy.quest.QuestEngine;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Kamuflaż zwiadowcy. Widoczność (0..1) zależy od płaszcza, kucania, bezruchu, roślinności i ciemności.
 * Używana przez moby (zasięg wykrycia) oraz przez obserwatorów w misjach skradania.
 */
public final class Stealth {
    public static final int BEZRUCH_TICKS = 200;
    public static final int BEZRUCH_COOLDOWN = 1200;

    private static final class Track {
        Vec3 last;
        int stillTicks;
        long bezruchUntil;
        Vec3 bezruchPos;
        int sentVis = -1;
        int sentBezruch = -1;
        boolean sentCloak;
    }

    private static final Map<UUID, Track> TRACKS = new HashMap<>();

    private static Track track(Player p) {
        return TRACKS.computeIfAbsent(p.getUUID(), u -> new Track());
    }

    public static boolean wearsCloak(Player p) {
        return p.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof RangerCloakItem;
    }

    public static boolean bezruchActive(Player p) {
        Track t = TRACKS.get(p.getUUID());
        return t != null && t.bezruchUntil > p.level().getGameTime();
    }

    public static double visibility(Player p) {
        Track t = track(p);
        if (t.bezruchUntil > p.level().getGameTime()) return 0.0;
        boolean cloak = wearsCloak(p);
        double v = cloak ? 0.55 : 1.0;
        if (p.isCrouching()) v *= cloak ? 0.6 : 0.7;
        if (t.stillTicks >= 20) v *= cloak ? 0.55 : 0.85;
        if (inFoliage(p)) v *= cloak ? 0.6 : 0.85;
        int light = p.level().getMaxLocalRawBrightness(p.blockPosition());
        if (light < 8) v *= cloak ? 0.65 : 0.7;
        if (p.isSprinting()) v = Math.min(1.0, v * 1.6);
        return Math.max(0.04, v);
    }

    public static double detectionFactor(ServerPlayer p) {
        double v = visibility(p);
        return v <= 0.0 ? 0.0 : v;
    }

    /** Czy obserwator widzi gracza: zasięg, stożek widzenia (±65°), linia wzroku. */
    public static boolean canSee(LivingEntity observer, Player p, double range) {
        if (range <= 0) return false;
        Vec3 eye = observer.getEyePosition();
        Vec3 diff = p.getEyePosition().subtract(eye);
        double dist = diff.length();
        if (dist > range) return false;
        Vec3 look = Vec3.directionFromRotation(observer.getXRot(), observer.getYHeadRot());
        double cos = look.dot(diff.normalize());
        if (dist < 1.8 && cos > -0.2) return true;
        if (cos < Math.cos(Math.toRadians(65))) return false;
        return observer.hasLineOfSight(p);
    }

    public static boolean inFoliage(Player p) {
        Level level = p.level();
        BlockPos feet = p.blockPosition();
        if (isFoliage(level.getBlockState(feet)) || isFoliage(level.getBlockState(feet.above()))) return true;
        for (BlockPos pos : BlockPos.betweenClosed(feet.offset(-1, 0, -1), feet.offset(1, 2, 1))) {
            if (level.getBlockState(pos).is(BlockTags.LEAVES)) return true;
        }
        return false;
    }

    private static boolean isFoliage(BlockState s) {
        return s.is(BlockTags.LEAVES) || s.is(BlockTags.REPLACEABLE_BY_TREES) || s.is(BlockTags.FLOWERS)
                || s.is(BlockTags.SAPLINGS) || s.is(BlockTags.CROPS) || s.is(Blocks.SWEET_BERRY_BUSH) || s.is(Blocks.HAY_BLOCK);
    }

    // ------------------------------------------------------------------ Bezruch

    public static void tryActivateBezruch(ServerPlayer p) {
        if (!wearsCloak(p)) {
            p.displayClientMessage(Component.literal("Bezruch wymaga założonego płaszcza zwiadowcy.").withStyle(ChatFormatting.GRAY), true);
            return;
        }
        Item cloak = p.getItemBySlot(EquipmentSlot.CHEST).getItem();
        if (p.getCooldowns().isOnCooldown(cloak)) {
            p.displayClientMessage(Component.literal("Musisz złapać oddech, zanim znów znikniesz.").withStyle(ChatFormatting.GRAY), true);
            return;
        }
        Track t = track(p);
        t.bezruchUntil = p.level().getGameTime() + BEZRUCH_TICKS;
        t.bezruchPos = p.position();
        p.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, BEZRUCH_TICKS, 0, false, false, true));
        p.getCooldowns().addCooldown(cloak, BEZRUCH_COOLDOWN);
        loseTargets(p, 0.0);
        p.level().playSound(null, p.blockPosition(), SoundEvents.ARMOR_EQUIP_LEATHER, SoundSource.PLAYERS, 0.8f, 0.7f);
        if (p.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.SMOKE, p.getX(), p.getY() + 1, p.getZ(), 12, 0.3, 0.5, 0.3, 0.01);
        }
        p.displayClientMessage(Component.literal("Bezruch - nie ruszaj się, a nikt cię nie zobaczy.").withStyle(ChatFormatting.DARK_GREEN), true);
        QuestEngine.fireEvent(p, "bezruch", 1);
    }

    private static void endBezruch(ServerPlayer p, Track t, boolean moved) {
        t.bezruchUntil = 0;
        MobEffectInstance inv = p.getEffect(MobEffects.INVISIBILITY);
        if (inv != null && inv.getDuration() <= BEZRUCH_TICKS) p.removeEffect(MobEffects.INVISIBILITY);
        if (moved) p.displayClientMessage(Component.literal("Poruszyłeś się - Bezruch przerwany.").withStyle(ChatFormatting.GRAY), true);
    }

    /** Wywoływane co tick po stronie serwera. */
    public static void tick(ServerPlayer p) {
        Track t = track(p);
        Vec3 pos = p.position();
        if (t.last != null && pos.distanceToSqr(t.last) < 0.0004) t.stillTicks++;
        else t.stillTicks = 0;
        t.last = pos;

        long now = p.level().getGameTime();
        if (t.bezruchUntil > 0) {
            if (now >= t.bezruchUntil) {
                endBezruch(p, t, false);
            } else {
                double dx = pos.x - t.bezruchPos.x, dz = pos.z - t.bezruchPos.z;
                if (dx * dx + dz * dz > 0.09 || p.swinging || p.isUsingItem()) endBezruch(p, t, true);
            }
        }

        boolean cloak = wearsCloak(p);
        if (p.tickCount % 10 == 0 && (cloak || t.bezruchUntil > now)) {
            loseTargets(p, visibility(p));
        }
        if (p.tickCount % 5 == 0) {
            int vis = (int) Math.round(visibility(p) * 100);
            int bz = t.bezruchUntil > now ? (int) (t.bezruchUntil - now) : 0;
            if (vis != t.sentVis || bz / 20 != t.sentBezruch / 20 || cloak != t.sentCloak) {
                t.sentVis = vis;
                t.sentBezruch = bz;
                t.sentCloak = cloak;
                ModNetwork.sendTo(p, new StealthPacket(vis, bz, cloak));
            }
        }
    }

    /** Moby, które ścigają gracza z dalszej odległości niż (zasięg × widoczność), tracą go z oczu. */
    private static void loseTargets(ServerPlayer p, double vis) {
        for (Mob mob : p.level().getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(48), m -> m.getTarget() == p)) {
            if (mob instanceof KalkarEntity && vis > 0) continue;
            double follow = mob.getAttribute(Attributes.FOLLOW_RANGE) != null ? mob.getAttributeValue(Attributes.FOLLOW_RANGE) : 16;
            double allowed = Math.max(2.5, follow * vis * 0.6);
            if (vis <= 0 || mob.distanceTo(p) > allowed) {
                mob.setTarget(null);
                mob.getNavigation().stop();
            }
        }
    }

    public static void forget(Player p) {
        TRACKS.remove(p.getUUID());
    }

    private Stealth() {}
}
