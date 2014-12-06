package net.boatcake.MyWorldGen.client;

import java.io.File;

import net.boatcake.MyWorldGen.CommonProxy;
import net.boatcake.MyWorldGen.WorldGenerator;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {
	@Override
	public File getGlobalSchemDir(String worldGenDir) {
		return new File(Minecraft.getMinecraft().mcDataDir, worldGenDir);
	}

	@Override
	public void registerResourceHandler(WorldGenerator worldGen) {
		new ResourceManagerListener(worldGen).register();
	}

	@Override
	public void registerItem(Item item, int metadata, String itemName) {
		ItemModelMesher mesher = Minecraft.getMinecraft().getRenderItem()
				.getItemModelMesher();
		mesher.register(item, metadata, new ModelResourceLocation(itemName,
				"inventory"));
	}

	@Override
	public void registerBlock(Block block, int metadata, String blockName) {
		registerItem(Item.getItemFromBlock(block), metadata, blockName);
	}
}
