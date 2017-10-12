package org.openbw.tsbw.example.strategy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbw.bwapi4j.BW;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Barracks;
import org.openbw.bwapi4j.unit.Building;
import org.openbw.bwapi4j.unit.CommandCenter;
import org.openbw.bwapi4j.unit.MobileUnit;
import org.openbw.bwapi4j.unit.SCV;
import org.openbw.tsbw.Group;
import org.openbw.tsbw.GroupListener;
import org.openbw.tsbw.UnitInventory;
import org.openbw.tsbw.building.BuildingPlanner;
import org.openbw.tsbw.building.Construction;
import org.openbw.tsbw.strategy.AbstractGameStrategy;
import org.openbw.tsbw.strategy.ScoutingStrategy;

/**
 * This is an extremely basic strategy to get you started.
 * Take a look at the different listeners. This strategy reacts as follows to different events:
 *  - own worker spawned: add it to the mining squad
 *  - enemy army found: attack it with all we've got
 *  - enemy building found: attack it with all we've got
 *  - enemy building destroyed: go attack the next enemy building (if there is any)
 *  
 *  Apart from that the strategy 
 *   - keeps building workers until it has 16 workers
 *   - sends a scout at frame 3000
 *   - builds supply depots as needed
 *   - spends the rest of its available resources on barracks and marines
 */
public class DummyStrategy extends AbstractGameStrategy {

	private static final Logger logger = LogManager.getLogger();
	
	protected UnitInventory myInventory;
	protected UnitInventory enemyInventory;
	
	/**
	 * Listens to events affecting my workers.
	 */
	private GroupListener<SCV> workerListener = new GroupListener<SCV>() {

		@Override
		public void onAdd(SCV worker) {
			
			logger.info("worker {} was added.", worker);
			
			// let's add every new worker to the mining squad by default
			myInventory.getMineralWorkers().add(worker);
		}

		@Override
		public void onRemove(SCV worker) {
			
			logger.info("worker {} was removed.", worker);
		}

		@Override
		public void onDestroy(SCV worker) {
			
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
			
			// if we find an enemy, let's attack it with all our forces
			myInventory.getArmyUnits().stream().forEach(u -> u.attack(unit.getPosition()));
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
			
			// if we find an enemy building, let's move out to attack there with all our forces
			myInventory.getArmyUnits().stream().forEach(u -> u.attack(building.getPosition()));
		}

		@Override
		public void onRemove(Building building) {
			
			logger.info("enemy building {} was removed.", building);
			
			// if an enemy building was killed, let's go and attack the next one (if there is any)
			if (!enemyInventory.getBuildings().isEmpty()) {
				myInventory.getArmyUnits().stream().forEach(u -> u.attack(enemyInventory.getBuildings().first().getPosition()));
			}
		}

		@Override
		public void onDestroy(Building building) {
			
			logger.info("enemy building {} was destroyed.", building);
		}
	};
	
	public DummyStrategy(BW bw, ScoutingStrategy scoutingStrategy, BuildingPlanner buildingPlanner, UnitInventory myInventory, UnitInventory enemyInventory) {
	
		super(bw, scoutingStrategy, buildingPlanner);
		
		this.myInventory = myInventory;
		this.enemyInventory = enemyInventory;
	}
	
	@Override
	public void initialize() {
		
		// do any initial one-time setup here
	}

	@Override
	public void start(int startMinerals, int startGas) {
		
		// listeners have to be added after the game has started.
		// feel free to add or remove listeners as needed. Any Group can be listened to.
		
		this.myInventory.getBuildings().addListener(buildingsListener);
		this.myInventory.getArmyUnits().addListener(armyListener);
		this.myInventory.getAllWorkers().addListener(workerListener);
		this.enemyInventory.getBuildings().addListener(enemyBuildingsListener);
		this.enemyInventory.getArmyUnits().addListener(enemyArmyListener);
	}

	@Override
	public void run(int frame, int availableMinerals, int availableGas, int availableSupply) {
		
		// at frame 3000 send out a single scout to explore the map.
		// we do this by simply moving the first worker we find from the mining squad to the scouts squad.
		if (frame == 3000) {
			myInventory.getMineralWorkers().move(myInventory.getMineralWorkers().first(), myInventory.getScouts());
		}
				
		// train workers until we have 16 workers.
		// we keep count of available minerals while we decide on how to spend them.
		if (this.myInventory.getAllWorkers().size() < 16 && availableMinerals >= 50) {
			availableMinerals -= trainWorker();
		}
		
		// as long as we have extra minerals available, spend it on marines
		for (Barracks barracks : myInventory.getBarracks()) {
			if (!barracks.isTraining() && availableMinerals >= 50) {
				barracks.trainMarine();
				availableMinerals -= 50;
			}
		}
				
		// build supply depots as required: if available supply is less than some threshold queue up a supply depot to be built.
		// in this case, we make the threshold depend on the number of command centers and barracks we have.
		int threshold = myInventory.getCommandCenters().size() * 4 + myInventory.getBarracks().size() * 4;
		if (availableSupply + buildingPlanner.getCount(Construction.Terran_Supply_Depot) * UnitType.Terran_Supply_Depot.supplyProvided() <= threshold) {

			buildingPlanner.queue(Construction.Terran_Supply_Depot);
			availableMinerals -= Construction.Terran_Supply_Depot.getMineralPrice();
		}
		
		// "end-game"
		if (availableMinerals > 250) {
			
			this.buildingPlanner.queue(Construction.Terran_Barracks);
		}
		
		if (frame % 4000 == 0 && !this.enemyInventory.getBuildings().isEmpty()) {
			
			Building building = this.enemyInventory.getBuildings().first();
			myInventory.getArmyUnits().stream().forEach(u -> u.attack(building.getPosition()));
		}
	}

	/**
	 * Trains a worker only if there is currently no worker in the queue.
	 * @return minerals spent: 50 if a new worker is trained, 0 else.
	 */
	private int trainWorker() {
		
		Group<CommandCenter> commandCenters = this.myInventory.getCommandCenters();
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
