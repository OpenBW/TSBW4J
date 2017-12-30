package org.openbw.tsbw.unit;


import org.openbw.bwapi4j.MapDrawer;
import org.openbw.bwapi4j.type.Color;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.CommandCenter;
import org.openbw.bwapi4j.unit.Unit;
import org.openbw.tsbw.Constants;
import org.openbw.tsbw.Group;
import org.openbw.tsbw.MapAnalyzer;

import bwta.Region;


public class MineralPatch extends org.openbw.bwapi4j.unit.MineralPatch {

	private MapAnalyzer mapAnalyzer;
	private Region myRegion;
	private Group<MineralPatch> mineralPatches;
	
	private int assignedScvs;
	private int dyToClosestCC;
	private int dxToClosestCC;
	private double roundTripTimeToClosestCC;
	
	MineralPatch(int id) {
		
		super(id);
		this.dyToClosestCC = Integer.MAX_VALUE;
		this.dxToClosestCC = Integer.MAX_VALUE;
		this.roundTripTimeToClosestCC = Double.MAX_VALUE;
	}

	public void initialize(MapAnalyzer mapAnalyzer, Group<MineralPatch> mineralPatches) {
		
		this.mapAnalyzer = mapAnalyzer;
		this.mineralPatches = mineralPatches;
		this.myRegion = this.mapAnalyzer.getRegion(this.getInitialPosition());
		this.assignedScvs = 0;
	}
	
	public double getRoundTripTime() {
		
		return roundTripTimeToClosestCC;
	}
	
	public double getMiningFactor() {
		
		// friction = empirically determined 'magic function' representing wandering/collision effects
		double friction = assignedScvs * (Math.pow(assignedScvs, 1.215) - 8.5);
		double oldRate = Math.min((assignedScvs) * Constants.MINERALS_PER_ROUNDTRIP/(roundTripTimeToClosestCC + friction), Constants.MINERALS_PER_ROUNDTRIP/Constants.MINING_TIME);
		double newRate = Math.min((assignedScvs+1) * Constants.MINERALS_PER_ROUNDTRIP/(roundTripTimeToClosestCC + friction), Constants.MINERALS_PER_ROUNDTRIP/Constants.MINING_TIME);
		double factor = newRate - oldRate;
		
		return factor;
	}
	
	public void updateDistance(Group<CommandCenter> commandCenters) {
		
		this.dyToClosestCC = Integer.MAX_VALUE;
		this.dxToClosestCC = Integer.MAX_VALUE;
		this.roundTripTimeToClosestCC = Double.MAX_VALUE;
		for (CommandCenter cc : commandCenters) {
			updateDistance(cc);
		}
		
	}
	
	public void updateDistance(CommandCenter commandCenter) {
		
		Region ccRegion = this.mapAnalyzer.getRegion(commandCenter.getPosition());
		
		double roundTripTime;
		double groundDistance;
		int dx = this.getPosition().getX() - commandCenter.getPosition().getX();
		int dy = this.getPosition().getY() - commandCenter.getPosition().getY();
		
		if (ccRegion != null && ccRegion.equals(this.myRegion)) {
			
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
			
			this.mineralPatches.remove(this);
			this.roundTripTimeToClosestCC = roundTripTime;
			this.dxToClosestCC = dx;
			this.dyToClosestCC = dy;
			this.mineralPatches.add(this);
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
	
	@Override
	public int compareTo(Unit otherUnit) {
		
		if (otherUnit instanceof MineralPatch) {
			
			double comparison = ((MineralPatch)otherUnit).getMiningFactor() - this.getMiningFactor();
			if (comparison == 0) {
				
				return super.compareTo((MineralPatch)otherUnit);
			} else {
				
				return (int)Math.signum(comparison);
			}
		}
		return super.compareTo(otherUnit);
	}

	public void resetScvCount() {
		
		this.mineralPatches.remove(this);
		this.assignedScvs = 0;
		this.mineralPatches.add(this);
	}
	
	public void removeScv() {
		
		this.mineralPatches.remove(this);
		this.assignedScvs--;
		this.mineralPatches.add(this);
	}

	public void addScv() {
		
		this.mineralPatches.remove(this);
		this.assignedScvs++;
		this.mineralPatches.add(this);
	}
	
	public int getScvCount() {
		
		return this.assignedScvs;
	}
}
