package net.boatcake.MyWorldGen.items;

import net.boatcake.MyWorldGen.MyWorldGen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemWandLoad extends Item {
	public ItemWandLoad() {
		this.maxStackSize = 1;
	}

	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos blockPos,
			EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (world.isRemote) {
			BlockPos newPos = blockPos.offset(side);
			player.openGui(MyWorldGen.instance, 1, world, newPos.getX(), newPos.getY(), newPos.getZ());
		}
		return EnumActionResult.SUCCESS;
	}
}
