package net.boatcake.MyWorldGen;

import java.io.File;

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
import net.boatcake.MyWorldGen.utils.FileUtils;
import net.boatcake.MyWorldGen.utils.NetUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameData;
import net.minecraftforge.fml.common.registry.GameRegistry;

import org.apache.logging.log4j.Logger;

@Mod(modid = MyWorldGen.MODID, name = "MyWorldGen", version = "1.3.5", dependencies = "after:OpenBlocks")
public class MyWorldGen {
	@Instance("MyWorldGen")
	public static MyWorldGen instance;

	@SidedProxy(clientSide = "net.boatcake.MyWorldGen.client.ClientProxy", serverSide = "net.boatcake.MyWorldGen.ServerProxy")
	public static CommonProxy sidedProxy;

	public static CreativeTabs creativeTab;
	public static int generateNothingWeight;
	public static int generateTries;
	public static File globalSchemDir;
	public static Block ignoreBlock;
	public static int ignoreBlockId;
	public static Block inventoryAnchorBlock;
	public static int inventoryAnchorBlockId;
	public static Logger log;
	public static Block materialAnchorBlock;
	public static int materialAnchorBlockId;
	public final static String MODID = "MyWorldGen";
	public static String resourcePath = "assets/myworldgen/worldgen";
	public static Item wandLoad;
	public static Item wandSave;
	public static WorldGenerator worldGen;

	private boolean enableItemsAndBlocks;

	private File sourceFile;
	private Configuration cfg;

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
		NetUtils.init();

		if (!globalSchemDir.isDirectory()) {
			globalSchemDir.mkdir();
			FileUtils.extractSchematics(sourceFile);
		}

		FMLInterModComms
				.sendMessage(
						"OpenBlocks",
						"donateUrl",
						"https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=UHDDACLRN2T46&lc=US&item_name=MyWorldGen&currency_code=USD&bn=PP-DonationsBF:btn_donate_SM.gif:NonHosted");

		sidedProxy.registerResourceHandler(worldGen);

		if (materialAnchorBlock != null) {
			for (BlockAnchorMaterial.AnchorType type : BlockAnchorMaterial.AnchorType
					.values()) {
				sidedProxy.registerBlock(materialAnchorBlock, type.id, MODID
						+ ":" + type.name + "_anchor");
			}
		}
		if (ignoreBlock != null) {
			sidedProxy.registerBlock(ignoreBlock, 0, MODID + ":ignore");
		}
		if (inventoryAnchorBlock != null) {
			sidedProxy.registerBlock(inventoryAnchorBlock, 0, MODID
					+ ":anchorInventory");
		}
		if (wandLoad != null) {
			sidedProxy.registerItem(wandLoad, 0, MODID + ":wandLoad");
		}
		if (wandSave != null) {
			sidedProxy.registerItem(wandSave, 0, MODID + ":wandSave");
		}
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		log = event.getModLog();
		sourceFile = event.getSourceFile();
		cfg = new Configuration(event.getSuggestedConfigurationFile());
	}

	@EventHandler
	public void init(FMLInitializationEvent event) throws Exception {
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
					return Item
							.getItemFromBlock(materialAnchorBlock == null ? Blocks.grass
									: materialAnchorBlock);
				}
			};
		}

		try {
			Property prop;
			materialAnchorBlock = registerBlock("anchor",
					BlockAnchorMaterial.class, BlockAnchorItem.class,
					BlockAnchorMaterialLogic.class);

			if (materialAnchorBlock != null) {
				sidedProxy.registerVariants(materialAnchorBlock,
						BlockAnchorMaterial.TYPE_PROP, "_anchor");
			}

			int defaultId;
			if (materialAnchorBlock == null) {
				defaultId = 1575;
			} else {
				defaultId = GameData.getBlockRegistry().getId(
						materialAnchorBlock);
			}
			prop = cfg
					.get("blocks", "materialAnchorBlock", defaultId,
							"Default ID for when an ID map is not found in a schematic");
			materialAnchorBlockId = prop.getInt(defaultId);

			ignoreBlock = registerBlock("ignore", BlockIgnore.class);
			if (ignoreBlock == null) {
				defaultId = 1576;
			} else {
				defaultId = GameData.getBlockRegistry().getId(ignoreBlock);
			}
			prop = cfg
					.get("blocks", "ignoreBlock", defaultId,
							"Default ID for when an ID map is not found in a schematic");
			ignoreBlockId = prop.getInt(defaultId);

			inventoryAnchorBlock = registerBlock("anchorInventory",
					BlockAnchorInventory.class, ItemBlock.class,
					BlockAnchorInventoryLogic.class);
			if (inventoryAnchorBlock == null) {
				defaultId = 1577;
			} else {
				defaultId = GameData.getBlockRegistry().getId(
						inventoryAnchorBlock);
			}
			prop = cfg
					.get("blocks", "inventoryAnchorBlock", defaultId,
							"Default ID for when an ID map is not found in a schematic");
			inventoryAnchorBlockId = prop.getInt(defaultId);

			wandSave = registerItem("wandSave", ItemWandSave.class, cfg);
			wandLoad = registerItem("wandLoad", ItemWandLoad.class, cfg);
		} catch (RuntimeException e) {
			throw new Exception("Could not load configuration", e);
		} catch (Exception e) {
			throw new Exception("Self-reflection failed. Is the mod intact?", e);
		}

		String worldGenDir = cfg.get("configuration", "schematicDirectory",
				"worldgen", "Subdirectory of .minecraft").getString();
		globalSchemDir = sidedProxy.getGlobalSchemDir(worldGenDir);

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
		GameRegistry.registerWorldGenerator(worldGen, 0);
	}

	private Block registerBlock(String name, Class<? extends Block> blockClass,
			Class<? extends ItemBlock> itemBlockClass,
			Class<? extends BlockAnchorLogic> matching, Object... itemCtorArgs)
			throws Exception {
		Block block = null;
		if (enableItemsAndBlocks) {
			block = blockClass.getConstructor(Material.class).newInstance(
					Material.circuits);
			block.setUnlocalizedName(name);
			block.setCreativeTab(creativeTab);
			GameRegistry.registerBlock(block, itemBlockClass, name,
					itemCtorArgs);
		}
		new BlockPlacementIgnore(MyWorldGen.MODID + ":" + name);
		if (matching != null) {
			matching.getConstructor(String.class).newInstance(
					MyWorldGen.MODID + ":" + name);
		}
		return block;
	}

	private Block registerBlock(String name, Class<? extends Block> blockClass)
			throws Exception {
		return registerBlock(name, blockClass, ItemBlock.class, null,
				new Object[] {});
	}

	private Item registerItem(String name, Class<? extends Item> itemClass,
			Configuration cfg) throws Exception {
		Item item = null;
		if (enableItemsAndBlocks) {
			item = itemClass.getConstructor().newInstance();
			item.setUnlocalizedName(name);
			item.setCreativeTab(creativeTab);
			GameRegistry.registerItem(item, name);
		}
		return item;
	}

	@EventHandler
	public void serverStart(FMLServerAboutToStartEvent event) {
		worldGen.addSchematicsFromDirectory(globalSchemDir);
	}
}
