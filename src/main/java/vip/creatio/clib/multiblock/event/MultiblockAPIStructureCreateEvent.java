package vip.creatio.clib.multiblock.event;

import vip.creatio.clib.multiblock.MultiblockStructure;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class MultiblockAPIStructureCreateEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private MultiblockStructure m;
    private boolean cancelled;

    public MultiblockAPIStructureCreateEvent(MultiblockStructure m) {
        this.m = m;
    }


    public MultiblockStructure getMultiblockStructure() {
        return this.m;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
