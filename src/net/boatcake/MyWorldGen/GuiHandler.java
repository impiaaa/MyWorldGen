package net.boatcake.MyWorldGen;

import net.boatcake.MyWorldGen.blocks.TileEntityAnchorInventory;
import net.boatcake.MyWorldGen.client.GuiAnchorInventory;
import net.boatcake.MyWorldGen.client.GuiLoadSchematic;
import net.boatcake.MyWorldGen.client.GuiSaveSchematic;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {

	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world,
			int x, int y, int z) {
		switch (id) {
		case 0:
			return new GuiSaveSchematic();
		case 1:
			return new GuiLoadSchematic(world, x, y, z,
					ForgeDirection.getOrientation(BlockPistonBase
							.determineOrientation(world, x, y, z, player)),
					(EntityClientPlayerMP) player);
		case 2:
			TileEntity tileEntity = world.getTileEntity(x, y, z);
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
			TileEntity tileEntity = world.getTileEntity(x, y, z);
			if (tileEntity instanceof TileEntityAnchorInventory) {
				return new ContainerAnchorInventory(player.inventory,
						(TileEntityAnchorInventory) tileEntity);
			}
		}
		return null;
	}

}
