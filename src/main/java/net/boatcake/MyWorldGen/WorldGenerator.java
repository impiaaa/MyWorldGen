package net.boatcake.MyWorldGen;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.Level;

import net.boatcake.MyWorldGen.utils.DirectionUtils;
import net.boatcake.MyWorldGen.utils.SchematicFilenameFilter;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.WeightedRandom;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.fml.common.IWorldGenerator;

public class WorldGenerator implements IWorldGenerator {
	private List<Schematic> worldgenFolderSchemList;
	public List<Schematic> resourcePackSchemList;

	// Dumb lock to prevent stack overflow when structures cause chunks to
	// generate
	// TODO: Ideally, structures should not ever cause chunks to generate,
	// instead waiting until all required chunks are loaded to check for a
	// valid location. However, that's non-trivial, and would probably require
	// a chunk load handler (though I'd need that for retro-gen anyway)
	private int currentlyGenerating;
	private static int GENERATING_STACK_LIMIT = 5;

	public WorldGenerator() {
		worldgenFolderSchemList = new ArrayList<Schematic>();
		resourcePackSchemList = new ArrayList<Schematic>();
		currentlyGenerating = 0;
	}

	public void addSchematicsFromDirectory(File schemDirectory) {
		File[] schemFiles = schemDirectory.listFiles(new SchematicFilenameFilter());
		Arrays.sort(schemFiles);
		worldgenFolderSchemList.clear();
		for (File schemFile : schemFiles) {
			try {
				addSchemFromStream(worldgenFolderSchemList, new FileInputStream(schemFile), schemFile.getName());
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		MyWorldGen.log.log(Level.INFO, "Loaded {} schematics from {}", worldgenFolderSchemList.size(),
				schemDirectory.toString());
	}

	public void addSchemFromStream(Collection<Schematic> section, InputStream stream, String name) throws IOException {
		Schematic newSchem = new Schematic(CompressedStreamTools.readCompressed(stream), name);
		section.add(newSchem);
	}

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator,
			IChunkProvider chunkProvider) {
		if (currentlyGenerating > GENERATING_STACK_LIMIT) {
			return;
		}
		currentlyGenerating++;
		if (world.getWorldInfo().isMapFeaturesEnabled() && random.nextDouble() < MyWorldGen.baseGenerateChance) {
			List<WeightedRandom.Item> applicableSchematics = new ArrayList<WeightedRandom.Item>();
			BlockPos chunkPos = new BlockPos(chunkX * 16, 0, chunkZ * 16);
			for (Schematic s : worldgenFolderSchemList) {
				if (s.info.matchesBiome(world.getBiomeGenForCoords(chunkPos))) {
					applicableSchematics.add(new WeightedRandomSchematic(s));
				}
			}
			for (Schematic s : resourcePackSchemList) {
				if (s.info.matchesBiome(world.getBiomeGenForCoords(chunkPos))) {
					applicableSchematics.add(new WeightedRandomSchematic(s));
				}
			}
			if (!applicableSchematics.isEmpty()) {
				WeightedRandom.Item noStructureItem = new WeightedRandom.Item(MyWorldGen.generateNothingWeight);
				applicableSchematics.add(noStructureItem);
				WeightedRandom.Item selectedItem = WeightedRandom.getRandomItem(random, applicableSchematics);
				if (selectedItem != noStructureItem) {
					Schematic schemToGenerate = ((WeightedRandomSchematic) selectedItem).schematic;
					if (schemToGenerate.info.fuzzyMatching) {
						Chunk chunk = world.getChunkFromChunkCoords(chunkX, chunkZ);
						EnumFacing randomDirection;
						if (schemToGenerate.info.lockRotation) {
							randomDirection = EnumFacing.SOUTH;
						} else {
							randomDirection = DirectionUtils.cardinalDirections[random.nextInt(4)];
						}
						BlockPos pos = schemToGenerate.getFuzzyMatchingLocation(chunk, randomDirection, random);
						if (pos != null) {
							schemToGenerate.placeInWorld(world, pos, randomDirection, true, true, true, random);
							MyWorldGen.log.log(Level.DEBUG, "Generated {} at {}, {}, {}",
									new Object[] { schemToGenerate.info.name, pos.getX(), pos.getY(), pos.getZ() });
						}
					} else {
						for (int i = 0; i < MyWorldGen.generateTries; i++) {
							int x = random.nextInt(16) + chunkPos.getX();
							int y = random.nextInt(world.getHeight());
							int z = random.nextInt(16) + chunkPos.getZ();
							BlockPos pos = new BlockPos(x, y, z);
							EnumFacing randomDirection;
							if (schemToGenerate.info.lockRotation) {
								randomDirection = EnumFacing.SOUTH;
							} else {
								randomDirection = DirectionUtils.cardinalDirections[random.nextInt(4)];
							}
							if (schemToGenerate.fitsIntoWorldAt(world, pos, randomDirection)) {
								schemToGenerate.placeInWorld(world, pos, randomDirection, true, true, true, random);
								MyWorldGen.log.log(Level.DEBUG, "Generated {} at {}, {}, {}; took {} tries",
										new Object[] { schemToGenerate.info.name, x, y, z, i + 1 });
								break;
							}
						}
					}
				}
			}
		}
		currentlyGenerating--;
	}
}
