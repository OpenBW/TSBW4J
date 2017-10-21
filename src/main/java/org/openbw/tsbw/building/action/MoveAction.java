package org.openbw.tsbw.building.action;

import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.unit.MobileUnit;

public class MoveAction implements Action {

	private MobileUnit mobileUnit;
	private Position position;
	
	public MoveAction(MobileUnit unit, Position position) {
		
		this.mobileUnit = unit;
		this.position = position;
	}
	
	public boolean execute() {
		
		return this.mobileUnit.move(position);
	}
}
