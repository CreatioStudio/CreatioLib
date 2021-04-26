package vip.creatio.clib.multiblock.listener;

import vip.creatio.clib.multiblock.event.MultiblockAPIStructureDestoryEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MultiblockAPIStructureDestroy implements Listener {

    @EventHandler
    public void onStructureDestroy(MultiblockAPIStructureDestoryEvent event) {

        //Do sth.
    }
}
