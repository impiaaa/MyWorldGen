package net.boatcake.MyWorldGen;

import net.minecraft.util.WeightedRandom.Item;

public class WeightedRandomSchematic extends Item {

	public Schematic schematic;

	public WeightedRandomSchematic(Schematic s) {
		super(s.info.randomWeight);
		schematic = s;
	}

}
