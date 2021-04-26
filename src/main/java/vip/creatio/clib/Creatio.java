package vip.creatio.clib;

import vip.creatio.basic.tools.FormatMsgManager;
import vip.creatio.basic.tools.loader.DelegatedPlugin;
import vip.creatio.clib.command.CommandRegister;
import vip.creatio.basic.config.Configs;
import vip.creatio.basic.packet.PacketManager;
import vip.creatio.clib.internal.CLibBootstrap;
import vip.creatio.clib.multiblock.MultiblockStructure;
import vip.creatio.clib.multiblock.StructureManager;
import vip.creatio.clib.multiblock.listener.MultiblockAPIStructureCreate;
import vip.creatio.clib.multiblock.listener.MultiblockAPIStructureDestroy;
import vip.creatio.clib.multiblock.listener.MultiblockAPIStructureInteract;
import vip.creatio.clib.multiblock.listener.MultiblockAPIStructureUnload;
import vip.creatio.clib.multiblock.machine.BlastFurnace;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

import static vip.creatio.clib.multiblock.StructureManager.saveMultiblockStructure;

public final class Creatio extends DelegatedPlugin {

    private static Creatio      instance;

    //Generic config
    private final CLibBootstrap bootstrap;

    //MultiblockAPI config
    private boolean             enable_multiblock_api;
    private int                 structure_check_radius;
    private int                 max_structure_in_radius;
    private int                 max_structure_in_server;
    private int                 max_active_structure;
    private int                 structure_save_interval;
    private int                 max_projectiles;
    private int                 max_dmg_indicators;

    private boolean             enable_update_checker;
    private boolean             enable_metrics;
    private boolean             enable_dmg_indicator;

    private Configuration       config;
    private FormatMsgManager    msg;
    private CommandRegister     plugin_commands;
    private PacketManager       packet_manager = new PacketManager();
    private int                 configVersion;

    //ThreadPool for async multi-tasking.
    private final ExecutorService threadPool = Executors.newCachedThreadPool();

    protected Creatio(CLibBootstrap bootstrap) {
        super(bootstrap);
        this.bootstrap = bootstrap;
        instance = this;
    }

    @Override
    public void onEnable() {
        loadConfig();
        loadLanguageConfig();

        msg.sendStatic(Level.INFO, "MAIN.LOADING");

        //Metrics
        if (enable_metrics) {
            int pluginId = 9236;
            Metrics metrics = new Metrics(bootstrap, pluginId);
            //metrics.addCustomChart(new Metrics.SimplePie("chart_id", () -> "My value"));
        }

        //Update Checker
        if (enable_update_checker) {
            threadPool.execute(new Updater());
        }

        //hookThirdParty();

        //Register
        plugin_commands = new CommandRegister("creatiolib");
        plugin_commands.init();

        registerEvent();

        msg.sendStatic(Level.INFO, "MAIN.LOADED");
    }

    @Override
    public void onDisable() {
        //Serialize MultiblockStructure
        if (enable_multiblock_api) {
            saveMultiblockStructure();
            StructureManager.releaseChunks();
        }
    }

    public void loadAllConfig() {
        loadConfig();
        loadLanguageConfig();
        loadMultiblockStructures();
    }

    //Config setter
    public void loadConfig() {
        Configs.updateConfig(bootstrap, "config.yml", configVersion);
        config =                                Configs.load(bootstrap, "config.yml");

        configVersion =                         config.getInt("version");
        enable_update_checker =                 config.getBoolean("enable_update_checker", true);
        enable_metrics =                        config.getBoolean("enable_metrics", true);

        max_projectiles =                       config.getInt("max_projectiles", 400);
        max_dmg_indicators =                    config.getInt("max_dmg_indicators", 200);
        enable_dmg_indicator =                  config.getBoolean("enable_dmg_indicator", true);

        //MultiblockAPI config set
        enable_multiblock_api =                 config.getBoolean("enable_multiblock_api", true);

        structure_check_radius =                config.getInt("multiblock_api.structure_check_radius", 0);
        structure_check_radius =                (structure_check_radius < 0) ? 2147483647 : structure_check_radius;

        max_structure_in_radius =               config.getInt("multiblock_api.max_structure_in_radius", 0);
        max_structure_in_radius =               (max_structure_in_radius < 0) ? 2147483647 : max_structure_in_radius;

        max_structure_in_server =               config.getInt("multiblock_api.max_structure_in_server", 0);
        max_structure_in_server =               (max_structure_in_server < 0) ? 2147483647 : max_structure_in_server;

        max_active_structure =                  config.getInt("multiblock_api.max_active_structure", 0);
        max_active_structure =                  (max_active_structure < 0) ? 2147483647 : max_active_structure;

        structure_save_interval =               config.getInt("multiblock_api.structure_save_interval", -1);
        structure_save_interval =               (structure_save_interval < 40) ? -1 : structure_save_interval;

    }

    public void loadLanguageConfig() {
        String lang = config.getString("language", "en_US");
        Configs.updateConfig(bootstrap, "lang/" + lang + ".yml", -1);
        Configuration langConfig = Configs.load(bootstrap, "lang/" + lang + ".yml");
        this.msg = new FormatMsgManager(langConfig, langConfig.getString("MAIN.FORMAT.PREFIX"));
        this.msg.addReplacer("%w%", langConfig.getString("MAIN.FORMAT.WARN", "&e"));
        this.msg.addReplacer("%e%", langConfig.getString("MAIN.FORMAT.ERROR", "&c"));
        this.msg.addReplacer("%n%", langConfig.getString("MAIN.FORMAT.NORMAL", "&7"));
        this.msg.addReplacer("%s%", langConfig.getString("MAIN.FORMAT.SUCCESS", "&a"));
        this.msg.addReplacer("%h%", langConfig.getString("MAIN.FORMAT.HIGHLIGHT", "&6"));
        msgSender = this.msg;
    }

    public void loadMultiblockStructures() {
        if (enable_multiblock_api) {
            File cfg = new File(bootstrap.getDataFolder(), "multiblock_structures");
            if (!(cfg.exists()) || (cfg.listFiles().length == 0)) {
                Configs.updateConfig(bootstrap, "multiblock_structures/example.yml", -1);
            }
            if (StructureManager.getMultiblockStructures() != null) {
                saveMultiblockStructure();
            }
            int[] load_success = StructureManager.loadAll();
            msg.sendStatic(Level.INFO, "MAIN.RELOAD.DONE", Integer.toString(load_success[0]), Integer.toString(load_success[1]));
        }
    }

    private void registerEvent() {
        PluginManager manager = bootstrap.getServer().getPluginManager();
        if (enable_multiblock_api) {

            //Register API event listener
            manager.registerEvents(new MultiblockAPIStructureDestroy(), bootstrap);
            manager.registerEvents(new MultiblockAPIStructureCreate(), bootstrap);
            manager.registerEvents(new MultiblockAPIStructureUnload(), bootstrap);
            manager.registerEvents(new MultiblockAPIStructureInteract(), bootstrap);


            //Register serialization
            ConfigurationSerialization.registerClass(MultiblockStructure.class);


            //Register Machine
            StructureManager.registerMachine(BlastFurnace.class);


            //load All
            loadMultiblockStructures();
        }
    }

    public static void intern(String msg) {
        instance.msg.intern(msg);
    }

    public static void log(String msg) {
        instance.msg.log(msg);
    }

    public static FormatMsgManager getSender() {
        return instance.msg;
    }

    public static Creatio getInstance() {
        return instance;
    }

    public static CLibBootstrap getBootstrap() {
        return instance.bootstrap;
    }

    public boolean enabledMultiblockApi() {
        return enable_multiblock_api;
    }

    public int structureCheckRadius() {
        return structure_check_radius;
    }

    public int maxStructureInRadius() {
        return max_structure_in_radius;
    }

    public int maxStructureInServer() {
        return max_structure_in_server;
    }

    public int maxActiveStructure() {
        return max_active_structure;
    }

    public int maxDamageIndicators() {
        return max_dmg_indicators;
    }

    public int structureSaveInterval() {
        return structure_save_interval;
    }

    public boolean enabledUpdateChecker() {
        return enable_update_checker;
    }

    public boolean enabledMetrics() {
        return enable_metrics;
    }

    public int configVersion() {
        return configVersion;
    }

    public int maxProjectiles() {
        return max_projectiles;
    }

    public boolean enabledHDDamageDisplay() {
        return enable_dmg_indicator;
    }

    public PacketManager packetManager() {
        return packet_manager;
    }

    public ExecutorService getThreadPool() {
        return threadPool;
    }
}
