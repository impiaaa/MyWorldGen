package net.boatcake.MyWorldGen.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;

import java.io.IOException;

import net.boatcake.MyWorldGen.Schematic;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

public class MessagePlaceSchem implements MWGMessage {
	public ForgeDirection direction;
	public NBTTagCompound schematicTag;
	public int x, y, z;

	@Override
	public void fromBytes(ByteBuf buf) {
		ByteBufInputStream inputStream = new ByteBufInputStream(buf);
		NBTTagCompound packetTag;
		try {
			packetTag = CompressedStreamTools.readCompressed(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		x = packetTag.getInteger("x");
		y = packetTag.getInteger("y");
		z = packetTag.getInteger("z");
		schematicTag = packetTag.getCompoundTag("schematic");
		direction = ForgeDirection.getOrientation(packetTag
				.getInteger("direction"));
	}

	@Override
	public MWGMessage handle(EntityPlayer player) {
		EntityPlayerMP playerMP = (EntityPlayerMP) player;
		// no cheating!
		if (playerMP.capabilities.isCreativeMode) {
			new Schematic(this.schematicTag, null).placeInWorld(
					playerMP.worldObj, this.x, this.y, this.z, this.direction,
					false, false, null);
		}
		return null;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		NBTTagCompound tagToSend = new NBTTagCompound();
		ByteBufOutputStream bos = new ByteBufOutputStream(buf);
		tagToSend.setInteger("x", x);
		tagToSend.setInteger("y", y);
		tagToSend.setInteger("z", z);
		tagToSend.setInteger("direction", direction.ordinal());
		// We might be able to send the file data directly, but it's better to
		// make sure that it's valid NBT first.
		try {
			tagToSend.setTag("schematic", schematicTag);
			CompressedStreamTools.writeCompressed(tagToSend, bos);
		} catch (Exception exc) {
			exc.printStackTrace();
			return;
		}
	}

}
