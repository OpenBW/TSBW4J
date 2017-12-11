package org.openbw.tsbw.micro;

import org.openbw.bwapi4j.unit.MobileUnit;
import org.openbw.tsbw.UnitInventory;


public class ScoutCommand implements Command {

	private MobileUnit unit;
	private UnitInventory unitInventory;
	
	public ScoutCommand(MobileUnit unit, UnitInventory unitInventory) {
		
		this.unit = unit;
		this.unitInventory = unitInventory;
	}

	@Override
	public boolean execute() {
		
		this.unitInventory.getScouts().add(unit);
		return true;
	}

	@Override
	public int getDelay() {
		
		return 0;
	}
	
	@Override
	public String toString() {
		
		return "command: 'scout'";
	}
}
