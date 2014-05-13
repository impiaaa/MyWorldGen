package net.boatcake.MyWorldGen.blocks;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockAnchorInventoryLogic extends BlockAnchorLogic {

	public BlockAnchorInventoryLogic(String blockName) {
		super(blockName);
	}

	@Override
	public boolean matches(int myMeta, TileEntity myTileEntity,
			World world, int x, int y, int z) {
		return ((TileEntityAnchorInventory) myTileEntity).matches(world
				.getBlockId(x, y, z));
	}

}
