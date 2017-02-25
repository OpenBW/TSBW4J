package org.openbw.tsbw.unit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.MapDrawer;
import org.openbw.tsbw.Constants;

import bwapi.Color;
import bwapi.Position;
import bwapi.Region;
import bwapi.TilePosition;
import bwapi.UnitType;

public class MineralPatch extends Unit {

	private static final Logger logger = LogManager.getLogger();
	
	public enum Status {BEING_MINED, FREE};
	
	private Region myRegion;
	private Status status;
	
	private int assignedScvs;
	private int lastKnownResources;
	
	private CommandCenter closestCommandCenter;
	private int dyToClosestCC = Integer.MAX_VALUE;
	private int dxToClosestCC = Integer.MAX_VALUE;
	private double roundTripTimeToClosestCC = Double.MAX_VALUE;
	
	/* default */ MineralPatch(bwapi.Unit bwUnit, BWMap bwMap) {
		super(bwUnit, bwMap, 0);
		this.status = Status.FREE;
		this.assignedScvs = 0;
		this.lastKnownResources = bwUnit.getResources();
		this.myRegion = bwMap.getRegionAt(bwUnit.getPosition());
		if (this.myRegion == null) {
			logger.error("Could not get region for patch {} at {}.", bwUnit.getID(), bwUnit.getPosition());
		}
	}

	public boolean isBeingGathered() {
		return bwUnit.isBeingGathered();
	}
	
	public void update(int frame, int resources) {
		this.lastSpotted = frame;
		this.lastKnownResources = resources;
	}
	
	public int getLastKnownResources() {
		return this.lastKnownResources;
	}
	
	public int getResources() {
		return bwUnit.getResources();
	}
	
	public int getInitialResources() {
		return bwUnit.getInitialResources();
	}
	
	public Status getStatus() {
		return this.status;
	}
	
	public void setStatus(Status status) {
		this.status = status;
	}
	
	public double getRoundTripTime() {
		return roundTripTimeToClosestCC;
	}
	
	public CommandCenter getClosestCommandCenter() {
		return this.closestCommandCenter;
	}
	
	public double getMiningFactor() {
		
		// friction = empirically determined 'magic function' representing wandering/collision effects
		double friction = assignedScvs * (Math.pow(assignedScvs, 1.215) - 8.5);
		double oldRate = Math.min((assignedScvs) * Constants.MINERALS_PER_ROUNDTRIP/(roundTripTimeToClosestCC + friction), Constants.MINERALS_PER_ROUNDTRIP/Constants.MINING_TIME);
		double newRate = Math.min((assignedScvs+1) * Constants.MINERALS_PER_ROUNDTRIP/(roundTripTimeToClosestCC + friction), Constants.MINERALS_PER_ROUNDTRIP/Constants.MINING_TIME);
		double factor = newRate - oldRate;
		
		return factor;
	}
	
	public void updateDistance(CommandCenter commandCenter, boolean wipeScvCount) {
		
		if (wipeScvCount) {
			this.assignedScvs = 0;
		}

		double roundTripTime;
		double groundDistance;
		int dx = bwUnit.getPosition().getX() - commandCenter.getPosition().getX();
		int dy = bwUnit.getPosition().getY() - commandCenter.getPosition().getY();
		
		groundDistance = this.getDistance(commandCenter);
		
		double accelerationDistance = 48.4128;
		double turnPenalty = 20.0;
		double timeAccelerating = 4 * accelerationDistance / UnitType.Terran_SCV.topSpeed();
		double topSpeedTime = (groundDistance * 2 - accelerationDistance * 2) / UnitType.Terran_SCV.topSpeed();
		roundTripTime = topSpeedTime + timeAccelerating + Constants.MINING_TIME + turnPenalty;
		
		if (groundDistance > 0 && roundTripTime < this.roundTripTimeToClosestCC) {
			
			this.closestCommandCenter = commandCenter;
			this.roundTripTimeToClosestCC = roundTripTime;
			this.dxToClosestCC = dx;
			this.dyToClosestCC = dy;
		}
	}

	/**
	 * Always returns the initial position, since mineral patches cannot move.
	 */
	public Position getPosition() {
		return super.bwUnit.getInitialPosition();
	}
	
	/**
	 * Always returns the initial tile position, since mineral patches cannot move.
	 */
	public TilePosition getTilePosition() {
		return super.bwUnit.getInitialTilePosition();
	}
	
	public void drawInfo(MapDrawer mapDrawer) {
		mapDrawer.drawBoxMap(
				this.getX() - UnitType.Resource_Mineral_Field.width()  / 2, 
				this.getY() - UnitType.Resource_Mineral_Field.height() / 2, 
				this.getX() + UnitType.Resource_Mineral_Field.width()  / 2, 
				this.getY() + UnitType.Resource_Mineral_Field.height() / 2, Color.Red);
		
		mapDrawer.drawLineMap(this.getX() + UnitType.Resource_Mineral_Field.width() / 2, this.getY(), this.getX() + UnitType.Resource_Mineral_Field.width() / 2 + (int)this.getRoundTripTime(), 
				this.getY(), Color.Yellow);
		
		int x = this.getPosition().getX();
		int y = this.getPosition().getY();
		mapDrawer.drawTextMap(x - 40, y - 15, "SCVs: " + this.assignedScvs);
		mapDrawer.drawTextMap(x - 40, y -  5, "rt: " + (int)this.roundTripTimeToClosestCC);
		mapDrawer.drawTextMap(x - 40, y +  5, "dx: " + this.dxToClosestCC + "; dy: " + this.dyToClosestCC);
	}
	
	/**
	 * {@link Comparable#compareTo(Object)}
	 * Note: this class has a natural ordering that is inconsistent with equals.
	 */
	@Override
	public int compareTo(Unit otherUnit) {
		if (otherUnit instanceof MineralPatch) {
			double comparison = ((MineralPatch)otherUnit).getMiningFactor() - this.getMiningFactor();
			if (comparison == 0) {
				return super.compareTo(otherUnit);
			} else {
				return (int)Math.signum(comparison);
			}
		} else {
			return super.compareTo(otherUnit);
		}
	}

	public void removeScv() {
		this.assignedScvs--;
	}

	public void addScv() {
		this.assignedScvs++;
	}
}
