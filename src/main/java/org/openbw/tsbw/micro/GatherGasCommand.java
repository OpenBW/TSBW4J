package org.openbw.tsbw.micro;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbw.bwapi4j.unit.SCV;
import org.openbw.tsbw.unit.Refinery;

public class GatherGasCommand implements Command {

	private static final Logger logger = LogManager.getLogger();
	
	private SCV worker;
	private Refinery targetRefinery;
	
	public GatherGasCommand(SCV worker, Refinery targetRefinery) {
		
		this.worker = worker;
		this.targetRefinery = targetRefinery;
	}
	
	@Override
	public boolean execute() {
		
		logger.debug("assigning {} to {}.", this.worker, this.targetRefinery);
		return this.worker.gather(this.targetRefinery);
	}

	@Override
	public int getDelay() {
		
		return 10;
	}
	
	@Override
	public String toString() {
		return "command: 'gather gas from " + targetRefinery + "'";
	}
}
