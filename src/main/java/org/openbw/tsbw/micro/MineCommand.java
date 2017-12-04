package org.openbw.tsbw.micro;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbw.bwapi4j.unit.MobileUnit;
import org.openbw.bwapi4j.unit.Refinery;
import org.openbw.tsbw.mining.WorkerBoard;
import org.openbw.tsbw.unit.MineralPatch;
import org.openbw.tsbw.unit.SCV;

public class MineCommand extends Command {

	private static final Logger logger = LogManager.getLogger();
	
	private SCV worker;
	private MineralPatch targetPatch;
	private Refinery targetRefinery;
	private boolean miningGas;
	
	public MineCommand(int issuedAt, SCV worker, Refinery targetRefinery, WorkerBoard publicBoard) {
		
		super(issuedAt);
		this.worker = worker;
		this.targetRefinery = targetRefinery;
		this.miningGas = true;
	}
	
	public MineCommand(int issuedAt, SCV worker, MineralPatch targetPatch) {
		
		super(issuedAt);
		this.worker = worker;
		this.targetPatch = targetPatch;
		this.miningGas = false;
	}

	@Override
	public boolean execute(MobileUnit unit) {
		
		boolean shift = false;
		if (this.worker.isCarryingMinerals()) {
			
			this.worker.returnCargo();
			shift = true;
		}
		
		boolean success;
		
		if (miningGas) {
			
			success = this.worker.gather(this.targetRefinery);
			logger.debug("assigning {} to {}.", this.worker, this.targetRefinery);
		} else {
			
			if (this.targetPatch.isVisible()) {
				
				success = this.worker.gather(this.targetPatch, shift);
				
				if (!success) {
					
					logger.debug("gather command failed with an error.");
				}
			} else {
				
				success = this.worker.move(this.targetPatch.getPosition(), shift);
			}
			logger.debug("assigning {} to {} with factor {}.", this.worker, this.targetPatch, this.targetPatch.getMiningFactor());
		}
		return success;
	}

	@Override
	public int getDelay() {
		
		return 3;
	}
}
