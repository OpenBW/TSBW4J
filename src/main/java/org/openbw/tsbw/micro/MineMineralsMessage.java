package org.openbw.tsbw.micro;

import org.openbw.tsbw.unit.MineralPatch;

public class MineMineralsMessage extends Message {

	private MineralPatch patch;
	
	public MineMineralsMessage(MineralPatch patch) {
		
		super("");
		this.patch = patch;
	}
	
	public MineralPatch getMineralPatch() {
		
		return this.patch;
	}
}
