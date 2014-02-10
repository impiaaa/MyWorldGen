package net.boatcake.MyWorldGen.network;

import net.minecraft.entity.player.EntityPlayer;
import io.netty.buffer.ByteBuf;

public interface MWGMessage {

	void fromBytes(ByteBuf buf);

	void toBytes(ByteBuf buf);

	MWGMessage handle(EntityPlayer player);
}
