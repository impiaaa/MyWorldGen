package net.boatcake.MyWorldGen;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.boatcake.MyWorldGen.utils.DirectionUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedRandom;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.util.ForgeDirection;

import org.apache.logging.log4j.Level;

import com.google.common.collect.Sets;

import cpw.mods.fml.common.IWorldGenerator;

public class WorldGenerator implements IWorldGenerator {
	private Map<File, Set<Schematic>> schemList;

	public WorldGenerator() {
		schemList = new HashMap<File, Set<Schematic>>();
	}

	public void addSchematicsFromDirectory(File schemDirectory) {
		File[] schemFiles = schemDirectory
				.listFiles(new SchematicFilenameFilter());
		Set<Schematic> section = getSection(schemDirectory, schemFiles.length);
		section.clear();
		for (File schemFile : schemFiles) {
			try {
				addSchemFromStream(section, new FileInputStream(schemFile), schemFile.getName());
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	public void addSchemFromStream(Set<Schematic> section, InputStream stream, String name)
			throws IOException {
		Schematic newSchem = new Schematic(
				CompressedStreamTools.readCompressed(stream),
				name);
		section.add(newSchem);
		MyWorldGen.log.debug("Added schematic: %s", name);
	}
	
	public Set<Schematic> getSection(File origin, int expectedSize) {
		if (schemList.containsKey(origin)) {
			return schemList.get(origin);
		}
		else {
			Set<Schematic> section = Sets.newHashSetWithExpectedSize(expectedSize);
			schemList.put(origin, section);
			return section;
		}
	}

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world,
			IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
		if (!schemList.isEmpty() && random.nextBoolean()) {
			ArrayList applicableSchematics = new ArrayList<WeightedRandom.Item>();
			for (Set<Schematic> section : schemList.values()) {
				for (Schematic s : section) {
					if (s.matchesBiome(world.getBiomeGenForCoords(chunkX * 16,
							chunkZ * 16))) {
						applicableSchematics.add(s);
					}
				}
			}
			if (!applicableSchematics.isEmpty()) {
				WeightedRandom.Item noStructureItem = new WeightedRandom.Item(
						MyWorldGen.generateNothingWeight);
				WeightedRandom.Item selectedItem = WeightedRandom
						.getRandomItem(random, applicableSchematics);
				if (selectedItem != noStructureItem) {
					Schematic schemToGenerate = (Schematic) selectedItem;
					for (int i = 0; i < MyWorldGen.generateTries; i++) {
						int x = random.nextInt(16) + chunkX * 16;
						int y = random.nextInt(world.getHeight());
						int z = random.nextInt(16) + chunkZ * 16;
						ForgeDirection randomDirection = DirectionUtils.cardinalDirections[random
								.nextInt(4)];
						if (schemToGenerate.fitsIntoWorldAt(world, x, y, z,
								randomDirection)) {
							schemToGenerate.placeInWorld(world, x, y, z,
									randomDirection, true, true, random);
							MyWorldGen.log
									.log(Level.OFF,
											"Generated {} at {}, {}, {}; took {} tries",
											new Object[] {
													schemToGenerate.name, x, y,
													z, i + 1 });
							break;
						}
					}
				}
			}
		}
	}
}
