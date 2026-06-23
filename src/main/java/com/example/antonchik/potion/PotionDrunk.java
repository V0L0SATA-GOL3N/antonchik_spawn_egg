package com.example.antonchik.potion;

import com.example.antonchik.Antonchik;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;

/**
 * Intoxication effect applied by drinking Jameson. Slows the drinker (more the higher the BAC,
 * encoded in the amplifier) and keeps vanilla nausea topped up for the drunken screen wobble.
 * The remaining duration doubles as both the "sober up" timer and the blood-alcohol readout, so
 * it is synced to the client for free via vanilla's active-effect packets.
 */
public class PotionDrunk extends Potion
{
    /** Stable UUID for the movement-speed modifier so it can be added/removed cleanly. */
    private static final String SPEED_MODIFIER_UUID = "b9766b59-9566-4402-bc1f-2ee2a276d836";

    public PotionDrunk()
    {
        super(true, 0x6B3F18); // bad effect, whiskey-brown liquid colour
        setRegistryName(new ResourceLocation(Antonchik.MODID, "drunk"));
        setPotionName("effect.antonchik.drunk");
        // Multiplicative slowdown (operation 2); vanilla scales the amount by (amplifier + 1).
        registerPotionAttributeModifier(
            SharedMonsterAttributes.MOVEMENT_SPEED, SPEED_MODIFIER_UUID, -0.10D, 2);
    }

    /** We render our own HUD readout, so suppress the default (and texture-less) status icon. */
    @Override
    public boolean hasStatusIcon()
    {
        return false;
    }

    @Override
    public boolean isReady(int duration, int amplifier)
    {
        return true; // run performEffect every tick
    }

    @Override
    public void performEffect(EntityLivingBase entity, int amplifier)
    {
        // Keep the screen-warping nausea topped up for as long as the drinker stays drunk.
        PotionEffect nausea = entity.getActivePotionEffect(MobEffects.NAUSEA);
        if (nausea == null || nausea.getDuration() < 40)
        {
            entity.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 100, 0, false, false));
        }
    }
}
