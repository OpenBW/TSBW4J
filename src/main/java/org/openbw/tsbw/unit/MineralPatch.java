package org.openbw.tsbw.unit;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbw.bwapi4j.MapDrawer;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.type.Color;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.CommandCenter;
import org.openbw.bwapi4j.unit.Unit;
import org.openbw.tsbw.Constants;
import org.openbw.tsbw.MapAnalyzer;


public class MineralPatch extends org.openbw.bwapi4j.unit.MineralPatch {

	private static final Logger logger = LogManager.getLogger();
	
	public enum Status {BEING_MINED, FREE};
	
	private Status status;
	
	private int assignedScvs;
	private Position myRegionCenter;
	
	private CommandCenter closestCommandCenter;
	private int dyToClosestCC = Integer.MAX_VALUE;
	private int dxToClosestCC = Integer.MAX_VALUE;
	private double roundTripTimeToClosestCC = Double.MAX_VALUE;
	
	/* default */ MineralPatch(int id) {
		super(id);
		this.status = Status.FREE;
		this.assignedScvs = 0;
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
	
	public void resetDistances() {
		this.closestCommandCenter = null;
		this.dyToClosestCC = Integer.MAX_VALUE;
		this.dxToClosestCC = Integer.MAX_VALUE;
		this.roundTripTimeToClosestCC = Double.MAX_VALUE;
	}
	
	public void updateDistance(MapAnalyzer mapAnalyzer, CommandCenter commandCenter, boolean wipeScvCount) {
		
		if (wipeScvCount) {
			this.assignedScvs = 0;
		}
		
		if (this.myRegionCenter == null) {
			this.myRegionCenter = mapAnalyzer.getRegionCenter(this.getTilePosition());
			if (this.myRegionCenter == null) {
				logger.error("Could not get region for {} at {}", this, this.getTilePosition());
			}
		}
		Position commandCenterRegionCenter = mapAnalyzer.getRegionCenter(commandCenter.getTilePosition());
		
		double roundTripTime;
		double groundDistance;
		int dx = this.getPosition().getX() - commandCenter.getPosition().getX();
		int dy = this.getPosition().getY() - commandCenter.getPosition().getY();
		
		if (myRegionCenter != null && myRegionCenter.equals(commandCenterRegionCenter)) {
			
			groundDistance = this.getDistance(commandCenter);
			
			double accelerationDistance = 48.4128;
			double turnPenalty = 20.0;
			double timeAccelerating = 4 * accelerationDistance / UnitType.Terran_SCV.topSpeed();
			double topSpeedTime = (groundDistance * 2 - accelerationDistance * 2) / UnitType.Terran_SCV.topSpeed();
			roundTripTime = topSpeedTime + timeAccelerating + Constants.MINING_TIME + turnPenalty;
			
		} else {
			
			groundDistance = (int)mapAnalyzer.getGroundDistance(commandCenter.getTilePosition(), this.getTilePosition());
			roundTripTime = groundDistance * 2 / UnitType.Terran_SCV.topSpeed() + Constants.MINING_TIME;
		}
		
		if (groundDistance > 0 && roundTripTime < this.roundTripTimeToClosestCC) {
			
			this.closestCommandCenter = commandCenter;
			this.roundTripTimeToClosestCC = roundTripTime;
			this.dxToClosestCC = dx;
			this.dyToClosestCC = dy;
		}
	}

	public void drawInfo(MapDrawer mapDrawer) {
		mapDrawer.drawBoxMap(
				this.getX() - UnitType.Resource_Mineral_Field.width()  / 2, 
				this.getY() - UnitType.Resource_Mineral_Field.height() / 2, 
				this.getX() + UnitType.Resource_Mineral_Field.width()  / 2, 
				this.getY() + UnitType.Resource_Mineral_Field.height() / 2, Color.RED);
		
		mapDrawer.drawLineMap(this.getX() + UnitType.Resource_Mineral_Field.width() / 2, this.getY(), this.getX() + UnitType.Resource_Mineral_Field.width() / 2 + (int)this.getRoundTripTime(), 
				this.getY(), Color.YELLOW);
		
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
