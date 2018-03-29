package winslow.shipwrecks;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

class ShipwreckLoot
{
    private Random random = new Random();
    /*
     * add chest loot based on lootPool value
     */
    void addChestLoot(World world, BlockPos chestPos, String lootPool)
    {
        TileEntityChest tileentitychest = (TileEntityChest) world.getTileEntity(chestPos);
        if (tileentitychest != null)
        {
            switch (lootPool)
            {
                case "cargo":
                    cargo_loot(tileentitychest);
                    break;
                case "captain":
                    captain_loot(tileentitychest);
                    break;
                case "low":  // common tier loot (e.g. sailboat loot)
                    low_loot(tileentitychest);
                    break;
                case "med":  //med tier loot (e.g. sloop)
                    med_loot(tileentitychest);
                    break;
                case "high":  //high tier loot (e.g. schooner)
                    high_loot(tileentitychest);
                    break;
                case "epic":  //epic tier loot (e.g. waverunner)
                    epic_loot(tileentitychest);
                    break;
                default:
                    break;
            }
        }
    }

    private int getRandomItem(int[] weights) {
        //find the sum of all weights
        int totalWeight = 0;
        for (int weight : weights)
            totalWeight += weight;

        int value = random.nextInt(totalWeight);
        totalWeight = 0;
        for (int i = 0; i < weights.length; ++i) {
            totalWeight += weights[i];
            if (totalWeight >= value) {
                return i;
            }
        }
        return 0;
    }

    private void captain_loot(TileEntityChest tileentitychest) {
        ItemStack item;
        item = new ItemStack(Items.FILLED_MAP);
        tileentitychest.setInventorySlotContents(random.nextInt(tileentitychest.getSizeInventory()), item);
        item = new ItemStack(Items.COMPASS);
        tileentitychest.setInventorySlotContents(random.nextInt(tileentitychest.getSizeInventory()), item);
    }

        private void cargo_loot(TileEntityChest tileentitychest)
    {
        for (int i = 0; i < 2; ++i)
        {
            int[] weights = {8, 6, 6, 6, 4, 4, 4, 2, 1, 1, 1};
            int idx = getRandomItem(weights);

            ItemStack item;
            switch(idx) {
                case 0:
                    item =  new ItemStack(Items.APPLE);
                    break;
                case 1:
                    item = new ItemStack(Items.CARROT, random.nextInt(3) + 1);
                    break;
                case 2:
                    item = new ItemStack(Items.POTATO, random.nextInt(3) + 1);
                    break;
                case 3:
                    item = new ItemStack(Items.BEETROOT);
                    break;
                case 4:
                    item = new ItemStack(Items.IRON_AXE);
                    break;
                case 5:
                    item = new ItemStack(Items.IRON_SHOVEL);
                    break;
                case 6:
                    item = new ItemStack(Items.LEATHER, random.nextInt(3) + 1);
                    break;
                case 7:
                    item = new ItemStack(Items.GHAST_TEAR);
                    break;
                case 8:
                    item = new ItemStack(Items.MAGMA_CREAM);
                    break;
                default:
                    item = new ItemStack(Items.CHORUS_FRUIT);
            }
            tileentitychest.setInventorySlotContents(random.nextInt(tileentitychest.getSizeInventory()), item);
        }
    }

    private void low_loot(TileEntityChest tileentitychest)
    {
        for (int i = 0; i < 4; ++i)
        {
            int[] weights = {5, 8, 8, 8, 6, 4, 4, 2, 1, 1};
            int idx = getRandomItem(weights);

            ItemStack item;
            switch(idx) {
                case 0:
                    item = new ItemStack(Items.BUCKET);
                    break;
                case 1:
                    item = new ItemStack(Items.BREAD, random.nextInt(3) + 1);
                    break;
                case 2:
                    item = new ItemStack(Items.IRON_INGOT, random.nextInt(4) + 1);
                    break;
                case 3:
                    item = new ItemStack(Items.GUNPOWDER, random.nextInt(4) + 1);
                    break;
                case 4:
                    item = new ItemStack(Items.GOLD_NUGGET, random.nextInt(4) + 1);
                    break;
                case 5:
                    item = new ItemStack(Items.GOLD_INGOT);
                    break;
                case 6:
                    item = new ItemStack(Items.SADDLE);
                    break;
                case 7:
                    item = new ItemStack(Items.DIAMOND, random.nextInt(3) + 1);
                    break;
                default:
                    item = new ItemStack(Items.ENDER_PEARL);
                    break;
            }
            tileentitychest.setInventorySlotContents(random.nextInt(tileentitychest.getSizeInventory()), item);
        }
    }

    private void med_loot(TileEntityChest tileentitychest)
    {
        for (int i = 0; i < 5; ++i) {
            int[] weights = {4, 8, 8, 8, 6, 4, 4, 2, 2, 1, 1};
            int idx = getRandomItem(weights);

            ItemStack item;
            switch (idx) {
                case 0:
                    item = new ItemStack(Items.BUCKET);
                    break;
                case 1:
                    item = new ItemStack(Items.BREAD, random.nextInt(3) + 1);
                    break;
                case 2:
                    item = new ItemStack(Items.IRON_INGOT, random.nextInt(4) + 1);
                    break;
                case 3:
                    item = new ItemStack(Items.GUNPOWDER, random.nextInt(4) + 1);
                    break;
                case 4:
                    item = new ItemStack(Items.GOLD_NUGGET, random.nextInt(4) + 1);
                    break;
                case 5:
                    item = new ItemStack(Items.GOLD_INGOT);
                    break;
                case 6:
                    item = new ItemStack(Items.SADDLE);
                    break;
                case 7:
                    item = new ItemStack(Items.DIAMOND, random.nextInt(3) + 1);
                    break;
                case 8:
                    item = new ItemStack(Items.EMERALD, random.nextInt(4) + 1);
                    break;
                default:
                    item = new ItemStack(Items.ENDER_PEARL);
                    break;
            }
            tileentitychest.setInventorySlotContents(random.nextInt(tileentitychest.getSizeInventory()), item);
        }
    }

    private void high_loot(TileEntityChest tileentitychest)
    {
        for (int i = 0; i < 6; ++i) {
            int[] weights = {4, 8, 8, 8, 8, 6, 6, 4, 4, 2, 2, 1, 1};
            int idx = getRandomItem(weights);

            ItemStack item;
            switch (idx) {
                case 0:
                    item = new ItemStack(Items.BUCKET);
                    break;
                case 1:
                    item = new ItemStack(Items.BREAD, random.nextInt(3) + 1);
                    break;
                case 2:
                    item = new ItemStack(Items.IRON_INGOT, random.nextInt(4) + 1);
                    break;
                case 3:
                    item = new ItemStack(Items.GUNPOWDER, random.nextInt(4) + 1);
                    break;
                case 4:
                    item = new ItemStack(Items.IRON_SWORD);
                    break;
                case 5:
                    item = new ItemStack(Items.GOLD_INGOT);
                    break;
                case 6:
                    item = new ItemStack(Items.SADDLE);
                    break;
                case 7:
                    item = new ItemStack(Items.DIAMOND, random.nextInt(3) + 1);
                    break;
                case 8:
                    item = new ItemStack(Items.EMERALD, random.nextInt(4) + 1);
                    break;
                case 9:
                    item = new ItemStack(Items.BLAZE_ROD);
                    break;
                case 10:
                    item = new ItemStack(Items.ENDER_PEARL);
                    break;
                default:
                    item = new ItemStack(Items.DIAMOND_SWORD);
                    break;
            }
            tileentitychest.setInventorySlotContents(random.nextInt(tileentitychest.getSizeInventory()), item);
        }
    }

    private void epic_loot(TileEntityChest tileentitychest)
    {
        for (int i = 0; i < 8; ++i) {
            int[] weights = {2, 6, 6, 6, 6, 6, 6, 4, 4, 2, 2, 2, 1, 1, 1, 1};
            int idx = getRandomItem(weights);

            ItemStack item;
            switch (idx) {
                case 0:
                    item = new ItemStack(Items.BUCKET);
                    break;
                case 1:
                    item = new ItemStack(Items.BREAD, random.nextInt(3) + 1);
                    break;
                case 2:
                    item = new ItemStack(Items.IRON_INGOT, random.nextInt(4) + 1);
                    break;
                case 3:
                    item = new ItemStack(Items.GUNPOWDER, random.nextInt(4) + 1);
                    break;
                case 4:
                    item = new ItemStack(Items.IRON_SWORD);
                    break;
                case 5:
                    item = new ItemStack(Items.GOLD_INGOT);
                    break;
                case 6:
                    item = new ItemStack(Items.SADDLE);
                    break;
                case 7:
                    item = new ItemStack(Items.DIAMOND, random.nextInt(3) + 1);
                    break;
                case 8:
                    item = new ItemStack(Items.EMERALD, random.nextInt(4) + 1);
                    break;
                case 9:
                    item = new ItemStack(Items.BLAZE_ROD);
                    break;
                case 10:
                    item = new ItemStack(Items.ENDER_PEARL);
                    break;
                case 11:
                    item = new ItemStack(Items.NETHER_WART);
                    break;
                case 12:
                    item = new ItemStack(Items.DIAMOND_SWORD);
                    break;
                case 13:
                    item = new ItemStack(Items.DIAMOND_PICKAXE);
                    break;
                default:
                    item = new ItemStack(Items.NETHER_STAR);
            }
            tileentitychest.setInventorySlotContents(random.nextInt(tileentitychest.getSizeInventory()), item);
        }
    }
}
