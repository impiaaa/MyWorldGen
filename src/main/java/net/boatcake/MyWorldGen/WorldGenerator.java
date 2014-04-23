package net.boatcake.MyWorldGen;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

import net.boatcake.MyWorldGen.utils.DirectionUtils;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.WeightedRandomItem;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.common.IWorldGenerator;

public class WorldGenerator implements IWorldGenerator {
	private Map<File, Schematic> schemList;

	public WorldGenerator() {
		schemList = new HashMap<File, Schematic>();
	}

	public void addSchematicsFromDirectory(File schemDirectory) {
		File[] schemFiles = schemDirectory
				.listFiles(new SchematicFilenameFilter());
		for (File schemFile : schemFiles) {
			try {
				addSchemFromFile(schemFile);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		/*
		 * ArrayList<File> filesToRemove = new ArrayList<File>(); for (File
		 * schemFile : schemList.keySet()) { if
		 * (!Arrays.asList(schemFiles).contains(schemFile)) {
		 * filesToRemove.add(schemFile); } } for (File schemFile :
		 * filesToRemove) { schemList.remove(schemFile); }
		 */
	}

	public void addSchemFromFile(File schemFile) throws FileNotFoundException,
			IOException {
		addSchemFromStream(new FileInputStream(schemFile), schemFile);
	}

	public void addSchemFromStream(InputStream stream, File schemFile)
			throws IOException {
		if (!schemList.containsKey(schemFile)) {
			Schematic newSchem = new Schematic(
					CompressedStreamTools.readCompressed(stream),
					schemFile.getName());
			schemList.put(schemFile, newSchem);
		}
	}

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world,
			IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
		if (!schemList.isEmpty() && random.nextBoolean()) {
			ArrayList applicableSchematics = new ArrayList<WeightedRandomItem>();
			for (Schematic s : schemList.values()) {
				if (s.matchesBiome(world.getBiomeGenForCoords(chunkX * 16,
						chunkZ * 16))) {
					applicableSchematics.add(s);
				}
			}
			if (!applicableSchematics.isEmpty()) {
				WeightedRandomItem noStructureItem = new WeightedRandomItem(
						MyWorldGen.generateNothingWeight);
				WeightedRandomItem selectedItem = WeightedRandom.getRandomItem(
						random, applicableSchematics);
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
											"Generated {0} at {1}, {2}, {3}; took {4} tries",
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
