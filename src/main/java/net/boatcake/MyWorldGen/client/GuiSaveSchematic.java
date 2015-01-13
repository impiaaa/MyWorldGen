package net.boatcake.MyWorldGen.client;

import java.io.File;
import java.io.FileOutputStream;

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
	public Schematic schematicToSave;

	private GuiButton lockRotationButton;
	private GuiButton generateSpawnersButton;
	private GuiButton fuzzyMatchingButton;
	private GuiButton terrainSmoothingButton;

	public GuiSaveSchematic() {
		super();
		// The schematicToSave is filled out for us in PacketHandler
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		super.actionPerformed(button);
		if (button.id == saveBtn.id && saveBtn.enabled) {
			// Step 5: Now that we have the block data and entity and tile
			// entity data, saving it to a file should be trivial.
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
		}
	}

	@Override
	public void drawScreen(int par1, int par2, float par3) {
		if (fileNameField == null) {
			// Sometimes, initGui will not have been called yet. I think it's a
			// race condition on my platform that I can't easily fix right now,
			// but this works anyway.
			return;
		}
		drawDefaultBackground();
		drawCenteredString(fontRendererObj, I18n.format("gui.filename"),
				this.width / 2, 20, 0xFFFFFF);
		drawCenteredString(fontRendererObj,
				I18n.format("selectWorld.resultFolder") + " "
						+ MyWorldGen.globalSchemDir.getAbsolutePath(),
				this.width / 2, 97, 0xA0A0A0);
		fileNameField.drawTextBox();
		super.drawScreen(par1, par2, par3);
	}

	@Override
	public void initGui() {
		super.initGui();
		Keyboard.enableRepeatEvents(true);
		this.buttonList.clear();
		buttonList.add(saveBtn = new GuiButton(0, this.width / 2 + 4,
				this.height - 52, 150, 20, I18n.format("gui.save")));
		buttonList.add(cancelBtn = new GuiButton(1, this.width / 2 - 154,
				this.height - 52, 150, 20, I18n.format("gui.cancel")));

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
				this.width / 2 + 4, this.height / 4 + 72, 150, 20, I18n
						.format("gui.lockRotation." + lockRotation)));
		buttonList.add(generateSpawnersButton = new GuiButton(3,
				this.width / 2 - 154, this.height / 4 + 72, 150, 20, I18n
						.format("gui.generateSpawners." + generateSpawners)));
		buttonList.add(fuzzyMatchingButton = new GuiButton(4,
				this.width / 2 + 4, this.height / 4 + 96, 150, 20, I18n
						.format("gui.fuzzyMatching." + fuzzyMatching)));
		buttonList.add(terrainSmoothingButton = new GuiButton(5,
				this.width / 2 - 154, this.height / 4 + 96, 150, 20, I18n
						.format("gui.terrainSmoothing." + terrainSmoothing)));

		fileNameField = new GuiTextField(this.fontRendererObj,
				this.width / 2 - 150, 60, 300, 20);
		fileNameField.setMaxStringLength(32767);
		fileNameField.setFocused(true);
		updateSaveButton();
	}

	@Override
	protected void keyTyped(char character, int keycode) {
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

	public void updateSaveButton() {
		// Call this every so often to make sure we have a valid file name and a
		// valid schematic
		saveBtn.enabled = fileNameField.getText().trim().length() > 0
				&& schematicToSave != null && schematicToSave.entities != null
				&& schematicToSave.tileEntities != null;
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		fileNameField.updateCursorCounter();
	}
}
