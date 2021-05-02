package vip.creatio.clib.modules.customDmg;

import vip.creatio.basic.util.EntityUtil;
import vip.creatio.basic.util.ItemUtil;
import vip.creatio.clib.modules.util.LocationUtil;
import vip.creatio.common.util.Mth;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("unused")
public final class Damage {

    public static final short NO_GLOBAL_MOD =           0x0001;
    public static final short NO_BLOCKING_MOD =         0x0002;
    public static final short NO_ARMOR_MOD =            0x0004;
    public static final short NO_RESIST_MOD =           0x0008;
    public static final short NO_MAGIC_MOD =            0x0010;
    public static final short NO_ABSORB_MOD =           0x0020;
    public static final short NO_MOD =                  0x003F;
    public static final short DMG_INVULNERABLE =        0x0040;
    public static final short NO_KNOCKBACK =            0x0080;
    public static final short RESET_COOLDOWN =          0x0100;
    public static final short NO_CONSUME_ITEM =         0x0200;     // will not consume item durability.
    public static final short NO_DMG_INDICATOR =        0x0400;
    public static final short NO_SOUND =                0x0800;
    public static final short NO_EXTERNAL_MOD =         NO_BLOCKING_MOD | NO_ARMOR_MOD | NO_MAGIC_MOD;

    private static final int GLOBAL_MOD =               0;
    private static final int BLOCKING_MOD =             1;
    private static final int ARMOR_MOD =                2;
    private static final int RESIST_MOD =               3;
    private static final int MAGIC_MOD =                4;
    private static final int ABSORB_MOD =               5;

    private final LivingEntity victim;
    private final @Nullable Entity attacker;

    private short modifiers;
    private final float[] modifier = new float[6];
    private float damage;
    private int noDmgTicks;
    private int itemConsumeRate = 100;

    private EntityDamageEvent.DamageCause cause;

    private Function<Damage, Float> GlobalModifier;
    private Function<Damage, Float> BlockingModifier;
    private Function<Damage, Float> ArmorModifier;
    private Function<Damage, Float> ResistanceModifier;
    private Function<Damage, Float> MagicModifier;
    private Function<Damage, Float> AbsorptionModifier;

    private Consumer<Damage> onDamageDealt;
    private Consumer<Damage> onVictimKilled;
    private Consumer<Damage> onKnockback;


    public Damage(@NotNull LivingEntity victim, @Nullable Entity attacker, float damage, int noDmgTicks, @Nullable EntityDamageEvent.DamageCause cause, int modifiers) {
        this.victim = victim;
        this.attacker = attacker;
        this.damage = damage;
        this.noDmgTicks = noDmgTicks;
        this.cause = cause == null ? EntityDamageEvent.DamageCause.ENTITY_ATTACK : cause;
        this.modifiers = (short) modifiers;
        calcDmgImmunity();
    }

    public Damage(@NotNull LivingEntity victim, @Nullable Entity attacker, float damage, int modifiers) {
        this.victim = victim;
        this.attacker = attacker;
        this.damage = damage;
        this.modifiers = (short) modifiers;
        this.cause = EntityDamageEvent.DamageCause.ENTITY_ATTACK;
        this.noDmgTicks = 10;
        calcDmgImmunity();
    }

    public Damage(@NotNull LivingEntity victim, @Nullable Entity attacker, float damage) {
        this.victim = victim;
        this.attacker = attacker;
        this.damage = damage;
        this.modifiers = 0x0000;
        this.cause = EntityDamageEvent.DamageCause.ENTITY_ATTACK;
        this.noDmgTicks = 10;
        calcDmgImmunity();
    }

    private boolean applyBlockingModifier() {
        boolean flag = false;
        if (attacker instanceof Arrow) {
            if (((Arrow) attacker).getPierceLevel() > 0) {
                flag = true;
            }
        }

        if (EntityUtil.isBlocking(victim) && !flag && attacker != null) {
            Vector vec = attacker.getLocation().toVector();
            Vector vec1 = EntityUtil.getViewVector(victim, 1F);
            Vector vec2 = victim.getLocation().toVector().subtract(vec).normalize();
            vec2 = new Vector(vec2.getX(), 0.0D, vec2.getZ());
            return vec2.dot(vec1) < 0.0D;
        }

        return false;
    }

    private static float calcArmorImmunity0(float damage, float strength, float toughness) {
        float var1 = 2.0F + toughness / 4.0F;
        float var2 = Mth.clamp(strength - damage / var1, strength * 0.2F, 20F);
        return damage * var2 / 25.0F;
    }
    private float applyArmorModifier(float dmg) {
        return calcArmorImmunity0(damage,
                (float) EntityUtil.getAttributeValue(Attribute.GENERIC_ARMOR, victim),
                (float) EntityUtil.getAttributeValue(Attribute.GENERIC_ARMOR_TOUGHNESS, victim));
    }

    private float applyMagicModifier(float damage) {
        if (damage <= 0.0F) {
            return 0.0F;
        } else {
            int i = 0;
            if (victim.getEquipment() == null) return damage;
            for (ItemStack item : victim.getEquipment().getArmorContents())
                if (item != null)
                    i += item.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL);
            if (i > 0) {
                float vars = Mth.clamp(damage, 0.0F, 20.0F);
                damage = i * (1.0F - vars / 25.0F);
            }
            return damage;
        }
    }

    private void calcDmgImmunity() {

        if (victim.isInvulnerable() && !damageInvulnerable()) return;

        float damage = this.damage;

        // Global modifier
        if (!noGlobalModifier()) {
            float globalModifier = (GlobalModifier == null) ? 0f : GlobalModifier.apply(this);
            modifier[GLOBAL_MOD] = globalModifier;
            damage += globalModifier;
        }

        // Get Shield blocking modifier, which could consume all the damages.
        if (!noBlockingModifier()) {
            Function<Float, Float> blocking = f -> -(applyBlockingModifier() ? f : 0.0F);
            float blockingModifier = (BlockingModifier == null)
                    ? blocking.apply(damage)
                    : BlockingModifier.apply(this);
            modifier[BLOCKING_MOD] = blockingModifier;
            damage += blockingModifier;
        }

        // Armor modifier
        if (!noArmorModifier()) {
            Function<Float, Float> armor = f -> -applyArmorModifier(f);
            float armorModifier = (ArmorModifier == null)
                    ? armor.apply(damage)
                    : ArmorModifier.apply(this);
            modifier[ARMOR_MOD] = armorModifier;
            damage += armorModifier;
        }

        // Resistance effect modifier
        if (!noResistanceModifier()) {
            Function<Float, Float> resistance = f -> {
                if (victim.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE)) {
                    int i = (EntityUtil.getPotionEffect(PotionEffectType.DAMAGE_RESISTANCE, victim) + 1) * 5;
                    int j = 25 - i;
                    float f1 = f * j;
                    return -(f - f1 / 25.0F);
                } else {
                    return -0.0F;
                }
            };
            float resistanceModifier = (ResistanceModifier == null)
                    ? resistance.apply(damage)
                    : ResistanceModifier.apply(this);
            modifier[RESIST_MOD] = resistanceModifier;
            damage += resistanceModifier;
        }

        // Magic and enchantment modifier
        if (!noMagicModifier()) {
            Function<Float, Float> magic = f -> -(f - applyMagicModifier(f));
            float magicModifier = (MagicModifier == null)
                    ? magic.apply(damage)
                    : MagicModifier.apply(this);
            modifier[MAGIC_MOD] = magicModifier;
            damage += magicModifier;
        }

        // Absorption modifier
        if (!noAbsorptionModifier()) {
            Function<Float, Float> absorption = f -> (float) -Math.max(f - Math.max(f - victim.getAbsorptionAmount(), 0.0D), 0.0D);
            float absorptionModifier = (AbsorptionModifier == null)
                    ? absorption.apply(damage)
                    : AbsorptionModifier.apply(this);
            modifier[ABSORB_MOD] = absorptionModifier;
        }
    }

    public Damage doDmg() {

        float finalDamage = getFinalDamage();
        boolean isPlayer = victim instanceof Player;

        if (!damageInvulnerable() && victim.isInvulnerable()) {
            return this;
        }

        if (victim.isDead()) return this;

        if (isPlayer) {
            if (EntityUtil.INVULNERABLE_MODE.contains(((Player) victim).getGameMode())) return this;
        }

        CustomDamageEvent event = new CustomDamageEvent(this);
        event.setDamage(damage);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return this;

        // reset attack cooldown
        if (willResetCooldown() && isPlayer) {
            EntityUtil.resetAttackCooldown((Player) victim);
        }

        float temp;
        // Player dmg block resistance effect statistic
        if (modifier[RESIST_MOD] < 0.0F) {
            temp = -modifier[RESIST_MOD];
            if (temp > 0.0F && temp < 3.4028235E37F) {
                if (isPlayer) {
                    ((Player) victim).incrementStatistic(Statistic.DAMAGE_RESISTED, Math.round(temp * 10.0F));
                } else if (attacker instanceof Player) {
                    ((Player) attacker).incrementStatistic(Statistic.DAMAGE_DEALT_RESISTED, Math.round(temp * 10.0F));
                }
            }
        }

        // damage armor item
        if (!noItemDurabilityConsuming()) {
            temp = finalDamage + modifier[GLOBAL_MOD] + modifier[BLOCKING_MOD];
            EntityUtil.damageArmor(victim, temp);
        }

        // damage shield
        if (modifier[BLOCKING_MOD] < 0.0F && isPlayer && !noItemDurabilityConsuming()) {
            victim.playEffect(EntityEffect.SHIELD_BLOCK);

            EntityUtil.damageShield((Player) victim, modifier[BLOCKING_MOD]);
            if (attacker instanceof LivingEntity) {
                EntityUtil.shieldBlock(victim, (LivingEntity) attacker);
            }
        }

        // absorption
        temp = -modifier[ABSORB_MOD];
        victim.setAbsorptionAmount(Math.max(victim.getAbsorptionAmount() - temp, 0.0F));
        if (temp > 0.0F && temp < 3.4028235E37F && isPlayer) {
            ((Player) victim).incrementStatistic(Statistic.DAMAGE_ABSORBED, Math.round(temp * 10F));
        }

        if (temp > 0.0F && temp < 3.4028235E37F && attacker instanceof Player) {
            ((Player) attacker).incrementStatistic(Statistic.DAMAGE_DEALT_ABSORBED, Math.round(temp * 10F));
        }

        if (onDamageDealt != null) onDamageDealt.accept(this);

        double after = Math.max(0, victim.getHealth() - finalDamage);

        if (finalDamage == 0) {
            return this;
        }

        victim.setHealth(after);
        victim.playEffect(EntityEffect.HURT);
        if (!noSoundEffect()) EntityUtil.playHurtSound(victim);
        victim.setLastDamage(finalDamage);
        victim.setLastDamageCause(event);
        if (victim.getNoDamageTicks() < noDmgTicks)
            victim.setNoDamageTicks(noDmgTicks);
        if (!(victim instanceof Player)) {
            double amount = victim.getAbsorptionAmount() - finalDamage;
            victim.setAbsorptionAmount(amount < 0 ? 0 : amount);
        }

        return this;
    }

    //private static final Vector KB_CONSTANT = new Vector(0.6D, 1.0D, 0.6D);
    public Damage doKnockback() {
        // Default knockback func
        if (onKnockback == null) {
            if (attacker instanceof LivingEntity) {
                LivingEntity e = (LivingEntity) attacker;
                float k = 0;
                k += EntityUtil.getAttributeValue(Attribute.GENERIC_ATTACK_KNOCKBACK, e);
                k += EntityUtil.getKnockbackBonus(e);
                doKnockback(Math.max(k, 0.5F));

                //TODO: Shield blocking incomplete
                if (victim instanceof HumanEntity) {
                    if (e.getEquipment() != null) {
                        maybeDisableShield((HumanEntity) victim, e.getEquipment().getItemInMainHand(),
                                EntityUtil.isHandRaised(victim) ? EntityUtil.getActiveItem(victim) : ItemUtil.NULL);
                    }
                }
            }
        } else {
            onKnockback.accept(this);
        }
        return this;
    }

    public Damage doKnockback(float kbStrength, Vector dir) {
        double dist = dir.length();
        EntityUtil.knockback(victim, kbStrength, -dir.getX() / dist,
                -dir.getZ() / dist);

        //TODO: Should upgrade
        if (attacker instanceof HumanEntity) {
            HumanEntity h = (HumanEntity) attacker;

            ItemStack active = EntityUtil.getActiveItem(h);

            if (active.getEnchantments().size() > 0)
                victim.getWorld().spawnParticle(Particle.CRIT_MAGIC, victim.getEyeLocation(),
                        10,0.3, 0.3, 0.3, 0.3);
            else
                victim.getWorld().spawnParticle(Particle.CRIT, victim.getEyeLocation(),
                        10,0.3, 0.3, 0.3, 0.3);

            //victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 1, 1);

            if (active.getEnchantmentLevel(Enchantment.SWEEPING_EDGE) > 0) {
                victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1, 1);
                victim.getWorld().spawnParticle(Particle.SWEEP_ATTACK, LocationUtil.localCoords(victim.getEyeLocation(), 0, 0, 0.5D),
                        1,0, 0, 0, 0.5);
            }
        }
        return this;
    }

    public Damage doKnockback(float kbStrength, Location from) {
        Vector vec = victim.getLocation().subtract(from).toVector();
        return doKnockback(kbStrength, vec);
    }

    public Damage doKnockback(float kbStrength) {
        if (attacker instanceof LivingEntity) {
            return doKnockback(kbStrength,attacker.getLocation().getDirection());
        }
        return this;
    }

    private void maybeDisableShield(HumanEntity human, ItemStack attackingItem, ItemStack blockingItem) {
        if (attacker instanceof LivingEntity) {
            if (       attackingItem.getType() != Material.AIR
                    && blockingItem.getType() != Material.AIR
                    && ItemUtil.isAxe(attackingItem.getType())
                    && blockingItem.getType() == Material.SHIELD) {
                LivingEntity e = (LivingEntity) attacker;

                float f = 0.25F +
                        EntityUtil.getHighestEnchantmentLevel(Enchantment.DIG_SPEED, e) * 0.05F;

                if (EntityUtil.getRandom(e).nextFloat() < f) {
                    human.setCooldown(Material.SHIELD, 100);
                    human.playEffect(EntityEffect.SHIELD_BLOCK);
                }
            }
        }
    }

    public LivingEntity getVictim() {
        return victim;
    }

    public @Nullable Entity getAttacker() {
        return attacker;
    }

    public float getDamage() {
        return damage;
    }

    public Damage setDamage(float damage) {
        this.damage = damage;
        return this;
    }

    public float getFinalDamage() {
        float dmg = this.damage;
        for (float f : modifier) {
            dmg += f;
        }
        return dmg;
    }

    public Function<Damage, Float> getGlobalModifier() {
        return GlobalModifier;
    }

    public Damage setGlobalModifier(@NotNull Function<Damage, Float> globalModifier) {
        GlobalModifier = globalModifier;
        return this;
    }

    public Function<Damage, Float> getBlockingModifier() {
        return BlockingModifier;
    }

    public Damage setBlockingModifier(@NotNull Function<Damage, Float> blockingModifier) {
        BlockingModifier = blockingModifier;
        return this;
    }

    public Function<Damage, Float> getArmorModifier() {
        return ArmorModifier;
    }

    public Damage setArmorModifier(@NotNull Function<Damage, Float> armorModifier) {
        ArmorModifier = armorModifier;
        return this;
    }

    public Function<Damage, Float> getResistanceModifier() {
        return ResistanceModifier;
    }

    public Damage setResistanceModifier(@NotNull Function<Damage, Float> resistanceModifier) {
        ResistanceModifier = resistanceModifier;
        return this;
    }

    public Function<Damage, Float> getMagicModifier() {
        return MagicModifier;
    }

    public Damage setMagicModifier(@NotNull Function<Damage, Float> magicModifier) {
        MagicModifier = magicModifier;
        return this;
    }

    public Function<Damage, Float> getAbsorptionModifier() {
        return AbsorptionModifier;
    }

    public Damage setAbsorptionModifier(@NotNull Function<Damage, Float> absorptionModifier) {
        AbsorptionModifier = absorptionModifier;
        return this;
    }

    public Consumer<Damage> getOnVictimKilled() {
        return onVictimKilled;
    }

    public void setOnVictimKilled(@NotNull Consumer<Damage> onVictimKilled) {
        this.onVictimKilled = onVictimKilled;
    }

    public EntityDamageEvent.DamageCause getCause() {
        return cause;
    }

    public Damage setCause(EntityDamageEvent.DamageCause cause) {
        this.cause = cause;
        return this;
    }

    public int getNoDmgTicks() {
        return noDmgTicks;
    }

    public Damage setNoDmgTicks(int noDmgTicks) {
        this.noDmgTicks = noDmgTicks;
        return this;
    }

    public short getModifiers() {
        return modifiers;
    }

    public Damage setModifiers(short modifiers) {
        this.modifiers = modifiers;
        return this;
    }

    public Consumer<Damage> getOnKnockback() {
        return onKnockback;
    }

    public void setOnKnockback(Consumer<Damage> onKnockback) {
        this.onKnockback = onKnockback;
    }

    public int getItemConsumeRate() {
        return itemConsumeRate;
    }

    public void setItemConsumeRate(int itemConsumeRate /* 0 ~ 100 (%) */) {
        this.itemConsumeRate = Mth.clamp(itemConsumeRate, 0, 100);;
    }

    public boolean noGlobalModifier() {
        return (modifiers & NO_GLOBAL_MOD) != 0;
    }

    public boolean noBlockingModifier() {
        return (modifiers & NO_BLOCKING_MOD) != 0;
    }

    public boolean noArmorModifier() {
        return (modifiers & NO_ARMOR_MOD) != 0;
    }

    public boolean noResistanceModifier() {
        return (modifiers & NO_RESIST_MOD) != 0;
    }

    public boolean noMagicModifier() {
        return (modifiers & NO_MAGIC_MOD) != 0;
    }

    public boolean noAbsorptionModifier() {
        return (modifiers & NO_ABSORB_MOD) != 0;
    }

    public boolean damageInvulnerable() {
        return (modifiers & DMG_INVULNERABLE) != 0;
    }

    public boolean noKnockback() {
        return (modifiers & NO_KNOCKBACK) != 0;
    }

    public boolean noItemDurabilityConsuming() {
        return (modifiers & NO_CONSUME_ITEM) != 0;
    }

    public boolean willResetCooldown() {
        return (modifiers & RESET_COOLDOWN) != 0;
    }

    public boolean noDamageIndicator() {
        return (modifiers & NO_DMG_INDICATOR) != 0;
    }

    public boolean noSoundEffect() {
        return (modifiers & NO_SOUND) != 0;
    }

    public Consumer<Damage> getOnDamageDealt() {
        return onDamageDealt;
    }

    public void setOnDamageDealt(Consumer<Damage> onDealt) {
        this.onDamageDealt = onDealt;
    }

    /** For testing only */
    @Deprecated
    public static void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event instanceof CustomDamageEvent)) {
            boolean shouldProc = false;
            /*if (event.getDamager() instanceof Projectile) {
                Projectile p = (Projectile) event.getDamager();
                if (p.getShooter() instanceof HumanEntity)
                    shouldProc = true;
            }*/
            if (event.getDamager() instanceof HumanEntity) {
                shouldProc = true;
            }

            if (shouldProc) {
                Damage dmg = new Damage((LivingEntity) event.getEntity(), event.getDamager(), (float) event.getDamage());
                dmg.setCause(event.getCause());
                event.setCancelled(true);

                dmg.doDmg();
                dmg.doKnockback();
            }
        }
    }
}
