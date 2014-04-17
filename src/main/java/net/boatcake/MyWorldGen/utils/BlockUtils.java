package net.boatcake.MyWorldGen.utils;

import net.minecraft.block.Block;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;

public class BlockUtils {

	public static Block getBlockFromName(String blockName) {
		int colon = blockName.indexOf(':');
		Block block = null;
		String name = blockName;
		if (colon != -1) {
			String modId = blockName.substring(0, colon);
			name = blockName.substring(colon);
			block = GameRegistry.findBlock(modId, name);
		}
		if (block == null) {
			String unlocalizedName = "tile." + name;
			for (Block block1 : Block.blocksList) {
				if (block1 != null
						&& unlocalizedName.equals(block1.getUnlocalizedName())) {
					return block1;
				}
			}
			return null;
		} else {
			return block;
		}
	}

	public static String getNameForBlock(Block block) {
		UniqueIdentifier uid = GameRegistry.findUniqueIdentifierFor(block);
		if (uid == null) {
			return block.getUnlocalizedName().substring(5);
		} else {
			return uid.modId + ":" + uid.name;
		}
	}

}
