package net.boatcake.MyWorldGen;

import net.boatcake.MyWorldGen.blocks.TileEntityAnchorInventory;
import net.boatcake.MyWorldGen.client.GuiAnchorInventory;
import net.boatcake.MyWorldGen.client.GuiLoadSchematic;
import net.boatcake.MyWorldGen.client.GuiSaveSchematic;
import net.boatcake.MyWorldGen.utils.DirectionUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;

public class GuiHandler implements IGuiHandler {

	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world,
			int x, int y, int z) {
		if (FMLCommonHandler.instance().getSide() != Side.CLIENT) {
			return null;
		}
		switch (id) {
		case 0:
			return new GuiSaveSchematic();
		case 1:
			return new GuiLoadSchematic(new BlockPos(x, y, z),
					DirectionUtils.getDirectionFromYaw(player.rotationYaw));
		case 2:
			TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
			if (tileEntity instanceof TileEntityAnchorInventory) {
				return new GuiAnchorInventory(player.inventory,
						(TileEntityAnchorInventory) tileEntity);
			}
			return null;
		default:
			return null;
		}
	}

	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world,
			int x, int y, int z) {
		if (id == 2) {
			TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
			if (tileEntity instanceof TileEntityAnchorInventory) {
				return new ContainerAnchorInventory(player.inventory,
						(TileEntityAnchorInventory) tileEntity);
			}
		}
		return null;
	}

}
