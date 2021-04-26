package vip.creatio.clib.command;

import vip.creatio.clib.Creatio;
import vip.creatio.accessor.Func;
import vip.creatio.accessor.Reflection;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CommandRegister {

    private static final SimpleCommandMap COMMAND_MAP = ((CraftServer) Bukkit.getServer()).getCommandMap();

    private final Map<String, Command> REGISTERED_COMMANDS = new HashMap<>();
    private final String FALLBACK_PREFIX;

    public static final List<String> COMPLETER_DIRECTIONS_4 = Collections.unmodifiableList(Arrays.asList("south", "north", "west", "east"));
    public static final List<String> COMPLETER_DIRECTIONS_6 = Collections.unmodifiableList(Arrays.asList("south", "north", "west", "east", "up", "down"));
    public static final List<String> COMPLETER_SMALL_INT = Collections.unmodifiableList(Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10"));
    public static final List<String> COMPLETER_EMPTY = Collections.unmodifiableList(new ArrayList<>());

    public CommandRegister(String FALLBACK_PREFIX) {
        this.FALLBACK_PREFIX = FALLBACK_PREFIX;
    }

    //Command register
    public void init() {
        register(Main.register());
        register(InGameTest.register());
    }

    public void register(Command cmd) {
        COMMAND_MAP.register(cmd.getName(), FALLBACK_PREFIX, cmd);
        REGISTERED_COMMANDS.put(cmd.getName(), cmd);
    }

    public void unregister(Command cmd) {
        unregister(cmd.getName());
    }

    public void registerAll(Collection<Command> commands) {
        for (Command c : commands) {
            register(c);
        }
    }

    public void unregister(String lable) {
        Command cmd = REGISTERED_COMMANDS.get(lable);
        cmd.unregister(COMMAND_MAP);
        REGISTERED_COMMANDS.remove(lable);
    }

    public void clearCommands() {
        for (String key : REGISTERED_COMMANDS.keySet()) {
            unregister(key);
        }
    }

    public Command getCommand(String name) {
        return REGISTERED_COMMANDS.get(name);
    }

    public Collection<Command> getCommands() {
        return Collections.unmodifiableCollection(REGISTERED_COMMANDS.values());
    }

    public static SimpleCommandMap getCommandMap() {
        return COMMAND_MAP;
    }

    private static final Func<PluginCommand> PLUGIN_COMMAND = Reflection.constructor(PluginCommand.class, String.class, Plugin.class);
    public static PluginCommand create(@NotNull String name) {
        return PLUGIN_COMMAND.invoke(name, Creatio.getBootstrap());
    }
}
