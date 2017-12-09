package org.openbw.tsbw.micro;

import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.unit.MobileUnit;


public class MoveCommand implements Command {

	private MobileUnit unit;
	private Position position;
	
	public MoveCommand(MobileUnit unit, Position position) {
		
		this.unit = unit;
		this.position = position;
	}

	@Override
	public boolean execute() {
		
		if (unit.getPosition().getDistance(position) < 64) {
			
			return true;
		} else {
			
			return unit.move(position);
		}
	}

	@Override
	public int getDelay() {
		
		return 2;
	}
	
	@Override
	public String toString() {
		
		return "command: 'move to " + position + "'";
	}
}
