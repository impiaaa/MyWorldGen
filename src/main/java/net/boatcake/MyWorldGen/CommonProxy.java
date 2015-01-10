package net.boatcake.MyWorldGen;

import java.io.File;

import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.item.Item;

public abstract class CommonProxy {
	public abstract File getGlobalSchemDir(String worldGenDir);

	public abstract void registerResourceHandler(WorldGenerator worldGen);

	public abstract void registerItem(Item item, int metadata, String itemName);

	public abstract void registerBlock(Block block, int metadata,
			String blockName);

	public abstract void registerVariants(Block block, PropertyEnum prop,
			String postfix);
}
