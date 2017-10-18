package org.openbw.tsbw.building;

public class Message {

	private int minerals;
	private int gas;
	private int frame;
	
	public Message(int minerals, int gas, int frame) {
		
		this.minerals = minerals;
		this.gas = gas;
		this.frame = frame;
	}

	public int getMinerals() {
		return minerals;
	}

	public int getGas() {
		return gas;
	}

	public int getFrame() {
		return frame;
	}
}
