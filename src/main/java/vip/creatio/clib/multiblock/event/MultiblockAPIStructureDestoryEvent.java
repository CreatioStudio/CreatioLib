package vip.creatio.clib.multiblock.event;

import vip.creatio.clib.multiblock.MultiblockStructure;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;

public final class MultiblockAPIStructureDestoryEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private List<Block> block;
    private Entity entity;
    private MultiblockStructure m;
    private boolean cancelled;

    public MultiblockAPIStructureDestoryEvent(List<Block> block, Entity entity, MultiblockStructure m) {
        this.block = block;
        this.entity = entity;
        this.m = m;
    }

    public List<Block> getBlock() {
        return this.block;
    }

    public Entity getPlayer() {
        return this.entity;
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
