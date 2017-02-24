package org.openbw.tsbw.unit;

import java.util.Queue;

import org.openbw.tsbw.UnitInventory;
import org.openbw.tsbw.building.ConstructionProject;
import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.DamageEvaluator;

import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.Region;

public class SupplyDepot extends Building implements Construction {

	private static SupplyDepot constructionInstance = null;
	
	SupplyDepot(DamageEvaluator damageEvaluator, BWMap bwMap, Unit bwUnit, int timeSpotted) {
		super(damageEvaluator, bwMap, bwUnit, timeSpotted);
	}
	
	private SupplyDepot(BWMap bwMap) {
		super(bwMap, UnitType.Terran_Supply_Depot);
	}
	
	public static Construction getInstance(BWMap bwMap) {
		
		if (constructionInstance == null) {
			constructionInstance = new SupplyDepot(bwMap);
		}
		return constructionInstance;
	}
	
	@Override
	public TilePosition getBuildTile(Worker builder, UnitInventory unitInventory, Queue<ConstructionProject> projects) {
		
		Region region;
		if (unitInventory.getMain() == null) {
			region = BWTA.getRegion(unitInventory.getAvailableWorkers().first().getPosition());
		} else {
			region = BWTA.getRegion(unitInventory.getMain().getPosition());
		}
		TilePosition choke = region.getChokepoints().get(0).getCenter().toTilePosition();
		TilePosition center = region.getCenter().toTilePosition();
		int dx = center.getX() - choke.getX();
		int dy = center.getY() - choke.getY();
		double d = Math.sqrt(dx * dx + dy * dy);
		
		TilePosition position = new TilePosition((int)(center.getX() + dx * 10 / d), (int)(center.getY() + dy * 10 / d));
		TilePosition nextPosition = position;
		
		for (int i = 0; !bwMap.canBuildHere(nextPosition, this.unitType, true); i++) {
			for (int j = 1; j <= i; j++) {
				
				int x = i/2 * ((i%2 * 2) - 1);
				int y = j/2 * ((j%2 * 2) - 1);
				nextPosition = new TilePosition(position.getX() + x, position.getY() + y);
				if (bwMap.canBuildHere(nextPosition, this.unitType, true) && !collidesWithConstruction(nextPosition, projects)) {
					
					return nextPosition;
				}
			}
		}
		
		return nextPosition;
	}
}
