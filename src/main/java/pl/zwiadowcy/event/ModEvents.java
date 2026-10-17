package pl.zwiadowcy.event;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import pl.zwiadowcy.Zwiadowcy;
import pl.zwiadowcy.block.ArcheryTargetBlock;
import pl.zwiadowcy.dialogue.DialogueManager;
import pl.zwiadowcy.dialogue.DialogueService;
import pl.zwiadowcy.item.SaxeKnifeItem;
import pl.zwiadowcy.quest.*;
import pl.zwiadowcy.registry.ModEffects;
import pl.zwiadowcy.registry.ModItems;
import pl.zwiadowcy.stealth.Stealth;
import pl.zwiadowcy.stealth.Tracking;
import pl.zwiadowcy.world.Araluen;

@Mod.EventBusSubscriber(modid = Zwiadowcy.MODID)
public final class ModEvents {

    @SubscribeEvent
    public static void onReload(AddReloadListenerEvent event) {
        event.addListener(QuestManager.INSTANCE);
        event.addListener(DialogueManager.INSTANCE);
    }

    @SubscribeEvent
    public static void onCommands(RegisterCommandsEvent event) {
        ZwiadowcyCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer p)) return;
        Stealth.tick(p);
        Tracking.tick(p);
        pl.zwiadowcy.story.StoryService.tick(p);
        if (p.tickCount % 4 == 0) QuestEngine.fastTick(p);
        if (p.tickCount % 10 == 0) QuestEngine.tick(p);
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer p)) return;
        QuestProgress prog = QuestCapability.get(p);
        if (!prog.chronicleGiven) {
            prog.chronicleGiven = true;
            p.getInventory().add(new ItemStack(ModItems.KRONIKA_ARALUENU.get()));
            p.sendSystemMessage(Component.literal("W twoim plecaku leży stara, oprawiona w skórę kronika…").withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
            p.sendSystemMessage(Component.literal("Użyj Kroniki Araluenu (prawy przycisk), aby przenieść się do królestwa Araluenu.").withStyle(ChatFormatting.GRAY));
            pl.zwiadowcy.story.StoryService.queue(p, pl.zwiadowcy.story.StoryTexts.KRONIKA, 60);
        } else {
            pl.zwiadowcy.story.StoryService.remindOnLogin(p);
        }
        QuestEngine.sync(p);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer p) {
            pl.zwiadowcy.story.StoryService.forget(p);
            DialogueService.forget(p);
            Stealth.forget(p);
            Tracking.forget(p);
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer p) QuestEngine.sync(p);
    }

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer p) QuestEngine.sync(p);
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide || victim.getServer() == null) return;
        var data = victim.getPersistentData();
        if (data.contains(QuestActions.TAG_DROP)) {
            Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(data.getString(QuestActions.TAG_DROP)));
            if (item != null) {
                net.minecraft.world.entity.item.ItemEntity drop = victim.spawnAtLocation(new ItemStack(item));
                if (drop != null) {
                    // przedmiot fabularny: świeci przez ściany, nie znika i nie spłonie; strzałka w HUD prowadzi prosto do niego
                    drop.setGlowingTag(true);
                    drop.setUnlimitedLifetime();
                    drop.setInvulnerable(true);
                    drop.getPersistentData().putBoolean(QuestActions.TAG_QUEST_DROP, true);
                }
            }
        }
        if (data.contains(QuestActions.TAG_QUEST) && data.hasUUID(QuestActions.TAG_OWNER)) {
            ServerPlayer owner = victim.getServer().getPlayerList().getPlayer(data.getUUID(QuestActions.TAG_OWNER));
            if (owner != null) {
                QuestEngine.onKill(owner, victim, data.getString(QuestActions.TAG_QUEST));
                return;
            }
        }
        if (event.getSource().getEntity() instanceof ServerPlayer killer) {
            QuestEngine.onKill(killer, victim, null);
        }
    }

    /** Oblany olejem: ogień rani podwójnie. */
    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        LivingEntity e = event.getEntity();
        if (event.getSource().is(DamageTypeTags.IS_FIRE) && e.hasEffect(ModEffects.OBLANY_OLEJEM.get())) {
            event.setAmount(event.getAmount() * 2f);
        }
    }

    /** Groza poziomu II paraliżuje - także skoki. */
    @SubscribeEvent
    public static void onJump(LivingEvent.LivingJumpEvent event) {
        LivingEntity e = event.getEntity();
        var groza = e.getEffect(ModEffects.GROZA.get());
        if (groza != null && groza.getAmplifier() >= 1) {
            Vec3 v = e.getDeltaMovement();
            e.setDeltaMovement(v.x, 0, v.z);
        }
    }

    /** Płaszcz zwiadowcy zmniejsza zasięg, z jakiego moby dostrzegają gracza. */
    @SubscribeEvent
    public static void onVisibility(LivingEvent.LivingVisibilityEvent event) {
        if (event.getEntity() instanceof Player p && (Stealth.wearsCloak(p) || Stealth.bezruchActive(p))) {
            event.modifyVisibility(Stealth.visibility(p));
        }
    }

    /** Promień wokół Redmontu bez Wargali (obejmuje lokacje pierwszego i drugiego aktu). */
    private static final double WARGAL_FREE_RADIUS = 600;

    /** Odległość (w poziomie) od faktycznego położenia zamku Redmont - albo od (0, 0), zanim zamek stanie. */
    private static double distanceToRedmont(net.minecraft.server.level.ServerLevel level, double x, double z) {
        pl.zwiadowcy.world.LocationData.Placed redmont = pl.zwiadowcy.world.LocationData.get(level.getServer()).placed("redmont");
        double cx = redmont != null ? redmont.center.getX() : 0, cz = redmont != null ? redmont.center.getZ() : 0;
        return Math.hypot(x - cx, z - cz);
    }

    /** W Araluenie nie ma zombie ani szkieletów - jedynymi potworami są stworzenia Morgaratha. */
    @SubscribeEvent
    public static void onFinalizeSpawn(MobSpawnEvent.FinalizeSpawn event) {
        if (!(event.getLevel() instanceof net.minecraft.server.level.ServerLevel level) || !Araluen.isAraluen(level)) return;
        if (!(event.getEntity() instanceof Enemy)) return;
        MobSpawnType spawn = event.getSpawnType();
        // Wargale to wrogowie z trzeciego aktu: w spokojnym lennie Redmont (Wensley, chata Halta, farma Boba, legowisko
        // dzika, obóz Zlotu) nie pojawiają się same - tylko w zasadzkach z misji
        if (event.getEntity() instanceof pl.zwiadowcy.entity.WargalEntity && (spawn == MobSpawnType.NATURAL
                || spawn == MobSpawnType.CHUNK_GENERATION || spawn == MobSpawnType.PATROL || spawn == MobSpawnType.EVENT)
                && distanceToRedmont(level, event.getEntity().getX(), event.getEntity().getZ()) < WARGAL_FREE_RADIUS) {
            event.setSpawnCancelled(true);
            return;
        }
        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(event.getEntity().getType());
        if (key != null && key.getNamespace().equals(Zwiadowcy.MODID)) return;
        MobSpawnType type = event.getSpawnType();
        if (type == MobSpawnType.NATURAL || type == MobSpawnType.CHUNK_GENERATION || type == MobSpawnType.PATROL
                || type == MobSpawnType.EVENT || type == MobSpawnType.REINFORCEMENT) {
            event.setSpawnCancelled(true);
        }
    }

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (event.getEntity() instanceof Projectile proj && proj.getOwner() instanceof Player
                && !proj.getPersistentData().contains(ArcheryTargetBlock.ORIGIN_TAG) && !event.loadedFromDisk()) {
            ArcheryTargetBlock.rememberOrigin(proj, proj.blockPosition());
        }
        if (event.getEntity() instanceof Phantom && Araluen.isAraluen(event.getLevel()) && !event.loadedFromDisk()) {
            event.setCanceled(true);
        }
        // spokojne okolice Redmont: wrogowie z losowych struktur nie pojawiają się bliżej niż 350 bloków od zamku,
        // a Wargale - bliżej niż 600 (to wrogowie z trzeciego aktu)
        if (!event.loadedFromDisk() && event.getEntity() instanceof net.minecraft.world.entity.monster.Monster
                && event.getEntity().getTags().contains("zw_losowe") && event.getLevel() instanceof net.minecraft.server.level.ServerLevel sl
                && Araluen.isAraluen(sl)) {
            double radius = event.getEntity() instanceof pl.zwiadowcy.entity.WargalEntity ? WARGAL_FREE_RADIUS : 350;
            if (distanceToRedmont(sl, event.getEntity().getX(), event.getEntity().getZ()) < radius) event.setCanceled(true);
        }
    }

    /** Ciosy saksą w manekin treningowy (misja "Dwa noże"). */
    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer p)) return;
        if (event.getTarget() instanceof ArmorStand stand && stand.getTags().contains("zwiadowcy_manekin")) {
            if (p.getMainHandItem().getItem() instanceof SaxeKnifeItem) {
                QuestEngine.fireEvent(p, "manekin_saksa", 1);
                p.level().playSound(null, stand.blockPosition(), net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_STRONG,
                        net.minecraft.sounds.SoundSource.PLAYERS, 0.8f, 1.1f);
            } else {
                p.displayClientMessage(Component.literal("Halt kazał ćwiczyć saksą.").withStyle(ChatFormatting.GRAY), true);
            }
            event.setCanceled(true);
        }
    }

    private ModEvents() {}
}
