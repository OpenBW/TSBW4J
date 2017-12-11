package org.openbw.tsbw.unit;

class GatherGasMessage extends Message {

	private Refinery refinery;
	
	public GatherGasMessage(Refinery refinery) {
		
		super("");
		this.refinery = refinery;
	}
	
	public Refinery getRefinery() {
		
		return this.refinery;
	}
}
