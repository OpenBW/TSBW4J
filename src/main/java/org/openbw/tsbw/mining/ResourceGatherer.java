package org.openbw.tsbw.mining;

import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbw.bwapi4j.unit.CommandCenter;
import org.openbw.tsbw.FrameUpdate;
import org.openbw.tsbw.Group;
import org.openbw.tsbw.GroupListener;
import org.openbw.tsbw.Squad;
import org.openbw.tsbw.Subscriber;
import org.openbw.tsbw.micro.math.AssignmentProblem;
import org.openbw.tsbw.unit.MineralPatch;
import org.openbw.tsbw.unit.SCV;
import org.openbw.tsbw.unit.VespeneGeyser;

public class ResourceGatherer implements Subscriber<FrameUpdate> {

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
	
	private GroupListener<SCV> workerListener = new GroupListener<SCV>() {

		@Override
		public void onAdd(SCV worker) {
			
			insertActor(worker);
		}

		@Override
		public void onRemove(SCV worker) {
			
			
		}

		@Override
		public void onDestroy(SCV worker) {
			
		}
		
	};
	
	private Group<MineralPatch> mineralPatches;
	private Group<VespeneGeyser> geysers;
	private Group<CommandCenter> commandCenters;
	private Squad<SCV> scvs;
	private WorkerBoard publicBoard;
	
	public ResourceGatherer() {
		
	}
	
	private void insertActor(SCV scv) {
		
		WorkerActor actor = new WorkerActor(scv, publicBoard);
		actor.initialize(mineralPatches, geysers);
		scv.setActor(actor);
	}
	
	public void initialize(Squad<SCV> scvs, Group<CommandCenter> commandCenters, Group<MineralPatch> mineralPatches, Group<VespeneGeyser> geysers) {
		
		this.scvs = scvs;
		this.commandCenters = commandCenters;
		this.mineralPatches = mineralPatches;
		this.geysers = geysers;
		this.publicBoard = new WorkerBoard();
		this.commandCenters.addListener(commandCenterListener);
		for (MineralPatch patch : mineralPatches) {
			
			patch.updateDistance(commandCenters);
		}
		for (SCV scv : scvs) {
			
			insertActor(scv);
		}
		scvs.addListener(workerListener);
		
	}
	
	private void reinitialize() {
		
		if (!this.commandCenters.isEmpty() && !this.mineralPatches.isEmpty()) {
			
			Set<SCV> scvs = new HashSet<>();
			scvs.addAll(publicBoard.getMineralMiningSCVs());
			int numberOfScvs = scvs.size();
			MineralPatch[] selectedPatches = new MineralPatch[numberOfScvs];
			double[][] distances = new double[numberOfScvs][numberOfScvs];
			
			// remove all scvs from patches
			int i = 0;
			for (SCV scv : scvs) {
				
				this.publicBoard.removeFromPatch(scv);
				i++;
			}
			
			// recalculate all distances and re-sort
			for (i = 0; i < numberOfScvs; i++) {
				
				MineralPatch targetPatch = this.mineralPatches.first();
				logger.trace("best patch {} has roundtrip time {} and mining factor of {}", targetPatch.getId(), targetPatch.getRoundTripTime(), targetPatch.getMiningFactor());
				
				this.mineralPatches.remove(targetPatch);
				targetPatch.addScv();
				this.mineralPatches.add(targetPatch);
				selectedPatches[i] = targetPatch;
				
				int j = 0;
				for (SCV worker : scvs) {
					
					distances[j][i] = worker.getDistance(selectedPatches[i]);
					j++;
				}
			}
			
			// solve assignment problem and re-assign workers to patches
			AssignmentProblem assignmentProblem = new AssignmentProblem(distances);
			i = 0;
			for (SCV worker : scvs) {
				
				int j = assignmentProblem.sol(i);
				selectedPatches[j].removeScv();
				this.publicBoard.assign(worker, selectedPatches[j]);
				i++;
			}
		}
	}

	@Override
	public void onReceive(FrameUpdate frameUpdate) {
		
		for (SCV scv : scvs) {
			
			scv.getActor().onFrame(frameUpdate);
		}
	}
}
