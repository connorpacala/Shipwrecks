package com.winslow.shipwrecks;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Random;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.fml.common.IWorldGenerator;

public class ShipwreckGen implements IWorldGenerator{

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator,
			IChunkProvider chunkProvider) {
		
		switch(world.provider.getDimension())
		{
			case 0: generateSurface(world, chunkX * 16, chunkZ * 16); //Overworld Generation
		}
	}
	
	/*
	 * Generate structures on the surface
	 */
	private void generateSurface(World world, int chunkX, int chunkZ)
	{
		//Get the highest non-air block
		BlockPos pos = new BlockPos(chunkX, world.getHeight(chunkX, chunkZ), chunkZ);//getWorldHeight(world, chunkX, chunkZ);
		Random random = new Random();
		
		Biome bio = world.getBiome(pos);
		String biomeName = bio.getBiomeName().toLowerCase();
		
		if(biomeName.contains("ocean")) //check to generate ship in ocean
			//generateStructures(world, structure, pos);
			generateStructures(world, getStructureName(true, random), pos);
		else if(biomeName.contains("beach")) //check to generate ship on beach
			//generateStructures(world, structure, pos);
			generateStructures(world, getStructureName(false, random), pos);
	}
	
	/*
	 * Calls a generate function for a random cardinal direction
	 */
	private void generateStructures(World world, String structure, BlockPos pos)
	{
		if(structure == null)
			return;
					
		JsonParser parser = new JsonParser();
		
		try
		{
			//Read JSON string from structure file
			URL path = ShipwrecksMain.class.getResource("/assets/" + ShipwrecksMain.MODID 
					+ "/structures/" + structure + ".json");	
			if(path == null)
				return;
			String textFile = Resources.toString(path, Charsets.UTF_8);
			
			JsonObject jsonObj = (JsonObject) parser.parse(textFile);
			
			Random random = new Random();
			int orientation = random.nextInt(4); //N, W, S, E orientation
			
			addBlocksJson(world, jsonObj, pos, "hull", Blocks.PLANKS, orientation); //add ship hull to the world
			addBlocksJson(world, jsonObj, pos, "mast", Blocks.LOG, orientation); //add ship mast to the world
			addChestsJson(world, jsonObj, pos, "chest", Blocks.CHEST, orientation); //add ship mast to the world

		} catch (JsonIOException e) {
			e.printStackTrace();
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * adds blocks to the world with positions read from passed JsonObject 
	 */
	private void addBlocksJson(World world, JsonObject jsonObj, BlockPos pos, String structurePiece, Block block, int orientation)
	{	
		if(jsonObj.has(structurePiece))
		{
			JsonArray blocks;
			blocks = jsonObj.getAsJsonArray(structurePiece);
			if(blocks != null)
			{
				for (int i = 0; i < blocks.size(); ++i)
				{
					JsonArray posArray = blocks.get(i).getAsJsonArray();

					if(posArray.size() >= 3) //json array has at least 3 values (x, y, z)
					{
						int x = posArray.get(0).getAsInt();
						int y = posArray.get(1).getAsInt() - 1;
						int z = posArray.get(2).getAsInt();
						
						//convert coords to correct position based on orientation
						switch(orientation)
						{
							case 1: //West
								x = -x;
								break;
							case 2: //North
								int tempN = x;
								x = z;
								z = -tempN;
								break;
							case 3: //South
								int tempS = x;
								x = z;
								z = tempS;
								break;
						}
						
						if(posArray.size() == 4) //add blocks with metadata
						{
							int md = posArray.get(3).getAsInt();
							
							//convert metadata to face correct direction
							switch(orientation)
							{
								case 1: //West
									md = convertMetaWest(md);
									break;
								case 2: //North
									md = convertMetaNorth(md);
									break;
								case 3: //South
									md = convertMetaSouth(md);
									break;
							}
							addBlock(world, pos.getX() + x, pos.getY() + y, pos.getZ() + z, block, md);
						}
						else //add blocks without metadata
							addBlock(world, pos.getX() + x, pos.getY() + y, pos.getZ() + z, block);
					}
				}
			}
		}
	}
	
	private void addChestsJson(World world, JsonObject jsonObj, BlockPos pos, String structurePiece, Block block, int orientation) //add ship mast to the world
	{
		if(jsonObj.has(structurePiece))
		{
			JsonArray blocks;
			blocks = jsonObj.getAsJsonArray(structurePiece); //get array of chest objects
			
			if(blocks != null)
			{
				//cycle through chest objects, add chest, and add chest loot
				for (int i = 0; i < blocks.size(); ++i)
				{
					if(blocks.get(i).isJsonObject()) //make sure that blocks is an array of JSON objects
					{
						JsonObject chest = blocks.get(i).getAsJsonObject();
						
						if(chest != null)
						{
							//coordinates for the chest
							if(chest.get("coords").isJsonArray())
							{
								JsonArray posArray = chest.get("coords").getAsJsonArray();
								int x = posArray.get(0).getAsInt();
								int y = posArray.get(1).getAsInt() - 1;
								int z = posArray.get(2).getAsInt();
								
								//convert coords to correct position based on orientation
								switch(orientation)
								{
									case 1: //West
										x = -x;
										break;
									case 2: //North
										int tempN = x;
										x = z;
										z = -tempN;
										break;
									case 3: //South
										int tempS = x;
										x = z;
										z = tempS;
										break;
								}
								int md = posArray.get(3).getAsInt();
								
								//convert metadata to face correct direction
								switch(orientation)
								{
									case 1: //West
										md = convertMetaWest(md);
										break;
									case 2: //North
										md = convertMetaNorth(md);
										break;
									case 3: //South
										md = convertMetaSouth(md);
										break;
								}
								addBlock(world, pos.getX() + x, pos.getY() + y, pos.getZ() + z, block, md);
								
								//add appropriate loot based on the loot pool that 
								int lootPool = chest.get("loot").getAsInt();
								addChestLoot(world, pos.getX() + x, pos.getY() + y, pos.getZ() + z, lootPool);
							}
						}
					}
				}
			}
		}
	}
	
	/*
	 * Converts passed metadata from east facing BP to value for West facing spawns
	 */
	private int convertMetaWest(int md)
	{
		switch(md)
		{
			case 2:
				md = 3;
				break;
			case 3:
				md = 2;
				break;
			case 4:
				md = 5;
				break;
			case 5:
				md = 4;
				break;
		}
		return md;
	}
	
	/*
	 * Converts passed metadata from east facing BP to value for North facing spawns
	 */
	private int convertMetaNorth(int md)
	{
		switch(md)
		{
			case 2:
				md = 4;
				break;
			case 3:
				md = 5;
				break;
			case 4:
				md = 3;
				break;
			case 5:
				md = 2;
				break;
		}
		return md;
	}
	
	/*
	 * Converts passed metadata from east facing BP to value for South facing spawns
	 */
	private int convertMetaSouth(int md)
	{
		switch(md)
		{
			case 2:
				md = 5;
				break;
			case 3:
				md = 4;
				break;
			case 4:
				md = 2;
				break;
			case 5:
				md = 3;
				break;
		}
		return md;
	}
	
	
	/*
	 * Finds the highest non-air block at the passed x and z coordinates
	 * Returns the BlockPos for the found height and x and z coords
	 */
	protected BlockPos getWorldHeight(World world, int x, int z)
	{
		//start at the highest block
		BlockPos pos = new BlockPos(x, world.getHeight(x, z), z);
		for(int i = pos.getY(); i > 63; --i)
		{
			pos = pos.down();
			if(world.getBlockState(pos).getBlock() != Blocks.AIR)
				return pos;
		}
		return pos;
	}
	
	/*
	 * Add a block without metadata
	 */
	private void addBlock(World world, int x, int y, int z, Block block)
	{
		world.setBlockState(new BlockPos(x, y, z), block.getDefaultState());
	}
	
	/*
	 * Add a block with metadata
	 */
	@SuppressWarnings("deprecation") //suppressed as getStateFromMeta is not actually deprecated by Mojang
	private void addBlock(World world, int x, int y, int z, Block block, int metadata)
	{
		world.setBlockState(new BlockPos(x, y, z), block.getStateFromMeta(metadata)); //getStateFromMeta not actually deprecated
	}
	
	/*
	 * Get the correct name for the structure to generate based on the corresponding weight
	 * 
	 * parameters: isOceanBiome, true = is an ocean biome, false = is not (it's a beach biome) 
	 */
	private String getStructureName(Boolean isOceanBiome, Random random)
	{
		int[] weights;
		//get the correct weights for structures based on the biome
		if(isOceanBiome)
			weights = ShipwreckConfig.getOceanWeights();
		else
			weights = ShipwreckConfig.getBeachWeights();
			
		//find the sum of all weights
		int totalWeight = 0;
		for(int i = 0; i < weights.length; ++i)
			totalWeight += weights[i];
		
		int value = random.nextInt(totalWeight);
		totalWeight = 0;
		for(int i = 0; i < weights.length; ++i)
		{
			totalWeight += weights[i];
			if(totalWeight >= value)
			{
				if(i == 0) //the first index, weighted value for no ships to spawn so don't return a name
					return null;
				else //return the name of the wreck corresponding to the weight
					return ShipwreckConfig.getNames()[i - 1]; //i - 1 as the weighted array has the no spawn value at index 0
			}
		}
		return null;
	}
	
	/*
	 * add chest loot based on lootPool value
	 */
	private void addChestLoot(World world, int x, int y, int z, int lootPool)
	{
		Random random = new Random();
		BlockPos chestPos = new BlockPos(x, y, z);
		
		ItemStack stack = new ItemStack(Items.GOLD_INGOT);
		
		switch(lootPool)
		{
			case 1:
				TileEntityChest tileentitychest = (TileEntityChest)world.getTileEntity(chestPos);
				tileentitychest.setInventorySlotContents(random.nextInt(tileentitychest.getSizeInventory()), stack);
				break;
		}
	}
}
