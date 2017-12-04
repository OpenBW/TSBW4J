package org.openbw.tsbw.micro;

import org.openbw.bwapi4j.unit.MobileUnit;

public abstract class Command {

	protected int issuedAt;
	
	public Command(int issuedAt) {
		this.issuedAt = issuedAt;
	}
	
	public int getIssuedAt() {
		return issuedAt;
	}
	
	public abstract boolean execute(MobileUnit unit);
	
	public abstract int getDelay();
}
