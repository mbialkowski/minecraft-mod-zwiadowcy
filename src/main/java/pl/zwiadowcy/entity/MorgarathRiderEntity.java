package pl.zwiadowcy.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import pl.zwiadowcy.registry.ModEntities;
import pl.zwiadowcy.registry.ModItems;

import java.util.EnumSet;

/**
 * Jeździec Morgaratha (tom II, mini-boss aktu II): człowiek w czarnej zbroi na czarnym rumaku. Faza konna: szarże
 * z kopią (zapowiedź: koń staje dęba), trzeba zrobić unik i strzelać w jeźdźca. Przy połowie zdrowia spada z konia
 * i walczy pieszo mieczem - zapowiadanymi ciosami, przed którymi trzeba uskoczyć.
 */
public class MorgarathRiderEntity extends StoryFoe {
    private final ServerBossEvent bossEvent = new ServerBossEvent(Component.literal("Jeździec Morgaratha").withStyle(ChatFormatting.DARK_RED),
            BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.NOTCHED_10);
    private boolean mounted;
    private boolean dismounted;
    private int swingIndex;

    public MorgarathRiderEntity(EntityType<? extends MorgarathRiderEntity> type, Level level) {
        super(type, level);
        setPersistenceRequired();
        xpReward = 60;
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.KOPIA.get()));
        setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
        setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.NETHERITE_CHESTPLATE));
        for (EquipmentSlot s : EquipmentSlot.values()) setDropChance(s, 0f);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 120)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 7)
                .add(Attributes.ARMOR, 6)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6)
                .add(Attributes.FOLLOW_RANGE, 48);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new FootFightGoal());
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide || !(level() instanceof ServerLevel sl)) return;
        bossEvent.setProgress(getHealth() / getMaxHealth());
        BossBarRange.update(this, bossEvent, 40);
        if (!mounted && !dismounted) {
            // pierwsze pojawienie się: dosiada czarnego rumaka
            BlackSteedEntity steed = ModEntities.CZARNY_RUMAK.get().create(sl);
            if (steed != null) {
                steed.moveTo(getX(), getY(), getZ(), getYRot(), 0f);
                sl.addFreshEntity(steed);
                startRiding(steed, true);
            }
            mounted = true;
        }
        // kopia w przejeździe szarży
        LivingEntity t = getTarget();
        if (getVehicle() instanceof BlackSteedEntity steed && steed.charging() && t != null && !chargeHit && distanceTo(t) < 3.0) {
            chargeHit = true;
            swing(net.minecraft.world.InteractionHand.MAIN_HAND);
            t.hurt(damageSources().mobAttack(this), 10f);
            playSound(SoundEvents.PLAYER_ATTACK_KNOCKBACK, 1.4f, 0.6f);
        }
        if (!dismounted && getHealth() <= getMaxHealth() * 0.5f) dismount();
    }

    /** Połowa zdrowia: jeździec spada, koń ucieka, dalej walka pieszo mieczem. */
    private void dismount() {
        dismounted = true;
        if (getVehicle() instanceof BlackSteedEntity steed) steed.flee();
        stopRiding();
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
        playSound(SoundEvents.HORSE_DEATH, 1.2f, 0.6f);
        for (ServerPlayer p : level().getEntitiesOfClass(ServerPlayer.class, getBoundingBox().inflate(40))) {
            p.displayClientMessage(Component.literal("Jeździec spada z konia i dobywa miecza!").withStyle(ChatFormatting.GOLD), true);
        }
    }

    /** Walka pieszo: zapowiadane ciosy na przemian - z góry i pchnięcie. */
    private class FootFightGoal extends Goal {
        FootFightGoal() { setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }

        @Override
        public boolean canUse() {
            return !isPassenger() && getTarget() != null && getTarget().isAlive();
        }

        @Override
        public void tick() {
            LivingEntity t = getTarget();
            if (t == null) return;
            getLookControl().setLookAt(t, 30f, 30f);
            if (winding()) return;
            double d = distanceTo(t);
            if (d > 2.8) getNavigation().moveTo(t, 1.1);
            else getNavigation().stop();
            if (d <= 3.4 && !busy()) {
                Swing s = swingIndex++ % 2 == 0
                        ? new Swing("cios z góry", 16, 7f, 3.2, 0.5, null)
                        : new Swing("pchnięcie", 12, 6f, 3.6, 0.8, null);
                startSwing(s, t);
            }
        }
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossEvent.removePlayer(player);
    }

    @Override
    public void remove(RemovalReason reason) {
        bossEvent.removeAllPlayers();
        super.remove(reason);
    }

    @Override
    public boolean removeWhenFarAway(double distance) { return false; }

    @Override protected SoundEvent getAmbientSound() { return SoundEvents.PILLAGER_AMBIENT; }
    @Override protected SoundEvent getHurtSound(DamageSource src) { return SoundEvents.PILLAGER_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.PILLAGER_DEATH; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Mounted", mounted);
        tag.putBoolean("Dismounted", dismounted);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        mounted = tag.getBoolean("Mounted");
        dismounted = tag.getBoolean("Dismounted");
    }
}
