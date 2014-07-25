package net.boatcake.MyWorldGen;

import java.io.File;

public abstract class CommonProxy {
	public abstract File getGlobalSchemDir(String worldGenDir);
	public abstract void registerResourceHandler(WorldGenerator worldGen);
}
