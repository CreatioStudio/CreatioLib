package vip.creatio.clib.modules.menu;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Menu {

    private Inventory inv;
    private List<Integer> safeSlot = new ArrayList<>();
    private List<Integer> outputSlot = new ArrayList<>();

    public Menu(Inventory preset, Boolean isProtected) {
        this.inv = preset;
        if (isProtected) {
            ItemStack[] content = inv.getStorageContents();
            for (int i = 0; i < content.length; i++) {
                safeSlot.add(i);
            }
        }
    }

    public Inventory getLinkedInventory() {return this.inv;}

    public void setLinkedInventory(Inventory inv) {this.inv = inv;}

    public List<Integer> getSafeSlot() {return this.safeSlot;}

    public void setSafeSlot(List<Integer> list) {this.safeSlot = list;}

    public void setSafeSlot(Integer... list) {this.safeSlot = Arrays.asList(list);}

    public List<Integer> getOutputSlot() {return this.outputSlot;}

    public void setOutputSlot(List<Integer> list) {this.outputSlot = list;}

    public void setOutputSlot(Integer... list) {this.outputSlot = Arrays.asList(list);}

    //Set menu slot item
    public Menu setSlotItem(ItemStack item, Integer slot) {
        inv.setItem(slot, item);
        return this;
    }
    public Menu setSlotItem(ItemStack item, List<Integer> slots) {
        for (Integer i : slots) {
            inv.setItem(i, item);
        }
        return this;
    }
    public Menu setSlotItem(ItemStack item, Integer... slots) {
        return setSlotItem(item, Arrays.asList(slots));
    }

    //get menu slot
    public MenuSlot getSlot(Integer slot) {
        return new MenuSlot(this, slot);
    }

    //Get slot item
    public ItemStack getSlotItem(Integer slot) {
        return inv.getItem(slot);
    }

    //Set menu slot item name
    public Menu setSlotName(Integer slot, String name) {
        inv.getItem(slot).getItemMeta().setDisplayName(name);
        return this;
    }
    public String getSlotName(Integer slot) {
        return inv.getItem(slot).getItemMeta().getDisplayName();
    }

    //Set menu slot item lore
    public Menu setSlotLores(Integer slot, List<String> lores) {
        inv.getItem(slot).getItemMeta().setLore(lores);
        return this;
    }
    public List<String> getSlotLores(Integer slot) {
        return inv.getItem(slot).getItemMeta().getLore();
    }

    //Set menu slot item lore
    public Menu setSlotLore(Integer slot, String lore, int row) {
        List<String> list = inv.getItem(slot).getItemMeta().getLore();
        list.set(row, lore);
        inv.getItem(slot).getItemMeta().setLore(list);
        return this;
    }
    public String getSlotLore(Integer slot, int row) {
        return inv.getItem(slot).getItemMeta().getLore().get(row);
    }

}
