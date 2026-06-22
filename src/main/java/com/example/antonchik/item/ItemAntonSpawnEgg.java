package com.example.antonchik.item;

import com.example.antonchik.Antonchik;
import com.example.antonchik.entity.EntityAntonMob;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemAntonSpawnEgg extends Item
{
    public ItemAntonSpawnEgg()
    {
        setRegistryName(new ResourceLocation(Antonchik.MODID, "anton_spawn_egg"));
        setUnlocalizedName(Antonchik.MODID + ".anton_spawn_egg");
        setCreativeTab(CreativeTabs.MISC);
        setMaxStackSize(64);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
                                      EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if (!world.isRemote)
        {
            BlockPos spawnPos = pos.offset(facing);
            EntityAntonMob mob = new EntityAntonMob(world);
            mob.setLocationAndAngles(
                spawnPos.getX() + 0.5D,
                spawnPos.getY(),
                spawnPos.getZ() + 0.5D,
                world.rand.nextFloat() * 360.0F,
                0.0F
            );
            world.spawnEntity(mob);

            if (!player.capabilities.isCreativeMode)
            {
                player.getHeldItem(hand).shrink(1);
            }
        }

        return EnumActionResult.SUCCESS;
    }
}
