package org.openbw.tsbw.building;

import java.util.Queue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mina.statemachine.StateControl;
import org.apache.mina.statemachine.StateMachine;
import org.apache.mina.statemachine.StateMachineFactory;
import org.apache.mina.statemachine.StateMachineProxyBuilder;
import org.apache.mina.statemachine.annotation.State;
import org.apache.mina.statemachine.annotation.Transition;
import org.apache.mina.statemachine.annotation.Transitions;
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

public class ConstructionProject implements Project {

	private static final Logger logger = LogManager.getLogger();
	
	public interface ConstructionStateMachine {
		
		public void onFrame(Message message);
		
		public void constructionStarted(Building building);
		
		public void completed();
	}
	
	public static class ConstructionStateMachineImpl {
		
		@State
		public static final String QUEUED = "Queued";
		@State
		public static final String MOVING_TO_SITE = "MovingToSite";
		@State
		public static final String CONSTRUCTING = "Constructing";
		@State
		public static final String COMPLETED = "Completed";
		@State
		public static final String ABANDONED = "Abandonded";
		
		private MapAnalyzer mapAnalyzer;
		private InteractionHandler interactionHandler;
		private UnitInventory myInventory;
		private Queue<Project> projects;
		
		private ConstructionType constructionType;
		private boolean completed;
		private int estimatedMineralMiningDuringTravel;
		private int estimatedGasMiningDuringTravel;
		
		private SCV builder;
		private TilePosition constructionSite;
		private Building building;
		private int queuedGas;
		private int queuedMinerals;
		
		public ConstructionStateMachineImpl(ConstructionType constructionType, MapAnalyzer mapAnalyzer, InteractionHandler interactionHandler, UnitInventory myInventory, Queue<Project> projects, TilePosition constructionSite, SCV builder) {
			
			this.constructionType = constructionType;
			this.mapAnalyzer = mapAnalyzer;
			this.interactionHandler = interactionHandler;
			this.myInventory = myInventory;
			this.projects = projects;
			
			this.constructionSite = constructionSite;
			this.builder = builder;
			this.queuedGas = this.constructionType.getGasPrice();
			this.queuedMinerals = this.constructionType.getMineralPrice();
			this.completed = false;
			
			SCV tmpBuilder = this.builder;
			
			if (tmpBuilder == null) {
				
				tmpBuilder = myInventory.getAvailableWorkers().max((u1, u2) -> Integer.compare(u1.getHitPoints(), u2.getHitPoints())).orElse(null);
			}
			if (this.constructionSite == null) {
				
				findConstructionSite(tmpBuilder);
			}
			estimateMining(tmpBuilder);
		}
		
		@Transitions({
			@Transition(on = "onFrame", in = COMPLETED),
			@Transition(on = "onFrame", in = ABANDONED)
		})
		public void doNothing() {
			
		}
		
		@Transition(on = "onFrame", in = CONSTRUCTING)
		public void checkIfBuilderAlive(Message message) {
			
			if (this.builder == null || !this.builder.exists()) {
				
				this.builder = myInventory.getAvailableWorkers().max((u1, u2) -> Integer.compare(u1.getHitPoints(), u2.getHitPoints())).orElse(null);
				if (this.builder != null) {
					this.builder.resume(this.building);
				}
			}
		}
		
		@Transition(on = "onFrame", in = MOVING_TO_SITE)
		public void checkIfArrivedAtSite(Message message) {
			
			if (!this.builder.exists()) {
				
				this.builder = null;
				StateControl.breakAndCallNow(QUEUED);
			}
		}
		
		@Transition(on = "onFrame", in = QUEUED)
		public void checkMoveToSite(Message message) {
			
			if (message.getMinerals() + this.estimatedMineralMiningDuringTravel >= this.constructionType.getMineralPrice() 
					&& message.getGas() + this.estimatedGasMiningDuringTravel >= this.constructionType.getGasPrice()) {

				if (this.builder == null) {
					
					this.builder = myInventory.getAvailableWorkers().min(
							(u1, u2) -> Integer.compare(this.mapAnalyzer.getGroundDistance(u1.getTilePosition(), this.constructionSite), 
									this.mapAnalyzer.getGroundDistance(u2.getTilePosition(), this.constructionSite))).orElse(null);
				}
				
				if (this.builder != null) {
					
					logger.trace("estimated resources on arrival meets requirements. Sending {} to build {}.", this.builder, this.constructionType);
					this.builder.construct(this.constructionSite, this.constructionType);
					StateControl.breakAndCallNext(MOVING_TO_SITE);
				}
			}
		}

		@Transition(on = "constructionStarted", in = MOVING_TO_SITE, next = CONSTRUCTING)
		public void startConstruction(Building building) {
			
			logger.debug("{}: Construction of {} has started.", this.interactionHandler.getFrameCount(), building);
			this.queuedGas = 0;
			this.queuedMinerals = 0;
			this.building = building;
		}
		
		@Transition(on = "completed", in = CONSTRUCTING, next = COMPLETED)
		public void complete() {
			
			this.completed = true;
			this.builder.gatherMinerals();
		}

		private void estimateMining(SCV builder) {
			
			double distance = this.mapAnalyzer.getGroundDistance(builder.getTilePosition(), constructionSite);
			double travelTimetoConstructionSite = distance / Constants.AVERAGE_SCV_SPEED;
			this.estimatedMineralMiningDuringTravel = (int)PPF2.calculateEstimatedMining((int)travelTimetoConstructionSite, (int)this.myInventory.getAvailableWorkers().count() - 1);
			this.estimatedGasMiningDuringTravel = (int)(this.myInventory.getRefineries().stream()
					.map(r -> (Refinery)r).mapToDouble(r -> r.getMiningRate()).sum() * travelTimetoConstructionSite);
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
		
		public boolean isDone() {
			
			return this.completed;
		}
		
		public void drawConstructionSite(MapDrawer mapDrawer) {
			
			if (this.constructionSite != null) {
				mapDrawer.drawBoxMap(this.constructionSite.getX() * 32, this.constructionSite.getY() * 32, 
						this.constructionSite.getX() * 32 + this.constructionType.tileWidth() * 32, 
						this.constructionSite.getY() * 32 + this.constructionType.tileHeight() * 32, Color.WHITE);
			}
		}
		
		public boolean isOfType(ConstructionType constructionType) {
			
			return this.constructionType == constructionType;
		}
	}
	
	private ConstructionStateMachineImpl constructionSMImpl;
	private ConstructionStateMachine constructionSM;
	
	ConstructionProject(ConstructionType constructionType, MapAnalyzer mapAnalyzer, InteractionHandler interactionHandler, UnitInventory myInventory, Queue<Project> projects, TilePosition constructionSite, SCV builder) {
		
		this.constructionSMImpl = new ConstructionStateMachineImpl(constructionType, mapAnalyzer, interactionHandler, myInventory, projects, constructionSite, builder);
		StateMachine stateMachine = StateMachineFactory.getInstance(Transition.class).create(ConstructionStateMachineImpl.QUEUED, this.constructionSMImpl);
		this.constructionSM = new StateMachineProxyBuilder().create(ConstructionStateMachine.class, stateMachine);
	}

	@Override
	public void onFrame(Message message) {
		
		this.constructionSM.onFrame(message);
	}

	@Override
	public int getQueuedGas() {
		
		return this.constructionSMImpl.getQueuedGas();
	}

	@Override
	public int getQueuedMinerals() {
		
		return this.constructionSMImpl.getQueuedMinerals();
	}

	@Override
	public void constructionStarted(Building building) {
		
		this.constructionSM.constructionStarted(building);
	}

	@Override
	public boolean isConstructing(Building building) {
		
		return this.constructionSMImpl.isConstructing(building);
	}

	@Override
	public boolean collidesWithConstruction(TilePosition position, UnitType unitType) {
		
		return this.constructionSMImpl.collidesWithConstruction(position, unitType);
	}

	@Override
	public boolean isDone() {
		
		return this.constructionSMImpl.isDone();
	}

	@Override
	public void completed() {
		
		this.constructionSM.completed();
	}

	@Override
	public void drawConstructionSite(MapDrawer mapDrawer) {
		
		this.constructionSMImpl.drawConstructionSite(mapDrawer);
	}

	@Override
	public boolean isOfType(ConstructionType constructionType) {
		
		return this.constructionSMImpl.isOfType(constructionType);
	}

}
