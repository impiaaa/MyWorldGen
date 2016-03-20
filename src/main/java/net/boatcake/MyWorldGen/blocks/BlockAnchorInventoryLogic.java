package net.boatcake.MyWorldGen.blocks;

import java.util.Random;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class BlockAnchorInventoryLogic extends BlockAnchorLogic {

	public BlockAnchorInventoryLogic(String blockName) {
		super(blockName);
	}

	@Override
	public boolean matches(int myMeta, TileEntity myTileEntity, World world,
			BlockPos pos) {
		return ((TileEntityAnchorInventory) myTileEntity).matches(world
				.getBlockState(pos));
	}

	@Override
	public BlockPos getQuickMatchingBlockInChunk(int myMeta,
			TileEntity myTileEntity, Chunk chunk, Random rand) {
		return null;
	}

}
