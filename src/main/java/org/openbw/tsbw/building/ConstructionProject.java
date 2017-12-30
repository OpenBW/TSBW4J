package org.openbw.tsbw.building;

import java.util.Queue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbw.bwapi4j.InteractionHandler;
import org.openbw.bwapi4j.MapDrawer;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.Color;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Building;
import org.openbw.tsbw.Constants;
import org.openbw.tsbw.MapAnalyzer;
import org.openbw.tsbw.UnitInventory;
import org.openbw.tsbw.analysis.PPF2;
import org.openbw.tsbw.unit.Refinery;
import org.openbw.tsbw.unit.SCV;

//TODO the implementation is not robust yet. handle cases:
//- worker gets killed after assignment but before arriving at construction site
//- worker gets killed while constructing
//- construction site gets unavailable after worker assignment
//- building gets destroyed while constructing
public class ConstructionProject implements Project {

	private static final Logger logger = LogManager.getLogger();
	
	private MapAnalyzer mapAnalyzer;
	private InteractionHandler interactionHandler;
	private UnitInventory myInventory;
	private Queue<Project> projects;
	
	private ConstructionType constructionType;
	private SCV builder;
	private TilePosition constructionSite;
	
	private Building building;
	private int queuedGas;
	private int queuedMinerals;
		
	private boolean started;
	private boolean done;
	
	private int estimatedMining;
	private int estimatedGas;
	
	ConstructionProject(ConstructionType constructionType, MapAnalyzer mapAnalyzer, InteractionHandler interactionHandler, UnitInventory myInventory, Queue<Project> projects) {
		
		this(constructionType, mapAnalyzer, interactionHandler, myInventory, projects, null, null);
	}
	
	ConstructionProject(ConstructionType constructionType, MapAnalyzer mapAnalyzer, InteractionHandler interactionHandler, UnitInventory myInventory, Queue<Project> projects, TilePosition constructionSite) {
		
		this(constructionType, mapAnalyzer, interactionHandler, myInventory, projects, constructionSite, null);
	}
	
	ConstructionProject(ConstructionType constructionType, MapAnalyzer mapAnalyzer, InteractionHandler interactionHandler, UnitInventory myInventory, Queue<Project> projects, TilePosition constructionSite, SCV builder) {
		
		this.constructionType = constructionType;
		this.mapAnalyzer = mapAnalyzer;
		this.interactionHandler = interactionHandler;
		this.myInventory = myInventory;
		this.projects = projects;
		this.constructionSite = constructionSite;
		this.builder = builder;
		
		this.done = false;
		this.started = false;
		this.building = null;
		this.queuedGas = this.constructionType.getGasPrice();
		this.queuedMinerals = this.constructionType.getMineralPrice();
		if (this.constructionSite != null) {
			
			if (this.builder != null) {
				
				findConstructionSite(this.builder);
				estimateMining(this.builder);
			} else {
				
				SCV tmpBuilder = myInventory.getAvailableWorkers().min(
						(u1, u2) -> Integer.compare(mapAnalyzer.getGroundDistance(u1.getTilePosition(), constructionSite), 
													mapAnalyzer.getGroundDistance(u2.getTilePosition(), constructionSite))).orElse(null);
				findConstructionSite(tmpBuilder);
				estimateMining(tmpBuilder);
			}
		}
	}
	
	private void estimateMining(SCV builder) {
		
		double distance = this.mapAnalyzer.getGroundDistance(builder.getTilePosition(), constructionSite);
		double travelTimetoConstructionSite = distance / Constants.AVERAGE_SCV_SPEED;
		this.estimatedMining = (int)PPF2.calculateEstimatedMining((int)travelTimetoConstructionSite, (int)this.myInventory.getAvailableWorkers().count() - 1);
		this.estimatedGas = (int)(this.myInventory.getRefineries().stream()
				.map(r -> (Refinery)r).mapToDouble(r -> r.getMiningRate()).sum() * travelTimetoConstructionSite);
		
	}
	
	public void onFrame(Message message) {
		
		if (constructionSite == null) {
		
			SCV tmpBuilder = myInventory.getAvailableWorkers().max((u1, u2) -> Integer.compare(u1.getHitPoints(), u2.getHitPoints())).orElse(null);
			
			findConstructionSite(tmpBuilder);
			estimateMining(tmpBuilder);
		}
	
		if (!started
				&& (message.getMinerals() + estimatedMining >= this.constructionType.getMineralPrice() && message.getGas() + estimatedGas >= this.constructionType.getGasPrice())) {
			
			if (this.builder == null) {
				
				this.builder = myInventory.getAvailableWorkers().min(
						(u1, u2) -> Integer.compare(mapAnalyzer.getGroundDistance(u1.getTilePosition(), constructionSite), 
													mapAnalyzer.getGroundDistance(u2.getTilePosition(), constructionSite))).orElse(null);
			}
			
			if (this.builder != null) {
				
				logger.trace("estimated resources on arrival meets requirements. Sending {} to build {}.", this.builder, this.constructionType);
				this.builder.construct(constructionSite, constructionType);
				this.started = true;
			}
		}
	}

	public void updateConstructionSite(TilePosition newSite) {
		
		// TODO this is very volatile because we don't know in what state we are in. Proper solution: implement update as a message to self
		this.constructionSite = this.constructionType.getBuildTile(builder, myInventory, mapAnalyzer, projects, newSite);
		if (this.builder != null) {
			this.builder.move(newSite.toPosition());
			logger.debug("{}: moving builder {} to updated location at {}.", this.interactionHandler.getFrameCount(), this.builder, this.constructionSite);
		}
	}
	
	private void findConstructionSite(SCV builder) { 
		
		// TODO the OR condition makes it less robust: CCs can only be built on base location tile positions.
		// better: attempt to free the construction site. if still not successful, find a spot nearby.
		if (this.constructionSite == null || this.constructionType == ConstructionType.Terran_Command_Center) {
			
			this.constructionSite = this.constructionType.getBuildTile(builder, myInventory, mapAnalyzer, projects);
		} else {
			
			this.constructionSite = this.constructionType.getBuildTile(builder, myInventory, mapAnalyzer, projects, this.constructionSite);
		}
		logger.trace("found construction site at {} for {}.", this.constructionSite, this.constructionType);
	}
	
	public boolean collidesWithConstruction(TilePosition position, UnitType unitType) {
		
		if (this.constructionSite != null) {
			
			if (this.constructionSite.getX() + this.constructionType.tileWidth() < position.getX() ||  this.constructionSite.getX() > position.getX() + unitType.tileWidth()) {
				
				return false;
			} else if (this.constructionSite.getY() + this.constructionType.tileHeight() < position.getY() || this.constructionSite.getY() > position.getY() + unitType.tileHeight()) {
				
				return false;
			} else {
				
				return true;
			}
		}
		return false;
	}

	public boolean isOfType(ConstructionType constructionType) {
		
		return this.constructionType.equals(constructionType);
	}
	
	public int getQueuedGas() {
		
		return this.queuedGas;
	}
	
	public int getQueuedMinerals() {
		
		return this.queuedMinerals;
	}
	
	public boolean isConstructing(Building building) {
		
		if (this.building != null) {
			
			return this.building.equals(building);
		} else if (building.getBuildUnit() == null) {
			
			return false;
		} else {
			return building.getBuildUnit().equals(builder);
		}
	}
	
	public void constructionStarted(Building building) {
		
		logger.debug("{}: Construction of {} has started.", this.interactionHandler.getFrameCount(), building);
		this.queuedGas = 0;
		this.queuedMinerals = 0;
		this.building = building;
	}

	public void drawConstructionSite(MapDrawer mapDrawer) {
		
		if (this.constructionSite != null) {
			mapDrawer.drawBoxMap(this.constructionSite.getX() * 32, this.constructionSite.getY() * 32, 
					this.constructionSite.getX() * 32 + this.constructionType.tileWidth() * 32, 
					this.constructionSite.getY() * 32 + this.constructionType.tileHeight() * 32, Color.WHITE);
		}
	}
	
	public void completed() {
		
		this.done = true;
		this.builder.gatherMinerals();
	}
	
	public boolean isDone() {
		
		return this.done;
	}

// TODO
//private void finishAbandonedConstructions() {
//	
//	for (Building unfinished : unitInventory.getUnderConstruction()) {
//		if (unfinished.getBuildUnit() == null && unitInventory.getAvailableWorkers().size() > 3) {
//			
//			Comparator<SCV> comp = (u1, u2) -> Integer.compare(u1.getHitPoints(), u2.getHitPoints());
//			SCV worker = unitInventory.getAvailableWorkers().stream().max(comp).get();
//			
//			unitInventory.getAvailableWorkers().move(worker, this.constructorSquad);
//			this.queue.stream().filter(c -> c.getConstructionSite().equals(unfinished.getTilePosition())).findFirst().ifPresent(b -> b.setAssignedWorker(worker));
//			
//			worker.resumeBuilding(unfinished);
//			logger.info("found unfinished building {}. attempting to resume with worker {}", unfinished, worker);
//		}
//	}
//}
}
