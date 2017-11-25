package org.openbw.tsbw.building;

import java.util.Comparator;
import java.util.Queue;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbw.bwapi4j.InteractionHandler;
import org.openbw.bwapi4j.MapDrawer;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.Color;
import org.openbw.bwapi4j.unit.Building;
import org.openbw.bwapi4j.unit.SCV;
import org.openbw.tsbw.Constants;
import org.openbw.tsbw.MapAnalyzer;
import org.openbw.tsbw.UnitInventory;
import org.openbw.tsbw.analysis.PPF2;
import org.openbw.tsbw.building.action.Action;
import org.openbw.tsbw.building.action.BuildAction;
import org.openbw.tsbw.building.action.MoveAction;

import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.Strand;
import co.paralleluniverse.strands.concurrent.ReentrantLock;

//TODO the implementation is not robust yet. handle cases:
//- worker gets killed after assignment but before arriving at construction site
//- worker gets killed while constructing
//- construction site gets unavailable after worker assignment
//- building gets destroyed while constructing
public class ConstructionProject extends BasicActor<Message, Void> {

	private static final Logger logger = LogManager.getLogger();
	
	private static final long serialVersionUID = 2L;

	private int latency;

	private MapAnalyzer mapAnalyzer;
	private InteractionHandler interactionHandler;
	private UnitInventory myInventory;
	private Queue<ConstructionProject> projects;
	
	private ConstructionType constructionType;
	private SCV builder;
	private TilePosition constructionSite;
	
	private Building building;
	private int queuedGas;
	private int queuedMinerals;
		
	private final ReentrantLock lock = new ReentrantLock();
	private Action nextActionToExecute;
	private boolean actionResult;
	private int wakeUp;
	
	private boolean done;
	
	ConstructionProject(ConstructionType constructionType, MapAnalyzer mapAnalyzer, InteractionHandler interactionHandler, UnitInventory myInventory, Queue<ConstructionProject> projects) {
		
		this(constructionType, mapAnalyzer, interactionHandler, myInventory, projects, null, null);
	}
	
	ConstructionProject(ConstructionType constructionType, MapAnalyzer mapAnalyzer, InteractionHandler interactionHandler, UnitInventory myInventory, Queue<ConstructionProject> projects, TilePosition constructionSite) {
		
		this(constructionType, mapAnalyzer, interactionHandler, myInventory, projects, constructionSite, null);
	}
	
	ConstructionProject(ConstructionType constructionType, MapAnalyzer mapAnalyzer, InteractionHandler interactionHandler, UnitInventory myInventory, Queue<ConstructionProject> projects, TilePosition constructionSite, SCV builder) {
		
		this.constructionType = constructionType;
		this.mapAnalyzer = mapAnalyzer;
		this.interactionHandler = interactionHandler;
		this.myInventory = myInventory;
		this.projects = projects;
		this.constructionSite = constructionSite;
		this.builder = builder;
		this.latency = interactionHandler.getLatency();
		
		this.nextActionToExecute = null;
		this.actionResult = false;
		this.wakeUp = 0;
		this.done = false;
		this.building = null;
		this.queuedGas = this.constructionType.getGasPrice();
		this.queuedMinerals = this.constructionType.getMineralPrice();
	}
	
	public void onFrame(Message message) {
		
		if (wakeUp <= message.getFrame()) {
			
			try {
				this.lock.lock();
				
				if (this.nextActionToExecute != null) {
					
					this.actionResult = this.nextActionToExecute.execute();
					logger.trace("executed {}.", this.nextActionToExecute);
					this.wakeUp = message.getFrame() + this.latency;
					this.nextActionToExecute = null;
					Strand.unpark(this.getStrand());
				}
				this.sendOrInterrupt(message);
				
			} finally {
				
				this.lock.unlock();
			}
		}
	}

	private boolean executeAction(Action action) throws InterruptedException, SuspendExecution {
	
		this.nextActionToExecute = action;
		Strand.park(this);
		
		return this.actionResult;
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
			
			this.lock.lock();
			try {
				
				/* no construction site yet: take strongest worker */
				if (this.constructionSite == null) {
					
					Comparator<SCV> comp = (u1, u2) -> Integer.compare(u1.getHitPoints(), u2.getHitPoints());
					this.builder = myInventory.getAvailableWorkers().stream().filter(w -> !w.isGatheringGas()).max(comp).orElse(null);
					
				/* construction site defined: take closest worker */
				} else {
					
					double distance = Double.MAX_VALUE;
					
					for (SCV worker : this.myInventory.getAvailableWorkers()) {
						
						if (!worker.isGatheringGas()) {
						double currentDistance = mapAnalyzer.getGroundDistance(worker.getTilePosition(), constructionSite);
							if (currentDistance < distance) {
								distance = currentDistance;
								this.builder = worker;
							}
						}
					}
				}
				if (this.builder == null) {
					receive();
				} else {
					this.myInventory.getAvailableWorkers().remove(this.builder);
					logger.debug("{}: found builder {} and removed from available workers.", this.interactionHandler.getFrameCount(), this.builder);
				}
			} finally {
			
				this.lock.unlock();
			}
		}
	}
	
	private void findConstructionSite() { 
		
		if (this.constructionSite == null) {
			
			this.constructionSite = this.constructionType.getBuildTile(builder, myInventory, mapAnalyzer, projects);
		} else {
			
			this.constructionSite = this.constructionType.getBuildTile(builder, myInventory, mapAnalyzer, projects, this.constructionSite);
		}
	}

	/**
	 * Estimates how much minerals are mined while the assigned worker travels to the construction site, assuming workerCount mining workers.
	 * @param assignedWorker
	 * @param suitableConstructionSite
	 * @param remainingMiningWorkerCount usually = number of mining workers - 1 because a mining worker is pulled to construct a building
	 * @return estimated minerals mined during travel time
	 */
	private int estimateMineralsMinedDuringTravel(int remainingMiningWorkerCount) {
		
		double distance = mapAnalyzer.getGroundDistance(this.builder.getTilePosition(), this.constructionSite);
		double travelTimetoConstructionSite = distance / Constants.AVERAGE_SCV_SPEED;
		int estimatedMiningDuringTravel = (int)PPF2.calculateEstimatedMining((int)travelTimetoConstructionSite, remainingMiningWorkerCount);
		
		return estimatedMiningDuringTravel;
	}
	
	private void travelToConstructionSite() throws InterruptedException, SuspendExecution {
		
		while (!done && this.builder.getDistance(this.constructionSite.toPosition()) > this.builder.getSightRange() - 64) {
			
			receive();
			
			if (this.builder.isIdle()) {
				
				logger.warn("{}: {} stopped moving. Issuing new move order to {}...", this.interactionHandler.getFrameCount(), this.builder, this.constructionSite);
				this.executeAction(new MoveAction(this.builder, this.constructionSite.toPosition()));
			} else if (!this.builder.exists()) {
				
				logger.warn("{}: {} died. Attempting to find new builder...", this.interactionHandler.getFrameCount(), this.builder);
				findBuilder();
			}
		}
	}
	
	private void waitForResources() throws InterruptedException, SuspendExecution {
		
		boolean workerMovedToSite = false;
		Message message;
		int minerals = 0;
		int gas = 0;
		
		while (!workerMovedToSite && !done) {
			
			message = receive();
			minerals = message.getMinerals();
			gas = message.getGas();
			int estimatedMining = 0;
			
			// for performance reasons: estimated mining only needs to be calculated if we don't have enough minerals anyways
			if (minerals < this.constructionType.getMineralPrice()) {
				estimatedMining = estimateMineralsMinedDuringTravel(this.myInventory.getMineralWorkers().size() - 1);
			}
			
			// TODO estimate gas mining as well and adjust moveout accordingly
			
			if (minerals >= this.constructionType.getMineralPrice() - estimatedMining) {
			
				logger.debug("{}: estimated mining during travel: {}", this.interactionHandler.getFrameCount(), estimatedMining);
				workerMovedToSite = this.executeAction(new MoveAction(this.builder, constructionSite.toPosition()));
				logger.debug("{}: Moved {} to {} to build {}: {}", this.interactionHandler.getFrameCount(), this.builder, this.constructionSite, this.constructionType, workerMovedToSite ? "success." : "failed.");
			}
			
		} ;
		
		
		while (!done && (minerals < this.constructionType.getMineralPrice() || gas < this.constructionType.getGasPrice())) {
			
			message = receive();
			minerals = message.getMinerals();
			gas = message.getGas();
			
		}
	}
	
	private void startConstruction() throws InterruptedException, SuspendExecution { 
		
		boolean success = false;
		while (!success && !done) {
			
			success = this.executeAction(new BuildAction(this.builder, this.constructionSite, this.constructionType));
			if (success) {
				
				logger.debug("{}: Command successful for {} to build {} at {}.", this.interactionHandler.getFrameCount(), this.builder, this.constructionType, this.constructionSite);
			} else {
				
				logger.warn("{}: {} could not build {} at {}: {}", this.interactionHandler.getFrameCount(), this.builder, this.constructionType, this.constructionSite, interactionHandler.getLastError());
				
				if (!this.constructionType.canBuildHere(this.mapAnalyzer, this.builder, this.constructionSite)) {
					
					logger.warn("{}: construction site {} is not free anymore. Attempting to find new site...", this.interactionHandler.getFrameCount(), this.constructionSite);
					findConstructionSite();
				}
				receive();
			}
		};
	}
	
	private void waitForCompletion() throws InterruptedException, SuspendExecution {
		
		boolean completed = false;
		this.wakeUp = interactionHandler.getFrameCount() + latency;
		while (!done && interactionHandler.getFrameCount() < this.wakeUp) {
			
			receive();
		}
		
		while (!done && !completed) {
			
			/* problem management */
			if (!this.builder.exists()) {
				
				logger.warn("{}: warning: {} should be constructing {} but is dead. Attempting to find new builder...", this.interactionHandler.getFrameCount(), this.builder, this.constructionType);
				findBuilder();
			} else if (this.builder.isStuck()) {
				
				logger.warn("{}: warning: {} should be constructing {} but is stuck.", this.interactionHandler.getFrameCount(), this.builder, this.constructionType);
				//this.executeAction(new MoveAction(this.builder, constructionSite.toPosition()));
				// TODO check if still stuck after a couple of frames. if yes, get new builder and then release old one.
			} else if (this.builder.isIdle()) {
				
				logger.warn("{}: warning: {} should be constructing {} but is idle. Attempting to restart construction...", this.interactionHandler.getFrameCount(), this.builder, this.constructionType);
				startConstruction();
			} else if (!this.builder.isConstructing() && !this.builder.isMoving()) {
				
				logger.warn("{}: warning: {} should be constructing {} but is not. Attempting to restart...", this.interactionHandler.getFrameCount(), this.builder, this.constructionType);
				//startConstruction();
			}
			// if SCV is being attacked...
			if (this.builder.getHitPoints() < 25) {
				releaseBuilder();
				// TODO potentially: findBuilder();
			}
			// TODO if construction is being attacked...
			
			Message message = receive();
			completed = message.isBuildingCompleted();
		}
	}
	
	@Override
	protected Void doRun() throws InterruptedException, SuspendExecution {
		
		logger.trace("{}: spawned {}.", this.interactionHandler.getFrameCount(), this);
		
		findBuilder();
		logger.trace("found builder.");
		findConstructionSite();
		logger.trace("found site.");
		waitForResources();
		logger.trace("got resources.");
		travelToConstructionSite();
		logger.trace("arrived at construction site.");
		startConstruction();
		logger.trace("started construction.");
		waitForCompletion();
		logger.trace("completed.");
		
		logger.trace("{}: finished project for {}.", this.interactionHandler.getFrameCount(), this.building);
		releaseBuilder();
		
		return null;
	}
	
	private void releaseBuilder() throws InterruptedException, SuspendExecution {
		
		this.wakeUp = interactionHandler.getFrameCount() + latency;
		while (!done && interactionHandler.getFrameCount() < this.wakeUp) {
			
			receive();
		}
		
		this.myInventory.getAvailableWorkers().add(this.builder);
		logger.debug("{}: builder {} released.", this.interactionHandler.getFrameCount(), this.builder);
	}
	
	boolean collidesWithConstruction(TilePosition position) {
		
		if (this.constructionSite != null) {
			
			if (this.constructionSite.getX() + this.constructionType.tileWidth() > position.getX() &&  this.constructionSite.getX() < position.getX() + this.constructionType.tileWidth()
					&& this.constructionSite.getY() + this.constructionType.tileHeight() > position.getY() && this.constructionSite.getY() < position.getY() + this.constructionType.tileHeight()) {
				
				return true;
			}
		}
		return false;
	}

	boolean isOfType(ConstructionType constructionType) {
		
		return this.constructionType.equals(constructionType);
	}
	
	int getQueuedGas() {
		
		return this.queuedGas;
	}
	
	int getQueuedMinerals() {
		
		return this.queuedMinerals;
	}
	
	boolean isConstructing(Building building) {
		
		if (this.building != null) {
			
			return this.building.equals(building);
		} else if (building.getBuildUnit() == null) {
			
			return false;
		} else {
			return building.getBuildUnit().equals(builder);
		}
	}
	
	void constructionStarted(Building building) {
		
		logger.debug("{}: Construction of {} has started.", this.interactionHandler.getFrameCount(), building);
		this.queuedGas = 0;
		this.queuedMinerals = 0;
		this.building = building;
	}

	void drawConstructionSite(MapDrawer mapDrawer) {
		
		if (this.constructionSite != null) {
			mapDrawer.drawBoxMap(this.constructionSite.getX() * 32, this.constructionSite.getY() * 32, 
					this.constructionSite.getX() * 32 + this.constructionType.tileWidth() * 32, 
					this.constructionSite.getY() * 32 + this.constructionType.tileHeight() * 32, Color.WHITE);
		}
	}
	
	void stop() throws ExecutionException, InterruptedException {
		
		logger.debug("shutting down {}...", this);
		this.done = true;
		this.sendOrInterrupt(new Message(interactionHandler.getFrameCount(), true));
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
