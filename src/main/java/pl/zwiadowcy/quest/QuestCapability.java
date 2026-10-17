package pl.zwiadowcy.quest;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.zwiadowcy.Zwiadowcy;

public final class QuestCapability {
    public static final Capability<QuestProgress> CAP = CapabilityManager.get(new CapabilityToken<>() {});
    private static final ResourceLocation KEY = Zwiadowcy.id("postep_misji");

    /** Zwraca postęp gracza; jeśli z jakiegoś powodu brak capability, zwraca pusty obiekt (nie zapisywany). */
    public static QuestProgress get(Player player) {
        return player.getCapability(CAP).orElseGet(QuestProgress::new);
    }

    @Mod.EventBusSubscriber(modid = Zwiadowcy.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ModBusEvents {
        @SubscribeEvent
        public static void register(RegisterCapabilitiesEvent event) {
            event.register(QuestProgress.class);
        }
    }

    @Mod.EventBusSubscriber(modid = Zwiadowcy.MODID)
    public static final class ForgeBusEvents {
        @SubscribeEvent
        public static void attach(AttachCapabilitiesEvent<Entity> event) {
            if (event.getObject() instanceof Player) {
                event.addCapability(KEY, new Provider());
            }
        }

        @SubscribeEvent
        public static void clone(PlayerEvent.Clone event) {
            Player original = event.getOriginal();
            original.reviveCaps();
            original.getCapability(CAP).ifPresent(old ->
                    event.getEntity().getCapability(CAP).ifPresent(fresh -> fresh.load(old.save())));
            original.invalidateCaps();
        }
    }

    private static final class Provider implements ICapabilitySerializable<CompoundTag> {
        private final QuestProgress progress = new QuestProgress();
        private final LazyOptional<QuestProgress> optional = LazyOptional.of(() -> progress);

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            return CAP.orEmpty(cap, optional);
        }

        @Override
        public CompoundTag serializeNBT() {
            return progress.save();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            progress.load(nbt);
        }
    }

    private QuestCapability() {}
}
