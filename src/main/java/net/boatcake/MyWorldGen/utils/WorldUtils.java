package net.boatcake.MyWorldGen.utils;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public class WorldUtils {

	public static NBTTagList getEntities(World world, int x1, int y1, int z1,
			int x2, int y2, int z2) {
		assert !world.isRemote;
		if (x1 > x2) {
			int t = x1;
			x1 = x2;
			x2 = t;
		}
		if (y1 > y2) {
			int t = y1;
			y1 = y2;
			y2 = t;
		}
		if (z1 > z2) {
			int t = z1;
			z1 = z2;
			z2 = t;
		}
		NBTTagList entities = new NBTTagList();
		for (Object o : world.getEntitiesWithinAABB(Entity.class, AxisAlignedBB
				.getBoundingBox(x1 - 0.5, y1 - 0.5, z1 - 0.5, x2 + 0.5,
						y2 + 0.5, z2 + 0.5))) {
			NBTTagCompound enbt = new NBTTagCompound();
			((Entity) o).writeToNBT(enbt);
			NBTTagList posNBT = (NBTTagList) enbt.getTag("Pos");
			posNBT.func_150304_a(0, new NBTTagDouble(posNBT.func_150309_d(0)
					- x1));
			posNBT.func_150304_a(1, new NBTTagDouble(posNBT.func_150309_d(1)
					- y1));
			posNBT.func_150304_a(2, new NBTTagDouble(posNBT.func_150309_d(2)
					- z1));
			entities.appendTag(enbt);
		}
		return entities;
	}

	public static NBTTagList getTileEntities(World world, int x1, int y1,
			int z1, int x2, int y2, int z2) {
		assert !world.isRemote;
		if (x1 > x2) {
			int t = x1;
			x1 = x2;
			x2 = t;
		}
		if (y1 > y2) {
			int t = y1;
			y1 = y2;
			y2 = t;
		}
		if (z1 > z2) {
			int t = z1;
			z1 = z2;
			z2 = t;
		}
		NBTTagList tileEntities = new NBTTagList();
		for (int x = x1; x <= x2; x++) {
			for (int y = y1; y <= y2; y++) {
				for (int z = z1; z <= z2; z++) {
					TileEntity tileEntity = world.getTileEntity(x, y, z);
					if (tileEntity != null) {
						NBTTagCompound tenbt = new NBTTagCompound();
						tileEntity.writeToNBT(tenbt);
						tenbt.setInteger("x", tenbt.getInteger("x") - x1);
						tenbt.setInteger("y", tenbt.getInteger("y") - y1);
						tenbt.setInteger("z", tenbt.getInteger("z") - z1);
						tileEntities.appendTag(tenbt);
					}
				}
			}
		}
		return tileEntities;
	}

}
