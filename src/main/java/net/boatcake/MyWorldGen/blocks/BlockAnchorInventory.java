package net.boatcake.MyWorldGen.blocks;

import net.boatcake.MyWorldGen.MyWorldGen;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class BlockAnchorInventory extends BlockContainer implements BlockAnchorBase, ITileEntityProvider {

	public BlockAnchorInventory(Material par2Material) {
		super(par2Material);
		setBlockUnbreakable();
		setResistance(6000000.0F);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int var2) {
		return new TileEntityAnchorInventory();
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState blockState, EntityPlayer player,
			EnumFacing side, float par7, float par8, float par9) {
		TileEntity tileEntity = world.getTileEntity(pos);
		if (tileEntity == null || player.isSneaking()) {
			return false;
		}
		// code to open gui explained later
		player.openGui(MyWorldGen.instance, 2, world, pos.getX(), pos.getY(), pos.getZ());
		return true;
	}

	@Override
	public int getRenderType() {
		return 3;
	}
}
