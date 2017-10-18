package org.openbw.tsbw.example.strategy;

import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Building;
import org.openbw.bwapi4j.unit.CommandCenter;
import org.openbw.bwapi4j.unit.Factory;
import org.openbw.bwapi4j.unit.ScienceFacility;
import org.openbw.bwapi4j.unit.Starport;

public class AddonAction implements BoAction {

	private Building building;
	private UnitType addon;
	
	/* default */ AddonAction(Building building, UnitType addon) {
		
		this.building = building;
		this.addon = addon;
	}
	
	@Override
	public boolean execute(int availableMinerals, int availableGas, int availableSupply) {
		
		if (addon == UnitType.Terran_Comsat_Station) {
			
			if (building instanceof CommandCenter) {
				return ((CommandCenter) building).buildComsatStation();
			}
		} else if (addon == UnitType.Terran_Control_Tower) {
			
			if (building instanceof Starport) {
				return ((Starport) building).buildControlTower();
			}
		} else if (addon == UnitType.Terran_Covert_Ops) {
			
			if (building instanceof ScienceFacility) {
				return ((ScienceFacility) building).buildCovertOps();
			}
		} else if (addon == UnitType.Terran_Machine_Shop) {
			
			if (building instanceof Factory) {
				return ((Factory) building).buildMachineShop();
			}
		} else if (addon == UnitType.Terran_Nuclear_Silo) {
			
			if (building instanceof CommandCenter) {
				return ((CommandCenter) building).buildNuclearSilo();
			}
		} else if (addon == UnitType.Terran_Physics_Lab) {
			
			if (building instanceof ScienceFacility) {
				return ((ScienceFacility) building).buildPhysicslab();
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return "action: build addon " + addon;
	}

}
