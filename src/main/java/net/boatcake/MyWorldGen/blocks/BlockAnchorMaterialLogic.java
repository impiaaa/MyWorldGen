package net.boatcake.MyWorldGen.blocks;

import java.util.Random;

import net.boatcake.MyWorldGen.blocks.BlockAnchorMaterial.AnchorType;
import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;

public class BlockAnchorMaterialLogic extends BlockAnchorLogic {

	public static boolean matchesStatic(AnchorType myType,
			IBlockState otherState, BiomeGenBase currentBiome) {
		switch (myType) {
		case GROUND:
			return otherState.equals(currentBiome.topBlock);
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

	@Override
	public BlockPos getQuickMatchingBlockInChunk(int myMeta,
			TileEntity myTileEntity, Chunk chunk, Random rand) {
		return getQuickMatchingBlockInChunkStatic(AnchorType.get(myMeta), chunk, rand);
	}
	
	public static BlockPos getQuickMatchingBlockInChunkStatic(AnchorType myType, Chunk chunk, Random rand) {
		int xPosInChunk = rand.nextInt(16);
		int zPosInChunk = rand.nextInt(16);
		int height = chunk.getHeight(xPosInChunk, zPosInChunk);
		int xPos = xPosInChunk + chunk.xPosition * 16;
		int zPos = zPosInChunk + chunk.zPosition * 16;
		World world = chunk.getWorld();
		BlockPos pos = new BlockPos(xPos, 0, zPos);
		BiomeGenBase currentBiome = world.getBiomeGenForCoords(pos);
		boolean isHell = chunk.getWorld().getChunkProvider().makeString()
				.equals("HellRandomLevelSource");
		do {
			switch (myType) {
			case AIR:
				// Anywhere between the ground and the top of the world
				pos = new BlockPos(xPos, rand.nextInt(world.getActualHeight()
						- height)
						+ height, zPos);
				break;
			case GROUND:
				// Ground level.
				pos = new BlockPos(xPos, height, zPos);
				height--;
				break;
			case LAVA:
				if (isHell) {
					// Lava ocean
					pos = new BlockPos(xPos, rand.nextInt(32), zPos);
				} else {
					// Bedrock lava
					pos = new BlockPos(xPos, rand.nextInt(11), zPos);
				}
				break;
			case STONE:
				// Anywhere below ground
				pos = new BlockPos(xPos, rand.nextInt(height), zPos);
				break;
			case WATER:
				// Ocean
				pos = new BlockPos(xPos, rand.nextInt(15) + 48, zPos);
				break;
			case DIRT:
			case LEAVES:
			case SAND:
			case WOOD:
			default:
				return null;
			}
			if (BlockAnchorMaterialLogic.matchesStatic(myType,
					chunk.getBlockState(pos), currentBiome)) {
				return pos;
			}
		} while (height >= 62);
		return null;
	}
}
