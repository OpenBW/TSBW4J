package org.openbw.tsbw.building;

public class Message {

	private int minerals;
	private int gas;
	private boolean buildingCompleted;
	private int frame;
	
	public Message(int minerals, int gas, int frame) {
		
		this.minerals = minerals;
		this.gas = gas;
		this.buildingCompleted = false;
		this.frame = frame;
	}

	public Message(int frame, boolean buildingCompleted) {
	
		this.minerals = 0;
		this.gas = 0;
		this.buildingCompleted = buildingCompleted;
		this.frame = frame;
	}
	
	public int getMinerals() {
		
		return minerals;
	}

	public int getGas() {
		
		return gas;
	}

	public boolean isBuildingCompleted() {
		
		return this.buildingCompleted;
	}
	
	public int getFrame() {
		
		return frame;
	}
}
