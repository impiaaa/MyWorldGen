package net.boatcake.MyWorldGen.items;

import net.boatcake.MyWorldGen.MyWorldGen;
import net.boatcake.MyWorldGen.network.MessageGetSchemServer;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
		setTextureName("MyWorldGen:wandSave");
	}

	@Override
	public boolean hasEffect(ItemStack stack) {
		return stack.hasTagCompound();
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world,
			int blockX, int blockY, int blockZ, int side, float hitX,
			float hitY, float hitZ) {
		if (world.isRemote) {
			if (stack.hasTagCompound()) {
				/*
				 * Step 2: While the client keeps a local copy of the blocks in
				 * the world, it does not know any entity or tile entity data
				 * until it's needed (e.g., the contents of a chest aren't sent
				 * until the chest is opened; a villager's trades are not known
				 * until the player talks to it). So, we need to send a request
				 * to the server for what entities and tile entities are within
				 * the selected region. For step 3, go to PacketHandler
				 */
				// Clear the item data, so that we can make a new selection
				stack.setTagCompound(null);

				MessageGetSchemServer message = new MessageGetSchemServer();
				message.x1 = stack.getTagCompound().getInteger("x");
				message.y1 = stack.getTagCompound().getInteger("y");
				message.z1 = stack.getTagCompound().getInteger("z");
				message.x2 = blockX;
				message.y2 = blockY;
				message.z2 = blockZ;

				MyWorldGen.instance.sendToServer(message);
			} else {
				/*
				 * START HERE
				 * Step 1: Find the first corner, and record it to
				 * the item data.
				 */
				NBTTagCompound tag = new NBTTagCompound();
				tag.setInteger("x", blockX);
				tag.setInteger("y", blockY);
				tag.setInteger("z", blockZ);
				stack.setTagCompound(tag);
			}
		}
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister ir) {
		this.itemIcon = ir.registerIcon(getIconString());
	}
}
