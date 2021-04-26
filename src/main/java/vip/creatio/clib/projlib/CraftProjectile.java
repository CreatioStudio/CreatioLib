package vip.creatio.clib.projlib;

import vip.creatio.clib.modules.util.GeoLocation;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class CraftProjectile extends Projectile {

    class InternalOrbitUnit implements OrbitUnit {
        private final Function<CraftProjectile, Vector> tick;

        InternalOrbitUnit(Function<CraftProjectile, Vector> tick) {this.tick = tick;}

        @Override
        public Vector nextLocOffset() {
            return tick.apply(CraftProjectile.this);
        }
    }

    private double hitbox_x;
    private double hitbox_y;
    private double hitbox_z;
    private Vector nextVec;

    private int hitTimes;
    private int maxTickingTimes;
    private int tickingTimes = 0;
    private int frequency = 1;

    private boolean discarded;
    private boolean hitEntity;
    private boolean hitGround;

    protected @NotNull OrbitUnit move;

    public CraftProjectile(@NotNull Location loc,
                           double hitbox_x,
                           double hitbox_y,
                           double hitbox_z,
                           int max_ticking_times,
                           @Nullable OrbitUnit moveTick,
                           boolean hitEntity,
                           boolean hitGround) {
        this.move = moveTick == null ? () -> ZERO : moveTick;
        this.center = new GeoLocation(loc);
        this.hitbox_x = hitbox_x;
        this.hitbox_y = hitbox_y;
        this.hitbox_z = hitbox_z;
        this.maxTickingTimes = max_ticking_times;
        this.hitEntity = hitEntity;
        this.hitGround = hitGround;
    }

    private static final int[][] cubicPoints = {
            {-1, -1, -1},
            {-1, -1, +1},
            {-1, +1, -1},
            {-1, +1, +1},
            {+1, -1, -1},
            {+1, -1, +1},
            {+1, +1, -1},
            {+1, +1, +1}
    };

    protected void onTick() {}

    protected void onHit(LivingEntity e) {}

    protected void onHitGround(Block b) {}

    protected void onDiscard() {}

    @Override
    public void tick() {
        assert center.getWorld() != null;

        if (nextVec == null) {
            nextVec = move.nextLocOffset();
        }
        center.add(nextVec);
        center.setDirection(nextVec);
        nextVec = move.nextLocOffset();
        if (tickingTimes++ >= maxTickingTimes) discard();

        onTick();

        if (hitEntity || hitGround) {

            Location corner = center.clone();
            Vector ctr = center.toVector(), selectedCorner = null;
            double len = nextVec.length();
            double nearestLen = Double.MAX_VALUE;
            RayTraceResult result = null;

            // Get nearest ray casting
            for (int[] cubicPoint : cubicPoints) {
                corner.setX(center.getX() + hitbox_x * cubicPoint[0]);
                corner.setY(center.getY() + hitbox_y * cubicPoint[1]);
                corner.setZ(center.getZ() + hitbox_z * cubicPoint[2]);
                RayTraceResult r;
                if (hitEntity && hitGround)
                    r = getWorld().rayTrace(
                            corner,
                            nextVec,
                            len,
                            FluidCollisionMode.NEVER,
                            true,
                            0D,
                            e -> e instanceof LivingEntity);
                else if (hitEntity)
                    r = getWorld().rayTraceEntities(
                            corner,
                            nextVec,
                            len,
                            e -> e instanceof LivingEntity);
                else
                    r = getWorld().rayTraceBlocks(
                            corner,
                            nextVec,
                            len,
                            FluidCollisionMode.NEVER,
                            true);
                if (r != null) {
                    double dist = ctr.distance(r.getHitPosition());
                    if (nearestLen > dist) {
                        nearestLen = dist;
                        result = r;
                        selectedCorner = corner.toVector();
                    }
                }
            }

            if (result != null) {
                if (result.getHitEntity() != null
                    && !result.getHitEntity().isInvulnerable()) {

                    ClibProjectileHitEvent event = new ClibProjectileHitEvent(result.getHitEntity(), this);
                    Bukkit.getPluginManager().callEvent(event);
                    if (event.isCancelled()) return;

                    hitTimes++;

                    center.moveTo(result.getHitPosition()).subtract(selectedCorner.subtract(ctr));
                    onHit((LivingEntity) result.getHitEntity());
                }
                if (result.getHitBlock() != null) {

                    ClibProjectileHitGroundEvent event = new ClibProjectileHitGroundEvent(result.getHitBlock(), this);
                    Bukkit.getPluginManager().callEvent(event);
                    if (event.isCancelled()) return;

                    center.moveTo(result.getHitPosition())
                            .subtract(selectedCorner.subtract(ctr));
                    onHitGround(result.getHitBlock());
                }

            }
        }
    }

    @Override
    public GeoLocation getLoc() {
        return center;
    }

    @Override
    public void setLoc(Location loc) {
        this.center = new GeoLocation(loc);
    }

    @Override
    public World getWorld() {
        return center.getWorld();
    }

    @Override
    public void discard() {
        Bukkit.getPluginManager().callEvent(new ClibProjectileDiscardEvent(this));
        onDiscard();
        discarded = true;
    }

    @Override
    public boolean isDiscarded() {
        return discarded;
    }

    @Override
    public BoundingBox getHitbox() {
        return new BoundingBox(center.getX() - hitbox_x,
                center.getY() - hitbox_y,
                center.getZ() - hitbox_z,
                center.getX() + hitbox_x,
                center.getY() + hitbox_y,
                center.getZ() + hitbox_z);
    }

    public void setHitbox(double hitbox_x, double hitbox_y, double hitbox_z) {
        this.hitbox_x = hitbox_x;
        this.hitbox_y = hitbox_y;
        this.hitbox_z = hitbox_z;
    }

    public int getHitTimes() {
        return hitTimes;
    }

    public boolean willHitEntity() {
        return hitEntity;
    }

    public boolean willHitGround() {
        return hitGround;
    }

    public void setHitEntity(boolean flag) {
        this.hitEntity = flag;
    }

    public void setHitGround(boolean flag) {
        this.hitGround = flag;
    }

    @Override
    public void setFrequency(int freq) {
        this.frequency = freq;
    }

    @Override
    public int getFrequency() {
        return frequency;
    }

    @Override
    public Vector getVelocity() {
        return nextVec;
    }

    @Override
    public int getMaxTickingTimes() {
        return maxTickingTimes;
    }

    @Override
    public int getTickingTimes() {
        return tickingTimes;
    }

    @Override
    public void setMaxTickingTimes(int maxTickingTimes) {
        this.maxTickingTimes = maxTickingTimes;
    }

    @Override
    public void setTickingTimes(int tickingTimes) {
        this.tickingTimes = tickingTimes;
    }
}
