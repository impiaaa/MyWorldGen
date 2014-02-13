package net.boatcake.MyWorldGen.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.boatcake.MyWorldGen.MyWorldGen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.FMLIndexedMessageToMessageCodec;
import cpw.mods.fml.common.network.NetworkRegistry;

public class MWGCodec extends FMLIndexedMessageToMessageCodec<MWGMessage> {
	public MWGCodec() {
		addDiscriminator(0, MessageGetSchemClient.class);
		addDiscriminator(1, MessageGetSchemServer.class);
		addDiscriminator(2, MessagePlaceSchem.class);
	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf source,
			MWGMessage msg) {
		msg.fromBytes(source);
		EntityPlayer player = null;
		switch (FMLCommonHandler.instance().getEffectiveSide()) {
		case CLIENT:
			player = null;
			break;
		case SERVER:
			INetHandler netHandler = ctx.channel()
					.attr(NetworkRegistry.NET_HANDLER).get();
			player = ((NetHandlerPlayServer) netHandler).playerEntity;
			break;
		}
		MWGMessage response = msg.handle(player);
		if (response != null && player instanceof EntityPlayerMP) {
			MyWorldGen.instance.sendTo(response, (EntityPlayerMP) player);
		}
	}

	@Override
	public void encodeInto(ChannelHandlerContext ctx, MWGMessage msg,
			ByteBuf target) throws Exception {
		msg.toBytes(target);
	}
}
