package net.boatcake.MyWorldGen.items;

import org.lwjgl.opengl.GL11;

import net.boatcake.MyWorldGen.network.MessageGetSchemClient;
import net.boatcake.MyWorldGen.utils.NetUtils;
import net.boatcake.MyWorldGen.utils.WorldUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemWandSave extends Item {

	public ItemWandSave() {
		super();
		setMaxStackSize(1);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public boolean hasEffect(ItemStack stack) {
		return stack.hasTagCompound();
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos blockPos, EnumFacing side,
			float hitX, float hitY, float hitZ) {
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
					message.pos1 = new BlockPos(stack.getTagCompound().getInteger("x"),
							stack.getTagCompound().getInteger("y"), stack.getTagCompound().getInteger("z"));
					message.pos2 = blockPos;
					message.entitiesTag = WorldUtils.getEntities(playerMP.worldObj, message.pos1, message.pos2);
					message.tileEntitiesTag = WorldUtils.getTileEntities(playerMP.worldObj, message.pos1, message.pos2);
					NetUtils.sendTo(message, playerMP);
				}
				// Clear the item data, so that we can make a new selection
				stack.setTagCompound(null);
			} else {
				/*
				 * START HERE Step 1: Find the first corner, and record it to
				 * the item data.
				 */
				NBTTagCompound tag = new NBTTagCompound();
				tag.setInteger("x", blockPos.getX());
				tag.setInteger("y", blockPos.getY());
				tag.setInteger("z", blockPos.getZ());
				stack.setTagCompound(tag);
			}
		}
		return true;
	}

	@SideOnly(Side.CLIENT)
	public static void translateToWorldCoords(Entity entity, float frame) {
		double interpPosX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * frame;
		double interpPosY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * frame;
		double interpPosZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * frame;
		GlStateManager.translate(-interpPosX, -interpPosY, -interpPosZ);
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onWorldRender(RenderWorldLastEvent event) {
		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayerSP player = mc.thePlayer;
		if (player != null && mc.objectMouseOver != null) {
			ItemStack stack = player.getHeldItem();
			BlockPos lookAtPos = mc.objectMouseOver.func_178782_a();
			if (stack != null && stack.getItem() == this && stack.hasTagCompound() && lookAtPos != null) {
				NBTTagCompound tag = stack.getTagCompound();
				double x1 = tag.getInteger("x");
				double y1 = tag.getInteger("y");
				double z1 = tag.getInteger("z");
				double x2 = lookAtPos.getX();
				double y2 = lookAtPos.getY();
				double z2 = lookAtPos.getZ();
				double t;

				if (x1 > x2) {
					x1 += 1.03125;
					x2 -= 0.03125;
				} else {
					t = x2 + 1.03125;
					x2 = x1 - 0.03125;
					x1 = t;
				}
				if (y1 > y2) {
					y1 += 1.03125;
					y2 -= 0.03125;
				} else {
					t = y2 + 1.03125;
					y2 = y1 - 0.03125;
					y1 = t;
				}
				if (z1 > z2) {
					z1 += 1.03125;
					z2 -= 0.03125;
				} else {
					t = z2 + 1.03125;
					z2 = z1 - 0.03125;
					z1 = t;
				}

				GlStateManager.enableBlend();
				GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				GlStateManager.disableLighting();
				GlStateManager.func_179090_x();
				GlStateManager.color(0.5F, 0.75F, 1.0F, 0.5F);

				GlStateManager.pushMatrix();

				Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
				translateToWorldCoords(entity, event.partialTicks);

				Tessellator tess = Tessellator.getInstance();
				WorldRenderer render = tess.getWorldRenderer();

				render.startDrawingQuads();
				render.addVertex(x1, y1, z2);
				render.addVertex(x1, y2, z2);
				render.addVertex(x2, y2, z2);
				render.addVertex(x2, y1, z2);

				render.addVertex(x1, y1, z1);
				render.addVertex(x2, y1, z1);
				render.addVertex(x2, y2, z1);
				render.addVertex(x1, y2, z1);

				render.addVertex(x1, y1, z2);
				render.addVertex(x1, y1, z1);
				render.addVertex(x1, y2, z1);
				render.addVertex(x1, y2, z2);

				render.addVertex(x1, y2, z2);
				render.addVertex(x1, y2, z1);
				render.addVertex(x2, y2, z1);
				render.addVertex(x2, y2, z2);

				render.addVertex(x2, y2, z2);
				render.addVertex(x2, y2, z1);
				render.addVertex(x2, y1, z1);
				render.addVertex(x2, y1, z2);

				render.addVertex(x1, y1, z1);
				render.addVertex(x1, y1, z2);
				render.addVertex(x2, y1, z2);
				render.addVertex(x2, y1, z1);
				tess.draw();

				GlStateManager.popMatrix();

				GlStateManager.disableBlend();
				GlStateManager.enableLighting();
				GlStateManager.func_179098_w();
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			}
		}
	}
}
