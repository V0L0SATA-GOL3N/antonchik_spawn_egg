package com.example.antonchik.init;

import com.example.antonchik.Antonchik;
import com.example.antonchik.entity.EntityAntonMob;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;

@Mod.EventBusSubscriber(modid = Antonchik.MODID)
public class RegistryHandler
{
    private static int entityId = 0;

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(ModItems.ANTON_SPAWN_EGG);
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
                .build()
        );
    }
}
