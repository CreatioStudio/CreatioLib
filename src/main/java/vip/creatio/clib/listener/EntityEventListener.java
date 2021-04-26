package vip.creatio.clib.listener;

import vip.creatio.clib.Creatio;
import vip.creatio.basic.annotation.Listener;
import vip.creatio.clib.modules.customDmg.CustomDamageEvent;
import vip.creatio.clib.modules.customDmg.Damage;
import vip.creatio.clib.modules.customDmg.Indicator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.*;

import java.util.Arrays;
import java.util.List;

import static org.bukkit.event.entity.EntityDamageEvent.DamageCause.*;
import static org.bukkit.event.entity.EntityDamageEvent.DamageCause.DRYOUT;
import static org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason.*;

public class EntityEventListener {

    private static final List<EntityRegainHealthEvent.RegainReason> BUFF =
            Arrays.asList(SATIATED, EATING, EntityRegainHealthEvent.RegainReason.WITHER);

    @Listener(priority = EventPriority.HIGHEST)
    public static void onHeal(EntityRegainHealthEvent event) {
        if (Creatio.getInstance().enabledHDDamageDisplay()) {
            if (event.getEntity() instanceof LivingEntity) {
                Location loc = ((LivingEntity) event.getEntity()).getEyeLocation();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getLocation().distance(loc) < 64) {
                        if (BUFF.contains(event.getRegainReason())) {
                            Indicator.newHealIndicator(((LivingEntity) event.getEntity()).getEyeLocation(),
                                    (float) event.getAmount(), Indicator.HEALING_IND_BUFF);
                        } else {
                            Indicator.newHealIndicator(((LivingEntity) event.getEntity()).getEyeLocation(),
                                    (float) event.getAmount());
                        }
                        break;
                    }
                }
            }
        }
    }

    private static final List<EntityDamageEvent.DamageCause> DEBUFF =
            Arrays.asList(FALL, FIRE, FIRE_TICK, MELTING, LAVA, DROWNING, VOID, STARVATION, POISON,
                    EntityDamageEvent.DamageCause.MAGIC,
                    EntityDamageEvent.DamageCause.WITHER,
                    FALLING_BLOCK, THORNS, DRAGON_BREATH, HOT_FLOOR, CRAMMING, DRYOUT);

    @Listener(priority = EventPriority.LOWEST)
    public static void onDamageOccurred(EntityDamageByEntityEvent event) {

        //Custom arrow projectile
        if (event.getDamager() instanceof Arrow
                && event.getDamager().getScoreboardTags().contains("CreatioCustom")
                && !(event instanceof CustomDamageEvent)) {
            event.setCancelled(true);
        }
    }

    @Listener(priority = EventPriority.HIGHEST)
    public static void onDamageConfirmed(EntityDamageEvent event) {
        if (Creatio.getInstance().enabledHDDamageDisplay()) {
            if (event.getEntity() instanceof LivingEntity && !(event.getEntity() instanceof ArmorStand)) {
                Location loc = ((LivingEntity) event.getEntity()).getEyeLocation();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getWorld() == loc.getWorld() && p.getLocation().distance(loc) < 64) {
                        if (event instanceof EntityDamageByEntityEvent) {
                            EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;

                            // Custom Damage process
                            if (event instanceof CustomDamageEvent) {
                                Damage dmg = ((CustomDamageEvent) event).getCustomDmg();
                                if (dmg.noDamageIndicator()) break;

                                // Custom Damage Event have to create damage indicator manually by use lambda in  onDamageDealt
                                Indicator.newAttackIndicator(loc,
                                        ((CustomDamageEvent) event).getCustomDmg().getFinalDamage());

                            } else {
                                if (e.getDamager() instanceof HumanEntity && !e.getDamager().isOnGround() && e.getDamager().getFallDistance() > 0.3F) {
                                    Indicator.newCritIndicator(loc,
                                            (float) event.getFinalDamage());
                                } else {
                                    Indicator.newAttackIndicator(loc,
                                            (float) event.getFinalDamage());
                                }
                            }
                        } else {
                            if (DEBUFF.contains(event.getCause())) {
                                Indicator.newDebuffIndicator(loc,
                                        (float) event.getFinalDamage());
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

}
