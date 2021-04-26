package vip.creatio.clib.modules.menu;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

public interface MenuInteractable {

    void linkMenu(Menu menu);

    Menu getLinkMenu();

    void onMenuOpen(InventoryOpenEvent event);

    void onMenuClose(InventoryCloseEvent event);

    void onMenuClick(InventoryClickEvent event, Boolean cancelled);

    void onMenuDrag(InventoryDragEvent event, Boolean cancelled);

    void TickMenu();

}
