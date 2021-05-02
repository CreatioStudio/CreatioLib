package vip.creatio.clib.modules.menu;

import vip.creatio.basic.tools.Listener;
import vip.creatio.basic.tools.Task;
import vip.creatio.clib.multiblock.StructureManager;
import vip.creatio.clib.multiblock.machine.Machine;
import org.bukkit.event.inventory.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MenuManager {

    public static List<Menu> preset_menus = new ArrayList<>();
    public static List<InventoryAction> input_action = Arrays.asList(InventoryAction.PLACE_ALL, InventoryAction.PLACE_ONE,
            InventoryAction.PLACE_SOME, InventoryAction.SWAP_WITH_CURSOR, InventoryAction.HOTBAR_SWAP);

    //No default constructor
    private MenuManager() {}

    @Task
    static void tickMenu() {
        for (Machine m : StructureManager.getMachines()) {
            if (m instanceof MenuInteractable) {
                try {
                    if (((MenuInteractable) m).getLinkMenu().getLinkedInventory().getViewers().size() > 0) {
                        ((MenuInteractable) m).TickMenu();
                    }
                } catch (NullPointerException ignored) {}
            }
        }
    }

    //OnMenuOpen handler
    @Listener
    static void handlerOpenInv(InventoryOpenEvent event) {
        for (Machine m : StructureManager.getMachines()) {
            if (m instanceof MenuInteractable) {
                if (((MenuInteractable) m).getLinkMenu().getLinkedInventory() == event.getInventory()) {
                    ((MenuInteractable) m).onMenuOpen(event);
                }
            }
        }
    }

    //OnMenuClose handler
    @Listener
    static void handlerCloseInv(InventoryCloseEvent event) {
        for (Machine m : StructureManager.getMachines()) {
            if (m instanceof MenuInteractable) {
                if (((MenuInteractable) m).getLinkMenu().getLinkedInventory() == event.getInventory()) {
                    ((MenuInteractable) m).onMenuClose(event);
                }
            }
        }
    }

    //OnMenuClick handler
    @Listener
    static void handlerClickInv(InventoryClickEvent event) {
        for (Machine m : StructureManager.getMachines()) {
            if (m instanceof MenuInteractable) {
                if (event.getClickedInventory() != null) {
                    if (((MenuInteractable) m).getLinkMenu().getLinkedInventory() == event.getWhoClicked().getOpenInventory().getTopInventory()) {
                        if (event.getClickedInventory().getType() != InventoryType.PLAYER) {
                            if (((MenuInteractable) m).getLinkMenu().getSafeSlot().contains(event.getSlot())) {
                                event.setCancelled(true);
                                return;
                            } else if (((MenuInteractable) m).getLinkMenu().getOutputSlot().contains(event.getSlot()) && input_action.contains(event.getAction())) {
                                event.setCancelled(true);
                                return;
                            }
                        } else if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                            event.setCancelled(true);
                            return;
                        }

                        ((MenuInteractable) m).onMenuClick(event, event.isCancelled());
                    }
                }
            }
        }
    }

    //OnMenuDrag handler
    public static void handlerDragInv(InventoryDragEvent event) {
        for (Machine m : StructureManager.getMachines()) {
            if (m instanceof MenuInteractable) {
                if (((MenuInteractable) m).getLinkMenu().getLinkedInventory() == event.getInventory()) {
                    for (Integer i : event.getRawSlots()) {
                        if (((MenuInteractable) m).getLinkMenu().getSafeSlot().contains(i) || ((MenuInteractable) m).getLinkMenu().getOutputSlot().contains(i)) {
                            event.setCancelled(true);
                            break;
                        }
                    }
                    ((MenuInteractable) m).onMenuDrag(event, event.isCancelled());
                }
            }

        }
    }
}
