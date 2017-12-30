package org.openbw.tsbw.micro;

import org.openbw.bwapi4j.unit.Mechanical;
import org.openbw.tsbw.unit.SCV;

public class RepairCommand implements Command {

	private SCV scv;
	private Mechanical toRepair;
	
	public RepairCommand(SCV scv, Mechanical toRepair) {
		
		this.scv = scv;
		this.toRepair = toRepair;
	}
	
	@Override
	public boolean execute() {
		
		return this.scv.repair(toRepair);
	}

	@Override
	public String toString() {
		
		return "command: repair " + toRepair;
	}

	@Override
	public int getDelay() {
		return 3;
	}

}
