package org.openbw.tsbw.unit;

import java.util.Queue;

import org.openbw.tsbw.Squad;
import org.openbw.tsbw.UnitInventory;
import org.openbw.tsbw.building.ConstructionProject;
import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.DamageEvaluator;

import bwapi.Position;
import bwapi.Region;
import bwapi.TilePosition;
import bwapi.UnitType;

public class Building extends PlayerUnit implements Construction {

	protected int probableConstructionStart;
	
	/* default */ Building(DamageEvaluator damageEvaluator, BWMap bwMap, bwapi.Unit bwUnit, int timeSpotted) {
		super(damageEvaluator, bwMap, bwUnit, timeSpotted);
		this.probableConstructionStart = calculateProbableConstructionStart(bwUnit, timeSpotted);
	}

	/* default */ Building(BWMap bwMap, UnitType unitType) {
		super();
		super.bwMap = bwMap;
		super.unitType = unitType;
	}
	
	public Worker getBuildUnit(Squad<Worker> workerSquad) {

		bwapi.Unit buildUnit = bwUnit.getBuildUnit();
		if (buildUnit != null) {
			return workerSquad.getValue(buildUnit.getID());
		}
		return null;
	}

	protected boolean collidesWithConstruction(TilePosition position, Queue<ConstructionProject> projects) {
		
		for (ConstructionProject project : projects) {
			
			TilePosition site = project.getConstructionSite();
			Construction construction = project.getConstruction();
			
			if (site != null && construction != null) {
				if (site.getX() + construction.tileWidth() > position.getX() &&  site.getX() < position.getX() + construction.tileWidth()
						&& site.getY() + construction.tileHeight() > position.getY() && site.getY() < position.getY() + construction.tileHeight()) {
					
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public TilePosition getBuildTile(Worker builder, UnitInventory unitInventory, Queue<ConstructionProject> projects) {
		
		Region region;
		if (unitInventory.getMain() == null) {
			region = bwMap.getRegionAt(unitInventory.getAvailableWorkers().first().getPosition());
		} else {
			region = bwMap.getRegionAt(unitInventory.getMain().getPosition());
		}
		TilePosition position = region.getCenter().toTilePosition();
		
		return getBuildTile(builder, position, unitInventory, projects);
	}
	
	@Override
	public TilePosition getBuildTile(Worker builder, TilePosition aroundHere, UnitInventory unitInventory, Queue<ConstructionProject> projects) {
		
		TilePosition nextPosition = aroundHere;
		
		for (int i = 0; !bwMap.canBuildHere(nextPosition, this.unitType, true); i++) {
			for (int j = 1; j <= i; j++) {
				
				int x = i/2 * ((i%2 * 2) - 1);
				int y = j/2 * ((j%2 * 2) - 1);
				nextPosition = new TilePosition(aroundHere.getX() + x, aroundHere.getY() + y);
				if (bwMap.canBuildHere(nextPosition, this.unitType, true) && !collidesWithConstruction(nextPosition, projects)) {
					
					return nextPosition;
				}
			}
		}
		
		return nextPosition;
	}

	public int buildTime() {
		return this.unitType.buildTime();
	}

	private int calculateProbableConstructionStart(bwapi.Unit bwUnit, int currentFrame) {
		
		int time = currentFrame;
		if (bwUnit.isCompleted()) {
			time = currentFrame - unitType.buildTime();
		} else {
			time = currentFrame - (bwUnit.getHitPoints() / bwUnit.getType().maxHitPoints()) * unitType.buildTime();
		}
		return time;
	}

	public boolean isCompleted() {
		return bwUnit.isCompleted();
	}
	
	public int getProbableConstructionStart() {
		return probableConstructionStart;
	}

	@Override
	public boolean build(Worker worker, TilePosition constructionSite) {
		return worker.build(this.unitType, constructionSite);
	}

	@Override
	public int tileHeight() {
		return super.unitType.tileHeight();
	}

	@Override
	public int tileWidth() {
		return super.unitType.tileWidth();
	}
	
	public int getDistance(TilePosition position) {
		
		// compute x distance
		int xDist = super.getLastKnownTilePosition().getX() - position.getX();
		if (xDist < 0) {
			xDist = position.getX() - (super.getLastKnownTilePosition().getX() + this.unitType.tileWidth());
			if (xDist < 0)
				xDist = 0;
		}

		// compute y distance
		int yDist = super.getLastKnownTilePosition().getY() - position.getY();
		if (yDist < 0) {
			yDist = position.getY() - (super.getLastKnownTilePosition().getY() + this.unitType.tileHeight());
			if (yDist < 0) {
				yDist = 0;
			}
		}
		return (int)Math.sqrt(xDist * xDist + yDist * yDist);
	}

	public int getDistance(Position position) {
		
		int left = position.getX() - 1;
		int top = position.getY() - 1;
		int right = position.getX() + 1;
		int bottom = position.getY() + 1;

		// compute x distance
		int xDist = (super.getLastKnownPosition().getX() - this.unitType.dimensionLeft()) - right;
		if (xDist < 0) {
			xDist = left - (super.getLastKnownPosition().getX() + this.unitType.dimensionRight());
			if (xDist < 0)
				xDist = 0;
		}

		// compute y distance
		int yDist = (super.getLastKnownPosition().getY() - this.unitType.dimensionUp()) - bottom;
		if (yDist < 0) {
			yDist = top - (super.getLastKnownPosition().getY() + this.unitType.dimensionDown());
			if (yDist < 0) {
				yDist = 0;
			}
		}
		return (int)Math.sqrt(xDist * xDist + yDist * yDist);
	}
	
	public int getDistance(Unit pUnit) {

		int left = pUnit.getLeft() - 1;
		int top = pUnit.getTop() - 1;
		int right = pUnit.getRight() + 1;
		int bottom = pUnit.getBottom() + 1;

		// compute x distance
		int xDist = (super.getLastKnownPosition().getX() - this.unitType.dimensionLeft()) - right;
		if (xDist < 0) {
			xDist = left - (super.getLastKnownPosition().getX() + this.unitType.dimensionRight());
			if (xDist < 0)
				xDist = 0;
		}

		// compute y distance
		int yDist = (super.getLastKnownPosition().getY() - this.unitType.dimensionUp()) - bottom;
		if (yDist < 0) {
			yDist = top - (super.getLastKnownPosition().getY() + this.unitType.dimensionDown());
			if (yDist < 0) {
				yDist = 0;
			}
		}
		return (int)Math.sqrt(xDist * xDist + yDist * yDist);
	}
}
