package pl.zwiadowcy.block;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import pl.zwiadowcy.entity.projectile.ThrowingKnifeEntity;
import pl.zwiadowcy.quest.QuestEngine;

/** Tarcza łucznicza: liczy trafienia strzał i noży, mierzy odległość strzału. */
public class ArcheryTargetBlock extends Block {
    public static final String ORIGIN_TAG = "zw_origin";

    public ArcheryTargetBlock(Properties props) {
        super(props);
    }

    @Override
    public void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
        if (level.isClientSide || !(projectile.getOwner() instanceof ServerPlayer player)) return;
        Vec3 origin = player.position();
        CompoundTag data = projectile.getPersistentData();
        if (data.contains(ORIGIN_TAG)) {
            CompoundTag o = data.getCompound(ORIGIN_TAG);
            origin = new Vec3(o.getDouble("x"), o.getDouble("y"), o.getDouble("z"));
        }
        Vec3 at = hit.getLocation();
        double distance = Math.sqrt(Math.pow(at.x - origin.x, 2) + Math.pow(at.z - origin.z, 2));

        Vec3 center = Vec3.atCenterOf(hit.getBlockPos());
        Vec3 off = at.subtract(center);
        Direction.Axis axis = hit.getDirection().getAxis();
        double dx = axis == Direction.Axis.X ? 0 : off.x;
        double dy = axis == Direction.Axis.Y ? 0 : off.y;
        double dz = axis == Direction.Axis.Z ? 0 : off.z;
        double r = Math.sqrt(dx * dx + dy * dy + dz * dz);
        String ring = r < 0.13 ? "w dziesiątkę!" : r < 0.3 ? "blisko środka" : "trafienie";
        ChatFormatting color = r < 0.13 ? ChatFormatting.GOLD : r < 0.3 ? ChatFormatting.YELLOW : ChatFormatting.WHITE;

        player.displayClientMessage(Component.literal(String.format("Tarcza: %s (%.0f kratek)", ring, distance)).withStyle(color), true);
        if (r < 0.13) level.playSound(null, player.blockPosition(), SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 0.6f, 1.2f);
        if (level instanceof ServerLevel sl && r < 0.13) {
            sl.sendParticles(ParticleTypes.CRIT, at.x, at.y, at.z, 10, 0.1, 0.1, 0.1, 0.2);
        }
        if (data.getBoolean("zw_counted")) return;
        data.putBoolean("zw_counted", true);
        String kind = projectile instanceof ThrowingKnifeEntity ? "knife" : "arrow";
        QuestEngine.onTargetHit(player, kind, distance);
    }

    public static void rememberOrigin(Projectile projectile, BlockPos unused) {
        CompoundTag o = new CompoundTag();
        o.putDouble("x", projectile.getX());
        o.putDouble("y", projectile.getY());
        o.putDouble("z", projectile.getZ());
        projectile.getPersistentData().put(ORIGIN_TAG, o);
    }
}
