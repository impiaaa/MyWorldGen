package net.boatcake.MyWorldGen.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.gui.ScaledResolution;

import org.lwjgl.opengl.GL11;

public abstract class GuiSlotResizable extends GuiSlot {

	public GuiSlotResizable(Minecraft mc, int x, int y, int width, int height,
			int slotHeight) {
		super(mc, width, height, y, y + height, slotHeight);
		this.left = x;
		this.right = x + width;
		this.setHasListHeader(false, 0);
	}

	@Override
	protected void drawBackground() {
	}

	@Override
	public int getListWidth() {
		return this.width - 12;
	}

	@Override
	protected void overlayBackground(int i, int j, int k, int l) {
	}

	@Override
	public void drawScreen(int i, int j, float k) {
		// The default GuiSlot expects to take up the whole screen, so it hides
		// out-of-bounds text by drawing the header and footer on top. However,
		// I'd like the view to only cover up its designated area, so I can just
		// use OpenGL scissoring to clip it to bounds.
		ScaledResolution scaledresolution = new ScaledResolution(this.mc,
				this.mc.displayWidth, this.mc.displayHeight);
		int f = scaledresolution.getScaleFactor();
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glScissor(this.left * f, this.mc.displayHeight - this.bottom * f,
				this.width * f, this.height * f);
		super.drawScreen(i, j, k);
		GL11.glScissor(0, 0, this.mc.displayWidth, this.mc.displayHeight);
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
	}

	@Override
	protected int getScrollBarX() {
		return this.right - 6;
	}

	@Override
	public void handleMouseInput() {
		// Work-around for "this.width / 2 + this.getListWidth() / 2"
		// shenanigans in GuiSlot
		int oldX = this.mouseX;
		int oldRight = this.right;
		int oldLeft = this.left;

		this.mouseX -= this.left;
		this.right -= this.left;
		this.left = 0;

		super.handleMouseInput();

		this.mouseX = oldX;
		this.right = oldRight;
		this.left = oldLeft;
	}
}