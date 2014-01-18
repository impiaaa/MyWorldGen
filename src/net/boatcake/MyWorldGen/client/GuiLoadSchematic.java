package net.boatcake.MyWorldGen.client;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;

import net.boatcake.MyWorldGen.MyWorldGen;
import net.boatcake.MyWorldGen.SchematicFilenameFilter;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiErrorScreen;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSmallButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiLoadSchematic extends GuiScreen {
	private World world;
	private int x, y, z;
	private GuiSlotFile slot;
	private GuiSmallButton doneButton;
	private ForgeDirection direction;
	private EntityClientPlayerMP player;
	
	public GuiLoadSchematic(World world, int x, int y, int z, ForgeDirection direction, EntityClientPlayerMP player) {
		super();
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		this.direction = direction;
		this.player = player;
	}

	public void initGui() {
		buttonList.add(doneButton = new GuiSmallButton(0, this.width / 2 - 75,
				this.height - 38, I18n.getString("gui.done")));
		slot = new GuiSlotFile(this.mc, this, MyWorldGen.globalSchemDir,
				this.fontRenderer, new SchematicFilenameFilter());
		slot.registerScrollButtons(1, 2);
	}

	protected void actionPerformed(GuiButton button) {
		if (button.enabled) {
			if (button.id == doneButton.id) {
				NBTTagCompound tagToSend = new NBTTagCompound();
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				tagToSend.setInteger("x", x);
				tagToSend.setInteger("y", y);
				tagToSend.setInteger("z", z);
				tagToSend.setInteger("direction", direction.ordinal());
				// We might be able to send the file data directly, but it's better to make sure that it's valid NBT first.
				try {
					tagToSend.setCompoundTag("schematic", CompressedStreamTools.readCompressed(new FileInputStream(slot.files[slot.selected])));
					CompressedStreamTools.writeCompressed(tagToSend, bos);
				} catch (Exception exc) {
					mc.displayGuiScreen(new GuiErrorScreen(
							exc.getClass().getName(), exc.getLocalizedMessage()));
					exc.printStackTrace();
					return;
				}
				Packet250CustomPayload packet = new Packet250CustomPayload();
				packet.channel = "MWGPlaceSchem";
				packet.data = bos.toByteArray();
				packet.length = bos.size();
				player.sendQueue.addToSendQueue(packet);
				mc.displayGuiScreen(null);
			} else {
				slot.actionPerformed(button);
			}
		}
	}
	
	public void drawScreen(int par1, int par2, float par3) {
		drawDefaultBackground();
		slot.drawScreen(par1, par2, par3);
		super.drawScreen(par1, par2, par3);
	}
}
