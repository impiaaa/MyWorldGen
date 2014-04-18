package net.boatcake.MyWorldGen.items;

import net.boatcake.MyWorldGen.network.MessageGetSchemClient;
import net.boatcake.MyWorldGen.utils.NetUtils;
import net.boatcake.MyWorldGen.utils.WorldUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class ItemWandSave extends Item {

	public ItemWandSave() {
		super();
		setMaxStackSize(1);
	}

	@Override
	public boolean hasEffect(ItemStack stack) {
		return stack.hasTagCompound();
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world,
			int blockX, int blockY, int blockZ, int side, float hitX,
			float hitY, float hitZ) {
		if (!world.isRemote) {
			if (stack.hasTagCompound()) {
				/*
				 * Step 2: While the client keeps a local copy of the blocks in
				 * the world, it does not know any entity or tile entity data
				 * until it's needed (e.g., the contents of a chest aren't sent
				 * until the chest is opened; a villager's trades are not known
				 * until the player talks to it). So, we the server to send back
				 * what entities and tile entities are within the selected
				 * region.
				 */

				// Step 3: The server receives the selection box from the
				// client.
				EntityPlayerMP playerMP = (EntityPlayerMP) player;
				/*
				 * First we need to make sure that they're allowed to see chests
				 * etc. I assume being in creative is good enough, I don't want
				 * to implement a permissions system right now.
				 */
				if (playerMP.capabilities.isCreativeMode) {
					/*
					 * Compile a response packet with both the original
					 * selection box, as well as the entity and tile entity
					 * data. We'll need the selection box in order to compile
					 * block data later.
					 */
					MessageGetSchemClient message = new MessageGetSchemClient();
					message.x1 = stack.getTagCompound().getInteger("x");
					message.y1 = stack.getTagCompound().getInteger("y");
					message.z1 = stack.getTagCompound().getInteger("z");
					message.x2 = blockX;
					message.y2 = blockY;
					message.z2 = blockZ;
					message.entitiesTag = WorldUtils.getEntities(playerMP.worldObj, message.x1, message.y1, message.z1, message.x2, message.y2, message.z2);
					message.tileEntitiesTag = WorldUtils.getTileEntities(playerMP.worldObj, message.x1, message.y1, message.z1, message.x2, message.y2, message.z2);
					NetUtils.sendTo(message, playerMP);
				}
				// Clear the item data, so that we can make a new selection
				stack.setTagCompound(null);
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
}
