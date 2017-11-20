package org.openbw.tsbw.building;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbw.bwapi4j.InteractionHandler;
import org.openbw.bwapi4j.MapDrawer;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.unit.Building;
import org.openbw.bwapi4j.unit.SCV;
import org.openbw.tsbw.GroupListener;
import org.openbw.tsbw.MapAnalyzer;
import org.openbw.tsbw.UnitInventory;

import co.paralleluniverse.fibers.SuspendExecution;

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
public class BuildingPlanner {

	private static final Logger logger = LogManager.getLogger();
	
	private UnitInventory myInventory;
	private MapAnalyzer mapAnalyzer;
	private InteractionHandler interactionHandler;
	
	private Queue<ConstructionProject> projects;
	
	private GroupListener<Building> constructionListener = new GroupListener<Building>() {
		
		@Override
		public void onAdd(Building building) {
			
			if (building == null) {
				logger.warn("attempting to add null building.");
			} else {
				logger.debug("building {} created.", building);
			}
			
			for (ConstructionProject project : projects) {
					
					if (project.hasBuilt(building.getBuildUnit())) {
						project.constructionStarted(building);
						return;
					}
			}
			if (building != myInventory.getMain()) {
				logger.warn("was informed that {} is created, but can't find the construction project.", building);
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
	
	public BuildingPlanner(UnitInventory myInventory, MapAnalyzer mapAnalyzer, InteractionHandler interactionHandler) {
	
		this.myInventory = myInventory;
		this.mapAnalyzer = mapAnalyzer;
		this.interactionHandler = interactionHandler;
		this.projects = new LinkedList<>();
	}
	
	public void initialize() {
		
		this.projects.clear();
		this.myInventory.getUnderConstruction().addListener(constructionListener);
	}

	public ConstructionProject queue(ConstructionType constructionType, TilePosition constructionSite, SCV worker) {
		
		logger.debug("Queueing {}...", constructionType);
		ConstructionProject constructionProject = new ConstructionProject(constructionType, this.mapAnalyzer, this.interactionHandler, this.myInventory, this.projects, constructionSite, worker);
		this.projects.add(constructionProject);
		constructionProject.spawn();
		return constructionProject;
	}

	public ConstructionProject queue(ConstructionType constructionType, TilePosition constructionSite) {
		
		logger.debug("Queueing {}...", constructionType);
		ConstructionProject constructionProject = new ConstructionProject(constructionType, this.mapAnalyzer, this.interactionHandler, this.myInventory, this.projects, constructionSite);
		this.projects.add(constructionProject);
		constructionProject.spawn();
		return constructionProject;
	}

	public ConstructionProject queue(ConstructionType constructionType) {
		
		logger.debug("Queueing {}...", constructionType);
		ConstructionProject constructionProject = new ConstructionProject(constructionType, this.mapAnalyzer, this.interactionHandler, this.myInventory, this.projects);
		this.projects.add(constructionProject);
		constructionProject.spawn();
		return constructionProject;
	}
	
	public int getQueuedGas() {
		
		int gas = 0;
		for (ConstructionProject project : this.projects) {
				
				gas += project.getQueuedGas();
		}
		return gas;
	}
	
	public int getQueuedMinerals() {
		
		int minerals = 0;
		for (ConstructionProject project : this.projects) {
				
			minerals += project.getQueuedMinerals();
		}
		return minerals;
	}
	
	public int getCount(ConstructionType constructionType) {

		int counter = 0;
		for (ConstructionProject project : this.projects) {
			if (project.isOfType(constructionType)) {
				counter++;
			}
		}
		return counter;
	}
	
	public void drawConstructionSites(MapDrawer mapDrawer) {
		
		for (ConstructionProject project : this.projects) {
					
			project.drawConstructionSite(mapDrawer);
		}
	}
	
	public void run(int currentMinerals, int currentGas, int frameCount) {
		
		List<ConstructionProject> completed = new LinkedList<>();
		
		for (ConstructionProject project : this.projects) {
			
			if (project.isFinished()) {
				
				completed.add(project);
			} else {
				
				try {
					project.ref().send(new Message(currentMinerals, currentGas, frameCount));
				} catch (SuspendExecution e) {
					logger.error("error sending message to actor.", e);
				}
				currentGas -= project.getQueuedGas();
				currentMinerals -= project.getQueuedMinerals();
			}
		}
		
		for (ConstructionProject project : completed) {
			
			this.projects.remove(project);
			project.releaseBuilder();
		}
		
		for (ConstructionProject project : this.projects) {
			project.unpark();
		}
	}
}
