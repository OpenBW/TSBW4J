package org.openbw.tsbw.example.strategy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbw.bwapi.BWMap;
import org.openbw.bwapi.DamageEvaluator;
import org.openbw.bwapi.MapDrawer;
import org.openbw.tsbw.Group;
import org.openbw.tsbw.GroupListener;
import org.openbw.tsbw.UnitInventory;
import org.openbw.tsbw.building.BuildingPlanner;
import org.openbw.tsbw.strategy.AbstractGameStrategy;
import org.openbw.tsbw.strategy.ScoutingStrategy;
import org.openbw.tsbw.unit.Barracks;
import org.openbw.tsbw.unit.Building;
import org.openbw.tsbw.unit.CommandCenter;
import org.openbw.tsbw.unit.MobileUnit;
import org.openbw.tsbw.unit.SupplyDepot;
import org.openbw.tsbw.unit.Worker;

import bwapi.UnitType;

public class DefaultStrategy extends AbstractGameStrategy {

	private static final Logger logger = LogManager.getLogger();
	
	/**
	 * Listens to events affecting my workers.
	 */
	private GroupListener<Worker> workerListener = new GroupListener<Worker>() {

		@Override
		public void onAdd(Worker worker) {
			
			logger.info("worker {} was added.", worker);
			
			// to start with, this is the only event we react to.
			myUnitInventory.getMiningWorkers().add(worker);
		}

		@Override
		public void onRemove(Worker worker) {
			
			logger.info("worker {} was removed.", worker);
		}

		@Override
		public void onDestroy(Worker worker) {
			
			logger.info("worker {} was destroyed.", worker);
		}
	};
	
	/**
	 * Listens to events affecting my army units.
	 */
	private GroupListener<MobileUnit> armyListener = new GroupListener<MobileUnit>() {

		@Override
		public void onAdd(MobileUnit unit) {
			
			logger.info("unit {} was added.", unit);
		}

		@Override
		public void onRemove(MobileUnit unit) {
			
			logger.info("unit {} was removed.", unit);
		}

		@Override
		public void onDestroy(MobileUnit unit) {
			
			logger.info("unit {} was destroyed.", unit);
		}
	};
	
	/**
	 * Listens to events affecting my buildings.
	 */
	private GroupListener<Building> buildingsListener = new GroupListener<Building>() {

		@Override
		public void onAdd(Building building) {
			
			logger.info("building {} was added.", building);
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
	
	/**
	 * Listens to events affecting enemy army units.
	 */
	private GroupListener<MobileUnit> enemyArmyListener = new GroupListener<MobileUnit>() {

		@Override
		public void onAdd(MobileUnit unit) {
			
			logger.info("enemy unit {} was added.", unit);
		}

		@Override
		public void onRemove(MobileUnit unit) {
			
			logger.info("enemy unit {} was removed.", unit);
		}

		@Override
		public void onDestroy(MobileUnit unit) {
			
			logger.info("enemy unit {} was destroyed.", unit);
		}
	};
	
	/**
	 * Listens to events affecting enemy buildings.
	 */
	private GroupListener<Building> enemyBuildingsListener = new GroupListener<Building>() {

		@Override
		public void onAdd(Building building) {
			
			logger.info("enemy building {} was added.", building);
		}

		@Override
		public void onRemove(Building building) {
			
			logger.info("enemy building {} was removed.", building);
		}

		@Override
		public void onDestroy(Building building) {
			
			logger.info("enemy building {} was destroyed.", building);
		}
	};
	
	public DefaultStrategy(MapDrawer mapDrawer, BWMap bwMap, ScoutingStrategy scoutingStrategy,
			UnitInventory myUnitInventory, UnitInventory enemyUnitInventory, BuildingPlanner buildingPlanner,
			DamageEvaluator damageEvaluator) {
	
		super(mapDrawer, bwMap, scoutingStrategy, myUnitInventory, enemyUnitInventory, buildingPlanner, damageEvaluator);
	}
	
	@Override
	public void initialize() {
		
		// do any initial one-time setup here
	}

	@Override
	public void start(int startMinerals) {
		
		// listeners have to be added after the game has started.
		// feel free to add or remove listeners as needed. Any Group can be listened to.
		
		super.myUnitInventory.getBuildings().addListener(buildingsListener);
		super.myUnitInventory.getArmyUnits().addListener(armyListener);
		super.myUnitInventory.getAllWorkers().addListener(workerListener);
		super.enemyUnitInventory.getBuildings().addListener(enemyBuildingsListener);
		super.enemyUnitInventory.getArmyUnits().addListener(enemyArmyListener);
	}

	@Override
	public void run(int frame, int availableMinerals, int availableSupply) {
		
		// at frame 3000 send out a single scout to explore the map.
		// we do this by simply moving the first worker we find from the mining squad to the scouts squad.
		if (frame == 3000) {
			myUnitInventory.getMiningWorkers().move(myUnitInventory.getMiningWorkers().first(), myUnitInventory.getScouts());
		}
				
		// train workers until we have 16 workers.
		// we keep count of available minerals while we decide on how to spend them.
		if (this.myUnitInventory.getAllWorkers().size() < 16 && availableMinerals >= 50) {
			availableMinerals -= trainWorker();
		}
		
		// as long as we have extra minerals available, spend it on marines
		for (Barracks barracks : myUnitInventory.getBarracks()) {
			if (!barracks.isTraining() && availableMinerals >= 50) {
				barracks.trainMarine();
				availableMinerals -= 50;
			}
		}
				
		// build supply depots as required: if available supply is less than some threshold queue up a supply depot to be built.
		// in this case, we make the threshold depend on the number of command centers and barracks we have.
		int threshold = myUnitInventory.getCommandCenters().size() * 4 + myUnitInventory.getBarracks().size() * 4;
		if (availableSupply + buildingPlanner.getCount(SupplyDepot.getInstance(bwMap)) * UnitType.Terran_Supply_Depot.supplyProvided() <= threshold) {

			buildingPlanner.queue(SupplyDepot.getInstance(bwMap));
			availableMinerals -= SupplyDepot.getInstance(bwMap).getMineralPrice();
		}
		
		// "end-game"
		if (availableMinerals > 250) {
			
			this.buildingPlanner.queue(Barracks.getInstance(bwMap));
		}
	}

	/**
	 * Trains a worker only if there is currently no worker in the queue.
	 * @return minerals spent: 50 if a new worker is trained, 0 else.
	 */
	private int trainWorker() {
		
		Group<CommandCenter> commandCenters = this.myUnitInventory.getCommandCenters();
		if (!commandCenters.isEmpty()) {
			CommandCenter commandCenter = commandCenters.first();
			
			if (!commandCenter.isTraining()) {
				commandCenter.trainWorker();
				return 50;
			}
		}
		return 0;
	}
}
