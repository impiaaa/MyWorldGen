package net.boatcake.MyWorldGen.client;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import net.boatcake.MyWorldGen.MyWorldGen;
import net.boatcake.MyWorldGen.Schematic;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiErrorScreen;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.CompressedStreamTools;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiSaveSchematic extends GuiScreen {
	private GuiButton cancelBtn;
	private GuiTextField fileNameField;
	private GuiButton saveBtn;
	private GuiTextField weightField;
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

		fileNameField = new GuiTextField(this.fontRendererObj,
				this.width / 2 - 150, 20, 300, 20);
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
		buttonList.add(lockRotationButton = new GuiButton(2,
				this.width / 2 + 2, 60, 150, 20, I18n
						.format("gui.lockRotation." + lockRotation)));
		buttonList.add(generateSpawnersButton = new GuiButton(3,
				this.width / 2 - 152, 60, 150, 20, I18n
						.format("gui.generateSpawners." + generateSpawners)));
		buttonList.add(fuzzyMatchingButton = new GuiButton(4,
				this.width / 2 + 2, 84, 150, 20, I18n
						.format("gui.fuzzyMatching." + fuzzyMatching)));
		buttonList.add(terrainSmoothingButton = new GuiButton(5,
				this.width / 2 - 152, 84, 150, 20, I18n
						.format("gui.terrainSmoothing." + terrainSmoothing)));

		weightField = new GuiTextField(this.fontRendererObj,
				this.width / 2 - 152, 108, 150, 20);
		weightField.setMaxStringLength(5);

		chestGenSlot = new GuiSlotChestGenTypes(this.mc, this,
				this.fontRendererObj, this.width / 2 - 152, 132, 150,
				this.height - 158);
		chestGenSlot.registerScrollButtons(6, 7);

		buttonList.add(biomeListTypeButton = new GuiButton(8,
				this.width / 2 + 2, 108, 150, 20,
				I18n.format("gui.biomeListType." + biomeListType.toString())));

		biomeSlot = new GuiSlotBiomes(this.mc, this, this.fontRendererObj,
				this.width / 2 + 2, 128, 150, this.height - 154);
		biomeSlot.registerScrollButtons(9, 10);

		buttonList.add(saveBtn = new GuiButton(0, this.width / 2 + 2,
				this.height - 22, 150, 20, I18n.format("gui.save")));
		buttonList.add(cancelBtn = new GuiButton(1, this.width / 2 - 152,
				this.height - 22, 150, 20, I18n.format("gui.cancel")));

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
		drawCenteredString(fontRendererObj, I18n.format("gui.filename"),
				this.width / 2, 5, 0xFFFFFF);
		drawCenteredString(fontRendererObj,
				I18n.format("selectWorld.resultFolder") + " "
						+ MyWorldGen.globalSchemDir.getAbsolutePath(),
				this.width / 2, 45, 0xA0A0A0);
		fileNameField.drawTextBox();
		weightField.drawTextBox();
		super.drawScreen(par1, par2, par3);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		super.actionPerformed(button);
		if (button.id == saveBtn.id && saveBtn.enabled) {
			// Step 5: Now that we have the block data and entity and tile
			// entity data, saving it to a file should be trivial.
			schematicToSave.info.chestType = chestGenSlot.hooks[chestGenSlot.selected];

			ArrayList<String> biomeNames = new ArrayList<String>(
					biomeSlot.selected.size());
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

			try {
				schematicToSave.info.randomWeight = Integer.valueOf(weightField
						.getText());
			} catch (NumberFormatException e) {

			}

			String name = fileNameField.getText();
			if (!name.contains(".")) {
				name += ".schematic";
			}
			try {
				CompressedStreamTools.writeCompressed(schematicToSave.getNBT(),
						new FileOutputStream(new File(
								MyWorldGen.globalSchemDir, name)));
			} catch (Exception exc) {
				// File does't exist/can't be written
				// TODO: make this nicer?
				mc.displayGuiScreen(new GuiErrorScreen(
						exc.getClass().getName(), exc.getLocalizedMessage()));
				exc.printStackTrace();
				return;
			}
			mc.displayGuiScreen(null);
		} else if (button.id == cancelBtn.id) {
			mc.displayGuiScreen(null);
		} else if (button.id == lockRotationButton.id) {
			if (schematicToSave != null) {
				schematicToSave.info.lockRotation = !schematicToSave.info.lockRotation;
				lockRotationButton.displayString = I18n
						.format("gui.lockRotation."
								+ schematicToSave.info.lockRotation);
			}
		} else if (button.id == generateSpawnersButton.id) {
			if (schematicToSave != null) {
				schematicToSave.info.generateSpawners = !schematicToSave.info.generateSpawners;
				generateSpawnersButton.displayString = I18n
						.format("gui.generateSpawners."
								+ schematicToSave.info.generateSpawners);
			}
		} else if (button.id == fuzzyMatchingButton.id) {
			if (schematicToSave != null) {
				schematicToSave.info.fuzzyMatching = !schematicToSave.info.fuzzyMatching;
				fuzzyMatchingButton.displayString = I18n
						.format("gui.fuzzyMatching."
								+ schematicToSave.info.fuzzyMatching);
			}
		} else if (button.id == terrainSmoothingButton.id) {
			if (schematicToSave != null) {
				schematicToSave.info.terrainSmoothing = !schematicToSave.info.terrainSmoothing;
				terrainSmoothingButton.displayString = I18n
						.format("gui.terrainSmoothing."
								+ schematicToSave.info.terrainSmoothing);
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
			biomeListTypeButton.displayString = I18n
					.format("gui.biomeListType." + biomeListType.toString());
		} else {
			chestGenSlot.actionPerformed(button);
			biomeSlot.actionPerformed(button);
		}
	}

	@Override
	protected void keyTyped(char character, int keycode) {
		if (this.fileNameField.isFocused()) {
			this.fileNameField.textboxKeyTyped(character, keycode);
		} else if (this.weightField.isFocused()) {
			if (Character.isDigit(character) || Character.isISOControl(character)) {
				this.weightField.textboxKeyTyped(character, keycode);
			}
		}
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

	public void updateSaveButton() {
		// Call this every so often to make sure we have a valid file name and a
		// valid schematic
		saveBtn.enabled = fileNameField.getText().trim().length() > 0
				&& schematicToSave != null && schematicToSave.entities != null
				&& schematicToSave.tileEntities != null;
	}

	@Override
	public void updateScreen() {
		if (fileNameField == null || weightField == null) {
			return;
		}
		super.updateScreen();
		fileNameField.updateCursorCounter();
		weightField.updateCursorCounter();
	}

	protected void mouseClicked(int p_73864_1_, int p_73864_2_, int p_73864_3_) {
		super.mouseClicked(p_73864_1_, p_73864_2_, p_73864_3_);
		this.fileNameField.mouseClicked(p_73864_1_, p_73864_2_, p_73864_3_);
		this.weightField.mouseClicked(p_73864_1_, p_73864_2_, p_73864_3_);
	}
}
