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

public class MessageGetSchemServer implements MWGMessage {
	public int x1, y1, z1;
	public int x2, y2, z2;

	@Override
	public void fromBytes(ByteBuf buf) {
		// get parameters
		ByteBufInputStream inputStream = new ByteBufInputStream(buf);
		NBTTagCompound packetTag;
		try {
			packetTag = CompressedStreamTools.readCompressed(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		x1 = packetTag.getInteger("x1");
		y1 = packetTag.getInteger("y1");
		z1 = packetTag.getInteger("z1");
		x2 = packetTag.getInteger("x2");
		y2 = packetTag.getInteger("y2");
		z2 = packetTag.getInteger("z2");
	}

	@Override
	public MWGMessage handle(EntityPlayer player) {
		// server
		// Step 3: The server receives the selection box from the client.
		/*
		 * First we need to make sure that they're allowed to see chests etc. I
		 * assume being in creative is good enough, I don't want to implement a
		 * permissions system right now. (though I guess that could be a TODO)
		 */
		EntityPlayerMP playerMP = (EntityPlayerMP) player;
		if (playerMP.capabilities.isCreativeMode) {
			/*
			 * Compile a response packet with both the original selection box,
			 * as well as the entity and tile entity data. We'll need the
			 * selection box in order to compile block data later.
			 */
			MessageGetSchemClient response = new MessageGetSchemClient();
			response.entitiesTag = Schematic.getEntities(playerMP.worldObj,
					this.x1, this.y1, this.z1, this.x2, this.y2, this.z2);
			response.tileEntitiesTag = Schematic.getTileEntities(
					playerMP.worldObj, this.x1, this.y1, this.z1, this.x2,
					this.y2, this.z2);
			return response;
		}
		return null;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		// Compile the packet with the selection box coordinates
		NBTTagCompound tagToSend = new NBTTagCompound();
		tagToSend.setInteger("x1", x1);
		tagToSend.setInteger("y1", y1);
		tagToSend.setInteger("z1", z1);
		tagToSend.setInteger("x2", x2);
		tagToSend.setInteger("y2", y2);
		tagToSend.setInteger("z2", z2);

		ByteBufOutputStream bos = new ByteBufOutputStream(buf);
		try {
			CompressedStreamTools.writeCompressed(tagToSend, bos);
		} catch (IOException ex) {
			ex.printStackTrace();
			return;
		}
	}

}
