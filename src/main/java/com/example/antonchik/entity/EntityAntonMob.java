package com.example.antonchik.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class EntityAntonMob extends EntityCreature
{
    /** True once the mob has been fed a water bottle: it stops being hostile and can be leashed. */
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
    }

    @Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(3.0D);
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

        if (!isFed() && stack.getItem() == Items.POTIONITEM
            && PotionUtils.getPotionFromItem(stack) == PotionTypes.WATER)
        {
            if (!world.isRemote)
            {
                setFed(true);
                setAttackTarget(null);
                playSound(SoundEvents.ENTITY_GENERIC_DRINK, 1.0F, 1.0F);

                if (!player.capabilities.isCreativeMode)
                {
                    stack.shrink(1);
                    ItemStack empty = new ItemStack(Items.GLASS_BOTTLE);
                    if (stack.isEmpty())
                    {
                        player.setHeldItem(hand, empty);
                    }
                    else if (!player.inventory.addItemStackToInventory(empty))
                    {
                        player.dropItem(empty, false);
                    }
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
