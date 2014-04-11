package net.boatcake.MyWorldGen.items;

import net.boatcake.MyWorldGen.blocks.BlockAnchorMaterial;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class BlockAnchorItem extends ItemBlock {
	public BlockAnchorItem(int id) {
		super(id);
		this.setMaxDamage(0);
		setHasSubtypes(true);
	}

	@Override
	public int getMetadata(int damageValue) {
		return damageValue;
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return super.getUnlocalizedName()
				+ BlockAnchorMaterial.AnchorType.get(stack.getItemDamage()).name;
	}
}
