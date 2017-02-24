package org.openbw.tsbw.building;


import java.util.Queue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.openbw.tsbw.Constants;
import org.openbw.tsbw.Squad;
import org.openbw.tsbw.UnitInventory;
import org.openbw.tsbw.analysis.PPF2;
import org.openbw.tsbw.unit.Building;
import org.openbw.tsbw.unit.Construction;
import org.openbw.tsbw.unit.Worker;
import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.InteractionHandler;

import bwapi.TilePosition;
import bwta.BWTA;

public class ConstructionProject {

	private static final Logger logger = LogManager.getLogger();
	
	public enum Status {QUEUED, ABOUT_TO_CONSTRUCT, CONSTRUCTING, COMPLETED}
	
	private int id;
	private InteractionHandler interactionHandler;
	private Status status;
	private Worker assignedWorker = null;
	private TilePosition constructionSite = null;
	
	private Construction construction;
	
	/* default */ ConstructionProject(int id, Construction construction, InteractionHandler interactionHandler) {
		this.status = Status.QUEUED;
		this.id = id;
		this.construction = construction;
		this.interactionHandler = interactionHandler;
	}
	
	/* default */ ConstructionProject(int id, Construction construction, InteractionHandler interactionHandler, TilePosition constructionSite) {
		this(id, construction, interactionHandler);
		this.constructionSite = constructionSite;
	}

	/* default */ void setStatus(Status status) {
		this.status = status;
	}
	
	/* default */ int getMineralPrice() {
		return this.construction.getMineralPrice();
	}
	
	/* default */ int getGasPrice() {
		return this.construction.getGasPrice();
	}
	
	/* default */ void findConstructionSite(UnitInventory unitInventory, Queue<ConstructionProject> projects) {
		
		this.constructionSite = this.construction.getBuildTile(this.assignedWorker, unitInventory, projects);
	}
	
	/* default */ boolean hasConstructionSite() {
		return this.constructionSite != null;
	}
	
	/* default */ boolean hasAssignedWorker() {
		return this.assignedWorker != null;
	}
	
	/* default */ Worker getAssignedWorker() {
		return this.assignedWorker;
	}
	
	/* default */ void releaseWorker() {
		this.assignedWorker = null;
	}
	
	/* default */ void releaseConstructionSite() {
		this.constructionSite = null;
	}
	
	/* default */ void setAssignedWorker(Worker worker) {
		this.assignedWorker = worker;
	}
	
	/**
	 * Trivial algorithm: find the closest worker to the construction site of all workers not yet constructing anything.
	 * @param constructionSite
	 * @return suitable builder
	 */
	/* default */ Worker findSuitableWorker(Squad<Worker> workerSquad) {
		
		double distance = Double.MAX_VALUE;
		Worker builder = null;
		
		if (hasConstructionSite()) {
		
			for (Worker worker : workerSquad) {
				double currentDistance = BWTA.getGroundDistance(worker.getTilePosition(), constructionSite);
				if (currentDistance < distance) {
					distance = currentDistance;
					builder = worker;
				}
			}
		}
		assignedWorker = builder;
		return builder;
	}
	
	/* default */ boolean build() {
		
		boolean success = this.construction.build(this.assignedWorker, this.constructionSite);
		if (!success) {
			logger.warn("Could not build {} at {}: {}", this.construction, this.constructionSite, interactionHandler.getLastError());
		}
		return success;
	}
	
	/* default */ boolean moveWorkerToSite() {
		
		if (this.assignedWorker == null || constructionSite == null) {
			return false;
		} else {
			logger.debug("Moving worker {} to {} to build {}...", this.assignedWorker.getID(), this.constructionSite, this.construction);
			return this.assignedWorker.move(constructionSite.toPosition());
		}
	}
	
	/**
	 * Estimates how much minerals are mined while the assigned worker travels to the construction site, assuming workerCount mining workers.
	 * @param assignedWorker
	 * @param suitableConstructionSite
	 * @param remainingMiningWorkerCount usually = number of mining workers - 1 because a mining worker is pulled to construct a building
	 * @return estimated minerals mined during travel time
	 */
	/* default */ int estimateMineralsMinedDuringTravel(int remainingMiningWorkerCount) {
		
		double distance = BWTA.getGroundDistance(this.assignedWorker.getTilePosition(), this.constructionSite);
		double travelTimetoConstructionSite = distance / Constants.AVERAGE_SCV_SPEED;
		int estimatedMiningDuringTravel = (int)PPF2.calculateEstimatedMining((int)travelTimetoConstructionSite, remainingMiningWorkerCount);
		
		return estimatedMiningDuringTravel;
	}

	/* default */ boolean isConstructionSiteVisible(BWMap bwMap) {
		
		boolean visible = true;
		for (int i = 0; i < construction.tileWidth(); i++) {
			for (int j = 0; j < construction.tileHeight(); j++) {
				
				TilePosition position = new TilePosition(this.constructionSite.getX() + i, this.constructionSite.getY() + j);
				if (!bwMap.isVisible(position)) {
					visible = false;
				}
			}
		}
		return visible;
	}
	
	/* default */ boolean hasBuilt(Building building) {
		
		if (this.constructionSite == null) {
			return false;
		} else {
			return this.constructionSite.equals(building.getTilePosition());
		}
	}

	public Status getStatus() {
		return this.status;
	}
	public TilePosition getConstructionSite() {
		return this.constructionSite;
	}
	public void setConstructionSite(TilePosition site) {
		this.constructionSite = site;
	}
	public Construction getConstruction() {
		return this.construction;
	}
	public int getId() {
		return id;
	}
	
	@Override
	public String toString() {
		return construction.toString() + ": " + this.status;
	}
	
	

}
