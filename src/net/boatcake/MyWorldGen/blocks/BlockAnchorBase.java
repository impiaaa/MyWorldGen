package net.boatcake.MyWorldGen.blocks;

import net.boatcake.MyWorldGen.MyWorldGen;
import net.boatcake.MyWorldGen.blocks.BlockAnchorMaterial.AnchorType;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public interface BlockAnchorBase {

	public boolean matches(int myMeta, TileEntity myTileEntity, World world, int x, int y, int z);

}