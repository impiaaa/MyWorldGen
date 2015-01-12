package net.boatcake.MyWorldGen.blocks;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public abstract class BlockPlacementLogic {
	private static Map<String, BlockPlacementLogic> blockNameToLogic = new HashMap<String, BlockPlacementLogic>();

	public static BlockPlacementLogic get(String blockName) {
		return blockNameToLogic.get(blockName);
	}

	public static boolean placementLogicExists(String blockName) {
		return blockNameToLogic.containsKey(blockName);
	}

	public BlockPlacementLogic(String blockName) {
		blockNameToLogic.put(blockName.toLowerCase(Locale.ROOT), this);
	}

	public abstract void affectWorld(int myMeta, TileEntity myTileEntity,
			World world, BlockPos pos, boolean matchTerrain);
}
