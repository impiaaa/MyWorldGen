package net.boatcake.MyWorldGen.client;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootTableList;

public class GuiSlotChestGenTypes extends GuiSlotResizable {
	public FontRenderer fr;
	public int selected;
	public ResourceLocation[] tables;
	public String[] tablesTranslated;
	public GuiScreen parent;

	public GuiSlotChestGenTypes(Minecraft mc, GuiScreen parent, FontRenderer fr, int x, int y, int width, int height) {
		super(mc, x, y, width, height, 18);
		selected = 0;
		this.fr = fr;
		this.parent = parent;

		Set<ResourceLocation> allTables = LootTableList.getAll();
		tables = ArrayUtils.add(Arrays.copyOf(allTables.toArray(), allTables.size(), ResourceLocation[].class), null);

		Arrays.sort(tables, new Comparator<ResourceLocation>() {
			@Override
			public int compare(ResourceLocation x, ResourceLocation y) {
				return (x == null ? "" : x.toString()).compareTo(y == null ? "" : y.toString());
			}
		});

		tablesTranslated = new String[tables.length];
		for (int i = 0; i < tables.length; i++) {
			if (tables[i] == null) {
				tablesTranslated[i] = I18n.format("chestGenType.none.name");
			} else {
				tablesTranslated[i] = tables[i].toString();
			}
			if (tables[i] != null && tables[i] == LootTableList.CHESTS_SIMPLE_DUNGEON) {
				selected = i;
			}
		}
	}

	@Override
	protected int getSize() {
		return tables.length;
	}

	@Override
	protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
		selected = slotIndex;
	}

	@Override
	protected boolean isSelected(int slotIndex) {
		return slotIndex == selected;
	}

	@Override
	protected void drawSlot(int i, int j, int k, int l, int var6, int var7) {
		parent.drawString(fr, tablesTranslated[i], j + 2, k + 1, 0xFFFFFF);
	}
}
