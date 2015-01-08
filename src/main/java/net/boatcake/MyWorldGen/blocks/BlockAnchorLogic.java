package net.boatcake.MyWorldGen.blocks;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public abstract class BlockAnchorLogic {
	private static Map<String, BlockAnchorLogic> blockNameToLogic = new HashMap<String, BlockAnchorLogic>();

	public static BlockAnchorLogic get(String blockName) {
		return blockNameToLogic.get(blockName);
	}

	public static boolean isAnchorBlock(String blockName) {
		return blockNameToLogic.containsKey(blockName);
	}

	public BlockAnchorLogic(String blockName) {
		blockNameToLogic.put(blockName, this);
	}

	public abstract boolean matches(int myMeta, TileEntity myTileEntity,
			World world, BlockPos pos);
}