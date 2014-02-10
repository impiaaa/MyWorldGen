package net.boatcake.MyWorldGen.blocks;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockPlacementPreserve extends BlockPlacementLogic {

	public BlockPlacementPreserve(String blockName) {
		super(blockName);
	}

	@Override
	public void affectWorld(int myMeta, TileEntity myTileEntity, World world,
			int x, int y, int z) {
	}

}
