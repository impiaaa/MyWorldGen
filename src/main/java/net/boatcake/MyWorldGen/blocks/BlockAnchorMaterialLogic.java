package net.boatcake.MyWorldGen.blocks;

import net.boatcake.MyWorldGen.blocks.BlockAnchorMaterial.AnchorType;
import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class BlockAnchorMaterialLogic extends BlockAnchorLogic {

	public static boolean matchesStatic(AnchorType myType,
			IBlockState otherState, BiomeGenBase currentBiome) {
		switch (myType) {
		case GROUND:
			return otherState.getBlock() == currentBiome.topBlock;
		case AIR:
			return otherState.getBlock() instanceof BlockAir
					|| (otherState.getBlock().getMaterial().isReplaceable() && !otherState
							.getBlock().getMaterial().isLiquid());
		default:
			return !(otherState.getBlock() instanceof BlockAir)
					&& myType != null && myType.material != null
					&& otherState.getBlock().getMaterial() == myType.material;
		}
	}

	public BlockAnchorMaterialLogic(String blockName) {
		super(blockName);
	}

	@Override
	public boolean matches(int myMeta, TileEntity myTileEntity, World world,
			BlockPos pos) {
		return matchesStatic(AnchorType.get(myMeta), world.getBlockState(pos),
				world.getBiomeGenForCoords(pos));
	}

}
