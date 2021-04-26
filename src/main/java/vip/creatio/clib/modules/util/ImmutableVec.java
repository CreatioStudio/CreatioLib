package vip.creatio.clib.modules.util;

import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/** Bukkit vector but will create new instance everytime when modified */
public class ImmutableVec extends Vector {

    public ImmutableVec() {
        super();
    }

    public ImmutableVec(int x, int y, int z) {
        super(x, y, z);
    }

    public ImmutableVec(double x, double y, double z) {
        super(x, y, z);
    }

    public ImmutableVec(float x, float y, float z) {
        super(x, y, z);
    }

    @Override
    public @NotNull ImmutableVec add(@NotNull Vector vec) {
        return new ImmutableVec(x + vec.getX(), y + vec.getY(), z + vec.getZ());
    }

    @Override
    public @NotNull ImmutableVec subtract(@NotNull Vector vec) {
        return new ImmutableVec(x - vec.getX(), y - vec.getY(), z - vec.getZ());
    }

    @Override
    public @NotNull ImmutableVec multiply(@NotNull Vector vec) {
        return new ImmutableVec(x * vec.getX(), y * vec.getY(), z * vec.getZ());
    }

    @Override
    public @NotNull ImmutableVec divide(@NotNull Vector vec) {
        return new ImmutableVec(x / vec.getX(), y / vec.getY(), z / vec.getZ());
    }

    @Override
    public @NotNull ImmutableVec copy(@NotNull Vector vec) {
        return new ImmutableVec(vec.getX(), vec.getY(), vec.getZ());
    }

    @Override
    public @NotNull ImmutableVec multiply(int m) {
        return new ImmutableVec(x * m, y * m, z * m);
    }

    @Override
    public @NotNull ImmutableVec multiply(double m) {
        return new ImmutableVec(x * m, y * m, z * m);
    }

    @Override
    public @NotNull ImmutableVec multiply(float m) {
        return new ImmutableVec(x * m, y * m, z * m);
    }

    @Override
    public @NotNull ImmutableVec crossProduct(@NotNull Vector o) {
        return (ImmutableVec) super.getCrossProduct(o);
    }

    @Override
    public @NotNull ImmutableVec normalize() {
        double length = length();
        return new ImmutableVec(x / length, y / length, z / length);
    }

    @Override
    public @NotNull ImmutableVec zero() {
        return new ImmutableVec(0D, 0D, 0D);
    }

    @Override
    public @NotNull ImmutableVec rotateAroundX(double angle) {
        return (ImmutableVec) clone().rotateAroundX(angle);
    }

    @Override
    public @NotNull ImmutableVec rotateAroundY(double angle) {
        return (ImmutableVec) clone().rotateAroundY(angle);
    }

    @Override
    public @NotNull ImmutableVec rotateAroundZ(double angle) {
        return (ImmutableVec) clone().rotateAroundZ(angle);
    }

    @Override
    public @NotNull ImmutableVec rotateAroundAxis(@NotNull Vector axis, double angle) throws IllegalArgumentException {
        return (ImmutableVec) clone().rotateAroundAxis(axis, angle);
    }

    @Override
    public @NotNull ImmutableVec rotateAroundNonUnitAxis(@NotNull Vector axis, double angle) throws IllegalArgumentException {
        return (ImmutableVec) clone().rotateAroundNonUnitAxis(axis, angle);
    }

    @Override
    public @NotNull ImmutableVec setX(int x) {
        return new ImmutableVec(x, y, z);
    }

    @Override
    public @NotNull ImmutableVec setX(double x) {
        return new ImmutableVec(x, y, z);
    }

    @Override
    public @NotNull ImmutableVec setX(float x) {
        return new ImmutableVec(x, y, z);
    }

    @Override
    public @NotNull ImmutableVec setY(int y) {
        return new ImmutableVec(x, y, z);
    }

    @Override
    public @NotNull ImmutableVec setY(double y) {
        return new ImmutableVec(x, y, z);
    }

    @Override
    public @NotNull ImmutableVec setY(float y) {
        return new ImmutableVec(x, y, z);
    }

    @Override
    public @NotNull ImmutableVec setZ(int z) {
        return new ImmutableVec(x, y, z);
    }

    @Override
    public @NotNull ImmutableVec setZ(double z) {
        return new ImmutableVec(x, y, z);
    }

    @Override
    public @NotNull ImmutableVec setZ(float z) {
        return new ImmutableVec(x, y, z);
    }
}
