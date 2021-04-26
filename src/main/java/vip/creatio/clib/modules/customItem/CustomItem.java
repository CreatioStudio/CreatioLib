package vip.creatio.clib.modules.customItem;

import vip.creatio.basic.util.ItemUtil;
import vip.creatio.basic.nbt.CompoundTag;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Container class
 */
public abstract class CustomItem {

    private static final CompoundTag WRAPPER = CompoundTag.create()
            .putString("id", "minecraft:stone")
            .putByte("Count", (byte) 1)
            .put("tag", CompoundTag.create());


    protected final LoreProcessor processor = new LoreProcessor();
    // Something like a internal id, cannot be repeated
    protected final String name;

    // Restrict the material of custom item, or any material can do the work
    protected Material restrictedMat;

    // Default tag that custom item has, only for generating itemstack
    protected CompoundTag defaultItemTag;

    public CustomItem(@NotNull String name, @Nullable Material restricted, @Nullable CompoundTag defaultTag) {
        this.name = name;
        this.restrictedMat = restricted;
        this.defaultItemTag = defaultTag == null
                ? CompoundTag.create().putString(CustomItemManager.CUSTOM_ITEM_TAG, name)
                : defaultTag;
    }

    public CustomItem(@NotNull String name, @Nullable Material restricted) {
        this(name, restricted, null);
    }

    public CustomItem(@NotNull String name) {
        this(name, null, null);
    }

    public final void register() {
        CustomItemManager.register(this);
    }

    public LoreProcessor getProcessor() {
        return processor;
    }

    public String getName() {
        return name;
    }

    public Material getRestrictedMat() {
        return restrictedMat;
    }

    public void setRestrictedMat(Material restrictedMat) {
        this.restrictedMat = restrictedMat;
    }

    public CompoundTag getDefaultItemTag() {
        return defaultItemTag;
    }

    public void setDefaultItemTag(CompoundTag defaultItemTag) {
        this.defaultItemTag = defaultItemTag;
    }

    public ItemStack getItem() {
        return ItemUtil.fromNBT(WRAPPER.clone().put("tag", defaultItemTag));
    }

    protected void tick(CustomItemStack item, Player p) {}

    protected void onClickBlock(CustomItemStack item, PlayerInteractEvent event) {}

    protected void onClickInv(CustomItemStack item, InventoryClickEvent event) {}

    protected void onDrop(CustomItemStack item, PlayerDropItemEvent event) {}

    protected void onPickup(CustomItemStack item, EntityPickupItemEvent event) {}

    protected void onDespawn(CustomItemStack item, ItemDespawnEvent event) {}

    protected void onEntityDamage(CustomItemStack item, EntityDamageEvent event) {}

    protected void onMend(CustomItemStack item, PlayerItemMendEvent event) {}

    protected void onSwap(CustomItemStack item, PlayerSwapHandItemsEvent event) {}

    protected void onBreak(CustomItemStack item, Player p) {}

    protected void onConsume(CustomItemStack item, PlayerItemConsumeEvent event) {}

    protected void onDamage(CustomItemStack item, PlayerItemDamageEvent event) {}

    protected void onRightClickEntity(CustomItemStack item, PlayerInteractEntityEvent event) {}

    protected void onLeftClickEntity(CustomItemStack item, EntityDamageByEntityEvent event) {}
}
