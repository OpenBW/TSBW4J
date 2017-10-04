package org.openbw.tsbw.strategy;


import org.openbw.bwapi4j.unit.CommandCenter;
import org.openbw.bwapi4j.unit.Refinery;
import org.openbw.bwapi4j.unit.SCV;
import org.openbw.tsbw.Group;
import org.openbw.tsbw.Squad;
import org.openbw.tsbw.unit.MineralPatch;

public interface MiningStrategy {

	public void initialize(Group<CommandCenter> commandCenters, Group<Refinery> refineries, Squad<SCV> miningSquad, Squad<SCV> gasSquad, Group<MineralPatch> mineralPatches);
	
	public void run(int frame);
}
