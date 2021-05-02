package vip.creatio.clib.command;

import vip.creatio.basic.tools.FormatMsgManager;
import vip.creatio.clib.modules.exception.StructureConstructionException;
import vip.creatio.basic.packet.Packet;
import vip.creatio.clib.multiblock.MultiblockStructure;
import vip.creatio.clib.multiblock.RegisteredStructure;
import vip.creatio.clib.multiblock.StructureManager;
import vip.creatio.basic.util.ItemUtil;
import vip.creatio.basic.chat.ClickEvent;
import vip.creatio.basic.chat.Component;
import vip.creatio.basic.chat.HoverEvent;
import vip.creatio.basic.nbt.CompoundTag;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vip.creatio.clib.Creatio;

import java.io.File;
import java.util.*;
import java.util.function.Predicate;

import static vip.creatio.common.util.ArrayUtil.get;

@SuppressWarnings("unused")
public final class Main implements CommandBuilder {

    private static final String name = "creatiolib";
    private static final String description = "Main command of CreatioLib.";
    private static final List<String> aliases = Arrays.asList("creatio", "clib");
    
    private static final FormatMsgManager msg = Creatio.getSender();

    private static final String[]
        TC = new String[]{"structures", "reload"},
            TC_STR = new String[]{"list", "create", "remove", "save", "info", "register"},
                TC_STR_LIST = new String[]{"registered", "multiblock", "active", "machine", "registered_machine"},
                TC_STR_REG = new String[]{"tool", "create"},
                TC_STR_REG_CFM = new String[]{"-flippable", "-stable", "-static", "-overlappable", "-name", "-hooked_machine"},
                TC_STR_INFO = new String[]{"registered", "multiblock"},
            TC_RLD = new String[]{"structures", "all"};
    ;

    private static final ItemStack STRUCTURE_SELECTOR;
    private static final Map<Player, Block[]> STRUCTURE_SELECTION_SET = new HashMap<>();
    private static final Map<Player, List<Packet<?>>> SELECTION_PARTICLE_BUFFER = new HashMap<>();
    private static final Map<Player, Long> COOLDOWN = new HashMap<>();

    static {
        STRUCTURE_SELECTOR = ItemUtil.fromNBT(Material.IRON_AXE, 1, new CompoundTag(
                "{display:{Name:'[{\"text\":\"Structure Selector\",\"color\":\"gold\",\"bold\":true" +
                ",\"italic\":false}]',Lore:['{\"text\":\"Shift + Left Click to select Center Block\",\"color\":\"yellow\"}'" +
                ",'{\"text\":\"Left Click to select the first Corner\",\"color\":\"yellow\"}'" +
                ",'{\"text\":\"Right Click to select the second Corner\",\"color\":\"yellow\"}']}" +
                ",CustomItem:\"structure_selector\",CustomModelData:-1}"));
    }

    //No default constructor
    private Main() {}

    public static PluginCommand register() {
        PluginCommand cmd = CommandRegister.create(name);
        cmd.setDescription(description);
        cmd.setAliases(aliases);
        cmd.setUsage("");
        cmd.setPermission(null);
        cmd.setPermissionMessage(null);
        cmd.setExecutor(new Executor());
        cmd.setTabCompleter(new Completer());
        return cmd;
    }

//    @Listener
//    static void onPlayerInteractEvent(PlayerInteractEvent e) {
//        if (e.hasBlock() && e.getPlayer().isOp()) {
//            String tag = ItemManager.handItemTag(e.getPlayer());
//            if (tag != null) {
//                if (tag.equals("structure_selector")) {
//                    e.setCancelled(true);
//                    COOLDOWN.putIfAbsent(e.getPlayer(), 0L);
//                    if (System.currentTimeMillis() - COOLDOWN.get(e.getPlayer()) <= 100) return;
//                    COOLDOWN.put(e.getPlayer(), System.currentTimeMillis());
//                    STRUCTURE_SELECTION_SET.computeIfAbsent(e.getPlayer(), v -> new Block[3]);
//                    if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
//                        updateSelectionBorder(e.getPlayer());
//                        assert e.getClickedBlock() != null;
//                        if (e.getPlayer().isSneaking()) {
//                            STRUCTURE_SELECTION_SET.get(e.getPlayer())[2] = e.getClickedBlock();
//                            sendStatic("MAIN.MULTIBLOCK_API.REGISTER.SELECTED", e.getPlayer(),
//                                    "Center", "[" + e.getClickedBlock().getX() + ", " + e.getClickedBlock().getY()
//                                            + ", " + e.getClickedBlock().getZ() + "]");
//                        } else {
//                            STRUCTURE_SELECTION_SET.get(e.getPlayer())[0] = e.getClickedBlock();
//                            sendStatic("MAIN.MULTIBLOCK_API.REGISTER.SELECTED", e.getPlayer(),
//                                    "Corner 1", "[" + e.getClickedBlock().getX() + ", " + e.getClickedBlock().getY()
//                                            + ", " + e.getClickedBlock().getZ() + "]");
//                        }
//                        return;
//                    }
//                    if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
//                        STRUCTURE_SELECTION_SET.get(e.getPlayer())[1] = e.getClickedBlock();
//                        updateSelectionBorder(e.getPlayer());
//                        sendStatic("MAIN.MULTIBLOCK_API.REGISTER.SELECTED", e.getPlayer(),
//                                "Corner 2", "[" + e.getClickedBlock().getX() + ", " + e.getClickedBlock().getY()
//                                        + ", " + e.getClickedBlock().getZ() + "]");
//                    }
//                }
//            }
//        }
//    }

    private static class Executor implements CommandExecutor {

        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
            Player p = sender instanceof Player ? (Player) sender : null;

            if (!sender.hasPermission("creatio.admin")) {
                msg.sendStatic(sender, "MAIN.NO_PERM");
                return true;
            }

            //max index args can have
            int l = args.length - 1;

            //creatio
            if (l >= 0) {

                //creatio reload
                if (args[0].equalsIgnoreCase(TC[1])) {

                    //creatio reload *
                    if (l == 0) {
                        msg.sendStatic(sender, "MAIN.RELOAD.CONFIG");
                        Creatio.getInstance().loadAllConfig();
                        return true;
                    }
                    //creatio reload all
                    else if (args[1].equalsIgnoreCase(TC_RLD[1])) {
                        msg.sendStatic(sender, "MAIN.RELOAD.CONFIG");
                        Creatio.getInstance().loadAllConfig();
                        return true;
                    }
                    //creatio reload structures
                    else if (args[1].equalsIgnoreCase(TC_RLD[0])) {

                        //creatio reload structures *name*
                        if (l >= 2) {
                            for (int i = 2; i < args.length; i++) {
                                File f = new File(Creatio.getBootstrap().getDataFolder(), args[i]);
                                msg.sendStatic(sender, "MAIN.RELOAD.SINGLE", args[i]);
                                if (f.exists()) {

                                } else {
                                    msg.sendStatic(sender, "MAIN.FILE.DOESNT_EXIST", args[i]);
                                }
                            }
                        }
                        msg.sendStatic(sender, "MAIN.RELOAD.STRUCTURE_CONFIG");
                        int[] load_success = StructureManager.loadAll();
                        msg.sendStatic(sender, "MAIN.RELOAD.DONE", Integer.toString(load_success[0]), Integer.toString(load_success[1]));
                        return true;
                    } else {
                        msg.sendStatic(sender, "COMMAND.USAGE.RELOAD");
                        return true;
                    }
                }

                //creatio structures
                else if (args[0].equalsIgnoreCase(TC[0])) {
                    if (l >= 1) {

                        //creatio structures create
                        if (args[1].equalsIgnoreCase(TC_STR[1])) {
                            if (rejectNonPlayer(sender)) {
                                return false;
                            }

                            //creatio structures create *name* *direction*
                            if (l >= 3) {
                                if (CommandRegister.COMPLETER_DIRECTIONS_4.contains(args[3].toLowerCase())) {
                                    Block b = p.getTargetBlockExact(6);
                                    if (b != null) {
                                        MultiblockStructure m;
                                        try {
                                            m = MultiblockStructure.fromName(args[2], b, BlockFace.valueOf(args[3].toUpperCase()));
                                        } catch (StructureConstructionException e) {
                                            msg.sendStatic(sender, "COMMAND.USAGE.STRUCTURES.FAILED", e.reason.name());
                                            return true;
                                        }
                                        msg.sendStatic(sender, "MAIN.MULTIBLOCK_API.CREATE", m.getNameSpace());
                                    } else {
                                        msg.sendStatic(sender, "COMMAND.USAGE.STRUCTURES.NO_TARGET");
                                    }
                                } else {
                                    msg.sendStatic(sender, "MAIN.ERROR.INVALID", args[2]);
                                    return true;
                                }
                            } else {
                                msg.sendStatic(sender, "COMMAND.USAGE.STRUCTURES.CREATE");
                                return true;
                            }
                        }
                        //creatio structures remove
                        else if (args[1].equalsIgnoreCase(TC_STR[2])) {
                            if (rejectNonPlayer(sender)) {
                                return false;
                            }

                            Block b = p.getTargetBlockExact(6);
                            if (b != null) {
                                for (MultiblockStructure m : StructureManager.getMultiblockStructures()) {
                                    if (m.getBlockList().contains(b)) {
                                        m.delete();
                                        msg.sendStatic(sender, "MAIN.MULTIBLOCK_API.REMOVE", m.getName(), m.getNameSpace());
                                        return true;
                                    }
                                }
                            }
                            msg.sendStatic(sender, "COMMAND.USAGE.STRUCTURES.NO_TARGET");
                            return true;
                        }
                        //creatio structures list
                        else if (args[1].equalsIgnoreCase(TC_STR[0])) {

                            //creatio structures list *
                            if (l >= 3) {
                                //creatio structures list *page*
                                int page;
                                try {
                                    page = Integer.parseInt(args[3]);
                                } catch (NumberFormatException e) {
                                    msg.sendStatic(sender, "MAIN.ERROR.NOT_NUM", args[3]);
                                    return true;
                                }

                                //creatio structures list registered
                                if (args[2].equalsIgnoreCase(TC_STR_LIST[0])) {
                                    msg.send(sender, listItems0(
                                            page,
                                            15,
                                            StructureManager.getRegisteredStructures().values(),
                                            msg.fromPath("MAIN.MULTIBLOCK_API.DISPLAY.REGISTERED")[0],
                                            msg.fromPath("MAIN.MULTIBLOCK_API.DISPLAY.REGISTERED")[0],
                                            (index, item) -> Component.of("  §l" + index + ". ")
                                                    .append(Component.of("§e§l<i> §a" + item)
                                                            .onClick(ClickEvent.runCmd("/creatio structures info registered " + item))
                                                            .onHover(HoverEvent.showText(Component.of("§eView structure details")))),
                                            new StandartBottom("/creatio structures list registered ")));
                                }
                                //creatio structures list multiblock
                                if (args[2].equalsIgnoreCase(TC_STR_LIST[1])) {
                                    msg.send(sender, listItems0(
                                            page,
                                            15,
                                            StructureManager.getMultiblockStructures(),
                                            msg.fromPath("MAIN.MULTIBLOCK_API.DISPLAY.MULTIBLOCK")[0],
                                            msg.fromPath("MAIN.MULTIBLOCK_API.DISPLAY.MULTIBLOCK")[0],
                                            (index, item) -> Component.of("  §l" + index + ". ")
                                                    .append(Component.of("§e§l<i> §a" + item.getNameSpace() + " §f- " + item.getName())
                                                            .onClick(ClickEvent.runCmd("/creatio structures info multiblock " + item.getUUID()))
                                                            .onHover(HoverEvent.showText(Component.of("§eView structure details")))),
                                            new StandartBottom("/creatio structures list multiblock ")));
                                }
                                //creatio structures list active
                                if (args[2].equalsIgnoreCase(TC_STR_LIST[2])) {
                                    msg.send(sender, listItems0(
                                            page,
                                            15,
                                            StructureManager.getActiveStructures(),
                                            msg.fromPath("MAIN.MULTIBLOCK_API.DISPLAY.ACTIVE")[0],
                                            msg.fromPath("MAIN.MULTIBLOCK_API.DISPLAY.ACTIVE")[0],
                                            (index, item) -> Component.of("  §l" + index + ". ")
                                                    .append(Component.of("§e§l<i> §a" + item.getNameSpace() + " §f- " + item.getName())
                                                            .onClick(ClickEvent.runCmd("/creatio structures info multiblock " + item.getUUID()))
                                                            .onHover(HoverEvent.showText(Component.of("§eView structure details")))),
                                            new StandartBottom("/creatio structures list active ")));
                                }
                                //creatio structures list machine
                                if (args[2].equalsIgnoreCase(TC_STR_LIST[3])) {
                                    msg.send(sender, listItems0(
                                            page,
                                            15,
                                            StructureManager.getMachines(),
                                            msg.fromPath("MAIN.MULTIBLOCK_API.DISPLAY.MACHINE")[0],
                                            msg.fromPath("MAIN.MULTIBLOCK_API.DISPLAY.MACHINE")[0],
                                            (index, item) -> Component.of("  §l" + index + ". ")
                                                    .append(Component.of("§e§l<i> §a" + item.getNameSpace() + " §f- " + item.getHookedStructure().getName())
                                                            .onClick(ClickEvent.runCmd("/creatio structures info multiblock "
                                                                    + item.getHookedStructure().getUUID()))
                                                            .onHover(HoverEvent.showText(Component.of("§eView structure details")))),
                                            new StandartBottom("/creatio structures list machine ")));
                                }
                                //creatio structures list registered_machine
                                if (args[2].equalsIgnoreCase(TC_STR_LIST[4])) {
                                    msg.send(sender, listItems0(
                                            page,
                                            15,
                                            StructureManager.getRegisteredMachines().values(),
                                            msg.fromPath("MAIN.MULTIBLOCK_API.DISPLAY.REGISTERED")[0],
                                            msg.fromPath("MAIN.MULTIBLOCK_API.DISPLAY.REGISTERED")[0],
                                            (index, item) -> Component.of("  §l" + index + ". ")
                                                    .append(Component.of("§a" + item.getDeclaringClass().getSimpleName()
                                                            + " §f[§7" + item.getDeclaringClass().getCanonicalName() + "§f]")
                                                            .onClick(ClickEvent.suggestCmd(item.getDeclaringClass().getCanonicalName()))
                                                            .onHover(HoverEvent.showText(Component.of("§eGet full class path")))),
                                            new StandartBottom("/creatio structures list registered_machine ")));
                                }
                                return true;
                            }
                            msg.sendStatic(sender, "COMMAND.USAGE.STRUCTURES.LIST");
                            return true;
                        }
                        //creatio structures save
                        else if (args[1].equalsIgnoreCase(TC_STR[3])) {
                            StructureManager.saveMultiblockStructure();
                            msg.sendStatic(sender, "MAIN.MULTIBLOCK_API.SAVE");
                        }
                        //creatio structures info
                        else if (args[1].equalsIgnoreCase(TC_STR[4])) {
                            if (l == 3) {
                                //creatio structures info registered
                                if (args[2].equalsIgnoreCase(TC_STR_INFO[0])) {
                                    RegisteredStructure reg = StructureManager.getRegisteredStructures().get(args[3]);
                                    if (reg != null) {
                                        msg.send(sender, registeredInfo0(reg));
                                        return true;
                                    }
                                    msg.sendStatic(sender, "MAIN.MULTIBLOCK_API.INFO.REGISTERED.NOT_EXIST");
                                    return true;
                                }
                                //creatio structures info multiblock
                                else if (args[2].equalsIgnoreCase(TC_STR_INFO[1])) {
                                    for (MultiblockStructure m : StructureManager.getMultiblockStructures()) {
                                        if (args[3].equals(m.getUUID().toString())) {
                                            msg.send(sender, multiblockInfo0(m));
                                            return true;
                                        }
                                    }
                                    msg.sendStatic(sender, "MAIN.MULTIBLOCK_API.INFO.STRUCTURE.NOT_EXIST", args[2]);
                                    return true;
                                }
                                msg.sendStatic(sender, "COMMAND.USAGE.STRUCTURES.INFO.USAGE");
                            }
                            //creatio structures info * (info of the structure player looking at)
                            else {
                                if (rejectNonPlayer(sender)) {
                                    return false;
                                }
                                Block b = p.getTargetBlockExact(6);
                                if (b != null) {
                                    for (MultiblockStructure m : StructureManager.getMultiblockStructures()) {
                                        if (m.getBlockList().contains(b)) {
                                            msg.send(sender, multiblockInfo0(m));
                                            return true;
                                        }
                                    }
                                }
                                msg.sendStatic(sender, "MAIN.MULTIBLOCK_API.INFO.STRUCTURES.NOT_FOUND");
                            }
                            return true;
                            //sendStatic("COMMAND.USAGE.STRUCTURES.INFO.USAGE", executor);
                        }
                        //creatio structures register
                        else if (args[1].equalsIgnoreCase(TC_STR[5])) {
                            if (rejectNonPlayer(sender)) {
                                return false;
                            }
                            if (l >= 2) {
                                //creatio structures register tool
                                if (args[2].equalsIgnoreCase(TC_STR_REG[0])) {
                                    p.getInventory().addItem(STRUCTURE_SELECTOR);
                                    msg.sendStatic(sender, "MAIN.MULTIBLOCK_API.REGISTER.SELECTOR_GIVEN", p.getDisplayName());
                                    return true;
                                }
                                //creatio structures register create
                                if (l >= 4) {
                                    if (CommandRegister.COMPLETER_DIRECTIONS_4.contains(args[4].toLowerCase())) {
                                        Block[] set = STRUCTURE_SELECTION_SET.get(p);
                                        if (set != null) {
                                            if (set[2] != null) {
                                                executeCreateStructure0(BlockFace.valueOf(args[4].toUpperCase()), set, Arrays.copyOfRange(args, 5, l + 2));
                                            }
                                        }
                                        msg.sendStatic(sender, "MAIN.MULTIBLOCK_API.REGISTER.NO_POINT");
                                    }
                                    return true;
                                } else msg.sendStatic(sender, "MAIN.ERROR.CONSOLE");
                            }
                            msg.sendStatic(sender, "COMMAND.USAGE.STRUCTURES.REGISTER.USAGE");
                        }
                    }
                    msg.sendStatic(sender, "COMMAND.USAGE.STRUCTURES.USAGE");
                    return true;
                }
            }
            msg.sendStatic(sender, "COMMAND.USAGE.CREATIO");
            return true;
        }
    }

    private static class Completer implements TabCompleter {

        @Override
        public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
            if (!sender.isOp()) return null;
            int l = args.length - 1;

            //creatio *
            if (l == 0) return Arrays.asList(TC);

            //creatio structures
            if (TC[0].equalsIgnoreCase(get(args, 0))) {

                //creatio structures list
                if (TC_STR[0].equalsIgnoreCase(get(args, 1))) {

                    //creatio structures list * *int*
                    if (l == 3) return CommandRegister.COMPLETER_SMALL_INT;
                    return (l == 2) ? Arrays.asList(TC_STR_LIST) : CommandRegister.COMPLETER_EMPTY;
                }

                //creatio structures create
                if (TC_STR[1].equalsIgnoreCase(get(args, 1))) {
                    //creatio structures create *name*
                    if (l == 2) {
                        return new ArrayList<>(StructureManager.getRegisteredStructures().keySet());
                    }
                    //creatio structures create *name* *direction*
                    if (l == 3) {
                        return CommandRegister.COMPLETER_DIRECTIONS_4;
                    }
                }

                //creatio structures info
                if (TC_STR[4].equalsIgnoreCase(get(args, 1))) {
                    //creatio structures info registered *name*
                    if (TC_STR_INFO[0].equalsIgnoreCase(get(args, 2))) {
                        return (l == 3) ? new ArrayList<>(StructureManager.getRegisteredStructures().keySet()) : CommandRegister.COMPLETER_EMPTY;
                    }
                    //creatio structures info multiblock *uuid*
                    if (TC_STR_INFO[1].equalsIgnoreCase(get(args, 2))) {
                        List<String> ll = new ArrayList<>();
                        for (MultiblockStructure m : StructureManager.getMultiblockStructures()) {
                            ll.add(m.getUUID().toString());
                        }
                        return (l == 3) ? ll : CommandRegister.COMPLETER_EMPTY;
                    }
                    return (l == 2) ? Arrays.asList(TC_STR_INFO) : CommandRegister.COMPLETER_EMPTY;
                }

                //creatio structures register
                if (TC_STR[5].equalsIgnoreCase(get(args, 1))) {
                    //creatio structures register tool

                    //creatio structures register create
                    if (TC_STR_REG[1].equalsIgnoreCase(get(args, 2))) {

                        //creatio structures register create *name space*
                        if (l == 3) {
                            return Collections.singletonList("<structure namespace>");
                        }

                        //creatio structures register create *name* *direction*
                        if (l == 4) {
                            return CommandRegister.COMPLETER_DIRECTIONS_4;
                        }

                        //creatio structures register create *name* *direction* *flags*
                        if (l >= 5) {
                            if (TC_STR_REG_CFM[4].equals(args[l - 1])) {
                                return Collections.singletonList("<default name>");
                            }
                            if (TC_STR_REG_CFM[5].equals(args[l - 1])) {
                                return new ArrayList<>(StructureManager.getRegisteredMachines().keySet());
                            }
                            List<String> flags = Arrays.asList(Arrays.copyOfRange(args, 5, l + 2));
                            List<String> newf = new ArrayList<>();
                            for (String element : TC_STR_REG_CFM) {
                                if (!flags.contains(element)) newf.add(element);
                            }
                            return newf;
                        }
                    }
                    return (l == 2) ? Arrays.asList(TC_STR_REG) : CommandRegister.COMPLETER_EMPTY;
                }

                return (l == 1) ? Arrays.asList(TC_STR) : CommandRegister.COMPLETER_EMPTY;
            }

            //creatio reload
            if (TC[1].equalsIgnoreCase(get(args, 0))) return (l == 1) ? Arrays.asList(TC_RLD) : CommandRegister.COMPLETER_EMPTY;

            return CommandRegister.COMPLETER_EMPTY;
        }
    }


    private static void executeCreateStructure0(BlockFace facing, Block[] blocks, String[] properties) {
        //flippable, stable, static, overlappable
        boolean[] contract1 = new boolean[4];
        //name, hooked machine
        String[] contract2 = new String[2];
        for (int i = 0; i < properties.length; i++) {
            if (properties[i].equalsIgnoreCase(TC_STR_REG_CFM[0])) contract1[0] = true;
            if (properties[i].equalsIgnoreCase(TC_STR_REG_CFM[1])) contract1[1] = true;
            if (properties[i].equalsIgnoreCase(TC_STR_REG_CFM[2])) contract1[2] = true;
            if (properties[i].equalsIgnoreCase(TC_STR_REG_CFM[3])) contract1[3] = true;
            if (properties[i].equalsIgnoreCase(TC_STR_REG_CFM[4]) && i < properties.length - 1) contract2[0] = properties[i + 1];
            if (properties[i].equalsIgnoreCase(TC_STR_REG_CFM[5]) && i < properties.length - 1) contract2[1] = properties[i + 1];
        }
        RegisteredStructure.register(facing, blocks, contract1, contract2);
    }

    private static boolean rejectNonPlayer(CommandSender sender) {
        if (sender instanceof Player) {
            return false;
        } else {
            msg.sendStatic(sender, "MAIN.ERROR.CONSOLE");
            return true;
        }
    }

    /*public static void updateSelectionBorder(Player player) {
        Block[] b = STRUCTURE_SELECTION_SET.get(player);
        if (b[0] != null && b[1] != null && b[2] != null) {
            List<Location> collection = LocationUtil.drawCube(b[0].getLocation(), b[1].getLocation(), 0.5f, true);
            List<Packet> pk = new LinkedList<>();
            for (Location l : collection) {
                pk.add(new WorldParticlesPacket(new ParticleParamRedstone(-1.2f, -0.01f, -0.01f, 1), l, 2));
            }
            collection = LocationUtil.drawCube(b[2].getLocation(), b[2].getLocation(), 0.2f, true);
            for (Location l : collection) {
                pk.add(new WorldParticlesPacket(new ParticleParamRedstone(-0.01f, -1.2f, -0.01f, 1), l, 2));
            }
*//*            System.out.println(collection);
            System.out.println(pk);*//*
            SELECTION_PARTICLE_BUFFER.put(player, pk);
        }
    }

    @Task
    static void displaySelectionBorder() {
        for (Map.Entry<Player, List<Packet>> entry : SELECTION_PARTICLE_BUFFER.entrySet()) {
            if (!entry.getKey().isOnline()) {
                STRUCTURE_SELECTION_SET.remove(entry.getKey());
                SELECTION_PARTICLE_BUFFER.remove(entry.getKey());
            }
            for (Packet p : entry.getValue()) {
                if (p != null) p.send(entry.getKey());
            }
        }
    }*/

    private static String genTpCmdFromLoc0(Location loc) {
        return "/tp @s " +
                loc.getBlockX() + ' ' +
                loc.getBlockY() + ' ' +
                loc.getBlockZ();
    }

    private static Component[] multiblockInfo0(MultiblockStructure str) {
        Component[] text = new Component[10];
        text[0] = Component.of(msg.fromPath("MAIN.MULTIBLOCK_API.INFO.STRUCTURES.HEADER", str.getNameSpace())[0]);
        text[1] = Component.of("  §7UUID: §e" + str.getUUID())
                .onHover(HoverEvent.showText(Component.of("§eCopy UUID")))
                .onClick(ClickEvent.suggestCmd(str.getUUID().toString()));
        text[2] = Component.of("  §7Name space: §f" + str.getNameSpace());
        text[3] = Component.of("  §7Custom name: §f" + str.getName());
        text[4] = Component.of("  §7Is flipped: §f" + str.isFlipped());
        text[5] = Component.of("  §7Direction: §f" + str.getFacing().name());
        text[6] = Component.of("  §7Corner block: §f")
                .append(Component.of
                        ("[§a" + str.getCorner().getX() + "§7, §a" + str.getCorner().getY() + "§7, §a" + str.getCorner().getZ() + "§f]")
                        .onHover(HoverEvent.showText(Component.of("§eCopy coords")))
                        .onClick(ClickEvent.suggestCmd(genTpCmdFromLoc0(str.getCorner().getLocation())))
                .append(Component.of
                        (" [§2" + str.getEndCorner().getX() + "§7, §2" + str.getEndCorner().getY() + "§7, §2" + str.getEndCorner().getZ() + "§f]")
                        .onHover(HoverEvent.showText(Component.of("§eCopy coords")))
                        .onClick(ClickEvent.suggestCmd(genTpCmdFromLoc0(str.getEndCorner().getLocation())))));
        text[7] = Component.of(" §7 Center block: §f")
                .append(Component.of
                (" [§b" + str.getCore().getX() + "§7, §b" + str.getCore().getY() + "§7, §b" + str.getCore().getZ() + "§f]")
                .onHover(HoverEvent.showText(Component.of("§eCopy coords")))
                .onClick(ClickEvent.suggestCmd(genTpCmdFromLoc0(str.getCore().getLocation()))));
        text[8] = Component.of("  §fIn active state: §e" + StructureManager.getActiveStructures().contains(str));
        text[9] = Component.of("  §8Hash Code: §7" + str.hashCode());
        return text;
    }

    private static Component[] registeredInfo0(RegisteredStructure reg) {
        Component[] text = new Component[7];
        text[0] = Component.of(msg.fromPath("MAIN.MULTIBLOCK_API.INFO.STRUCTURES.HEADER", reg.getNameSpace())[0]);
        text[1] = Component.of("  §7Name space: §f" + reg.getNameSpace());
        text[2] = Component.of("  §7Default name: §f" + reg.getName());
        text[3] = Component.of("  §7Flippable: §f" + reg.flippable());
        text[4] = Component.of("  §7Center material: §f" + reg.getCenter());
        String mcn = "§8Null";
        if (reg.getMachineConst() != null) mcn = reg.getMachineConst().getName();
        text[5] = Component.of("  §7Hooked machine class: §f" + mcn);
        text[6] = Component.of("  §7Size(lhw): §f" + reg.getCorner()[0] + " " + reg.getCorner()[1] + " " + reg.getCorner()[2]);
        return text;
    }

    private static Component craftBottom0(@Nullable String prevCommand, @Nullable String sufCommand) {
        Component comp = Component.create();
        if (prevCommand == null) comp.append(Component.of("  §7<<< "));
        else comp.append(Component.of("  §2§l<<< ").onClick(ClickEvent.runCmd(prevCommand)));
        comp.append(Component.of("§3|"));
        if (sufCommand == null) comp.append(Component.of(" §7>>>"));
        else comp.append(Component.of(" §2§l>>>").onClick(ClickEvent.runCmd(sufCommand)));
        return comp;
    }

    private static <T> Component[] listItems0(int page,
                                         int pgSize,
                                         Collection<T> src,
                                         String nullHolder,
                                         String title,
                                         Listable<T> sortItem,
                                         Bottom bottom) {
        return listItems0(page, pgSize, src, s -> s.size() == 0, nullHolder, title, sortItem, bottom);
    }

    /** Generic list crafter */
    private static <T> Component[] listItems0(int page,
                                              int pgSize,
                                              Collection<T> src,
                                              Predicate<Collection<T>> nullLogic,
                                              String nullHolder,
                                              String title,
                                              Listable<T> sortItem,
                                              Bottom bottom) {

        Component[] text = new Component[Math.min(src.size(), pgSize) + 3];
        if (nullLogic.test(src)) {
            text[0] = Component.of(msg.fromPath("MAIN.LIST.NOT_FOUND", nullHolder)[0]);
        } else {
            text[0] = Component.of(msg.fromPath("MAIN.LIST.HEADER", title)[0]);

            int k = 1;
            int set = 0;
            page--;

            for (T values : src) {
                if (set < 15 * page) continue;
                text[k] = sortItem.get(k, values);
                if (k >= 15) break;
                k++;
            }

            text[k] = bottom.get(page, k < 15);
            text[k + 1] = Component.of(msg.fromPath("MAIN.LIST.FOOTER", Integer.toString(src.size()))[0]);
        }
        return text;
    }

    private static Component dyePath(String path) {
        String[] str = new String[2];
        str[0] = path.substring(0, path.lastIndexOf("/") + 1);
        str[1] = path.substring(path.length() - str[0].length());
        return Component.of(str[0]).withColor(0xFCFCFC).append(Component.of(str[1]).withColor("yellow"));
    }

    /** Listable interface for listItem() */
    private interface Listable<T> {
        Component get(int index, T item);
    }

    /** Bottom crafting */
    private interface Bottom {
        Component get(int page, boolean latest);
    }

    private static final class StandartBottom implements Bottom {

        private final String cmd;

        private StandartBottom(String cmd) {
            this.cmd = cmd;
        }

        @Override
        public Component get(int page, boolean latest) {
            String prev = (page == 0) ? null : cmd + page;
            String next = (latest) ? null : cmd + (page + 2);
            return craftBottom0(prev, next);
        }
    }
}
