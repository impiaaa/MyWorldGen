package net.boatcake.MyWorldGen;

public enum BlockPlacementOption {
	ASGENERATED("gui.placement.asgenerated", 0, true, true, true), LOSSLESS("gui.placement.lossless", 1, false, false,
			false);

	public String text;
	public BlockPlacementOption next;
	public int id;
	public boolean generateChests;
	public boolean generateSpawners;
	public boolean followPlacementRules;

	static {
		ASGENERATED.next = LOSSLESS;
		LOSSLESS.next = ASGENERATED;
	}

	private BlockPlacementOption(String text, int id, boolean generateChests, boolean generateSpawners,
			boolean followPlacementRules) {
		this.text = text;
		this.id = id;
		this.generateChests = generateChests;
		this.generateSpawners = generateSpawners;
		this.followPlacementRules = followPlacementRules;
	}

	public static BlockPlacementOption get(int id) {
		for (BlockPlacementOption o : BlockPlacementOption.values()) {
			if (o.id == id) {
				return o;
			}
		}
		return null;
	}
}