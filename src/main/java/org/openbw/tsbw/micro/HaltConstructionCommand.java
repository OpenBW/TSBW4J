package org.openbw.tsbw.micro;

import org.openbw.bwapi4j.unit.SCV;


public class HaltConstructionCommand implements Command {

	private SCV unit;
	
	public HaltConstructionCommand(SCV unit) {
		
		this.unit = unit;
	}

	@Override
	public boolean execute() {
		
		return this.unit.haltConstruction();
	}

	@Override
	public int getDelay() {
		
		return 2;
	}
	
	@Override
	public String toString() {
		
		return "command: 'halt construction'";
	}
}
