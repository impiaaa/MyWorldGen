package net.boatcake.MyWorldGen.blocks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class BlockPlacementIgnore extends BlockPlacementLogic {

	public BlockPlacementIgnore(String blockName) {
		super(blockName);
	}

	@Override
	public void affectWorld(IBlockState myState, TileEntity myTileEntity,
			World world, BlockPos pos) {
		// do nothing!
	}
}
