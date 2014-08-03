package net.boatcake.MyWorldGen;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.ChestGenHooks;

public class SchematicInfo {
	public ArrayList<String> excludeBiomes;
	public String chestType;
	public ArrayList<String> onlyIncludeBiomes;
	public String name;

	public SchematicInfo() {
		chestType = ChestGenHooks.DUNGEON_CHEST;
		excludeBiomes = null;
		onlyIncludeBiomes = null;
	}

	public boolean matchesBiome(BiomeGenBase biome) {
		if ((excludeBiomes != null)
				&& (containsIgnoreCase(excludeBiomes, biome.biomeName))) {
			return false;
		}
		if ((onlyIncludeBiomes != null)
				&& (!containsIgnoreCase(onlyIncludeBiomes, biome.biomeName))) {
			return false;
		}
		return true;
	}

	public static boolean containsIgnoreCase(List<String> list, String thing) {
		for (String other : list) {
			if (thing.equalsIgnoreCase(other)) {
				return true;
			}
		}
		return false;
	}
}