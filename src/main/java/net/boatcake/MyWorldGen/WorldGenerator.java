package net.boatcake.MyWorldGen;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.boatcake.MyWorldGen.utils.DirectionUtils;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.util.WeightedRandom;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.util.ForgeDirection;

import org.apache.logging.log4j.Level;

import com.google.common.collect.Sets;

import cpw.mods.fml.common.IWorldGenerator;

public class WorldGenerator implements IWorldGenerator {
	private Set<Schematic> worldgenFolderSchemList;
	public Set<Schematic> resourcePackSchemList;

	public WorldGenerator() {
		worldgenFolderSchemList = Sets.newHashSet();
		resourcePackSchemList = Sets.newHashSet();
	}

	public void addSchematicsFromDirectory(File schemDirectory) {
		File[] schemFiles = schemDirectory
				.listFiles(new SchematicFilenameFilter());
		worldgenFolderSchemList.clear();
		for (File schemFile : schemFiles) {
			try {
				addSchemFromStream(worldgenFolderSchemList,
						new FileInputStream(schemFile), schemFile.getName());
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	public void addSchemFromStream(Set<Schematic> section, InputStream stream,
			String name) throws IOException {
		Schematic newSchem = new Schematic(
				CompressedStreamTools.readCompressed(stream), name);
		section.add(newSchem);
	}

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world,
			IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
		if (world.getWorldInfo().isMapFeaturesEnabled() && random.nextBoolean()) {
			Set<Schematic> applicableSchematics = Sets.newHashSet();
			for (Schematic s : worldgenFolderSchemList) {
				if (s.info.matchesBiome(world.getBiomeGenForCoords(chunkX * 16,
						chunkZ * 16))) {
					applicableSchematics.add(s);
				}
			}
			for (Schematic s : resourcePackSchemList) {
				if (s.info.matchesBiome(world.getBiomeGenForCoords(chunkX * 16,
						chunkZ * 16))) {
					applicableSchematics.add(s);
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
						ForgeDirection randomDirection;
						if (schemToGenerate.info.lockRotation) {
							randomDirection = ForgeDirection.SOUTH;
						} else {
							randomDirection = DirectionUtils.cardinalDirections[random
									.nextInt(4)];
						}
						if (schemToGenerate.fitsIntoWorldAt(world, x, y, z,
								randomDirection)) {
							schemToGenerate.placeInWorld(world, x, y, z,
									randomDirection, true, true, random);
							MyWorldGen.log
									.log(Level.DEBUG,
											"Generated {} at {}, {}, {}; took {} tries",
											new Object[] {
													schemToGenerate.info.name,
													x, y, z, i + 1 });
							break;
						}
					}
				}
			}
		}
	}
}
