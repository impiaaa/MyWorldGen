package net.boatcake.MyWorldGen.items;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import net.boatcake.MyWorldGen.MyWorldGen;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemWandSave extends Item {

	public ItemWandSave() {
		super();
		setMaxStackSize(0);
		setUnlocalizedName("wandSave");
		setCreativeTab(MyWorldGen.creativeTab);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister ir) {
		this.itemIcon = ir.registerIcon("MyWorldGen:" + (this.getUnlocalizedName().substring(5)));
	}
	
	@Override
	public boolean hasEffect(ItemStack stack)
	{
		return stack.hasTagCompound();
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int blockX, int blockY, int blockZ, int side, float hitX, float hitY, float hitZ) {
		if (world.isRemote) {
			if (stack.hasTagCompound()) {
				// Step 2: While the client keeps a local copy of the blocks in the world,
				// it does not know any entity or tile entity data until it's needed (e.g.,
				// the contents of a chest aren't sent until the chest is opened; a
				// villager's trades are not known until the player talks to it). So, we
				// need to send a request to the server for what entities and tile entities
				// are within the selected region. For step 3, go to PacketHandler
				
				// Compile the packet with the selection box coordinates
				NBTTagCompound tagToSend = new NBTTagCompound();
				tagToSend.setInteger("x1", stack.getTagCompound().getInteger("x"));
				tagToSend.setInteger("y1", stack.getTagCompound().getInteger("y"));
				tagToSend.setInteger("z1", stack.getTagCompound().getInteger("z"));
				tagToSend.setInteger("x2", blockX);
				tagToSend.setInteger("y2", blockY);
				tagToSend.setInteger("z2", blockZ);

				// Clear the item data, so that we can make a new selection
				stack.setTagCompound(null);

				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				try {
					CompressedStreamTools.writeCompressed(tagToSend, bos);
				} catch (IOException ex) {
					ex.printStackTrace();
					return false;
				}

				Packet250CustomPayload packet = new Packet250CustomPayload("MWGGetSchem", bos.toByteArray());
				((EntityClientPlayerMP)player).sendQueue.addToSendQueue(packet);
			}
			else {
				// START HERE
				// Step 1: Find the first corner, and record it to the item data.
				NBTTagCompound tag = new NBTTagCompound();
				tag.setInteger("x", blockX);
				tag.setInteger("y", blockY);
				tag.setInteger("z", blockZ);
				stack.setTagCompound(tag);
			}
		}
		return true;
	}
}
