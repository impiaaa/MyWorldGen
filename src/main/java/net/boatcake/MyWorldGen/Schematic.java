package net.boatcake.MyWorldGen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import net.boatcake.MyWorldGen.blocks.BlockAnchorLogic;
import net.boatcake.MyWorldGen.blocks.BlockAnchorMaterial;
import net.boatcake.MyWorldGen.blocks.BlockAnchorMaterialLogic;
import net.boatcake.MyWorldGen.blocks.BlockPlacementLogic;
import net.boatcake.MyWorldGen.utils.DirectionUtils;
import net.boatcake.MyWorldGen.utils.WorldUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.Vec3;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.common.DungeonHooks;
import net.minecraftforge.common.util.ForgeDirection;

import org.apache.logging.log4j.Level;

public class Schematic extends WeightedRandom.Item {
	// cache of the x,y,z locations of all anchor blocks
	private ArrayList<Integer[]> anchorBlockLocations;
	private int blocks[][][];
	public String chestType;
	public NBTTagList entities;
	public ArrayList<String> excludeBiomes;
	public short height;
	public Map<Integer, Block> idMap;
	public short length;
	public Map<Integer, BlockAnchorLogic> matchingMap;
	private int meta[][][];
	public String name;
	public ArrayList<String> onlyIncludeBiomes;
	public Map<Integer, BlockPlacementLogic> placingMap;
	public NBTTagList tileEntities;
	public short width;

	public Schematic() {
		this((short) 0, (short) 0, (short) 0, null);
	}

	public Schematic(NBTTagCompound tag, String n) {
		super(tag.hasKey("randomWeight") ? tag.getInteger("randomWeight") : 10);
		name = n;
		if (!tag.getString("Materials").equals("Alpha")) {
			throw new RuntimeException(
					"Non-Alpha schematics are not supported!");
		}
		width = tag.getShort("Width");
		height = tag.getShort("Height");
		length = tag.getShort("Length");

		idMap = new HashMap<Integer, Block>(2);
		matchingMap = new HashMap<Integer, BlockAnchorLogic>();
		placingMap = new HashMap<Integer, BlockPlacementLogic>();

		if (tag.hasKey("MWGIDMap")) {
			NBTTagCompound mapTag = tag.getCompoundTag("MWGIDMap");
			for (Object o : mapTag.func_150296_c()) {
				String blockName = (String) o;
				int id = mapTag.getInteger(blockName);
				Block block = Block.getBlockFromName(blockName);
				if (block != null) {
					idMap.put(id, block);
				}
				else if(!BlockAnchorLogic.isAnchorBlock(blockName)
						&& !BlockPlacementLogic.placementLogicExists(blockName)) {
					MyWorldGen.log.log(Level.WARN,
							"Can't find a block named {}", blockName);
				}
				if (BlockAnchorLogic.isAnchorBlock(blockName)) {
					matchingMap.put(id, BlockAnchorLogic.get(blockName));
				}
				if (BlockPlacementLogic.placementLogicExists(blockName)) {
					placingMap.put(id, BlockPlacementLogic.get(blockName));
				}
			}
		} else {
			if (tag.hasKey("ignoreBlockId")) {
				if (MyWorldGen.ignoreBlock != null) {
					idMap.put(tag.getInteger("ignoreBlockId"),
							MyWorldGen.ignoreBlock);
				}
				placingMap.put(tag.getInteger("ignoreBlockId"),
						BlockPlacementLogic.get("ignore"));
			} else {
				MyWorldGen.log
						.log(Level.WARN,
								"Schematic file {} has no ignoreBlockId tag, defaulting to ID from config",
								name);
			}

			if (tag.hasKey("anchorBlockId")) {
				int id = tag.getInteger("anchorBlockId");
				if (MyWorldGen.materialAnchorBlock != null) {
					idMap.put(id, MyWorldGen.materialAnchorBlock);
				}
				placingMap.put(id, BlockPlacementLogic.get("anchor"));
				matchingMap.put(id, BlockAnchorLogic.get("anchor"));
			} else {
				MyWorldGen.log
						.log(Level.WARN,
								"Schematic file {} has no anchorBlockId tag, defaulting to ID from config",
								name);
			}
		}

		anchorBlockLocations = new ArrayList<Integer[]>();
		blocks = new int[width][height][length];
		meta = new int[width][height][length];
		byte blockBytes[] = tag.getByteArray("Blocks");
		byte metaBytes[] = tag.getByteArray("Data");
		byte blockUpperBits[];
		if (tag.hasKey("AddBlocks")) {
			blockUpperBits = tag.getByteArray("AddBlocks");
		} else {
			blockUpperBits = null;
		}
		// YZX order
		for (int y = 0, blockIdx = 0; y < height; y++) {
			for (int z = 0; z < length; z++) {
				for (int x = 0; x < width; x++, blockIdx++) {
					blocks[x][y][z] = (blockBytes[blockIdx]) & 0xFF;
					meta[x][y][z] = (metaBytes[blockIdx]) & 0xFF;
					if (blockUpperBits != null) {
						blocks[x][y][z] |= (blockUpperBits[blockIdx >> 1] << ((blockIdx % 2 == 0) ? 4
								: 8)) & 0xF00;
					}
					if (matchingMap.containsKey(blocks[x][y][z])) {
						anchorBlockLocations.add(new Integer[] { x, y, z });
					}
				}
			}
		}

		entities = (NBTTagList) tag.getTag("Entities");
		tileEntities = (NBTTagList) tag.getTag("TileEntities");

		if (anchorBlockLocations.isEmpty() && name != null) {
			MyWorldGen.log.log(Level.WARN,
					"No anchors found in schematic {}", name);
		}

		if (tag.hasKey("chestType")) {
			chestType = tag.getString("chestType");
		} else {
			chestType = ChestGenHooks.DUNGEON_CHEST;
		}

		if (tag.hasKey("excludeBiomes")) {
			NBTTagList l = (NBTTagList) tag.getTag("excludeBiomes");
			excludeBiomes = new ArrayList<String>(l.tagCount());
			for (int i = 0; i < l.tagCount(); i++) {
				excludeBiomes.add(l.getStringTagAt(i));
			}
		} else {
			excludeBiomes = null;
		}

		if (tag.hasKey("onlyIncludeBiomes")) {
			NBTTagList l = (NBTTagList) tag.getTag("onlyIncludeBiomes");
			onlyIncludeBiomes = new ArrayList<String>(l.tagCount());
			for (int i = 0; i < l.tagCount(); i++) {
				onlyIncludeBiomes.add(l.getStringTagAt(i));
			}
		} else {
			onlyIncludeBiomes = null;
		}
	}

	public Schematic(short w, short h, short d, String n) {
		super(10);
		width = w;
		height = h;
		length = d;
		blocks = new int[w][h][d];
		meta = new int[w][h][d];
		entities = null;
		tileEntities = null;
		idMap = new HashMap<Integer, Block>();
		matchingMap = new HashMap<Integer, BlockAnchorLogic>();
		placingMap = new HashMap<Integer, BlockPlacementLogic>();
		anchorBlockLocations = new ArrayList<Integer[]>();
		chestType = ChestGenHooks.DUNGEON_CHEST;
		excludeBiomes = new ArrayList<String>();
		excludeBiomes.add(BiomeGenBase.hell.biomeName);
		excludeBiomes.add(BiomeGenBase.sky.biomeName);
		onlyIncludeBiomes = null;
		name = n;
	}

	public Schematic(World world, int x1, int y1, int z1, int x2, int y2, int z2) {
		this((short) (Math.abs(x2 - x1) + 1), (short) (Math.abs(y2 - y1) + 1),
				(short) (Math.abs(z2 - z1) + 1), String.format(
						"%s: %d,%d,%d:%d,%d,%d", world.getWorldInfo()
								.getWorldName(), x1, y1, z1, x2, y2, z2));

		if (x1 > x2) {
			int t = x1;
			x1 = x2;
			x2 = t;
		}
		if (y1 > y2) {
			int t = y1;
			y1 = y2;
			y2 = t;
		}
		if (z1 > z2) {
			int t = z1;
			z1 = z2;
			z2 = t;
		}

		for (int x = x1; x <= x2; x++) {
			for (int y = y1; y <= y2; y++) {
				for (int z = z1; z <= z2; z++) {
					Block block = world.getBlock(x, y, z);
					int id = Block.getIdFromBlock(block);
					blocks[x - x1][y - y1][z - z1] = id;
					idMap.put(id, block);
					meta[x - x1][y - y1][z - z1] = world.getBlockMetadata(x, y,
							z);
				}
			}
		}
		if (!world.isRemote) {
			this.entities = WorldUtils.getEntities(world, x1, y1, z1, x2, y2, z2);
			this.tileEntities = WorldUtils.getTileEntities(world, x1, y1, z1, x2, y2, z2);
		}
	}

	public boolean containsIgnoreCase(List<String> list, String thing) {
		for (String other : list) {
			if (thing.equalsIgnoreCase(other)) {
				return true;
			}
		}
		return false;
	}

	public boolean fitsIntoWorldAt(World world, int atX, int atY, int atZ,
			ForgeDirection rotationDirection) {
		// used for world generation to determine if all anchor blocks in the
		// schematic match up with the world
		ForgeDirection rotationAxis = DirectionUtils.axisForDirection(rotationDirection);
		int rotationCount = DirectionUtils.rotationCountForDirection(rotationDirection);
		Vec3 offset = Vec3.createVectorHelper(atX, atY, atZ);
		if (anchorBlockLocations.isEmpty()) {
			Vec3 middle = DirectionUtils.rotateCoords(
					Vec3.createVectorHelper(width / 2, 0, length / 2), offset,
					rotationAxis, rotationCount);
			int midX = (int) middle.xCoord;
			int midY = (int) middle.yCoord;
			int midZ = (int) middle.zCoord;
			Block otherBlockBelow = world.getBlock(midX, midY, midZ);
			int otherMetaBelow = world.getBlockMetadata(midX, midY, midZ);
			Block otherBlockAbove = world.getBlock(midX, midY + 1, midZ);
			int otherMetaAbove = world.getBlockMetadata(midX, midY + 1, midZ);
			BiomeGenBase biome = world.getBiomeGenForCoords(midX, midZ);
			return BlockAnchorMaterialLogic.matchesStatic(
					BlockAnchorMaterial.AnchorType.GROUND.id, otherBlockBelow,
					otherMetaBelow, biome)
					&& BlockAnchorMaterialLogic.matchesStatic(
							BlockAnchorMaterial.AnchorType.AIR.id,
							otherBlockAbove, otherMetaAbove, biome);
		} else {
			for (int i = 0; i < anchorBlockLocations.size(); i++) {
				Integer[] origCoords = anchorBlockLocations.get(i);
				Vec3 rotatedCoords = DirectionUtils.rotateCoords(Vec3.createVectorHelper(
						origCoords[0], origCoords[1], origCoords[2]), offset,
						rotationAxis, rotationCount);
				if (!world.blockExists((int) rotatedCoords.xCoord,
						(int) rotatedCoords.yCoord, (int) rotatedCoords.zCoord)
						|| !(matchingMap
								.get(blocks[origCoords[0]][origCoords[1]][origCoords[2]]))
								.matches(
										meta[origCoords[0]][origCoords[1]][origCoords[2]],
										getTileEntityAt(origCoords[0],
												origCoords[1], origCoords[2]),
										world, (int) rotatedCoords.xCoord,
										(int) rotatedCoords.yCoord,
										(int) rotatedCoords.zCoord)) {
					return false;
				}
			}
			return true;
		}
	}

	public NBTTagCompound getNBT() {
		// http://www.minecraftwiki.net/wiki/Schematic_file_format
		NBTTagCompound base = new NBTTagCompound();

		base.setShort("Width", width);
		base.setShort("Height", height);
		base.setShort("Length", length);
		base.setString("Materials", "Alpha");

		int size = width * height * length;
		byte blockBytes[] = new byte[size];
		byte metaBytes[] = new byte[size];
		if (size % 2 != 0) {
			size++;
		}
		byte blockUpperBits[] = new byte[size / 2];
		// YZX order
		for (int y = 0, blockIdx = 0; y < height; y++) {
			for (int z = 0; z < length; z++) {
				for (int x = 0; x < width; x++, blockIdx++) {
					blockBytes[blockIdx] = (byte) (blocks[x][y][z] & 0xFF);
					metaBytes[blockIdx] = (byte) (meta[x][y][z]);
					blockUpperBits[blockIdx >> 1] |= (byte) ((blocks[x][y][z] & 0xF00) >> ((blockIdx % 2 == 0) ? 4
							: 8));
				}
			}
		}

		base.setByteArray("Blocks", blockBytes);
		base.setByteArray("Data", metaBytes);
		base.setByteArray("AddBlocks", blockUpperBits);

		if (entities != null) {
			base.setTag("Entities", entities);
		}

		if (tileEntities != null) {
			base.setTag("TileEntities", tileEntities);
		}

		NBTTagCompound idMapTag = new NBTTagCompound();
		for (Entry<Integer, Block> entry : idMap.entrySet()) {
			idMapTag.setInteger(Block.blockRegistry.getNameForObject(entry.getValue()), entry.getKey());
		}
		base.setTag("MWGIDMap", idMapTag);

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

		base.setInteger("randomWeight", super.itemWeight);

		return base;
	}

	public TileEntity getTileEntityAt(int x, int y, int z) {
		for (int i = 0; i < tileEntities.tagCount(); i++) {
			NBTTagCompound tileEntityTag = tileEntities.getCompoundTagAt(i);
			if (tileEntityTag.getInteger("x") == x
					&& tileEntityTag.getInteger("y") == y
					&& tileEntityTag.getInteger("z") == z) {
				TileEntity e = TileEntity.createAndLoadEntity(tileEntityTag);
				return e;
			}
		}
		return null;
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

	public void placeInWorld(World world, int atX, int atY, int atZ,
			ForgeDirection rotationDirection, boolean generateChests,
			boolean generateSpawners, Random rand) {
		ForgeDirection rotationAxis = DirectionUtils.axisForDirection(rotationDirection);
		int rotationCount = DirectionUtils.rotationCountForDirection(rotationDirection);
		float pitchOffset = DirectionUtils.pitchOffsetForDirection(rotationDirection);
		float yawOffset = DirectionUtils.yawOffsetForDirection(rotationDirection);
		Vec3 offset = Vec3.createVectorHelper(atX, atY, atZ);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				for (int z = 0; z < length; z++) {
					Vec3 rotatedCoords = DirectionUtils.rotateCoords(
							Vec3.createVectorHelper(x, y, z), offset,
							rotationAxis, rotationCount);
					if (placingMap.containsKey(blocks[x][y][z])) {
						placingMap.get(blocks[x][y][z]).affectWorld(
								meta[x][y][z], getTileEntityAt(x, y, z), world,
								x, y, z);
					} else if (idMap.containsKey(blocks[x][y][z])) {
						Block block = idMap.get(blocks[x][y][z]);
						world.setBlock((int) rotatedCoords.xCoord,
								(int) rotatedCoords.yCoord,
								(int) rotatedCoords.zCoord, block,
								meta[x][y][z], 0x2);
					} else {
						Block block = (Block) Block.getBlockById(blocks[x][y][z]);
						world.setBlock((int) rotatedCoords.xCoord,
								(int) rotatedCoords.yCoord,
								(int) rotatedCoords.zCoord, block,
								meta[x][y][z], 0x2);
					}
				}
			}
		}

		if (entities != null) {
			for (int i = 0; i < entities.tagCount(); i++) {
				NBTTagCompound entityTag = entities.getCompoundTagAt(i);
				Entity e = EntityList.createEntityFromNBT(entityTag, world);
				if (e == null) {
					MyWorldGen.log.log(Level.WARN,
							"Not loading entity ID {}",
							entityTag.getString("id"));
				} else {
					Vec3 newCoords = DirectionUtils.rotateCoords(
							Vec3.createVectorHelper(e.posX, e.posY, e.posZ),
							offset, rotationAxis, rotationCount);
					e.setPositionAndRotation(newCoords.xCoord,
							newCoords.yCoord, newCoords.zCoord, e.rotationPitch
									+ pitchOffset, e.rotationYaw + yawOffset);
					world.spawnEntityInWorld(e);
				}
			}
		}

		if (tileEntities != null) {
			for (int i = 0; i < tileEntities.tagCount(); i++) {
				NBTTagCompound tileEntityTag = tileEntities.getCompoundTagAt(i);
				TileEntity e = TileEntity.createAndLoadEntity(tileEntityTag);
				if (e == null) {
					MyWorldGen.log.log(Level.WARN,
							"Not loading tile entity ID {}",
							tileEntityTag.getString("id"));
				} else {
					Vec3 newCoords = DirectionUtils.rotateCoords(Vec3.createVectorHelper(
							e.xCoord, e.yCoord, e.zCoord), offset,
							rotationAxis, rotationCount);
					e.xCoord = (int) newCoords.xCoord;
					e.yCoord = (int) newCoords.yCoord;
					e.zCoord = (int) newCoords.zCoord;
					world.getChunkFromBlockCoords((int) newCoords.xCoord,
							(int) newCoords.zCoord).addTileEntity(e);
				}
			}
		}

		// Check for chests *after* we place the ones from the schematic,
		// because the schematic may not have defined the tile entities
		// properly.
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				for (int z = 0; z < length; z++) {
					Vec3 rotatedCoords = DirectionUtils.rotateCoords(
							Vec3.createVectorHelper(x, y, z), offset,
							rotationAxis, rotationCount);
					Block block = world.getBlock((int) rotatedCoords.xCoord,
							(int) rotatedCoords.yCoord,
							(int) rotatedCoords.zCoord);
					TileEntity e = world.getTileEntity(
							(int) rotatedCoords.xCoord,
							(int) rotatedCoords.yCoord,
							(int) rotatedCoords.zCoord);
					if (generateChests && !chestType.isEmpty()) {
						if (block == Blocks.chest
								|| block == Blocks.trapped_chest) {
							ChestGenHooks info = ChestGenHooks
									.getInfo(chestType);
							WeightedRandomChestContent.generateChestContents(
									rand, info.getItems(rand),
									(TileEntityChest) e, info.getCount(rand));
						} else if (block == Blocks.dispenser) {
							ChestGenHooks info = ChestGenHooks
									.getInfo(ChestGenHooks.PYRAMID_JUNGLE_DISPENSER);
							WeightedRandomChestContent
									.generateDispenserContents(rand,
											info.getItems(rand),
											(TileEntityDispenser) e,
											info.getCount(rand));
						}
					}
					if (generateSpawners && block == Blocks.mob_spawner) {
						DungeonHooks.getRandomDungeonMob(rand);
					}
				}
			}
		}

		/*
		 * Rotate blocks afterward to try to avoid block updates making invalid
		 * configurations (torches on air). Sometimes that still happens though.
		 * Also, some blocks might have their rotation in tile entity data.
		 * Forge devs, can I turn off block updates somehow please? :(
		 */
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				for (int z = 0; z < length; z++) {
					Block block;
					if (idMap.containsKey(blocks[x][y][z])) {
						block = idMap.get(blocks[x][y][z]);
					}
					else {
						block = Block.getBlockById(blocks[x][y][z]);
					}
					if (block != null) {
						Vec3 rotatedCoords = DirectionUtils.rotateCoords(
								Vec3.createVectorHelper(x, y, z), offset,
								rotationAxis, rotationCount);
						for (int i = 0; i < rotationCount; i++) {
							block.rotateBlock(world,
									(int) rotatedCoords.xCoord,
									(int) rotatedCoords.yCoord,
									(int) rotatedCoords.zCoord, rotationAxis);
						}
					}
				}
			}
		}
	}
}
