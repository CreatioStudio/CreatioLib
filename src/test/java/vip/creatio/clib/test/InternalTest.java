package vip.creatio.clib.test;

import vip.creatio.clib.modules.configReader.Config;
import vip.creatio.clib.modules.configReader.ResourceConfig;
import org.junit.Test;

public class InternalTest {

    @Test
    public void run_test() {
        ResourceConfig d = new ResourceConfig("/multiblock_structures/example.yml");
        //ResourceConfig d = new ResourceConfig("/config.yml");
        Config l = d.getConfig();
    }

//    public int getDepth(String sample) {
//        int max = 0, loop = 0;
//        boolean inString = false;
//        for (byte c : sample.getBytes()) {
//            if (!inString) {
//                switch (c) {
//                    case '{':
//                    case '[':
//                        loop++;
//                        max = max < loop ? loop : max;
//                        break;
//                    case '}':
//                    case ']':
//                        loop--;
//                        break;
//                    case '\'':
//                    case  '"':
//                        inString = true;
//                        break;
//                }
//            } else {
//                if (c == '"' || c == '\'') {
//                    inString = false;
//                }
//            }
//        }
//        max = loop != 0 ? -1 : max;
//        max = inString ? -2 : max;
//        return max;
//    }

//    public Map<String, Object> readStructure(@NotNull FileConfiguration c, @NotNull String id) {
//        Map<String, Object> map = new HashMap<>();
//        map.put("id",id);
//        ConfigurationSection s = c.getConfigurationSection("DATA." + id);
//        map.put("data", setMap(s));
//        return map;
//    }
//
//    public Map<String, Object> setMap(ConfigurationSection s) {
//        Map<String, Object> map = s.getValues(false);
//        for (String key : map.keySet()) {
//            if (map.get(key) instanceof MemorySection) {
//                ConfigurationSection c = s.getConfigurationSection(key);
//                map.put(key, setMap(c));
//            }
//        }
//        return map;
//    }
}

