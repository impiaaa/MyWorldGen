package net.boatcake.MyWorldGen.blocks;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockPlacementIgnore extends BlockPlacementLogic {

	public BlockPlacementIgnore(String blockName) {
		super(blockName);
	}

	@Override
	public void affectWorld(int myMeta, TileEntity myTileEntity, World world,
			int x, int y, int z, boolean matchTerrain) {
		// do nothing!
	}
}
