package org.openbw.tsbw;

import java.util.List;

import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.unit.MobileUnit;
import org.openbw.tsbw.micro.MicroMath;

public class Squad<T extends MobileUnit> extends Group<T> {

	private static final long serialVersionUID = 36098407138275454L;
	private String name;
	
	/* default */ Squad() {
		super();
	}
	
	/* default */ Squad(String name) {
		super();
		this.name = name;
	}
	
	/* default */ Squad(String name, List<T> units) {
		super(units);
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public double getDistance(MobileUnit unit) {
		
		return this.getDistance(unit.getPosition());
	}
	
	public double getDistance(Position target) {
		
		if (this.isEmpty()) {
			return 0;
		}
		
		double minDistance = Double.MAX_VALUE;
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
