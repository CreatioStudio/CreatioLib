package vip.creatio.clib.modules.customItem;

import vip.creatio.basic.util.ItemUtil;
import vip.creatio.basic.nbt.CompoundTag;
import net.minecraft.server.ItemStack;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vip.creatio.basic.util.NMS;

public class CustomItemStack {

    final ItemStack handle;
    final org.bukkit.inventory.ItemStack item;
    final CustomItem type;

    CustomItemStack(ItemStack handle, CustomItem type) {
        this.handle = handle;
        this.item = NMS.toBukkit(handle);
        this.type = type;
    }

    public org.bukkit.inventory.ItemStack getItem() {
        return item;
    }

    public @Nullable CompoundTag getTag() {
        return ItemUtil.getTag(handle);
    }

    public @NotNull CompoundTag getOrCreateTag() {
        return ItemUtil.getOrCreateTag(handle);
    }

    public CustomItem getType() {
        return type;
    }

    public Material getMaterial() {
        return item.getType();
    }

    public int getAmount() {
        return item.getAmount();
    }


}
