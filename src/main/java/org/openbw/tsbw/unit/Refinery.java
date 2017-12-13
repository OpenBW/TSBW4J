package org.openbw.tsbw.unit;

public class Refinery extends org.openbw.bwapi4j.unit.Refinery {

	public static double MINING_RATE_PER_SCV = 0.072;
	
	private int assignedScvs;
	
	protected Refinery(int id, int timeSpotted) {
		
		super(id, timeSpotted);
		this.assignedScvs = 0;
	}

	public double getMiningRate() {
	
		return this.assignedScvs * MINING_RATE_PER_SCV;
	}
	
	public void resetScvCount() {
		
		this.assignedScvs = 0;
	}
	
	public void removeScv() {
		
		this.assignedScvs--;
	}

	public void addScv() {
		
		this.assignedScvs++;
	}
}
