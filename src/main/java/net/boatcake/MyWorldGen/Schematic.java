package net.boatcake.MyWorldGen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.logging.log4j.Level;

import cpw.mods.fml.common.registry.GameData;
import net.boatcake.MyWorldGen.blocks.BlockAnchorLogic;
import net.boatcake.MyWorldGen.blocks.BlockAnchorMaterial;
import net.boatcake.MyWorldGen.blocks.BlockAnchorMaterial.AnchorType;
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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.Vec3;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.common.DungeonHooks;
import net.minecraftforge.common.util.ForgeDirection;

public class Schematic {
	public short width;
	public short height;
	public short length;

	private int blocks[][][];
	private int meta[][][];

	public NBTTagList entities;
	public NBTTagList tileEntities;

	public Map<Integer, Block> idMap;
	public Map<Integer, BlockAnchorLogic> matchingMap;
	public Map<Integer, BlockPlacementLogic> placingMap;

	// cache of the x,y,z locations of all anchor blocks
	private ArrayList<Integer[]> anchorBlockLocations;

	public SchematicInfo info;

	// Designated constructor
	public Schematic(short w, short h, short d, String n) {
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
		info = new SchematicInfo();
		info.name = n;
	}

	public Schematic() {
		this((short) 0, (short) 0, (short) 0, null);
	}

	public Schematic(NBTTagCompound tag, String n) {
		info = new SchematicInfo();
		info.name = n;
		if (!tag.getString("Materials").equals("Alpha")) {
			throw new RuntimeException("Non-Alpha schematics are not supported!");
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
				} else if (!BlockAnchorLogic.isAnchorBlock(blockName)
						&& !BlockPlacementLogic.placementLogicExists(blockName)) {
					MyWorldGen.log.log(Level.WARN, "Can't find a block named {}", blockName);
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
				int id = tag.getInteger("ignoreBlockId");
				if (MyWorldGen.ignoreBlock != null) {
					idMap.put(id, MyWorldGen.ignoreBlock);
				}
				placingMap.put(id, BlockPlacementLogic.get(MyWorldGen.MODID + ":ignore"));
			} else {
				MyWorldGen.log.log(Level.WARN,
						"Schematic file {} has no ignoreBlockId tag, defaulting to ID from config", info.name);
				if (MyWorldGen.ignoreBlock != null) {
					placingMap.put(GameData.getBlockRegistry().getId(MyWorldGen.ignoreBlock),
							BlockPlacementLogic.get(MyWorldGen.MODID + ":ignore"));
				}
				placingMap.put(MyWorldGen.ignoreBlockId, BlockPlacementLogic.get(MyWorldGen.MODID + ":ignore"));
			}

			if (tag.hasKey("anchorBlockId")) {
				int id = tag.getInteger("anchorBlockId");
				if (MyWorldGen.materialAnchorBlock != null) {
					idMap.put(id, MyWorldGen.materialAnchorBlock);
				}
				placingMap.put(id, BlockPlacementLogic.get(MyWorldGen.MODID + ":anchor"));
				matchingMap.put(id, BlockAnchorLogic.get(MyWorldGen.MODID + ":anchor"));
			} else {
				MyWorldGen.log.log(Level.WARN,
						"Schematic file {} has no anchorBlockId tag, defaulting to ID from config", info.name);
				if (MyWorldGen.materialAnchorBlock != null) {
					placingMap.put(GameData.getBlockRegistry().getId(MyWorldGen.materialAnchorBlock),
							BlockPlacementLogic.get(MyWorldGen.MODID + ":anchor"));
					matchingMap.put(GameData.getBlockRegistry().getId(MyWorldGen.materialAnchorBlock),
							BlockAnchorLogic.get(MyWorldGen.MODID + ":anchor"));
				}
				placingMap.put(MyWorldGen.materialAnchorBlockId, BlockPlacementLogic.get(MyWorldGen.MODID + ":anchor"));
				matchingMap.put(MyWorldGen.materialAnchorBlockId, BlockAnchorLogic.get(MyWorldGen.MODID + ":anchor"));
			}

			if (MyWorldGen.inventoryAnchorBlock != null) {
				placingMap.put(GameData.getBlockRegistry().getId(MyWorldGen.inventoryAnchorBlock),
						BlockPlacementLogic.get(MyWorldGen.MODID + ":anchorInventory"));
				matchingMap.put(GameData.getBlockRegistry().getId(MyWorldGen.inventoryAnchorBlock),
						BlockAnchorLogic.get(MyWorldGen.MODID + ":anchorInventory"));
			}
			placingMap.put(MyWorldGen.inventoryAnchorBlockId,
					BlockPlacementLogic.get(MyWorldGen.MODID + ":anchorInventory"));
			matchingMap.put(MyWorldGen.inventoryAnchorBlockId,
					BlockAnchorLogic.get(MyWorldGen.MODID + ":anchorInventory"));
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
					meta[x][y][z] = (metaBytes[blockIdx]) & 0x0F;
					if (blockUpperBits != null) {
						blocks[x][y][z] |= (blockUpperBits[blockIdx >> 1] << ((blockIdx % 2 == 0) ? 4 : 8)) & 0xF00;
					}
					if (matchingMap.containsKey(blocks[x][y][z])) {
						anchorBlockLocations.add(new Integer[] { x, y, z });
					}
				}
			}
		}

		entities = (NBTTagList) tag.getTag("Entities");
		tileEntities = (NBTTagList) tag.getTag("TileEntities");

		if (anchorBlockLocations.isEmpty() && info.name != null) {
			MyWorldGen.log.log(Level.WARN, "No anchors found in schematic {}", info.name);
			info.fuzzyMatching = true;
		}

		info.readFromNBT(tag);
	}

	public Schematic(World world, int x1, int y1, int z1, int x2, int y2, int z2) {
		this((short) (Math.abs(x2 - x1) + 1), (short) (Math.abs(y2 - y1) + 1), (short) (Math.abs(z2 - z1) + 1), null);

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
					meta[x - x1][y - y1][z - z1] = world.getBlockMetadata(x, y, z);
				}
			}
		}
		if (!world.isRemote) {
			this.entities = WorldUtils.getEntities(world, x1, y1, z1, x2, y2, z2);
			this.tileEntities = WorldUtils.getTileEntities(world, x1, y1, z1, x2, y2, z2);
		}
	}

	public boolean fitsIntoWorldAt(World world, int atX, int atY, int atZ, ForgeDirection rotationDirection) {
		// used for world generation to determine if all anchor blocks in the
		// schematic match up with the world
		ForgeDirection rotationAxis = DirectionUtils.axisForDirection(rotationDirection);
		int rotationCount = DirectionUtils.rotationCountForDirection(rotationDirection);
		Vec3 offset = Vec3.createVectorHelper(atX, atY, atZ);
		if (anchorBlockLocations.isEmpty()) {
			Vec3 middle = DirectionUtils.rotateCoords(Vec3.createVectorHelper(width / 2, 0, length / 2), offset,
					rotationAxis, rotationCount);
			int midX = (int) middle.xCoord;
			int midY = (int) middle.yCoord;
			int midZ = (int) middle.zCoord;
			Block otherBlockBelow = world.getBlock(midX, midY, midZ);
			int otherMetaBelow = world.getBlockMetadata(midX, midY, midZ);
			Block otherBlockAbove = world.getBlock(midX, midY + 1, midZ);
			int otherMetaAbove = world.getBlockMetadata(midX, midY + 1, midZ);
			BiomeGenBase biome = world.getBiomeGenForCoords(midX, midZ);
			return BlockAnchorMaterialLogic.matchesStatic(BlockAnchorMaterial.AnchorType.GROUND, otherBlockBelow,
					otherMetaBelow, biome)
					&& BlockAnchorMaterialLogic.matchesStatic(BlockAnchorMaterial.AnchorType.AIR, otherBlockAbove,
							otherMetaAbove, biome);
		} else {
			for (int i = 0; i < anchorBlockLocations.size(); i++) {
				Integer[] origCoords = anchorBlockLocations.get(i);
				Vec3 rotatedCoords = DirectionUtils.rotateCoords(
						Vec3.createVectorHelper(origCoords[0], origCoords[1], origCoords[2]), offset, rotationAxis,
						rotationCount);
				if (!(matchingMap.get(blocks[origCoords[0]][origCoords[1]][origCoords[2]])).matches(
						meta[origCoords[0]][origCoords[1]][origCoords[2]],
						getTileEntityAt(origCoords[0], origCoords[1], origCoords[2]), world, (int) rotatedCoords.xCoord,
						(int) rotatedCoords.yCoord, (int) rotatedCoords.zCoord)) {
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
					metaBytes[blockIdx] = (byte) (meta[x][y][z] & 0x0F);
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

		info.writeToNBT(base);

		return base;
	}

	public TileEntity getTileEntityAt(int x, int y, int z) {
		for (int i = 0; i < tileEntities.tagCount(); i++) {
			NBTTagCompound tileEntityTag = tileEntities.getCompoundTagAt(i);
			if (tileEntityTag.getInteger("x") == x && tileEntityTag.getInteger("y") == y
					&& tileEntityTag.getInteger("z") == z) {
				TileEntity e = TileEntity.createAndLoadEntity(tileEntityTag);
				return e;
			}
		}
		return null;
	}

	public void placeInWorld(World world, int atX, int atY, int atZ, ForgeDirection rotationDirection,
			boolean generateChests, boolean generateSpawners, boolean followPlacementRules, Random rand) {
		ForgeDirection rotationAxis = DirectionUtils.axisForDirection(rotationDirection);
		int rotationCount = DirectionUtils.rotationCountForDirection(rotationDirection);
		float pitchOffset = DirectionUtils.pitchOffsetForDirection(rotationDirection);
		float yawOffset = DirectionUtils.yawOffsetForDirection(rotationDirection);
		Vec3 offset = Vec3.createVectorHelper(atX, atY, atZ);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				for (int z = 0; z < length; z++) {
					Vec3 rotatedCoords = DirectionUtils.rotateCoords(Vec3.createVectorHelper(x, y, z), offset,
							rotationAxis, rotationCount);
					if (placingMap.containsKey(blocks[x][y][z]) && followPlacementRules) {
						placingMap.get(blocks[x][y][z]).affectWorld(meta[x][y][z], getTileEntityAt(x, y, z), world,
								(int) rotatedCoords.xCoord, (int) rotatedCoords.yCoord, (int) rotatedCoords.zCoord,
								info.terrainSmoothing);
					} else if (idMap.containsKey(blocks[x][y][z])) {
						Block block = idMap.get(blocks[x][y][z]);
						world.setBlock((int) rotatedCoords.xCoord, (int) rotatedCoords.yCoord,
								(int) rotatedCoords.zCoord, block, meta[x][y][z], 0x2);
					} else {
						Block block = Block.getBlockById(blocks[x][y][z]);
						world.setBlock((int) rotatedCoords.xCoord, (int) rotatedCoords.yCoord,
								(int) rotatedCoords.zCoord, block, meta[x][y][z], 0x2);
					}
				}
			}
		}

		// late update
		for (int x = -1; x < width + 1; x++) {
			for (int y = -1; y < height + 1; y++) {
				for (int z = -1; z < length + 1; z++) {
					Vec3 rotatedCoords = DirectionUtils.rotateCoords(Vec3.createVectorHelper(x, y, z), offset,
							rotationAxis, rotationCount);
					world.notifyBlockChange((int) rotatedCoords.xCoord, (int) rotatedCoords.yCoord,
							(int) rotatedCoords.zCoord, world.getBlock((int) rotatedCoords.xCoord,
									(int) rotatedCoords.yCoord, (int) rotatedCoords.zCoord));
				}
			}
		}

		if (entities != null) {
			for (int i = 0; i < entities.tagCount(); i++) {
				NBTTagCompound entityTag = entities.getCompoundTagAt(i);
				Entity e = EntityList.createEntityFromNBT(entityTag, world);
				if (e == null) {
					MyWorldGen.log.log(Level.WARN, "Not loading entity ID {}", entityTag.getString("id"));
				} else {
					Vec3 newCoords = DirectionUtils.rotateCoords(Vec3.createVectorHelper(e.posX, e.posY, e.posZ),
							offset, rotationAxis, rotationCount);
					e.setPositionAndRotation(newCoords.xCoord, newCoords.yCoord, newCoords.zCoord,
							e.rotationPitch + pitchOffset, e.rotationYaw + yawOffset);
					world.spawnEntityInWorld(e);
				}
			}
		}

		if (tileEntities != null) {
			for (int i = 0; i < tileEntities.tagCount(); i++) {
				NBTTagCompound tileEntityTag = tileEntities.getCompoundTagAt(i);
				TileEntity e = TileEntity.createAndLoadEntity(tileEntityTag);
				if (e == null) {
					MyWorldGen.log.log(Level.WARN, "Not loading tile entity ID {}", tileEntityTag.getString("id"));
				} else {
					Vec3 newCoords = DirectionUtils.rotateCoords(Vec3.createVectorHelper(e.xCoord, e.yCoord, e.zCoord),
							offset, rotationAxis, rotationCount);
					e.xCoord = (int) newCoords.xCoord;
					e.yCoord = (int) newCoords.yCoord;
					e.zCoord = (int) newCoords.zCoord;
					world.getChunkFromBlockCoords((int) newCoords.xCoord, (int) newCoords.zCoord).addTileEntity(e);
				}
			}
		}

		// Check for chests *after* we place the ones from the schematic,
		// because the schematic may not have defined the tile entities
		// properly.
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				for (int z = 0; z < length; z++) {
					Vec3 rotatedCoords = DirectionUtils.rotateCoords(Vec3.createVectorHelper(x, y, z), offset,
							rotationAxis, rotationCount);
					Block block = world.getBlock((int) rotatedCoords.xCoord, (int) rotatedCoords.yCoord,
							(int) rotatedCoords.zCoord);
					TileEntity e = world.getTileEntity((int) rotatedCoords.xCoord, (int) rotatedCoords.yCoord,
							(int) rotatedCoords.zCoord);
					if (generateChests && !info.chestType.isEmpty()) {
						if (e instanceof TileEntityChest) {
							ChestGenHooks hook = ChestGenHooks.getInfo(info.chestType);
							WeightedRandomChestContent.generateChestContents(rand, hook.getItems(rand),
									(TileEntityChest) e, hook.getCount(rand));
						} else if (e instanceof TileEntityDispenser) {
							ChestGenHooks hook = ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_JUNGLE_DISPENSER);
							WeightedRandomChestContent.generateDispenserContents(rand, hook.getItems(rand),
									(TileEntityDispenser) e, hook.getCount(rand));
						}
					}
					if (info.generateSpawners && generateSpawners && block == Blocks.mob_spawner
							&& e instanceof TileEntityMobSpawner) {
						((TileEntityMobSpawner) e).func_145881_a()
								.setEntityName(DungeonHooks.getRandomDungeonMob(rand));
					}
				}
			}
		}

		/*
		 * Rotate blocks afterward to try to avoid block updates making invalid
		 * configurations (torches on air). Sometimes that still happens though.
		 * Also, some blocks might have their rotation in tile entity data.
		 * TODO: Look into some common rotation API. Block.rotateBlock is
		 * supposed to be used for wrenches etc. in-game.
		 */
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				for (int z = 0; z < length; z++) {
					Block block;
					if (idMap.containsKey(blocks[x][y][z])) {
						block = idMap.get(blocks[x][y][z]);
					} else {
						block = Block.getBlockById(blocks[x][y][z]);
					}
					if (block != null) {
						Vec3 rotatedCoords = DirectionUtils.rotateCoords(Vec3.createVectorHelper(x, y, z), offset,
								rotationAxis, rotationCount);
						for (int i = 0; i < rotationCount; i++) {
							block.rotateBlock(world, (int) rotatedCoords.xCoord, (int) rotatedCoords.yCoord,
									(int) rotatedCoords.zCoord, rotationAxis);
						}
					}
				}
			}
		}
	}

	public Integer[] getFuzzyMatchingLocation(Chunk chunk, ForgeDirection rotationDirection, Random rand) {
		ForgeDirection rotationAxis = DirectionUtils.axisForDirection(rotationDirection);
		int rotationCount = DirectionUtils.rotationCountForDirection(rotationDirection);
		if (anchorBlockLocations.isEmpty()) {
			// Now, this is a tough situation. Structures without anchors
			// should probably be able to fit anywhere on the ground, and
			// fuzzy/quick matching is a great way to make that happen.
			// Unfortunately, that means the generation rate is way too high,
			// and we can't just modify the base generation rate. For now, just
			// add in an additional chance to fail.
			if (rand.nextInt(64) <= 62) { // 1/64 chance to place
				return null;
			}
			Integer[] middleBottomOfSchematic = { width / 2, 0, height / 2 };
			Integer[] worldAnchorPos = BlockAnchorMaterialLogic.getQuickMatchingBlockInChunkStatic(AnchorType.GROUND,
					chunk, rand);
			if (worldAnchorPos != null) {
				Vec3 worldSchematicPos = DirectionUtils.rotateCoords(
						Vec3.createVectorHelper(-middleBottomOfSchematic[0], -middleBottomOfSchematic[1],
								-middleBottomOfSchematic[2]),
						Vec3.createVectorHelper(worldAnchorPos[0], worldAnchorPos[1], worldAnchorPos[2]), rotationAxis,
						rotationCount);
				return new Integer[] { (int) worldSchematicPos.xCoord, (int) worldSchematicPos.yCoord,
						(int) worldSchematicPos.zCoord };
			} else {
				return null;
			}
		} else {
			ArrayList<Integer[]> shuffledAnchors = (ArrayList<Integer[]>) anchorBlockLocations.clone();
			Collections.shuffle(shuffledAnchors, rand);
			for (Integer[] anchorPos : shuffledAnchors) {
				BlockAnchorLogic matching = matchingMap.get(blocks[anchorPos[0]][anchorPos[1]][anchorPos[2]]);
				int anchorMeta = meta[anchorPos[0]][anchorPos[1]][anchorPos[2]];
				TileEntity anchorEntity = getTileEntityAt(anchorPos[0], anchorPos[1], anchorPos[2]);
				Integer[] worldAnchorPos = matching.getQuickMatchingBlockInChunk(anchorMeta, anchorEntity, chunk, rand);
				if (worldAnchorPos != null) {
					Vec3 worldSchematicPos = DirectionUtils.rotateCoords(
							Vec3.createVectorHelper(-anchorPos[0], -anchorPos[1], -anchorPos[2]),
							Vec3.createVectorHelper(worldAnchorPos[0], worldAnchorPos[1], worldAnchorPos[2]),
							rotationAxis, rotationCount);
					return new Integer[] { (int) worldSchematicPos.xCoord, (int) worldSchematicPos.yCoord,
							(int) worldSchematicPos.zCoord };
				}
			}
			return null;
		}
	}
}
