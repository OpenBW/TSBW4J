package org.openbw.tsbw.unit;

class GatherMineralsMessage extends Message {

	private MineralPatch patch;
	
	public GatherMineralsMessage(MineralPatch patch) {
		
		super("");
		this.patch = patch;
	}
	
	public MineralPatch getMineralPatch() {
		
		return this.patch;
	}
}
