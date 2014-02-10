package net.boatcake.MyWorldGen.blocks;

import java.util.List;

import net.boatcake.MyWorldGen.MyWorldGen;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockAnchorMaterial extends Block implements BlockAnchorBase {
	public enum AnchorType {
		GROUND(0, "Ground", null),
		AIR(1, "Air", null),
		STONE(2, "Stone", Material.rock),
		WATER(3, "Water", Material.water),
		LAVA(4, "Lava", Material.lava),
		DIRT(5, "Dirt", Material.ground),
		WOOD(6, "Wood", Material.wood),
		LEAVES(7, "Leaves", Material.leaves),
		SAND(8, "Sand", Material.sand);
		public final int id;
		public final String name;
		public final Material material;
		private static final AnchorType[] v = values();
		public static final int size = v.length;
		
		private AnchorType(int id, String name, Material mat) {
			this.id = id;
			this.name = name;
			this.material = mat;
		}

		public static AnchorType get(int id) {
			if (id > AnchorType.class.getEnumConstants().length) {
				return null;
			}
			return v[id];
		}
	}
	public IIcon[] icons;
	
	public BlockAnchorMaterial(Material par2Material) {
		super(par2Material);
		setBlockUnbreakable();
		setResistance(6000000.0F);
		setStepSound(Block.soundTypeStone);
		setBlockName("anchor");
		setCreativeTab(MyWorldGen.creativeTab);
		setBlockTextureName("anchor");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		icons = new IIcon[AnchorType.size];
		for (int i = 0; i < icons.length; i++) {
			this.icons[i] = iconRegister.registerIcon(this.getTextureName()+AnchorType.get(i).name);
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta)
    {
        return this.icons[meta];
    }
	
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs creativeTabs, List subBlockList) {
		for (int i = 0; i < AnchorType.size; i++) {
			subBlockList.add(new ItemStack(item, 1, i));
		}
	}
	
	@Override
	public int damageDropped (int metadata) {
		return metadata;
	}
	
	@Override
	public boolean matches(int myMeta, TileEntity myTileEntity, World world, int x, int y, int z) {
		return matchesStatic(myMeta, world.getBlock(x, y, z), world.getBlockMetadata(x, y, z), world.getBiomeGenForCoords(x, z));
	}

	public static boolean matchesStatic(int myMeta, Block otherBlock, int otherMeta, BiomeGenBase currentBiome) {
		AnchorType type = AnchorType.get(myMeta);
		switch (type) {
		case GROUND:
			return otherBlock == currentBiome.topBlock;
		case AIR:
			return otherBlock instanceof BlockAir || (otherBlock.getMaterial().isReplaceable() && !otherBlock.getMaterial().isLiquid());
		default:
			return !(otherBlock instanceof BlockAir) && type != null && type.material != null && otherBlock.getMaterial() == type.material;
		}
	}
}
