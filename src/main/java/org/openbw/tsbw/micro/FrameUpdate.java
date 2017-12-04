package org.openbw.tsbw.micro;

import java.util.ArrayList;
import java.util.List;

import org.openbw.bwapi4j.unit.MobileUnit;
import org.openbw.tsbw.UnitInventory;

public class FrameUpdate extends Message {

	private int frame;
	private int remainingLatencyFrames;
	private List<MobileUnit> enemyUnits;
	
	public FrameUpdate(int frame, int remainingLatencyFrames, UnitInventory enemyInventory) {
		
		super("");
		this.frame = frame;
		this.remainingLatencyFrames = remainingLatencyFrames;
		this.enemyUnits = new ArrayList<MobileUnit>();
		this.enemyUnits.addAll(enemyInventory.getArmyUnits());
		this.enemyUnits.addAll(enemyInventory.getWorkers());
	}
	
	public int getFrame() {
		
		return this.frame;
	}
	
	public int getRemainingLatencyFrames() {
		
		return this.remainingLatencyFrames;
	}
	
	public List<MobileUnit> getEnemyUnits() {
		
		return this.enemyUnits;
	}
}
