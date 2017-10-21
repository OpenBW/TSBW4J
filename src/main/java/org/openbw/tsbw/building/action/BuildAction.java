package org.openbw.tsbw.building.action;

import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.unit.SCV;
import org.openbw.tsbw.building.ConstructionType;

public class BuildAction implements Action {

	private SCV scv;
	private TilePosition constructionSite;
	private ConstructionType constructionType;
	
	public BuildAction(SCV scv, TilePosition constructionSite, ConstructionType constructionType) {
		
		this.scv = scv;
		this.constructionSite = constructionSite;
		this.constructionType = constructionType;
	}
	
	@Override
	public boolean execute() {
		
		return constructionType.build(this.scv, this.constructionSite);
	}

}
