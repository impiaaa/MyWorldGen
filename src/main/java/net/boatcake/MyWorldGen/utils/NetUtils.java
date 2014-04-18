package net.boatcake.MyWorldGen.utils;

import java.util.EnumMap;

import cpw.mods.fml.common.network.FMLEmbeddedChannel;
import cpw.mods.fml.common.network.FMLOutboundHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.boatcake.MyWorldGen.network.MWGMessage;
import net.minecraft.entity.player.EntityPlayerMP;

public class NetUtils {
	public static EnumMap<Side, FMLEmbeddedChannel> net;
	public static void sendTo(MWGMessage message, EntityPlayerMP player) {
		net.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET)
				.set(FMLOutboundHandler.OutboundTarget.PLAYER);
		net.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS)
				.set(player);
		net.get(Side.SERVER).writeAndFlush(message);
	}

	@SideOnly(Side.CLIENT)
	public static void sendToServer(MWGMessage message) {
		net.get(Side.CLIENT).attr(FMLOutboundHandler.FML_MESSAGETARGET)
				.set(FMLOutboundHandler.OutboundTarget.TOSERVER);
		net.get(Side.CLIENT).writeAndFlush(message);
	}
}
