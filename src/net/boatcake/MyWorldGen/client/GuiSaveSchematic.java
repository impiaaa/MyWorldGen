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
	private GuiTextField fileNameField;
	private GuiButton saveBtn;
	private GuiButton cancelBtn;
	public Schematic schematicToSave;

	public GuiSaveSchematic() {
		super();
		// The schematicToSave is filled out for us in PacketHandler
	}
	
	public void updateScreen() {
		super.updateScreen();
		fileNameField.updateCursorCounter();
	}

	public void onGuiClosed() {
		super.onGuiClosed();
		Keyboard.enableRepeatEvents(false);
	}

	public void initGui() {
		super.initGui();
		Keyboard.enableRepeatEvents(true);
		this.buttonList.clear();
		buttonList.add(saveBtn = new GuiButton(0, this.width / 2 - 100,
				this.height / 4 + 72, I18n.getStringParams("gui.save")));
		buttonList.add(cancelBtn = new GuiButton(1, this.width / 2 - 100,
				this.height / 4 + 96, I18n.getStringParams("gui.cancel")));
		fileNameField = new GuiTextField(this.fontRenderer,
				this.width / 2 - 150, 60, 300, 20);
		fileNameField.setMaxStringLength(32767);
		fileNameField.setFocused(true);
		updateSaveButton();
	}

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
				CompressedStreamTools.writeCompressed(schematicToSave.getNBT(), new FileOutputStream(new File(MyWorldGen.globalSchemDir, name)));
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
		}
	}

	public void updateSaveButton() {
		// Call this every so often to make sure we have a valid file name and a valid schematic
		saveBtn.enabled = fileNameField.getText().trim().length() > 0 && schematicToSave.entities != null && schematicToSave.tileEntities != null;
	}
	
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

	public void drawScreen(int par1, int par2, float par3) {
		drawDefaultBackground();
		drawCenteredString(fontRenderer, I18n.getStringParams("gui.filename"), this.width / 2,
				20, 0xFFFFFF);
		drawCenteredString(fontRenderer, I18n.getStringParams("selectWorld.resultFolder")+" "+MyWorldGen.globalSchemDir.getAbsolutePath(), this.width / 2, 97, 0xA0A0A0);
		fileNameField.drawTextBox();
		super.drawScreen(par1, par2, par3);
	}
}
