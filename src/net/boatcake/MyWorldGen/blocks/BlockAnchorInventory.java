package net.boatcake.MyWorldGen.blocks;

import net.boatcake.MyWorldGen.MyWorldGen;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockAnchorInventory extends BlockContainer implements
		BlockAnchorBase {

	public BlockAnchorInventory(int par1, Material par2Material) {
		super(par1, par2Material);
		setBlockUnbreakable();
		setResistance(6000000.0F);
		setStepSound(Block.soundStoneFootstep);
		setUnlocalizedName("anchorInventory");
		setCreativeTab(MyWorldGen.creativeTab);
	}

	@Override
	public boolean matches(int myMeta, TileEntity myTileEntity, World world,
			int x, int y, int z) {
		return ((TileEntityAnchorInventory) myTileEntity).matches(world
				.getBlockId(x, y, z));
	}

	@Override
	public TileEntity createNewTileEntity(World world) {
		return new TileEntityAnchorInventory();
	}

	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister par1IconRegister) {
		this.blockIcon = par1IconRegister.registerIcon("MyWorldGen:"
				+ this.getUnlocalizedName().substring(5));
	}

	public boolean onBlockActivated(World world, int x, int y, int z,
			EntityPlayer player, int par6, float par7, float par8, float par9) {
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
		if (tileEntity == null || player.isSneaking()) {
			return false;
		}
		// code to open gui explained later
		player.openGui(MyWorldGen.instance, 2, world, x, y, z);
		return true;
	}
}
