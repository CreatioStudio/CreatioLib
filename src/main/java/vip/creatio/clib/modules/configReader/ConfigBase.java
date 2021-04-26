package vip.creatio.clib.modules.configReader;

import java.util.Arrays;
import java.util.Collection;

public interface ConfigBase {

    void setComment(Collection<String> comment);

    default void setComment(String... comment) {
        setComment(Arrays.asList(comment));
    }

    default void addComment(String... comment) {
        addComment(commentSize() - 1, comment);
    }

    void addComment(int index, String... comment);

    void removeComment(int index);

    int commentSize();
}
