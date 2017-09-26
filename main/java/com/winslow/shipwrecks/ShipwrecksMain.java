package com.winslow.shipwrecks;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod(modid = ShipwrecksMain.MODID, version = ShipwrecksMain.VERSION)
public class ShipwrecksMain {
    static final String MODID = "shipwrecks_winslow";
    static final String VERSION = "2.0.0";

    //@SidedProxy(clientSide = "com.winslow.shipwrecks.ClientProxy",
    //		serverSide = "com.winslow.shipwrecks.ServerProxy")
    //public static ServerProxy proxy;

    //Mob variables
    //static int startEntityId = 0;
    private static ShipwreckGen shipwreckgen = new ShipwreckGen();

    @EventHandler
    public void Init(FMLPreInitializationEvent event) {
        ShipwreckConfig.initConfiguration(event);
    }

    @EventHandler
    public void Init(FMLInitializationEvent event) {
        //Adds shipwreck generation to the world generation
        GameRegistry.registerWorldGenerator(shipwreckgen, 5);
    }
}