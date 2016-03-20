package net.boatcake.MyWorldGen.client;

import net.boatcake.MyWorldGen.MyWorldGen;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class NamespacedStateMap extends StateMapperBase {
	private String postfix;
	private PropertyEnum prop;

	public NamespacedStateMap(PropertyEnum typeProp, String string) {
		postfix = string;
		prop = typeProp;
	}

	@Override
	protected ModelResourceLocation getModelResourceLocation(IBlockState arg0) {
		String s = ((IStringSerializable) arg0.getValue(prop)).getName();
		return new ModelResourceLocation(new ResourceLocation(MyWorldGen.MODID,
				s + postfix), "normal");
	}
}
