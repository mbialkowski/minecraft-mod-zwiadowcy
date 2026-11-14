package pl.zwiadowcy.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.UUID;

/**
 * Skandianie (tom II, bród na Równinie Uthal i rozlewisko): wojownik z okrągłą tarczą, która z przodu (stożek 120°)
 * zatrzymuje strzały i noże - obraca się wolno, więc trzeba go zajść z boku; berserker poniżej połowy zdrowia
 * wpada w szał (+40% prędkości, odporność na odrzut). Oba walczą zapowiadanymi ciosami topora.
 */
public class SkandianEntity extends StoryFoe {
    public enum Variant { WOJOWNIK, BERSERKER }

    private static final UUID RAGE = UUID.fromString("2a7d0f43-5b1c-4f0e-9d61-0c7a3e5b9f11");
    /** Połowa stożka tarczy (120° → 60° od osi). */
    private static final double SHIELD_COS = Math.cos(Math.toRadians(60));

    private final Variant variant;
    private boolean raging;

    public SkandianEntity(EntityType<? extends SkandianEntity> type, Level level) {
        this(type, level, Variant.WOJOWNIK);
    }

    public SkandianEntity(EntityType<? extends SkandianEntity> type, Level level, Variant variant) {
        super(type, level);
        this.variant = variant;
        xpReward = variant == Variant.BERSERKER ? 14 : 12;
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_AXE));
        if (variant == Variant.WOJOWNIK) setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
        setItemSlot(EquipmentSlot.HEAD, new ItemStack(variant == Variant.BERSERKER ? Items.LEATHER_HELMET : Items.IRON_HELMET));
        for (EquipmentSlot s : EquipmentSlot.values()) setDropChance(s, 0f);
        // wolny obrót: tarczę trzeba obejść
        this.lookControl = new LookControl(this) {
            @Override
            protected boolean resetXRotOnTick() { return false; }
        };
        this.moveControl = new MoveControl(this) {
            @Override
            public void tick() {
                super.tick();
                if (variant == Variant.WOJOWNIK && !raging) {
                    // ogranicz obrót ciała do 9° na tik (vanilla: 90°)
                    mob.setYRot(Mth.approachDegrees(mob.yRotO, mob.getYRot(), 9f));
                    mob.yBodyRot = mob.getYRot();
                }
            }
        };
    }

    public Variant variant() { return variant; }

    public static AttributeSupplier.Builder attributes(Variant v) {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, v == Variant.BERSERKER ? 50 : 40)
                .add(Attributes.MOVEMENT_SPEED, v == Variant.BERSERKER ? 0.3 : 0.26)
                .add(Attributes.ATTACK_DAMAGE, v == Variant.BERSERKER ? 10 : 8)
                .add(Attributes.ARMOR, v == Variant.BERSERKER ? 2 : 6)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.3)
                .add(Attributes.FOLLOW_RANGE, 32);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new AxeGoal());
        goalSelector.addGoal(4, new MarchGoal(this, 0.9));
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.7));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, StoryNpc.class, 10, true, false,
                e -> e instanceof StoryNpc n && n.isBattleAlly()));
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;
        if (variant == Variant.BERSERKER && !raging && getHealth() < getMaxHealth() * 0.5f) {
            raging = true;
            var speed = getAttribute(Attributes.MOVEMENT_SPEED);
            if (speed != null && speed.getModifier(RAGE) == null) {
                speed.addPermanentModifier(new AttributeModifier(RAGE, "szal_berserkera", 0.4, AttributeModifier.Operation.MULTIPLY_BASE));
            }
            var kb = getAttribute(Attributes.KNOCKBACK_RESISTANCE);
            if (kb != null) kb.setBaseValue(1.0);
            playSound(SoundEvents.RAVAGER_ROAR, 1.2f, 1.3f);
            if (level() instanceof ServerLevel sl) sl.sendParticles(ParticleTypes.ANGRY_VILLAGER, getX(), getEyeY() + 0.3, getZ(), 6, 0.4, 0.3, 0.4, 0);
        }
    }

    /** Tarcza: pocisk z przodu (stożek 120°) odbija się bez obrażeń. */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (variant == Variant.WOJOWNIK && source.getDirectEntity() instanceof Projectile proj && !winding()) {
            Vec3 from = proj.position().subtract(position()).multiply(1, 0, 1);
            Vec3 facing = Vec3.directionFromRotation(0, yBodyRot);
            if (from.lengthSqr() > 1e-4 && facing.dot(from.normalize()) >= SHIELD_COS) {
                playSound(SoundEvents.SHIELD_BLOCK, 1f, 0.9f + random.nextFloat() * 0.2f);
                if (!level().isClientSide) proj.discard();
                return false;
            }
        }
        return super.hurt(source, amount);
    }

    /** Topór: na przemian cięcie z góry i zamach z boku, oba zapowiadane. */
    private class AxeGoal extends Goal {
        private int index;

        AxeGoal() { setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }

        @Override
        public boolean canUse() {
            return getTarget() != null && getTarget().isAlive();
        }

        @Override
        public void tick() {
            LivingEntity t = getTarget();
            if (t == null) return;
            getLookControl().setLookAt(t, 20f, 30f);
            if (winding()) return;
            double d = distanceTo(t);
            if (d > 2.6) getNavigation().moveTo(t, raging ? 1.25 : 1.0);
            else getNavigation().stop();
            if (d <= 3.2 && !busy()) {
                float dmg = (float) getAttributeValue(Attributes.ATTACK_DAMAGE);
                Swing s = index++ % 2 == 0
                        ? new Swing("cios toporem z góry", raging ? 11 : 15, dmg, 3.0, 0.5, null)
                        : new Swing("zamach z boku", raging ? 12 : 17, dmg * 0.85f, 3.4, 0.0, null);
                startSwing(s, t);
            }
        }
    }

    @Override protected SoundEvent getAmbientSound() { return SoundEvents.VINDICATOR_AMBIENT; }
    @Override protected SoundEvent getHurtSound(DamageSource src) { return SoundEvents.VINDICATOR_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.VINDICATOR_DEATH; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Raging", raging);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        raging = tag.getBoolean("Raging");
    }
}
