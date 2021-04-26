package vip.creatio.clib.modules;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Serializer {

//    @Test
//    public void test() {
//        File f = new File("C:\\Users\\HenryRenYz\\Desktop\\Minecraft Server\\Creatio Project - BungeeCord\\[N]Test\\plugins\\CreatioLib\\multiblock_structures\\data","saved_structures.yml");
//        FileConfiguration c = YamlConfiguration.loadConfiguration(f);
//
//        Map<String, Object> map = readStructure(c, "ddc6cf3f-d27c-49a0-aed6-64d7af8fdc2c", true);
//        System.out.println(map);
//    }

    //read multiblock structures
    public static Map<String, Object> readStructure(@NotNull FileConfiguration c, @NotNull String id, @NotNull Boolean isData) {
        ConfigurationSection s;
        if (isData) s = c.getConfigurationSection("DATA." + id);
        else s = c.getConfigurationSection("STRUCTURES." + id);
        return convertToMap(s);
    }

    //Convert a section of yml to map
    public static Map<String, Object> convertToMap(ConfigurationSection s) {
        Map<String, Object> map = s.getValues(false);
        for (String key : map.keySet()) {
            if (map.get(key) instanceof MemorySection) {
                ConfigurationSection c = s.getConfigurationSection(key);
                map.put(key, convertToMap(c));
            }
        }
        return map;
    }

    //Get element from a map
    public static Object getFromMap(Map<String, Object> map, String path) {
        String[] node = path.split("\\.");
        if (node.length > 1) {
            for (int i = 0; i < node.length - 1; i++) {
                map = (Map<String, Object>) map.get(node[i]);
            }
        }
        return map.get(node[node.length - 1]);
    }

    //serialize all the element in a list of serializable element
    public static List<Map<String, Object>> serializeListElement(List<? extends ConfigurationSerializable> list) {
        List<Map<String, Object>> l = new ArrayList<>();
        for (ConfigurationSerializable c : list) {
            if (c != null) {
                l.add(c.serialize());
            } else {
                l.add(null);
            }
        }
        return l;
    }

    //deserialize a ItemStack list
    public static List<ItemStack> deserializeItemStackList(List<Map<String, Object>> list) {
        List<ItemStack> l = new ArrayList<>();
        for (Map<String, Object> m : list) {
            if (m != null) {
                l.add(ItemStack.deserialize(m));
            } else {
                l.add(null);
            }
        }
        return l;
    }

//    public Map<String, Object> addToMap(Map<String, Object> map, String path, Object value) {
//        List<String> node = Arrays.asList(path.split("\\."));
//        if (node.size() > 1) {
//            Map<String, Object> map1 = map;
//            for (int i = 0; i < node.size() - 1; i++) {
//                if (map.get(node.get(i)) == null) {
//                    map = createNewMap(map, node.subList(i, node.size() - 1));
//                }
//                map = (Map<String, Object>) map.get(node.get(i));
//            }
//        }
//        return map;
//    }
//
//    private Map<String, Object> createNewMap(Map<String, Object> map, List<String> list) {
//        if (list.size() > 1) {
//            Collections.reverse(list);
//            list.remove(0);
//            Map<String, Object> map1 = new HashMap<>();
//            for (int i = 0; i < list.size() - 1; i++) {
//                Map<String, Object> map2 = new HashMap<>();
//                map2.put(list.get(i), map1);
//                map1 = map2;
//            }
//            map.put(list.get(list.size() - 1), map1);
//        }
//        return map;
//    }
}
