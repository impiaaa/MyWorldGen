package net.boatcake.MyWorldGen.blocks;

import net.boatcake.MyWorldGen.blocks.BlockAnchorMaterial.AnchorType;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class BlockPlacementMaterialAnchor extends BlockPlacementLogic {

	public BlockPlacementMaterialAnchor(String blockName) {
		super(blockName);
	}

	@Override
	public void affectWorld(int myMeta, TileEntity myTileEntity, World world, int x, int y, int z,
			boolean matchTerrain) {
		if (matchTerrain) {
			switch (AnchorType.get(myMeta)) {
			case AIR:
				world.setBlockToAir(x, y, z);
				break;
			case DIRT:
				world.setBlock(x, y, z, Blocks.dirt);
				break;
			case GROUND:
				BiomeGenBase biome = world.getBiomeGenForCoords(x, z);
				world.setBlock(x, y, z, biome.topBlock);
				setBlocksDownward(world, x, y - 1, z, biome.fillerBlock);
				break;
			case LAVA:
				world.setBlock(x, y, z, Blocks.lava);
				break;
			case SAND:
				setBlocksDownward(world, x, y, z, Blocks.sand);
				break;
			case STONE:
				world.setBlock(x, y, z, Blocks.stone);
				break;
			case WATER:
				world.setBlock(x, y, z, Blocks.water);
				break;
			case LEAVES:
			case WOOD:
			default:
				break;
			}
		}
	}

	private void setBlocksDownward(World world, int x, int y, int z, Block block) {
		while (!world.getBlock(x, y, z).isNormalCube()) {
			world.setBlock(x, y, z, block);
			y--;
		}
	}
}
