package net.boatcake.MyWorldGen;

import java.io.File;

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
}
