package vip.creatio.clib.modules.customDmg;

import vip.creatio.basic.chat.Component;
import vip.creatio.common.Mth;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class CritAttackIndicator extends AttackIndicator {
    public CritAttackIndicator(Location loc, float amount) {
        super(loc, Component.of(VALUE_FORMAT.format(amount))
                .withColor(ATTACK_IND_CRIT).withBold(true),
                new Vector(0, Mth.nextDouble(0.4D, 0.6D), 0));
    }

    public CritAttackIndicator(Location loc, float amount, Color color) {
        super(loc, Component.of(VALUE_FORMAT.format(amount))
                        .withColor(color).withBold(true),
                new Vector(0, Mth.nextDouble(0.4D, 0.6D), 0));
    }
}
