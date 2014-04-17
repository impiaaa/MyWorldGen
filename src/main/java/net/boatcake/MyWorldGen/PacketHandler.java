package net.boatcake.MyWorldGen;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import net.boatcake.MyWorldGen.client.GuiSaveSchematic;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class PacketHandler implements IPacketHandler {

	@Override
	public void onPacketData(INetworkManager manager,
			Packet250CustomPayload packet, Player player) {
		if (packet.channel.equals("MWGPlaceSchem")) {
			EntityPlayerMP playerMP = (EntityPlayerMP) player;
			// no cheating!
			if (playerMP.capabilities.isCreativeMode) {
				ByteArrayInputStream inputStream = new ByteArrayInputStream(
						packet.data);
				NBTTagCompound packetTag;
				try {
					packetTag = CompressedStreamTools
							.readCompressed(inputStream);
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
				Schematic schematic = new Schematic(
						packetTag.getCompoundTag("schematic"), "remote:"
								+ playerMP.username);
				int direction = packetTag.getInteger("direction");
				schematic.placeInWorld(playerMP.worldObj,
						packetTag.getInteger("x"), packetTag.getInteger("y"),
						packetTag.getInteger("z"),
						ForgeDirection.getOrientation(direction), false, false,
						null);
			}
		} else if (packet.channel.equals("MWGGetSchem")) {
			if (player instanceof EntityPlayerMP) {
				// server
			} else if (player instanceof EntityClientPlayerMP) {
				// client
				/*
				 * Step 4: The client has received all of the entity and tile
				 * entity data. Now we need to gather the block data from the
				 * client copy, and then open a save dialog.
				 */
				EntityClientPlayerMP playerMP = (EntityClientPlayerMP) player;

				ByteArrayInputStream inputStream = new ByteArrayInputStream(
						packet.data);
				NBTTagCompound packetTag;
				try {
					packetTag = CompressedStreamTools
							.readCompressed(inputStream);
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}

				// Open the GUI
				playerMP.openGui(MyWorldGen.instance, 0, playerMP.worldObj, 0,
						0, 0);

				// Give the GUI the entity & tile entity information
				GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
				if (currentScreen instanceof GuiSaveSchematic) {
					GuiSaveSchematic guiSchematic = (GuiSaveSchematic) currentScreen;

					int x1 = packetTag.getInteger("x1");
					int y1 = packetTag.getInteger("y1");
					int z1 = packetTag.getInteger("z1");
					int x2 = packetTag.getInteger("x2");
					int y2 = packetTag.getInteger("y2");
					int z2 = packetTag.getInteger("z2");

					guiSchematic.schematicToSave = new Schematic(
							playerMP.worldObj, x1, y1, z1, x2, y2, z2);
					guiSchematic.schematicToSave.entities = packetTag
							.getTagList("entities");
					guiSchematic.schematicToSave.tileEntities = packetTag
							.getTagList("tileEntities");
					guiSchematic.updateSaveButton();
				}
				// For step 5, go to GuiSaveSchematic
			}
		}
	}
}
