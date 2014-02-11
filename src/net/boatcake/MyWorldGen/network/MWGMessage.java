package net.boatcake.MyWorldGen.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

public interface MWGMessage {

	void fromBytes(ByteBuf buf);

	MWGMessage handle(EntityPlayer player);

	void toBytes(ByteBuf buf);
}
