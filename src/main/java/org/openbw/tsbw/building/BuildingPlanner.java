package org.openbw.tsbw.building;

import java.util.LinkedList;
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

public class BuildingPlanner {

	private static final Logger logger = LogManager.getLogger();
	
	private UnitInventory unitInventory;
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
			if (building != unitInventory.getMain()) {
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
	
	private GroupListener<Building> buildingListener = new GroupListener<Building>() {
		
		@Override
		public void onAdd(Building building) {
			
			for (ConstructionProject project : projects) {
					
					if (building.equals(project.getBuilding())) {
						projects.remove(project);
						return;
					}
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
	
	public BuildingPlanner(UnitInventory unitInventory, MapAnalyzer mapAnalyzer, InteractionHandler interactionHandler) {
	
		this.unitInventory = unitInventory;
		this.mapAnalyzer = mapAnalyzer;
		this.interactionHandler = interactionHandler;
		this.projects = new LinkedList<>();
	}
	
	public void initialize() {
		
		this.projects.clear();
		this.unitInventory.getUnderConstruction().addListener(constructionListener);
		this.unitInventory.getBuildings().addListener(buildingListener);
	}

	public int queue(ConstructionType constructionType, TilePosition constructionSite, SCV worker) {
		
		ConstructionProject constructionProject = new ConstructionProject(constructionType, this.mapAnalyzer, this.interactionHandler, this.unitInventory, this.projects, constructionSite, worker);
		this.projects.add(constructionProject);
		constructionProject.spawn();
		return projects.size();
	}

	public int queue(ConstructionType constructionType, TilePosition constructionSite) {
		
		ConstructionProject constructionProject = new ConstructionProject(constructionType, this.mapAnalyzer, this.interactionHandler, this.unitInventory, this.projects, constructionSite);
		this.projects.add(constructionProject);
		constructionProject.spawn();
		return projects.size();
	}

	public int queue(ConstructionType constructionType) {
		
		ConstructionProject constructionProject = new ConstructionProject(constructionType, this.mapAnalyzer, this.interactionHandler, this.unitInventory, this.projects);
		this.projects.add(constructionProject);
		constructionProject.spawn();
		return projects.size();
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
		
		if (this.projects.size() > 0) {
			logger.debug("build planner: frame: {}, projects: {}, minerals: {}, gas: {}", frameCount, projects.size(), currentMinerals, currentGas);
		}
		
		for (ConstructionProject project : this.projects) {
			
			project.sendOrInterrupt(new Message(currentMinerals, currentGas, frameCount));
			logger.debug("   - available resources: minerals: {}, gas: {}", currentMinerals, currentGas);
			currentGas -= project.getQueuedGas();
			currentMinerals -= project.getQueuedMinerals();
		}
	}
}
