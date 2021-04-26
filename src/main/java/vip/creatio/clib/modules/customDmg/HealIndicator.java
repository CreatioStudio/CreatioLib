package vip.creatio.clib.modules.customDmg;

import vip.creatio.basic.chat.Component;
import vip.creatio.common.Mth;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class HealIndicator extends AttackIndicator {
    public HealIndicator(Location loc, float amount) {
        super(loc, Component.of(VALUE_FORMAT.format(amount))
                .withColor(HEALING_IND),
                new Vector(0, Mth.nextDouble(0.4D, 0.5D), 0));
    }

    public HealIndicator(Location loc, float amount, Color color) {
        super(loc, Component.of(VALUE_FORMAT.format(amount))
                        .withColor(color),
                new Vector(0, Mth.nextDouble(0.4D, 0.5D), 0));
    }
}
