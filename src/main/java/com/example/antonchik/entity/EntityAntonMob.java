package com.example.antonchik.entity;

import com.example.antonchik.init.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class EntityAntonMob extends EntityCreature
{
    /** True once the mob has been fed a Jameson: it stops being hostile and can be leashed. */
    private static final DataParameter<Boolean> FED =
        EntityDataManager.createKey(EntityAntonMob.class, DataSerializers.BOOLEAN);

    public EntityAntonMob(World world)
    {
        super(world);
        // Doubled hitbox (was 0.6 x 1.8) to match the 2x model scale.
        setSize(1.2F, 3.6F);
    }

    @Override
    protected void entityInit()
    {
        super.entityInit();
        dataManager.register(FED, Boolean.FALSE);
    }

    public boolean isFed()
    {
        return dataManager.get(FED);
    }

    public void setFed(boolean fed)
    {
        dataManager.set(FED, fed);
    }

    @Override
    protected void initEntityAI()
    {
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(1, new EntityAIAttackMelee(this, 1.2D, true));
        tasks.addTask(2, new EntityAIWander(this, 1.0D));
        tasks.addTask(3, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        tasks.addTask(4, new EntityAILookIdle(this));

        // Hostile to players only while not yet fed.
        targetTasks.addTask(1, new EntityAINearestAttackableTarget<EntityPlayer>(
            this, EntityPlayer.class, 10, true, false, player -> !isFed()));

        // Once fed (tamed), defend players by attacking any hostile mob that is targeting one.
        targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityMob>(
            this, EntityMob.class, 10, true, false,
            mob -> isFed() && mob.getAttackTarget() instanceof EntityPlayer));
    }

    @Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0D);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(3.5D);
    }

    @Override
    public void onLivingUpdate()
    {
        super.onLivingUpdate();

        // Once fed, slowly regenerate health (1 HP every 2 seconds) until full.
        if (!world.isRemote && isFed() && isEntityAlive()
            && getHealth() < getMaxHealth() && ticksExisted % 40 == 0)
        {
            heal(1.0F);
        }
    }

    /** Burst of hearts shown to all nearby clients when the mob is fed. */
    private void spawnFeedParticles()
    {
        if (!(world instanceof WorldServer))
        {
            return;
        }

        WorldServer server = (WorldServer) world;
        for (int i = 0; i < 7; i++)
        {
            double dx = rand.nextGaussian() * 0.02D;
            double dy = rand.nextGaussian() * 0.02D;
            double dz = rand.nextGaussian() * 0.02D;
            server.spawnParticle(EnumParticleTypes.HEART,
                posX + (rand.nextFloat() * width * 2.0F) - width,
                posY + 0.5D + (rand.nextFloat() * height),
                posZ + (rand.nextFloat() * width * 2.0F) - width,
                1, dx, dy, dz, 0.0D);
        }
    }

    /**
     * Spawn like a hostile mob (creeper-style): only in the dark and not on peaceful, instead of
     * the unconditional animal-style spawning of {@link EntityCreature}.
     */
    @Override
    public boolean getCanSpawnHere()
    {
        return world.getDifficulty() != EnumDifficulty.PEACEFUL
            && isValidLightLevel()
            && super.getCanSpawnHere();
    }

    /** Mirrors EntityMob: requires low light at the spawn position. */
    protected boolean isValidLightLevel()
    {
        BlockPos pos = new BlockPos(posX, getEntityBoundingBox().minY, posZ);

        if (world.getLightFor(EnumSkyBlock.SKY, pos) > rand.nextInt(32))
        {
            return false;
        }

        int light = world.getLightFromNeighbors(pos);
        if (world.isThundering())
        {
            int prev = world.getSkylightSubtracted();
            world.setSkylightSubtracted(10);
            light = world.getLightFromNeighbors(pos);
            world.setSkylightSubtracted(prev);
        }

        return light <= rand.nextInt(8);
    }

    @Override
    public boolean attackEntityAsMob(Entity target)
    {
        float damage = (float) getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
        return target.attackEntityFrom(DamageSource.causeMobDamage(this), damage);
    }

    @Override
    protected boolean processInteract(EntityPlayer player, EnumHand hand)
    {
        ItemStack stack = player.getHeldItem(hand);

        if (stack.getItem() == ModItems.JAMESON)
        {
            boolean canPacify = !isFed();
            boolean canHeal = getHealth() < getMaxHealth();

            // Treating with Jameson pacifies the mob (first time) and fully heals it (every time).
            // Only spend a bottle when there is something to do, so it is never wasted on a fed,
            // full-health mob -- but always consume the interaction (return true) regardless, so
            // right-clicking the mob never falls through to the player drinking the Jameson.
            if ((canPacify || canHeal) && !world.isRemote)
            {
                if (canPacify)
                {
                    setFed(true);
                    setAttackTarget(null);
                }
                setHealth(getMaxHealth());
                playSound(SoundEvents.ENTITY_GENERIC_DRINK, 1.0F, 1.0F);
                spawnFeedParticles();

                if (!player.capabilities.isCreativeMode)
                {
                    stack.shrink(1);
                }
            }
            return true;
        }

        // Leads are handled by the (final) processInitialInteract before this is called;
        // leashing is gated by canBeLeashedTo, which only allows it once fed.
        return super.processInteract(player, hand);
    }

    @Override
    public boolean canBeLeashedTo(EntityPlayer player)
    {
        return isFed() && !getLeashed();
    }

    /**
     * Never drop a lead when unleashed: the lead is kept in the player's inventory the whole time
     * (it is never consumed on attach — see the interaction handler), so dropping one would dupe it.
     */
    @Override
    public void clearLeashed(boolean sendPacket, boolean dropLead)
    {
        super.clearLeashed(sendPacket, false);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound)
    {
        super.writeEntityToNBT(compound);
        compound.setBoolean("Fed", isFed());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound)
    {
        super.readEntityFromNBT(compound);
        setFed(compound.getBoolean("Fed"));
    }
}
