package com.example.antonchik.item;

import com.example.antonchik.Antonchik;
import com.example.antonchik.init.ModPotions;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

/** Whiskey bottle: feeds (and pacifies) the Antonchik mob, or can be drunk by the player. */
public class ItemJameson extends Item
{
    /** Death source for fatal over-drinking; message is keyed by "death.attack.too_drunk". */
    private static final DamageSource TOO_DRUNK =
        new DamageSource("too_drunk").setDamageBypassesArmor().setDamageIsAbsolute();

    public ItemJameson()
    {
        setRegistryName(new ResourceLocation(Antonchik.MODID, "jameson"));
        setUnlocalizedName(Antonchik.MODID + ".jameson");
        setCreativeTab(CreativeTabs.MISC);
        setMaxStackSize(1);
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack)
    {
        return EnumAction.DRINK;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack)
    {
        return 32;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
    {
        player.setActiveHand(hand);
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World world, EntityLivingBase entity)
    {
        if (!(entity instanceof EntityPlayer))
        {
            return stack;
        }

        EntityPlayer player = (EntityPlayer) entity;

        if (!world.isRemote)
        {
            // Stack the existing intoxication; drinking past the lethal BAC kills the player.
            PotionEffect current = player.getActivePotionEffect(ModPotions.DRUNK);
            int duration = (current == null ? 0 : current.getDuration()) + ModPotions.DRINK_DURATION;

            if (ModPotions.promille(duration) > ModPotions.LETHAL_PROMILLE)
            {
                player.attackEntityFrom(TOO_DRUNK, Float.MAX_VALUE);
            }
            else
            {
                player.addPotionEffect(new PotionEffect(
                    ModPotions.DRUNK, duration, ModPotions.amplifierFor(duration), false, false));
            }
        }

        if (!player.capabilities.isCreativeMode)
        {
            stack.shrink(1);
            ItemStack empty = new ItemStack(Items.GLASS_BOTTLE);
            if (stack.isEmpty())
            {
                return empty;
            }
            if (!player.inventory.addItemStackToInventory(empty))
            {
                player.dropItem(empty, false);
            }
        }

        return stack;
    }
}
