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
        event.addListener(pl.zwiadowcy.cutscene.Cutscenes.INSTANCE);
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
        pl.zwiadowcy.tracking.TrackerSense.tick(p);
        pl.zwiadowcy.cutscene.Cutscenes.tick(p);
        pl.zwiadowcy.world.CelticWeather.tick(p);
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
        pl.zwiadowcy.volume.Volumes.onLogin(p);
        QuestEngine.sync(p);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer p) {
            pl.zwiadowcy.story.StoryService.forget(p);
            DialogueService.forget(p);
            Stealth.forget(p);
            Tracking.forget(p);
            pl.zwiadowcy.tracking.TrackerSense.forget(p);
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer p) {
            QuestEngine.sync(p);
            if (!event.isEndConquered()) pl.zwiadowcy.quest.QuestTom2.onRespawn(p);
        }
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
        // tom II: uratowanie rannego towarzysza (wróg atakował go przy < 30% zdrowia) - zaufanie +2, do 3 razy na misję
        if (victim instanceof net.minecraft.world.entity.Mob mob && mob.getTarget() instanceof pl.zwiadowcy.entity.StoryNpc npc
                && npc.getMode() == pl.zwiadowcy.entity.StoryNpc.Mode.COMPANION && npc.getHealth() < npc.getMaxHealth() * 0.3f
                && event.getSource().getEntity() instanceof ServerPlayer saver && npc.ownerPlayer() == saver) {
            QuestProgress prog = QuestCapability.get(saver);
            String key = "ratunek:" + prog.tracked() + ":" + npc.npcId();
            int n = (int) prog.cooldown(key);
            if (n < 0 || n > 3) n = 0;
            if (n < 3) {
                pl.zwiadowcy.companion.Trust.change(saver, npc.npcId(), 2);
                prog.setCooldown(key, n + 1);
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

    /** Po śmierci gracz nie traci przedmiotów (2.0.2): zasada keepInventory jest włączana przy każdym starcie świata. */
    @SubscribeEvent
    public static void onServerStarted(net.minecraftforge.event.server.ServerStartedEvent event) {
        var rule = event.getServer().getGameRules().getRule(net.minecraft.world.level.GameRules.RULE_KEEPINVENTORY);
        if (!rule.get()) rule.set(true, event.getServer());
    }

    /**
     * Tom II: trafienie z siodła. Liczy się każda strzała gracza jadącego konno - zwykła, specjalna, z łuku zwiadowcy
     * albo zwykłego - która trafi wroga. Jedna strzała to jedno trafienie (odbita i lecąca drugi raz nie liczy się).
     */
    @SubscribeEvent
    public static void onProjectileImpact(net.minecraftforge.event.entity.ProjectileImpactEvent event) {
        if (!(event.getProjectile() instanceof net.minecraft.world.entity.projectile.AbstractArrow arrow) || arrow.level().isClientSide) return;
        if (!(event.getRayTraceResult() instanceof net.minecraft.world.phys.EntityHitResult hit)) return;
        if (!(arrow.getOwner() instanceof ServerPlayer sp)) return;
        var tag = arrow.getPersistentData();
        if (tag.getBoolean("zw_trafila") || (!tag.getBoolean("zw_z_siodla") && sp.getVehicle() == null)) return;
        if (!(hit.getEntity() instanceof LivingEntity target) || !target.isAlive() || target == sp.getVehicle()) return;
        if (!(target instanceof Enemy)) return;
        tag.putBoolean("zw_trafila", true);
        QuestEngine.fireEvent(sp, "trafienie_z_siodla", 1);
    }

    /** Oblany olejem: ogień rani podwójnie. */
    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        LivingEntity e = event.getEntity();
        // tom II: obrażenia od wrogów według poziomu trudności fabularnej
        if (e instanceof ServerPlayer sp && event.getSource().getEntity() instanceof net.minecraft.world.entity.Mob attacker
                && attacker instanceof net.minecraft.world.entity.monster.Enemy && Araluen.isAraluen(sp.level())
                && QuestCapability.get(sp).hasFlag(pl.zwiadowcy.volume.Volumes.FLAG_TOM2_RUNNING)) {
            event.setAmount((float) (event.getAmount() * pl.zwiadowcy.difficulty.StoryDifficulty.of(sp).enemyDamage));
        }
        if (event.getSource().is(DamageTypeTags.IS_FIRE) && e.hasEffect(ModEffects.OBLANY_OLEJEM.get())) {
            event.setAmount(event.getAmount() * 2f);
        }
    }

    /** Fabularne powalenie (etap „survive” bez śmierci): gracz nie spada poniżej pół serca. */
    @SubscribeEvent
    public static void onDamage(net.minecraftforge.event.entity.living.LivingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer p && event.getAmount() >= p.getHealth()
                && pl.zwiadowcy.quest.QuestTom2.protectsFromDeath(p)) {
            event.setAmount(Math.max(0f, p.getHealth() - 1f));
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

    /** PPM na bloku: wskazówka śledztwa (Oczy tropiciela) albo ognisko zwiadowców. */
    @SubscribeEvent
    public static void onRightClickBlock(net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer p) || event.getHand() != net.minecraft.world.InteractionHand.MAIN_HAND) return;
        if (pl.zwiadowcy.quest.QuestTom2.onUseBlock(p, event.getPos())) {
            event.setCanceled(true);
            return;
        }
        if (pl.zwiadowcy.bridge.BridgeBurn.onUseBlock(p, event.getPos(), p.getMainHandItem())) {
            event.setCanceled(true);
            return;
        }
        var clue = pl.zwiadowcy.tracking.TrackerSense.clueAt(p, event.getPos());
        if (clue != null && !pl.zwiadowcy.tracking.Clues.found(QuestCapability.get(p), clue.id())) {
            pl.zwiadowcy.tracking.TrackerSense.onClueFound(p, clue);
            event.setCanceled(true);
        }
    }

    /** Ciosy saksą w manekin treningowy (misja "Dwa noże"). */
    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer p)) return;
        if (event.getTarget() instanceof net.minecraft.world.entity.Mob mob && pl.zwiadowcy.stealth.Alertness.tryTakedown(p, mob)) {
            event.setCanceled(true);
            return;
        }
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
