package com.example.antonchik.init;

import com.example.antonchik.Antonchik;
import com.example.antonchik.crafting.IngredientPotion;
import com.example.antonchik.entity.EntityAntonMob;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

@Mod.EventBusSubscriber(modid = Antonchik.MODID)
public class RegistryHandler
{
    private static int entityId = 0;

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(ModItems.ANTON_SPAWN_EGG);
        event.getRegistry().register(ModItems.JAMESON);
    }

    @SubscribeEvent
    public static void registerPotions(RegistryEvent.Register<Potion> event)
    {
        event.getRegistry().register(ModPotions.DRUNK);
    }

    /**
     * Jameson recipe: sugar on top of a water bottle (a 1-wide, 2-tall shaped recipe). The water
     * bottle is matched by NBT via {@link IngredientPotion} since it is an NBT-tagged potion item.
     */
    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event)
    {
        ItemStack waterBottle = PotionUtils.addPotionToItemStack(
            new ItemStack(Items.POTIONITEM), PotionTypes.WATER);

        NonNullList<Ingredient> inputs = NonNullList.from(
            Ingredient.EMPTY,
            Ingredient.fromItem(Items.SUGAR),   // top
            new IngredientPotion(waterBottle)); // bottom

        IRecipe recipe = new ShapedRecipes(
            Antonchik.MODID, 1, 2, inputs, new ItemStack(ModItems.JAMESON));
        recipe.setRegistryName(new ResourceLocation(Antonchik.MODID, "jameson"));
        event.getRegistry().register(recipe);
    }

    /**
     * Custom lead handling: right-clicking the (fed) mob with a lead leashes it without consuming
     * the lead, and right-clicking again with a lead unleashes it. The lead always stays in the
     * player's inventory. Cancelling the event prevents vanilla's consume-on-leash. Tying the leash
     * to a fence is unaffected (that is a block interaction) and works as in vanilla.
     */
    @SubscribeEvent
    public static void onLeadInteract(PlayerInteractEvent.EntityInteract event)
    {
        Entity target = event.getTarget();
        if (!(target instanceof EntityAntonMob))
        {
            return;
        }

        ItemStack stack = event.getItemStack();
        if (stack.getItem() != Items.LEAD)
        {
            return;
        }

        // We own all lead interactions on this mob so the lead is never consumed or dropped.
        event.setCanceled(true);
        event.setCancellationResult(EnumActionResult.SUCCESS);

        if (event.getWorld().isRemote)
        {
            return;
        }

        EntityAntonMob anton = (EntityAntonMob) target;
        EntityPlayer player = event.getEntityPlayer();

        if (anton.getLeashed())
        {
            if (anton.getLeashHolder() == player)
            {
                anton.clearLeashed(true, false);
            }
        }
        else if (anton.canBeLeashedTo(player))
        {
            anton.setLeashHolder(player, true);
        }
    }

    @SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityEntry> event)
    {
        event.getRegistry().register(
            EntityEntryBuilder.<EntityAntonMob>create()
                .entity(EntityAntonMob.class)
                .id(new ResourceLocation(Antonchik.MODID, "antonchik"), entityId++)
                .name(Antonchik.MODID + ".antonchik")
                .tracker(64, 1, true)
                // Spawn scattered like creepers: hostile type, small groups (1-2), all biomes.
                .spawn(EnumCreatureType.MONSTER, 30, 1, 2,
                    ForgeRegistries.BIOMES.getValuesCollection())
                .build()
        );
    }
}
