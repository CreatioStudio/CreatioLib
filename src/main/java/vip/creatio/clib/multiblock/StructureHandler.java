package vip.creatio.clib.multiblock;

import vip.creatio.basic.tools.Listener;
import vip.creatio.basic.tools.Task;
import vip.creatio.clib.modules.util.LocationUtil;
import vip.creatio.clib.multiblock.event.MultiblockAPIStructureDestoryEvent;
import vip.creatio.clib.multiblock.event.MultiblockAPIStructureInteractEvent;
import vip.creatio.clib.multiblock.event.MultiblockAPIStructureUnloadEvent;
import vip.creatio.clib.multiblock.machine.MachineClickable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import vip.creatio.clib.Creatio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StructureHandler extends StructureManager {

    //Multiblock Structure ticker.
    @Task(period = 50)
    static void tickStructure1t() {
        if (getMultiblockStructures().size() > 0) {
            for (MultiblockStructure m : getMultiblockStructures()) {
                //multiblock structure do




                //active structure do
                if (getActiveStructures().contains(m)) {
                    List<Location> collection = LocationUtil.drawCube(m.getCorner().getLocation(), m.getEndCorner().getLocation(), 0.5f, true);

                    for (Location l : collection) {
                        if (l.getWorld() != null) l.getWorld().spawnParticle(Particle.FLAME, l.getX(), l.getY(), l.getZ(), 2, 0, 0, 0, 0);
                    }

                    //tick machine
                    if (m.getHookedMachine() != null) {
                        m.getHookedMachine().Tick();
                    }
                }
            }
        }
    }

    @Task(period = 3000)
    public static void tickStructure3s() {
        getActiveStructures().clear();
        for (MultiblockStructure m : getMultiblockStructures()) {
            if (getActiveStructures().size() >= Creatio.getInstance().maxActiveStructure()) break;
            if (m.getHookedRegistered().requireUpdate() && m.isEnabled()) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getWorld() == m.getCore().getWorld()) {
                        if (m.getCore().getLocation().distance(p.getLocation()) <= Creatio.getInstance().structureCheckRadius()) {
                            getActiveStructures().add(m);
                        }
                    }
                }
            }
        }
    }


    public static Boolean ifStructureChanged(MultiblockStructure m, Block b) {
        Location loc = b.getLocation();
        if (loc.distance(m.getCore().getLocation()) <= Creatio.getInstance().structureCheckRadius()) {
            if (b.equals(m.getCore())) {
                return true;
            }
            if (m.isUnstable() && LocationUtil.inArea(loc, m.getCorner().getLocation(), m.getEndCorner().getLocation())) {
                return m.getBlockList().contains(b);
            }
        }
        return false;

    }

    //Implements of on Structure Destroy Event - player break.
    @Listener
    static void onBlockBreakStructureCheck(BlockBreakEvent event) {
        List<MultiblockStructure> l = new ArrayList<>();

        for (MultiblockStructure m : getMultiblockStructures()) {
            if (ifStructureChanged(m, event.getBlock())) {
                MultiblockAPIStructureDestoryEvent e = new MultiblockAPIStructureDestoryEvent(Collections.singletonList(event.getBlock()), event.getPlayer(), m);
                Bukkit.getServer().getPluginManager().callEvent(e);
                if (e.isCancelled()) {
                    event.setCancelled(true);
                    return;
                }
                l.add(m);
            }
        }
        for (MultiblockStructure m : l) {
            m.deleteStructure(event);
        }
    }

    //Implements of on Structure Destroy Event - piston extend.
    @Listener
    static void onPistonExtendStructureCheck(BlockPistonExtendEvent event) {
        for (MultiblockStructure m : getMultiblockStructures()) {
            for (Block b : event.getBlocks()) {
                if (ifStructureChanged(m, b)) {event.setCancelled(true);}
            }
        }
    }

    //Implements of on Structure Destroy Event - piston retract.
    @Listener
    static void onPistonRetractStructureCheck(BlockPistonRetractEvent event) {
        for (MultiblockStructure m : getMultiblockStructures()) {
            for (Block b : event.getBlocks()) {
                if (ifStructureChanged(m, b)) {event.setCancelled(true);}
            }
        }
    }

    //Implements of on Structure Destroy Event - block explode.
    @Listener
    static void onBlockExplodeStructureCheck(BlockExplodeEvent event) {
        List<MultiblockStructure> l = new ArrayList<>();

        for (MultiblockStructure m : getMultiblockStructures()) {
            if (event.getBlock().getLocation().distance(m.getCore().getLocation()) <= Creatio.getInstance().structureCheckRadius()) {
                for (Block b : event.blockList()) {
                    if (ifStructureChanged(m, b)) {
                        MultiblockAPIStructureDestoryEvent e = new MultiblockAPIStructureDestoryEvent(event.blockList(), null, m);
                        Bukkit.getServer().getPluginManager().callEvent(e);
                        if (e.isCancelled()) {
                            event.setCancelled(true);
                            return;
                        }
                        l.add(m);
                    }
                }
            }
        }
        for (MultiblockStructure m : l) {
            m.deleteStructure(event);
        }
    }

    //Implements of on Structure Destroy Event - entity explode.
    @Listener
    static void onEntityExplodeStructureCheck(EntityExplodeEvent event) {
        List<MultiblockStructure> l = new ArrayList<>();

        for (MultiblockStructure m : getMultiblockStructures()) {
            if (event.getEntity().getLocation().distance(m.getCore().getLocation()) <= Creatio.getInstance().structureCheckRadius()) {
                for (Block b : event.blockList()) {
                    if (ifStructureChanged(m, b)) {
                        MultiblockAPIStructureDestoryEvent e = new MultiblockAPIStructureDestoryEvent(event.blockList(), event.getEntity(), m);
                        Bukkit.getServer().getPluginManager().callEvent(e);
                        if (e.isCancelled()) {
                            event.setCancelled(true);
                            return;
                        }
                        l.add(m);
                    }
                }
            }
        }
        for (MultiblockStructure m : l) {
            m.deleteStructure(event);
        }
    }

    //Implements of on Structure Destroy Event.
    @Listener
    static void onBlockPlaceStructureCheck(BlockPlaceEvent event) {
        List<MultiblockStructure> l = new ArrayList<>();

        for (MultiblockStructure m : getMultiblockStructures()) {
            if (ifStructureChanged(m, event.getBlock())) {
                MultiblockAPIStructureDestoryEvent e = new MultiblockAPIStructureDestoryEvent(Collections.singletonList(event.getBlock()), event.getPlayer(), m);
                Bukkit.getServer().getPluginManager().callEvent(e);
                if (e.isCancelled()) {
                    event.setCancelled(true);
                    return;
                }
                l.add(m);
            }
        }
        for (MultiblockStructure m : l) {
            m.deleteStructure(event);
        }
    }

    //Check if a structure is unloaded
    @Listener
    static void onChunkUnloadCheck(ChunkUnloadEvent event) {
        List<MultiblockStructure> l = new ArrayList<>();

        for (MultiblockStructure m : getActiveStructures()) {
            if (m.getCore().getChunk() == event.getChunk()) {
                MultiblockAPIStructureUnloadEvent e = new MultiblockAPIStructureUnloadEvent(m, event.getChunk());
                Bukkit.getServer().getPluginManager().callEvent(e);
                l.add(m);
            }
        }
        getActiveStructures().removeAll(l);
    }

    //Implements of on Structure Click Event.
    @Listener
    static void onPlayerInteractCheck(PlayerInteractEvent event) {
        for (MultiblockStructure m : getActiveStructures()) {
            if (event.hasBlock()) {
                if (m.getBlockList().contains(event.getClickedBlock())) {
                    MultiblockAPIStructureInteractEvent e;
                    if (m.getCore() == event.getClickedBlock()) {
                        e = new MultiblockAPIStructureInteractEvent(event.getPlayer(), event.getItem(), event.getAction(), event.getClickedBlock(), m, true);
                    } else {
                        e = new MultiblockAPIStructureInteractEvent(event.getPlayer(), event.getItem(), event.getAction(), event.getClickedBlock(), m, false);
                    }
                    Bukkit.getServer().getPluginManager().callEvent(e);

                    if (e.isCancelled()) {
                        event.setCancelled(true);
                        return;
                    }

                    if (m.getHookedMachine() != null) {
                        if (m.getHookedMachine() instanceof MachineClickable) {
                            ((MachineClickable) m.getHookedMachine()).onClick(event);
                        }
                    }
                }
            }
        }
    }


}
