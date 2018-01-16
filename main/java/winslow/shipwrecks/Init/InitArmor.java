package winslow.shipwrecks.Init;

import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import winslow.shipwrecks.Items.DivingArmor;
import winslow.shipwrecks.ShipwrecksMain;

//@GameRegistry.ObjectHolder(ShipwrecksMain.MODID)
public class InitArmor {
    static ItemArmor.ArmorMaterial DIVING_ARMOR = EnumHelper.addArmorMaterial("DivingArmor", ShipwrecksMain.MODID + ":diving_armor",
            15, new int[]{3, 5, 4, 3}, 5, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
    public static DivingArmor diving_helmet = new DivingArmor("diving_helmet", DIVING_ARMOR, 1, EntityEquipmentSlot.HEAD);
    public static DivingArmor diving_chest = new DivingArmor("diving_chest", DIVING_ARMOR, 1, EntityEquipmentSlot.CHEST);
    public static DivingArmor diving_legs = new DivingArmor("diving_legs", DIVING_ARMOR, 2, EntityEquipmentSlot.LEGS);
    public static DivingArmor diving_boots = new DivingArmor("diving_boots", DIVING_ARMOR, 1, EntityEquipmentSlot.FEET);

    public static Item[] armors = {
            diving_helmet,
            diving_chest,
            diving_legs,
            diving_boots,
    };
}
