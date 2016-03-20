package net.boatcake.MyWorldGen.blocks;

import net.boatcake.MyWorldGen.MyWorldGen;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockAnchorInventory extends BlockContainer implements
		BlockAnchorBase, ITileEntityProvider {

	public BlockAnchorInventory(Material par2Material) {
		super(par2Material);
		setBlockUnbreakable();
		setResistance(6000000.0F);
		setStepSound(SoundType.STONE);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int var2) {
		return new TileEntityAnchorInventory();
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos,
			IBlockState blockState, EntityPlayer player, EnumHand hand,
			ItemStack stack, EnumFacing side,
			float par7, float par8, float par9) {
		TileEntity tileEntity = world.getTileEntity(pos);
		if (tileEntity == null || player.isSneaking()) {
			return false;
		}
		// code to open gui explained later
		player.openGui(MyWorldGen.instance, 2, world, pos.getX(), pos.getY(),
				pos.getZ());
		return true;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
	}
}
