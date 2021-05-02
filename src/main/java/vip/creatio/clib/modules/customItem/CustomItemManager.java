package vip.creatio.clib.modules.customItem;

import vip.creatio.basic.util.NMS;
import vip.creatio.clib.Creatio;
import vip.creatio.basic.tools.Listener;
import vip.creatio.basic.tools.Task;
import vip.creatio.basic.tools.GlobalTaskExecutor;
import vip.creatio.basic.util.EntityUtil;
import vip.creatio.basic.util.ItemUtil;
import vip.creatio.basic.chat.Component;
import vip.creatio.basic.nbt.CompoundTag;
import vip.creatio.basic.nbt.ListTag;
import vip.creatio.common.util.ArrayUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class CustomItemManager {

    static final HashMap<String, CustomItem> REGISTERED_ITEMS = new HashMap<>();
    public static final String CUSTOM_ITEM_TAG = "CLibCustomItem";
    public static final String CUSTOM_LORE_OFFSETS = "CLibCustomLoreOffsets";

    private static final GlobalTaskExecutor TASK_MGR = Creatio.getInstance().getTaskManager();

    private CustomItemManager() {}

    @Task(period = 50)
    static void tick() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            // Bukkit's getInventoryContent will create massive amount of ItemStack instances
            // so here I use direct nms inv list getter.
            for (net.minecraft.server.ItemStack i : ItemUtil.getNmsInvItems(p)) {
                if (!i.isEmpty()) {
                    CustomItemStack customItem = getCustomItem(i);
                    if (customItem != null && (customItem.type.restrictedMat == null
                            || customItem.getMaterial() == customItem.type.restrictedMat)) {
                        customItem.type.tick(customItem, p);
                        // Update lores
                        LoreProcessor proc = customItem.type.processor;
                        if (proc.isValid()) {
                            // Lazy process
                            if (proc.lazyProcess && p.getOpenInventory().getType() == InventoryType.CRAFTING)
                                continue;
                            if (TASK_MGR.counter() % proc.processInterval == 0) {
                                CompoundTag tag = customItem.getOrCreateTag();
                                int[] array = tag.getIntArray(CUSTOM_LORE_OFFSETS);

                                tag.putIfAbsent("display", new CompoundTag());
                                CompoundTag display = tag.getCompound("display");

                                List<Component> lores = ItemUtil.getLores(i);
                                if (lores == null) lores = new ArrayList<>();

                                List<Integer> offsets =
                                        ArrayUtil.toList(array.length == 0 ? new int[]{lores.size()} : array);

                                proc.process(lores, offsets);

                                display.put("Lore", ListTag.toList(lores));
                                tag.putIntArray(CUSTOM_LORE_OFFSETS, offsets);
                            }
                        }
                    }
                }
            }
        }
    }

    public static void register(CustomItem item) {
        REGISTERED_ITEMS.put(item.name, item);
    }

    public static void unregister(String itemName) {
        REGISTERED_ITEMS.remove(itemName);
    }

    public static void unregister(CustomItem item) {
        REGISTERED_ITEMS.remove(item.name);
    }

    public static void unregisterAll() {
        REGISTERED_ITEMS.clear();
    }

    public static Collection<CustomItem> getRegisteredItem() {
        return REGISTERED_ITEMS.values();
    }

    @Nullable
    public static CustomItemStack getCustomItem(ItemStack itemStack) {
        if (itemStack instanceof CraftItemStack) {
            return getCustomItem(NMS.toNms(itemStack));
        }
        return null;
    }

    @Nullable
    static CustomItemStack getCustomItem(net.minecraft.server.ItemStack itemStack) {
        if (itemStack == null) return null;
        CompoundTag tag = ItemUtil.getTag(itemStack);
        if (tag == null) return null;

        String name = tag.getString(CUSTOM_ITEM_TAG);
        if (!name.isBlank()) {
            CustomItem item = REGISTERED_ITEMS.get(name);
            if (item != null) {
                return new CustomItemStack(itemStack, item);
            }
        }
        return null;
    }

    @Listener(priority = EventPriority.MONITOR)
    static void onItemMend(PlayerItemMendEvent event) {
        CustomItemStack stack = getCustomItem(event.getItem());
        if (stack != null) {
            stack.type.onMend(stack, event);
        }
    }

    @Listener(priority = EventPriority.MONITOR)
    static void onItemSwap(PlayerSwapHandItemsEvent event) {
        CustomItemStack stack = getCustomItem(event.getMainHandItem());
        if (stack != null) {
            stack.type.onSwap(stack, event);
        }

        stack = getCustomItem(event.getOffHandItem());
        if (stack != null) {
            stack.type.onSwap(stack, event);
        }
    }

    @Listener(priority = EventPriority.MONITOR)
    static void onItemClick(PlayerInteractEvent event) {
        // Make sure that onClick and onClickEntity will not be triggered at the same time
        Player p = event.getPlayer();
        Location loc = p.getEyeLocation();
        RayTraceResult r = p.getWorld()
                .rayTraceEntities(loc, loc.getDirection(), EntityUtil.getReachDistance(p), 0.0D, e -> e != p);
        if (r == null || r.getHitEntity() == null) {
            CustomItemStack stack = getCustomItem(event.getItem());
            if (stack != null) {
                stack.type.onClickBlock(stack, event);
            }
        }
    }

    @Listener(priority = EventPriority.MONITOR)
    static void onRightClickOnEntity(PlayerInteractEntityEvent event) {
        PlayerInventory inv = event.getPlayer().getInventory();
        CustomItemStack stack = getCustomItem(inv.getItem(event.getHand()));
        if (stack != null) {
            stack.type.onRightClickEntity(stack, event);
        }
    }

    @Listener(priority = EventPriority.MONITOR)
    static void onLeftClickOnEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            PlayerInventory inv = ((Player) event.getDamager()).getInventory();
            CustomItemStack stack = getCustomItem(inv.getItemInMainHand());
            if (stack != null) {
                stack.type.onLeftClickEntity(stack, event);
            } else {
                stack = getCustomItem(inv.getItemInOffHand());
                if (stack != null) {
                    stack.type.onLeftClickEntity(stack, event);
                }
            }
        }
    }

    @Listener(priority = EventPriority.MONITOR)
    static void onInventoryClick(InventoryClickEvent event) {
        CustomItemStack stack = getCustomItem(event.getCurrentItem());
        if (stack != null) {
            stack.type.onClickInv(stack, event);
        }
    }

    @Listener(priority = EventPriority.MONITOR)
    static void onItemDrop(PlayerDropItemEvent event) {
        CustomItemStack stack = getCustomItem(event.getItemDrop().getItemStack());
        if (stack != null) {
            stack.type.onDrop(stack, event);
        }
    }
    
    @Listener(priority = EventPriority.MONITOR)
    static void onItemPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) {
            CustomItemStack stack = getCustomItem(event.getItem().getItemStack());
            if (stack != null) {
                stack.type.onPickup(stack, event);
            }
        }
    }

    @Listener(priority = EventPriority.MONITOR)
    static void onItemDespawn(ItemDespawnEvent event) {
        CustomItemStack stack = getCustomItem(event.getEntity().getItemStack());
        if (stack != null) {
            stack.type.onDespawn(stack, event);
        }
    }

    @Listener(priority = EventPriority.MONITOR)
    static void onItemEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Item) {
            CustomItemStack stack = getCustomItem(((Item) event.getEntity()).getItemStack());
            if (stack != null) {
                stack.type.onEntityDamage(stack, event);
            }
        }
    }

    @Listener(priority = EventPriority.MONITOR)
    static void onItemBreak(PlayerItemBreakEvent event) {
        CustomItemStack stack = getCustomItem(event.getBrokenItem());
        if (stack != null) {
            stack.type.onBreak(stack, event.getPlayer());
        }
    }

    @Listener(priority = EventPriority.MONITOR)
    static void onItemConsume(PlayerItemConsumeEvent event) {
        CustomItemStack stack = getCustomItem(event.getItem());
        if (stack != null) {
            stack.type.onConsume(stack, event);
        }
    }

    @Listener(priority = EventPriority.MONITOR)
    static void onItemDamage(PlayerItemDamageEvent event) {
        CustomItemStack stack = getCustomItem(event.getItem());
        if (stack != null) {
            stack.type.onDamage(stack, event);
        }
    }

}
