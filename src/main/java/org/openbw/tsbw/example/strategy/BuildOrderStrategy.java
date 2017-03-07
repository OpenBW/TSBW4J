package org.openbw.tsbw.example.strategy;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.DamageEvaluator;
import org.openbw.bwapi.InteractionHandler;
import org.openbw.bwapi.MapDrawer;
import org.openbw.bwapi.Player;
import org.openbw.tsbw.GroupListener;
import org.openbw.tsbw.UnitInventory;
import org.openbw.tsbw.building.BuildingPlanner;
import org.openbw.tsbw.strategy.AbstractGameStrategy;
import org.openbw.tsbw.strategy.ScoutingStrategy;
import org.openbw.tsbw.unit.Barracks;
import org.openbw.tsbw.unit.Building;
import org.openbw.tsbw.unit.Factory;
import org.openbw.tsbw.unit.Refinery;
import org.openbw.tsbw.unit.SupplyDepot;
import org.openbw.tsbw.unit.Worker;

/**
 * This strategy contains a very basic concept to execute a pre-defined build order.
 * The build order is defined in the start method, and then executed step by step in the run method.
 * 
 * You could write code to read a build order from a file and generate the corresponding actions to be executed.
 */
class BuildOrderStrategy extends AbstractGameStrategy {

	private static final Logger logger = LogManager.getLogger();
	
	private List<BoAction> buildOrder;
	private int boPointer;
	
	private UnitInventory myUnitInventory;
	
	private GroupListener<Worker> workerListener = new GroupListener<Worker>() {

		@Override
		public void onAdd(Worker worker) {
			myUnitInventory.getMiningWorkers().add(worker);
		}

		@Override
		public void onRemove(Worker worker) {
			
			// do nothing
		}

		@Override
		public void onDestroy(Worker worker) {
			
			// do nothing
		}
	};
	
	/**
	 * Listens to events affecting my buildings.
	 */
	private GroupListener<Building> buildingsListener = new GroupListener<Building>() {

		@Override
		public void onAdd(Building building) {
			
			logger.info("building {} was added.", building);
			
			// if our refinery finished, add an additional workers to mine gas from it
			if (building instanceof Refinery) {
				myUnitInventory.getMiningWorkers().first().gather((Refinery)building);
			}
		}

		@Override
		public void onRemove(Building building) {
			
			logger.info("building {} was removed.", building);
		}

		@Override
		public void onDestroy(Building building) {
			
			logger.info("building {} was destroyed.", building);
		}
	};
	
	/* default */ BuildOrderStrategy(MapDrawer mapDrawer, BWMap bwMap, ScoutingStrategy scoutingStrategy,
			Player self, Player enemy, BuildingPlanner buildingPlanner,
			DamageEvaluator damageEvaluator, InteractionHandler interactionHandler) {
	
		super(mapDrawer, bwMap, scoutingStrategy, self, enemy, buildingPlanner, damageEvaluator, interactionHandler);
		
		this.buildOrder = new ArrayList<BoAction>();
		this.myUnitInventory = self.getUnitInventory();
	}
	
	@Override
	public void initialize() {
		
		this.boPointer = 0;
		this.buildOrder.clear();
	}
	
	@Override
	public void start(int startMinerals, int startGas) {
		
		this.myUnitInventory.getAllWorkers().addListener(workerListener);
		this.myUnitInventory.getBuildings().addListener(buildingsListener);
		
		// add actions here. this just a random build order building some depots, a barracks, and a factory.
		this.buildOrder.add(new TrainWorkerAction(myUnitInventory.getMain()));
		this.buildOrder.add(new TrainWorkerAction(myUnitInventory.getMain()));
		this.buildOrder.add(new TrainWorkerAction(myUnitInventory.getMain()));
		this.buildOrder.add(new TrainWorkerAction(myUnitInventory.getMain()));
		this.buildOrder.add(new ConstructionAction(buildingPlanner, SupplyDepot.getInstance(bwMap)));
		this.buildOrder.add(new TrainWorkerAction(myUnitInventory.getMain()));
		this.buildOrder.add(new TrainWorkerAction(myUnitInventory.getMain()));
		this.buildOrder.add(new ConstructionAction(buildingPlanner, Barracks.getInstance(bwMap)));
		this.buildOrder.add(new TrainWorkerAction(myUnitInventory.getMain()));
		this.buildOrder.add(new TrainWorkerAction(myUnitInventory.getMain()));
		this.buildOrder.add(new ConstructionAction(buildingPlanner, Refinery.getInstance(bwMap)));
		this.buildOrder.add(new TrainMarineAction(myUnitInventory.getBarracks()));
		this.buildOrder.add(new TrainWorkerAction(myUnitInventory.getMain()));
		this.buildOrder.add(new TrainMarineAction(myUnitInventory.getBarracks()));
		this.buildOrder.add(new ConstructionAction(buildingPlanner, SupplyDepot.getInstance(bwMap)));
		this.buildOrder.add(new TrainWorkerAction(myUnitInventory.getMain()));
		this.buildOrder.add(new TrainWorkerAction(myUnitInventory.getMain()));
		this.buildOrder.add(new ConstructionAction(buildingPlanner, Factory.getInstance(bwMap)));
		this.buildOrder.add(new TrainWorkerAction(myUnitInventory.getMain()));
		this.buildOrder.add(new TrainMarineAction(myUnitInventory.getBarracks()));
		this.buildOrder.add(new TrainMarineAction(myUnitInventory.getBarracks()));
	}
	
	@Override
	public void run(int frame, int availableMinerals, int availableGas, int availableSupply) {
		
		if (boPointer < buildOrder.size()) {
			BoAction action = buildOrder.get(boPointer);
			if (action.execute(availableMinerals, availableGas, availableSupply)) {
				this.boPointer++;
				logger.debug("executed BO step: " + action);
			}
		}
	}
}
