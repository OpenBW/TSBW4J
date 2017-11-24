package org.openbw.tsbw.building;

import java.util.ArrayList;
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

/**
 * Manages all building construction for the game.
 * Building can be queued for construction. The planner takes care of finding a builder and a construction site in case they are not provided.
 * It also makes sure the construction site is legal and finds the closest legal location in case it is not.
 * Once a construction is queued, the required resources to build are locked, such that they will not be spent by someone else
 * before construction can start. Once construction has started the resources are released, because the game has made the payment.
 * This mechanism simulates SC2 behavior, where the payment is made already when ordering a worker to construct and refunded if construction cannot be started.
 */
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
			
			projects.stream().filter(p -> p.isConstructing(building)).findFirst().ifPresent(p -> p.constructionStarted(building));
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
	
	private GroupListener<Building> buildingListener = new GroupListener<Building>() {
		
		@Override
		public void onAdd(Building building) {
			
			if (building == null) {
				logger.warn("attempting to add null building.");
			} else {
				logger.debug("building {} completed.", building);
			}
			
			projects.stream().filter(p -> p.isConstructing(building)).findFirst().ifPresent(p -> {
				p.sendOrInterrupt(new Message(interactionHandler.getFrameCount(), true));
			});
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
		this.myInventory.getBuildings().addListener(buildingListener);
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
		
		List<ConstructionProject> completed = new ArrayList<>();
		
		for (ConstructionProject project : this.projects) {
			
			project.onFrame(new Message(currentMinerals, currentGas, frameCount));
			currentGas -= project.getQueuedGas();
			currentMinerals -= project.getQueuedMinerals();
			if (project.isDone()) {
				completed.add(project);
			}
		}
		
		completed.stream().forEach(p -> this.projects.remove(p));
	}
}
