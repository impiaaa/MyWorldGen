package net.boatcake.MyWorldGen.utils;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class WorldUtils {

	public static NBTTagList getEntities(World world, BlockPos pos1,
			BlockPos pos2) {
		assert !world.isRemote;
		BlockPos min = new BlockPos(Math.min(pos1.getX(), pos2.getX()),
				Math.min(pos1.getY(), pos2.getY()), Math.min(pos1.getZ(),
						pos2.getZ()));
		BlockPos max = new BlockPos(Math.max(pos1.getX(), pos2.getX()),
				Math.max(pos1.getY(), pos2.getY()), Math.max(pos1.getZ(),
						pos2.getZ()));
		NBTTagList entities = new NBTTagList();
		for (Object o : world.getEntitiesWithinAABB(Entity.class,
				new AxisAlignedBB(min, max))) {
			NBTTagCompound enbt = new NBTTagCompound();
			((Entity) o).writeToNBTOptional(enbt);
			if (enbt.hasNoTags()) {
				continue;
			}
			NBTTagList posNBT = (NBTTagList) enbt.getTag("Pos");
			posNBT.set(0, new NBTTagDouble(posNBT.getDoubleAt(0) - min.getX()));
			posNBT.set(1, new NBTTagDouble(posNBT.getDoubleAt(1) - min.getY()));
			posNBT.set(2, new NBTTagDouble(posNBT.getDoubleAt(2) - min.getZ()));
			entities.appendTag(enbt);
		}
		return entities;
	}

	public static NBTTagList getTileEntities(World world, BlockPos pos1,
			BlockPos pos2) {
		assert !world.isRemote;
		BlockPos min = new BlockPos(Math.min(pos1.getX(), pos2.getX()),
				Math.min(pos1.getY(), pos2.getY()), Math.min(pos1.getZ(),
						pos2.getZ()));
		BlockPos max = new BlockPos(Math.max(pos1.getX(), pos2.getX()),
				Math.max(pos1.getY(), pos2.getY()), Math.max(pos1.getZ(),
						pos2.getZ()));
		NBTTagList tileEntities = new NBTTagList();
		for (Object o : BlockPos.getAllInBox(min, max)) {
			BlockPos pos = (BlockPos) o;
			TileEntity tileEntity = world.getTileEntity(pos);
			if (tileEntity != null) {
				NBTTagCompound tenbt = new NBTTagCompound();
				tileEntity.writeToNBT(tenbt);
				tenbt.setInteger("x", tenbt.getInteger("x") - min.getX());
				tenbt.setInteger("y", tenbt.getInteger("y") - min.getY());
				tenbt.setInteger("z", tenbt.getInteger("z") - min.getZ());
				tileEntities.appendTag(tenbt);
			}
		}
		return tileEntities;
	}

}
