package net.boatcake.MyWorldGen.client;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.ChestGenHooks;

import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.opengl.GL11;

public class GuiSlotChestGenTypes extends GuiSlot {
	public FontRenderer fr;
	public int selected;
	public String[] hooks;
	public String[] hooksTranslated;
	public GuiScreen parent;

	public GuiSlotChestGenTypes(Minecraft mc, GuiScreen parent,
			FontRenderer fr, int x, int y, int width, int height) {
		super(mc, width, height, y, y + height, 18);
		selected = 0;
		this.fr = fr;
		this.left = x;
		this.right = x + width;
		this.parent = parent;
		this.setHasListHeader(false, 0);

		try {
			Field chestInfoField = ChestGenHooks.class
					.getDeclaredField("chestInfo");
			chestInfoField.setAccessible(true);
			HashMap<String, ChestGenHooks> chestInfo = (HashMap<String, ChestGenHooks>) chestInfoField
					.get(null);
			Set<String> keySet = chestInfo.keySet();
			hooks = ArrayUtils.add(Arrays.copyOf(keySet.toArray(),
					keySet.size(), String[].class), "");
		} catch (ReflectiveOperationException e) {
			hooks = new String[] { "" };
			e.printStackTrace();
		}

		hooksTranslated = new String[hooks.length];
		for (int i = 0; i < hooks.length; i++) {
			if (hooks[i].equals("")) {
				hooksTranslated[i] = I18n.format("chestGenType.none.name");
			} else {
				hooksTranslated[i] = I18n.format("chestGenType." + hooks[i]
						+ ".name");
			}
			if (hooks[i].equals(ChestGenHooks.DUNGEON_CHEST)) {
				selected = i;
			}
		}
	}

	@Override
	protected int getSize() {
		return hooks.length;
	}

	@Override
	protected void elementClicked(int slotIndex, boolean isDoubleClick,
			int mouseX, int mouseY) {
		selected = slotIndex;
	}

	@Override
	protected boolean isSelected(int slotIndex) {
		return slotIndex == selected;
	}

	@Override
	protected void drawBackground() {
	}

	@Override
	protected void drawSlot(int i, int j, int k, int l, int var6, int var7) {
		parent.drawString(fr, hooksTranslated[i], j + 2, k + 1, 0xFFFFFF);
	}

	@Override
	public int getListWidth() {
		return this.width;
	}

	@Override
	protected void overlayBackground(int i, int j, int k, int l) {
	}

	@Override
	public void drawScreen(int i, int j, float k) {
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
}
