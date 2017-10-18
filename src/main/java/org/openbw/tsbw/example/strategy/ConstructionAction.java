package org.openbw.tsbw.example.strategy;

import org.openbw.tsbw.building.BuildingPlanner;
import org.openbw.tsbw.building.ConstructionType;

public class ConstructionAction implements BoAction {

	private BuildingPlanner buildingPlanner;
	private ConstructionType constructionType;
	
	/* default */ ConstructionAction(BuildingPlanner buildingPlanner, ConstructionType constructionType) {
		
		this.buildingPlanner = buildingPlanner;
		this.constructionType = constructionType;
	}
	
	@Override
	public boolean execute(int availableMinerals, int availableGas, int availableSupply) {
		buildingPlanner.queue(constructionType);
		return true;
	}

	@Override
	public String toString() {
		return "action: construct " + constructionType;
	}

}
