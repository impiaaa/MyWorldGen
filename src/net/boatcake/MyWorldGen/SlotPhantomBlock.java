package net.boatcake.MyWorldGen;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class SlotPhantomBlock extends Slot {

	public SlotPhantomBlock(IInventory inventory, int slotIndex, int posX,
			int posY) {
		super(inventory, slotIndex, posX, posY);
	}

	public boolean isItemValid(ItemStack stack) {
		return stack.getItem() instanceof ItemBlock;
	}
}
