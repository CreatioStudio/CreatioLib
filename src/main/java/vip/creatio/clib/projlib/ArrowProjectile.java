package vip.creatio.clib.projlib;

import vip.creatio.clib.modules.Tags;
import vip.creatio.clib.modules.customDmg.Damage;
import vip.creatio.basic.util.EntityUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class ArrowProjectile extends CraftProjectile {

    private static final ItemStack DEFAULT_CRACK = new ItemStack(Material.OAK_PLANKS);

    private final Arrow arrow;

    public ArrowProjectile(@NotNull Location loc,
                           Arrow arrow,
                           double hitBox,
                           int livingTime,
                           double gravityAcc /* in sec*/,
                           Vector initVel) {
        super(  loc,
                hitBox,
                hitBox,
                hitBox,
                livingTime,
                new ArrowOrbit(initVel.clone(), gravityAcc, arrow),
                true,
                true);
        this.arrow = arrow;
        arrow.setGravity(false);
        arrow.setPortalCooldown(Integer.MAX_VALUE);
        arrow.setInvulnerable(true);
        arrow.addScoreboardTag(Tags.CUSTOM);
        arrow.addScoreboardTag(Tags.PROJECTILE);
    }

    public ArrowProjectile(@NotNull Location loc, Arrow arrow, float dmg, Vector initVel) {
        this(loc, arrow, 0.25D, 1200, 1, initVel);
    }

    public ArrowProjectile(@NotNull Location loc, float dmg, Vector initVel) {
        this(loc, loc.getWorld().spawnArrow(loc, initVel, (float) initVel.length(), 0F), dmg, initVel);
    }

    @Override
    protected void onTick() {
        EntityUtil.setLoc(arrow, getLoc());
        EntityUtil.setMot(arrow, getVelocity());
    }

    @Override
    protected void onHit(LivingEntity e) {
        EntityUtil.setLoc(arrow, getLoc());
        arrowHit();
        new Damage(e, arrow, 10F).doDmg().doKnockback(0.5F, getLoc());
    }

    @Override
    protected void onHitGround(Block b) {
        arrowHit();
    }

    private void arrowHit() {
        this.getWorld().playSound(this.getLoc(), Sound.BLOCK_WOOD_BREAK, 1.5F, 1.5F);
        this.getWorld().playSound(this.getLoc(), Sound.ENTITY_ARROW_HIT, 1F, 2F);
        this.getWorld().spawnParticle(Particle.ITEM_CRACK, this.getLoc(), 15, 0.2D, 0.2D, 0.2D,
                Math.max(0.1D, this.getVelocity().length() / 10D), DEFAULT_CRACK, true);
        this.discard();
    }

    @Override
    protected void onDiscard() {
        arrow.remove();
    }

    private static class ArrowOrbit implements OrbitUnit {

        private final Arrow arrow;
        private final Vector prevVec;
        private final Vector acc;

        private ArrowOrbit(Vector init, double acc, Arrow arrow) {
            this.prevVec = init.add(new Vector(0, acc / 10D, 0));
            this.acc = new Vector(0D, acc / 20D, 0D);
            this.arrow = arrow;
        }

        @Override
        public Vector nextLocOffset() {
            if (arrow.isInWater()) {
                return prevVec.multiply(0.9D).subtract(acc);
            }
            return prevVec.multiply(0.9995D).subtract(acc);
        }
    }






}
