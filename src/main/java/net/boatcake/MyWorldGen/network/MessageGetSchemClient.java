package net.boatcake.MyWorldGen.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;

import java.io.IOException;

import net.boatcake.MyWorldGen.MyWorldGen;
import net.boatcake.MyWorldGen.Schematic;
import net.boatcake.MyWorldGen.client.GuiSaveSchematic;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class MessageGetSchemClient implements IMessage, IMessageHandler<MessageGetSchemClient, IMessage> {
	public NBTTagList entitiesTag;
	public NBTTagList tileEntitiesTag;
	public int x1, y1, z1;
	public int x2, y2, z2;

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
		x1 = packetTag.getInteger("x1");
		y1 = packetTag.getInteger("y1");
		z1 = packetTag.getInteger("z1");
		x2 = packetTag.getInteger("x2");
		y2 = packetTag.getInteger("y2");
		z2 = packetTag.getInteger("z2");
		entitiesTag = packetTag.getTagList("entities", 10);
		tileEntitiesTag = packetTag.getTagList("tileEntities", 10);
	}

	@Override
	public IMessage onMessage(MessageGetSchemClient message, MessageContext ctx) {
		// client
		/*
		 * Step 4: The client has received all of the entity and tile entity
		 * data. Now we need to gather the block data from the client copy, and
		 * then open a save dialog.
		 */
		EntityClientPlayerMP playerMP = Minecraft.getMinecraft().thePlayer;

		// Open the GUI
		playerMP.openGui(MyWorldGen.instance, 0, playerMP.worldObj, 0, 0, 0);

		// Give the GUI the entity & tile entity information
		GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
		if (currentScreen instanceof GuiSaveSchematic) {
			GuiSaveSchematic guiSchematic = (GuiSaveSchematic) currentScreen;

			guiSchematic.schematicToSave = new Schematic(playerMP.worldObj,
					message.x1, message.y1, message.z1, message.x2, message.y2, message.z2);
			guiSchematic.schematicToSave.entities = message.entitiesTag;
			guiSchematic.schematicToSave.tileEntities = message.tileEntitiesTag;
			guiSchematic.updateSaveButton();
		}
		// For step 5, go to GuiSaveSchematic
		return null;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		NBTTagCompound tagToSend = new NBTTagCompound();
		tagToSend.setInteger("x1", x1);
		tagToSend.setInteger("y1", y1);
		tagToSend.setInteger("z1", z1);
		tagToSend.setInteger("x2", x2);
		tagToSend.setInteger("y2", y2);
		tagToSend.setInteger("z2", z2);
		tagToSend.setTag("entities", entitiesTag);
		tagToSend.setTag("tileEntities", tileEntitiesTag);

		ByteBufOutputStream bos = new ByteBufOutputStream(buf);
		try {
			CompressedStreamTools.writeCompressed(tagToSend, bos);
		} catch (IOException exc) {
			exc.printStackTrace();
			return;
		}
	}
}
