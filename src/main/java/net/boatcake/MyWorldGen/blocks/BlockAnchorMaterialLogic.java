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
		int xPos = rand.nextInt(16);
		int zPos = rand.nextInt(16);
		int height = chunk.getHeight(xPos, zPos);
		switch (AnchorType.get(myMeta)) {
		case AIR:
			return new BlockPos(xPos+chunk.xPosition*16, rand.nextInt(chunk.getWorld().getHeight()-height)+height, zPos+chunk.zPosition*16);
		case GROUND:
			return new BlockPos(xPos+chunk.xPosition*16, height, zPos+chunk.zPosition*16);
		case LAVA:
			String providerName = chunk.getWorld().getChunkProvider().makeString();
			if (providerName.equals("HellRandomLevelSource")) {
				return new BlockPos(xPos+chunk.xPosition*16, rand.nextInt(32), zPos+chunk.zPosition*16);
			}
			else {
				return new BlockPos(xPos+chunk.xPosition*16, rand.nextInt(11), zPos+chunk.zPosition*16);
			}
		case STONE:
			return new BlockPos(xPos+chunk.xPosition*16, rand.nextInt(height), zPos+chunk.zPosition*16);
		case WATER:
			return new BlockPos(xPos+chunk.xPosition*16, rand.nextInt(15)+48, zPos+chunk.zPosition*16);
		case DIRT:
		case LEAVES:
		case SAND:
		case WOOD:
		default:
			return null;
		}
	}
}
