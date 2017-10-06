package org.openbw.tsbw.unit;

import org.openbw.bwapi4j.BW;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Unit;

public class UnitFactory extends org.openbw.bwapi4j.unit.UnitFactory {

	public UnitFactory(BW bw) {
		super(bw);
	}

	@Override
	public Unit createUnit(int unitId, UnitType unitType, int timeSpotted) {
		
		Unit unit;
		switch (unitType) {
		case Resource_Mineral_Field:
			unit = new MineralPatch(unitId);
			break;
		case Resource_Vespene_Geyser:
			unit = new VespeneGeyser(unitId);
			break;
		default:
			unit = super.createUnit(unitId, unitType, timeSpotted);	
		}
		return unit;
	}
	
}
