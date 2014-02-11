package net.boatcake.MyWorldGen.blocks;

import net.boatcake.MyWorldGen.MyWorldGen;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockIgnore extends Block {
	public BlockIgnore(Material par2Material) {
		super(par2Material);
		setBlockUnbreakable();
		setResistance(6000000.0F);
		setBlockName("ignore");
		setCreativeTab(MyWorldGen.creativeTab);
		setBlockTextureName("MyWorldGen:ignore");
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World par1World,
			int par2, int par3, int par4) {
		return null;
	}

	public boolean isBlockSolidOnSide(World world, int x, int y, int z,
			ForgeDirection side) {
		return true;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		this.blockIcon = iconRegister.registerIcon(getTextureName());
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}
}
