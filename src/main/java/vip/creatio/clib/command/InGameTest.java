package vip.creatio.clib.command;

import net.minecraft.server.EntityPlayer;
import org.bukkit.command.*;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vip.creatio.clib.Creatio;

import java.util.ArrayList;
import java.util.List;

public final class InGameTest implements CommandBuilder {

    private static final String name = "creatiotest";
    private static final String description = "Test command of CreatioLib.";
    private static final List<String> aliases = new ArrayList<>();

    //No default constructor
    private InGameTest() {}

    public static PluginCommand register() {
        PluginCommand cmd = CommandRegister.create(name);
        cmd.setDescription(description);
        cmd.setAliases(aliases);
        cmd.setPermission(null);
        cmd.setPermissionMessage(null);
        cmd.setExecutor(new Executor());
        cmd.setTabCompleter(new Completer());
        return cmd;
    }

    private static class Executor implements CommandExecutor {

        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
            if (!sender.hasPermission("creatio.admin")) {
                Creatio.getSender().sendStatic(sender, "MAIN.NO_PERM");
            }

            System.out.println("Now getting 100_00_000 times entity pos X");
            if (args[0].equals("direct")) {
                System.out.println("Using direct calling...");
                long t0 = System.nanoTime();
                double result = 0d;
                EntityPlayer player = ((CraftPlayer) sender).getHandle();
                for (int i = 0; i < 100_000; i++) {
                    result = player.locX();
                }
                System.out.println("Time used: " + (System.nanoTime() - t0) / 1000000 + "ms");
                System.out.println("Result: " + result);
            }

            System.out.println("Command Executed!");
            return true;
        }
    }

    private static String connector(int from, String... args) {
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < args.length; i++) {
            sb.append(args[i]).append(' ');
        }
        return sb.deleteCharAt(sb.length() - 1).toString();
    }

    private static class Completer implements TabCompleter {

        @Override
        public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
            return null;
        }
    }

}
