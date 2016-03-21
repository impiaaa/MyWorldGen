package net.boatcake.MyWorldGen.blocks;

import net.boatcake.MyWorldGen.blocks.BlockAnchorMaterial.AnchorType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class BlockPlacementMaterialAnchor extends BlockPlacementLogic {

	public BlockPlacementMaterialAnchor(String blockName) {
		super(blockName);
	}

	@Override
	public void affectWorld(int myMeta, TileEntity myTileEntity, World world, BlockPos pos, boolean matchTerrain) {
		if (matchTerrain) {
			switch (AnchorType.get(myMeta)) {
			case AIR:
				world.setBlockToAir(pos);
				break;
			case DIRT:
				world.setBlockState(pos, Blocks.dirt.getDefaultState());
				break;
			case GROUND:
				BiomeGenBase biome = world.getBiomeGenForCoords(pos);
				world.setBlockState(pos, biome.topBlock);
				setBlocksDownward(world, pos.down(), biome.fillerBlock);
				break;
			case LAVA:
				world.setBlockState(pos, Blocks.lava.getDefaultState());
				break;
			case SAND:
				setBlocksDownward(world, pos, Blocks.sand.getDefaultState());
				break;
			case STONE:
				world.setBlockState(pos, Blocks.stone.getDefaultState());
				break;
			case WATER:
				world.setBlockState(pos, Blocks.water.getDefaultState());
				break;
			case LEAVES:
			case WOOD:
			default:
				break;
			}
		}
	}

	private void setBlocksDownward(World world, BlockPos pos, IBlockState blockState) {
		while (!world.getBlockState(pos).isFullCube()) {
			world.setBlockState(pos, blockState);
			pos = pos.down();
		}
	}
}
