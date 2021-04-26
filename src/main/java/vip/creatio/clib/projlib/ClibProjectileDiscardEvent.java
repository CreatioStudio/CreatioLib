package vip.creatio.clib.projlib;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ClibProjectileDiscardEvent extends Event implements ClibProjectileEvent {

    private static final HandlerList handlers = new HandlerList();
    Projectile projectile;

    public ClibProjectileDiscardEvent(@NotNull final Projectile proj) {
        projectile = proj;
    }

    public Projectile getProjectile() {
        return projectile;
    }

    @NotNull @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
