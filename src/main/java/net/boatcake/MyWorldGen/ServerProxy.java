package net.boatcake.MyWorldGen;

import java.io.File;

import net.minecraft.server.MinecraftServer;

public class ServerProxy extends CommonProxy {
	@Override
	public File getGlobalSchemDir(String worldGenDir) {
		return MinecraftServer.getServer().getFile(worldGenDir);
	}

	@Override
	public void registerResourceHandler(WorldGenerator worldGen) {
	}
}
