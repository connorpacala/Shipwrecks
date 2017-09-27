package com.winslow.shipwrecks.Items;

import com.winslow.shipwrecks.ShipwrecksMain;
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
    public DivingArmor(String name, ArmorMaterial material, int renderIndex, EntityEquipmentSlot slot) {
        super(material, renderIndex, slot);
        this.setUnlocalizedName(name);
        this.setRegistryName(name);
        this.setCreativeTab(CreativeTabs.COMBAT);
        this.setMaxStackSize(1);
    }

    @Override
    public void onArmorTick(World world, EntityPlayer player, ItemStack items) {
        ItemStack helm = player.inventory.armorItemInSlot(3);
        ItemStack chest = player.inventory.armorItemInSlot(2);
        ItemStack legs = player.inventory.armorItemInSlot(1);
        ItemStack feet = player.inventory.armorItemInSlot(0);

        int slowdown = 0;

        slowdown += (helm.getItem() == ShipwrecksMain.diving_helmet) ? 1 : 0;
        slowdown += (chest.getItem() == ShipwrecksMain.diving_chest) ? 1 : 0;
        slowdown += (legs.getItem() == ShipwrecksMain.diving_legs) ? 1 : 0;
        slowdown += (feet.getItem() == ShipwrecksMain.diving_boots) ? 1 : 0;

        if (slowdown == 4)
            addPotionEffect(player, Potion.getPotionById(13), 10, 2);
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
                player.addPotionEffect(new PotionEffect(potion, duration, level, true, true));
    }
}
