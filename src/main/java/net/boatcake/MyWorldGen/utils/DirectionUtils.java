package net.boatcake.MyWorldGen.utils;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class DirectionUtils {
	// To rotate facing A, rotate around B, N times
	public static Axis axisForDirection(EnumFacing rotationDirection) {
		switch (rotationDirection) {
		case UP:
			return Axis.X;
		case WEST:
			return Axis.Y;
		case NORTH:
			return Axis.Y;
		case DOWN:
			return Axis.X;
		case EAST:
			return Axis.Y;
		case SOUTH:
		default:
			return Axis.Z;
		}
	}

	public static float pitchOffsetForDirection(EnumFacing rotationDirection) {
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
		default:
			return 0;
		}
	}

	public static EnumFacing getFakeAxisFromAxis(Axis axis) {
		switch (axis) {
		case X:
			return EnumFacing.EAST;
		case Y:
			return EnumFacing.UP;
		case Z:
			return EnumFacing.SOUTH;
		default:
			return null;
		}
	}

	public static int rotationCountForDirection(EnumFacing rotationDirection) {
		switch (rotationDirection) {
		case UP:
			return 1;
		case WEST:
			return 1;
		case NORTH:
			return 2;
		case DOWN:
			return 3;
		case EAST:
			return 3;
		case SOUTH:
		default:
			return 0;
		}
	}

	public static float yawOffsetForRotation(Rotation rotation) {
		switch (rotation) {
		case NONE:
			return 0;
		case CLOCKWISE_90:
			return 90;
		case CLOCKWISE_180:
			return 180;
		case COUNTERCLOCKWISE_90:
			return -90;
		default:
			return 0;
		}
	}
	
	public static float yawOffsetForDirection(EnumFacing rotationDirection) {
		switch (rotationDirection) {
		case SOUTH:
			return 0;
		case WEST:
			return 90;
		case NORTH:
			return 180;
		case EAST:
			return -90;
		default:
			return 0;
		}
	}
	
	public static Rotation rotationForFacing(EnumFacing facing) {
		switch (facing) {
		case SOUTH:
			return Rotation.NONE;
		case WEST:
			return Rotation.CLOCKWISE_90;
		case NORTH:
			return Rotation.CLOCKWISE_180;
		case EAST:
			return Rotation.COUNTERCLOCKWISE_90;
		default:
			return null;
		}
	}

	public static Vec3d rotateCoords(Vec3d coords, Vec3d at, Rotation rot) {
		double worldX = coords.xCoord;
		double worldY = coords.yCoord;
		double worldZ = coords.zCoord;
		switch (rot) {
		case CLOCKWISE_180:
			return new Vec3d(-worldX+at.xCoord, worldY+at.yCoord, -worldZ+at.zCoord);
		case CLOCKWISE_90:
			return new Vec3d(-worldZ+at.xCoord, worldY+at.yCoord, worldX+at.zCoord);
		case COUNTERCLOCKWISE_90:
			return new Vec3d(worldZ+at.xCoord, worldY+at.yCoord, -worldX+at.zCoord);
		case NONE:
		default:
			return new Vec3d(worldX+at.xCoord, worldY+at.yCoord, worldZ+at.zCoord);
		}
	}

	public static Vec3d rotateCoords(Vec3i coords, Vec3d at, Rotation rot) {
		return rotateCoords(new Vec3d(coords), at,rot);
	}

	public static EnumFacing[] cardinalDirections = new EnumFacing[] {
			EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH,
			EnumFacing.WEST };

	public static EnumFacing getDirectionFromYaw(float yaw) {
		return cardinalDirections[MathHelper
				.floor_double(yaw * 4.0F / 360.0F + 0.5D) & 0x3];
	}

	public static Rotation getRotationFromYaw(float yaw) {
		return Rotation.values()[MathHelper
				.floor_double(yaw * 4.0F / 360.0F + 0.5D) & 0x3];
	}

	// This method already exists in EnumFacing, but it's SideOnly(CLIENT) for
	// some reason >:(
	public static EnumFacing rotateAround(EnumFacing facing,
			EnumFacing.Axis axis) {
		switch (axis) {
		case X:
			switch (facing) {
			case NORTH:
				return EnumFacing.DOWN;
			case EAST:
			case WEST:
			default:
				return facing;
			case SOUTH:
				return EnumFacing.UP;
			case UP:
				return EnumFacing.NORTH;
			case DOWN:
				return EnumFacing.SOUTH;
			}

		case Y:
			if (facing != EnumFacing.UP && facing != EnumFacing.DOWN) {
				return facing.rotateY();
			}
			return facing;

		case Z:
			switch (facing) {
			case EAST:
				return EnumFacing.DOWN;
			case NORTH:
			case SOUTH:
			default:
				return facing;
			case WEST:
				return EnumFacing.UP;
			case UP:
				return EnumFacing.EAST;
			case DOWN:
				return EnumFacing.WEST;
			}

		default:
			throw new IllegalStateException("Unable to get CW facing for axis "
					+ axis);
		}
	}
}
