package com.example.antonchik;

import com.example.antonchik.proxy.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = Antonchik.MODID, name = Antonchik.NAME, version = Antonchik.VERSION)
public class Antonchik
{
    public static final String MODID = "antonchik";
    public static final String NAME = "Antonchik";
    public static final String VERSION = "1.0";

    @SidedProxy(
        clientSide = "com.example.antonchik.proxy.ClientProxy",
        serverSide = "com.example.antonchik.proxy.CommonProxy"
    )
    public static CommonProxy proxy;

    private static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
        proxy.preInit();
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.init();
    }
}
