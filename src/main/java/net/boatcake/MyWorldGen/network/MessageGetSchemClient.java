package net.boatcake.MyWorldGen.network;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.boatcake.MyWorldGen.MyWorldGen;
import net.boatcake.MyWorldGen.Schematic;
import net.boatcake.MyWorldGen.client.GuiSaveSchematic;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageGetSchemClient implements IMessage, IMessageHandler<MessageGetSchemClient, IMessage> {
	public NBTTagList entitiesTag;
	public NBTTagList tileEntitiesTag;
	public BlockPos pos1;
	public BlockPos pos2;

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
		pos1 = new BlockPos(packetTag.getInteger("x1"), packetTag.getInteger("y1"), packetTag.getInteger("z1"));
		pos2 = new BlockPos(packetTag.getInteger("x2"), packetTag.getInteger("y2"), packetTag.getInteger("z2"));
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
		EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;

		// Open the GUI
		player.openGui(MyWorldGen.instance, 0, player.worldObj, 0, 0, 0);

		// Give the GUI the entity & tile entity information
		GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
		if (currentScreen instanceof GuiSaveSchematic) {
			GuiSaveSchematic guiSchematic = (GuiSaveSchematic) currentScreen;

			guiSchematic.schematicToSave = new Schematic(player.worldObj, message.pos1, message.pos2);
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
		tagToSend.setInteger("x1", pos1.getX());
		tagToSend.setInteger("y1", pos1.getY());
		tagToSend.setInteger("z1", pos1.getZ());
		tagToSend.setInteger("x2", pos2.getX());
		tagToSend.setInteger("y2", pos2.getY());
		tagToSend.setInteger("z2", pos2.getZ());
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
