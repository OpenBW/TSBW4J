package org.openbw.tsbw.unit;

import org.openbw.bwapi4j.unit.CommandCenter;
import org.openbw.bwapi4j.unit.Unit;

public class VespeneGeyser extends org.openbw.bwapi4j.unit.VespeneGeyser {

	private double distanceToClosestCC;
	
	VespeneGeyser(int id) {
		super(id);
		this.distanceToClosestCC = Integer.MAX_VALUE;
	}

	@Override
	public int compareTo(Unit otherUnit) {
		
		if (otherUnit instanceof VespeneGeyser) {
			int comparison = (int)(this.distanceToClosestCC - ((VespeneGeyser)otherUnit).distanceToClosestCC);
			if (comparison == 0) {
				return super.compareTo(otherUnit);
			} else {
				return comparison;
			}
		} else {
			return super.compareTo(otherUnit);
		}
	}

	public void updateDistance(CommandCenter commandCenter) {
		
		double distance = this.getDistance(commandCenter);
		if (distance < this.distanceToClosestCC) {
			this.distanceToClosestCC = distance;
		}
	}
}
