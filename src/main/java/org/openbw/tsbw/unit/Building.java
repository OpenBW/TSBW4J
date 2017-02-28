package org.openbw.tsbw.unit;

import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.DamageEvaluator;
import org.openbw.tsbw.Squad;

import bwapi.Position;
import bwapi.TilePosition;

public class Building extends PlayerUnit {

	protected int probableConstructionStart;
	
	/* default */ Building(DamageEvaluator damageEvaluator, BWMap bwMap, bwapi.Unit bwUnit, int timeSpotted) {
		super(damageEvaluator, bwMap, bwUnit, timeSpotted);
		this.probableConstructionStart = calculateProbableConstructionStart(bwUnit, timeSpotted);
	}

	public Worker getBuildUnit(Squad<Worker> workerSquad) {

		bwapi.Unit buildUnit = bwUnit.getBuildUnit();
		if (buildUnit != null) {
			return workerSquad.getValue(buildUnit.getID());
		}
		return null;
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
