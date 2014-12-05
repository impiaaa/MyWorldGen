package net.boatcake.MyWorldGen.blocks;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockAnchorMaterial extends Block implements BlockAnchorBase {
	public enum AnchorType {
		AIR(1, "Air", null), DIRT(5, "Dirt", Material.ground), GROUND(0,
				"Ground", null), LAVA(4, "Lava", Material.lava), LEAVES(7,
				"Leaves", Material.leaves), SAND(8, "Sand", Material.sand), STONE(
				2, "Stone", Material.rock), WATER(3, "Water", Material.water), WOOD(
				6, "Wood", Material.wood);

		public static AnchorType get(int id) {
			for (AnchorType a : AnchorType.values()) {
				if (a.id == id) {
					return a;
				}
			}
			return null;
		}

		public final int id;
		public final Material material;
		public final String name;

		private AnchorType(int id, String name, Material mat) {
			this.id = id;
			this.name = name;
			this.material = mat;
		}
	}

	public IIcon[] icons;

	public BlockAnchorMaterial(Material par2Material) {
		super(par2Material);
		setBlockUnbreakable();
		setResistance(6000000.0F);
		setStepSound(Block.soundTypeStone);
	}

	@Override
	public int damageDropped(IBlockState state) {
		return getMetaFromState(state);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		return this.icons[meta];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs creativeTabs,
			List subBlockList) {
		for (AnchorType a : AnchorType.values()) {
			subBlockList.add(new ItemStack(item, 1, a.id));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		icons = new IIcon[16];
		for (AnchorType a : AnchorType.values()) {
			this.icons[a.id] = iconRegister.registerIcon(this.getTextureName()
					+ a.name);
		}
	}
}
