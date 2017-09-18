package com.winslow.shipwrecks;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBed.EnumPartType;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockLog.EnumAxis;
import net.minecraft.block.BlockPlanks.EnumType;
import net.minecraft.block.BlockSlab.EnumBlockHalf;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
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
			
			if(biomeName.indexOf("ocean") != -1) //check to generate ship in ocean
				generateStructures(world, getStructureName(true, random), pos);
			else if(biomeName.indexOf("beach") != -1) //check to generate ship on beach
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
			int orientation = random.nextInt(4); //E, W, N, S orientation
			
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
				JsonArray sections = jsonObj.getAsJsonArray("sections"); //get sections (an array of objects containing block types and coordinates)
				
				for(int i = 0; i < sections.size(); ++i) //loop through array and add each segment
					addBlocksJson(world, sections.get(i).getAsJsonObject(), pos, orientation);
			}
			if(jsonObj.has("random")) //structure pieces that can appear a random orientation and distance from the center of the structure
			{
				JsonArray sections = jsonObj.getAsJsonArray("random");
				
				for(int i = 0; i < sections.size(); ++i) //loop through array and add each segment
				{
					JsonObject data = sections.get(i).getAsJsonObject();
					
					if(data.has("range"))
					{
						JsonArray range = data.getAsJsonArray("range");
						
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
						
						addBlocksJson(world, data, newPos, orientation);
					}
				}
			}
			if(jsonObj.has("chance_sections")) //sections that have a given chance to spawn
			{
				JsonArray chance_sections = jsonObj.getAsJsonArray("chance_sections");
				for(int i = 0; i < chance_sections.size(); ++i)
				{
					JsonObject data = chance_sections.get(i).getAsJsonObject();
					Boolean isExclusive = false;
					if(data.has("exclusive"))
						isExclusive = data.get("exclusive").getAsBoolean();
					
					if(!data.has("chance")) //these sections require a weight field
						return;
					JsonArray chance = data.getAsJsonArray("chance");
					JsonArray sections = data.getAsJsonArray("chance_blocks"); //get sections (an array of objects containing block types and coordinates)
					
					for(int j = 0; j < sections.size() && j < chance.size(); ++j)
					{
						if(random.nextInt(chance.get(j).getAsInt()) == 0)
						{
							JsonArray coords = sections.get(j).getAsJsonArray();
							for(int k = 0; k < coords.size(); ++k)
								addBlocksJson(world, coords.get(k).getAsJsonObject(), pos, orientation);
							
							if(isExclusive)
								break;
						}
					}
				}
			}
			if(jsonObj.has("damage_sections")) //create damage on ship. Replace removed blocks with block type 1 away from center and 1 Y coord up
			{
				JsonArray damageSections = jsonObj.getAsJsonArray("damage_sections");
				
				for(int i = 0; i < damageSections.size(); ++i)
				{
					JsonObject piece = damageSections.get(i).getAsJsonObject();
					if(!piece.has("chance")) //these sections require a weight field
						return;
					JsonArray chance = piece.getAsJsonArray("chance");
					JsonArray sections = piece.getAsJsonArray("chance_blocks");
					
					for(int j = 0; j < chance.size() && j < sections.size(); ++j)
					{
						if(random.nextInt(chance.get(j).getAsInt()) == 0)
						{
							JsonObject data = sections.get(j).getAsJsonObject(); //get block coordinates
							JsonArray coordArray = data.getAsJsonArray("coords");
							
							for(int k = 0; k < coordArray.size(); ++k)
							{
								JsonArray coords = coordArray.get(k).getAsJsonArray();
								int index = 0;
								int x = coords.get(index).getAsInt();
								++index;
								int y = coords.get(index).getAsInt() - 1;
								++index;
								int z = coords.get(index).getAsInt();
								++index;
								
								//convert coords to correct position based on orientation
								switch(orientation)
								{
									case 1: //West
										x = -x;
										z = -z;
										break;
									case 2: //North
										int tempN = x;
										x = -z;
										z = tempN;
										break;
									case 3: //South
										int tempS = -x;
										x = z;
										z = tempS;
										break;
								}
								
								//facing determines which side of the block to get the replacement block from
								BlockPos blkSource = pos.add(x, y, z);
								EnumFacing dir = getFacing(orientation, piece.get("facing").getAsString());
								blkSource = blkSource.offset(dir);
								blkSource = blkSource.up();
								
								world.setBlockState(pos.add(x, y, z), world.getBlockState(blkSource));
							}
						}
					}
				}
			}
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
	private void addBlocksJson(World world, JsonObject jsonObj, BlockPos pos, int orientation)
	{	
		if(!jsonObj.has("block") || !jsonObj.has("coords")) //missing required field, don't know what block to add/where to put them
			return;
	
		//get block type to add
		String blockType = jsonObj.get("block").getAsString();
		Block block = Block.getBlockFromName(blockType);
		
		if(block == null) //blockType incorrect, unknown block to add.
			return;
		
		IBlockState blkState = block.getDefaultState();
		Collection<IProperty<?>> propertyKeys = block.getDefaultState().getPropertyKeys();
		String value = "";
		
		//iterate over the block's properties and add any values that appear in the json.
		Iterator<IProperty<?>> itr = propertyKeys.iterator();
		while(itr.hasNext())
		{
			IProperty<?> property = itr.next();
			if(property.getName() == "facing" && jsonObj.has("facing"))
			{
				value = jsonObj.get("facing").getAsString();
				blkState = blkState.withProperty((PropertyDirection) property, getFacing(orientation, value));
			}
			else if(property.getName() == "axis" && jsonObj.has("axis"))
			{
				value = jsonObj.get("axis").getAsString();
				blkState = blkState.withProperty((PropertyEnum) property, getAxis(orientation, value));
			}
			else if(property.getName() == "variant" && jsonObj.has("variant"))
			{
				value = jsonObj.get("variant").getAsString();
				blkState = blkState.withProperty((PropertyEnum) property, EnumType.valueOf(value));
			}
			else if(property.getName() == "half" && jsonObj.has("half"))
			{
				value = jsonObj.get("half").getAsString();
				if(block.getUnlocalizedName().indexOf("door") != -1)
					blkState = blkState.withProperty((PropertyEnum) property, BlockDoor.EnumDoorHalf.valueOf(value));
				else if(block.getUnlocalizedName().indexOf("stair") != -1)
					blkState = blkState.withProperty((PropertyEnum) property, BlockStairs.EnumHalf.valueOf(value));
				else
					blkState = blkState.withProperty((PropertyEnum) property, EnumBlockHalf.valueOf(value));
			}
			else if(property.getName() == "part" && jsonObj.has("part"))
			{
				value = jsonObj.get("part").getAsString();
				blkState = blkState.withProperty((PropertyEnum) property, EnumPartType.valueOf(value));
			}
		}
		
		JsonArray coords = jsonObj.getAsJsonArray("coords"); //array of block positions. "coords" existence checked at beginning of function
		
		for(int i = 0; i < coords.size(); ++i)
		{
			JsonArray posArray = coords.get(i).getAsJsonArray(); //get first set of coords
			
			int index = 0;
			int x = posArray.get(index).getAsInt();
			++index;
			int y = posArray.get(index).getAsInt() - 1;
			++index;
			int z = posArray.get(index).getAsInt();
			++index;
			
			//convert coords to correct position based on orientation
			switch(orientation)
			{
				case 1: //West
					x = -x;
					z = -z;
					break;
				case 2: //North
					int tempN = x;
					x = -z;
					z = tempN;
					break;
				case 3: //South
					int tempS = -x;
					x = z;
					z = tempS;
					break;
			}
			
			world.setBlockState(pos.add(x, y, z), blkState);
			
			if(jsonObj.has("loot")) //process blocks with inventory differently (e.g. chests have loot tiers)
			{
				int lootPool = jsonObj.get("loot").getAsInt();
				addChestLoot(world, pos.add(x, y, z), lootPool);
			}
		}
	}
	
	/*
	 * Get the facing direction and rotate the face of the object from the default East facing to the
	 * correct facing for W, N, or S facing structures 
	 */
	private EnumFacing getFacing(int orientation, String facing)
	{
		
		EnumFacing dir = EnumFacing.byName(facing);//EnumFacing.EAST;
		
		switch(orientation)
		{
			case 1:	//West
				dir = dir.rotateY();
				dir = dir.rotateY();
				break;
			case 2:	//North
				dir = dir.rotateY();
				break;
			case 3:	//South
				dir = dir.rotateYCCW();
				break;
		}
		
		return dir;
	}
	
	/*
	 * Get the axis direction and rotate the object from the default East facing to the
	 * correct facing for W, N, or S facing structures
	 */
	private EnumAxis getAxis(int orientation, String facing)
	{
		EnumAxis axis = EnumAxis.valueOf(facing);//EnumAxis.Y;
		if(axis == EnumAxis.Y)
			return axis;
		
		if(axis == EnumAxis.NONE)
			return axis;
		
		if(orientation == 2 || orientation == 3)
			return (axis == EnumAxis.X) ? EnumAxis.Z : EnumAxis.X;
		
		return axis;
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
	private void addChestLoot(World world, BlockPos chestPos, int lootPool)
	{
		Random random = new Random();
		
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
