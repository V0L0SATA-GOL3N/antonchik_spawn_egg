package com.example.antonchik.proxy;

import com.example.antonchik.init.ModPotions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Draws the blood-alcohol (per mille) readout and sober-up timer while the player is drunk. */
@SideOnly(Side.CLIENT)
public class ClientEventHandler
{
    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event)
    {
        if (event.getType() != RenderGameOverlayEvent.ElementType.TEXT)
        {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;
        if (player == null)
        {
            return;
        }

        PotionEffect effect = player.getActivePotionEffect(ModPotions.DRUNK);
        if (effect == null)
        {
            return;
        }

        int ticks = effect.getDuration();
        double promille = ModPotions.promille(ticks);
        int seconds = ticks / 20;

        String bac = String.format("§6BAC: %.2f‰", promille);
        String timer = String.format("§eDrunk: %d:%02d", seconds / 60, seconds % 60);

        FontRenderer fr = mc.fontRenderer;
        fr.drawStringWithShadow(bac, 6, 6, 0xFFFFFF);
        fr.drawStringWithShadow(timer, 6, 6 + fr.FONT_HEIGHT + 1, 0xFFFFFF);
    }
}
