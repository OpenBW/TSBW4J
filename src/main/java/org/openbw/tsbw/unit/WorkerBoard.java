package org.openbw.tsbw.unit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openbw.bwapi4j.InteractionHandler;
import org.openbw.bwapi4j.unit.Building;
import org.openbw.bwapi4j.unit.Mechanical;
import org.openbw.bwapi4j.unit.MobileUnit;
import org.openbw.tsbw.MapAnalyzer;
import org.openbw.tsbw.UnitInventory;
import org.openbw.tsbw.strategy.ScoutingStrategy;

import co.paralleluniverse.strands.concurrent.ReentrantLock;

public class WorkerBoard {

	private final ReentrantLock lock;
	private final ReentrantLock repairLock;
	private boolean token;
	
	private UnitInventory myInventory;
	private MapAnalyzer mapAnalyzer;
	private InteractionHandler interactionHandler;
	private ScoutingStrategy scoutingStrategy;
	
	private Map<MobileUnit, Set<SCV>> attackers;
	private Map<Building, Set<SCV>> repairs;
	
	public WorkerBoard() {

		this.lock = new ReentrantLock();
		this.repairLock = new ReentrantLock();
		this.token = false;
		this.attackers = new HashMap<>();
		this.repairs = new HashMap<>();
	}
	
	Map<MobileUnit, Set<SCV>> getAttackers() {
	
		return this.attackers;
	}
	
	boolean addRepair(Building mechanical, SCV worker) {
		
		this.repairLock.lock();
		
		boolean repairNeeded = false;
		
		Set<SCV> repairWorkers = this.repairs.get(mechanical);
		if (repairWorkers == null) {
			
			repairWorkers = new HashSet<>();
			this.repairs.put(mechanical, repairWorkers);
		}
		if (repairWorkers.size() < 2) {
			
			repairWorkers.add(worker);
			repairNeeded = true;
		}
		
		this.repairLock.unlock();
		
		return repairNeeded;
	}
	
	void removeRepair(Mechanical mechanical, SCV worker) {
		
		this.repairs.get(mechanical).remove(worker);
	}
	
	MapAnalyzer getMapAnalyzer() {
		
		return this.mapAnalyzer;
	}
	
	UnitInventory getMyInventory() {
		
		return this.myInventory;
	}

	InteractionHandler getInteractionHandler() {
		
		return this.interactionHandler;
	}
	
	ScoutingStrategy getScoutingStrategy() {
		
		return this.scoutingStrategy;
	}
	
	boolean reserveToken() {
		
		boolean success;
		
		try {
			this.lock.lock();
			
			if (this.token) {
				success = false;
			} else {
				this.token = true;
				success = true;
			}
			
		} finally {
			this.lock.unlock();
		}
		
		return success;
	}
	
	void releaseToken() {
		
		try {
			this.lock.lock();
			
			this.token = false;
			
		} finally {
			this.lock.unlock();
		}
	}

	public void initialize(MapAnalyzer mapAnalyzer, UnitInventory myInventory, InteractionHandler interactionHandler,
			ScoutingStrategy scoutingStrategy) {
		
		this.mapAnalyzer = mapAnalyzer;
		this.myInventory = myInventory;
		this.interactionHandler = interactionHandler;
		this.scoutingStrategy = scoutingStrategy;
	}
	
}
