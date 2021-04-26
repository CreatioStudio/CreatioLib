package vip.creatio.clib.modules.customDmg;

import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class CustomDamageEvent extends EntityDamageByEntityEvent {

    private final Damage dmg;

    public CustomDamageEvent(Damage dmg) {
        super(dmg.getAttacker() == null ? dmg.getVictim() : dmg.getAttacker(),
                dmg.getVictim(), dmg.getCause(), dmg.getDamage());
        this.dmg = dmg;
    }

    public Damage getCustomDmg() {
        return dmg;
    }
}
