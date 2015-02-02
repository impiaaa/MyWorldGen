package net.boatcake.MyWorldGen.items;

import net.boatcake.MyWorldGen.network.MessageGetSchemClient;
import net.boatcake.MyWorldGen.utils.NetUtils;
import net.boatcake.MyWorldGen.utils.WorldUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemWandSave extends Item {

	public ItemWandSave() {
		super();
		setMaxStackSize(1);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public boolean hasEffect(ItemStack stack, int pass) {
		return stack.hasTagCompound() && (pass == 0);
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
					message.entitiesTag = WorldUtils.getEntities(
							playerMP.worldObj, message.x1, message.y1,
							message.z1, message.x2, message.y2, message.z2);
					message.tileEntitiesTag = WorldUtils.getTileEntities(
							playerMP.worldObj, message.x1, message.y1,
							message.z1, message.x2, message.y2, message.z2);
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
				tag.setInteger("x", blockX);
				tag.setInteger("y", blockY);
				tag.setInteger("z", blockZ);
				stack.setTagCompound(tag);
			}
		}
		return true;
	}

	@SideOnly(Side.CLIENT)
	public static void translateToWorldCoords(Entity entity, float frame) {
		double interpPosX = entity.lastTickPosX
				+ (entity.posX - entity.lastTickPosX) * frame;
		double interpPosY = entity.lastTickPosY
				+ (entity.posY - entity.lastTickPosY) * frame;
		double interpPosZ = entity.lastTickPosZ
				+ (entity.posZ - entity.lastTickPosZ) * frame;
		GL11.glTranslated(-interpPosX, -interpPosY, -interpPosZ);
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onWorldRender(RenderWorldLastEvent event) {
		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayerSP player = mc.thePlayer;
		if (player != null && mc.objectMouseOver != null) {
			ItemStack stack = player.getHeldItem();
			if (stack != null && stack.getItem() == this
					&& stack.hasTagCompound()) {
				NBTTagCompound tag = stack.getTagCompound();
				double x1 = tag.getInteger("x");
				double y1 = tag.getInteger("y");
				double z1 = tag.getInteger("z");
				double x2 = mc.objectMouseOver.blockX;
				double y2 = mc.objectMouseOver.blockY;
				double z2 = mc.objectMouseOver.blockZ;
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

				GL11.glEnable(GL11.GL_BLEND);
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA,
						GL11.GL_ONE_MINUS_SRC_ALPHA);
				GL11.glDisable(GL11.GL_LIGHTING);
				GL11.glDisable(GL11.GL_TEXTURE_2D);
				GL11.glColor4f(0.5F, 0.75F, 1.0F, 0.5F);

				GL11.glPushMatrix();

				Entity entity = Minecraft.getMinecraft().renderViewEntity;
				translateToWorldCoords(entity, event.partialTicks);

				Tessellator tess = Tessellator.instance;

				tess.startDrawingQuads();
				tess.addVertex(x1, y1, z2);
				tess.addVertex(x1, y2, z2);
				tess.addVertex(x2, y2, z2);
				tess.addVertex(x2, y1, z2);

				tess.addVertex(x1, y1, z1);
				tess.addVertex(x2, y1, z1);
				tess.addVertex(x2, y2, z1);
				tess.addVertex(x1, y2, z1);

				tess.addVertex(x1, y1, z2);
				tess.addVertex(x1, y1, z1);
				tess.addVertex(x1, y2, z1);
				tess.addVertex(x1, y2, z2);

				tess.addVertex(x1, y2, z2);
				tess.addVertex(x1, y2, z1);
				tess.addVertex(x2, y2, z1);
				tess.addVertex(x2, y2, z2);

				tess.addVertex(x2, y2, z2);
				tess.addVertex(x2, y2, z1);
				tess.addVertex(x2, y1, z1);
				tess.addVertex(x2, y1, z2);

				tess.addVertex(x1, y1, z1);
				tess.addVertex(x1, y1, z2);
				tess.addVertex(x2, y1, z2);
				tess.addVertex(x2, y1, z1);
				tess.draw();

				GL11.glPopMatrix();

				GL11.glDisable(GL11.GL_BLEND);
				GL11.glEnable(GL11.GL_LIGHTING);
				GL11.glEnable(GL11.GL_TEXTURE_2D);
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			}
		}
	}
}
