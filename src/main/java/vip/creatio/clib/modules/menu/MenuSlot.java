package vip.creatio.clib.modules.menu;

import org.bukkit.inventory.ItemStack;

import java.util.List;

public class MenuSlot {

    private int slot = 0;

    private Menu menu;

    public MenuSlot(Menu menu, int slot) {
        this.slot = slot;
        this.menu = menu;
    }

    public int getSlot() {return this.slot;}

    public Menu getMenu() {return this.menu;}

    public void setItem(ItemStack item) {
        menu.setSlotItem(item, slot);
    }

    public MenuSlot setName(String name) {
        menu.setSlotName(slot, name);
        return this;
    }
    public String getName() {
        return menu.getSlotName(slot);
    }

    public MenuSlot setLores(List<String> lores) {
        menu.setSlotLores(slot, lores);
        return this;
    }
    public List<String> getLores() {
        return menu.getSlotLores(slot);
    }

    public MenuSlot setLore(String lore, int row) {
        menu.setSlotLore(slot, lore, row);
        return this;
    }
    public String getLore(int row) {
        return menu.getSlotLore(slot, row);
    }

}
