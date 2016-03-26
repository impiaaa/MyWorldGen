package net.boatcake.MyWorldGen.blocks;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public abstract class BlockAnchorLogic {
	private static Map<String, BlockAnchorLogic> blockNameToLogic = new HashMap<String, BlockAnchorLogic>();

	public static BlockAnchorLogic get(String blockName) {
		return blockNameToLogic.get(blockName.toLowerCase(Locale.ROOT));
	}

	public static boolean isAnchorBlock(String blockName) {
		return blockNameToLogic.containsKey(blockName.toLowerCase(Locale.ROOT));
	}

	public BlockAnchorLogic(String blockName) {
		blockNameToLogic.put(blockName.toLowerCase(Locale.ROOT), this);
	}

	public abstract boolean matches(int myMeta, TileEntity myTileEntity, World world, int x, int y, int z);

	public abstract Integer[] getQuickMatchingBlockInChunk(int myMeta, TileEntity myTileEntity, Chunk chunk,
			Random rand);
}
