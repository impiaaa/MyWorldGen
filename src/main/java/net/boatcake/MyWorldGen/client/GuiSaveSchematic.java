package net.boatcake.MyWorldGen.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.lwjgl.input.Keyboard;

import net.boatcake.MyWorldGen.MyWorldGen;
import net.boatcake.MyWorldGen.Schematic;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiErrorScreen;
import net.minecraft.client.gui.GuiPageButtonList;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlider;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiSaveSchematic extends GuiScreen implements GuiSlider.FormatHelper, GuiPageButtonList.GuiResponder {
	private GuiButton cancelBtn;
	private GuiTextField fileNameField;
	private GuiButton saveBtn;
	private GuiSlotChestGenTypes chestGenSlot;

	private enum BiomeListType {
		ONLYINCLUDE, EXCLUDE
	};

	private BiomeListType biomeListType;
	private GuiButton biomeListTypeButton;
	private GuiSlotBiomes biomeSlot;

	private GuiButton lockRotationButton;
	private GuiButton generateSpawnersButton;
	private GuiButton fuzzyMatchingButton;
	private GuiButton terrainSmoothingButton;

	public Schematic schematicToSave;

	public GuiSaveSchematic() {
		super();
		biomeListType = BiomeListType.EXCLUDE;
		// The schematicToSave is filled out for us in PacketHandler
	}

	@Override
	public void initGui() {
		super.initGui();
		Keyboard.enableRepeatEvents(true);
		this.buttonList.clear();

		fileNameField = new GuiTextField(0, this.fontRendererObj, this.width / 2 - 150, 20, 300, 20);
		fileNameField.setMaxStringLength(32767);
		fileNameField.setFocused(true);

		boolean lockRotation, generateSpawners, fuzzyMatching, terrainSmoothing;
		if (schematicToSave == null) {
			lockRotation = false;
			generateSpawners = true;
			fuzzyMatching = false;
			terrainSmoothing = false;
		} else {
			lockRotation = schematicToSave.info.lockRotation;
			generateSpawners = schematicToSave.info.generateSpawners;
			fuzzyMatching = schematicToSave.info.fuzzyMatching;
			terrainSmoothing = schematicToSave.info.terrainSmoothing;
		}
		buttonList.add(lockRotationButton = new GuiButton(2, this.width / 2 + 2, 60, 150, 20,
				I18n.format("gui.lockRotation." + lockRotation)));
		buttonList.add(generateSpawnersButton = new GuiButton(3, this.width / 2 - 152, 60, 150, 20,
				I18n.format("gui.generateSpawners." + generateSpawners)));
		buttonList.add(fuzzyMatchingButton = new GuiButton(4, this.width / 2 + 2, 84, 150, 20,
				I18n.format("gui.fuzzyMatching." + fuzzyMatching)));
		buttonList.add(terrainSmoothingButton = new GuiButton(5, this.width / 2 - 152, 84, 150, 20,
				I18n.format("gui.terrainSmoothing." + terrainSmoothing)));

		buttonList.add(new GuiSlider(this, 11, this.width / 2 - 152, 108, I18n.format("gui.randomWeight"), 1.0f, 100.0f,
				10.0f, this));

		chestGenSlot = new GuiSlotChestGenTypes(this.mc, this, this.fontRendererObj, this.width / 2 - 152, 132, 150,
				this.height - 158);
		chestGenSlot.registerScrollButtons(6, 7);

		buttonList.add(biomeListTypeButton = new GuiButton(8, this.width / 2 + 2, 108, 150, 20,
				I18n.format("gui.biomeListType." + biomeListType.toString())));

		biomeSlot = new GuiSlotBiomes(this.mc, this, this.fontRendererObj, this.width / 2 + 2, 128, 150,
				this.height - 154);
		biomeSlot.registerScrollButtons(9, 10);

		buttonList.add(
				saveBtn = new GuiButton(0, this.width / 2 + 2, this.height - 22, 150, 20, I18n.format("gui.save")));
		buttonList.add(cancelBtn = new GuiButton(1, this.width / 2 - 152, this.height - 22, 150, 20,
				I18n.format("gui.cancel")));

		updateSaveButton();
	}

	@Override
	public void drawScreen(int par1, int par2, float par3) {
		if (biomeSlot == null) {
			// Sometimes, initGui will not have been called yet. I think it's a
			// race condition on my platform that I can't easily fix right now,
			// but this works anyway.
			return;
		}
		drawDefaultBackground();
		chestGenSlot.drawScreen(par1, par2, par3);
		biomeSlot.drawScreen(par1, par2, par3);
		drawCenteredString(fontRendererObj, I18n.format("gui.filename"), this.width / 2, 5, 0xFFFFFF);
		drawCenteredString(fontRendererObj,
				I18n.format("selectWorld.resultFolder") + " " + MyWorldGen.globalSchemDir.getAbsolutePath(),
				this.width / 2, 45, 0xA0A0A0);
		fileNameField.drawTextBox();
		super.drawScreen(par1, par2, par3);
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);
		if (button.id == saveBtn.id && saveBtn.enabled) {
			// Step 5: Now that we have the block data and entity and tile
			// entity data, saving it to a file should be trivial.
			schematicToSave.info.chestType = chestGenSlot.hooks[chestGenSlot.selected];

			ArrayList<String> biomeNames = new ArrayList<String>(biomeSlot.selected.size());
			for (int i : biomeSlot.selected) {
				biomeNames.add(biomeSlot.biomeNames.get(i));
			}

			switch (biomeListType) {
			case EXCLUDE:
				schematicToSave.info.excludeBiomes = biomeNames;
				break;
			case ONLYINCLUDE:
				schematicToSave.info.onlyIncludeBiomes = biomeNames;
				break;
			default:
				break;
			}

			String name = fileNameField.getText();
			if (!name.contains(".")) {
				name += ".schematic";
			}
			try {
				CompressedStreamTools.writeCompressed(schematicToSave.getNBT(),
						new FileOutputStream(new File(MyWorldGen.globalSchemDir, name)));
			} catch (Exception exc) {
				// File does't exist/can't be written
				// TODO: make this nicer?
				mc.displayGuiScreen(new GuiErrorScreen(exc.getClass().getName(), exc.getLocalizedMessage()));
				exc.printStackTrace();
				return;
			}
			mc.displayGuiScreen(null);
		} else if (button.id == cancelBtn.id) {
			mc.displayGuiScreen(null);
		} else if (button.id == lockRotationButton.id) {
			if (schematicToSave != null) {
				schematicToSave.info.lockRotation = !schematicToSave.info.lockRotation;
				lockRotationButton.displayString = I18n.format("gui.lockRotation." + schematicToSave.info.lockRotation);
			}
		} else if (button.id == generateSpawnersButton.id) {
			if (schematicToSave != null) {
				schematicToSave.info.generateSpawners = !schematicToSave.info.generateSpawners;
				generateSpawnersButton.displayString = I18n
						.format("gui.generateSpawners." + schematicToSave.info.generateSpawners);
			}
		} else if (button.id == fuzzyMatchingButton.id) {
			if (schematicToSave != null) {
				schematicToSave.info.fuzzyMatching = !schematicToSave.info.fuzzyMatching;
				fuzzyMatchingButton.displayString = I18n
						.format("gui.fuzzyMatching." + schematicToSave.info.fuzzyMatching);
			}
		} else if (button.id == terrainSmoothingButton.id) {
			if (schematicToSave != null) {
				schematicToSave.info.terrainSmoothing = !schematicToSave.info.terrainSmoothing;
				terrainSmoothingButton.displayString = I18n
						.format("gui.terrainSmoothing." + schematicToSave.info.terrainSmoothing);
			}
		} else if (button.id == biomeListTypeButton.id) {
			switch (biomeListType) {
			case EXCLUDE:
				biomeListType = BiomeListType.ONLYINCLUDE;
				break;
			case ONLYINCLUDE:
				biomeListType = BiomeListType.EXCLUDE;
				break;
			default:
				return;
			}
			biomeListTypeButton.displayString = I18n.format("gui.biomeListType." + biomeListType.toString());
		} else {
			chestGenSlot.actionPerformed(button);
			biomeSlot.actionPerformed(button);
		}
	}

	@Override
	protected void keyTyped(char character, int keycode) throws IOException {
		fileNameField.textboxKeyTyped(character, keycode);
		updateSaveButton();

		switch (keycode) {
		case Keyboard.KEY_RETURN:
		case Keyboard.KEY_NUMPADENTER:
			this.actionPerformed(saveBtn);
			break;
		case Keyboard.KEY_ESCAPE:
			this.actionPerformed(cancelBtn);
			break;
		default:
			break;
		}
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	public void handleMouseInput() throws IOException {
		if (biomeSlot == null) {
			return;
		}
		super.handleMouseInput();
		chestGenSlot.handleMouseInput();
		biomeSlot.handleMouseInput();
	}

	public void updateSaveButton() {
		// Call this every so often to make sure we have a valid file name and a
		// valid schematic
		saveBtn.enabled = fileNameField.getText().trim().length() > 0 && schematicToSave != null
				&& schematicToSave.entities != null && schematicToSave.tileEntities != null;
	}

	@Override
	public void updateScreen() {
		if (fileNameField == null) {
			return;
		}
		super.updateScreen();
		fileNameField.updateCursorCounter();
	}

	@Override
	public String getText(int p_175318_1_, String p_175318_2_, float p_175318_3_) {
		return p_175318_2_ + ": " + String.format("%.0f", p_175318_3_);
	}

	@Override
	public void func_175321_a(int id, boolean p_175321_2_) {
	}

	@Override
	public void onTick(int id, float val) {
		schematicToSave.info.randomWeight = (int) val;
	}

	@Override
	public void func_175319_a(int id, String p_175319_2_) {
	}

}
