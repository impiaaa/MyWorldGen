package net.boatcake.MyWorldGen.blocks;

import net.boatcake.MyWorldGen.blocks.BlockAnchorMaterial.AnchorType;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.BiomeGenBase;

public class BlockAnchorMaterialLogic extends BlockAnchorLogic {

	public static boolean matchesStatic(int myMeta, int otherBlock,
			int otherMeta, BiomeGenBase currentBiome) {
		AnchorType type = AnchorType.get(myMeta);
		switch (type) {
		case GROUND:
			return otherBlock == currentBiome.topBlock;
		case AIR:
			return otherBlock == 0
					|| (Block.blocksList[otherBlock].blockMaterial
							.isReplaceable() && !Block.blocksList[otherBlock].blockMaterial
							.isLiquid());
		default:
			return otherBlock != 0
					&& type != null
					&& type.material != null
					&& Block.blocksList[otherBlock].blockMaterial == type.material;
		}
	}

	public BlockAnchorMaterialLogic(String blockName) {
		super(blockName);
	}

	@Override
	public boolean matches(int myMeta, TileEntity myTileEntity, IBlockAccess world,
			int x, int y, int z) {
		return matchesStatic(myMeta, world.getBlockId(x, y, z),
				world.getBlockMetadata(x, y, z),
				world.getBiomeGenForCoords(x, z));
	}

}
