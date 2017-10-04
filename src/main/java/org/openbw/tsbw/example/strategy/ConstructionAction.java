package org.openbw.tsbw.example.strategy;

import org.openbw.tsbw.building.BuildingPlanner;
import org.openbw.tsbw.building.Construction;

public class ConstructionAction implements BoAction {

	private BuildingPlanner buildingPlanner;
	private Construction construction;
	
	/* default */ ConstructionAction(BuildingPlanner buildingPlanner, Construction construction) {
		
		this.buildingPlanner = buildingPlanner;
		this.construction = construction;
	}
	
	@Override
	public boolean execute(int availableMinerals, int availableGas, int availableSupply) {
		buildingPlanner.queue(construction);
		return true;
	}

	@Override
	public String toString() {
		return "action: construct " + construction;
	}

}
