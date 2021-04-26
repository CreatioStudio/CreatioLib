package vip.creatio.clib.multiblock.machine;

import vip.creatio.clib.multiblock.MultiblockStructure;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public abstract class Machine implements ConfigurationSerializable {

    public abstract void Enable();

    public abstract void Tick();

    public abstract void Disable();

    public abstract void onDestroy(Event event);

    public abstract String getNameSpace();

    public abstract @NotNull Map<String, Object> serialize();

    public abstract void deserialize(Map<String, Object> map);

    public abstract MultiblockStructure getHookedStructure();

}
