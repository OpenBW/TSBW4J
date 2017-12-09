package org.openbw.tsbw.micro;

import org.openbw.bwapi4j.unit.Unit;
import org.openbw.bwapi4j.unit.SCV;

public class AttackUnitCommand implements Command {

	private SCV worker;
	private Unit victim;
	
	public AttackUnitCommand(SCV worker, Unit victim) {
		
		this.worker = worker;
		this.victim = victim;
	}

	@Override
	public boolean execute() {
		
		return worker.attack(this.victim);
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
