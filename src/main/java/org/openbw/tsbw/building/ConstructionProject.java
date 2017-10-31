package org.openbw.tsbw.building;

import java.util.Comparator;
import java.util.Queue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbw.bwapi4j.InteractionHandler;
import org.openbw.bwapi4j.MapDrawer;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.Color;
import org.openbw.bwapi4j.unit.Building;
import org.openbw.bwapi4j.unit.Refinery;
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

public class ConstructionProject extends BasicActor<Message, Void> {

	private static final Logger logger = LogManager.getLogger();
	
	private static final long serialVersionUID = 1L;

	private static final int LATENCY = 3;

	private MapAnalyzer mapAnalyzer;
	private InteractionHandler interactionHandler;
	private UnitInventory unitInventory;
	private Queue<ConstructionProject> projects;
	
	private ConstructionType constructionType;
	private SCV builder;
	private TilePosition constructionSite;
	
	private Building building;
	private int queuedGas;
	private int queuedMinerals;
	private boolean finished;
		
	private final ReentrantLock lock = new ReentrantLock();
	private Action nextActionToExecute;
	private boolean actionResult;
	
	public void unpark() {
	
		if (this.nextActionToExecute != null) {
			this.actionResult = this.nextActionToExecute.execute();
			this.nextActionToExecute = null;
		}
		Strand.unpark(this.getStrand());
	}
	
	private boolean executeAction(Action action) throws InterruptedException, SuspendExecution {
	
		this.nextActionToExecute = action;
		Strand.park(this);
		
		return this.actionResult;
	}
	
	public ConstructionProject(ConstructionType constructionType, MapAnalyzer mapAnalyzer, InteractionHandler interactionHandler, UnitInventory unitInventory, Queue<ConstructionProject> projects) {
		
		this(constructionType, mapAnalyzer, interactionHandler, unitInventory, projects, null, null);
	}
	
	public ConstructionProject(ConstructionType constructionType, MapAnalyzer mapAnalyzer, InteractionHandler interactionHandler, UnitInventory unitInventory, Queue<ConstructionProject> projects, TilePosition constructionSite) {
		
		this(constructionType, mapAnalyzer, interactionHandler, unitInventory, projects, constructionSite, null);
	}
	
	public ConstructionProject(ConstructionType constructionType, MapAnalyzer mapAnalyzer, InteractionHandler interactionHandler, UnitInventory unitInventory, Queue<ConstructionProject> projects, TilePosition constructionSite, SCV builder) {
		
		this.constructionType = constructionType;
		this.mapAnalyzer = mapAnalyzer;
		this.interactionHandler = interactionHandler;
		this.unitInventory = unitInventory;
		this.projects = projects;
		this.constructionSite = constructionSite;
		this.builder = builder;
		
		this.nextActionToExecute = null;
		this.actionResult = false;
		this.building = null;
		this.finished = false;
		this.queuedGas = this.constructionType.getGasPrice();
		this.queuedMinerals = this.constructionType.getMineralPrice();
	}
	
	private void findBuilder() throws InterruptedException, SuspendExecution { 
		
		while(this.builder == null) {
			
			this.lock.lock();
			try {
				
				/* no construction site yet: take strongest worker */
				if (this.constructionSite == null) {
					
					Comparator<SCV> comp = (u1, u2) -> Integer.compare(u1.getHitPoints(), u2.getHitPoints());
					this.builder = unitInventory.getAvailableWorkers().stream().filter(w -> !w.isGatheringGas()).max(comp).get();
					
				/* construction site defined: take closest worker */
				} else {
					
					double distance = Double.MAX_VALUE;
					
					for (SCV worker : this.unitInventory.getAvailableWorkers()) {
						
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
					this.unitInventory.getAvailableWorkers().remove(this.builder);
					logger.debug("{}: found builder {} and removed from available workers.", this.interactionHandler.getFrameCount(), this.builder);
				}
			} finally {
			
				this.lock.unlock();
			}
		}
	}
	
	private void findConstructionSite() throws InterruptedException, SuspendExecution { 
		
		do {
			
			this.lock.lock();
			try {
				this.constructionSite = this.constructionType.getBuildTile(builder, unitInventory, mapAnalyzer, projects);
			} finally {
				this.lock.unlock();
			}
			if (this.constructionSite == null) {
				receive();
			}
		} while (this.constructionSite == null);
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
	
	private void waitForResources() throws InterruptedException, SuspendExecution { 
		
		boolean workerMovedToSite = false;
		Message message;
		do {
			message = receive();
			
			int estimatedMining = 0;
			
			// for performance reasons: estimated mining only needs to be calculated if we don't have enough minerals anyways
			if (message.getMinerals() < this.constructionType.getMineralPrice()) {
				estimatedMining = estimateMineralsMinedDuringTravel(this.unitInventory.getMineralWorkers().size() - 1);
			}
			
			// TODO estimate gas mining as well and adjust moveout accordingly
			
			if (message.getMinerals() >= this.constructionType.getMineralPrice() - estimatedMining) {
			
				logger.debug("estimated mining during travel: {}", estimatedMining);
				workerMovedToSite = this.executeAction(new MoveAction(this.builder, constructionSite.toPosition()));
				logger.debug("{}: Moved {} to {} to build {}: {}", this.interactionHandler.getFrameCount(), this.builder, this.constructionSite, this.constructionType, workerMovedToSite ? "success." : "failed.");
			}
			
		} while (!workerMovedToSite);
		
		while (message.getMinerals() < this.constructionType.getMineralPrice() || message.getGas() < this.constructionType.getGasPrice()) {
			
			message = receive();
		}
	}
	
	private void startConstruction() throws InterruptedException, SuspendExecution { 
		
		boolean success = false;
		do {
			success = this.executeAction(new BuildAction(this.builder, this.constructionSite, this.constructionType));
			if (success) {
				logger.debug("Command successful for {} to build {} at {}.", this.builder, this.constructionType, this.constructionSite);
			} else {
				logger.warn("{} could not build {} at {}: {}", this.builder, this.constructionType, this.constructionSite, interactionHandler.getLastError());
				
				if (!this.constructionType.canBuildHere(this.mapAnalyzer, this.builder, this.constructionSite)) {
					logger.warn("construction site {} is not free anymore. Attempting to find new site...", this.constructionSite);
					findConstructionSite();
				}
				receive();
			}
		} while (!success);
		
		int frame = interactionHandler.getFrameCount();
		Message message;
		do {
			message = receive();
		} while (message.getFrame() >= frame + LATENCY);
	}
	
	private void waitForCompletion() throws InterruptedException, SuspendExecution {
		
		while (this.building == null || !this.building.isCompleted() || !(this.builder.isIdle() || this.building instanceof Refinery)) {
			
			/* problem management */
			if (!this.builder.exists()) {
				
				logger.warn("warning: {} should be constructing {} but is dead. Attempting to find new builder...", this.builder, this.constructionType);
				findBuilder();
			} else if (this.builder.isStuck()) {
				
				logger.warn("warning: {} should be constructing {} but is stuck.", this.builder, this.constructionType);
				this.executeAction(new MoveAction(this.builder, constructionSite.toPosition()));
				// TODO check if still stuck after a couple of frames. if yes, get new builder and then release old one.
			} else if (this.builder.isIdle()) {
				
				logger.warn("warning: {} should be constructing {} but is idle. Attempting to restart construction...", this.builder, this.constructionType);
				startConstruction();
			} else if (!this.builder.isConstructing() && !this.builder.isMoving()) {
				
				logger.warn("warning: {} should be constructing {} but is not. Attempting to restart...", this.builder, this.constructionType);
				startConstruction();
			}
			// if SCV is being attacked...
			if (this.builder.getHitPoints() < 25) {
				releaseBuilder();
				// TODO potentially: findBuilder();
			}
			// TODO if construction is being attacked...
			receive();
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
		startConstruction();
		logger.trace("started construction.");
		waitForCompletion();
		logger.trace("completed.");
		
		this.finished = true;
		logger.trace("finished " + this.building);
		return null;
	}
	
	public void releaseBuilder() {
		
		this.unitInventory.getAvailableWorkers().add(this.builder);
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
	
	public boolean isConstructing(Building building) {
		
		return building.equals(this.building);
	}
	
	public int getQueuedGas() {
		
		return this.queuedGas;
	}
	
	public int getQueuedMinerals() {
		
		return this.queuedMinerals;
	}

	public boolean hasBuilt(SCV buildUnit) {
		
		return buildUnit.equals(builder);
	}
	
	public void constructionStarted(Building building) {
		
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

	public boolean isFinished() {
		return this.finished;
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
