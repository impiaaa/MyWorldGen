package net.boatcake.MyWorldGen.client;

import java.io.FileInputStream;

import net.boatcake.MyWorldGen.MyWorldGen;
import net.boatcake.MyWorldGen.SchematicFilenameFilter;
import net.boatcake.MyWorldGen.network.MessagePlaceSchem;
import net.boatcake.MyWorldGen.utils.NetUtils;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiErrorScreen;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiLoadSchematic extends GuiScreen {
	private ForgeDirection direction;
	private GuiButton doneButton;
	private EntityClientPlayerMP player;
	private GuiSlotFile slot;
	private World world;
	private int x, y, z;

	public GuiLoadSchematic(World world, int x, int y, int z,
			ForgeDirection direction, EntityClientPlayerMP player) {
		super();
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		this.direction = direction;
		this.player = player;
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.enabled) {
			if (button.id == doneButton.id) {
				MessagePlaceSchem message = new MessagePlaceSchem();
				message.x = x;
				message.y = y;
				message.z = z;
				message.direction = direction;
				// We might be able to send the file data directly, but it's
				// better to make sure that it's valid NBT first.
				try {
					message.schematicTag = CompressedStreamTools
							.readCompressed(new FileInputStream(
									slot.files[slot.selected]));
				} catch (Exception exc) {
					this.mc.displayGuiScreen(new GuiErrorScreen(exc.getClass()
							.getName(), exc.getLocalizedMessage()));
					exc.printStackTrace();
					return;
				}
				NetUtils.sendToServer(message);
				this.mc.displayGuiScreen(null);
			} else {
				slot.actionPerformed(button);
			}
		}
	}

	@Override
	public void drawScreen(int par1, int par2, float par3) {
		drawDefaultBackground();
		slot.drawScreen(par1, par2, par3);
		super.drawScreen(par1, par2, par3);
	}

	@Override
	public void initGui() {
		buttonList.add(doneButton = new GuiButton(0, this.width / 2 - 75,
				this.height - 38, I18n.format("gui.done")));
		slot = new GuiSlotFile(this.mc, this, MyWorldGen.globalSchemDir,
				this.fontRendererObj, new SchematicFilenameFilter());
		slot.registerScrollButtons(1, 2);
	}
}
