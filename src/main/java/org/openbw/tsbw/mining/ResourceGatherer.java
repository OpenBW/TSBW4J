package org.openbw.tsbw.mining;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbw.bwapi4j.unit.CommandCenter;
import org.openbw.tsbw.Group;
import org.openbw.tsbw.GroupListener;
import org.openbw.tsbw.Squad;
import org.openbw.tsbw.micro.math.AssignmentProblem;
import org.openbw.tsbw.unit.MineralPatch;
import org.openbw.tsbw.unit.SCV;

public class ResourceGatherer {

	private static final Logger logger = LogManager.getLogger();
	
	private GroupListener<CommandCenter> commandCenterListener = new GroupListener<CommandCenter>() {

		@Override
		public void onAdd(CommandCenter commandCenter) {
			
			for (MineralPatch patch : mineralPatches) {
				
				patch.updateDistance(commandCenter);
			}
			reinitialize();
		}

		@Override
		public void onRemove(CommandCenter commandCenter) {
			
			// do nothing
		}

		@Override
		public void onDestroy(CommandCenter commandCenter) {
			
			for (MineralPatch patch : mineralPatches) {
				
				patch.updateDistance(commandCenters);
			}
			reinitialize();
		}
		
	};
	
	private Group<MineralPatch> mineralPatches;
	private Group<CommandCenter> commandCenters;
	private Squad<SCV> scvs;
	
	public ResourceGatherer() {
		
	}
	
	public void initialize(Squad<SCV> scvs, Group<CommandCenter> commandCenters, Group<MineralPatch> mineralPatches) {
		
		this.scvs = scvs;
		this.commandCenters = commandCenters;
		this.mineralPatches = mineralPatches;
		this.commandCenters.addListener(commandCenterListener);
		for (MineralPatch patch : mineralPatches) {
			
			patch.updateDistance(commandCenters);
		}
	}
	
	private void reinitialize() {
		
		if (!this.commandCenters.isEmpty() && !this.mineralPatches.isEmpty()) {
			
			List<SCV> miningScvs = this.scvs.stream().filter(w -> w.isGathering()).collect(Collectors.toList());
			
			int numberOfScvs = miningScvs.size();
			MineralPatch[] selectedPatches = new MineralPatch[numberOfScvs];
			double[][] distances = new double[numberOfScvs][numberOfScvs];
			
			for (MineralPatch patch : this.mineralPatches) {
				
				patch.resetScvCount();
			}
			
			logger.trace("solving WEKA for {} scvs and {} patches.", miningScvs.size(), this.mineralPatches.size());
			
			// recalculate all distances and re-sort
			for (int i = 0; i < numberOfScvs; i++) {
				
				MineralPatch targetPatch = this.mineralPatches.first();
				logger.trace("best patch {} has roundtrip time {} and mining factor of {}", targetPatch.getId(), targetPatch.getRoundTripTime(), targetPatch.getMiningFactor());
				
				this.mineralPatches.remove(targetPatch);
				targetPatch.addScv();
				this.mineralPatches.add(targetPatch);
				selectedPatches[i] = targetPatch;
				
				int j = 0;
				for (SCV worker : miningScvs) {
					
					distances[j][i] = worker.getDistance(selectedPatches[i]);
					j++;
				}
			}
			
			// solve assignment problem and re-assign workers to patches
			AssignmentProblem assignmentProblem = new AssignmentProblem(distances);
			int i = 0;
			for (SCV worker : miningScvs) {
				
				int j = assignmentProblem.sol(i);
				selectedPatches[j].removeScv();
				worker.gather(selectedPatches[j]);
				i++;
			}
		}
	}
}
