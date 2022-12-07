package mustapelto.deepmoblearning.common.entities;

import mustapelto.deepmoblearning.DMLRelearned;
import mustapelto.deepmoblearning.common.ServerProxy;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class EntityGlitchOrb extends EntityFireball {

    private float damage = 1.6F;

    @SuppressWarnings("unused")
    public EntityGlitchOrb(World worldIn) {
        super(worldIn);
    }

    public EntityGlitchOrb(World worldIn, EntityLivingBase shooter, double accelX, double accelY, double accelZ, boolean empowered) {
        super(worldIn, shooter, accelX, accelY, accelZ);
        if(empowered) {
            damage = 2.8F;
        }
    }

    @Override
    public void onUpdate() {
        if (!world.isRemote) {
            setFlag(6, isGlowing());

            List<Entity> entities = world.getEntitiesWithinAABBExcludingEntity(this, getEntityBoundingBox().grow(1.0D));

            entities.forEach(entity -> {
                if (entity instanceof EntityPlayer) {
                    onImpact((EntityPlayer) entity);
                }
            });
        }

        if (world.isRemote || (shootingEntity == null || !shootingEntity.isDead) && world.isBlockLoaded(new BlockPos(this))) {
            this.posX += motionX;
            this.posY += motionY;
            this.posZ += motionZ;
            ProjectileHelper.rotateTowardsMovement(this, 0.055F);

            // Motion multiplier
            float f = 1.11F;
            if (isInWater()) {
                for (int i = 0; i < 4; ++i) {
                    //noinspection RedundantArrayCreation
                    world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, posX - motionX * 0.25D, posY - motionY * 0.25D, posZ - motionZ * 0.25D, motionX, motionY, motionZ, new int[0]);
                }
                f = 0.8F;
            }

            this.motionX += accelerationX;
            this.motionY += accelerationY;
            this.motionZ += accelerationZ;
            this.motionX *= f;
            this.motionY *= f;
            this.motionZ *= f;

            ThreadLocalRandom rand = ThreadLocalRandom.current();

            DMLRelearned.proxy.spawnSmokeParticle(world,
                    posX + rand.nextDouble(-0.5D, 0.5D),
                    posY  + rand.nextDouble(-0.1D, 0.1D),
                    posZ + rand.nextDouble(-0.5D, 0.5D),
                    rand.nextDouble(-0.08D, 0.08D),
                    rand.nextDouble(-0.08D, 0),
                    rand.nextDouble(-0.08D, 0.08D),
                    ServerProxy.SmokeType.SMOKE
            );
            setPosition(posX, posY, posZ);
        }

        if (ticksExisted > 400){
            setDead();
        }
        onEntityUpdate();
    }

    private void onImpact(EntityPlayer entity) {
        if (!world.isRemote) {
            entity.attackEntityFrom(DamageSource.causeIndirectMagicDamage(this, shootingEntity), damage);
            //noinspection ConstantConditions
            entity.addPotionEffect(new PotionEffect(Potion.getPotionById(20), 40, 1));
            setDead();
        }
    }

    @Override
    protected void onImpact(RayTraceResult rayTraceResult) {
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    protected boolean isFireballFiery() {
        return false;
    }
}
