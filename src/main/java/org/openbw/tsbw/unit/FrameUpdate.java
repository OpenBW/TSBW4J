package org.openbw.tsbw.unit;

import java.util.ArrayList;
import java.util.List;

import org.openbw.bwapi4j.unit.MobileUnit;
import org.openbw.tsbw.UnitInventory;

public class FrameUpdate extends Message {

	private int frame;
	private int minerals;
	private int gas;
	private int remainingLatencyFrames;
	private List<MobileUnit> enemyUnits;
	
	public FrameUpdate(int frame, int minerals, int gas, int remainingLatencyFrames, UnitInventory enemyInventory) {
		
		super("");
		this.frame = frame;
		this.minerals = minerals;
		this.gas = gas;
		this.remainingLatencyFrames = remainingLatencyFrames;
		this.enemyUnits = new ArrayList<MobileUnit>();
		this.enemyUnits.addAll(enemyInventory.getArmyUnits());
		this.enemyUnits.addAll(enemyInventory.getWorkers());
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
	
	public List<MobileUnit> getEnemyUnits() {
		
		return this.enemyUnits;
	}
}
