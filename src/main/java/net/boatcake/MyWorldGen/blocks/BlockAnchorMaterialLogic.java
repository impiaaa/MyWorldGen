package net.boatcake.MyWorldGen.blocks;

import java.util.Random;

import net.boatcake.MyWorldGen.blocks.BlockAnchorMaterial.AnchorType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;

public class BlockAnchorMaterialLogic extends BlockAnchorLogic {

	public static boolean matchesStatic(AnchorType myType, Block otherBlock, int otherMeta, BiomeGenBase currentBiome) {
		switch (myType) {
		case GROUND:
			return otherBlock == currentBiome.topBlock;
		case AIR:
			return otherBlock instanceof BlockAir
					|| (otherBlock.getMaterial().isReplaceable() && !otherBlock.getMaterial().isLiquid());
		default:
			return !(otherBlock instanceof BlockAir) && myType != null && myType.material != null
					&& otherBlock.getMaterial() == myType.material;
		}
	}

	public BlockAnchorMaterialLogic(String blockName) {
		super(blockName);
	}

	@Override
	public boolean matches(int myMeta, TileEntity myTileEntity, World world, int x, int y, int z) {
		return matchesStatic(AnchorType.get(myMeta), world.getBlock(x, y, z), world.getBlockMetadata(x, y, z),
				world.getBiomeGenForCoords(x, z));
	}

	@Override
	public Integer[] getQuickMatchingBlockInChunk(int myMeta, TileEntity myTileEntity, Chunk chunk, Random rand) {
		return getQuickMatchingBlockInChunkStatic(AnchorType.get(myMeta), chunk, rand);
	}

	public static Integer[] getQuickMatchingBlockInChunkStatic(AnchorType myType, Chunk chunk, Random rand) {
		int xPosInChunk = rand.nextInt(16);
		int zPosInChunk = rand.nextInt(16);
		int height = chunk.getHeightValue(xPosInChunk, zPosInChunk);
		int xPos = xPosInChunk + chunk.xPosition * 16;
		int zPos = zPosInChunk + chunk.zPosition * 16;
		World world = chunk.worldObj;
		int yPos;
		BiomeGenBase currentBiome = world.getBiomeGenForCoords(zPos, zPos);
		boolean isHell = world.getChunkProvider().makeString().equals("HellRandomLevelSource");
		do {
			switch (myType) {
			case AIR:
				// Anywhere between the ground and the top of the world
				yPos = rand.nextInt(world.getActualHeight() - height) + height;
				break;
			case GROUND:
				// Ground level.
				yPos = height;
				height--;
				break;
			case LAVA:
				if (isHell) {
					// Lava ocean
					yPos = rand.nextInt(32);
				} else {
					// Bedrock lava
					yPos = rand.nextInt(11);
				}
				break;
			case STONE:
				// Anywhere below ground
				yPos = rand.nextInt(height);
				break;
			case WATER:
				// Ocean
				yPos = rand.nextInt(15) + 48;
				break;
			case DIRT:
			case LEAVES:
			case SAND:
			case WOOD:
			default:
				return null;
			}
			if (BlockAnchorMaterialLogic.matchesStatic(myType, chunk.getBlock(xPosInChunk, yPos, zPosInChunk),
					chunk.getBlockMetadata(xPosInChunk, yPos, zPosInChunk), currentBiome)) {
				return new Integer[] { xPos, yPos, zPos };
			}
		} while (height >= 62);
		return null;
	}
}
