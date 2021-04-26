package vip.creatio.clib.multiblock;

import vip.creatio.clib.modules.exception.NoSuchRegisteredStructureException;
import vip.creatio.clib.modules.exception.StructureConstructionException;
import vip.creatio.clib.modules.util.LocationUtil;
import vip.creatio.clib.multiblock.event.MultiblockAPIStructureCreateEvent;
import vip.creatio.clib.multiblock.machine.Machine;
import vip.creatio.common.Mth;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import vip.creatio.clib.Creatio;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

@SuppressWarnings("unchecked")
public class MultiblockStructure implements ConfigurationSerializable {

    //Register Element
    private final RegisteredStructure hooked_registered;

    //Attributes
    private UUID uuid;
    private String name_space;
    private boolean flipped = false;
    private BlockFace facing;
    private Location corner, edcorner, core;
    private boolean enabled = true;
    private final List<Block> block_list = new ArrayList<>();           //block_list all the blocks contained in the structure

    //Ticking Element
    private String name;
    private boolean load_chunk;
    private int load_chunk_radius;

    //Temporarily
    private final List<Chunk> chunk = new ArrayList<>();
    private Machine hooked_machine;

    public static MultiblockStructure fromName(String name_space, Block center, BlockFace facing) throws StructureConstructionException {
        RegisteredStructure reg = StructureManager.getRegisteredStructures().get(name_space);
        if (reg != null) return new MultiblockStructure(reg, center, facing);
        throw new NoSuchRegisteredStructureException("No registered structure named " + name_space);
    }

    public MultiblockStructure(RegisteredStructure reg, Block center, BlockFace facing) throws StructureConstructionException {
        //Check validation
        checkLimitValidation(center);

        //Get vars from registered structures
        this.hooked_registered = reg;
        this.facing = facing;
        this.uuid = UUID.randomUUID();
        this.name = reg.getName();
        this.name_space = reg.getNameSpace();
        this.load_chunk = reg.loadChunk();
        this.load_chunk_radius = reg.getLoadRadius();
        if (reg.getMachineConst() != null) {
            try {
                this.hooked_machine = (Machine) reg.getMachineConst().newInstance(this);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                Creatio.intern("&4Exception in constructing machine &6&l" + reg.getMachineConst().getName() + "&4!");
                e.printStackTrace();
            }
        }


        if (center.getType() != reg.getCenter()) {
            delete();
            throw new StructureConstructionException("Structure construction failed! Mismatching center material: " + center.getType() + ", expected: " + reg.getCenter(), StructureConstructionException.DisabledReason.CENTER_MISMATCH);
        }

        Integer[] cor = reg.getCorner();

        //Get corner blocks
        this.core = LocationUtil.setDirection(center.getLocation(), facing);
        this.corner = LocationUtil.localBlockCoords(this.core, (double) cor[0], (double) cor[1], (double) cor[2]);
        //System.out.println(cor[0] +" " + cor[1] +" "+ cor[2] +" - "+this.core + this.corner + this.center);

        //Check structure completion
        int mismatch = checkStructure(true, false, true);

        if (mismatch > 0 && reg.flippable()) {
            this.corner = LocationUtil.localBlockCoords(this.core, ((double) cor[0]) * -1, (double) cor[1], (double) cor[2]);
            this.flipped = true;
            this.block_list.clear();
            mismatch = checkStructure(true, true, true);
        }

        if (mismatch > 0) {
            //System.out.println("DEBUG: STRUCTURE CONSRUCTING FAILED: "+ mismatch);
            delete();
            throw new StructureConstructionException("Structure construction failed! Mismatch block count: " + mismatch, StructureConstructionException.DisabledReason.MISMATCH);
        }

        //Check for structure overlap
        if (StructureManager.getMultiblockStructures() != null && !reg.overlappable()) {
            for (MultiblockStructure m : StructureManager.getMultiblockStructures()) {
                if (m == this) continue;
                if (checkCorner(this.corner, this.edcorner, m.corner, m.edcorner)) {
                    delete();
                    throw new StructureConstructionException("Structure construction failed! Overlap with structure " + m.getUUID(), StructureConstructionException.DisabledReason.OVERLAPPED);
                }
            }
        }

        //Call Multiblock structure create event
        MultiblockAPIStructureCreateEvent e = new MultiblockAPIStructureCreateEvent(this);
        Bukkit.getServer().getPluginManager().callEvent(e);
        if (e.isCancelled()) {
            delete();
            throw new StructureConstructionException("Structure construction failed! Event cancelled." + mismatch, StructureConstructionException.DisabledReason.EVENT_CANCELLED);
        }

        //Load Chunks
        if (this.load_chunk) loadChunks(true);

        assert StructureManager.getMultiblockStructures() != null;
        StructureManager.getMultiblockStructures().add(this);
    }


    public MultiblockStructure(UUID uuid, String name_space, boolean flipped, BlockFace facing, Location corner, Location edcorner,
                               Location core, boolean enabled, String name, boolean load_chunk, int load_chunk_radius,
                               Map<String, Object> machine_data) {
        //Find hooked registered structure
        RegisteredStructure reg = StructureManager.getRegisteredStructures().get(name_space);
        if (reg != null) {
            if (reg.getMachineConst() != null) {
                try {
                    this.hooked_machine = (Machine) reg.getMachineConst().newInstance(this);
                    this.hooked_machine.deserialize(machine_data);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException ee) {
                    Creatio.intern("&4Exception in constructing machine &6&l" + reg.getMachineConst().getName() + "&4!");
                    ee.printStackTrace();
                }
            }
        } else {
            delete();
            throw new StructureConstructionException("Structure deserialization failed! No registered structure named " + name_space + "found!", StructureConstructionException.DisabledReason.NAMESPACE_NOT_EXIST);
        }
        this.hooked_registered = reg;

        //Normal variables set
        this.uuid = uuid;
        this.name_space = name_space;
        this.flipped = flipped;
        this.facing = facing;
        this.corner = corner;
        this.edcorner = edcorner;
        this.core = core;
        this.enabled = enabled;
        this.name = name;
        this.load_chunk = load_chunk;
        this.load_chunk_radius = load_chunk_radius;

        //Check for structure overlap
        if (StructureManager.getMultiblockStructures() != null && !hooked_registered.overlappable()) {
            for (MultiblockStructure m : StructureManager.getMultiblockStructures()) {
                if (m == this) continue;
                if (checkCorner(this.corner, this.edcorner, m.corner, m.edcorner)) {
                    delete();
                    throw new StructureConstructionException("Structure deserialization failed! No registered structure named " + name_space + "found!", StructureConstructionException.DisabledReason.NAMESPACE_NOT_EXIST);
                }
            }
        }

        //Check for center material
        if (this.core.getBlock().getType() != reg.getCenter()) {
            delete();
            throw new StructureConstructionException("Structure deserialization failed! Mismatching center material: " + this.core.getBlock().getType(), StructureConstructionException.DisabledReason.CENTER_MISMATCH);
        }

        //Structure completion check
        if (reg.unstable()) {
            int mismatch = checkStructure(false, this.flipped, true);

            if (mismatch > 0) {
                //System.out.println("DEBUG: STRUCTURE CONSRUCTING FAILED: "+ mismatch);
                delete();
                throw new StructureConstructionException("Structure deserialization failed! Mismatch block count: " + mismatch, StructureConstructionException.DisabledReason.MISMATCH);
            }
        }

        //Load Chunks
        if (this.load_chunk) loadChunks(true);

        assert StructureManager.getMultiblockStructures() != null;
        StructureManager.getMultiblockStructures().add(this);
    }


    private void checkLimitValidation(Block center) throws StructureConstructionException {
        //Server structure limit
        if (StructureManager.getMultiblockStructures().size() >= Creatio.getInstance().maxStructureInServer()) {
            delete();
            throw new StructureConstructionException("Reach server structure limits! Max: " + Creatio.getInstance().maxStructureInServer(), StructureConstructionException.DisabledReason.REACH_WORLD_LIMIT);
        }

        //radius structure limit check
        int count = 0;
        for (MultiblockStructure m : StructureManager.getMultiblockStructures()) {
            if (center.getLocation().distance(m.getCore().getLocation()) <= Creatio.getInstance().structureCheckRadius()) {
                count++;
            }
        }
        if (count >= Creatio.getInstance().maxStructureInRadius()) {
            delete();
            throw new StructureConstructionException("Reach radius structure limits! Maximum" + Creatio.getInstance().maxStructureInRadius() + " structures in " + Creatio.getInstance().structureCheckRadius() + "blocks radius", StructureConstructionException.DisabledReason.REACH_RANGE_LIMIT);
        }
    }


    //Forceload chunks
    public void loadChunks(boolean setlist) {
        if (setlist) {
            this.chunk.clear();
            for (int rad = 0; rad < 360; rad += 2) {
                Location loop = this.core.clone();
                for (int i = 5; i < load_chunk_radius; i += 5) {
                    loop.add(Mth.cos(rad) * 5, 0, Mth.sin(rad * 5));
                    this.chunk.add(loop.getChunk());
                    loop.getChunk().setForceLoaded(true);
                }
            }
        } else {
            for (Chunk c : this.chunk) {
                c.setForceLoaded(true);
            }
        }
    }


    //Unforceload chunks
    public void releaseChunks(boolean accurate) {
        for (Chunk c : this.chunk) {
            if (accurate) {
                for (MultiblockStructure m : StructureManager.getMultiblockStructures()) {
                    if (m != this) {
                        if (!m.getForceChunks().contains(c)) {
                            c.setForceLoaded(false);
                        }
                    }
                }
            } else {
                c.setForceLoaded(false);
            }
        }
    }


    private static boolean checkCorner(Location targetLoc1, Location targetLoc2, Location area1, Location area2) {
        double[] x0 = {targetLoc1.getX(), targetLoc2.getX()},
                y0 = {targetLoc1.getY(), targetLoc2.getY()},
                z0 = {targetLoc1.getZ(), targetLoc2.getZ()};
        for (double x : x0) {
            for (double y : y0) {
                for (double z : z0) {
                    if (LocationUtil.inArea(new Location(targetLoc1.getWorld(), x, y, z), area1, area2)) return true;
                }
            }
        }
        return false;
    }

    //The first time to check the structure and create BlockList.
    private int checkStructure(boolean updatecorner, boolean isFlipped, boolean createBlockList) {
        int k = 0, k_layer = 0, k_line = 0, k_dot = 0;
        int delta = (isFlipped) ? -1 : 1;
        Block looper = this.corner.getBlock();
        for (List<Object[][]> layer : this.hooked_registered.getPattern()) {
            k_line = 0;
            for (Object[][] line : layer) {
                for (k_dot = 0; k_dot < line.length; k_dot++) {
                    Object[] dot = line[k_dot];
                    if (dot != null) {
                        if (!LocationUtil.directionalBlockDataCheck(LocationUtil.localBlockGet(looper,this.facing,k_dot * delta,k_layer,k_line).getBlockData(), dot, this.facing, isFlipped)) {
                            k++;
                            //System.out.println("MISMATCH: @ " + Utils.LocalBlockGet(looper,this.facing,k_dot,k_layer,k_line).getLocation());
                            //System.out.println("  EXPECTED " +dot[0]);
                        } else if (createBlockList) {
                            this.block_list.add(LocationUtil.localBlockGet(looper,this.facing,k_dot * delta,k_layer,k_line));
                        }
                    }
                }
                k_line++;
            }
            k_layer++;
        }
        if (updatecorner) this.edcorner = LocationUtil.localBlockCoords(
                this.corner,((double) k_dot - 1) * delta, (double) k_layer - 1, (double) k_line - 1);
        return k;
    }

    //A scan of structure from BlockList, this should be much faster than checkStructure.
    private int scanStructure() {
        int k = 0;
        int delta = this.flipped ? -1 : 1;
        Block looper = this.corner.getBlock();
        for (int k_layer = 0; k_layer < this.hooked_registered.getPattern().size(); k_layer++) {
            for (int k_line = 0; k_line < this.hooked_registered.getPattern().get(0).size(); k_line++) {
                for (int k_dot = 0; k_dot < this.hooked_registered.getPattern().get(0).get(0).length; k_dot++) {
                    if (!this.block_list.contains(LocationUtil.localBlockGet(looper,this.facing,k_dot * delta,k_layer,k_line))) {
                        //System.out.println("DMISMATCH: @ " + Utils.LocalBlockGet(looper,this.facing,k_dot * delta,k_layer,k_line).getLocation());
                        k++;
                    }
                }
            }
        }
        return k;
    }


    //Serialization Section
    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>(), map1 = new HashMap<>();
        map.put("name_space", this.name_space);
        map.put("flipped", this.flipped);
        map.put("facing", this.facing.name());
        map.put("corner", this.corner.serialize());
        map.put("edcorner", this.edcorner.serialize());
        map.put("core", this.core.serialize());
        map.put("enabled", this.enabled);

        map.put("name", this.name);
        map.put("load_chunk", this.load_chunk);
        map.put("load_chunk_radius", this.load_chunk_radius);
        map.put("machine_data", (this.hooked_machine != null) ? this.getHookedMachine().serialize() : null);
        map1.put("id", this.uuid.toString());
        map1.put("data", map);
        return map1;
    }

    public static MultiblockStructure deserialize(Map<String, Object> map1) {
        return deserialize(map1, UUID.randomUUID());
    }

    public static MultiblockStructure deserialize(Map<String, Object> map1, UUID uuid) {
        Map<String, Object> map = (Map<String, Object>) map1.get("data");
        MultiblockStructure m = new MultiblockStructure(
                uuid,
                (map.get("name_space") != null ? (String) map.get("name_space") : null),
                (map.get("flipped") != null ? (Boolean) map.get("flipped") : false),
                (map.get("facing") != null ? BlockFace.valueOf(map.get("facing").toString()) : BlockFace.NORTH),
                (map.get("corner") != null ? Location.deserialize((Map<String, Object>) map.get("corner")) : null),
                (map.get("edcorner") != null ? Location.deserialize((Map<String, Object>) map.get("edcorner")) : null),
                (map.get("core") != null ? Location.deserialize((Map<String, Object>) map.get("core")) : null),
                (map.get("enabled") != null ? (Boolean) map.get("enabled") : false),

                (map.get("name") != null ? (String) map.get("name") : "Noname"),
                (map.get("load_chunk") != null ? (Boolean) map.get("load_chunk") : false),
                (map.get("load_chunk_radius") != null ? (Integer) map.get("load_chunk_radius") : 16),
                (map.get("machine_data") != null ? (Map<String, Object>) map.get("machine_data") : new HashMap<>())
        );
        m.uuid = UUID.fromString((String) map1.get("id"));
        return m;
    }

    //delete this object and it's linked machine
    public void delete() {
        deleteStructure(null);
    }

    public void deleteStructure(Event event) {
        if (this.hooked_machine != null) {
            this.hooked_machine.onDestroy(event);
            StructureManager.getMachines().remove(this.hooked_machine);
            this.hooked_machine = null;
        }
        releaseChunks(true);
        StructureManager.getMultiblockStructures().remove(this);
    }


    public boolean isEnabled() {
        return this.enabled;
    }
    public void setEnabled(boolean a) {
        this.enabled = a;
        if (a) {
            this.hooked_machine.Enable();
        } else {
            this.hooked_machine.Disable();
        }
    }


    public UUID getUUID() {
        return this.uuid;
    }

    public String getNameSpace() {
        return this.name_space;
    }


    public String getName() {
        return this.name;
    }
    public void setName(String a) {
        this.name = a;
    }


    public Material getCenter() {
        return this.hooked_registered.getCenter();
    }


    public boolean isUnstable() {
        return this.hooked_registered.unstable();
    }

    public boolean isFlipped() {
        return this.flipped;
    }


    public boolean loadChunk() {
        return this.load_chunk;
    }
    public void setLoadChunk(boolean a) {
        this.load_chunk = a;
    }


    public int getLoadRadius() {
        return this.load_chunk_radius;
    }
    public void setLoadRadius(int a) {
        this.load_chunk_radius = a;
    }


    public Block getCorner() {
        return this.corner.getBlock();
    }


    public Block getEndCorner() {
        return this.edcorner.getBlock();
    }


    public Block getCore() {
        return this.core.getBlock();
    }

    public Machine getHookedMachine() {
        return this.hooked_machine;
    }
    public void setHookedMachine(Machine m) {
        this.hooked_machine = m;
    }

    public List<Chunk> getForceChunks() {
        return this.chunk;
    }


    public BlockFace getFacing() {
        return this.facing;
    }


    public int getStructureMismatch() {
        return scanStructure();
    }


    public boolean isTargetBy(Player p, int radius, FluidCollisionMode mode) {
        Block t = p.getTargetBlockExact(radius, mode);
        if (t != null) return LocationUtil.inArea(t.getLocation(), this.corner, this.edcorner);
        return false;
    }

    public RegisteredStructure getHookedRegistered() {
        return hooked_registered;
    }

    public List<Block> getBlockList() {
        return this.block_list;
    }


    public String getString() {
        return "MultiblockAPIStructure{name_space=" + this.name_space + "world=" + this.core.getWorld()+ ",x=" + this.core.getX() + ",y=" + this.core.getY() + ",z=" + this.core.getZ() + '}';
    }

    @Override
    public int hashCode() {
        int code = 0;
        code += 31 * uuid.hashCode();
        code += 31 * hooked_registered.hashCode();
        code += 31 * name_space.hashCode();
        code += 31 * core.hashCode();
        code += 31 * block_list.hashCode();
        return code;
    }
}
