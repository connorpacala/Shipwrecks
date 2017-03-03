package com.winslow.shipwrecks;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ShipwreckConfig {
	private static String[] names;
	private static int[] oceanWeights;
	private static int[] beachWeights;
	
	/*
	 * load the configuration and initialize values
	 */
	public static void initConfiguration(FMLPreInitializationEvent event)
	{
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
		
		config.get("Structures", "A: Instructions", "This is a list of the structures that will be used in generation. These MUST be seperated by a ',' and match the name of the JSON filename for the structure (without the .json extension).");
		setNames(config.get("Structures", "Names", "rowboat,sailboatup,sailboatside,sloop,schooner,waverunner").getString().split(","));
		int[] defaultOceanWeights = {30, 20, 15, 15, 10, 7, 3};
		config.get("Weights", "A: Instructions", "These values determine the chance the above structures have of spawning. The first value is the chance of NO structures spawning on that spot, the remaining values correspond to the stucture names listed above IN ORDER. So, by default, the second number in the list is the weight for the rowboat (the first name in the list). Set the value to 0 to prevent it from being counted/generating.");
		setOceanWeights(config.get("Weights", "Ocean Generation Weights", defaultOceanWeights).getIntList());
		int [] defaultBeachWeights = {30, 20, 15, 15, 10, 7, 3};
		setBeachWeights(config.get("Weights", "Beach Generation Weights", defaultBeachWeights).getIntList());
		
		config.save();
	}

	/*
	 * Getter for names, an array of structure names
	 */
	public static String[] getNames() {
		return names;
	}

	/*
	 * Setter for names, an array of structure names
	 */
	public static void setNames(String[] names) {
		ShipwreckConfig.names = names;
	}

	/*
	 * Getter for ocean weights, an int array of weights for spawning structures in ocean biomes
	 */
	public static int[] getOceanWeights() {
		return oceanWeights;
	}

	/*
	 * Setter for ocean weights, an int array of weights for spawning structures in ocean biomes
	 */
	public static void setOceanWeights(int[] weights) {
		ShipwreckConfig.oceanWeights = weights;
	}
	
	/*
	 * Getter for beach weights, an int array of weights for spawning structures in beach biomes
	 */
	public static int[] getBeachWeights() {
		return beachWeights;
	}

	/*
	 * Setter for ocean weights, an int array of weights for spawning structures in ocean biomes
	 */
	public static void setBeachWeights(int[] weights) {
		ShipwreckConfig.beachWeights = weights;
	}
}
