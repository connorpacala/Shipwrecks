package com.winslow.shipwrecks;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Random;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockBed.EnumPartType;
import net.minecraft.block.BlockLog.EnumAxis;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockPlanks.EnumType;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockSlab.EnumBlockHalf;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockStairs.EnumHalf;
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
		
		String variant = ""; //value to specify variation on objects like wood planks (e.g. oak, spruce, etc)
		if(jsonObj.has("variant")) //get block variation (e.g. oak or spruce for wood planks)
			variant = jsonObj.get("variant").getAsString();
		
		String facing = "";
		if(jsonObj.has("facing"))
			facing = jsonObj.get("facing").getAsString();
		
		String half = ""; //value to specify variation on objects like wood slabs (e.g. top/bottom)
		if(jsonObj.has("half"))
			half = jsonObj.get("half").getAsString();
		
		String part = ""; //value to specify variation on objects like beds (e.g. head/foot)
		if(jsonObj.has("part"))
			part = jsonObj.get("part").getAsString();
		
		IBlockState blkState = block.getDefaultState();
		
		Collection<IProperty<?>> properties = block.getDefaultState().getPropertyKeys();
		PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
		PropertyEnum<EnumAxis> AXIS = PropertyEnum.create("axis", EnumAxis.class);
		
		System.out.println(block.getUnlocalizedName());
		
		//if(!facing.isEmpty())
		//{
		//	if(block == Blocks.DISPENSER)
		//		System.out.println(properties.toString());
			//Blocks can either have the facing property or the axis property but not both...at least it wouldn't make sense to have both
		if(properties.contains(FACING))
		{
			blkState = blkState.withProperty(FACING, getFacing(orientation, facing));
		}
		else if(properties.contains(AXIS))
		{
			blkState = blkState.withProperty(AXIS, getAxis(orientation, facing));
		}
		//}
		
		if(!variant.isEmpty())
		{
			PropertyEnum<EnumType> VARIANT;
			if(block.getUnlocalizedName().indexOf("log") != -1)
				VARIANT = BlockOldLog.VARIANT;
			else //if(block.getUnlocalizedName().indexOf("plank") != -1)
				VARIANT = BlockPlanks.VARIANT;
			blkState = blkState.withProperty(VARIANT, EnumType.valueOf(variant));
		}
		
		if(!half.isEmpty())
		{
			if(block.getUnlocalizedName().indexOf("stair") != -1)
			{
				PropertyEnum<EnumHalf> HALF = BlockStairs.HALF;
				blkState = blkState.withProperty(HALF, EnumHalf.valueOf(half));
			}
			else
			{
				PropertyEnum<EnumBlockHalf> HALF = BlockSlab.HALF;
				blkState = blkState.withProperty(HALF, EnumBlockHalf.valueOf(half));
			}
		}
		
		if(!part.isEmpty())
		{
			PropertyEnum<EnumPartType> PART = BlockBed.PART;
			blkState = blkState.withProperty(PART, EnumPartType.valueOf(part));
		}
		
		
		if(jsonObj.has("loot")) //process blocks with inventory differently (e.g. chests have loot tiers)
		{
			addChestsJson(world, jsonObj, pos, block, orientation);
		}
		else //regular blocks, no loot added to them
		{
			JsonArray coords = jsonObj.getAsJsonArray("coords"); //array of block positions. "coords" existence checked at beginning of function
			addBlocksFromArray(world, pos, coords, blkState, variant, orientation, facing);
		}
	}
	
	/*
	 * Attempts to add chests from the passed json file to the world
	 */
	private void addChestsJson(World world, JsonObject jsonObj, BlockPos pos, Block block, int orientation) //add ship mast to the world
	{
		JsonArray posArray = jsonObj.getAsJsonArray("coords"); //get array of chest objects
		//add appropriate loot based on the loot pool that 
		int lootPool = jsonObj.get("loot").getAsInt();
		String facing = jsonObj.get("facing").getAsString();
		int numLoops = (posArray.get(0).isJsonArray()) ? posArray.size() : 1; //1 loop if not an array (single entry). Fixes an error when reading an array of one value
		
		for(int i = 0; i < numLoops; ++i)
		{
			JsonArray coords = (posArray.get(0).isJsonArray()) ? posArray.get(i).getAsJsonArray() : posArray;
			
			int index = 0;
			int x = coords.get(index).getAsInt();
			++index;
			int y = coords.get(index).getAsInt() - 1;
			++index;
			int z = coords.get(index).getAsInt();
			++index;
			//block = Block.getBlockById(posArray.get(index).getAsInt());
			//++index;
			
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
			//int md = coords.get(index).getAsInt();
			
			addBlock(world, pos.getX() + x, pos.getY() + y, pos.getZ() + z, block, getFacing(orientation, facing));
			addChestLoot(world, pos.getX() + x, pos.getY() + y, pos.getZ() + z, lootPool);
		}
	}
	
	//Get the facing direction and rotate the face of the object from the default East facing to the 
	//correct facing for W, N, or S facing structures
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
	
	//Get the axis direction and rotate the object from the default East facing to the 
	//correct facing for W, N, or S facing structures
	private EnumAxis getAxis(int orientation, String facing)
	{
		
		EnumAxis axis = EnumAxis.valueOf(facing);//EnumAxis.Y;
		if(axis == EnumAxis.Y)
			return axis;
		
		if(orientation == 2 || orientation == 3)
			return (facing == "X") ? EnumAxis.Z : EnumAxis.X;
		
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
	 * Loops through an array of coordinates and adds blocks of type block with the correct orientation
	 */
	private void addBlocksFromArray(World world, BlockPos pos, JsonArray coords, IBlockState blkState, String variant, int orientation, String facing)
	{	
		for(int i = 0; i < coords.size(); ++i)
		{
			JsonArray posArray = coords.get(i).getAsJsonArray(); //get first set of coords
			
			if(posArray.size() >= 3) //json array has at least 3 values (x, y, z)
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
			}
		}
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
		world.setBlockState(new BlockPos(x, y, z), block.getStateFromMeta(metadata));
	}	
	
	@SuppressWarnings("deprecation")
	private void addBlock(World world, int x, int y, int z, Block block, int metadata, EnumFacing facing)
	{
		PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
		world.setBlockState(new BlockPos(x, y, z), block.getStateFromMeta(metadata).withProperty(FACING, facing));
	}
	
	private void addBlock(World world, int x, int y, int z, Block block, EnumFacing facing)
	{
		PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
		world.setBlockState(new BlockPos(x, y, z), block.getDefaultState().withProperty(FACING, facing));
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
