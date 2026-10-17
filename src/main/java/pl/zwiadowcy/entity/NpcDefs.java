package pl.zwiadowcy.entity;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import pl.zwiadowcy.registry.ModItems;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/** Postacie niezależne. Tekstury: assets/zwiadowcy/textures/entity/npc/&lt;id&gt;.png. */
public final class NpcDefs {

    public enum Combat { NONE, MELEE, ARCHER }

    public record NpcDef(String id, String name, boolean slim, boolean unique, @Nullable String initialLocation,
                         Combat combat, Supplier<ItemStack> mainHand, StoryNpc.Mode defaultMode) {}

    private static final Map<String, NpcDef> DEFS = new LinkedHashMap<>();

    private static void add(String id, String name, boolean slim, boolean unique, @Nullable String loc,
                            Combat combat, Supplier<ItemStack> hand, StoryNpc.Mode mode) {
        DEFS.put(id, new NpcDef(id, name, slim, unique, loc, combat, hand, mode));
    }

    static {
        Supplier<ItemStack> none = () -> ItemStack.EMPTY;
        add("halt", "Halt", false, true, "redmont", Combat.ARCHER, () -> new ItemStack(ModItems.LUK_ZWIADOWCY.get()), StoryNpc.Mode.IDLE);
        add("baron_arald", "Baron Arald", false, true, "redmont", Combat.MELEE, () -> new ItemStack(Items.IRON_SWORD), StoryNpc.Mode.IDLE);
        add("sir_rodney", "Sir Rodney", false, true, "redmont", Combat.MELEE, () -> new ItemStack(Items.IRON_SWORD), StoryNpc.Mode.IDLE);
        add("mistrz_nigel", "Mistrz Nigel", false, true, "redmont", Combat.NONE, () -> new ItemStack(Items.FEATHER), StoryNpc.Mode.IDLE);
        add("mistrz_chubb", "Mistrz Chubb", false, true, "redmont", Combat.NONE, () -> new ItemStack(Items.WOODEN_SHOVEL), StoryNpc.Mode.IDLE);
        add("lady_pauline", "Lady Pauline", true, true, "redmont", Combat.NONE, none, StoryNpc.Mode.IDLE);
        add("horace", "Horace", false, true, "redmont", Combat.MELEE, () -> new ItemStack(Items.IRON_SWORD), StoryNpc.Mode.IDLE);
        add("alyss", "Alyss", true, true, "redmont", Combat.NONE, none, StoryNpc.Mode.IDLE);
        add("jenny", "Jenny", true, true, "redmont", Combat.NONE, () -> new ItemStack(Items.BREAD), StoryNpc.Mode.IDLE);
        add("stary_bob", "Stary Bob", false, true, "stajnia_boba", Combat.NONE, () -> new ItemStack(Items.WHEAT), StoryNpc.Mode.IDLE);
        add("gilan", "Gilan", false, true, "oboz_zlotu", Combat.ARCHER, () -> new ItemStack(ModItems.LUK_ZWIADOWCY.get()), StoryNpc.Mode.IDLE);
        add("crowley", "Crowley", false, true, "oboz_zlotu", Combat.ARCHER, none, StoryNpc.Mode.IDLE);
        add("krol_duncan", "Król Duncan", false, true, null, Combat.NONE, none, StoryNpc.Mode.IDLE);
        add("alda", "Alda", false, true, "wensley", Combat.MELEE, none, StoryNpc.Mode.IDLE);
        add("bryn", "Bryn", false, true, "wensley", Combat.MELEE, none, StoryNpc.Mode.IDLE);
        add("jerome", "Jerome", false, true, "wensley", Combat.MELEE, none, StoryNpc.Mode.IDLE);
        add("straznik", "Strażnik", false, false, null, Combat.MELEE, () -> new ItemStack(Items.IRON_SWORD), StoryNpc.Mode.SENTRY);
        add("zwiadowca", "Zwiadowca", false, false, null, Combat.ARCHER, () -> new ItemStack(ModItems.LUK_ZWIADOWCY.get()), StoryNpc.Mode.IDLE);
        add("wiesniak", "Wieśniak", false, false, null, Combat.NONE, none, StoryNpc.Mode.IDLE);
        add("karczmarz", "Karczmarz", false, false, null, Combat.NONE, () -> new ItemStack(Items.GLASS_BOTTLE), StoryNpc.Mode.IDLE);
        add("kowal", "Kowal", false, false, null, Combat.MELEE, () -> new ItemStack(Items.IRON_AXE), StoryNpc.Mode.IDLE);
        add("chlopka", "Chłopka", true, false, null, Combat.NONE, () -> new ItemStack(Items.WHEAT), StoryNpc.Mode.IDLE);
        // postoje na szlaku
        add("kupiec", "Wędrowny kupiec", false, false, null, Combat.NONE, none, StoryNpc.Mode.IDLE);
        add("drwal", "Ranny drwal", false, false, null, Combat.NONE, none, StoryNpc.Mode.WOUNDED);
        add("uciekinierka", "Uciekinierka", true, false, null, Combat.NONE, none, StoryNpc.Mode.IDLE);
    }

    @Nullable
    public static NpcDef get(String id) {
        return DEFS.get(id);
    }

    public static Iterable<NpcDef> all() {
        return DEFS.values();
    }

    private NpcDefs() {}
}
