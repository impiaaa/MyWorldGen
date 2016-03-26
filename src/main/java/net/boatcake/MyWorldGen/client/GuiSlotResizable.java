package net.boatcake.MyWorldGen.client;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.gui.ScaledResolution;

public abstract class GuiSlotResizable extends GuiSlot {
	protected Minecraft minecraft;

	public GuiSlotResizable(Minecraft mc, int x, int y, int width, int height, int slotHeight) {
		super(mc, width, height, y, y + height, slotHeight);
		this.minecraft = mc;
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
	public void drawScreen(int i, int j, float k) {
		// The default GuiSlot expects to take up the whole screen, so it hides
		// out-of-bounds text by drawing the header and footer on top. However,
		// I'd like the view to only cover up its designated area, so I can just
		// use OpenGL scissoring to clip it to bounds.
		ScaledResolution scaledresolution = new ScaledResolution(this.minecraft, this.minecraft.displayWidth,
				this.minecraft.displayHeight);
		int f = scaledresolution.getScaleFactor();
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glScissor(this.left * f, this.minecraft.displayHeight - this.bottom * f, this.width * f, this.height * f);

		// Work-around for "this.width / 2 + this.getListWidth() / 2"
		// shenanigans in GuiSlot. Unfortunately it also messes up the rendering
		// of list items and selection while scrolling, but at least clicking
		// things works now.
		int oldWidth = this.width;
		this.width = this.minecraft.displayWidth / f;

		super.drawScreen(i, j, k);

		this.width = oldWidth;

		GL11.glScissor(0, 0, this.minecraft.displayWidth, this.minecraft.displayHeight);
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
	}

	@Override
	protected int getScrollBarX() {
		return this.right - 6;
	}
}