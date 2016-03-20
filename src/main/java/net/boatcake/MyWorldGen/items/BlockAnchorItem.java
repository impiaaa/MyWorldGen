package net.boatcake.MyWorldGen.items;

import com.google.common.base.Function;

import net.boatcake.MyWorldGen.blocks.BlockAnchorMaterial;
import net.minecraft.block.Block;
import net.minecraft.item.ItemMultiTexture;
import net.minecraft.item.ItemStack;

public class BlockAnchorItem extends ItemMultiTexture {
	public BlockAnchorItem(Block block) {
		super(block, block, new Function<ItemStack, String>() {
			public String apply(ItemStack stack) {
				return BlockAnchorMaterial.AnchorType.get(stack.getMetadata()).name;
			}
		});
	}
}
