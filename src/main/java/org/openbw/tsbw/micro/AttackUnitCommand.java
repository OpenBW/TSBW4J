package org.openbw.tsbw.micro;

import org.openbw.bwapi4j.unit.MobileUnit;
import org.openbw.bwapi4j.unit.Unit;

public class AttackUnitCommand implements Command {

	private MobileUnit unit;
	private Unit victim;
	
	public AttackUnitCommand(MobileUnit unit, Unit victim) {
		
		this.unit = unit;
		this.victim = victim;
	}

	@Override
	public boolean execute() {
		
		return unit.attack(this.victim);
	}

	@Override
	public int getDelay() {
		return 7;
	}
	
	@Override
	public String toString() {
		return "command: 'attack unit " + victim + "'";
	}
}
