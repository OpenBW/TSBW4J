package org.openbw.tsbw.building;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.openbw.tsbw.GroupListener;
import org.openbw.tsbw.Squad;
import org.openbw.tsbw.UnitInventory;
import org.openbw.tsbw.unit.Building;
import org.openbw.tsbw.unit.Construction;
import org.openbw.tsbw.unit.Worker;
import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.InteractionHandler;

import bwapi.TilePosition;

/**
 * Manages all building construction for the game.
 * Building can be queued for construction. Every iteration, the planner attempts to find a suitable worker and location. If successful,
 * the worker is sent to construct the building and the building changes its state from "queued" to "about to construct".
 * Once construction has actually started the building changes its state from "about to construct" to "constructing".
 * In the "queued" and "about to construct" state, the required resources to build are locked, such that they will not be spent by someone else
 * before construction can start. Once construction has started the resources are released, because the game has made the payment.
 * This mechanism simulates SC2 behavior, where the payment is made already when ordering a worker to construct and refunded if construction cannot be started.
 */
// TODO the implementation is not robust yet. handle cases:
// - worker gets killed after assignment but before arriving at construction site
// - worker gets killed while constructing
// - construction site gets unavailable after worker assignment
// - building gets destroyed while constructing
public class BuildingPlanner  {

	private static final Logger logger = LogManager.getLogger();
	
	private UnitInventory unitInventory;
	private InteractionHandler interactionHandler;
	private BWMap bwMap;
	private int uniqueIdCounter;
	
	private GroupListener<Building> buildingListener = new GroupListener<Building>() {
		
		@Override
		public void onAdd(Building building) {
			if (building == null) {
				logger.warn("attempting to add null building");
			}
			for (ConstructionProject project : queue) {
				if (project.hasBuilt(building)) {
					Worker worker = project.getAssignedWorker();
					constructorSquad.move(worker, unitInventory.getAvailableWorkers());
					project.setStatus(ConstructionProject.Status.COMPLETED);
					queue.remove(project);
					completed.add(project);
					return;
				}
			}
			if (building != unitInventory.getMain()) {
				logger.warn("was informed that {} is completed, but can't find the building", building);
			}
		}

		@Override
		public void onRemove(Building unit) {
			
			// do nothing
		}
		
		@Override
		public void onDestroy(Building unit) {
			
			// do nothing
		}
	};
	
	private GroupListener<Worker> constructorsListener = new GroupListener<Worker>() {

		@Override
		public void onAdd(Worker worker) {
			
			// do nothing
		}

		@Override
		public void onRemove(Worker worker) {
			
			// do nothing
		}

		@Override
		public void onDestroy(Worker unit) {
			
			// TODO plan-repair! worker was killed while constructing something
		}
		
	};
	
	private Queue<ConstructionProject> queue;
	private Queue<ConstructionProject> completed;
	
	private Squad<Worker> constructorSquad = null;

	private Squad<Worker> availableWorkers = null;
	
	public BuildingPlanner(UnitInventory unitInventory, InteractionHandler interactionHandler, BWMap bwMap) {
		
		this.unitInventory = unitInventory;
		
		this.interactionHandler = interactionHandler;
		this.bwMap = bwMap;
		this.queue = new LinkedList<ConstructionProject>();
		this.completed = new LinkedList<ConstructionProject>();
	}
	
	public void initialize() {
		
		this.queue.clear();
		this.completed.clear();
		this.constructorSquad = unitInventory.createSquad(Worker.class, "constructors");
		this.constructorSquad.addListener(constructorsListener);
		
		this.availableWorkers = unitInventory.getAvailableWorkers();
		this.unitInventory.getBuildings().addListener(buildingListener);
		
		this.uniqueIdCounter = 0;
	}
	
	public int queue(Construction construction, TilePosition constructionSite, Worker worker) {
		
		ConstructionProject project = new ConstructionProject(++uniqueIdCounter, construction, this.interactionHandler, constructionSite);
		project.setAssignedWorker(worker);
		this.queue.add(project);
		logger.debug("added {} at {} with worker {} to queue", construction, constructionSite, worker);
		return uniqueIdCounter;
	}

	public int queue(Construction construction, TilePosition constructionSite) {
		
		this.queue.add(new ConstructionProject(++uniqueIdCounter, construction, this.interactionHandler, constructionSite));
		logger.debug("added {} at {} to queue", construction, constructionSite);
		return uniqueIdCounter;
	}

	public int queue(Construction construction) {
		
		this.queue.add(new ConstructionProject(++uniqueIdCounter, construction, this.interactionHandler));
		logger.debug("added {} to queue", construction);
		return uniqueIdCounter;
	}

	public int getQueuedGas() {
		
		int gas = 0;
		for (ConstructionProject project : this.queue) {
			if (project.getStatus().equals(ConstructionProject.Status.QUEUED)
					|| project.getStatus().equals(ConstructionProject.Status.ABOUT_TO_CONSTRUCT)) {
				
				gas += project.getGasPrice();
			}
		}
		return gas;
	}
	
	public int getQueuedMinerals() {
		
		int minerals = 0;
		for (ConstructionProject project : this.queue) {
			if (project.getStatus().equals(ConstructionProject.Status.QUEUED)
					|| project.getStatus().equals(ConstructionProject.Status.ABOUT_TO_CONSTRUCT)) {
				
				minerals += project.getMineralPrice();
			}
		}
		return minerals;
	}
	
	public boolean isConstructing(Worker worker) {
		
		return worker.isConstructing() || constructorSquad.contains(worker);
	}
	
	/**
	 * Removes a building from the queue and re-adds it at the end of the queue.
	 * @param project
	 */
	private void requeue(ConstructionProject project) {
		
		this.queue.remove(project);
		project.setStatus(ConstructionProject.Status.QUEUED);
		this.queue.add(project);
	}
	
	public void run(int currentMinerals, int currentGas, int frameCount) {
		
		if (frameCount % 100 == 0) {
			logger.debug("frame {}: building queue size: {}", frameCount, queue.size());
		}

		finishAbandonedConstructions();
		
		abandonIfCriticallyWounded();
		
		for (ConstructionProject project : this.queue) {
			if (project.getStatus() == ConstructionProject.Status.ABOUT_TO_CONSTRUCT) {
				
				if (!project.getAssignedWorker().isConstructing() && currentMinerals >= project.getMineralPrice() 
						&& currentGas >= project.getGasPrice() && project.isConstructionSiteVisible(bwMap)) {
					
					boolean success = project.build();
					if (!success) {
						TilePosition newSite = project.getConstruction().getBuildTile(null, project.getConstructionSite(), unitInventory, this.getConstructionProjects());
						project.setConstructionSite(newSite);
						project.setStatus(ConstructionProject.Status.QUEUED);
					}
					// problem management
				} else if (!project.getAssignedWorker().exists()) {
								
					logger.warn("worker {} assigned to construct {} is dead", project.getAssignedWorker(), project.getConstruction());
					this.constructorSquad.remove(project.getAssignedWorker());
					project.releaseWorker();
					project.setStatus(ConstructionProject.Status.QUEUED);
				} else if (project.getAssignedWorker().isStuck()) {
					
					logger.warn("worker {} assigned to construct {} is stuck", project.getAssignedWorker(), project.getConstruction());
					this.constructorSquad.move(project.getAssignedWorker(), unitInventory.getMiningWorkers());
					project.releaseWorker();
					project.setStatus(ConstructionProject.Status.QUEUED);
				} else if (!project.getAssignedWorker().isMoving() && !project.getAssignedWorker().isConstructing()) {
					
					logger.warn("worker {} assigned to construct {} is not moving", project.getAssignedWorker(), project.getConstruction());
					project.moveWorkerToSite();
				} else if (project.getAssignedWorker().isGatheringMinerals() || project.getAssignedWorker().isGatheringGas()) {
					
					logger.warn("worker {} assigned to construct {} is still mining", project.getAssignedWorker(), project.getConstruction());
					project.moveWorkerToSite();
				}
				currentMinerals -= project.getMineralPrice();
				currentGas -= project.getGasPrice();
			} else if (project.getStatus() == ConstructionProject.Status.QUEUED) {
				
				if (!project.hasConstructionSite() && !this.availableWorkers.isEmpty()) {
					project.findConstructionSite(this.unitInventory, this.queue);
				}
				if (!project.hasAssignedWorker()) {
					project.findSuitableWorker(this.unitInventory.getAvailableWorkers());
				}
				if (project.hasAssignedWorker()) {
					this.availableWorkers.move(project.getAssignedWorker(), this.constructorSquad);
					int estimatedMining = 0;
					
					// for performance reasons: estimated mining only needs to be calculated if we don't have enough minerals anyways
					if (currentMinerals < project.getMineralPrice()) {
						estimatedMining = project.estimateMineralsMinedDuringTravel(this.unitInventory.getMiningWorkers().size() - 1);
					}
					
					// TODO estimate gas mining as well and adjust moveout accordingly
					
					if (currentMinerals >= project.getMineralPrice() - estimatedMining) {
					
						logger.debug("estimated mining during travel: {}", estimatedMining);
						if (project.moveWorkerToSite()) {
							project.setStatus(ConstructionProject.Status.ABOUT_TO_CONSTRUCT);
						}
					}
				} else {
					
					logger.warn("Could not find a suitable worker for {}. requeueing...", project);
					requeue(project);
					break;
				}
				currentMinerals -= project.getMineralPrice();
				currentGas -= project.getGasPrice();
			}
		}
	}

	private void abandonIfCriticallyWounded() {
		for (Building building : unitInventory.getUnderConstruction()) {
			
			Worker worker = building.getBuildUnit(constructorSquad);
			if (worker != null && worker.getHitPoints() < 25) {
				worker.haltConstruction();
				unitInventory.getMiningWorkers().add(worker);
			}
		}
	}

	private void finishAbandonedConstructions() {
		for (Building unfinished : unitInventory.getUnderConstruction()) {
			if (unfinished.getBuildUnit(unitInventory.getAllWorkers()) == null && unitInventory.getAvailableWorkers().size() > 3) {
				
				Comparator<Worker> comp = (u1, u2) -> Integer.compare(u1.getHitPoints(), u2.getHitPoints());
				Worker worker = unitInventory.getAvailableWorkers().stream().max(comp).get();
				
				unitInventory.getAvailableWorkers().move(worker, this.constructorSquad);
				this.queue.stream().filter(c -> c.getConstructionSite().equals(unfinished.getTilePosition())).findFirst().ifPresent(b -> b.setAssignedWorker(worker));
				
				worker.resumeBuilding(unfinished);
				logger.info("found unfinished building {}. attempting to resume with worker {}", unfinished, worker);
			}
		}
	}
	
	/**
	 * Callback to inform the building planner that building is now being constructed.
	 * @param building
	 */
	public void onConstructionStarted(Worker worker) {
		
		for (ConstructionProject project : this.queue) {
			if (worker.equals(project.getAssignedWorker())) {
				project.setStatus(ConstructionProject.Status.CONSTRUCTING);
				return;
			}
		}
		logger.warn("was informed that {} is constructing, but can't find the building", worker);
	}

	public Queue<ConstructionProject> getConstructionProjects() {
		return this.queue;
	}
	
	public ConstructionProject getConstructionProject(int id) {
	
		for (ConstructionProject project : this.queue) {
			if (project.getId() == id) {
				return project;
			}
		}
		return null;
	}
	
	public Queue<ConstructionProject> getCompletedProjects() {
		return this.completed;
	}
	
	public int getCount(Construction construction) {

		int counter = 0;
		for (ConstructionProject project : this.queue) {
			if (project.getConstruction() == construction) {
				counter++;
			}
		}
		return counter;
	}
}
