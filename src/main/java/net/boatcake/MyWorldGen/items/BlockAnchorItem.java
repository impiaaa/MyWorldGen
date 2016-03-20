package net.boatcake.MyWorldGen.items;

import net.boatcake.MyWorldGen.blocks.BlockAnchorMaterial;
import net.minecraft.block.Block;
import net.minecraft.item.ItemMultiTexture;
import net.minecraft.item.ItemStack;

import com.google.common.base.Function;

public class BlockAnchorItem extends ItemMultiTexture {
	public BlockAnchorItem(Block block) {
		super(block, block, new Function<ItemStack, String>() {
			public String apply(ItemStack stack) {
				return BlockAnchorMaterial.AnchorType.get(stack.getMetadata()).name;
			}
		});
	}
}
