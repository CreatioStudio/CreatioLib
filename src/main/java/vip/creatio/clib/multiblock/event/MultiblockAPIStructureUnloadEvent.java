package vip.creatio.clib.multiblock.event;

import vip.creatio.clib.multiblock.MultiblockStructure;
import org.bukkit.Chunk;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class MultiblockAPIStructureUnloadEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private MultiblockStructure m;
    private Chunk chunk;

    public MultiblockAPIStructureUnloadEvent(MultiblockStructure m, Chunk chunk) {
        this.m = m;
        this.chunk = chunk;
    }

    public MultiblockStructure getMultiblockStructure() {
        return this.m;
    }

    public Chunk getChunk() {
        return this.chunk;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
