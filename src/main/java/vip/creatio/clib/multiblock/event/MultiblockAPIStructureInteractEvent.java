package vip.creatio.clib.multiblock.event;

import vip.creatio.clib.multiblock.MultiblockStructure;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

public final class MultiblockAPIStructureInteractEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private Block block;
    private ItemStack item;
    private Action action;
    private Boolean iscore;
    private MultiblockStructure m;
    private boolean cancelled;

    public MultiblockAPIStructureInteractEvent(Player player, ItemStack item, Action act, Block block, MultiblockStructure m, Boolean iscore) {
        this.player = player;
        this.block = block;
        this.item = item;
        this.action = act;
        this.m = m;
        this.iscore = iscore;
    }

    public Block getBlock() {
        return this.block;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Action getAction() {
        return this.action;
    }

    public ItemStack getItem() {
        return this.item;
    }

    public Boolean isCoreEvent() {
        return this.iscore;
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
