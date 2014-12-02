package net.boatcake.MyWorldGen.items;

import net.boatcake.MyWorldGen.MyWorldGen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class ItemWandLoad extends Item {
	public ItemWandLoad() {
		super();
		setMaxStackSize(1);
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world,
			BlockPos blockPos, EnumFacing side, float hitX, float hitY,
			float hitZ) {
		if (world.isRemote) {
			BlockPos newPos = blockPos.offset(side);
			player.openGui(MyWorldGen.instance, 1, world, newPos.getX(),
					newPos.getY(), newPos.getZ());
		}
		return true;
	}
}
