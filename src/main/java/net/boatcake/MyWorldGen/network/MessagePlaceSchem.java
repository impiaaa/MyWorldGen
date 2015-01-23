package net.boatcake.MyWorldGen.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;

import java.io.IOException;
import java.util.Random;

import net.boatcake.MyWorldGen.BlockPlacementOption;
import net.boatcake.MyWorldGen.Schematic;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessagePlaceSchem implements IMessage,
		IMessageHandler<MessagePlaceSchem, IMessage> {
	public EnumFacing direction;
	public NBTTagCompound schematicTag;
	public BlockPos pos;
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
		pos = new BlockPos(packetTag.getInteger("x"),
				packetTag.getInteger("y"), packetTag.getInteger("z"));
		schematicTag = packetTag.getCompoundTag("schematic");
		direction = EnumFacing.getFront(packetTag.getInteger("direction"));
		placementOption = BlockPlacementOption.get(packetTag
				.getInteger("placementOption"));
	}

	@Override
	public IMessage onMessage(MessagePlaceSchem message, MessageContext ctx) {
		EntityPlayerMP playerMP = ctx.getServerHandler().playerEntity;
		// no cheating!
		if (playerMP.capabilities.isCreativeMode) {
			new Schematic(message.schematicTag, null).placeInWorld(
					playerMP.worldObj, message.pos, message.direction,
					message.placementOption.generateChests,
					message.placementOption.generateSpawners,
					message.placementOption.followPlacementRules, new Random());
		}
		return null;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		NBTTagCompound tagToSend = new NBTTagCompound();
		ByteBufOutputStream bos = new ByteBufOutputStream(buf);
		tagToSend.setInteger("x", pos.getX());
		tagToSend.setInteger("y", pos.getY());
		tagToSend.setInteger("z", pos.getZ());
		tagToSend.setInteger("direction", direction.getIndex());
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
