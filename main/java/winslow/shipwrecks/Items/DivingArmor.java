package winslow.shipwrecks.Items;

import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import winslow.shipwrecks.Init.InitArmor;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

import java.util.Random;

public class DivingArmor extends ItemArmor {
    private String name;

    public DivingArmor(String name, ArmorMaterial material, int renderIndex, EntityEquipmentSlot slot) {
        super(material, renderIndex, slot);
        this.name = name;
        setRegistryName(name);
        setUnlocalizedName(getRegistryName().toString());
        setCreativeTab(CreativeTabs.COMBAT);
        setMaxStackSize(1);
    }

    @Override
    public void onArmorTick(World world, EntityPlayer player, ItemStack items) {
        ItemStack helm = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        ItemStack chest = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        ItemStack legs = player.getItemStackFromSlot(EntityEquipmentSlot.LEGS);
        ItemStack feet = player.getItemStackFromSlot(EntityEquipmentSlot.FEET);

        int slowdown = 0;

        slowdown += (helm.getItem() == InitArmor.diving_helmet) ? 1 : 0;
        slowdown += (chest.getItem() == InitArmor.diving_chest) ? 1 : 0;
        slowdown += (legs.getItem() == InitArmor.diving_legs) ? 1 : 0;
        slowdown += (feet.getItem() == InitArmor.diving_boots) ? 1 : 0;

        if (slowdown == 4)
            addPotionEffect(player, Potion.getPotionById(13), 10, 2);
            if (!player.isInWater())
                addPotionEffect(player, Potion.getPotionById(2), 10, slowdown);
        else if (slowdown > 0){
            if (!player.isInWater())
                addPotionEffect(player, Potion.getPotionById(2), 10, slowdown);
            else if (player.getAir() < 290) { //player in water with slowdown > 0 at this point < 290 as max air is 300 and I add 10
                Random random = new Random();
                if (random.nextInt(100) < slowdown)
                    player.setAir(player.getAir() + 10);
            }
        }
    }

    private void addPotionEffect(EntityPlayer player, Potion potion, int duration, int level) {
        if (player.getActivePotionEffect(potion) == null || player.getActivePotionEffect(potion).getDuration() <= 1)
                player.addPotionEffect(new PotionEffect(potion, duration, level, true, false));
    }
}
