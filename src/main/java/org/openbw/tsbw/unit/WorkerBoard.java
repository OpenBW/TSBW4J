package org.openbw.tsbw.unit;

import org.openbw.bwapi4j.InteractionHandler;
import org.openbw.tsbw.MapAnalyzer;
import org.openbw.tsbw.UnitInventory;
import org.openbw.tsbw.strategy.ScoutingStrategy;

import co.paralleluniverse.strands.concurrent.ReentrantLock;

public class WorkerBoard {

	private final ReentrantLock lock;
	private boolean token;
	
	private UnitInventory unitInventory;
	private MapAnalyzer mapAnalyzer;
	private InteractionHandler interactionHandler;
	private ScoutingStrategy scoutingStrategy;
	
	public WorkerBoard() {

		this.lock = new ReentrantLock();
		this.token = false;
	}
	
	MapAnalyzer getMapAnalyzer() {
		
		return this.mapAnalyzer;
	}
	
	UnitInventory getUnitInventory() {
		
		return this.unitInventory;
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

	public void initialize(MapAnalyzer mapAnalyzer, UnitInventory unitInventory, InteractionHandler interactionHandler,
			ScoutingStrategy scoutingStrategy) {
		
		this.mapAnalyzer = mapAnalyzer;
		this.unitInventory = unitInventory;
		this.interactionHandler = interactionHandler;
		this.scoutingStrategy = scoutingStrategy;
	}
	
}
