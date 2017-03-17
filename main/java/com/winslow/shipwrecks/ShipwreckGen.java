package com.winslow.shipwrecks;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
		BlockPos pos = new BlockPos(chunkX, 0, chunkZ);//getWorldHeight(world, chunkX, chunkZ);
		
		int max = ShipwreckConfig.getMaxDist();
		if(canSpawnHere(pos, max))
		{
			Random random = new Random();

			Biome bio = world.getBiome(pos);
			String biomeName = bio.getBiomeName().toLowerCase();
			
			//Random offset from chunk between min and max from center of chunk
			int min = ShipwreckConfig.getMinDist();
			
			double maxOffset = ( (double)max - (double)min ) / 2.0;
			int newX = (int)((random.nextDouble() * maxOffset - 2 * random.nextDouble() * maxOffset) * 16);
			int newZ = (int)((random.nextDouble() * maxOffset - 2 * random.nextDouble() * maxOffset) * 16);
			pos = pos.add(newX, 0, newZ);
			pos = pos.add(0, findSeafloor(world, pos), 0);
			
			if(biomeName.contains("ocean")) //check to generate ship in ocean
				generateStructures(world, getStructureName(true, random), pos);
			else if(biomeName.contains("beach")) //check to generate ship on beach
				generateStructures(world, getStructureName(false, random), pos);
		}
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
			
			//check if the object is able to float, if no value set, assume that it can't float
			Boolean canFloat = false;
			
			//if(jsonObj.has("canfloat"))
			//	canFloat = jsonObj.get("canfloat").getAsBoolean();
			
		//	if(canFloat && random.nextInt(2) == 0)
				pos = pos.add(0, world.getSeaLevel() - pos.getY(), 0);
			
			//if(!canFloat || random.nextInt(2) != 0) //chance for structures that can float to not float REPLACE THIS WITH A VARIABLE IN THE CONFIG
			//	pos = findSeafloor(world, pos);
			
			if(jsonObj.has("sections"))
			{
				JsonArray sections = jsonObj.getAsJsonArray("sections"); //get sections (an array of objects containing block types and coordinate)
				
				for(int i = 0; i < sections.size(); ++i) //loop through array and add each segment
					addBlocksJson(world, sections.get(i).getAsJsonObject(), pos, "temp", Blocks.GOLD_BLOCK, orientation);
			}
			//add random features
				
			//addBlocksJson(world, jsonObj, pos, "hull", Block.getBlockFromName("minecraft:planks"), orientation); //add ship hull to the world
			//addBlocksJson(world, jsonObj, pos, "deck", Block.getBlockFromName("minecraft:planks"), orientation); //add ship deck to the world
			//addBlocksJson(world, jsonObj, pos, "mast", Blocks.LOG, orientation); //add ship mast to the world
			//addBlocksJson(world, jsonObj, pos, "chest", Blocks.CHEST, orientation); //add ship chests to the world
			//addBlocksJson(world, jsonObj, pos, "random", Blocks.LOG, orientation); //add blocks that appear in a random range around the ship to the world

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
	 * Check if wreck can spawn here based on min and max distances set in config
	 */
	private Boolean canSpawnHere(BlockPos pos, int maxDist)
	{
		int xVal = (pos.getX() / 16) % maxDist;
		int zVal = (pos.getZ() / 16) % maxDist;
		//wrecks can spawn only on (maxDist, Y, maxDist) nodes) but get offset a random distance from there
		if(xVal == 0 && zVal == 0)
			return true;
		
		return false;
	}
	
	/*
	 * adds blocks to the world with positions read from passed JsonObject 
	 */
	private void addBlocksJson(World world, JsonObject jsonObj, BlockPos pos, String structurePiece, Block block, int orientation)
	{	
		int subtype = -1; //value to specify variation on objects like wood planks (e.g. oak, spruce, etc)
		
		if(!jsonObj.has("block") || !jsonObj.has("coords")) //missing required field, don't know what block to add/where to put them
			return;
		
		//get block type to add
		String blockType = jsonObj.get("block").getAsString();
		block = Block.getBlockFromName(blockType);
		
		if(block == null) //blockType incorrect, unknown block to add.
			return;
		
		if(jsonObj.has("loot")) //process blocks with inventory differently (e.g. chests have loot tiers)
		{
			addChestsJson(world, jsonObj, pos, structurePiece, block, orientation);
		}
		else //regular blocks, no loot added to them
		{
			if(jsonObj.has("subtype")) //get block variation (e.g. oak or spruce for wood planks)
				subtype = jsonObj.get("subtype").getAsInt();
			
			JsonArray blocks = jsonObj.getAsJsonArray("coords"); //array of block positions. "coords" existence checked at beginning of function
			for(int i = 0; i < blocks.size(); ++i)
			{
				JsonArray posArray = blocks.get(i).getAsJsonArray(); //get first set of coords
				
				if(posArray.size() >= 3) //json array has at least 4 values (x, y, z, blockID)
				{
					int index = 0;
					int x = posArray.get(index).getAsInt();
					++index;
					int y = posArray.get(index).getAsInt() - 1;
					++index;
					int z = posArray.get(index).getAsInt();
					++index;
					
					//int blockID = posArray.get(3).getAsInt();
					
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
						int md = posArray.get(index).getAsInt();
						
						//logs have annoying metadata. 4 = east/west, 8 = North/South, so if wreck is facing N/S (instead of default East), swap metadata
						if(block == Blocks.LOG && (orientation == 2 || orientation == 3))
						{
							md = (md == 4) ? 8 : 4;
						}
						else
						{
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
						}
						
						addBlock(world, pos.getX() + x, pos.getY() + y, pos.getZ() + z, block, md);
					}
					else if(subtype != -1)
						addBlock(world, pos.getX() + x, pos.getY() + y, pos.getZ() + z, block, subtype);
					else//add blocks without metadata
						addBlock(world, pos.getX() + x, pos.getY() + y, pos.getZ() + z, block);
				}
			}
		}
		
		/*
		if(structurePiece.equalsIgnoreCase("chest"))
		{
			addChestsJson(world, jsonObj, pos, structurePiece, Blocks.CHEST, orientation);
		}
		else if(structurePiece.equalsIgnoreCase("random"))
		{
			addRandomBlocksJson(world, jsonObj, pos, structurePiece, block);
		}
		else if(jsonObj.has(structurePiece))
		{
			JsonArray blocks;
			blocks = jsonObj.getAsJsonArray(structurePiece);
			if(blocks != null)
			{
				for (int i = 0; i < blocks.size(); ++i)
				{
					JsonArray posArray = blocks.get(i).getAsJsonArray();

					if(posArray.size() >= 3) //json array has at least 4 values (x, y, z, blockID)
					{
						int index = 0;
						int x = posArray.get(index).getAsInt();
						++index;
						int y = posArray.get(index).getAsInt() - 1;
						++index;
						int z = posArray.get(index).getAsInt();
						++index;
						
						//int blockID = posArray.get(3).getAsInt();
						
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
							int md = posArray.get(index).getAsInt();
							
							//logs have annoying metadata. 4 = east/west, 8 = North/South, so if wreck is facing N/S (instead of default East), swap metadata
							if(block == Blocks.LOG && (orientation == 2 || orientation == 3))
							{
								md = (md == 4) ? 8 : 4;
							}
							else
							{
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
							}
							
							addBlock(world, pos.getX() + x, pos.getY() + y, pos.getZ() + z, block, md);
						}
						else //add blocks without metadata
							addBlock(world, pos.getX() + x, pos.getY() + y, pos.getZ() + z, block);
					}
				}
			}
		}*/
	}
	
	/*
	 * Add blocks to the structure that can be a random distance between a min and max value stored in the JSON file.
	 */
	private void addRandomBlocksJson(World world, JsonObject jsonObj, BlockPos pos, String structurePiece, Block block)
	{
		Random random = new Random();
		
		if(jsonObj.has(structurePiece))
		{
			JsonArray blocks;
			blocks = jsonObj.getAsJsonArray(structurePiece); //get array of random objects
			
			if(blocks != null)
			{
				for(int i = 0; i < blocks.size(); ++i)
				{
					JsonObject obj = blocks.get(i).getAsJsonObject();
					
					//get key in Json object corresponding to object being added (e.g. get "chest" key and add a chest)
					Set<Entry<String, JsonElement>> keys = obj.entrySet();
					String structureName = "";
					for(Map.Entry<String, JsonElement> key: keys) //get the last key in the object (should be the name of the object)
						structureName = key.getKey();
					
					//get the random range (min, max) values to spawn object in
					JsonArray range = obj.get("range").getAsJsonArray();
					
					int min = range.get(0).getAsInt();
					int max = range.get(1).getAsInt();
					
					int xOffset = min + random.nextInt(max - min);
					int zOffset = min + random.nextInt(max - min);
					
					//50% chance to be negative x or y from center of wreck
					if(random.nextInt(2) == 0)
						xOffset *= -1;
					if(random.nextInt(2) == 0)
						zOffset *= -1;
					
					//find new position to act as (0, 0, 0) for random object
					BlockPos newPos = new BlockPos(pos.getX() + xOffset, pos.getY(), pos.getZ() + zOffset);
					
					addBlocksJson(world, obj, newPos, structureName, block, random.nextInt(4));
				}
			}
		}
	}
	
	/*
	 * Attempts to add chests from the passed json file to the world
	 */
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
								int index = 0;
								int x = posArray.get(index).getAsInt();
								++index;
								int y = posArray.get(index).getAsInt() - 1;
								++index;
								int z = posArray.get(index).getAsInt();
								++index;
								//block = Block.getBlockById(posArray.get(index).getAsInt());
								//++index;
								
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
								int md = posArray.get(index).getAsInt();
								
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
	 * Finds the highest non-water/non-air block at the passed x and z coordinates
	 * Returns the y coordinate for the found height and x and z coords
	 */
	private int findSeafloor(World world, BlockPos pos)
	{
		//start at the highest block
		while(world.getBlockState(pos).getBlock() == Blocks.WATER || world.getBlockState(pos).getBlock() == Blocks.AIR)
			pos = pos.down();
		
		return pos.getY();
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
