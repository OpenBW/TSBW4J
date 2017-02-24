package org.openbw.tsbw;

import java.util.List;

import org.openbw.tsbw.micro.MicroMath;
import org.openbw.tsbw.unit.MobileUnit;

import bwapi.Position;

public class Squad<T extends MobileUnit> extends Group<T> {

	private static final long serialVersionUID = 36098407138275454L;

	/* default */ Squad(String name) {
		super(name);
	}
	
	/* default */ Squad(String name, List<T> units) {
		super(name, units);
	}
	
	public int getDistance(MobileUnit unit) {
		
		return this.getDistance(unit.getPosition());
	}
	
	public int getDistance(Position target) {
		
		if (this.isEmpty()) {
			return 0;
		}
		
		int minDistance = Integer.MAX_VALUE;
		for (MobileUnit squadUnit : this) {
			if (squadUnit.getDistance(target) < minDistance) {
				minDistance = squadUnit.getDistance(target);
			}
		}
		return minDistance;
	}

	public Position getSquadCenter() {
		
		double avg[] = MicroMath.getAverages(this);
		return new Position((int)avg[0], (int)avg[1]);
	}
}
