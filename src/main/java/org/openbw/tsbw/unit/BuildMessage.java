package org.openbw.tsbw.unit;

import org.openbw.bwapi4j.TilePosition;
import org.openbw.tsbw.building.ConstructionType;

class BuildMessage extends Message {

	private TilePosition constructionSite;
	private ConstructionType type;
	
	public BuildMessage(TilePosition constructionSite, ConstructionType type) {
		
		super("");
		this.constructionSite = constructionSite;
		this.type = type;
	}

	public TilePosition getConstructionSite() {
		
		return this.constructionSite;
	}

	public ConstructionType getType() {
		
		return this.type;
	}
}
