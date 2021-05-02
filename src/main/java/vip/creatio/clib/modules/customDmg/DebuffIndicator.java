package vip.creatio.clib.modules.customDmg;

import vip.creatio.clib.modules.Tags;
import vip.creatio.basic.util.EntityUtil;
import vip.creatio.basic.util.MthUtil;
import vip.creatio.basic.chat.Component;
import vip.creatio.basic.nbt.CompoundTag;
import vip.creatio.common.util.Mth;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

public class DebuffIndicator extends Indicator {

    private static final CompoundTag initNBT =
            new CompoundTag("{CustomNameVisible:1b,Invisible:1b,Small:1b,NoBasePlate:1b,Marker:1b}");

    protected final Vector velocity;
    protected final Component display;
    protected final Vector acc = new Vector(0, Mth.nextDouble(0.06D, 0.1D), 0);

    public DebuffIndicator(Location loc, Component display, Vector initVel) {
        super(EntityUtil.spawn(loc.getWorld(), initNBT, display, loc, EntityType.ARMOR_STAND), 12, null);
        ArmorStand as = getCarrier();
        as.addScoreboardTag(Tags.CUSTOM);
        as.addScoreboardTag(Tags.INDICATOR);
        EntityUtil.setLoc(as, loc);
        this.velocity = initVel.add(acc);
        this.display = display;
        super.move = () -> velocity.subtract(acc);
    }

    public DebuffIndicator(Location loc, float amount) {
        this(loc, Component.of(VALUE_FORMAT.format(amount))
                .withColor(Indicator.DEBUFF_IND),
                MthUtil.randVecInRadius(0.07)
                .add(new Vector(0, Mth.nextDouble(0.25D, 0.4D), 0)));
    }

    public DebuffIndicator(Location loc, float amount, Color color) {
        this(loc, Component.of(VALUE_FORMAT.format(amount))
                        .withColor(color),
                MthUtil.randVecInRadius(0.07)
                        .add(new Vector(0, Mth.nextDouble(0.25D, 0.4D), 0)));
    }

    @Override
    protected Component display() {
        return display;
    }

    @Override
    protected void discard() {
        carrier.remove();
    }

    @Override
    public ArmorStand getCarrier() {
        return (ArmorStand) carrier;
    }
}
