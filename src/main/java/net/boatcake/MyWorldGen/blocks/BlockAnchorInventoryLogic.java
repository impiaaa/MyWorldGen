package net.boatcake.MyWorldGen.blocks;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;

public class BlockAnchorInventoryLogic extends BlockAnchorLogic {

	public BlockAnchorInventoryLogic(String blockName) {
		super(blockName);
	}

	@Override
	public boolean matches(int myMeta, TileEntity myTileEntity,
			IBlockAccess world, int x, int y, int z) {
		return ((TileEntityAnchorInventory) myTileEntity).matches(world
				.getBlock(x, y, z));
	}

}
