package vip.creatio.clib.modules.customItem;

import vip.creatio.basic.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class LoreProcessor {

    List<LoreProcessUnit> lores = new ArrayList<>();
    int processInterval = 4;            // 5 times / s by default
    boolean lazyProcess = false;        // lore will not update if player's inventory is closed

    LoreProcessor() {}

    void process(List<Component> components, List<Integer> offsets) {
        int sum = 0;
        if (offsets.size() == 0) offsets.add(0);
        for (int i = 0; i < lores.size(); i++) {
            if (offsets.size() <= i) {
                offsets.add(lores.get(i).getSize());
            }

            sum += offsets.get(i);

            while (components.size() <= sum) {
                components.add(Component.create());
            }

            Component c =  components.get(sum);
            c = lores.get(i).apply(c);
            components.set(sum, c);
        }
    }

    public void addUnit(LoreProcessUnit unit) {
        lores.add(unit);
    }

    public void removeUnit(int index) {
        lores.remove(index);
    }

    public List<LoreProcessUnit> getUnits() {
        return lores;
    }

    public boolean isValid() {
        return !lores.isEmpty() || processInterval < 1;
    }

    public int getProcessInterval() {
        return processInterval;
    }

    public void setProcessInterval(int processInterval) {
        this.processInterval = processInterval;
    }

    public boolean isLazyProcess() {
        return lazyProcess;
    }

    public void setLazyProcess(boolean lazyProcess) {
        this.lazyProcess = lazyProcess;
    }
}
