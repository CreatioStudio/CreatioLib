package vip.creatio.clib.modules.customItem;

import vip.creatio.basic.chat.Component;
import org.jetbrains.annotations.Nullable;

public interface LoreProcessUnit {

    Component apply(@Nullable Component comp);

    default int getSize() {
        return 1;
    }

}
