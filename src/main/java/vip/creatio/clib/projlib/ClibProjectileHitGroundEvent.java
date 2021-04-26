package vip.creatio.clib.projlib;

import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;
import org.jetbrains.annotations.NotNull;

public class ClibProjectileHitGroundEvent extends BlockEvent implements Cancellable, ClibProjectileEvent {

    private static final HandlerList handlers = new HandlerList();
    Projectile projectile;
    boolean cancelled = false;

    public ClibProjectileHitGroundEvent(@NotNull final Block theBlock, @NotNull final Projectile proj) {
        super(theBlock);
        projectile = proj;
    }

    public Projectile getProjectile() {
        return projectile;
    }

    @NotNull @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }
}
