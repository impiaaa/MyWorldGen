package net.boatcake.MyWorldGen.utils;

import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;

public class DirectionUtils {

	public static ForgeDirection axisForDirection(ForgeDirection rotationDirection) {
		switch (rotationDirection) {
		case UP:
			return ForgeDirection.EAST;
		case WEST:
			return ForgeDirection.UP;
		case NORTH:
			return ForgeDirection.UP;
		case DOWN:
			return ForgeDirection.WEST;
		case EAST:
			return ForgeDirection.DOWN;
		case SOUTH:
		case UNKNOWN:
		default:
			return ForgeDirection.UNKNOWN;
		}
	}

	public static float pitchOffsetForDirection(ForgeDirection rotationDirection) {
		switch (rotationDirection) {
		case UP:
			return 90;
		case WEST:
			return 0;
		case NORTH:
			return 0;
		case DOWN:
			return -90;
		case EAST:
			return 0;
		case SOUTH:
		case UNKNOWN:
		default:
			return 0;
		}
	}

	public static int rotationCountForDirection(ForgeDirection rotationDirection) {
		switch (rotationDirection) {
		case UP:
			return 1;
		case WEST:
			return 1;
		case NORTH:
			return 2;
		case DOWN:
			return 1;
		case EAST:
			return 1;
		case SOUTH:
		case UNKNOWN:
		default:
			return 0;
		}
	}

	public static float yawOffsetForDirection(ForgeDirection rotationDirection) {
		switch (rotationDirection) {
		case UP:
			return 0;
		case WEST:
			return 90;
		case NORTH:
			return 180;
		case DOWN:
			return 0;
		case EAST:
			return -90;
		case SOUTH:
		case UNKNOWN:
		default:
			return 0;
		}
	}

	public static Vec3 rotateCoords(Vec3 coords, Vec3 at, ForgeDirection rotationAxis, int rotationCount) {
		double worldX = coords.xCoord;
		double worldY = coords.yCoord;
		double worldZ = coords.zCoord;
		for (int i = 0; i < rotationCount; i++) {
			if (rotationAxis.offsetX == 1) {
				double temp = worldY;
				worldY = -worldZ;
				worldZ = temp;
			} else if (rotationAxis.offsetX == -1) {
				double temp = worldY;
				worldY = worldZ;
				worldZ = -temp;
			}
			if (rotationAxis.offsetY == 1) {
				double temp = worldX;
				worldX = -worldZ;
				worldZ = temp;
			} else if (rotationAxis.offsetY == -1) {
				double temp = worldX;
				worldX = worldZ;
				worldZ = -temp;
			}
			if (rotationAxis.offsetZ == 1) {
				double temp = worldX;
				worldX = -worldY;
				worldY = temp;
			} else if (rotationAxis.offsetZ == -1) {
				double temp = worldX;
				worldX = worldY;
				worldY = -temp;
			}
		}

		worldX += at.xCoord;
		worldY += at.yCoord;
		worldZ += at.zCoord;

		return Vec3.createVectorHelper(worldX, worldY, worldZ);
	}

	public static ForgeDirection[] cardinalDirections = new ForgeDirection[] { ForgeDirection.NORTH,
			ForgeDirection.EAST, ForgeDirection.SOUTH, ForgeDirection.WEST };

	public static ForgeDirection getDirectionFromYaw(float yaw) {
		return cardinalDirections[MathHelper.floor_double(yaw * 4.0F / 360.0F + 0.5D) & 0x3];
	}
}
