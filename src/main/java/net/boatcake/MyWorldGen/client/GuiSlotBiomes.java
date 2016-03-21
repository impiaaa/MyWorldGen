package net.boatcake.MyWorldGen.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenEnd;
import net.minecraft.world.biome.BiomeGenForestMutated;
import net.minecraft.world.biome.BiomeGenHell;
import net.minecraft.world.biome.BiomeGenSavannaMutated;

public class GuiSlotBiomes extends GuiSlotResizable {
	public FontRenderer fr;
	public Set<Integer> selected;
	public ArrayList<String> biomeNames;
	public GuiScreen parent;

	public GuiSlotBiomes(Minecraft mc, GuiScreen parent, FontRenderer fr, int x, int y, int width, int height) {
		super(mc, x, y, width, height, 18);
		selected = new HashSet<Integer>(2);
		this.fr = fr;
		this.parent = parent;

		Set<ResourceLocation> biomes = BiomeGenBase.biomeRegistry.getKeys();

		biomeNames = new ArrayList<String>(biomes.size());
		ArrayList<String> otherWorldBiomes = new ArrayList<String>(biomes.size());
		for (ResourceLocation res : biomes) {
			BiomeGenBase b = BiomeGenBase.biomeRegistry.getObject(res);
			if (b != null && !(b instanceof BiomeGenSavannaMutated) && !(b instanceof BiomeGenForestMutated)) {
				biomeNames.add(b.getBiomeName());
				if (b instanceof BiomeGenHell || b instanceof BiomeGenEnd) {
					otherWorldBiomes.add(b.getBiomeName());
				}
			}
		}

		biomeNames.sort(null);
		// By default, do not generate in the Nether or End
		for (String biomeName : otherWorldBiomes) {
			selected.add(biomeNames.indexOf(biomeName));
		}
	}

	@Override
	protected int getSize() {
		return biomeNames.size();
	}

	@Override
	protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
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
