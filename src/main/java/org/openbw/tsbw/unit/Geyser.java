package org.openbw.tsbw.unit;

import org.openbw.bwapi.BWMap;

import bwapi.Position;
import bwapi.TilePosition;

public class Geyser extends Unit {

	private int distanceToClosestCC;
	private int lastKnownResources;
	
	Geyser(bwapi.Unit bwUnit, BWMap bwMap) {
		super(bwUnit, bwMap, 0);
		this.distanceToClosestCC = Integer.MAX_VALUE;
		this.lastKnownResources = bwUnit.getResources();
	}

	@Override
	public int compareTo(Unit otherUnit) {
		
		if (otherUnit instanceof Geyser) {
			int comparison = this.distanceToClosestCC - ((Geyser)otherUnit).distanceToClosestCC;
			if (comparison == 0) {
				return super.compareTo(otherUnit);
			} else {
				return comparison;
			}
		} else {
			return super.compareTo(otherUnit);
		}
	}

	public void updateDistance(CommandCenter commandCenter) {
		
		int distance = this.getDistance(commandCenter);
		if (distance < this.distanceToClosestCC) {
			this.distanceToClosestCC = distance;
		}
	}
	
	public void update(int frame, int resources) {
		this.lastSpotted = frame;
		this.lastKnownResources = resources;
	}
	
	public int getLastKnownResources() {
		return this.lastKnownResources;
	}
	
	/**
	 * Always returns the initial position, since geysers cannot move.
	 */
	public Position getPosition() {
		return super.bwUnit.getInitialPosition();
	}
	
	/**
	 * Always returns the initial position, since geysers cannot move.
	 */
	public TilePosition getTilePosition() {
		return super.bwUnit.getInitialTilePosition();
	}
}
