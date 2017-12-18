package org.openbw.tsbw.unit;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.openbw.bwapi4j.InteractionHandler;
import org.openbw.bwapi4j.unit.MobileUnit;
import org.openbw.tsbw.MapAnalyzer;
import org.openbw.tsbw.UnitInventory;
import org.openbw.tsbw.strategy.ScoutingStrategy;

import co.paralleluniverse.strands.concurrent.ReentrantLock;

public class WorkerBoard {

	private final ReentrantLock lock;
	private boolean token;
	
	private UnitInventory myInventory;
	private MapAnalyzer mapAnalyzer;
	private InteractionHandler interactionHandler;
	private ScoutingStrategy scoutingStrategy;
	
	private Map<MobileUnit, Set<SCV>> attackers;
	
	public WorkerBoard() {

		this.lock = new ReentrantLock();
		this.token = false;
		this.attackers = new HashMap<>();
	}
	
	Map<MobileUnit, Set<SCV>> getAttackers() {
	
		return this.attackers;
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
