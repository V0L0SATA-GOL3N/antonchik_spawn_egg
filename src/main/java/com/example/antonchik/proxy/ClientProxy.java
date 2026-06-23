package com.example.antonchik.proxy;

import com.example.antonchik.entity.EntityAntonMob;
import com.example.antonchik.entity.render.RenderAntonMob;
import com.example.antonchik.init.ModItems;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy
{
    @Override
    public void preInit()
    {
        super.preInit();

        RenderingRegistry.registerEntityRenderingHandler(EntityAntonMob.class, RenderAntonMob::new);

        // Bind the spawn egg item to its model (assets/antonchik/models/item/anton_spawn_egg.json).
        ModelLoader.setCustomModelResourceLocation(
            ModItems.ANTON_SPAWN_EGG, 0,
            new ModelResourceLocation(ModItems.ANTON_SPAWN_EGG.getRegistryName(), "inventory"));
    }
}
