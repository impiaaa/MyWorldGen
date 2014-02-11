package net.boatcake.MyWorldGen.blocks;

import net.boatcake.MyWorldGen.blocks.BlockAnchorMaterial.AnchorType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class BlockAnchorMaterialLogic extends BlockAnchorLogic {

	public static boolean matchesStatic(int myMeta, Block otherBlock,
			int otherMeta, BiomeGenBase currentBiome) {
		AnchorType type = AnchorType.get(myMeta);
		switch (type) {
		case GROUND:
			return otherBlock == currentBiome.topBlock;
		case AIR:
			return otherBlock instanceof BlockAir
					|| (otherBlock.getMaterial().isReplaceable() && !otherBlock
							.getMaterial().isLiquid());
		default:
			return !(otherBlock instanceof BlockAir) && type != null
					&& type.material != null
					&& otherBlock.getMaterial() == type.material;
		}
	}

	public BlockAnchorMaterialLogic(String blockName) {
		super(blockName);
	}

	@Override
	public boolean matches(int myMeta, TileEntity myTileEntity, World world,
			int x, int y, int z) {
		return matchesStatic(myMeta, world.getBlock(x, y, z),
				world.getBlockMetadata(x, y, z),
				world.getBiomeGenForCoords(x, z));
	}

}
