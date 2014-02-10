package net.boatcake.MyWorldGen;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.boatcake.MyWorldGen.blocks.BlockAnchorInventory;
import net.boatcake.MyWorldGen.blocks.BlockAnchorMaterial;
import net.boatcake.MyWorldGen.blocks.BlockIgnore;
import net.boatcake.MyWorldGen.blocks.TileEntityAnchorInventory;
import net.boatcake.MyWorldGen.items.BlockAnchorItem;
import net.boatcake.MyWorldGen.items.ItemWandLoad;
import net.boatcake.MyWorldGen.items.ItemWandSave;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraftforge.common.Configuration;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = "MyWorldGen", name = "MyWorldGen", version = "1.2")
@NetworkMod(clientSideRequired = false, serverSideRequired = false, channels = {
		"MWGPlaceSchem", "MWGGetSchem" }, packetHandler = PacketHandler.class)
public class MyWorldGen {
	@Instance("MyWorldGen")
	public static MyWorldGen instance;
	public static WorldGenerator worldGen;
	public static String resourcePath = "assets/myworldgen/worldgen";
	public static CreativeTabs creativeTab = new CreativeTabs("tabMyWorldGen") {
		public ItemStack getIconItemStack() {
			return new ItemStack(materialAnchorBlock, 1, 0);
		}
	};

	// Config options
	public static Block materialAnchorBlock;
	public static Block ignoreBlock;
	public static Block inventoryAnchorBlock;
	public static Item wandSave;
	public static Item wandLoad;
	public static File globalSchemDir;
	public static int generateNothingWeight;
	public static int generateTries;

	private File sourceFile;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		sourceFile = event.getSourceFile();
		Configuration cfg = new Configuration(
				event.getSuggestedConfigurationFile());
		try {
			cfg.load();
			materialAnchorBlock = new BlockAnchorMaterial(cfg.getBlock(
					"anchor", 1575).getInt(1575), Material.rock);
			ignoreBlock = new BlockIgnore(cfg.getBlock("ignore", 1576).getInt(
					1576), Material.circuits);
			inventoryAnchorBlock = new BlockAnchorInventory(cfg.getBlock(
					"anchorInventory", 1577).getInt(1577), Material.circuits);
			wandSave = new ItemWandSave(cfg.getItem("wandSave", 4175).getInt(
					4175));
			wandLoad = new ItemWandLoad(cfg.getItem("wandLoad", 4176).getInt(
					4176));
			String worldGenDir = cfg.get("configuration", "schematicDirectory",
					"worldgen", "Subdirectory of .minecraft").getString();
			switch (event.getSide()) {
			case CLIENT:
				globalSchemDir = new File(Minecraft.getMinecraft().mcDataDir,
						worldGenDir);
				break;
			case SERVER:
				globalSchemDir = DedicatedServer.getServer().getFile(
						worldGenDir);
				break;
			}

			generateNothingWeight = cfg
					.get("configuration", "generateNothingWeight", 10,
							"Increase this number to generate fewer structures, decrease to generate more")
					.getInt(10);
			generateTries = cfg
					.get("configuration",
							"generateTries",
							128,
							"Increase this if you have structures with complex anchor block layouts. Higher numbers will make longer load times.")
					.getInt(128);
		} catch (Exception e) {
			FMLLog.log(Level.SEVERE, e,
					"MyWorldGen could not load its configuration");
		} finally {
			if (cfg.hasChanged()) {
				cfg.save();
			}
		}
		worldGen = new WorldGenerator();

		GameRegistry.registerBlock(ignoreBlock, "ignore");
		GameRegistry.registerBlock(materialAnchorBlock, BlockAnchorItem.class,
				"anchor");
		GameRegistry.registerBlock(inventoryAnchorBlock, "anchorInventory");
		GameRegistry.registerTileEntity(TileEntityAnchorInventory.class,
				"anchorInventory");
		GameRegistry.registerWorldGenerator(worldGen);
		GameRegistry.registerItem(wandSave, wandSave.getUnlocalizedName());
		GameRegistry.registerItem(wandLoad, wandLoad.getUnlocalizedName());
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		NetworkRegistry.instance().registerGuiHandler(this, new GuiHandler());

		if (!globalSchemDir.isDirectory()) {
			globalSchemDir.mkdir();
			// Self-extract bundled schematics into the worldgen directory
			// so that the players have something to start with
			try {
				ZipFile zf = new ZipFile(sourceFile);
				ZipEntry worldGenDir = zf.getEntry(resourcePath + "/");
				if (worldGenDir != null && worldGenDir.isDirectory()) {
					for (Enumeration<? extends ZipEntry> e = zf.entries(); e
							.hasMoreElements();) {
						ZipEntry ze = e.nextElement();
						if (!ze.isDirectory()
								&& ze.getName().startsWith(
										worldGenDir.getName())) {
							writeStream(zf.getInputStream(ze), ze.getName());
						}
					}
				}
				zf.close();
			} catch (FileNotFoundException e) {
				// Not in a jar
				File f = new File(MyWorldGen.class.getClassLoader()
						.getResource(resourcePath).getPath());
				if (f.isDirectory()) {
					for (String s : f.list()) {
						try {
							writeStream(new FileInputStream(new File(f, s)), s);
						} catch (Throwable e1) {
							e1.printStackTrace();
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (event.getSide() == Side.CLIENT) {
			File resourcePacksDir = new File(Minecraft.getMinecraft().mcDataDir,
					"resourcepacks");
			for (File resourcePack : resourcePacksDir.listFiles()) {
				try {
					ZipFile zf = new ZipFile(resourcePack);
					ZipEntry worldGenDir = zf.getEntry(resourcePath + "/");
					if (worldGenDir != null && worldGenDir.isDirectory()) {
						for (Enumeration<? extends ZipEntry> e = zf.entries(); e
								.hasMoreElements();) {
							ZipEntry ze = e.nextElement();
							if (!ze.isDirectory()
									&& ze.getName().startsWith(
											worldGenDir.getName())) {
								worldGen.addSchemFromStream(zf.getInputStream(ze),
										new File(resourcePack, ze.getName()));
							}
						}
					}
					zf.close();
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}

		FMLInterModComms
				.sendMessage(
						"OpenBlocks",
						"donateUrl",
						"https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=UHDDACLRN2T46&lc=US&item_name=MyWorldGen&currency_code=USD&bn=PP-DonationsBF:btn_donate_SM.gif:NonHosted");

	}

	private static void writeStream(InputStream inStream, String outName)
			throws IOException {
		// Used for self-extracting files
		OutputStream outStream = new FileOutputStream(new File(globalSchemDir,
				new File(outName).getName()));
		byte[] buffer = new byte[256];
		int readLen;
		while (true) {
			readLen = inStream.read(buffer, 0, buffer.length);
			if (readLen <= 0) {
				break;
			}
			outStream.write(buffer, 0, readLen);
		}
		inStream.close();
		outStream.close();
	}

	@EventHandler
	public void serverStart(FMLServerAboutToStartEvent event) {
		worldGen.addSchematicsFromDirectory(globalSchemDir);
	}
}
