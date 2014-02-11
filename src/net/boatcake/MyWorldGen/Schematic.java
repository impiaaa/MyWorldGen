package net.boatcake.MyWorldGen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.logging.Level;

import net.boatcake.MyWorldGen.blocks.BlockAnchorLogic;
import net.boatcake.MyWorldGen.blocks.BlockAnchorMaterial;
import net.boatcake.MyWorldGen.blocks.BlockAnchorMaterialLogic;
import net.boatcake.MyWorldGen.blocks.BlockPlacementLogic;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.util.WeightedRandomItem;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.common.DungeonHooks;
import net.minecraftforge.common.ForgeDirection;

public class Schematic extends WeightedRandomItem {
	public static ForgeDirection axisForDirection(
			ForgeDirection rotationDirection) {
		switch (rotationDirection) {
		case UP:
			return ForgeDirection.EAST;
		case WEST:
			return ForgeDirection.UP;
		case NORTH:
			return ForgeDirection.UP;
		case DOWN:
			return ForgeDirection.WEST;
		case EAST:
			return ForgeDirection.DOWN;
		case SOUTH:
		case UNKNOWN:
		default:
			return ForgeDirection.UNKNOWN;
		}
	}

	public static NBTTagList getEntities(World world, int x1, int y1, int z1,
			int x2, int y2, int z2) {
		assert !world.isRemote;
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
		NBTTagList entities = new NBTTagList();
		for (Object o : world.getEntitiesWithinAABB(Entity.class, AxisAlignedBB
				.getBoundingBox(x1 - 0.5, y1 - 0.5, z1 - 0.5, x2 + 0.5,
						y2 + 0.5, z2 + 0.5))) {
			NBTTagCompound enbt = new NBTTagCompound();
			((Entity) o).writeToNBT(enbt);
			NBTTagList posNBT = enbt.getTagList("Pos");
			NBTTagDouble coordNBT = (NBTTagDouble) posNBT.tagAt(0);
			coordNBT.data -= x1;
			coordNBT = (NBTTagDouble) posNBT.tagAt(1);
			coordNBT.data -= y1;
			coordNBT = (NBTTagDouble) posNBT.tagAt(2);
			coordNBT.data -= z1;
			entities.appendTag(enbt);
		}
		return entities;
	}

	public static NBTTagList getTileEntities(World world, int x1, int y1,
			int z1, int x2, int y2, int z2) {
		assert !world.isRemote;
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
		NBTTagList tileEntities = new NBTTagList();
		for (int x = x1; x <= x2; x++) {
			for (int y = y1; y <= y2; y++) {
				for (int z = z1; z <= z2; z++) {
					if (world.blockHasTileEntity(x, y, z)) {
						TileEntity tileEntity = world.getBlockTileEntity(x, y,
								z);
						NBTTagCompound tenbt = new NBTTagCompound();
						tileEntity.writeToNBT(tenbt);
						tenbt.setInteger("x", tenbt.getInteger("x") - x1);
						tenbt.setInteger("y", tenbt.getInteger("y") - y1);
						tenbt.setInteger("z", tenbt.getInteger("z") - z1);
						tileEntities.appendTag(tenbt);
					}
				}
			}
		}
		return tileEntities;
	}

	public static float pitchOffsetForDirection(ForgeDirection rotationDirection) {
		switch (rotationDirection) {
		case UP:
			return 90;
		case WEST:
			return 0;
		case NORTH:
			return 0;
		case DOWN:
			return -90;
		case EAST:
			return 0;
		case SOUTH:
		case UNKNOWN:
		default:
			return 0;
		}
	}

	public static int rotationCountForDirection(ForgeDirection rotationDirection) {
		switch (rotationDirection) {
		case UP:
			return 1;
		case WEST:
			return 1;
		case NORTH:
			return 2;
		case DOWN:
			return 1;
		case EAST:
			return 1;
		case SOUTH:
		case UNKNOWN:
		default:
			return 0;
		}
	}

	public static float yawOffsetForDirection(ForgeDirection rotationDirection) {
		switch (rotationDirection) {
		case UP:
			return 0;
		case WEST:
			return 90;
		case NORTH:
			return 180;
		case DOWN:
			return 0;
		case EAST:
			return -90;
		case SOUTH:
		case UNKNOWN:
		default:
			return 0;
		}
	}

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
			for (Object o : mapTag.getTags()) {
				NBTTagInt idTag = (NBTTagInt) o;
				String blockName = idTag.getName();
				String unlocalizedName = "tile." + blockName;
				int id = idTag.data;
				for (Block block : Block.blocksList) {
					if (block != null
							&& unlocalizedName.equals(block
									.getUnlocalizedName())) {
						idMap.put(id, block);
					}
				}
				if (!idMap.containsKey(id)
						&& !BlockAnchorLogic.isAnchorBlock(blockName)
						&& !BlockPlacementLogic.placementLogicExists(blockName)) {
					MyWorldGen.log.log(Level.WARNING,
							"Can't find a block named {0}", unlocalizedName);
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
						.log(Level.WARNING,
								"Schematic file {0} has no ignoreBlockId tag, defaulting to ID from config",
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
						.log(Level.WARNING,
								"Schematic file {0} has no anchorBlockId tag, defaulting to ID from config",
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

		entities = tag.getTagList("Entities");
		tileEntities = tag.getTagList("TileEntities");

		if (anchorBlockLocations.isEmpty()) {
			MyWorldGen.log.log(Level.WARNING,
					"No anchors found in schematic {0}", name);
		}

		if (tag.hasKey("chestType")) {
			chestType = tag.getString("chestType");
		} else {
			chestType = ChestGenHooks.DUNGEON_CHEST;
		}

		if (tag.hasKey("excludeBiomes")) {
			NBTTagList l = tag.getTagList("excludeBiomes");
			excludeBiomes = new ArrayList<String>(l.tagCount());
			for (int i = 0; i < l.tagCount(); i++) {
				excludeBiomes.add(((NBTTagString) l.tagAt(i)).data);
			}
		} else {
			excludeBiomes = null;
		}

		if (tag.hasKey("onlyIncludeBiomes")) {
			NBTTagList l = tag.getTagList("onlyIncludeBiomes");
			onlyIncludeBiomes = new ArrayList<String>(l.tagCount());
			for (int i = 0; i < l.tagCount(); i++) {
				onlyIncludeBiomes.add(((NBTTagString) l.tagAt(i)).data);
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
					int blockId = world.getBlockId(x, y, z);
					blocks[x - x1][y - y1][z - z1] = blockId;
					if (Block.blocksList[blockId] != null) {
						idMap.put(blockId, Block.blocksList[blockId]);
					}
					meta[x - x1][y - y1][z - z1] = world.getBlockMetadata(x, y,
							z);
				}
			}
		}
		if (!world.isRemote) {
			this.entities = getEntities(world, x1, y1, z1, x2, y2, z2);
			this.tileEntities = getTileEntities(world, x1, y1, z1, x2, y2, z2);
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
		ForgeDirection rotationAxis = axisForDirection(rotationDirection);
		int rotationCount = rotationCountForDirection(rotationDirection);
		Vec3 offset = Vec3.createVectorHelper(atX, atY, atZ);
		if (anchorBlockLocations.isEmpty()) {
			Vec3 middle = rotateCoords(
					Vec3.createVectorHelper(width / 2, 0, length / 2), offset,
					rotationAxis, rotationCount);
			int midX = (int) middle.xCoord;
			int midY = (int) middle.yCoord;
			int midZ = (int) middle.zCoord;
			int otherBlockBelow = world.getBlockId(midX, midY, midZ);
			int otherMetaBelow = world.getBlockMetadata(midX, midY, midZ);
			int otherBlockAbove = world.getBlockId(midX, midY + 1, midZ);
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
				Vec3 rotatedCoords = rotateCoords(Vec3.createVectorHelper(
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
			String unlocalizedName = entry.getValue().getUnlocalizedName();
			// getUnlocalizedName always adds a tile. to the beginning
			idMapTag.setInteger(unlocalizedName.substring(5), entry.getKey());
		}
		base.setTag("MWGIDMap", idMapTag);

		base.setString("chestType", chestType);

		if (excludeBiomes != null) {
			NBTTagList t = new NBTTagList();
			for (String biome : excludeBiomes) {
				t.appendTag(new NBTTagString(null, biome));
			}
			base.setTag("excludeBiomes", t);
		}

		if (onlyIncludeBiomes != null) {
			NBTTagList t = new NBTTagList();
			for (String biome : onlyIncludeBiomes) {
				t.appendTag(new NBTTagString(null, biome));
			}
			base.setTag("onlyIncludeBiomes", t);
		}

		base.setInteger("randomWeight", super.itemWeight);

		return base;
	}

	public TileEntity getTileEntityAt(int x, int y, int z) {
		for (int i = 0; i < tileEntities.tagCount(); i++) {
			NBTTagCompound tileEntityTag = (NBTTagCompound) tileEntities
					.tagAt(i);
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
		ForgeDirection rotationAxis = axisForDirection(rotationDirection);
		int rotationCount = rotationCountForDirection(rotationDirection);
		float pitchOffset = pitchOffsetForDirection(rotationDirection);
		float yawOffset = yawOffsetForDirection(rotationDirection);
		Vec3 offset = Vec3.createVectorHelper(atX, atY, atZ);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				for (int z = 0; z < length; z++) {
					Vec3 rotatedCoords = rotateCoords(
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
								(int) rotatedCoords.zCoord, block.blockID,
								meta[x][y][z], 0x2);
					} else {
						world.setBlock((int) rotatedCoords.xCoord,
								(int) rotatedCoords.yCoord,
								(int) rotatedCoords.zCoord, blocks[x][y][z],
								meta[x][y][z], 0x2);
					}
				}
			}
		}

		if (entities != null) {
			for (int i = 0; i < entities.tagCount(); i++) {
				NBTTagCompound entityTag = (NBTTagCompound) entities.tagAt(i);
				Entity e = EntityList.createEntityFromNBT(entityTag, world);
				if (e == null) {
					MyWorldGen.log.log(Level.WARNING,
							"Not loading entity ID {0}",
							entityTag.getString("id"));
				} else {
					Vec3 newCoords = rotateCoords(
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
				NBTTagCompound tileEntityTag = (NBTTagCompound) tileEntities
						.tagAt(i);
				TileEntity e = TileEntity.createAndLoadEntity(tileEntityTag);
				if (e == null) {
					MyWorldGen.log.log(Level.WARNING,
							"Not loading tile entity ID {0}",
							tileEntityTag.getString("id"));
				} else {
					Vec3 newCoords = rotateCoords(Vec3.createVectorHelper(
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
					Vec3 rotatedCoords = rotateCoords(
							Vec3.createVectorHelper(x, y, z), offset,
							rotationAxis, rotationCount);
					int blockId = world.getBlockId((int) rotatedCoords.xCoord,
							(int) rotatedCoords.yCoord,
							(int) rotatedCoords.zCoord);
					TileEntity e = world.getBlockTileEntity(
							(int) rotatedCoords.xCoord,
							(int) rotatedCoords.yCoord,
							(int) rotatedCoords.zCoord);
					if (generateChests && !chestType.isEmpty()) {
						if (blockId == Block.chest.blockID
								|| blockId == Block.chestTrapped.blockID) {
							ChestGenHooks info = ChestGenHooks
									.getInfo(chestType);
							WeightedRandomChestContent.generateChestContents(
									rand, info.getItems(rand),
									(TileEntityChest) e, info.getCount(rand));
						} else if (blockId == Block.dispenser.blockID) {
							ChestGenHooks info = ChestGenHooks
									.getInfo(ChestGenHooks.PYRAMID_JUNGLE_DISPENSER);
							WeightedRandomChestContent
									.generateDispenserContents(rand,
											info.getItems(rand),
											(TileEntityDispenser) e,
											info.getCount(rand));
						}
					}
					if (generateSpawners && blockId == Block.mobSpawner.blockID) {
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
					if (Block.blocksList[blocks[x][y][z]] != null) {
						Vec3 rotatedCoords = rotateCoords(
								Vec3.createVectorHelper(x, y, z), offset,
								rotationAxis, rotationCount);
						for (int i = 0; i < rotationCount; i++) {
							Block.blocksList[blocks[x][y][z]].rotateBlock(
									world, (int) rotatedCoords.xCoord,
									(int) rotatedCoords.yCoord,
									(int) rotatedCoords.zCoord, rotationAxis);
						}
					}
				}
			}
		}
	}

	private Vec3 rotateCoords(Vec3 coords, Vec3 at,
			ForgeDirection rotationAxis, int rotationCount) {
		double worldX = coords.xCoord;
		double worldY = coords.yCoord;
		double worldZ = coords.zCoord;
		for (int i = 0; i < rotationCount; i++) {
			if (rotationAxis.offsetX == 1) {
				double temp = worldY;
				worldY = -worldZ;
				worldZ = temp;
			} else if (rotationAxis.offsetX == -1) {
				double temp = worldY;
				worldY = worldZ;
				worldZ = -temp;
			}
			if (rotationAxis.offsetY == 1) {
				double temp = worldX;
				worldX = -worldZ;
				worldZ = temp;
			} else if (rotationAxis.offsetY == -1) {
				double temp = worldX;
				worldX = worldZ;
				worldZ = -temp;
			}
			if (rotationAxis.offsetZ == 1) {
				double temp = worldX;
				worldX = -worldY;
				worldY = temp;
			} else if (rotationAxis.offsetZ == -1) {
				double temp = worldX;
				worldX = worldY;
				worldY = -temp;
			}
		}

		worldX += at.xCoord;
		worldY += at.yCoord;
		worldZ += at.zCoord;

		return Vec3.createVectorHelper(worldX, worldY, worldZ);
	}
}
