package org.openbw.tsbw.micro;

public interface Command {

	public boolean execute();
	
	public int getDelay();
}
