package org.openbw.tsbw.building;

import org.openbw.bwapi4j.InteractionHandler;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Factory;

public class MachineShopProject extends AddonProject {

	private Factory factory;
	
	public MachineShopProject(Factory factory, InteractionHandler interactionHandler) {
		
		super(UnitType.Terran_Machine_Shop, factory, interactionHandler);
		
		this.factory = factory;
	}
	
	@Override
	void construct() {
		
		this.factory.buildMachineShop();
	}

}
