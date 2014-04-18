package net.boatcake.MyWorldGen.items;

import net.boatcake.MyWorldGen.MyWorldGen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Facing;
import net.minecraft.world.World;

public class ItemWandLoad extends Item {
	public ItemWandLoad() {
		super();
		setMaxStackSize(1);
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world,
			int blockX, int blockY, int blockZ, int side, float hitX,
			float hitY, float hitZ) {
		if (world.isRemote) {
			player.openGui(MyWorldGen.instance, 1, world, blockX
					+ Facing.offsetsXForSide[side], blockY
					+ Facing.offsetsYForSide[side], blockZ
					+ Facing.offsetsZForSide[side]);
		}
		return true;
	}
}
