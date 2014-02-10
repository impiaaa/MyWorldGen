package net.boatcake.MyWorldGen;

import net.boatcake.MyWorldGen.blocks.TileEntityAnchorInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerAnchorInventory extends Container {
	protected TileEntityAnchorInventory tileEntity;

	public ContainerAnchorInventory(InventoryPlayer inventoryPlayer,
			TileEntityAnchorInventory te) {
		tileEntity = te;

		// the Slot constructor takes the IInventory and the slot number in that
		// it binds to
		// and the x-y coordinates it resides on-screen
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				addSlotToContainer(new SlotPhantomBlock(tileEntity, j + i * 3,
						62 + j * 18, 17 + i * 18));
			}
		}
		bindPlayerInventory(inventoryPlayer);
	}

	protected void bindPlayerInventory(InventoryPlayer inventoryPlayer) {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9,
						8 + j * 18, 84 + i * 18));
			}
		}

		for (int i = 0; i < 9; i++) {
			addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 142));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return tileEntity.isUseableByPlayer(entityplayer);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotIdx) {
		ItemStack stackCopy = null;
		Slot slot = (Slot) inventorySlots.get(slotIdx);

		// null checks and checks if the item can be stacked (maxStackSize > 1)
		if (slot != null && slot.getHasStack()) {
			ItemStack stackInSlot = slot.getStack();
			stackCopy = stackInSlot.copy();

			// merges the item into player inventory since its in the tileEntity
			if (slotIdx < 9) {
				if (!this.mergeItemStack(stackInSlot, 0, 35, true)) {
					return null;
				}
			}
			// places it into the tileEntity is possible since its in the player
			// inventory
			else if (!slot.isItemValid(stackInSlot)
					|| !this.mergeItemStack(stackInSlot, 0, 9, false)) {
				return null;
			}

			if (stackInSlot.stackSize == 0) {
				slot.putStack(null);
			} else {
				slot.onSlotChanged();
			}

			if (stackInSlot.stackSize == stackCopy.stackSize) {
				return null;
			}
			slot.onPickupFromSlot(player, stackInSlot);
		}
		return stackCopy;
	}

}
