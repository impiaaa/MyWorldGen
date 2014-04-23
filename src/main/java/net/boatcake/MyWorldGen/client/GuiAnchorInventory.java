package net.boatcake.MyWorldGen.client;

import net.boatcake.MyWorldGen.ContainerAnchorInventory;
import net.boatcake.MyWorldGen.blocks.TileEntityAnchorInventory;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

public class GuiAnchorInventory extends GuiContainer {
	private static final ResourceLocation guiTextures = new ResourceLocation(
			"textures/gui/container/dispenser.png");
	public TileEntityAnchorInventory tileEntity;

	public GuiAnchorInventory(InventoryPlayer inventoryPlayer,
			TileEntityAnchorInventory te) {
		super(new ContainerAnchorInventory(inventoryPlayer, te));
		tileEntity = te;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float par1, int par2,
			int par3) {
		// draw your Gui here, only thing you need to change is the path
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(guiTextures);
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int param1, int param2) {
		// draw text and stuff here
		// the parameters for drawString are: string, x, y, color
		fontRendererObj.drawString(I18n.format(tileEntity.getInventoryName()),
				8, 6, 0x404040);
		// draws "Inventory" or your regional equivalent
		fontRendererObj.drawString(I18n.format("container.inventory"), 8,
				ySize - 96 + 2, 0x404040);
	}
}
