package vip.creatio.clib.modules.buff;

import vip.creatio.clib.modules.customDmg.Damage;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/**
 * For both attribute and damage
 */
public class AttributeDamageBuff extends DamageBuff {

    @Nullable
    protected final AttributeInstance instance;
    protected final AttributeModifier modifier;

    public AttributeDamageBuff(Damage dmg,
                               int interval,
                               int remaining,
                               Attribute attr,
                               AttributeModifier modifier,
                               @Nullable String name) {
        super(dmg, interval, remaining, name);
        this.instance = ((LivingEntity) owner).getAttribute(attr);
        String bufName = getAttributeBuffName();
        if (this.instance != null && modifier != null) {
            this.modifier = new AttributeModifier(modifier.getUniqueId(), bufName, modifier.getAmount(), modifier.getOperation());
            if (instance.getModifiers().stream().parallel().noneMatch(m -> m.getName().equals(bufName)))
                instance.addModifier(modifier);
        } else {
            this.modifier = null;
        }
    }

    public AttributeDamageBuff(LivingEntity entity,
                               float damage,
                               int interval,
                               int remaining,
                               Attribute attr,
                               AttributeModifier modifier,
                               @Nullable String name) {
        this(new Damage(entity, entity, damage,
                Damage.NO_DMG_INDICATOR | Damage.NO_SOUND | Damage.DMG_INVULNERABLE | Damage.NO_EXTERNAL_MOD | Damage.NO_RESIST_MOD)
                .setNoDmgTicks(0), interval, remaining, attr, modifier, name);
    }

    public AttributeDamageBuff(Damage dmg,
                               int interval,
                               int remaining,
                               Attribute attr,
                               double amount,
                               @Nullable String name) {
        super(dmg, interval, remaining, name == null ? "AttributeDamageBuff" : name);
        this.instance = ((LivingEntity) owner).getAttribute(attr);
        String bufName = getAttributeBuffName();
        if (this.instance != null) {
            this.modifier = new AttributeModifier(bufName, amount, AttributeModifier.Operation.ADD_NUMBER);
            // Prevent attribute from same buff from being add multiple times
            if (instance.getModifiers().stream().parallel().noneMatch(m -> m.getName().equals(bufName)))
                instance.addModifier(modifier);
        } else {
            this.modifier = null;
        }
    }

    public AttributeDamageBuff(LivingEntity entity,
                               float damage,
                               int interval,
                               int remaining,
                               Attribute attr,
                               double amount,
                               @Nullable String name) {
        this(new Damage(entity, entity, damage,
                Damage.NO_DMG_INDICATOR | Damage.NO_SOUND | Damage.DMG_INVULNERABLE | Damage.NO_EXTERNAL_MOD | Damage.NO_RESIST_MOD)
                .setNoDmgTicks(0), interval, remaining, attr, amount, name);
    }

    @Override
    public void discard() {
        if (instance != null)
            instance.removeModifier(modifier);
    }
}
