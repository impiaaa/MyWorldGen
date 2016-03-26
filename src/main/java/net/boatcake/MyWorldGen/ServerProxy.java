package net.boatcake.MyWorldGen;

import java.io.File;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.server.MinecraftServer;

@SideOnly(Side.SERVER)
public class ServerProxy extends CommonProxy {
	@Override
	public File getGlobalSchemDir(String worldGenDir) {
		return MinecraftServer.getServer().getFile(worldGenDir);
	}

	@Override
	public void registerResourceHandler(WorldGenerator worldGen) {
	}
}
