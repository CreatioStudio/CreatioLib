package vip.creatio.clib.multiblock.machine;

import vip.creatio.clib.modules.Serializer;
import vip.creatio.clib.modules.menu.Menu;
import vip.creatio.clib.modules.menu.MenuInteractable;
import vip.creatio.clib.modules.util.ItemRecipe;
import vip.creatio.basic.util.ItemUtil;
import vip.creatio.clib.multiblock.MultiblockStructure;
import vip.creatio.clib.multiblock.StructureManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SuppressWarnings("unchecked")
public class BlastFurnace extends Machine implements MenuInteractable, MachineClickable {

    private final MultiblockStructure hooked_structure;
    private Menu menu = new Menu(Bukkit.createInventory(null, 36, "§6§lBlast Furnace"), false);

    //Menu
    private static final Integer[]
            safeSlot = new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 13, 16, 18, 22, 25, 27, 28, 29, 30, 31, 32, 33, 34, 35},
            void0s = new Integer[]{9, 13, 16, 18, 22, 25},
            void1s = new Integer[]{5, 6, 7, 8},
            void2s = new Integer[]{0, 1, 2, 3},
            temp0s = new Integer[]{27, 28, 29, 30, 31, 32, 33, 34, 35},
            matSlot = new Integer[]{10, 11, 12, 19, 20, 21},
            resultSlot = new Integer[]{14, 15, 23, 24},
            fuelSlot = new Integer[]{17, 26};
    private static final ItemStack
            void0 = new ItemStack(Material.WHITE_STAINED_GLASS_PANE),
            void1 = new ItemStack(Material.GRAY_STAINED_GLASS_PANE),
            remaining = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE),
            void2 = new ItemStack(Material.BLACK_STAINED_GLASS_PANE),
            process = new ItemStack(Material.RED_STAINED_GLASS_PANE),
            temp = new ItemStack(Material.GOLD_INGOT),
            temp0 = new ItemStack(Material.NETHERITE_INGOT),
            info = new ItemStack(Material.PAPER);
    private static final Map<Material, Burnable.Template> fuelMap = new HashMap<>();
    static List<ItemRecipe<ItemRecipe.Matcher>> recipe = new ArrayList<>();

    static {
        //recipe.add(new ItemRecipe<>(new ItemRecipe.Matcher(Material.IRON_INGOT), ));

        ItemUtil.setName(void0, "§1");
        ItemUtil.setName(void1, "§fFuel Remaining: §l0");
        ItemUtil.setName(void2, "§fFinish in §l0 §fseconds");
        ItemUtil.setName(remaining, "§fFuel Remaining: §l0");
        ItemUtil.setName(process, "§fFinish in §l0 §fseconds");
        ItemUtil.setName(temp, "§eTemperature: §l§6273K");
        ItemUtil.setName(temp0, "§eTemperature: §l§6273K");
        ItemUtil.setName(info, "§eBlast Furnace");
        ItemUtil.setLores(info, Arrays.asList("§fPut ores on the left", "§fPut fuel on the right"));

        Burnable.Template logs = new Burnable.Template(1300, 300);
        Burnable.Template stems = new Burnable.Template(1400, 500);
        //logs and stems
        fuelMap.put(Material.ACACIA_LOG, logs);
        fuelMap.put(Material.BIRCH_LOG, logs);
        fuelMap.put(Material.DARK_OAK_LOG, logs);
        fuelMap.put(Material.JUNGLE_LOG, logs);
        fuelMap.put(Material.OAK_LOG, logs);
        fuelMap.put(Material.SPRUCE_LOG, logs);
        fuelMap.put(Material.STRIPPED_ACACIA_LOG, logs);
        fuelMap.put(Material.STRIPPED_BIRCH_LOG, logs);
        fuelMap.put(Material.STRIPPED_DARK_OAK_LOG, logs);
        fuelMap.put(Material.STRIPPED_JUNGLE_LOG, logs);
        fuelMap.put(Material.STRIPPED_OAK_LOG, logs);
        fuelMap.put(Material.STRIPPED_SPRUCE_LOG, logs);
        fuelMap.put(Material.ACACIA_WOOD, logs);
        fuelMap.put(Material.BIRCH_WOOD, logs);
        fuelMap.put(Material.DARK_OAK_WOOD, logs);
        fuelMap.put(Material.JUNGLE_WOOD, logs);
        fuelMap.put(Material.OAK_WOOD, logs);
        fuelMap.put(Material.SPRUCE_WOOD, logs);
        fuelMap.put(Material.STRIPPED_ACACIA_WOOD, logs);
        fuelMap.put(Material.STRIPPED_BIRCH_WOOD, logs);
        fuelMap.put(Material.STRIPPED_DARK_OAK_WOOD, logs);
        fuelMap.put(Material.STRIPPED_JUNGLE_WOOD, logs);
        fuelMap.put(Material.STRIPPED_OAK_WOOD, logs);
        fuelMap.put(Material.STRIPPED_SPRUCE_WOOD, logs);
        fuelMap.put(Material.CRIMSON_STEM, stems);
        fuelMap.put(Material.WARPED_STEM, stems);
        fuelMap.put(Material.STRIPPED_CRIMSON_STEM, stems);
        fuelMap.put(Material.STRIPPED_WARPED_STEM, stems);
        fuelMap.put(Material.CRIMSON_HYPHAE, stems);
        fuelMap.put(Material.WARPED_HYPHAE, stems);
        fuelMap.put(Material.STRIPPED_CRIMSON_HYPHAE, stems);
        fuelMap.put(Material.STRIPPED_WARPED_HYPHAE, stems);

        try {
            fuelMap.put(Material.LAVA_BUCKET, new Burnable.Template(1500, 12000, true));
            fuelMap.put(Material.MAGMA_BLOCK, new Burnable.Template(1100, 200));
            fuelMap.put(Material.COAL, new Burnable.Template(2000, 3200, "{CustomItem:\"coking_coal\"}", new Burnable.Template(1700, 1600)));
            fuelMap.put(Material.CHARCOAL, new Burnable.Template(1600, 1600));
            fuelMap.put(Material.GLOWSTONE_DUST, new Burnable.Template(1700, 1600, "{CustomItem:\"burnium_crystal\"}"));
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    //static variables
    private static final double room_temperature = 273d, max_delta_temperature = 1d;

        //exprements
    static {
        Map<String, Object> map = new HashMap<>();
        map.put("ori", new ItemStack(Material.IRON_ORE, 1));
        map.put("result", new ItemStack(Material.IRON_INGOT, 2));
        map.put("temp", 1500d);
        //recipe.add(map);


    }

    //permanent data
    private double temperature = 273d, fuel_remaining = 0d, fuel_max_temperature = 273d, delta_temperature = 1d;
        //inventory
    private List<ItemStack> mat;
    private List<ItemStack> done;
    private List<ItemStack> fuel;

    //temperate data
    private Double speed, target_temperature;
    private ItemStack lockMat;
    private ItemStack lockFuel;

    public BlastFurnace(MultiblockStructure hooked) {
        this.hooked_structure = hooked;
        initInv();
        menu.setSafeSlot(safeSlot);
        menu.setOutputSlot(resultSlot);
        initMenu();
        hooked.setHookedMachine(this);
        StructureManager.getMachines().add(this);
        //hooked.
    }

    private void initInv() {
        if (mat == null) {
            mat = new ArrayList<>(6);
            mat.addAll(Arrays.asList(null, null, null, null, null, null));
        }
        if (done == null) {
            done = new ArrayList<>(4);
            done.addAll(Arrays.asList(null, null, null, null));
        }
        if (fuel == null) {
            fuel = new ArrayList<>(2);
            fuel.addAll(Arrays.asList(null, null));
        }
    }


    public void initMenu() {
        menu.setSlotItem(void0, void0s);
        menu.setSlotItem(void1, void1s);
        menu.setSlotItem(void2, void2s);
        menu.setSlotItem(temp0, temp0s);
        menu.setSlotItem(info, 4);
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && !event.getPlayer().isSneaking()) {
            event.getPlayer().openInventory(menu.getLinkedInventory());
            event.setCancelled(true);
        }
    }

    @Override
    public void Enable() {

    }

    @Override
    public void Disable() {

    }

    @Override
    public void onDestroy(Event event) {
        if (event != null) {
            if (event instanceof BlockEvent) {
                dropEverything(((BlockEvent) event).getBlock().getLocation());
                return;
            }
        }
        dropEverything(hooked_structure.getCore().getLocation());
    }

    private void dropEverything(Location loc) {
        invToList();
        assert loc.getWorld() != null;
        for (int i = 0; i < mat.size(); i++) {
            if (mat.get(i) != null) {
                loc.getWorld().dropItem(loc, mat.get(i));
                mat.set(i, null);
            }
        }
        for (int i = 0; i < done.size(); i++) {
            if (done.get(i) != null) {
                loc.getWorld().dropItem(loc, done.get(i));
                done.set(i, null);
            }
        }
        for (int i = 0; i < fuel.size(); i++) {
            if (fuel.get(i) != null) {
                loc.getWorld().dropItem(loc, fuel.get(i));
                fuel.set(i, null);
            }
        }
        listToInv();
    }

    //Menu System
    @Override
    public void linkMenu(Menu menu) {
        this.menu = menu;
    }

    @Override
    public Menu getLinkMenu() {
        return this.menu;
    }

    @Override
    public void onMenuOpen(InventoryOpenEvent event) {

    }

    @Override
    public void onMenuClose(InventoryCloseEvent event) {

    }

    @Override
    public void onMenuClick(InventoryClickEvent event, Boolean cancelled) {
        if (!cancelled) {

        }
    }

    @Override
    public void onMenuDrag(InventoryDragEvent event, Boolean cancelled) {
        if (!cancelled) {

        }
    }

    @Override
    public void Tick() {

    }

    @Override
    public void TickMenu() {

    }

    private void addToFuel(int slot) {

    }

    private void invToList() {
        for (int i = 0; i < matSlot.length; i++) {
            mat.set(i, menu.getSlotItem(matSlot[i]));
        }
        for (int i = 0; i < resultSlot.length; i++) {
            done.set(i, menu.getSlotItem(resultSlot[i]));
        }
        for (int i = 0; i < fuelSlot.length; i++) {
            fuel.set(i, menu.getSlotItem(fuelSlot[i]));
        }
    }

    private void listToInv() {
        for (int i = 0; i < matSlot.length; i++) {
            menu.setSlotItem(mat.get(i), matSlot[i]);
        }
        for (int i = 0; i < resultSlot.length; i++) {
            menu.setSlotItem(done.get(i), resultSlot[i]);
        }
        for (int i = 0; i < fuelSlot.length; i++) {
            menu.setSlotItem(fuel.get(i), fuelSlot[i]);
        }
    }

    private boolean checkSlotAvailable(List<ItemStack> slot, ItemStack item) {
        for (ItemStack i : slot) {
            if (i == null) {
                return true;
            }
            if (i.getType() == item.getType()) return i.getItemMeta() == item.getItemMeta();
        }
        return false;
    }

    private boolean addToSlot(List<ItemStack> slot, ItemStack item) {
        for (ItemStack i : slot) {
            if (i == null) {
                i = item.clone();
                item.setType(Material.AIR);
                item.setAmount(0);
                return true;
            }
            if (i.getType() == item.getType() && i.getItemMeta() == item.getItemMeta()) {
                int delta = Math.min(64 - i.getAmount(), item.getAmount());
                if (delta > 0) {
                    i.setAmount(i.getAmount() + delta);
                    item.setAmount(item.getAmount() - delta);
                    if (item.getAmount() == 0) item.setType(Material.AIR);
                    return true;
                }
            }
        }
        return false;
    }


    @Override
    public @NotNull Map<String, Object> serialize() {
        invToList();

        Map<String, Object> map = new HashMap<>();
        map.put("temperature", temperature);
        map.put("fuel_remaining", fuel_remaining);
        map.put("fuel_max_temperature", fuel_max_temperature);
        map.put("delta_temperature", delta_temperature);
        map.put("ori", Serializer.serializeListElement(mat));
        map.put("done", Serializer.serializeListElement(done));
        map.put("fuel", Serializer.serializeListElement(fuel));
        return map;
    }

    @Override
    public void deserialize(Map<String, Object> map) {
        temperature = map.get("temperature") != null ? (Double) map.get("temperature") : room_temperature;
        fuel_remaining = map.get("fuel_remaining") != null ? (Double) map.get("fuel_remaining") : 0;
        fuel_max_temperature = map.get("fuel_max_temperature") != null ? (Double) map.get("fuel_max_temperature") : 0;
        delta_temperature = map.get("delta_temperature") != null ? (Double) map.get("delta_temperature") : 1;
        initInv();
        mat = map.get("ori") != null ? Serializer.deserializeItemStackList((List<Map<String, Object>>) map.get("ori")) : null;
        done = map.get("done") != null ? Serializer.deserializeItemStackList((List<Map<String, Object>>) map.get("done")) : null;
        fuel = map.get("fuel") != null ? Serializer.deserializeItemStackList((List<Map<String, Object>>) map.get("fuel")) : null;
        listToInv();
    }

    @Override
    public String getNameSpace() {
        return this.hooked_structure.getNameSpace();
    }

    @Override
    public MultiblockStructure getHookedStructure() {
        return this.hooked_structure;
    }

    private static final class Burnable {
        private int temp;
        private int tickTime;
        private ItemStack item = null;
        private boolean returnBucket = false;

        private Burnable(int temp, int tickTime, ItemStack item, boolean returnBucket) {
            this.temp = temp;
            this.tickTime = tickTime;
            this.item = item;
            this.returnBucket = returnBucket;
        }

        private Burnable() {}

        public int getTemp() {
            return temp;
        }

        public int getTickTime() {
            return tickTime;
        }

        public ItemStack getItem() {
            return item;
        }

        public boolean isReturnBucket() {
            return returnBucket;
        }

        public boolean isAvailable() {
            return item != null;
        }

        public boolean consume() {
            int a = item.getAmount();
            if (returnBucket) {
                item.setItemMeta(null);
                item.setType(Material.BUCKET);
                return true;
            }
            if (a > 0) {
                item.setAmount(--a);
                if (a == 0) item.setType(Material.AIR);
                return true;
            }
            return false;
        }

        private static class Template extends ItemRecipe.Matcher {
            private final int temp;
            private final int tickTime;
            private Template replacement = null;
            private boolean returnBucket = false;

            private Template(int temp, int tickTime) {
                super(null);
                this.temp = temp;
                this.tickTime = tickTime;
            }

            //replacement: when material does not meet the tag
            private Template(int temp, int tickTime, String tag, Template replacement) throws Exception {
                super(null, 1, tag);
                this.temp = temp;
                this.tickTime = tickTime;
                this.replacement = replacement;
            }

            private Template(int temp, int tickTime, String tag) throws Exception {
                super(null, 1, tag);
                this.temp = temp;
                this.tickTime = tickTime;
            }

            private Template(int temp, int tickTime, boolean returnBukkit) {
                super(null);
                this.temp = temp;
                this.tickTime = tickTime;
                this.returnBucket = returnBukkit;
            }

            private Burnable build(ItemStack item) {
                if (match(item)) return new Burnable(temp, tickTime, item, returnBucket);
                else if (replacement != null) return replacement.build(item);
                else return new Burnable();
            }
        }
    }
}
