package com.example.antonchik.init;

import com.example.antonchik.potion.PotionDrunk;
import net.minecraft.potion.Potion;

public class ModPotions
{
    public static final Potion DRUNK = new PotionDrunk();

    /** Ticks of drunkenness added per Jameson. */
    public static final int DRINK_DURATION = 1200;   // 60s per drink

    /** Drinking past this blood-alcohol level (per mille) is fatal. */
    public static final double LETHAL_PROMILLE = 0.8D;

    /** Blood-alcohol content (per mille) represented by the given remaining drunk duration. */
    public static double promille(int durationTicks)
    {
        return durationTicks / 2400.0D;
    }

    /** Amplifier (severity of slowness) for the given remaining drunk duration. */
    public static int amplifierFor(int durationTicks)
    {
        return Math.min((durationTicks - 1) / DRINK_DURATION, 4);
    }
}
