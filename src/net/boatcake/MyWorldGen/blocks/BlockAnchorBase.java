package net.boatcake.MyWorldGen.blocks;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public interface BlockAnchorBase {

	public boolean matches(int myMeta, TileEntity myTileEntity, World world,
			int x, int y, int z);

}