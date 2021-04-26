package vip.creatio.clib.modules.configReader;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ConfigSection implements ConfigBase {

    private Map<String, ConfigElement> section;
    private List<String> attached;

    public ConfigSection(Map<String, ConfigElement> section) {
        this.section = section;
    }

    @Override
    public void setComment(Collection<String> comment) {
        this.attached = (List<String>) comment;
    }

    @Override
    public void addComment(int index, String... comment) {
        this.attached.addAll(index, Arrays.asList(comment));
    }

    @Override
    public void removeComment(int index) {
        this.attached.remove(index);
    }

    @Override
    public int commentSize() {
        return this.attached.size();
    }

}
