package winslow.shipwrecks;

import net.minecraftforge.fml.common.SidedProxy;
import winslow.shipwrecks.Register.RegisterArmor;
import winslow.shipwrecks.Items.DivingArmor;
//import winslow.shipwrecks.Mobs.EntityHostileFishSchool;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
//import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
//import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import winslow.shipwrecks.Proxy.CommonProxy;

@Mod(modid = ShipwrecksMain.MODID, version = ShipwrecksMain.VERSION)
public class ShipwrecksMain {
    public static final String MODID = "shipwrecks_winslow";
    static final String VERSION = "2.0.0";

    @SidedProxy(serverSide = "winslow.shipwrecks.Proxy.ServerProxy", clientSide = "winslow.shipwrecks.Proxy.ClientProxy")
    public static CommonProxy proxy;

    //Mob variables
    //static int startEntityId = 0;
    private static ShipwreckGen shipwreckgen = new ShipwreckGen();

    private static ItemArmor.ArmorMaterial DIVING_ARMOR = EnumHelper.addArmorMaterial("DivingArmor", MODID + ":diving_armor",
            15, new int[]{3, 5, 4, 3}, 5, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);

    @EventHandler
    public void PreInit(FMLPreInitializationEvent event) {
        ShipwreckConfig.initConfiguration(event);
    }

    @EventHandler
    public void Init(FMLInitializationEvent event) {
        //Adds shipwreck generation to the world generation
        //String piranhaName = "piranha";
        //ResourceLocation loc = new ResourceLocation(MODID + ":" + piranhaName);
        //EntityRegistry.registerModEntity(loc, EntityHostileFishSchool.class, piranhaName, 3281, this, 12, 12, true);
        //EntityRegistry.registerEgg(loc, 0x1f263a, 0x6b0000);
        GameRegistry.registerWorldGenerator(shipwreckgen, 5);
    }
}