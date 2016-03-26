package net.boatcake.MyWorldGen;

import java.util.HashMap;
import java.util.Map;

import net.boatcake.MyWorldGen.utils.DirectionUtils;
import net.minecraft.block.BlockHugeMushroom;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockQuartz;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;

public class BlockRotation {
	public interface IBlockRotater {
		public IBlockState getRotatedState(IBlockState initialState, int rotationCount, Axis rotationAxis,
				IProperty prop);
	}

	public static Map<Class, IBlockRotater> rotationMap = new HashMap<Class, IBlockRotater>();
	static {
		rotationMap.put(EnumFacing.class, new FacingRotater());
		rotationMap.put(EnumFacing.Axis.class, new AxisRotater());
		rotationMap.put(BlockRailBase.EnumRailDirection.class, new RailRotater());
		rotationMap.put(BlockHugeMushroom.EnumType.class, new MushroomRotater());
		rotationMap.put(BlockQuartz.EnumType.class, new QuartzRotater());
		rotationMap.put(BlockLog.EnumAxis.class, new LogRotater());
	}

	public static IBlockState getRotatedState(IBlockState initialState, int rotationCount, Axis rotationAxis) {
		for (IProperty prop : (java.util.Set<IProperty>) initialState.getProperties().keySet()) {
			IBlockState rotatedState = null;
			IBlockRotater rotater = null;
			if (rotationMap.containsKey(prop.getValueClass())) {
				rotater = rotationMap.get(prop.getValueClass());
				rotatedState = rotater.getRotatedState(initialState, rotationCount, rotationAxis, prop);
			} else {
				for (Map.Entry<Class, IBlockRotater> entry : rotationMap.entrySet()) {
					if (entry.getKey().isAssignableFrom(prop.getValueClass())) {
						rotater = entry.getValue();
						rotatedState = rotater.getRotatedState(initialState, rotationCount, rotationAxis, prop);
						break;
					}
				}
			}
			if (rotatedState != null) {
				return rotatedState;
			}
		}
		// If there is no valid rotation, indicate an error so that we can avoid
		// a block update
		return null;
	}

	private static class FacingRotater implements IBlockRotater {
		@Override
		public IBlockState getRotatedState(IBlockState initialState, int rotationCount, Axis rotationAxis,
				IProperty prop) {
			EnumFacing facing = (EnumFacing) initialState.getValue(prop);
			for (int i = 0; i < rotationCount; i++) {
				facing = DirectionUtils.rotateAround(facing, rotationAxis);
			}
			return initialState.withProperty(prop, facing);
		}
	}

	private static class AxisRotater implements IBlockRotater {
		@Override
		public IBlockState getRotatedState(IBlockState initialState, int rotationCount, Axis rotationAxis,
				IProperty prop) {
			if (rotationCount % 2 == 0) {
				// "valid," but identity, rotation
				return null;
			}
			EnumFacing.Axis initialAxis = (EnumFacing.Axis) initialState.getValue(prop);
			EnumFacing.Axis rotatedAxis;
			switch (initialAxis) {
			case X:
				switch (rotationAxis) {
				case X:
					return null; // identity
				case Y:
					rotatedAxis = EnumFacing.Axis.Z;
					break;
				case Z:
					rotatedAxis = EnumFacing.Axis.Y;
					break;
				default:
					return null;
				}
				break;
			case Y:
				switch (rotationAxis) {
				case X:
					rotatedAxis = EnumFacing.Axis.Z;
					break;
				case Y:
					return null; // identity
				case Z:
					rotatedAxis = EnumFacing.Axis.X;
					break;
				default:
					return null;
				}
				break;
			case Z:
				switch (rotationAxis) {
				case X:
					rotatedAxis = EnumFacing.Axis.Y;
					break;
				case Y:
					rotatedAxis = EnumFacing.Axis.X;
					break;
				case Z:
					return null; // identity
				default:
					return null;
				}
				break;
			default:
				return null;
			}
			return initialState.withProperty(prop, rotatedAxis);
		}
	}

	private static class RailRotater implements IBlockRotater {

		@Override
		public IBlockState getRotatedState(IBlockState initialState, int rotationCount, Axis rotationAxis,
				IProperty prop) {
			if (rotationAxis != Axis.Y) {
				return null;
			}
			BlockRailBase.EnumRailDirection direction = (BlockRailBase.EnumRailDirection) initialState.getValue(prop);
			for (int i = 0; i < rotationCount; i++) {
				switch (direction) {
				case ASCENDING_EAST:
					direction = BlockRailBase.EnumRailDirection.ASCENDING_SOUTH;
					break;
				case ASCENDING_NORTH:
					direction = BlockRailBase.EnumRailDirection.ASCENDING_EAST;
					break;
				case ASCENDING_SOUTH:
					direction = BlockRailBase.EnumRailDirection.ASCENDING_WEST;
					break;
				case ASCENDING_WEST:
					direction = BlockRailBase.EnumRailDirection.ASCENDING_NORTH;
					break;
				case EAST_WEST:
					direction = BlockRailBase.EnumRailDirection.NORTH_SOUTH;
					break;
				case NORTH_EAST:
					direction = BlockRailBase.EnumRailDirection.SOUTH_EAST;
					break;
				case NORTH_SOUTH:
					direction = BlockRailBase.EnumRailDirection.EAST_WEST;
					break;
				case NORTH_WEST:
					direction = BlockRailBase.EnumRailDirection.NORTH_EAST;
					break;
				case SOUTH_EAST:
					direction = BlockRailBase.EnumRailDirection.SOUTH_WEST;
					break;
				case SOUTH_WEST:
					direction = BlockRailBase.EnumRailDirection.NORTH_WEST;
					break;
				default:
					return null;
				}
			}
			return initialState.withProperty(prop, direction);
		}
	}

	private static class MushroomRotater implements IBlockRotater {

		@Override
		public IBlockState getRotatedState(IBlockState initialState, int rotationCount, Axis rotationAxis,
				IProperty prop) {
			if (rotationAxis != Axis.Y) {
				return null;
			}
			BlockHugeMushroom.EnumType direction = (BlockHugeMushroom.EnumType) initialState.getValue(prop);
			for (int i = 0; i < rotationCount; i++) {
				switch (direction) {
				case EAST:
					direction = BlockHugeMushroom.EnumType.SOUTH;
					break;
				case NORTH:
					direction = BlockHugeMushroom.EnumType.EAST;
					break;
				case NORTH_EAST:
					direction = BlockHugeMushroom.EnumType.SOUTH_EAST;
					break;
				case NORTH_WEST:
					direction = BlockHugeMushroom.EnumType.NORTH_EAST;
					break;
				case SOUTH:
					direction = BlockHugeMushroom.EnumType.WEST;
					break;
				case SOUTH_EAST:
					direction = BlockHugeMushroom.EnumType.SOUTH_WEST;
					break;
				case SOUTH_WEST:
					direction = BlockHugeMushroom.EnumType.NORTH_WEST;
					break;
				case WEST:
					direction = BlockHugeMushroom.EnumType.NORTH;
					break;
				case ALL_INSIDE:
				case ALL_OUTSIDE:
				case ALL_STEM:
				case CENTER:
				case STEM:
				default:
					return null;
				}
			}
			return initialState.withProperty(prop, direction);
		}

	}

	private static class QuartzRotater implements IBlockRotater {

		@Override
		public IBlockState getRotatedState(IBlockState initialState, int rotationCount, Axis rotationAxis,
				IProperty prop) {
			if (rotationCount % 2 == 0) {
				// "valid," but identity, rotation
				return null;
			}
			BlockQuartz.EnumType type = (BlockQuartz.EnumType) initialState.getValue(prop);
			switch (rotationAxis) {
			case X:
				switch (type) {
				case LINES_X:
					type = BlockQuartz.EnumType.LINES_X;
					break;
				case LINES_Y:
					type = BlockQuartz.EnumType.LINES_Z;
					break;
				case LINES_Z:
					type = BlockQuartz.EnumType.LINES_Y;
					break;
				case CHISELED:
				case DEFAULT:
				default:
					return null;
				}
				break;
			case Y:
				switch (type) {
				case LINES_X:
					type = BlockQuartz.EnumType.LINES_Z;
					break;
				case LINES_Y:
					type = BlockQuartz.EnumType.LINES_Y;
					break;
				case LINES_Z:
					type = BlockQuartz.EnumType.LINES_X;
					break;
				case CHISELED:
				case DEFAULT:
				default:
					return null;
				}
				break;
			case Z:
				switch (type) {
				case LINES_X:
					type = BlockQuartz.EnumType.LINES_Y;
					break;
				case LINES_Y:
					type = BlockQuartz.EnumType.LINES_X;
					break;
				case LINES_Z:
					type = BlockQuartz.EnumType.LINES_Z;
					break;
				case CHISELED:
				case DEFAULT:
				default:
					return null;
				}
				break;
			default:
				return null;
			}
			return initialState.withProperty(prop, type);
		}

	}

	private static class LogRotater implements IBlockRotater {

		@Override
		public IBlockState getRotatedState(IBlockState initialState, int rotationCount, Axis rotationAxis,
				IProperty prop) {
			if (rotationCount % 2 == 0) {
				// "valid," but identity, rotation
				return null;
			}
			BlockLog.EnumAxis type = (BlockLog.EnumAxis) initialState.getValue(prop);
			switch (rotationAxis) {
			case X:
				switch (type) {
				case X:
					type = BlockLog.EnumAxis.X;
					break;
				case Y:
					type = BlockLog.EnumAxis.Z;
					break;
				case Z:
					type = BlockLog.EnumAxis.Y;
					break;
				case NONE:
				default:
					return null;
				}
				break;
			case Y:
				switch (type) {
				case X:
					type = BlockLog.EnumAxis.Z;
					break;
				case Y:
					type = BlockLog.EnumAxis.Y;
					break;
				case Z:
					type = BlockLog.EnumAxis.X;
					break;
				case NONE:
				default:
					return null;
				}
				break;
			case Z:
				switch (type) {
				case X:
					type = BlockLog.EnumAxis.Y;
					break;
				case Y:
					type = BlockLog.EnumAxis.X;
					break;
				case Z:
					type = BlockLog.EnumAxis.Z;
					break;
				case NONE:
				default:
					return null;
				}
				break;
			default:
				return null;
			}
			return initialState.withProperty(prop, type);
		}

	}
}
