package com.example.antonchik.proxy;

import com.example.antonchik.entity.EntityAntonMob;
import com.example.antonchik.entity.render.RenderAntonMob;
import com.example.antonchik.init.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy
{
    @Override
    public void preInit()
    {
        super.preInit();

        RenderingRegistry.registerEntityRenderingHandler(EntityAntonMob.class, RenderAntonMob::new);

        // HUD readout of the player's blood-alcohol level while drunk.
        MinecraftForge.EVENT_BUS.register(new ClientEventHandler());

        // Register each item's model as a variant so it gets loaded and baked. Each call uses the
        // item's OWN registry name, so the models map to assets/antonchik/models/item/<name>.json.
        registerItemModel(ModItems.ANTON_SPAWN_EGG);
        registerItemModel(ModItems.JAMESON);
    }

    @Override
    public void init()
    {
        super.init();

        // Re-pin each item to its own baked model on the ItemModelMesher, in init (after baking).
        // With more than one item, setCustomModelResourceLocation alone can cross-wire the
        // item -> model mapping -- the first item ends up showing the second item's texture, and
        // the second renders as missing. Re-registering here, once models are baked, locks each
        // item to the correct model. See MinecraftForge forums topic 63663.
        ItemModelMesher mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
        meshItemModel(mesher, ModItems.ANTON_SPAWN_EGG);
        meshItemModel(mesher, ModItems.JAMESON);
    }

    private static void registerItemModel(Item item)
    {
        ModelLoader.setCustomModelResourceLocation(
            item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }

    private static void meshItemModel(ItemModelMesher mesher, Item item)
    {
        mesher.register(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }
}
