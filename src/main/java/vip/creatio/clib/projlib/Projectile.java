package vip.creatio.clib.projlib;

import vip.creatio.clib.Creatio;
import vip.creatio.basic.tools.Task;
import vip.creatio.basic.tools.TaskType;
import vip.creatio.clib.modules.function.Tickable;
import vip.creatio.clib.modules.util.GeoLocation;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.*;

public abstract class Projectile implements Tickable {

    private static final LinkedList<Projectile> PROJECTILES = new LinkedList<>();

    @Task
    static void tickProjectiles() {
        synchronized (PROJECTILES) {
            Iterator<Projectile> itr = PROJECTILES.iterator();
            while (itr.hasNext()) {
                Projectile p = itr.next();
                if (p.getLoc().getY() < -16) p.discard();
                try {
                    if (p.isDiscarded()) itr.remove();
                    else {
                        for (int i = 0, j = p.getFrequency(); i < j; i++)
                            p.tick();
                    }
                } catch (Throwable t) {
                    Creatio.intern("Generated a exception while ticking projectile " + p + ", auto killing...");
                    itr.remove();
                    t.printStackTrace();
                }
            }
        }
    }

    public static void addProjectile(Projectile p) {
        synchronized (PROJECTILES) {
            PROJECTILES.addFirst(p);
            if (PROJECTILES.size() > Creatio.getInstance().maxProjectiles())
                PROJECTILES.removeLast();
        }
    }

    public static boolean removeProjectile(Projectile p) {
        synchronized (PROJECTILES) {
            if (PROJECTILES.remove(p)) {
                p.discard();
                return true;
            }
            return false;
        }
    }

    @Task(value = TaskType.ON_UNLOAD)
    public static void clearAllProjectile() {
        synchronized (PROJECTILES) {
            PROJECTILES.forEach(Projectile::discard);
            PROJECTILES.clear();
        }
    }

    public static List<Projectile> getAllProjectiles() {
        return Collections.unmodifiableList(PROJECTILES);
    }



    protected static final Vector ZERO = new Vector(0, 0, 0);

    protected GeoLocation center;

    // Sync with Minecraft, 20 times / second
    @Override
    public abstract void tick();

    public abstract GeoLocation getLoc();

    public abstract World getWorld();
    public abstract void setLoc(Location loc);

    public abstract Vector getVelocity();

    public abstract BoundingBox getHitbox();
    public abstract void setHitbox(double hitbox_x, double hitbox_y, double hitbox_z);

    public abstract void discard();

    public abstract boolean isDiscarded();

    public abstract int getMaxTickingTimes();
    public abstract int getTickingTimes();

    public abstract void setMaxTickingTimes(int maxTickingTimes);
    public abstract void setTickingTimes(int tickingTimes);

    public abstract void setFrequency(int freq);
    public abstract int getFrequency();

    public final Projectile addProjectile() {
        addProjectile(this);
        return this;
    }
}
