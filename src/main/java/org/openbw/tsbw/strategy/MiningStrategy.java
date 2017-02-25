package org.openbw.tsbw.strategy;


import org.openbw.tsbw.Group;
import org.openbw.tsbw.Squad;
import org.openbw.tsbw.unit.CommandCenter;
import org.openbw.tsbw.unit.MineralPatch;
import org.openbw.tsbw.unit.Worker;

public interface MiningStrategy {

	public void initialize(Group<CommandCenter> commandCenters, Squad<Worker> miningSquad, Group<MineralPatch> mineralPatches);
	
	public void run(int frame);
}
