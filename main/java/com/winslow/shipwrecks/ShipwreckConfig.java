package com.winslow.shipwrecks;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

class ShipwreckConfig {
    private static String[] names;
    private static int[] oceanWeights;
    private static int[] beachWeights;
    private static int minDist;
    private static int maxDist;
    private static boolean includeDivingArmor;

    /*
     * load the configuration and initialize values
     */
    static void initConfiguration(FMLPreInitializationEvent event) {
        Configuration config = new Configuration(event.getSuggestedConfigurationFile());
        config.load();

        config.get("Distance", "A: Instructions", "This section controls the distance between shipwrecks. I recommend keeping the max distance relatively low as you will have wrecks noticeably pop-in otherwise when they spawn (e.g. if you set it to 100 chunks, you could see a wreck pop in 100 chunks behind you). I also recommend you keep minDist above 3 to prevent ships from spawning on each other.");
        setMaxDist(config.get("Distance", "maxDist", 5, "maxDist = the maximum number of chunks between wrecks (with 100% spawn rates of wrecks, actually works out to be [maxDist * 2 - minDist] as the actual maximum distance between wrecks)").getInt());
        setMinDist(config.get("Distance", "minDist", 3, "minDist = the minimum chunks between wrecks, wrecks will not be closer than this.").getInt());

        String[] defaultStructures = {"rowboat", "sailboatup", "sailboatside", "sloop", "schooner", "waverunner"};
        config.get("Structures", "A: Instructions", "This is a list of the structures that will be used in generation. These MUST match the name of the JSON filename for the structure (without the .json extension).");
        setNames(config.get("Structures", "Names", defaultStructures).getStringList());
        int[] defaultOceanWeights = {0, 20, 15, 15, 10, 7, 3};
        config.get("Weights", "A: Instructions", "These values determine the chance the above structures have of spawning. The first value is the chance of NO structures spawning on that spot, the remaining values correspond to the stucture names listed above IN ORDER. So, by default, the second number in the list is the weight for the rowboat (the first name in the list). Set the value to 0 to prevent it from being counted/generating.");
        setOceanWeights(config.get("Weights", "Ocean Generation Weights", defaultOceanWeights).getIntList());
        int[] defaultBeachWeights = {0, 20, 15, 15, 10, 7, 3};
        setBeachWeights(config.get("Weights", "Beach Generation Weights", defaultBeachWeights).getIntList());

        setIncludeDivingArmor(config.get("Items", "Include Diving Armor", true).getBoolean(false));

        config.save();
    }

    /*
     * Getter for names, an array of structure names
     */
    static String[] getNames() {
        return names;
    }

    /*
     * Setter for names, an array of structure names
     */
    private static void setNames(String[] names) {
        ShipwreckConfig.names = names;
    }

    /*
     * Getter for ocean weights, an int array of weights for spawning structures in ocean biomes
     */
    static int[] getOceanWeights() {
        return oceanWeights;
    }

    /*
     * Setter for ocean weights, an int array of weights for spawning structures in ocean biomes
     */
    private static void setOceanWeights(int[] weights) {
        ShipwreckConfig.oceanWeights = weights;
    }

    /*
     * Getter for beach weights, an int array of weights for spawning structures in beach biomes
     */
    static int[] getBeachWeights() {
        return beachWeights;
    }

    /*
     * Setter for ocean weights, an int array of weights for spawning structures in ocean biomes
     */
    private static void setBeachWeights(int[] weights) {
        ShipwreckConfig.beachWeights = weights;
    }

    static int getMinDist() {
        return minDist;
    }

    /*
     * Setter for ocean weights, an int array of weights for spawning structures in ocean biomes
     */
    private static void setMinDist(int minDist) {
        ShipwreckConfig.minDist = minDist;
    }

    static int getMaxDist() {
        return maxDist;
    }

    /*
     * Setter for max dist, the maximum distance between shipwrecks
     */
    private static void setMaxDist(int maxDist) {
        ShipwreckConfig.maxDist = maxDist;
    }

    /*
     * Getter for names, an array of structure names
     */
    static boolean getIncludeDivingArmor() {
        return includeDivingArmor;
    }

    /*
     * Setter for names, an array of structure names
     */
    private static void setIncludeDivingArmor(Boolean include) {
        ShipwreckConfig.includeDivingArmor = include;
    }
}
