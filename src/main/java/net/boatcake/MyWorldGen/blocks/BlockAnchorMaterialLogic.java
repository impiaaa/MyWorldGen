package net.boatcake.MyWorldGen.blocks;

import net.boatcake.MyWorldGen.blocks.BlockAnchorMaterial.AnchorType;
import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class BlockAnchorMaterialLogic extends BlockAnchorLogic {

	public static boolean matchesStatic(IBlockState myState,
			IBlockState otherState, BiomeGenBase currentBiome) {
		AnchorType type = AnchorType.get(myMeta);
		switch (type) {
		case GROUND:
			return otherState.getBlock() == currentBiome.topBlock;
		case AIR:
			return otherState.getBlock() instanceof BlockAir
					|| (otherState.getBlock().getMaterial().isReplaceable() && !otherState
							.getBlock().getMaterial().isLiquid());
		default:
			return !(otherState.getBlock() instanceof BlockAir) && type != null
					&& type.material != null
					&& otherState.getBlock().getMaterial() == type.material;
		}
	}

	public BlockAnchorMaterialLogic(String blockName) {
		super(blockName);
	}

	@Override
	public boolean matches(IBlockState myState, TileEntity myTileEntity,
			World world, BlockPos pos) {
		return matchesStatic(myState, world.getBlockState(pos),
				world.getBiomeGenForCoords(pos));
	}

}
