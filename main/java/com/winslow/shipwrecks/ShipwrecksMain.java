package com.winslow.shipwrecks;

import com.winslow.shipwrecks.Items.DivingArmor;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod(modid = ShipwrecksMain.MODID, version = ShipwrecksMain.VERSION)
public class ShipwrecksMain {
    static final String MODID = "shipwrecks_winslow";
    static final String VERSION = "2.0.0";
    public static DivingArmor diving_helmet;
    public static DivingArmor diving_chest;
    public static DivingArmor diving_legs;
    public static DivingArmor diving_boots;

    //@SidedProxy(clientSide = "com.winslow.shipwrecks.ClientProxy",
    //		serverSide = "com.winslow.shipwrecks.ServerProxy")
    //public static ServerProxy proxy;

    //Mob variables
    //static int startEntityId = 0;
    private static ShipwreckGen shipwreckgen = new ShipwreckGen();

    private static ItemArmor.ArmorMaterial DIVING_ARMOR = EnumHelper.addArmorMaterial("DivingArmor", MODID + ":diving_armor",
            15, new int[]{3, 5, 4, 3}, 5, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
    @EventHandler
    public void Init(FMLPreInitializationEvent event) {
        ShipwreckConfig.initConfiguration(event);
        if(ShipwreckConfig.getIncludeDivingArmor()) {
            diving_helmet = new DivingArmor("diving_helmet", DIVING_ARMOR, 1, EntityEquipmentSlot.HEAD);
            diving_chest = new DivingArmor("diving_chest", DIVING_ARMOR, 1, EntityEquipmentSlot.CHEST);
            diving_legs = new DivingArmor("diving_legs", DIVING_ARMOR, 2, EntityEquipmentSlot.LEGS);
            diving_boots = new DivingArmor("diving_boots", DIVING_ARMOR, 1, EntityEquipmentSlot.FEET);

            GameRegistry.register(diving_helmet);
            GameRegistry.register(diving_chest);
            GameRegistry.register(diving_legs);
            GameRegistry.register(diving_boots);
        }
    }

    @EventHandler
    public void Init(FMLInitializationEvent event) {
        //Adds shipwreck generation to the world generation
        GameRegistry.registerWorldGenerator(shipwreckgen, 5);
    }
}