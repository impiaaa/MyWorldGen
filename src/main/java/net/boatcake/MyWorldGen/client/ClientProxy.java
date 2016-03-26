package net.boatcake.MyWorldGen.client;

import java.io.File;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.boatcake.MyWorldGen.CommonProxy;
import net.boatcake.MyWorldGen.WorldGenerator;
import net.minecraft.client.Minecraft;

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
}
