package net.boatcake.MyWorldGen.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;

import java.io.IOException;

import net.boatcake.MyWorldGen.utils.WorldUtils;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class MessageGetSchemServer implements IMessage,
		IMessageHandler<MessageGetSchemServer, MessageGetSchemClient> {
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
	public MessageGetSchemClient onMessage(MessageGetSchemServer message,
			MessageContext ctx) {
		// server
		// Step 3: The server receives the selection box from the client.
		/*
		 * First we need to make sure that they're allowed to see chests etc. I
		 * assume being in creative is good enough, I don't want to implement a
		 * permissions system right now. (though I guess that could be a TODO)
		 */
		EntityPlayerMP playerMP = ctx.getServerHandler().playerEntity;
		if (playerMP.capabilities.isCreativeMode) {
			/*
			 * Compile a response packet with both the original selection box,
			 * as well as the entity and tile entity data. We'll need the
			 * selection box in order to compile block data later.
			 */
			MessageGetSchemClient response = new MessageGetSchemClient();
			response.entitiesTag = WorldUtils.getEntities(playerMP.worldObj,
					message.x1, message.y1, message.z1, message.x2, message.y2,
					message.z2);
			response.tileEntitiesTag = WorldUtils.getTileEntities(
					playerMP.worldObj, message.x1, message.y1, message.z1,
					message.x2, message.y2, message.z2);
			response.x1 = x1;
			response.y1 = y1;
			response.z1 = z1;
			response.x2 = x2;
			response.y2 = y2;
			response.z2 = z2;
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
