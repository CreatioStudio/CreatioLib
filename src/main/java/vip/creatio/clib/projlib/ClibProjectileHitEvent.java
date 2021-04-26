package vip.creatio.clib.projlib;

import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.jetbrains.annotations.NotNull;

public class ClibProjectileHitEvent extends EntityEvent implements Cancellable, ClibProjectileEvent {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    Projectile projectile;

    public ClibProjectileHitEvent(@NotNull final Entity what, @NotNull final Projectile proj) {
        super(what);
        projectile = proj;
        cancelled = false;
    }

    public Projectile getProjectile() {
        return projectile;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @NotNull @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
