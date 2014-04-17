package net.boatcake.MyWorldGen.items;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import net.boatcake.MyWorldGen.utils.WorldUtils;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.world.World;

public class ItemWandSave extends Item {

	public ItemWandSave(int id) {
		super(id);
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
					// get parameters
					int x1 = stack.getTagCompound().getInteger("x");
					int y1 = stack.getTagCompound().getInteger("y");
					int z1 = stack.getTagCompound().getInteger("z");
					int x2 = blockX;
					int y2 = blockY;
					int z2 = blockZ;

					/*
					 * Compile a response packet with both the original
					 * selection box, as well as the entity and tile entity
					 * data. We'll need the selection box in order to compile
					 * block data later.
					 */
					NBTTagCompound tagToSend = new NBTTagCompound("MWGGetSchem");
					tagToSend.setInteger("x1", x1);
					tagToSend.setInteger("y1", y1);
					tagToSend.setInteger("z1", z1);
					tagToSend.setInteger("x2", x2);
					tagToSend.setInteger("y2", y2);
					tagToSend.setInteger("z2", z2);
					tagToSend.setTag("entities", WorldUtils.getEntities(
							playerMP.worldObj, x1, y1, z1, x2, y2, z2));
					tagToSend.setTag("tileEntities", WorldUtils.getTileEntities(
							playerMP.worldObj, x1, y1, z1, x2, y2, z2));

					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					try {
						CompressedStreamTools.writeCompressed(tagToSend, bos);
						Packet250CustomPayload packetToSend = new Packet250CustomPayload(
								"MWGGetSchem", bos.toByteArray());
						PacketDispatcher.sendPacketToPlayer(packetToSend, (Player) player);
					} catch (IOException exc) {
						exc.printStackTrace();
					}
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
