package net.boatcake.MyWorldGen;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.ChestGenHooks;

public class SchematicInfo {
	public ArrayList<String> excludeBiomes;
	public String chestType;
	public ArrayList<String> onlyIncludeBiomes;
	public String name;
	public boolean lockRotation;
	public int randomWeight;

	public SchematicInfo() {
		chestType = ChestGenHooks.DUNGEON_CHEST;
		excludeBiomes = null;
		onlyIncludeBiomes = null;
		lockRotation = false;
		randomWeight = 10;
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

	public void readFromNBT(NBTTagCompound tag) {
		if (tag.hasKey("chestType")) {
			chestType = tag.getString("chestType");
		}

		if (tag.hasKey("excludeBiomes")) {
			NBTTagList l = (NBTTagList) tag.getTag("excludeBiomes");
			excludeBiomes = new ArrayList<String>(l.tagCount());
			for (int i = 0; i < l.tagCount(); i++) {
				excludeBiomes.add(l.getStringTagAt(i));
			}
		}

		if (tag.hasKey("onlyIncludeBiomes")) {
			NBTTagList l = (NBTTagList) tag.getTag("onlyIncludeBiomes");
			onlyIncludeBiomes = new ArrayList<String>(l.tagCount());
			for (int i = 0; i < l.tagCount(); i++) {
				onlyIncludeBiomes.add(l.getStringTagAt(i));
			}
		}

		if (tag.hasKey("lockRotation")) {
			lockRotation = tag.getBoolean("lockRotation");
		}

		if (tag.hasKey("randomWeight")) {
			randomWeight = tag.getInteger("randomWeight");
		}
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