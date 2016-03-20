package net.boatcake.MyWorldGen.client;

import java.io.File;

import net.boatcake.MyWorldGen.CommonProxy;
import net.boatcake.MyWorldGen.MyWorldGen;
import net.boatcake.MyWorldGen.WorldGenerator;
import net.boatcake.MyWorldGen.blocks.BlockAnchorMaterial;
import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelBakery;
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
		Minecraft
				.getMinecraft()
				.getRenderItem()
				.getItemModelMesher()
				.register(item, metadata,
						new ModelResourceLocation(itemName, "inventory"));
	}

	@Override
	public void registerBlock(Block block, int metadata, String blockName) {
		registerItem(Item.getItemFromBlock(block), metadata, blockName);
	}

	@Override
	public void registerVariants(Block block, PropertyEnum prop, String postfix) {
		String[] names = new String[BlockAnchorMaterial.AnchorType.values().length];
		for (int i = 0; i < names.length; i++) {
			names[i] = MyWorldGen.MODID + ":"
					+ BlockAnchorMaterial.AnchorType.get(i).name + postfix;
		}
		ModelBakery.addVariantName(Item.getItemFromBlock(block), names);
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher()
				.getModelManager().getBlockModelShapes()
				.registerBlockWithStateMapper(block, new NamespacedStateMap(prop, postfix));
	}
}
