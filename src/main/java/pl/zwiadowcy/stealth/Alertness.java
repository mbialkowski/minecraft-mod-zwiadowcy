package pl.zwiadowcy.stealth;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import pl.zwiadowcy.item.SaxeKnifeItem;
import pl.zwiadowcy.quest.QuestActions;
import pl.zwiadowcy.quest.QuestEngine;

/** Skradanie 2.0 - pomocnicze funkcje dla gracza, misji i przedmiotów (alarm, hałas, ogłuszenie). */
public final class Alertness {

    /** Czy w pobliżu gracza trwa alarm (blokuje szybką podróż). */
    public static boolean alarmNear(ServerPlayer p) {
        return !p.level().getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(48),
                m -> Watchful.active(m) && ((Watchful) m).alertBrain().alarmed()).isEmpty();
    }

    /** Alarm podniesiony: misja dostaje zdarzenie "alarm" (posiłki, utrata premii za ciche przejście). */
    static void onAlarm(Mob mob, ServerPlayer p) {
        ServerPlayer owner = p;
        var data = mob.getPersistentData();
        if (data.hasUUID(QuestActions.TAG_OWNER) && mob.getServer() != null) {
            ServerPlayer o = mob.getServer().getPlayerList().getPlayer(data.getUUID(QuestActions.TAG_OWNER));
            if (o != null) owner = o;
        }
        owner.displayClientMessage(Component.literal("Alarm! Wrogowie cię zauważyli.").withStyle(ChatFormatting.RED), true);
        QuestEngine.fireEvent(owner, "alarm", 1);
    }

    /** Hałas w punkcie (kamyk, strzała gwiżdżąca): czujni wrogowie w promieniu idą sprawdzić. */
    public static void noise(ServerLevel level, BlockPos pos, double radius) {
        for (Mob m : level.getEntitiesOfClass(Mob.class, new net.minecraft.world.phys.AABB(pos).inflate(radius), Watchful::active)) {
            ((Watchful) m).alertBrain().hearNoise(pos);
        }
    }

    /**
     * Ciche ogłuszenie: gracz kuca za plecami wroga, który nie podniósł alarmu, i uderza pięścią, saksą albo mieczem.
     * Zwraca true, gdy ogłuszenie się udało (wtedy zwykły cios jest anulowany).
     */
    public static boolean tryTakedown(ServerPlayer p, Mob target) {
        if (!Watchful.active(target)) return false;
        AlertBrain brain = ((Watchful) target).alertBrain();
        if (brain.knockedOut() || brain.alarmed() || target.getTarget() == p || !p.isCrouching()) return false;
        ItemStack held = p.getMainHandItem();
        boolean weapon = held.isEmpty() || held.getItem() instanceof SaxeKnifeItem
                || held.getItem() instanceof net.minecraft.world.item.SwordItem;
        if (!weapon) return false;
        Vec3 look = Vec3.directionFromRotation(0, target.getYHeadRot());
        Vec3 toPlayer = new Vec3(p.getX() - target.getX(), 0, p.getZ() - target.getZ()).normalize();
        // za plecami: gracz poza przednim półkolem wroga (ponad 110° od kierunku patrzenia)
        if (look.dot(toPlayer) > Math.cos(Math.toRadians(110))) return false;
        brain.knockOut();
        target.level().playSound(null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_KNOCKBACK, SoundSource.PLAYERS, 1f, 0.6f);
        p.displayClientMessage(Component.literal("Ogłuszony - po cichu.").withStyle(ChatFormatting.DARK_GREEN), true);
        // ogłuszony wróg liczy się w misji jak pokonany
        String quest = target.getPersistentData().getString(QuestActions.TAG_QUEST);
        QuestEngine.onKill(p, target, quest.isEmpty() ? null : quest);
        QuestEngine.fireEvent(p, "ogluszenie", 1);
        return true;
    }

    public static boolean isKnockedOut(Player viewer, Mob mob) {
        return mob instanceof Watchful w && w.alertBrain().knockedOut();
    }

    private Alertness() {}
}
