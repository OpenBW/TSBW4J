package org.openbw.tsbw.micro;

import org.openbw.bwapi4j.unit.MobileUnit;
import org.openbw.bwapi4j.unit.Unit;

public class AttackUnitCommand extends Command {

	private Unit victim;
	
	public AttackUnitCommand(int issuedAt, Unit victim) {
		super(issuedAt);
		this.victim = victim;
	}

	@Override
	public boolean execute(MobileUnit unit) {
		
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
