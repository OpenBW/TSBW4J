package org.openbw.tsbw.building;

import java.util.Queue;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbw.bwapi4j.InteractionHandler;
import org.openbw.bwapi4j.MapDrawer;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.Color;
import org.openbw.bwapi4j.unit.Building;
import org.openbw.tsbw.Constants;
import org.openbw.tsbw.MapAnalyzer;
import org.openbw.tsbw.UnitInventory;
import org.openbw.tsbw.analysis.PPF2;
import org.openbw.tsbw.unit.SCV;

import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.Strand;

//TODO the implementation is not robust yet. handle cases:
//- worker gets killed after assignment but before arriving at construction site
//- worker gets killed while constructing
//- construction site gets unavailable after worker assignment
//- building gets destroyed while constructing
public class ConstructionProject extends BasicActor<Message, Void> implements Project {

	private static final Logger logger = LogManager.getLogger();
	
	private static final long serialVersionUID = 2L;

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
		
	private boolean done;
	
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
		this.building = null;
		this.queuedGas = this.constructionType.getGasPrice();
		this.queuedMinerals = this.constructionType.getMineralPrice();
	}
	
	public void onFrame(Message message) {
		
		this.sendOrInterrupt(message);
	}

	public void updateConstructionSite(TilePosition newSite) {
		
		// TODO this is very volatile because we don't know in what state we are in. Proper solution: implement update as a message to self
		this.constructionSite = this.constructionType.getBuildTile(builder, myInventory, mapAnalyzer, projects, newSite);
		if (this.builder != null) {
			this.builder.move(newSite.toPosition());
			logger.debug("{}: moving builder {} to updated location at {}.", this.interactionHandler.getFrameCount(), this.builder, this.constructionSite);
		}
	}
	
	private void findBuilder() throws InterruptedException, SuspendExecution { 
		
		while(this.builder == null && !done) {
			
			/* no construction site yet: take strongest worker */
			if (this.constructionSite == null) {
				
				this.builder = myInventory.getAvailableWorkers().max((u1, u2) -> Integer.compare(u1.getHitPoints(), u2.getHitPoints())).orElse(null);
				
			/* construction site defined: take closest worker */
			} else {
				
				this.builder = myInventory.getAvailableWorkers().min(
						(u1, u2) -> Integer.compare(mapAnalyzer.getGroundDistance(u1.getTilePosition(), constructionSite), 
													mapAnalyzer.getGroundDistance(u2.getTilePosition(), constructionSite))).orElse(null);
			}
			
			if (this.builder == null) {
				
				receive();
			} else {
				
				logger.debug("{}: found builder {} and removed from available workers.", this.interactionHandler.getFrameCount(), this.builder);
			}
		}
	}
	
	private void findConstructionSite() { 
		
		// TODO the OR condition makes it less robust: CCs can only be built on base location tile positions.
		// better: attempt to free the construction site. if stil not successful, find a spot nearby.
		if (this.constructionSite == null || this.constructionType == ConstructionType.Terran_Command_Center) {
			
			this.constructionSite = this.constructionType.getBuildTile(builder, myInventory, mapAnalyzer, projects);
		} else {
			
			this.constructionSite = this.constructionType.getBuildTile(builder, myInventory, mapAnalyzer, projects, this.constructionSite);
		}
		logger.trace("found site construction site at {}.", this.constructionSite);
	}
	
	private void waitForCompletion() throws InterruptedException, SuspendExecution {
		
		boolean completed = false;
		
		while (!done && !completed) {
			
			Message message = receive();
			completed = message.isBuildingCompleted();
		}
	}
	
	@Override
	protected Void doRun() throws InterruptedException, SuspendExecution {
		
		logger.trace("{}: spawned {}.", this.interactionHandler.getFrameCount(), this);
		
		findBuilder();
		findConstructionSite();
		this.builder.construct(constructionSite, constructionType);
		waitForCompletion();
		logger.trace("completed.");
		
		logger.trace("{}: finished project for {}.", this.interactionHandler.getFrameCount(), this.building);
		return null;
	}
	
	public boolean collidesWithConstruction(TilePosition position) {
		
		if (this.constructionSite != null) {
			
			if (this.constructionSite.getX() + this.constructionType.tileWidth() > position.getX() &&  this.constructionSite.getX() < position.getX() + this.constructionType.tileWidth()
					&& this.constructionSite.getY() + this.constructionType.tileHeight() > position.getY() && this.constructionSite.getY() < position.getY() + this.constructionType.tileHeight()) {
				
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
	
	public void stop() throws ExecutionException, InterruptedException {
		
		logger.debug("shutting down {}...", this);
		this.done = true;
		
		Strand.unpark(this.getStrand());
		this.sendOrInterrupt(new Message(interactionHandler.getFrameCount(), true));
		Strand.unpark(this.getStrand());
		
		this.join();
		logger.debug("done.");
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
