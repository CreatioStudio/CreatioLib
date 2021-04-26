package vip.creatio.clib.multiblock.listener;

import vip.creatio.clib.multiblock.event.MultiblockAPIStructureUnloadEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MultiblockAPIStructureUnload implements Listener {

    @EventHandler
    public void onStructureUnload(MultiblockAPIStructureUnloadEvent event) {
        System.out.println("Yes, this is unloaded!!!!!!!");
    }
}
