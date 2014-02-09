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
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.ForgeDirection;

public class PacketHandler implements IPacketHandler {

	@Override
	public void onPacketData(INetworkManager manager,
			Packet250CustomPayload packet, Player player) {
		if (packet.channel.equals("MWGPlaceSchem")) {
			EntityPlayerMP playerMP = (EntityPlayerMP) player;
			// no cheating!
			if (playerMP.capabilities.isCreativeMode) {
				ByteArrayInputStream inputStream = new ByteArrayInputStream(packet.data);
				NBTTagCompound packetTag;
				try {
					packetTag = CompressedStreamTools.readCompressed(inputStream);
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
				Schematic schematic = new Schematic(packetTag.getCompoundTag("schematic"), "remote:"+playerMP.getDisplayName());
				int direction = packetTag.getInteger("direction");
				schematic.placeInWorld(playerMP.worldObj,
						packetTag.getInteger("x"),
						packetTag.getInteger("y"),
						packetTag.getInteger("z"),
						ForgeDirection.getOrientation(direction),
						false, false, null);
			}
		}
		else if (packet.channel.equals("MWGGetSchem")) {
			if (player instanceof EntityPlayerMP) {
				// server
				// Step 3: The server receives the selection box from the client.
				EntityPlayerMP playerMP = (EntityPlayerMP) player;
				// First we need to make sure that they're allowed to see chests
				// etc. I assume being in creative is good enough,
				// I don't want to implement a permissions system right now.
				// (though I guess that could be a TODO)
				if (playerMP.capabilities.isCreativeMode) {
					// get parameters
					ByteArrayInputStream inputStream = new ByteArrayInputStream(packet.data);
					NBTTagCompound packetTag;
					try {
						packetTag = CompressedStreamTools.readCompressed(inputStream);
					} catch (IOException e) {
						e.printStackTrace();
						return;
					}
					int x1 = packetTag.getInteger("x1");
					int y1 = packetTag.getInteger("y1");
					int z1 = packetTag.getInteger("z1");
					int x2 = packetTag.getInteger("x2");
					int y2 = packetTag.getInteger("y2");
					int z2 = packetTag.getInteger("z2");
					
					// Compile a response packet with both the original selection box,
					// as well as the entity and tile entity data. We'll need the
					// selection box in order to compile block data later.
					NBTTagCompound tagToSend = new NBTTagCompound();
					tagToSend.setInteger("x1", x1);
					tagToSend.setInteger("y1", y1);
					tagToSend.setInteger("z1", z1);
					tagToSend.setInteger("x2", x2);
					tagToSend.setInteger("y2", y2);
					tagToSend.setInteger("z2", z2);
					tagToSend.setTag("entities", Schematic.getEntities(playerMP.worldObj, x1, y1, z1, x2, y2, z2));
					tagToSend.setTag("tileEntities", Schematic.getTileEntities(playerMP.worldObj, x1, y1, z1, x2, y2, z2));

					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					try {
						CompressedStreamTools.writeCompressed(tagToSend, bos);
					} catch (IOException exc) {
						exc.printStackTrace();
						return;
					}
					Packet250CustomPayload packetToSend = new Packet250CustomPayload("MWGGetSchem", bos.toByteArray());
					PacketDispatcher.sendPacketToPlayer(packetToSend, player);
				}
			}
			else if (player instanceof EntityClientPlayerMP) {
				// client
				// Step 4: The client has received all of the entity and tile entity data.
				// Now we need to gather the block data from the client copy, and then
				// open a save dialog.
				EntityClientPlayerMP playerMP = (EntityClientPlayerMP) player;
				
				ByteArrayInputStream inputStream = new ByteArrayInputStream(packet.data);
				NBTTagCompound packetTag;
				try {
					packetTag = CompressedStreamTools.readCompressed(inputStream);
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}

				// Open the GUI
				playerMP.openGui(MyWorldGen.instance, 0, playerMP.worldObj, 0, 0, 0);
				
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
					
					guiSchematic.schematicToSave = new Schematic(playerMP.worldObj, x1, y1, z1, x2, y2, z2);
					guiSchematic.schematicToSave.entities = (NBTTagList) packetTag.getTag("entities");
					guiSchematic.schematicToSave.tileEntities = (NBTTagList) packetTag.getTag("tileEntities");
					guiSchematic.updateSaveButton();
				}
				// For step 5, go to GuiSaveSchematic
			}
		}
	}

}
