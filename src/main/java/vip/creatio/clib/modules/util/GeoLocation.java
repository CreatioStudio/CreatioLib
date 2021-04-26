package vip.creatio.clib.modules.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GeoLocation extends Location {
    public GeoLocation(@Nullable World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    public GeoLocation(@Nullable World world, double x, double y, double z, float yaw, float pitch) {
        super(world, x, y, z, yaw, pitch);
    }

    public GeoLocation(@NotNull Location loc) {
        super(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }

    public GeoLocation(@NotNull Vector vec, @NotNull World world) {
        this(vec.toLocation(world));
    }

    public @NotNull GeoLocation addLocal(double left, double upwards, double forwards) {
        double a = Math.toRadians(this.getYaw());
        double b = Math.toRadians(this.getPitch());
        double x = (left * Math.cos(a)) - (upwards * Math.sin(b) * Math.sin(a)) - (forwards * Math.cos(b) * Math.sin(a));
        double y = (upwards * Math.cos(b)) - (forwards * Math.sin(b));
        double z = (left * Math.sin(a)) + (upwards * Math.sin(b) * Math.cos(a)) + (forwards * Math.cos(b) * Math.cos(a));
        return this.add(x, y, z);
    }

    public @NotNull GeoLocation subtractLocal(double left, double upwards, double forwards) {
        double a = Math.toRadians(this.getYaw());
        double b = Math.toRadians(this.getPitch());
        double x = (left * Math.cos(a)) - (upwards * Math.sin(b) * Math.sin(a)) - (forwards * Math.cos(b) * Math.sin(a));
        double y = (upwards * Math.cos(b)) - (forwards * Math.sin(b));
        double z = (left * Math.sin(a)) + (upwards * Math.sin(b) * Math.cos(a)) + (forwards * Math.cos(b) * Math.cos(a));
        return this.subtract(x, y, z);
    }

    public @NotNull Vector localCoordOffset(double left, double upwards, double forwards) {
        double a = Math.toRadians(this.getYaw());
        double b = Math.toRadians(this.getPitch());
        double x = (left * Math.cos(a)) - (upwards * Math.sin(b) * Math.sin(a)) - (forwards * Math.cos(b) * Math.sin(a));
        double y = (upwards * Math.cos(b)) - (forwards * Math.sin(b));
        double z = (left * Math.sin(a)) + (upwards * Math.sin(b) * Math.cos(a)) + (forwards * Math.cos(b) * Math.cos(a));
        return new Vector(x, y, z);
    }

    public @NotNull GeoLocation moveTo(Location loc) {
        setX(loc.getX());
        setY(loc.getY());
        setZ(loc.getZ());
        setYaw(loc.getYaw());
        setPitch(loc.getPitch());
        setWorld(loc.getWorld());
        return this;
    }

    public @NotNull GeoLocation moveTo(Vector vec) {
        assert getWorld() != null;
        return moveTo(vec.toLocation(getWorld()));
    }

    @Override
    public @NotNull GeoLocation setDirection(@NotNull Vector vector) {
        return (GeoLocation) super.setDirection(vector);
    }

    @Override
    public @NotNull GeoLocation add(@NotNull Location vec) {
        return (GeoLocation) super.add(vec);
    }

    @Override
    public @NotNull GeoLocation add(@NotNull Vector vec) {
        return (GeoLocation) super.add(vec);
    }

    @Override
    public @NotNull GeoLocation add(double x, double y, double z) {
        return (GeoLocation) super.add(x, y, z);
    }

    @Override
    public @NotNull GeoLocation subtract(@NotNull Location vec) {
        return (GeoLocation) super.subtract(vec);
    }

    @Override
    public @NotNull GeoLocation subtract(@NotNull Vector vec) {
        return (GeoLocation) super.subtract(vec);
    }

    @Override
    public @NotNull GeoLocation subtract(double x, double y, double z) {
        return (GeoLocation) super.subtract(x, y, z);
    }

    @Override
    public @NotNull GeoLocation multiply(double m) {
        return (GeoLocation) super.multiply(m);
    }

    @Override
    public @NotNull GeoLocation zero() {
        return (GeoLocation) super.zero();
    }

    @Override
    public @NotNull GeoLocation clone() {
        return (GeoLocation) super.clone();
    }
}
