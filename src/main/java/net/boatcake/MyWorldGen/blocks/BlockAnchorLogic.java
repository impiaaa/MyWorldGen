package net.boatcake.MyWorldGen.blocks;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public abstract class BlockAnchorLogic {
	private static Map<String, BlockAnchorLogic> blockNameToLogic = new HashMap<String, BlockAnchorLogic>();

	public static BlockAnchorLogic get(String blockName) {
		return blockNameToLogic.get(blockName);
	}

	public static boolean isAnchorBlock(String blockName) {
		return blockNameToLogic.containsKey(blockName);
	}

	public BlockAnchorLogic(String blockName) {
		blockNameToLogic.put(blockName.toLowerCase(Locale.ROOT), this);
	}

	public abstract boolean matches(int myMeta, TileEntity myTileEntity,
			World world, BlockPos pos);

	public abstract BlockPos getQuickMatchingBlockInChunk(int myMeta,
			TileEntity myTileEntity, Chunk chunk, Random rand);
}
