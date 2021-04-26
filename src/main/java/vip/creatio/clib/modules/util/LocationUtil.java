package vip.creatio.clib.modules.util;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class LocationUtil {

    //Get a local coord location from a given location, just like the one in vanilla.
    public static Location localCoords(Location location, double x0, double y0, double z0) {
        Location loc = location.clone();
        double a = Math.toRadians(loc.getYaw());
        double b = Math.toRadians(loc.getPitch());
        double x = (x0 * Math.cos(a)) - (y0 * Math.sin(b) * Math.sin(a)) - (z0 * Math.cos(b) * Math.sin(a));
        double y = (y0 * Math.cos(b)) - (z0 * Math.sin(b));
        double z = (x0 * Math.sin(a)) + (y0 * Math.sin(b) * Math.cos(a)) + (z0 * Math.cos(b) * Math.cos(a));
        return loc.add(x, y, z);
    }

    //Get a local coords location by with NWES direction only.
    public static Location localBlockCoords(Location location, double x0, double y0, double z0) {
        Location loc = location.clone();
        float a = loc.getYaw();
        double sin = 0d, cos = 0d;
        if (a < -135 || a >= 135) {
            sin = 0d;
            cos = -1d;
        } else if (a >= -135 && a < -45) {
            sin = -1d;
            cos = 0d;
        } else if (a >= -45 && a < 45) {
            sin = 0d;
            cos = 1d;
        } else if (a >= 45 && a < 135) {
            sin = 1d;
            cos = 0d;
        }
        double x = (x0 * cos) - (z0 * sin);
        double z = (x0 * sin) + (z0 * cos);
        return loc.add(x, y0, z);
    }

    //Get the block on a local coords location
    public static Block localBlockGet(Block block, BlockFace facing, Integer left, Integer up, Integer forward) {
        Location loc = block.getLocation();
        loc.setYaw(Direction.valueOf(facing.name()).getYaw());
        loc = localBlockCoords(loc, (double) left, (double) up, (double) forward);
        return loc.getBlock();
    }

    //Set the yaw of a location to a specific direction
    public static Location setDirection(Location loc, BlockFace facing) {
        loc.setYaw(Direction.valueOf(facing.name()).getYaw());
        return loc;
    }

    //Check if a BlockData contains another(BlockData check)
    public static boolean directionalBlockDataCheck(BlockData original, Object[] template, BlockFace facing, boolean isFlipped) {
        if (original.getMaterial() != template[0]) return false;
        String ori = original.toString();

        if (template.length > 1) {
            for (int i = 1; i < template.length; i ++) {
                String d = ((String) template[i]);
                if (d.contains("=\\?") || d.contains("east=") || d.contains("west=") || d.contains("north=") || d.contains("south=")) continue;
                if (d.contains("facing=")) {
                    String direction = d.replaceAll("facing=","").toUpperCase();
                    if (isFlipped) {
                        if (direction.contains("EAST")) {
                            direction = "WEST";
                        } else if (direction.contains("WEST")) {
                            direction = "EAST";
                        }
                    }
                    d = "facing=" + (Direction.valueOf(direction).getConvertedFacing(facing).name().toLowerCase());
                } else if (d.contains("axis=")) {
                    d = "axis=" + relativeFacingAxis(d.replaceAll("axis=",""), facing);
                }
                if (!ori.contains(d)) return false;

            }
        }
        return true;
    }

    //Check if a location is in an area of two location
    public static boolean inArea(Location targetLoc, Location area1, Location area2) {
        if ((area1.getWorld() == area2.getWorld()) && (targetLoc.getWorld() == area1.getWorld())) {
            if ((targetLoc.getX() >= area1.getX() && targetLoc.getX() <= area2.getX())
                    || (targetLoc.getX() <= area1.getX() && targetLoc.getX() >= area2.getX())) {
                if ((targetLoc.getY() >= area1.getY() && targetLoc.getY() <= area2.getY())
                        || (targetLoc.getY() <= area1.getY() && targetLoc.getY() >= area2.getY())) {
                    return (targetLoc.getZ() >= area1.getZ() && targetLoc.getZ() <= area2.getZ())
                            || (targetLoc.getZ() <= area1.getZ() && targetLoc.getZ() >= area2.getZ());
                }
            }
        }
        return false;
    }

    //give a cube edge from two given locations
    public static List<Location> drawCube(Location location1, Location location2, Float density, boolean isBlock) {
        Location loc1 = location1.clone(), loc2 = location2.clone();

        loc1.setX(Math.min(location1.getX(),location2.getX()));
        loc1.setY(Math.min(location1.getY(),location2.getY()));
        loc1.setZ(Math.min(location1.getZ(),location2.getZ()));
        loc2.setX(Math.max(location1.getX(),location2.getX()));
        loc2.setY(Math.max(location1.getY(),location2.getY()));
        loc2.setZ(Math.max(location1.getZ(),location2.getZ()));

        if (isBlock) {
            loc1 = loc1.getBlock().getLocation();
            loc2 = loc2.getBlock().getLocation();
            loc2.add(1, 1, 1);
        }

        double deltaX = Math.abs(loc1.getX() - loc2.getX());
        double deltaY = Math.abs(loc1.getY() - loc2.getY());
        double deltaZ = Math.abs(loc1.getZ() - loc2.getZ());

        int dx = (int) Math.ceil(deltaX / density);
        int dy = (int) Math.ceil(deltaY / density);
        int dz = (int) Math.ceil(deltaZ / density);

        List<Location> collection = new ArrayList<>();
        int[][] var1 = {{0, 0}, {1, 0}, {0, 1}, {1, 1}};
        for (int[] i : var1) {
            Location locx = loc1.clone().add(i[0] * deltaX, 0, i[1] * deltaZ);
            collection.addAll(drawline(locx, locx.clone().add(0, deltaY, 0),dy));

            locx = loc1.clone().add(0, i[0] * deltaY, i[1] * deltaZ);
            collection.addAll(drawline(locx, locx.clone().add(deltaX, 0, 0),dx));

            locx = loc1.clone().add(i[0] * deltaX, i[1] * deltaY, 0);
            collection.addAll(drawline(locx, locx.clone().add(0, 0, deltaZ),dz));
        }
        return collection;
    }


    //give a line of locations from two give locations
    public static List<Location> drawline(Location location1, Location location2, Integer split) {
        Vector vector = location2.toVector().subtract(location1.toVector());
        List<Location> collection = new ArrayList<>();
        double speedx = vector.getX() / split;
        double speedy = vector.getY() / split;
        double speedz = vector.getZ() / split;
        for (int i = 0; i <= split; i++) {
            collection.add(location1.clone().add(speedx * i, speedy * i, speedz * i));
        }
        return collection;
    }

    //Relative Block Axis Convertion
    public static String relativeFacingAxis(String ori, BlockFace facing) {
        if (facing == BlockFace.EAST || facing == BlockFace.WEST) {
            switch (ori.toLowerCase()) {
                case "x":
                    return "z";
                case "z":
                    return "x";
            }
        }
        return ori;
    }



    public enum Direction {

        NORTH(new Vector(0, 0, -1), 0, -180),
        EAST(new Vector(1, 0, 0), Math.PI / 2, -90),
        SOUTH(new Vector(0, 0, 1), Math.PI, 0),
        WEST(new Vector(-1, 0, 0), Math.PI / -2, 90);

        private final Vector direction;
        private final double init;
        private final float yaw;

        private Direction(Vector direction, double init, float yaw) {
            this.direction = direction;
            this.init = init;
            this.yaw = yaw;
        }

        private Vector getVector() {
            return this.direction;
        }

        private float getYaw() {
            return this.yaw;
        }

        public BlockFace getConvertedFacing(BlockFace facing) {
            return vectorToBlockFace(this.getVector().clone().rotateAroundY(valueOf(facing.name()).init));
        }

        public BlockFace vectorToBlockFace(Vector v) {
            if (v.getX() < -0.8 && v.getX() > -1.2) return BlockFace.EAST;
            if (v.getZ() > 0.8 && v.getZ() < 1.2) return BlockFace.SOUTH;
            if (v.getX() > 0.8 && v.getX() < 1.2) return BlockFace.WEST;
            //if (v.getZ() < -0.8 && v.getZ() > -1.2) return BlockFace.NORTH;
            return BlockFace.NORTH;
        }
    }
}
