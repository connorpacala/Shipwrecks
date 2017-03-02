package com.winslow.shipwrecks;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ShipwreckConfig {
	private static String[] names;
	private static int[] oceanWeights;
	private static int[] beachWeights;
	
	public static void initConfiguration(FMLPreInitializationEvent event)
	{
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
		
		Property nameInstr = config.get("Structures", "Instructions", "This is a list of the structures that will be used in generation. These MUST be seperated by a ',' and match the name of the JSON filename for the structure (without the .json extension). If you dislike a specific structure, remove it from the list and it will no longer generate.");
		setNames(config.get("Structures", "Names", "rowboat,sailboatup,sailboatside,sloop,schooner,waverunner").getString().split(","));
		int[] defaultOceanWeights = {30, 20, 15, 15, 10, 7, 3};
		setOceanWeights(config.get("Structures", "Ocean Generation Weights", defaultOceanWeights).getIntList());
		int [] defaultBeachWeights = {30, 20, 15, 15, 10, 7, 3};
		setBeachWeights(config.get("Structures", "Beach Generation Weights", defaultBeachWeights).getIntList());
		
		config.save();
	}

	public static String[] getNames() {
		return names;
	}

	public static void setNames(String[] names) {
		ShipwreckConfig.names = names;
	}

	public static int[] getOceanWeights() {
		return oceanWeights;
	}

	public static void setOceanWeights(int[] weights) {
		ShipwreckConfig.oceanWeights = weights;
	}
	
	public static int[] getBeachWeights() {
		return beachWeights;
	}

	public static void setBeachWeights(int[] weights) {
		ShipwreckConfig.beachWeights = weights;
	}
}
