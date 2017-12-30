package org.openbw.tsbw.unit;

import org.openbw.bwapi4j.unit.Building;

class ResumeBuildingMessage extends Message {

	private Building construction;
	
	public ResumeBuildingMessage(Building construction) {
		
		super("");
		this.construction = construction;
	}

	public Building getConstruction() {
		
		return this.construction;
	}
}
