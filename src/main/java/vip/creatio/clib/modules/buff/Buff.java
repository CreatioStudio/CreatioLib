package vip.creatio.clib.modules.buff;

import vip.creatio.clib.Creatio;
import vip.creatio.basic.tools.Listener;
import vip.creatio.basic.tools.Task;
import vip.creatio.basic.tools.TaskType;
import vip.creatio.clib.modules.function.Tickable;
import vip.creatio.basic.chat.Component;
import vip.creatio.common.util.StringUtil;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Buff is a tiny version of potion effect with following features:
 *   1. Will be cleared when server restart, which is not something precious.
 *   2. Cannot be cancelled by simply drinking milk.
 *   3. Cannot be seen by player by default.
 */
public abstract class Buff implements Tickable {

    private static final HashMap<Entity, LinkedList<Buff>> BUFF_MAP = new HashMap<>();

    /** Buff immunity map, buff that in this list will not tick on it's entity, but will still consume remaining ticks. */
    private static final HashMap<Entity, HashSet<String>> IMMUNITY_MAP = new HashMap<>();

    @Task(value = TaskType.SYNC_TICK)
    static void tickBuffs() {
        synchronized (BUFF_MAP) {
            LinkedList<Entity> removal = null;
            for (Map.Entry<Entity, LinkedList<Buff>> entry : BUFF_MAP.entrySet()) {

                // Dead entity check
                if (entry.getKey().isDead()) {
                    if (removal == null) removal = new LinkedList<>();
                    removal.addLast(entry.getKey());
                    entry.getValue().forEach(Buff::discard);
                    continue;
                }

                // Player check
                if (entry.getKey() instanceof Player) {
                    Player p = (Player) entry.getKey();
                    // If player is not only, then remove the buff.
                    if (!p.isOnline()) {
                        if (removal == null) removal = new LinkedList<>();
                        removal.addLast(p);
                        entry.getValue().forEach(Buff::discard);
                        continue;
                    }
                }

                // Buff loop
                Iterator<Buff> iter = entry.getValue().iterator();
                HashSet<String> immunity = IMMUNITY_MAP.get(entry.getKey());
                while (iter.hasNext()) {
                    Buff buff = iter.next();
                    if (buff.remainingTicks < 1) {
                        buff.discard();
                        iter.remove();
                    }
                    else {
                        if (immunity != null && immunity.contains(buff.name)) {
                            buff.remainingTicks--;
                            continue;
                        }

                        try {
                            buff.tick();
                            buff.remainingTicks--;
                        } catch (Throwable t) {
                            Creatio.intern("Generated a exception while ticking com.henryrenyz.creatio.buff " + buff + ", auto killing...");
                            buff.discard();
                            iter.remove();
                            t.printStackTrace();
                        }
                    }
                }
            }

            if (removal != null) removal.forEach(BUFF_MAP::remove);
        }
    }

    public static void addBuff(Buff buff) {
        synchronized (BUFF_MAP) {
            LinkedList<Buff> buffList = BUFF_MAP.get(buff.owner);
            if (buffList == null) {
                buffList = new LinkedList<>();
                buffList.add(buff);
                BUFF_MAP.put(buff.owner, buffList);
            } else {
                for (Buff buf : buffList) {
                    if (buf.name.hashCode() == buff.name.hashCode()) {
                        buf.merge(buff);
                        return;
                    }
                }
                buffList.add(buff);
            }
        }
    }

    public static void removeBuff(Buff buff) {
        synchronized (BUFF_MAP) {
            LinkedList<Buff> list = BUFF_MAP.get(buff.owner);
            if (list == null) return;
            list.remove(buff);
            buff.discard();
        }
    }

    public static void removeBuff(Entity e, String buffName) {
        synchronized (BUFF_MAP) {
            LinkedList<Buff> list = BUFF_MAP.get(e);
            if (list == null) return;

            Iterator<Buff> iter = list.iterator();
            int hash = buffName.hashCode();
            while (iter.hasNext()) {
                Buff b = iter.next();
                if (b.name.hashCode() == hash) {
                    iter.remove();
                    b.discard();
                }
            }
        }
    }

    public static void clearBuffs(Entity e) {
        synchronized (BUFF_MAP) {
            LinkedList<Buff> list = BUFF_MAP.get(e);
            if (list != null) list.forEach(Buff::discard);
            BUFF_MAP.remove(e);
        }
    }

    @Task(value = TaskType.ON_UNLOAD)
    public static void clearAllBuffs() {
        synchronized (BUFF_MAP) {
            for (List<Buff> list : BUFF_MAP.values()) {
                list.forEach(Buff::discard);
            }
            BUFF_MAP.clear();
        }
    }

    public static List<Buff> getAllBuffs(Entity e) {
        synchronized (BUFF_MAP) {
            LinkedList<Buff> list = BUFF_MAP.get(e);
            if (list == null) return Collections.emptyList();
            else return Collections.unmodifiableList(list);
        }
    }

    @Listener
    static void onPlayerDeath(PlayerDeathEvent event) {
        String msg = Buff.getDeathMessage(event.getEntity());
        if (msg != null) event.setDeathMessage(msg);
    }

    @Nullable
    public static String getDeathMessage(Entity e) {
        for (Buff b : getAllBuffs(e)) {
            String s = b.getDeathMessage();
            if (b.getDeathMessage() != null) {
                return s;
            }
        }
        return null;
    }

    @NotNull
    private static HashSet<String> getImmunity0(Entity e) {
        HashSet<String> immunity = IMMUNITY_MAP.get(e);
        return immunity == null ? new HashSet<>() : immunity;
    }

    @NotNull
    public static Set<String> getImmunity(Entity e) {
        return new HashSet<>(getImmunity0(e));
    }

    public static boolean isImmuneTo(Entity e, String buff) {
        return getImmunity0(e).contains(buff);
    }

    public static boolean isImmuneTo(Entity e, Buff b) {
        return isImmuneTo(e, b.name);
    }

    /** Returns true if immunity string does not exist before */
    public static boolean addImmunity(Entity e, String name) {
        HashSet<String> immunity = IMMUNITY_MAP.get(e);
        if (immunity != null) {
            if (immunity.contains(name)) return false;
            immunity.add(name);
        } else {
            immunity = new HashSet<>();
            immunity.add(name);
            IMMUNITY_MAP.put(e, immunity);
        }
        return true;
    }

    public static boolean addImmunity(Entity e, Buff b) {
        return addImmunity(e, b.name);
    }

    public static void removeImmunity(Entity e, String name) {
        HashSet<String> immunity = IMMUNITY_MAP.get(e);
        if (immunity != null) {
            immunity.remove(name);
        }
    }

    public static void removeImmunity(Entity e, Buff b) {
        removeImmunity(e, b.name);
    }

    /**
     * All attribute buff should use this name
     * as the name of modifier, so plugin can safely clear
     * these buff when something unexpected happens.
     */
    public static final String ATTRIBUTE_HEADER = "AttributeBuff";
    protected String getAttributeBuffName() {
        return ATTRIBUTE_HEADER + '_' + getClass().getCanonicalName();
    }


    protected final Entity owner;
    protected int amplifier = 1;
    protected int remainingTicks;
    protected int tickTime;
    protected final String name;

    protected Buff(Entity owner, int remainingTicks, @Nullable String name) {
        this.owner = owner;
        this.remainingTicks = remainingTicks;
        this.name = name == null ? "Buff" : name;
    }

    public String getName() {
        return name;
    }

    @Override
    public void tick() {
        onTick();
        tickTime++;
    }

    protected void onTick() {}

    public void discard() {}

    @Nullable
    public String getDeathMessage() {
        return null;
    }

    public Entity getOwner() {
        return owner;
    }

    public Location getLoc() {
        return getOwner().getLocation();
    }

    public int getAmplifier() {
        return amplifier;
    }

    public void setAmplifier(int amplifier) {
        this.amplifier = amplifier;
    }

    public int getRemainingTicks() {
        return remainingTicks;
    }

    public void setRemainingTicks(int remainingTicks) {
        this.remainingTicks = remainingTicks;
    }

    /**
     * A formal name that contains both name and level
     * For example, "AttributedBuff IV"
     */
    public Component getFormalName() {
        return craftFormat(name);
    }

    protected Component craftFormat(String name) {
        return amplifier == 1
                ? Component.of(name)
                : Component.of(name + ' ' + toRomanNumber(amplifier));
    }

    protected String toRomanNumber(int num) {
        return StringUtil.toRomanNumber(num);
    }

    /** Merge 2 same kind of com.henryrenyz.creatio.buff, returns true if any operation applied on this com.henryrenyz.creatio.buff */
    public boolean merge(Buff b) {
        if (b.owner == this.owner && b.name.hashCode() == this.name.hashCode()) {
            if (this.amplifier == b.amplifier) {
                this.remainingTicks = Math.max(b.remainingTicks, this.remainingTicks);
            } else {
                if (this.amplifier < b.amplifier) {
                    this.remainingTicks = b.remainingTicks;
                    this.amplifier = b.amplifier;
                } else {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public final Buff addBuff() {
        addBuff(this);
        return this;
    }

    @Override
    public int hashCode() {
        return name.hashCode() + owner.hashCode() * 31 + amplifier * 12321;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() == this.getClass()) {
            return obj.hashCode() == this.hashCode();
        }
        return false;
    }

    @Override
    public String toString() {
        return "Buff{entity=" + owner + ",amplifier=" + amplifier + ",remaining=" + remainingTicks + ",name=" + name + '}';
    }

    // For attribute buff
    @Listener
    static void onPlayerJoin(PlayerJoinEvent event) {

        //Buff attribute clear
        for (Attribute att : Attribute.values()) {
            AttributeInstance instance = event.getPlayer().getAttribute(att);
            if (instance != null) {
                Collection<AttributeModifier> c = instance.getModifiers();
                c.removeIf(attributeModifier -> !attributeModifier.getName().startsWith(Buff.ATTRIBUTE_HEADER));
                c.forEach(instance::removeModifier);
            }
        }
    }
}
