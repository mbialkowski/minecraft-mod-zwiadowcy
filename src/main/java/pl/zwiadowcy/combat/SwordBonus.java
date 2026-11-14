package pl.zwiadowcy.combat;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pl.zwiadowcy.Zwiadowcy;
import pl.zwiadowcy.item.RangerSwordItem;
import pl.zwiadowcy.skill.Skills;

/**
 * Premie do walki mieczem zwiadowcy z umiejętności tomu II: „Lekcja Gilana” +2 obrażenia,
 * szkolny fechtunek (wybór rycerza w tomie I) +1.
 */
@Mod.EventBusSubscriber(modid = Zwiadowcy.MODID)
public final class SwordBonus {
    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer p) || event.getSource().getDirectEntity() != p) return;
        if (!(p.getMainHandItem().getItem() instanceof RangerSwordItem)) return;
        float bonus = 0;
        if (Skills.has(p, Skills.LEKCJA_GILANA)) bonus += 2;
        if (Skills.has(p, Skills.SZKOLNY_FECHTUNEK)) bonus += 1;
        if (bonus > 0) event.setAmount(event.getAmount() + bonus);
    }

    private SwordBonus() {}
}
