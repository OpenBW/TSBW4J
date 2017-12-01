package org.openbw.tsbw.example.strategy;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.type.Color;
import org.openbw.bwapi4j.type.WeaponType;
import org.openbw.bwapi4j.unit.Building;
import org.openbw.bwapi4j.unit.Factory;
import org.openbw.bwapi4j.unit.MobileUnit;
import org.openbw.bwapi4j.unit.Refinery;
import org.openbw.tsbw.GroupListener;
import org.openbw.tsbw.building.ConstructionType;
import org.openbw.tsbw.strategy.AbstractGameStrategy;
import org.openbw.tsbw.unit.SCV;

import bwta.Chokepoint;
import bwta.Polygon;
import bwta.Region;
	
/**
 * This strategy contains a very basic concept to execute a pre-defined build order.
 * The build order is defined in the start method, and then executed step by step in the run method.
 * 
 * You could write code to read a build order from a file and generate the corresponding actions to be executed.
 */
public class BuildOrderStrategy extends AbstractGameStrategy {

	private static final Logger logger = LogManager.getLogger();
	
	private List<BoAction> buildOrder;
	private int boPointer;
	
	private GroupListener<SCV> workerListener = new GroupListener<SCV>() {

		@Override
		public void onAdd(SCV worker) {
			
			worker.mine();
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
				
				for (int i = 0; i < 3; i++) {
					SCV gasMiner = myInventory.getAvailableWorker();
					if (gasMiner != null) {
						gasMiner.setAvailable(false);
						gasMiner.gather((Refinery) building);
					}
				}
			} else if (building instanceof Factory) {
				buildingPlanner.queueMachineShop((Factory)building);
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
	
	public BuildOrderStrategy() {
	
	}
		
	@Override
	public void start(int startMinerals, int startGas) {
		
		this.buildOrder = new ArrayList<BoAction>();
		this.boPointer = 0;
		this.buildOrder.clear();
		
		this.myInventory.getWorkers().addListener(workerListener);
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
		
		drawGraphics();
		
		if (boPointer < buildOrder.size()) {
			BoAction action = buildOrder.get(boPointer);
			if (action.execute(availableMinerals, availableGas, availableSupply)) {
				this.boPointer++;
				logger.debug("frame {}: executed BO step: {}", frame, action);
			}
		}
	}
	
	private void drawGraphics() {

		for (Region region : this.mapAnalyzer.getRegions()) {
			
			Polygon polygon = region.getPolygon();
			Position previousPoint = polygon.getPoints().get(polygon.getPoints().size() - 1);
			for (Position point : polygon.getPoints()) {

				mapDrawer.drawLineMap(previousPoint, point, Color.GREEN);
				previousPoint = point;
			}
		}
		mapDrawer.drawCircleMap(2512, 2408, 50, Color.RED);
		mapDrawer.drawCircleMap(2656, 2848, 50, Color.RED);
		for (Chokepoint chokepoint : this.mapAnalyzer.getChokepoints()) {
			
			if (chokepoint.getSides().first == null) {
				mapDrawer.drawCircleMap(chokepoint.getSides().second, 50, Color.RED);
				System.out.println(chokepoint.getSides().second);
			} else if (chokepoint.getSides().second == null) {
				mapDrawer.drawCircleMap(chokepoint.getSides().first, 50, Color.RED);
				System.out.println(chokepoint.getSides().first);
			} else {
				mapDrawer.drawLineMap(chokepoint.getSides().first, chokepoint.getSides().second, Color.YELLOW);
			}
			mapDrawer.drawTextMap(chokepoint.getCenter(), "" + this.mapAnalyzer.getValue(chokepoint));
		}

		for (MobileUnit unit : myInventory.getArmyUnits()) {
			
			mapDrawer.drawTextMap(unit.getPosition(), unit.getTilePosition().toString());
			if (unit.getTargetPosition() != null && unit.getGroundWeapon() != WeaponType.None) {
				mapDrawer.drawLineMap(unit.getPosition(), unit.getTargetPosition(), Color.RED);
				mapDrawer.drawCircleMap(unit.getPosition(), unit.getGroundWeapon().maxRange() + unit.height() / 2, Color.RED);
			}
		}
		
		for (SCV unit : myInventory.getWorkers()) {
			
			mapDrawer.drawTextMap(unit.getPosition(), unit.getTilePosition().toString());
			if (unit.isMoving()) {
				
				mapDrawer.drawLineMap(unit.getPosition(), unit.getTargetPosition(), Color.RED);
			}
		}
	}
}
