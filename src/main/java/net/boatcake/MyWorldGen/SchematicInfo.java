package net.boatcake.MyWorldGen;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.ChestGenHooks;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class SchematicInfo {
	public ArrayList<String> excludeBiomes;
	public String chestType;
	public ArrayList<String> onlyIncludeBiomes;
	public String name;
	public boolean lockRotation;
	public int randomWeight;
	public boolean generateSpawners;
	public boolean fuzzyMatching;
	public boolean terrainSmoothing;

	public SchematicInfo() {
		chestType = ChestGenHooks.DUNGEON_CHEST;
		excludeBiomes = null;
		onlyIncludeBiomes = null;
		lockRotation = false;
		randomWeight = 10;
		fuzzyMatching = false;
		terrainSmoothing = false;
		generateSpawners = true;
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

		if (tag.hasKey("generateSpawners")) {
			generateSpawners = tag.getBoolean("generateSpawners");
		}

		if (tag.hasKey("fuzzyMatching")) {
			fuzzyMatching = tag.getBoolean("fuzzyMatching");
		}

		if (tag.hasKey("terrainSmoothing")) {
			terrainSmoothing = tag.getBoolean("terrainSmoothing");
		}
	}

	public void writeToNBT(NBTTagCompound base) {
		base.setString("chestType", chestType);

		if (excludeBiomes != null) {
			NBTTagList t = new NBTTagList();
			for (String biome : excludeBiomes) {
				t.appendTag(new NBTTagString(biome));
			}
			base.setTag("excludeBiomes", t);
		}

		if (onlyIncludeBiomes != null) {
			NBTTagList t = new NBTTagList();
			for (String biome : onlyIncludeBiomes) {
				t.appendTag(new NBTTagString(biome));
			}
			base.setTag("onlyIncludeBiomes", t);
		}

		base.setBoolean("lockRotation", lockRotation);
		base.setInteger("randomWeight", randomWeight);
		base.setBoolean("generateSpawners", generateSpawners);
		base.setBoolean("fuzzyMatching", fuzzyMatching);
		base.setBoolean("terrainSmoothing", terrainSmoothing);
	}

	public static boolean containsIgnoreCase(List<String> list, String thing) {
		for (String other : list) {
			if (thing.equalsIgnoreCase(other)) {
				return true;
			}
		}
		return false;
	}

	public void readFromJson(JsonObject jsonobject) {
		if (jsonobject.has("chestType")) {
			this.chestType = jsonobject.get("chestType").getAsString();
		}

		if (jsonobject.has("excludeBiomes")) {
			JsonArray l = jsonobject.get("excludeBiomes").getAsJsonArray();
			this.excludeBiomes = new ArrayList<String>(l.size());
			for (JsonElement el : l) {
				this.excludeBiomes.add(el.getAsString());
			}
		}

		if (jsonobject.has("onlyIncludeBiomes")) {
			JsonArray l = jsonobject.get("onlyIncludeBiomes").getAsJsonArray();
			this.onlyIncludeBiomes = new ArrayList<String>(l.size());
			for (JsonElement el : l) {
				this.onlyIncludeBiomes.add(el.getAsString());
			}
		}

		if (jsonobject.has("lockRotation")) {
			this.lockRotation = jsonobject.get("lockRotation").getAsBoolean();
		}

		if (jsonobject.has("randomWeight")) {
			this.randomWeight = jsonobject.get("randomWeight").getAsInt();
		}

		if (jsonobject.has("generateSpawners")) {
			this.generateSpawners = jsonobject.get("generateSpawners")
					.getAsBoolean();
		}

		if (jsonobject.has("fuzzyMatching")) {
			this.fuzzyMatching = jsonobject.get("fuzzyMatching").getAsBoolean();
		}

		if (jsonobject.has("terrainSmoothing")) {
			this.terrainSmoothing = jsonobject.get("terrainSmoothing")
					.getAsBoolean();
		}
	}
}