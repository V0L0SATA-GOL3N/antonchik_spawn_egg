package com.example.antonchik.proxy;

import com.example.antonchik.entity.EntityAntonMob;
import com.example.antonchik.entity.render.RenderAntonMob;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy
{
    @Override
    public void preInit()
    {
        // No model yet — register a no-op renderer so the entity can spawn without crashing.
        RenderingRegistry.registerEntityRenderingHandler(EntityAntonMob.class, RenderAntonMob::new);
    }
}
