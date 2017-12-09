package org.openbw.tsbw.micro;

import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.unit.SCV;
import org.openbw.tsbw.building.ConstructionType;

public class ConstructCommand implements Command {

	private SCV scv;
	private TilePosition constructionSite;
	private ConstructionType constructionType;
	
	public ConstructCommand(SCV scv, TilePosition constructionSite, ConstructionType constructionType) {
		
		this.scv = scv;
		this.constructionSite = constructionSite;
		this.constructionType = constructionType;
	}
	
	@Override
	public boolean execute() {
		
		return constructionType.build(this.scv, this.constructionSite);
	}

	@Override
	public String toString() {
		
		return "command: build " + constructionType + " at " + constructionSite;
	}

	@Override
	public int getDelay() {
		return 3;
	}

}
