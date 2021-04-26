package vip.creatio.clib.projlib;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;

public abstract class EffectUnit {

    // Sync with Minecraft, 20 times / second
    public abstract void tick();

    public abstract Vector getCenterOffset();

    public abstract List<Player> visibleTo();



}
