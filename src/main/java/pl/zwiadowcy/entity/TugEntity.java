package pl.zwiadowcy.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import pl.zwiadowcy.dialogue.DialogueService;
import pl.zwiadowcy.quest.QuestCapability;
import pl.zwiadowcy.quest.QuestEngine;
import pl.zwiadowcy.quest.QuestProgress;
import pl.zwiadowcy.world.Locations;

import javax.annotation.Nullable;

/**
 * Wyrwij - kuc zwiadowcy. Daje się dosiąść tylko temu, kto grzecznie poprosi (dialog).
 * Nie ginie - wyczerpany wraca do chaty Halta. Parska, gdy wyczuje ukrytego wroga.
 */
public class TugEntity extends AbstractHorse {
    @Nullable private BlockPos homePos;
    private int generation;
    private long tiredUntil;
    private long lastWarning;

    public TugEntity(EntityType<? extends TugEntity> type, Level level) {
        super(type, level);
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractHorse.createBaseHorseAttributes()
                .add(Attributes.MAX_HEALTH, 34.0)
                .add(Attributes.MOVEMENT_SPEED, 0.32)
                .add(Attributes.JUMP_STRENGTH, 0.8);
    }

    public void setHomePos(BlockPos pos) {
        this.homePos = pos;
        restrictTo(pos, 10);
    }

    public void setGeneration(int g) { this.generation = g; }

    public boolean isTired() {
        return level().getGameTime() < tiredUntil;
    }

    /** Wiąże konia z graczem (po poprawnej odpowiedzi w dialogu). */
    public void bondTo(ServerPlayer player) {
        setTamed(true);
        setOwnerUUID(player.getUUID());
        clearRestriction();
        QuestProgress prog = QuestCapability.get(player);
        prog.tugUuid = getUUID();
        generation = prog.tugGeneration;
        QuestEngine.fireEvent(player, "wyrwij_oswojony", 1);
        playSound(SoundEvents.HORSE_AMBIENT, 1f, 1.1f);
    }

    @Override
    public boolean isSaddled() {
        return isTamed();
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (level().isClientSide) return InteractionResult.SUCCESS;
        ServerPlayer sp = (ServerPlayer) player;
        if (!isTamed()) {
            DialogueService.open(sp, "wyrwij", this);
            return InteractionResult.CONSUME;
        }
        if (!player.getUUID().equals(getOwnerUUID())) {
            sp.displayClientMessage(Component.literal("Wyrwij parska nieufnie i odsuwa się.").withStyle(ChatFormatting.GRAY), true);
            makeMad();
            return InteractionResult.CONSUME;
        }
        ItemStack held = player.getItemInHand(hand);
        if (held.is(Items.APPLE) || held.is(Items.CARROT) || held.is(Items.GOLDEN_CARROT)) {
            heal(held.is(Items.GOLDEN_CARROT) ? 12f : 5f);
            tiredUntil = 0;
            if (!player.getAbilities().instabuild) held.shrink(1);
            playSound(SoundEvents.HORSE_EAT, 1f, 1f);
            sp.displayClientMessage(Component.literal("Wyrwij chrupie z zadowoleniem.").withStyle(ChatFormatting.GREEN), true);
            return InteractionResult.CONSUME;
        }
        if (player.isSecondaryUseActive()) {
            DialogueService.open(sp, "wyrwij", this);
            return InteractionResult.CONSUME;
        }
        if (isTired()) {
            sp.displayClientMessage(Component.literal("Wyrwij jest wyczerpany. Daj mu odpocząć albo jabłko.").withStyle(ChatFormatting.GRAY), true);
            return InteractionResult.CONSUME;
        }
        doPlayerRide(player);
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!level().isClientSide && isTamed() && amount >= getHealth()) {
            exhaust();
            return false;
        }
        return super.hurt(source, amount);
    }

    private void exhaust() {
        setHealth(getMaxHealth() * 0.5f);
        ejectPassengers();
        tiredUntil = level().getGameTime() + 1200;
        Player owner = getOwnerUUID() != null ? level().getPlayerByUUID(getOwnerUUID()) : null;
        if (owner != null) {
            owner.displayClientMessage(Component.literal("Wyrwij jest wyczerpany i ucieka do chaty Halta!").withStyle(ChatFormatting.RED), true);
        }
        if (getServer() != null) {
            BlockPos home = Locations.peek(getServer(), "chata_halta:wyrwij");
            if (home != null) teleportTo(home.getX() + 0.5, home.getY(), home.getZ() + 0.5);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide || !(level() instanceof ServerLevel)) return;
        if (tickCount % 40 != 0 || !isTamed() || getOwnerUUID() == null || getServer() == null) return;
        ServerPlayer owner = getServer().getPlayerList().getPlayer(getOwnerUUID());
        if (owner == null) return;
        if (QuestCapability.get(owner).tugGeneration != generation) {
            discard();
            return;
        }
        if (owner.level() == level() && owner.distanceTo(this) < 24 && level().getGameTime() - lastWarning > 600) {
            var foes = level().getEntitiesOfClass(Mob.class, getBoundingBox().inflate(20),
                    m -> m instanceof Enemy && m.isAlive() && !m.hasEffect(MobEffects.GLOWING));
            if (!foes.isEmpty()) {
                lastWarning = level().getGameTime();
                playSound(SoundEvents.HORSE_ANGRY, 1.2f, 1f);
                for (LivingEntity f : foes) f.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0, false, false));
                owner.displayClientMessage(Component.literal("Wyrwij parska nerwowo - wyczuł wroga w pobliżu!").withStyle(ChatFormatting.GOLD), true);
            }
        }
    }

    @Override
    public boolean canMate(Animal other) { return false; }

    @Override
    @Nullable
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) { return null; }

    @Override
    public boolean removeWhenFarAway(double distance) { return false; }

    @Override protected SoundEvent getAmbientSound() { return SoundEvents.HORSE_AMBIENT; }
    @Override protected SoundEvent getHurtSound(DamageSource src) { return SoundEvents.HORSE_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.HORSE_DEATH; }
    @Override protected SoundEvent getAngrySound() { return SoundEvents.HORSE_ANGRY; }
    @Override protected SoundEvent getEatingSound() { return SoundEvents.HORSE_EAT; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (homePos != null) tag.put("Home", NbtUtils.writeBlockPos(homePos));
        tag.putInt("Generation", generation);
        tag.putLong("TiredUntil", tiredUntil);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Home")) {
            homePos = NbtUtils.readBlockPos(tag.getCompound("Home"));
            if (!isTamed()) restrictTo(homePos, 10);
        }
        generation = tag.getInt("Generation");
        tiredUntil = tag.getLong("TiredUntil");
    }
}
