package net.boatcake.MyWorldGen.utils;

import net.boatcake.MyWorldGen.MyWorldGen;
import net.boatcake.MyWorldGen.network.MessageGetSchemClient;
import net.boatcake.MyWorldGen.network.MessagePlaceSchem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class NetUtils {
	public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE
			.newSimpleChannel(MyWorldGen.MODID.toLowerCase());

	public static void sendTo(IMessage message, EntityPlayerMP player) {
		INSTANCE.sendTo(message, player);
	}

	@SideOnly(Side.CLIENT)
	public static void sendToServer(IMessage message) {
		INSTANCE.sendToServer(message);
	}

	public static void init() {
		INSTANCE.registerMessage(MessageGetSchemClient.class, MessageGetSchemClient.class, 0, Side.CLIENT);
		INSTANCE.registerMessage(MessagePlaceSchem.class, MessagePlaceSchem.class, 2, Side.SERVER);
	}
}
