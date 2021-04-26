package vip.creatio.clib.modules.buff;

import vip.creatio.clib.modules.customDmg.Damage;
import vip.creatio.clib.modules.customDmg.Indicator;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/**
 * A Buff that gives periodic potion effect or(and) damage
 */
public class DamageBuff extends Buff {

    protected final Damage dmg;
    /** Interval between damage */
    protected final int interval;

    public DamageBuff(Damage dmg, int interval, int remaining, @Nullable String name) {
        super(dmg.getVictim(), remaining, name == null ? "DamageBuff" : name);
        this.dmg = dmg;
        this.interval = interval;
    }

    public DamageBuff(LivingEntity entity, float damage, int interval, int remaining, @Nullable String name) {
        this(new Damage(entity, entity, damage, Damage.NO_DMG_INDICATOR | Damage.NO_SOUND | Damage.DMG_INVULNERABLE | Damage.NO_EXTERNAL_MOD | Damage.NO_RESIST_MOD | Damage.NO_CONSUME_ITEM)
                .setNoDmgTicks(0), interval, remaining, name);
    }

    public Damage getDmg() {
        return dmg;
    }

    @Override
    public LivingEntity getOwner() {
        return (LivingEntity) owner;
    }

    @Override
    public void tick() {
        super.tick();
        if (tickTime % interval == 0) {
            dmg.doDmg();    //A com.henryrenyz.creatio.buff will not knockback
            Indicator.newDebuffIndicator(getOwner().getEyeLocation(), dmg.getDamage());
        }
    }
}
