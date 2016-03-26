package net.boatcake.MyWorldGen.network;

import java.io.IOException;
import java.util.Random;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.boatcake.MyWorldGen.BlockPlacementOption;
import net.boatcake.MyWorldGen.Schematic;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

public class MessagePlaceSchem implements IMessage, IMessageHandler<MessagePlaceSchem, IMessage> {
	public ForgeDirection direction;
	public NBTTagCompound schematicTag;
	public int x, y, z;
	public BlockPlacementOption placementOption;

	@Override
	public void fromBytes(ByteBuf buf) {
		ByteBufInputStream inputStream = new ByteBufInputStream(buf);
		NBTTagCompound packetTag;
		try {
			packetTag = CompressedStreamTools.readCompressed(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		x = packetTag.getInteger("x");
		y = packetTag.getInteger("y");
		z = packetTag.getInteger("z");
		schematicTag = packetTag.getCompoundTag("schematic");
		direction = ForgeDirection.getOrientation(packetTag.getInteger("direction"));
		placementOption = BlockPlacementOption.get(packetTag.getInteger("placementOption"));
	}

	@Override
	public IMessage onMessage(MessagePlaceSchem message, MessageContext ctx) {
		EntityPlayerMP playerMP = ctx.getServerHandler().playerEntity;
		// no cheating!
		if (playerMP.capabilities.isCreativeMode) {
			new Schematic(message.schematicTag, null).placeInWorld(playerMP.worldObj, message.x, message.y, message.z,
					message.direction, message.placementOption.generateChests, message.placementOption.generateSpawners,
					message.placementOption.followPlacementRules, new Random());
		}
		return null;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		NBTTagCompound tagToSend = new NBTTagCompound();
		ByteBufOutputStream bos = new ByteBufOutputStream(buf);
		tagToSend.setInteger("x", x);
		tagToSend.setInteger("y", y);
		tagToSend.setInteger("z", z);
		tagToSend.setInteger("direction", direction.ordinal());
		tagToSend.setInteger("placementOption", placementOption.id);
		// We might be able to send the file data directly, but it's better to
		// make sure that it's valid NBT first.
		try {
			tagToSend.setTag("schematic", schematicTag);
			CompressedStreamTools.writeCompressed(tagToSend, bos);
		} catch (Exception exc) {
			exc.printStackTrace();
			return;
		}
	}

}
