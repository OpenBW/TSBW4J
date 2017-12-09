package org.openbw.tsbw.micro;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbw.tsbw.unit.MineralPatch;
import org.openbw.bwapi4j.unit.SCV;

public class GatherMineralsCommand implements Command {

	private static final Logger logger = LogManager.getLogger();
	
	private SCV worker;
	private MineralPatch targetPatch;
	
	public GatherMineralsCommand(SCV worker, MineralPatch targetPatch) {
		
		this.worker = worker;
		this.targetPatch = targetPatch;
	}

	@Override
	public boolean execute() {
			
		logger.debug("assigning {} to {} with factor {}.", this.worker, this.targetPatch, this.targetPatch.getMiningFactor());
		return this.worker.gather(this.targetPatch);
	}

	@Override
	public int getDelay() {
		
		return 3;
	}
	
	@Override
	public String toString() {
		return "command: 'gather minerals from " + targetPatch + "'";
	}
}
