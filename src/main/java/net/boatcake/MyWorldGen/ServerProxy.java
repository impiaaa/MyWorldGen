package net.boatcake.MyWorldGen;

import java.io.File;

import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.SERVER)
public class ServerProxy extends CommonProxy {
	@Override
	public File getGlobalSchemDir(String worldGenDir) {
		return MinecraftServer.getServer().getFile(worldGenDir);
	}

	@Override
	public void registerResourceHandler(WorldGenerator worldGen) {
	}

	@Override
	public void registerItem(Item item, int metadata, String itemName) {
	}

	@Override
	public void registerBlock(Block block, int metadata, String blockName) {
	}

	@Override
	public void registerVariants(Block block, PropertyEnum prop, String postfix) {
	}
}
