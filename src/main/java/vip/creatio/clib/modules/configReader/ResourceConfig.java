package vip.creatio.clib.modules.configReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ResourceConfig implements Readable {

    private final Config config;

    public ResourceConfig(String path) {
        InputStream s = getClass().getResourceAsStream(path);
        BufferedReader br = new BufferedReader(new InputStreamReader(s));
        String temp;
        List<String> list = new ArrayList<>();
        try {
            while((temp = br.readLine()) != null) {
                list.add(temp);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = new Config(list);
    }

    public Config getConfig() {
        return config;
    }
}
