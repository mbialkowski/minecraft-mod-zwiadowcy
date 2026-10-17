package pl.zwiadowcy.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import pl.zwiadowcy.registry.ModEntities;

import javax.annotation.Nullable;

/**
 * Niedźwiedź brunatny z lasów Araluenu. Neutralny, ale atakuje każdego, kto podejdzie bliżej niż 6 bloków
 * i się nie skrada - zwiadowca przejdzie obok, jeśli zachowa ostrożność.
 */
public class BearEntity extends PolarBear {

    public BearEntity(EntityType<? extends PolarBear> type, Level level) {
        super(type, level);
        xpReward = 8;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PolarBear.createAttributes()
                .add(Attributes.MAX_HEALTH, 36.0)
                .add(Attributes.ATTACK_DAMAGE, 7.0)
                .add(Attributes.MOVEMENT_SPEED, 0.27);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, true,
                p -> !isBaby() && !p.isCrouching() && p.distanceToSqr(this) < 36));
    }

    @Override
    @Nullable
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        return ModEntities.NIEDZWIEDZ.get().create(level);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.SWEET_BERRIES) || stack.is(Items.HONEYCOMB);
    }
}
