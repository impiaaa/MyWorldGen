package net.boatcake.MyWorldGen.client;

import java.io.File;
import java.io.FilenameFilter;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.Tessellator;

public class GuiSlotFile extends GuiSlot {
	public File[] files;
	public int selected;
	public GuiScreen parent;
	public FontRenderer fr;

	public GuiSlotFile(Minecraft mc, GuiScreen parent, File file,
			FontRenderer fr, FilenameFilter filter) {
		super(mc, parent.width, parent.height, 32, parent.height - 65 + 4, 18);
		files = file.listFiles(filter);
		selected = 0;
		this.parent = parent;
		this.fr = fr;
	}

	@Override
	protected int getSize() {
		return files.length;
	}

	@Override
	protected void elementClicked(int i, boolean flag, int var3, int var4)  {
		selected = i;
	}

	@Override
	protected boolean isSelected(int i) {
		return i == selected;
	}

	@Override
	protected void drawBackground() {
	}

	@Override
	protected void drawSlot(int i, int j, int k, int l, Tessellator tessellator, int var6, int var7) {
		parent.drawString(fr, files[i].getName(), j + 2, k + 1,
				0xFFFFFF);
	}
	
    protected int getContentHeight()
    {
        return this.getSize() * 18;
    }
}
