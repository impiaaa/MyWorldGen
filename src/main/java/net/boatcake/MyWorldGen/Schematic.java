package net.boatcake.MyWorldGen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.logging.log4j.Level;

import net.boatcake.MyWorldGen.blocks.BlockAnchorLogic;
import net.boatcake.MyWorldGen.blocks.BlockAnchorMaterial;
import net.boatcake.MyWorldGen.blocks.BlockAnchorMaterial.AnchorType;
import net.boatcake.MyWorldGen.blocks.BlockAnchorMaterialLogic;
import net.boatcake.MyWorldGen.blocks.BlockPlacementLogic;
import net.boatcake.MyWorldGen.utils.DirectionUtils;
import net.boatcake.MyWorldGen.utils.WorldUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec3i;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.common.DungeonHooks;
import net.minecraftforge.fml.common.registry.GameData;

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
	private ArrayList<BlockPos> anchorBlockLocations;

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
		anchorBlockLocations = new ArrayList<BlockPos>();
		info = new SchematicInfo();
		info.name = n;
	}

	public Schematic() {
		this((short) 0, (short) 0, (short) 0, null);
	}

	public Schematic(NBTTagCompound tag, String n) {
		this();
		readFromNBT(tag, n);
	}

	public void readFromNBT(NBTTagCompound tag, String n) {
		info.name = n;
		if (!tag.getString("Materials").equals("Alpha")) {
			throw new RuntimeException("Non-Alpha schematics are not supported!");
		}
		width = tag.getShort("Width");
		height = tag.getShort("Height");
		length = tag.getShort("Length");

		if (tag.hasKey("MWGIDMap")) {
			NBTTagCompound mapTag = tag.getCompoundTag("MWGIDMap");
			for (Object o : mapTag.getKeySet()) {
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

		anchorBlockLocations.clear();
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
						anchorBlockLocations.add(new BlockPos(x, y, z));
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

	public Schematic(World world, BlockPos pos1, BlockPos pos2) {
		this((short) (Math.abs(pos2.getX() - pos1.getX()) + 1), (short) (Math.abs(pos2.getY() - pos1.getY()) + 1),
				(short) (Math.abs(pos2.getZ() - pos1.getZ()) + 1), null);

		BlockPos min = new BlockPos(Math.min(pos1.getX(), pos2.getX()), Math.min(pos1.getY(), pos2.getY()),
				Math.min(pos1.getZ(), pos2.getZ()));
		Vec3i minVec = new Vec3i(min.getX(), min.getY(), min.getZ());
		BlockPos max = new BlockPos(Math.max(pos1.getX(), pos2.getX()), Math.max(pos1.getY(), pos2.getY()),
				Math.max(pos1.getZ(), pos2.getZ()));

		for (Object o : BlockPos.getAllInBox(min, max)) {
			BlockPos pos = (BlockPos) o;
			IBlockState blockState = world.getBlockState(pos);
			Block block = blockState.getBlock();
			int id = Block.getIdFromBlock(block);
			BlockPos offset = pos.subtract(minVec);
			blocks[offset.getX()][offset.getY()][offset.getZ()] = id;
			idMap.put(id, block);
			meta[offset.getX()][offset.getY()][offset.getZ()] = block.getMetaFromState(blockState);
		}
		if (!world.isRemote) {
			this.entities = WorldUtils.getEntities(world, min, max);
			this.tileEntities = WorldUtils.getTileEntities(world, min, max);
		}
	}

	public boolean fitsIntoWorldAt(World world, BlockPos at, EnumFacing rotationDirection) {
		// used for world generation to determine if all anchor blocks in the
		// schematic match up with the world
		Axis rotationAxis = DirectionUtils.axisForDirection(rotationDirection);
		int rotationCount = DirectionUtils.rotationCountForDirection(rotationDirection);
		Vec3 offset = new Vec3(at.getX(), at.getY(), at.getZ());
		if (anchorBlockLocations.isEmpty()) {
			Vec3 middle = DirectionUtils.rotateCoords(new Vec3(width / 2, 0, length / 2), offset, rotationAxis,
					rotationCount);
			BlockPos mid = new BlockPos(middle);
			BlockPos midDown = mid.offsetDown();
			IBlockState otherBlockBelow = world.getBlockState(mid);
			IBlockState otherBlockAbove = world.getBlockState(midDown);
			BiomeGenBase biome = world.getBiomeGenForCoords(mid);
			return BlockAnchorMaterialLogic.matchesStatic(BlockAnchorMaterial.AnchorType.GROUND, otherBlockBelow, biome)
					&& BlockAnchorMaterialLogic.matchesStatic(BlockAnchorMaterial.AnchorType.AIR, otherBlockAbove,
							biome);
		} else {
			for (int i = 0; i < anchorBlockLocations.size(); i++) {
				BlockPos origCoords = anchorBlockLocations.get(i);
				Vec3 rotatedCoords = DirectionUtils.rotateCoords(origCoords, offset, rotationAxis, rotationCount);
				BlockPos rotatedPos = new BlockPos(rotatedCoords);
				int blockId = blocks[origCoords.getX()][origCoords.getY()][origCoords.getZ()];
				if (!(matchingMap.get(blockId)).matches(meta[origCoords.getX()][origCoords.getY()][origCoords.getZ()],
						getTileEntityAt(origCoords), world, rotatedPos)) {
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
			idMapTag.setInteger(Block.blockRegistry.getNameForObject(entry.getValue()).toString(), entry.getKey());
		}
		base.setTag("MWGIDMap", idMapTag);

		info.writeToNBT(base);

		return base;
	}

	public TileEntity getTileEntityAt(BlockPos pos) {
		for (int i = 0; i < tileEntities.tagCount(); i++) {
			NBTTagCompound tileEntityTag = tileEntities.getCompoundTagAt(i);
			if (tileEntityTag.getInteger("x") == pos.getX() && tileEntityTag.getInteger("y") == pos.getY()
					&& tileEntityTag.getInteger("z") == pos.getZ()) {
				TileEntity e = TileEntity.createAndLoadEntity(tileEntityTag);
				return e;
			}
		}
		return null;
	}

	public void placeInWorld(World world, BlockPos at, EnumFacing rotationDirection, boolean generateChests,
			boolean generateSpawners, boolean followPlacementRules, Random rand) {
		Axis rotationAxis = DirectionUtils.axisForDirection(rotationDirection);
		int rotationCount = DirectionUtils.rotationCountForDirection(rotationDirection);
		float pitchOffset = DirectionUtils.pitchOffsetForDirection(rotationDirection);
		float yawOffset = DirectionUtils.yawOffsetForDirection(rotationDirection);
		Vec3 offset = new Vec3(at.getX(), at.getY(), at.getZ());
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				for (int z = 0; z < length; z++) {
					BlockPos pos = new BlockPos(x, y, z);
					Vec3 rotatedCoords = DirectionUtils.rotateCoords(pos, offset, rotationAxis, rotationCount);
					BlockPos rotatedPos = new BlockPos(rotatedCoords);
					if (placingMap.containsKey(blocks[x][y][z]) && followPlacementRules) {
						placingMap.get(blocks[x][y][z]).affectWorld(meta[x][y][z], getTileEntityAt(pos), world,
								rotatedPos, info.terrainSmoothing);
					} else if (idMap.containsKey(blocks[x][y][z])) {
						IBlockState blockState = idMap.get(blocks[x][y][z]).getStateFromMeta(meta[x][y][z]);
						world.setBlockState(rotatedPos, blockState, 0x2);
					} else {
						IBlockState blockState = Block.getBlockById(blocks[x][y][z]).getStateFromMeta(meta[x][y][z]);
						world.setBlockState(rotatedPos, blockState, 0x2);
					}
				}
			}
		}

		// late update
		for (int x = -1; x < width + 1; x++) {
			for (int y = -1; y < height + 1; y++) {
				for (int z = -1; z < length + 1; z++) {
					BlockPos pos = new BlockPos(x, y, z);
					Vec3 rotatedCoords = DirectionUtils.rotateCoords(pos, offset, rotationAxis, rotationCount);
					BlockPos rotatedPos = new BlockPos(rotatedCoords);

					world.notifyBlockOfStateChange(rotatedPos, world.getBlockState(rotatedPos).getBlock());
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
					Vec3 newCoords = DirectionUtils.rotateCoords(new Vec3(e.posX, e.posY, e.posZ), offset, rotationAxis,
							rotationCount);
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
					BlockPos newPos = new BlockPos(
							DirectionUtils.rotateCoords(e.getPos(), offset, rotationAxis, rotationCount));
					e.setPos(newPos);
					world.addTileEntity(e);
				}
			}
		}

		// Check for chests *after* we place the ones from the schematic,
		// because the schematic may not have defined the tile entities
		// properly.
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				for (int z = 0; z < length; z++) {
					BlockPos rotatedPos = new BlockPos(
							DirectionUtils.rotateCoords(new Vec3(x, y, z), offset, rotationAxis, rotationCount));
					Block block = world.getBlockState(rotatedPos).getBlock();
					TileEntity e = world.getTileEntity(rotatedPos);
					if (generateChests && !info.chestType.isEmpty()) {
						if (e instanceof TileEntityChest) {
							ChestGenHooks hook = ChestGenHooks.getInfo(info.chestType);
							WeightedRandomChestContent.generateChestContents(rand, hook.getItems(rand),
									(TileEntityChest) e, hook.getCount(rand));
						} else if (e instanceof TileEntityDispenser) {
							ChestGenHooks hook = ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_JUNGLE_DISPENSER);
							WeightedRandomChestContent.func_177631_a(rand, hook.getItems(rand), (TileEntityDispenser) e,
									hook.getCount(rand));
						}
					}
					if (info.generateSpawners && generateSpawners && block == Blocks.mob_spawner
							&& e instanceof TileEntityMobSpawner) {
						((TileEntityMobSpawner) e).getSpawnerBaseLogic()
								.setEntityName(DungeonHooks.getRandomDungeonMob(rand));
					}
				}
			}
		}

		/*
		 * Rotate blocks afterward to try to avoid block updates making invalid
		 * configurations (torches on air). Sometimes that still happens though.
		 * Also, some blocks might have their rotation in tile entity data.
		 * TODO: Look into some common rotation API.
		 */
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				for (int z = 0; z < length; z++) {
					BlockPos rotatedCoords = new BlockPos(
							DirectionUtils.rotateCoords(new Vec3(x, y, z), offset, rotationAxis, rotationCount));
					IBlockState state = world.getBlockState(rotatedCoords);
					IBlockState rotatedState = BlockRotation.getRotatedState(state, rotationCount, rotationAxis);
					if (rotatedState != null) {
						world.setBlockState(rotatedCoords, rotatedState);
					}
				}
			}
		}
	}

	public BlockPos getFuzzyMatchingLocation(Chunk chunk, EnumFacing rotationDirection, Random rand) {
		Axis rotationAxis = DirectionUtils.axisForDirection(rotationDirection);
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
			BlockPos middleBottomOfSchematic = new BlockPos(width / 2, 0, height / 2);
			BlockPos worldAnchorPos = BlockAnchorMaterialLogic.getQuickMatchingBlockInChunkStatic(AnchorType.GROUND,
					chunk, rand);
			if (worldAnchorPos != null) {
				Vec3 worldSchematicPos = DirectionUtils.rotateCoords(
						new BlockPos(0, 0, 0).subtract(middleBottomOfSchematic),
						new Vec3(worldAnchorPos.getX(), worldAnchorPos.getY(), worldAnchorPos.getZ()), rotationAxis,
						rotationCount);
				return new BlockPos(worldSchematicPos);
			} else {
				return null;
			}
		} else {
			ArrayList<BlockPos> shuffledAnchors = (ArrayList<BlockPos>) anchorBlockLocations.clone();
			Collections.shuffle(shuffledAnchors, rand);
			for (BlockPos anchorPos : shuffledAnchors) {
				BlockAnchorLogic matching = matchingMap
						.get(blocks[anchorPos.getX()][anchorPos.getY()][anchorPos.getZ()]);
				int anchorMeta = meta[anchorPos.getX()][anchorPos.getY()][anchorPos.getZ()];
				TileEntity anchorEntity = getTileEntityAt(anchorPos);
				BlockPos worldAnchorPos = matching.getQuickMatchingBlockInChunk(anchorMeta, anchorEntity, chunk, rand);
				if (worldAnchorPos != null) {
					Vec3 worldSchematicPos = DirectionUtils.rotateCoords(new BlockPos(0, 0, 0).subtract(anchorPos),
							new Vec3(worldAnchorPos.getX(), worldAnchorPos.getY(), worldAnchorPos.getZ()), rotationAxis,
							rotationCount);
					return new BlockPos(worldSchematicPos);
				}
			}
			return null;
		}
	}
}
