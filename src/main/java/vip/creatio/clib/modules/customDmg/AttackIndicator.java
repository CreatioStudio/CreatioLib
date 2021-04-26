package vip.creatio.clib.modules.customDmg;

import vip.creatio.clib.modules.Tags;
import vip.creatio.basic.util.EntityUtil;
import vip.creatio.basic.chat.Component;
import vip.creatio.basic.nbt.CompoundTag;
import vip.creatio.common.Mth;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

public class AttackIndicator extends Indicator {

    private static final Vector deltaAcc = new Vector(0, -0.005D, 0);
    private static final CompoundTag initNBT =
            new CompoundTag("{CustomNameVisible:1b,Invisible:1b,Small:1b,NoBasePlate:1b,Marker:1b}");

    private double lastVelocity = Integer.MAX_VALUE;
    protected final Vector velocity;
    protected final Component display;
    protected final Vector acc = new Vector(0, Mth.nextDouble(0.07D, 0.08D), 0);

    public AttackIndicator(Location loc, Component display, Vector initVel) {
        super(EntityUtil.spawn(loc.getWorld(), initNBT, display, loc, EntityType.ARMOR_STAND), 60,
                null);
        ArmorStand as = getCarrier();
        as.addScoreboardTag(Tags.CUSTOM);
        as.addScoreboardTag(Tags.INDICATOR);
        EntityUtil.setLoc(as, loc);
        this.velocity = initVel;
        this.display = display;
    }

    public AttackIndicator(Location loc, float amount) {
        this(loc, Component.of(VALUE_FORMAT.format(amount))
                .withColor(ATTACK_IND), new Vector(0, Mth.nextDouble(0.4D, 0.5D), 0));
    }

    public AttackIndicator(Location loc, float amount, Color color) {
        this(loc, Component.of(VALUE_FORMAT.format(amount))
                        .withColor(color),
                new Vector(0, Mth.nextDouble(0.4D, 0.5D), 0));
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
    protected void tick() {
        double len = velocity.length();
        if (lastVelocity > len) {
            velocity.subtract(acc);
            EntityUtil.addLoc(carrier, velocity);
            acc.add(deltaAcc);
        }
        lastVelocity = len;
    }

    @Override
    public ArmorStand getCarrier() {
        return (ArmorStand) carrier;
    }
}
