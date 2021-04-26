package vip.creatio.clib.multiblock;

import vip.creatio.clib.modules.Serializer;
import vip.creatio.clib.multiblock.machine.Machine;
import org.bukkit.Chunk;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import vip.creatio.clib.Creatio;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.*;

public class StructureManager {

    private static final Map<String, RegisteredStructure> registered_structures = new HashMap<>();
    private static final Map<String, Constructor<?>> registered_machines = new HashMap<>();

    private static final List<MultiblockStructure> multiblock_structures = new LinkedList<>();
    private static final List<MultiblockStructure> active_structures = new LinkedList<>();
    private static final List<Machine> machine = new LinkedList<>();

    public static int[] loadAll() {
        List<FileConfiguration> conf = new ArrayList<>();
        int k = 0;
        int kd = 0;

        for (File f : new File(Creatio.getBootstrap().getDataFolder(), "multiblock_structures").listFiles((dir, name) ->
                new File(dir,name).isFile() && !name.contains("data"))) {
            conf.add(YamlConfiguration.loadConfiguration(f));
            k++;
        }

        for (FileConfiguration c : conf) {
            if ((loadRegisteredStructure(c))) {
                kd++;
            }
        }

        loadMultiblockStructure();

        return new int[]{k,kd};
    }

    public static boolean loadRegisteredStructure(FileConfiguration c) {
        for (String m : c.getConfigurationSection("STRUCTURES").getKeys(false)) {
            Map<String, Object> map = Serializer.readStructure(c, m, false), map1 = new HashMap<>();
            map1.put("data", map);
            map1.put("id", m);
            RegisteredStructure.deserialize(map1);
        }
        return true;
    }

    public static boolean loadMultiblockStructure() {
        File f = new File(Creatio.getBootstrap().getDataFolder(), "multiblock_structures/data/saved_structures.yml");
        if (f.exists()) {
            multiblock_structures.clear();
            FileConfiguration c = YamlConfiguration.loadConfiguration(f);
            if (c.getConfigurationSection("DATA") != null) {
                for(String m : c.getConfigurationSection("DATA").getKeys(false)) {

                    Map<String, Object> map = Serializer.readStructure(c, m, true);
                    try {
                        UUID id = UUID.fromString(m);
                        MultiblockStructure.deserialize(map, id);
                    } catch (IllegalArgumentException e) {
                        Creatio.intern("&4Exception in structure deserialization! Invalid structure uuid: &6&l" + m);
                        e.printStackTrace();
                    }

                    return true;
                }
            }
        }
        return false;
    }

    static {
        if (Creatio.getInstance().structureSaveInterval() > 0)
            Creatio.getInstance().getTaskManager()
                    .addSyncTask(StructureManager::saveMultiblockStructure, Creatio.getInstance().structureSaveInterval() * 50);
    }
    public static boolean saveMultiblockStructure() {
        if (multiblock_structures.size() > 0) {
//          File[] fl = new File(plugin.getDataFolder(), "multiblock_structures/data").listFiles();
//            if (fl.length > 30) {
//                fl[fl.length - 1].delete();
//            }
            File f = new File(Creatio.getBootstrap().getDataFolder(), "multiblock_structures/data/saved_structures.yml");
            try {
                if (f.exists()) {
                    FileWriter writer = new FileWriter(f);
                    writer.write("");
                    writer.flush();
                    writer.close();
                }
                File v = new File(Creatio.getBootstrap().getDataFolder(), "multiblock_structures/data/saved_structures.yml");
                v.getParentFile().mkdirs();
                v.createNewFile();

                FileConfiguration c = YamlConfiguration.loadConfiguration(f);
                int k = 0;
                for (MultiblockStructure m : multiblock_structures) {
                    c.set("DATA."+m.getUUID(), m.serialize());
                    k++;

                }
                c.save(v);
                return true;
            } catch (IOException e) {
                Creatio.intern("&4Exception in saving multiblock structures! Unable to modify data file!");
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    public static void releaseChunks() {
        for (MultiblockStructure m : multiblock_structures) {
            for (Chunk ch : m.getForceChunks()) {
                ch.setForceLoaded(false);
            }
        }
    }

    public static void registerMachine(Class<?> cls) {
        try {
            String name = cls.getSimpleName();
            registered_machines.put(name, cls.getConstructor(MultiblockStructure.class));
        } catch (NoSuchMethodException e) {
            Creatio.intern("&4Exception in registering machine! No constructor found in class &6&l" + cls.getSimpleName());
            e.printStackTrace();
        }
    }

    public static Constructor<?> getMachineConst(String name) {
        return registered_machines.get(name);
    }

    public static Map<String, RegisteredStructure> getRegisteredStructures() {
        return registered_structures;
    }

    public static List<MultiblockStructure> getMultiblockStructures() {
        return multiblock_structures;
    }

    public static List<MultiblockStructure> getActiveStructures() {
        return active_structures;
    }

    public static List<Machine> getMachines() {
        return machine;
    }

    public static Map<String, Constructor<?>> getRegisteredMachines() {
        return Collections.unmodifiableMap(registered_machines);
    }
}
