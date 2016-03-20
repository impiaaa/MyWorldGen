package net.boatcake.MyWorldGen.blocks;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockAnchorMaterial extends Block implements BlockAnchorBase {
	public enum AnchorType implements IStringSerializable {
		GROUND(0, "ground", null), AIR(1, "air", null), STONE(2, "stone",
				Material.rock), WATER(3, "water", Material.water), LAVA(4,
				"lava", Material.lava), DIRT(5, "dirt", Material.ground), WOOD(
				6, "wood", Material.wood), LEAVES(7, "leaves", Material.leaves), SAND(
				8, "sand", Material.sand);

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

		@Override
		public String getName() {
			return name;
		}
	}

	public static final PropertyEnum<AnchorType> TYPE_PROP = PropertyEnum.create("type",
			AnchorType.class);

	public BlockAnchorMaterial(Material par2Material) {
		super(par2Material);
		setBlockUnbreakable();
		setResistance(6000000.0F);
		setStepSound(SoundType.STONE);
		setDefaultState(blockState.getBaseState().withProperty(TYPE_PROP,
				AnchorType.GROUND));
	}

	@Override
	public int damageDropped(IBlockState state) {
		return ((AnchorType) state.getValue(TYPE_PROP)).id;
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
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(TYPE_PROP, AnchorType.get(meta));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return ((AnchorType) state.getValue(TYPE_PROP)).id;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { TYPE_PROP });
	}
}
