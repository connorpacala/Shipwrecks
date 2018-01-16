package winslow.shipwrecks.Register;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import winslow.shipwrecks.Init.InitArmor;

@Mod.EventBusSubscriber
public class RegisterArmor {
    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        for(Item item : InitArmor.armors) {
            event.getRegistry().register(item);
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        for(Item item : InitArmor.armors) {
            ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
        }
    }
}