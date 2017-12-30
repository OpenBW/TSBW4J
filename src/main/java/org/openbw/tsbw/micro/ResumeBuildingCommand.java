package org.openbw.tsbw.micro;

import org.openbw.bwapi4j.unit.Building;
import org.openbw.bwapi4j.unit.SCV;

public class ResumeBuildingCommand implements Command {

	private SCV scv;
	private Building construction;
	
	public ResumeBuildingCommand(SCV scv, Building construction) {
		
		this.scv = scv;
		this.construction = construction;
	}
	
	@Override
	public boolean execute() {
		
		return this.scv.resumeBuilding(construction);
	}

	@Override
	public String toString() {
		
		return "command: resume building " + construction ;
	}

	@Override
	public int getDelay() {
		return 3;
	}

}
