package vip.creatio.clib.modules.customDmg;

import vip.creatio.clib.Creatio;
import vip.creatio.basic.tools.Task;
import vip.creatio.basic.tools.TaskType;
import vip.creatio.basic.util.EntityUtil;
import vip.creatio.basic.util.MthUtil;
import vip.creatio.clib.projlib.OrbitUnit;
import vip.creatio.basic.chat.Component;
import vip.creatio.common.util.Mth;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.*;

public abstract class Indicator {

    public static final Color ATTACK_IND = Color.fromRGB(0xE49047);
    public static final Color ATTACK_IND_CRIT = Color.fromRGB(0xD25118);
    public static final Color DEBUFF_IND = Color.fromRGB(0xD50E04);
    public static final Color HEALING_IND = Color.fromRGB(0x5FF05F);
    public static final Color HEALING_IND_BUFF = Color.fromRGB(0xb0e617);

    public static final DecimalFormat VALUE_FORMAT = new DecimalFormat();
    static {
        VALUE_FORMAT.setMaximumFractionDigits(2);
        VALUE_FORMAT.setGroupingUsed(false);
    }

    private static final LinkedList<Indicator> INDICATORS = new LinkedList<>();

    @Task
    static void tickIndicators() {
        synchronized (INDICATORS) {
            Iterator<Indicator> iter = INDICATORS.iterator();
            while (iter.hasNext()) {
                Indicator dmg = iter.next();
                if (dmg.remainingTicks < 1) {
                    dmg.discard();
                    iter.remove();
                }
                else {
                    try {
                        dmg.tick();
                        dmg.remainingTicks--;
                    } catch (Throwable t) {
                        Creatio.intern("Generated a exception while ticking health indicator " + dmg + ", auto killing...");
                        dmg.discard();
                        iter.remove();
                        t.printStackTrace();
                    }
                }
            }
        }
    }

    public static Indicator newAttackIndicator(Location loc, float amount) {
        return newAttackIndicator(loc, amount, ATTACK_IND);
    }

    public static Indicator newAttackIndicator(Location loc, float amount, Color color) {
        Indicator ind = new AttackIndicator(loc.add(MthUtil.randVecInRadius(Mth.nextDouble(0D, 1D)))
                .add(0D, Mth.nextFloat(-1F, 1F), 0), amount, color);
        addIndicator(ind);
        return ind;
    }

    public static Indicator newCritIndicator(Location loc, float amount) {
        return newCritIndicator(loc, amount, ATTACK_IND_CRIT);
    }

    public static Indicator newCritIndicator(Location loc, float amount, Color color) {
        Indicator ind = new CritAttackIndicator(loc.add(MthUtil.randVecInRadius(Mth.nextDouble(0D, 1D)))
                .add(0D, Mth.nextFloat(-1F, 1F), 0), amount, color);
        addIndicator(ind);
        return ind;
    }

    public static Indicator newHealIndicator(Location loc, float amount) {
        return newHealIndicator(loc, amount, HEALING_IND);
    }

    public static Indicator newHealIndicator(Location loc, float amount, Color color) {
        Indicator ind = new HealIndicator(loc.add(MthUtil.randVecInRadius(Mth.nextDouble(0D, 0.3D)))
                .add(0D, Mth.nextFloat(-0.5F, 1F), 0), amount, color);
        addIndicator(ind);
        return ind;
    }

    public static Indicator newDebuffIndicator(Location loc, float amount) {
        return newDebuffIndicator(loc, amount, DEBUFF_IND);
    }

    public static Indicator newDebuffIndicator(Location loc, float amount, Color color) {
        Indicator ind = new DebuffIndicator(loc.add(MthUtil.randVecInRadius(Mth.nextDouble(0D, 0.3D)))
                .add(0D, Mth.nextFloat(-1.5F, 0.5F), 0), amount, color);
        addIndicator(ind);
        return ind;
    }

    public static void addIndicator(Indicator ind) {
        synchronized (INDICATORS) {
            INDICATORS.addFirst(ind);
            if (INDICATORS.size() > Creatio.getInstance().maxProjectiles()) {
                Indicator i = INDICATORS.removeLast();
                i.discard();
            }
        }
    }

    public static boolean removeIndicator(Indicator ind) {
        synchronized (INDICATORS) {
            if (INDICATORS.remove(ind)) {
                ind.discard();
                return true;
            }
            return false;
        }
    }

    @Task(value = TaskType.ON_UNLOAD)
    public static void clearAllIndicators() {
        synchronized (INDICATORS) {
            INDICATORS.forEach(Indicator::discard);
            INDICATORS.clear();
        }
    }

    public static List<Indicator> getAllIndicators() {
        return Collections.unmodifiableList(INDICATORS);
    }

    private static final Vector ZERO = new Vector(0, 0, 0);

    protected final Entity carrier;
    protected int remainingTicks;
    protected OrbitUnit move;

    protected Indicator(Entity carrier, int remainingTicks, @Nullable OrbitUnit move) {
        this.carrier = carrier;
        this.remainingTicks = remainingTicks;
        this.move = move == null ? () -> ZERO : move;
    }

    protected void tick() {
        EntityUtil.addLoc(carrier, move.nextLocOffset());
    }

    protected abstract Component display();

    protected abstract void discard();

    public Entity getCarrier() {
        return carrier;
    }

    public int getRemainingTicks() {
        return remainingTicks;
    }

    public final Indicator addIndicator() {
        addIndicator(this);
        return this;
    }

    @Override
    public String toString() {
        return "Indicator{carrier=" + carrier + ",remining=" + remainingTicks + '}';
    }
}
