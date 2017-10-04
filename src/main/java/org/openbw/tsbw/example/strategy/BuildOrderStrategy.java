package org.openbw.tsbw.example.strategy;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbw.bwapi4j.BW;
import org.openbw.bwapi4j.unit.Building;
import org.openbw.bwapi4j.unit.Refinery;
import org.openbw.bwapi4j.unit.SCV;
import org.openbw.tsbw.GroupListener;
import org.openbw.tsbw.UnitInventory;
import org.openbw.tsbw.building.BuildingPlanner;
import org.openbw.tsbw.building.Construction;
import org.openbw.tsbw.strategy.AbstractGameStrategy;
import org.openbw.tsbw.strategy.ScoutingStrategy;
	
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
	
	private UnitInventory myInventory;
	
	private GroupListener<SCV> workerListener = new GroupListener<SCV>() {

		@Override
		public void onAdd(SCV worker) {
			myInventory.getMineralWorkers().add(worker);
		}

		@Override
		public void onRemove(SCV worker) {
			
			// do nothing
		}

		@Override
		public void onDestroy(SCV worker) {
			
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
				myInventory.getMineralWorkers().first().gather((Refinery)building);
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
	
	/* default */ BuildOrderStrategy(BW bw, ScoutingStrategy scoutingStrategy, BuildingPlanner buildingPlanner, UnitInventory myInventory, UnitInventory enemyInventory) {
	
		super(bw, scoutingStrategy, buildingPlanner);
		
		this.myInventory = myInventory;
		this.buildOrder = new ArrayList<BoAction>();
	}
		
	@Override
	public void initialize() {
		
		this.boPointer = 0;
		this.buildOrder.clear();
	}
	
	@Override
	public void start(int startMinerals, int startGas) {
		
		this.myInventory.getAllWorkers().addListener(workerListener);
		this.myInventory.getBuildings().addListener(buildingsListener);
		
		// add actions here. this just a random build order building some depots, a barracks, and a factory.
		this.buildOrder.add(new TrainWorkerAction(myInventory.getMain()));
		this.buildOrder.add(new TrainWorkerAction(myInventory.getMain()));
		this.buildOrder.add(new TrainWorkerAction(myInventory.getMain()));
		this.buildOrder.add(new TrainWorkerAction(myInventory.getMain()));
		this.buildOrder.add(new ConstructionAction(buildingPlanner, Construction.Terran_Supply_Depot));
		this.buildOrder.add(new TrainWorkerAction(myInventory.getMain()));
		this.buildOrder.add(new TrainWorkerAction(myInventory.getMain()));
		this.buildOrder.add(new ConstructionAction(buildingPlanner, Construction.Terran_Barracks));
		this.buildOrder.add(new TrainWorkerAction(myInventory.getMain()));
		this.buildOrder.add(new TrainWorkerAction(myInventory.getMain()));
		this.buildOrder.add(new ConstructionAction(buildingPlanner, Construction.Terran_Refinery));
		this.buildOrder.add(new TrainMarineAction(myInventory.getBarracks()));
		this.buildOrder.add(new TrainWorkerAction(myInventory.getMain()));
		this.buildOrder.add(new TrainMarineAction(myInventory.getBarracks()));
		this.buildOrder.add(new ConstructionAction(buildingPlanner, Construction.Terran_Supply_Depot));
		this.buildOrder.add(new TrainWorkerAction(myInventory.getMain()));
		this.buildOrder.add(new TrainWorkerAction(myInventory.getMain()));
		this.buildOrder.add(new ConstructionAction(buildingPlanner, Construction.Terran_Factory));
		this.buildOrder.add(new TrainWorkerAction(myInventory.getMain()));
		this.buildOrder.add(new TrainMarineAction(myInventory.getBarracks()));
		this.buildOrder.add(new TrainMarineAction(myInventory.getBarracks()));
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
