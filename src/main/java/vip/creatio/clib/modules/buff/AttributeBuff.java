package vip.creatio.clib.modules.buff;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Buff thay holds an custom attribute
 */
public class AttributeBuff extends Buff {

    @Nullable
    protected final AttributeInstance instance;
    protected final AttributeModifier modifier;

    public AttributeBuff(LivingEntity owner,
                         int remaining,
                         Attribute attr,
                         AttributeModifier modifier,
                         @Nullable String name) {
        super(owner, remaining, name == null ? "AttributeBuff" : name);
        this.instance = owner.getAttribute(attr);
        String bufName = getAttributeBuffName();
        if (this.instance != null && modifier != null) {
            this.modifier = new AttributeModifier(modifier.getUniqueId(), bufName, modifier.getAmount(), modifier.getOperation());
            if (instance.getModifiers().stream().parallel().noneMatch(m -> m.getName().equals(bufName)))
                instance.addModifier(modifier);
        } else {
            this.modifier = null;
        }
    }

    public AttributeBuff(LivingEntity owner,
                         int remaining,
                         Attribute attr,
                         double amount,
                         @Nullable String name) {
        super(owner, remaining, name == null ? "AttributeBuff" : name);
        this.instance = owner.getAttribute(attr);
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

    public AttributeBuff(LivingEntity owner,
                         int remaining,
                         Attribute attr,
                         double amount) {
        this(owner, remaining, attr, amount, null);
    }

    public AttributeBuff(LivingEntity owner,
                         int remaining,
                         Attribute attr,
                         AttributeModifier modifier) {
        this(owner, remaining, attr, modifier, null);
    }

    @Override
    public void discard() {
        if (instance != null)
            instance.removeModifier(modifier);
    }
}
