package winslow.shipwrecks.Proxy;

import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy{
    //adds armor to the renderer
    @Override
    public int addArmor(String armor)
    {
        return 0; //return RenderingRegistry.addNewArmourRendererPrefix(armor);
    }
}
