package net.boatcake.MyWorldGen.items;

import net.boatcake.MyWorldGen.blocks.BlockAnchorMaterial;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockAnchorItem extends ItemBlock {

	public BlockAnchorItem(Block block) {
		super(block);
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
