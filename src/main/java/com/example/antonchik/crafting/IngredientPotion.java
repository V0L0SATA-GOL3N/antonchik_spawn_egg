package com.example.antonchik.crafting;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.crafting.IngredientNBT;

/**
 * Crafting ingredient that matches a specific potion ItemStack by its NBT (e.g. a water bottle).
 * Vanilla JSON ingredients match only item + metadata, which is not enough to distinguish a water
 * bottle from other potions, so we reuse Forge's NBT-comparing ingredient.
 */
public class IngredientPotion extends IngredientNBT
{
    public IngredientPotion(ItemStack stack)
    {
        super(stack);
    }
}
