package org.openbw.tsbw.example.strategy;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbw.bwapi4j.BW;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Building;
import org.openbw.bwapi4j.unit.Factory;
import org.openbw.bwapi4j.unit.Refinery;
import org.openbw.bwapi4j.unit.SCV;
import org.openbw.tsbw.GroupListener;
import org.openbw.tsbw.MapAnalyzer;
import org.openbw.tsbw.UnitInventory;
import org.openbw.tsbw.building.BuildingPlanner;
import org.openbw.tsbw.building.ConstructionType;
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
			
			logger.info("frame {}: building {} was added.", interactionHandler.getFrameCount(), building);
			
			// if our refinery finished, add an additional workers to mine gas from it
			if (building instanceof Refinery) {
				myInventory.getMineralWorkers().first().gather((Refinery)building);
			} else if (building instanceof Factory) {
				buildOrder.add(new AddonAction(myInventory.getFactories().first(), UnitType.Terran_Machine_Shop));
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
	
	/* default */ BuildOrderStrategy(BW bw, MapAnalyzer mapAnalyzer, ScoutingStrategy scoutingStrategy, BuildingPlanner buildingPlanner, UnitInventory myInventory, UnitInventory enemyInventory) {
	
		super(bw, mapAnalyzer, scoutingStrategy, buildingPlanner);
		
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
		this.buildOrder.add(new ConstructionAction(buildingPlanner, ConstructionType.Terran_Supply_Depot));
		this.buildOrder.add(new TrainWorkerAction(myInventory.getMain()));
		this.buildOrder.add(new TrainWorkerAction(myInventory.getMain()));
		this.buildOrder.add(new ConstructionAction(buildingPlanner, ConstructionType.Terran_Barracks));
		this.buildOrder.add(new TrainWorkerAction(myInventory.getMain()));
		this.buildOrder.add(new TrainWorkerAction(myInventory.getMain()));
		this.buildOrder.add(new ConstructionAction(buildingPlanner, ConstructionType.Terran_Refinery));
		this.buildOrder.add(new TrainMarineAction(myInventory));
		this.buildOrder.add(new TrainWorkerAction(myInventory.getMain()));
		this.buildOrder.add(new TrainMarineAction(myInventory));
		this.buildOrder.add(new ConstructionAction(buildingPlanner, ConstructionType.Terran_Supply_Depot));
		this.buildOrder.add(new TrainWorkerAction(myInventory.getMain()));
		this.buildOrder.add(new TrainWorkerAction(myInventory.getMain()));
		this.buildOrder.add(new ConstructionAction(buildingPlanner, ConstructionType.Terran_Factory));
		this.buildOrder.add(new TrainWorkerAction(myInventory.getMain()));
		this.buildOrder.add(new TrainMarineAction(myInventory));
		this.buildOrder.add(new TrainMarineAction(myInventory));
		this.buildOrder.add(new ConstructionAction(buildingPlanner, ConstructionType.Terran_Academy));
		this.buildOrder.add(new ConstructionAction(buildingPlanner, ConstructionType.Terran_Engineering_Bay));
		this.buildOrder.add(new ConstructionAction(buildingPlanner, ConstructionType.Terran_Bunker));
		this.buildOrder.add(new ConstructionAction(buildingPlanner, ConstructionType.Terran_Missile_Turret));
		this.buildOrder.add(new ConstructionAction(buildingPlanner, ConstructionType.Terran_Armory));
		this.buildOrder.add(new ConstructionAction(buildingPlanner, ConstructionType.Terran_Starport));
		this.buildOrder.add(new ConstructionAction(buildingPlanner, ConstructionType.Terran_Science_Facility));
	}
	
	@Override
	public void run(int frame, int availableMinerals, int availableGas, int availableSupply) {
		
		if (boPointer < buildOrder.size()) {
			BoAction action = buildOrder.get(boPointer);
			if (action.execute(availableMinerals, availableGas, availableSupply)) {
				this.boPointer++;
				logger.debug("frame {}: executed BO step: {}", frame, action);
			}
		}
	}
}
