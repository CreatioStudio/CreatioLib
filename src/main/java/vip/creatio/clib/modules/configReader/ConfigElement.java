package vip.creatio.clib.modules.configReader;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ConfigElement implements ConfigBase {

    private Type type;
    private Object element;
    private List<String> attached;

    public ConfigElement(Object element) {
        this.element = element;
        switch (element.getClass().getSimpleName()) {
            case "Integer":
                type = Type.Int;
                break;
            case "Short":
                type = Type.Short;
                break;
            case "Long":
                type = Type.Long;
                break;
            case "Byte":
                type = Type.Byte;
                break;
            case "Double":
                type = Type.Double;
                break;
            case "Float":
                type = Type.Float;
                break;
            case "Boolean":
                type = Type.Boolean;
                break;
            case "String":
                type = Type.String;
                break;
            case "ArrayList":
                type = Type.List;
                break;
            case "HashMap":
                type = Type.Map;
                break;
        }
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

enum Type {
    Int, Double, Short, String, List, Boolean, Long, Float, Map, Byte;
}
