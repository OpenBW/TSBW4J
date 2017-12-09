package org.openbw.tsbw.micro;

import org.openbw.tsbw.unit.Refinery;

public class MineGasMessage extends Message {

	private Refinery refinery;
	
	public MineGasMessage(Refinery refinery) {
		
		super("");
		this.refinery = refinery;
	}
	
	public Refinery getRefinery() {
		
		return this.refinery;
	}
}
