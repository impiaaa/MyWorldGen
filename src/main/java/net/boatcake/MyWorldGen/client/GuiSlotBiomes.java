package net.boatcake.MyWorldGen.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenMutated;

public class GuiSlotBiomes extends GuiSlotResizable {
	public FontRenderer fr;
	public Set<Integer> selected;
	public ArrayList<String> biomeNames;
	public GuiScreen parent;

	public GuiSlotBiomes(Minecraft mc, GuiScreen parent, FontRenderer fr,
			int x, int y, int width, int height) {
		super(mc, x, y, width, height, 18);
		selected = new HashSet<Integer>(2);
		this.fr = fr;
		this.parent = parent;

		BiomeGenBase[] biomes = BiomeGenBase.getBiomeGenArray();

		biomeNames = new ArrayList<String>(biomes.length);
		int i = 0;
		for (BiomeGenBase b : biomes) {
			if (b != null && !(b instanceof BiomeGenMutated)) {
				biomeNames.add(b.biomeName);
				i++;
			}
		}

		biomeNames.sort(null);
		selected.add(biomeNames.indexOf(BiomeGenBase.hell.biomeName));
		selected.add(biomeNames.indexOf(BiomeGenBase.sky.biomeName));
	}

	@Override
	protected int getSize() {
		return biomeNames.size();
	}

	@Override
	protected void elementClicked(int slotIndex, boolean isDoubleClick,
			int mouseX, int mouseY) {
		if (selected.contains(slotIndex)) {
			selected.remove(slotIndex);
		} else {
			selected.add(slotIndex);
		}
	}

	@Override
	protected boolean isSelected(int slotIndex) {
		return selected.contains(slotIndex);
	}

	@Override
	protected void drawSlot(int i, int j, int k, int l, int var6, int var7) {
		parent.drawString(fr, biomeNames.get(i), j + 2, k + 1, 0xFFFFFF);
	}
}
