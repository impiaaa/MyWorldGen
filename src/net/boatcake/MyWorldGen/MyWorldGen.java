package net.boatcake.MyWorldGen;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.EnumMap;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.boatcake.MyWorldGen.blocks.BlockAnchorInventory;
import net.boatcake.MyWorldGen.blocks.BlockAnchorInventoryLogic;
import net.boatcake.MyWorldGen.blocks.BlockAnchorLogic;
import net.boatcake.MyWorldGen.blocks.BlockAnchorMaterial;
import net.boatcake.MyWorldGen.blocks.BlockAnchorMaterialLogic;
import net.boatcake.MyWorldGen.blocks.BlockIgnore;
import net.boatcake.MyWorldGen.blocks.BlockPlacementIgnore;
import net.boatcake.MyWorldGen.blocks.TileEntityAnchorInventory;
import net.boatcake.MyWorldGen.items.BlockAnchorItem;
import net.boatcake.MyWorldGen.items.ItemWandLoad;
import net.boatcake.MyWorldGen.items.ItemWandSave;
import net.boatcake.MyWorldGen.network.MWGCodec;
import net.boatcake.MyWorldGen.network.MWGMessage;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.config.Configuration;

import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.network.FMLEmbeddedChannel;
import cpw.mods.fml.common.network.FMLOutboundHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mod(modid = MyWorldGen.MODID, name = "MyWorldGen", version = "1.3")
public class MyWorldGen {
	public static CreativeTabs creativeTab;
	public static int generateNothingWeight;
	public static int generateTries;
	public static File globalSchemDir;
	public static Block ignoreBlock;
	@Instance("MyWorldGen")
	public static MyWorldGen instance;
	public static Block inventoryAnchorBlock;
	public static Logger log;
	public static Block materialAnchorBlock;
	public final static String MODID = "MyWorldGen";
	public static EnumMap<Side, FMLEmbeddedChannel> net;
	public static String resourcePath = "assets/myworldgen/worldgen";
	public static Item wandLoad;
	public static Item wandSave;
	public static WorldGenerator worldGen;

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

	private boolean enableItemsAndBlocks;

	private File sourceFile;

	@EventHandler
	public void init(FMLInitializationEvent event) {
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
		net = NetworkRegistry.INSTANCE.newChannel("MyWorldGen", new MWGCodec());

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
			File resourcePacksDir = new File(
					Minecraft.getMinecraft().mcDataDir, "resourcepacks");
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
								worldGen.addSchemFromStream(zf
										.getInputStream(ze), new File(
										resourcePack, ze.getName()));
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

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		log = event.getModLog();
		sourceFile = event.getSourceFile();
		Configuration cfg = new Configuration(
				event.getSuggestedConfigurationFile());
		cfg.load();

		enableItemsAndBlocks = cfg
				.get("configuration",
						"enableItemsAndBlocks",
						true,
						"Turn this off if you're running a server and the clients don't have the mod installed")
				.getBoolean(true);
		if (!enableItemsAndBlocks) {
			log.info("Skipping block & item registration");
		}

		if (enableItemsAndBlocks) {
			creativeTab = new CreativeTabs("tabMyWorldGen") {
				@Override
				public Item getTabIconItem() {
					return ItemBlock.getItemFromBlock(materialAnchorBlock == null ? Blocks.grass : materialAnchorBlock);
				}
			};
		}

		try {
			materialAnchorBlock = registerBlock("anchor",
					BlockAnchorMaterial.class, cfg, BlockAnchorItem.class,
					BlockAnchorMaterialLogic.class);
			ignoreBlock = registerBlock("ignore", BlockIgnore.class, cfg,
					null, null);
			inventoryAnchorBlock = registerBlock("anchorInventory",
					BlockAnchorInventory.class, cfg, null,
					BlockAnchorInventoryLogic.class);
			wandSave = registerItem("wandSave", ItemWandSave.class, cfg);
			wandLoad = registerItem("wandLoad", ItemWandLoad.class, cfg);
		} catch (RuntimeException e) {
			log.fatal("Could not load configuration");
			e.printStackTrace();
			return;
		} catch (ReflectiveOperationException e) {
			log.fatal("Self-reflection failed. Is the mod intact?");
			e.printStackTrace();
			return;
		}

		String worldGenDir = cfg.get("configuration", "schematicDirectory",
				"worldgen", "Subdirectory of .minecraft").getString();

		switch (event.getSide()) {
		case CLIENT:
			globalSchemDir = new File(Minecraft.getMinecraft().mcDataDir,
					worldGenDir);
			break;
		case SERVER:
			globalSchemDir = MinecraftServer.getServer().getFile(worldGenDir);
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

		if (cfg.hasChanged()) {
			cfg.save();
		}

		worldGen = new WorldGenerator();

		GameRegistry.registerTileEntity(TileEntityAnchorInventory.class,
				"anchorInventory");
		GameRegistry.registerWorldGenerator(worldGen, 3);
	}

	private Block registerBlock(String name,
			Class<? extends Block> blockClass, Configuration cfg,
			Class<? extends ItemBlock> itemBlockClass,
			Class<? extends BlockAnchorLogic> matching)
			throws RuntimeException, ReflectiveOperationException {
		Block block = null;
		if (enableItemsAndBlocks) {
			block = blockClass.getConstructor(Material.class)
					.newInstance(Material.circuits);
			block.setBlockName(name);
			block.setBlockTextureName(MyWorldGen.MODID + ":" + name);
			block.setCreativeTab(creativeTab);
			GameRegistry
					.registerBlock(block,
							(itemBlockClass == null) ? ItemBlock.class
									: itemBlockClass, name);
		}
		new BlockPlacementIgnore(name);
		if (matching != null) {
			matching.getConstructor(String.class).newInstance(name);
		}
		return block;
	}

	private Item registerItem(String name, 
			Class<? extends Item> itemClass, Configuration cfg)
			throws RuntimeException, ReflectiveOperationException {
		Item item = null;
		if (enableItemsAndBlocks) {
			item = itemClass.getConstructor().newInstance();
			item.setUnlocalizedName(name);
			item.setTextureName(MyWorldGen.MODID + ":" + name);
			item.setCreativeTab(creativeTab);
			GameRegistry.registerItem(item, name);
		}
		return item;
	}

	public void sendTo(MWGMessage message, EntityPlayerMP player) {
		net.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET)
				.set(FMLOutboundHandler.OutboundTarget.PLAYER);
		net.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS)
				.set(player);
		net.get(Side.SERVER).writeAndFlush(message);
	}

	@SideOnly(Side.CLIENT)
	public void sendToServer(MWGMessage message) {
		net.get(Side.CLIENT).attr(FMLOutboundHandler.FML_MESSAGETARGET)
				.set(FMLOutboundHandler.OutboundTarget.TOSERVER);
		net.get(Side.CLIENT).writeAndFlush(message);
	}

	@EventHandler
	public void serverStart(FMLServerAboutToStartEvent event) {
		worldGen.addSchematicsFromDirectory(globalSchemDir);
	}
}
