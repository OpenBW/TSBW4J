package org.openbw.tsbw.unit;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.unit.Building;
import org.openbw.bwapi4j.unit.Bunker;
import org.openbw.bwapi4j.unit.MobileUnit;
import org.openbw.tsbw.UnitInventory;

public class FrameUpdate extends Message {

	private int frame;
	private int minerals;
	private int gas;
	private int remainingLatencyFrames;
	private List<MobileUnit> enemyUnits;
	private List<MobileUnit> attackingEnemies;
	private List<Building> buildingsToRepair;
	
	public FrameUpdate(int frame, int minerals, int gas, int remainingLatencyFrames, UnitInventory myInventory, UnitInventory enemyInventory) {
		
		super("");
		this.frame = frame;
		this.minerals = minerals;
		this.gas = gas;
		this.remainingLatencyFrames = remainingLatencyFrames;
		this.enemyUnits = new ArrayList<MobileUnit>();
		this.enemyUnits.addAll(enemyInventory.getArmyUnits());
		this.enemyUnits.addAll(enemyInventory.getWorkers());
		
		MineralPatch nearestPatch = myInventory.getMineralPatches().stream().min((u1, u2) -> Double.compare(
        		u1.getDistance(myInventory.getMain().getPosition()), 
        		u2.getDistance(myInventory.getMain().getPosition()))).get();
		Position defensePosition = nearestPatch.getMiddle(myInventory.getMain());
		
		this.attackingEnemies = this.enemyUnits.stream().filter(
				e -> e.getDistance(defensePosition) < 192 &&
				e.isAttacking()).collect(Collectors.toList());
		
		this.buildingsToRepair = myInventory.getBuildings().stream().filter(b -> b instanceof Bunker && b.getHitPoints() < b.maxHitPoints())
					.map(b -> (Building)b).collect(Collectors.toList());
	}
	
	public int getFrame() {
		
		return this.frame;
	}
	
	public int getMinerals() {
		
		return this.minerals;
	}
	
	public int getGas() {
		
		return this.gas;
	}
	
	public int getRemainingLatencyFrames() {
		
		return this.remainingLatencyFrames;
	}
	
	public List<Building> getBuildingsToRepair() {
	
		return this.buildingsToRepair;
	}
	
	public List<MobileUnit> getAttackingUnits() {
		
		return this.attackingEnemies;
	}
	
	public List<MobileUnit> getEnemyUnits() {
		
		return this.enemyUnits;
	}
}
