package org.openbw.tsbw.example.mining;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbw.bwapi4j.InteractionHandler;
import org.openbw.bwapi4j.MapDrawer;
import org.openbw.bwapi4j.unit.CommandCenter;
import org.openbw.bwapi4j.unit.Refinery;
import org.openbw.bwapi4j.unit.SCV;
import org.openbw.tsbw.Group;
import org.openbw.tsbw.GroupListener;
import org.openbw.tsbw.MapAnalyzer;
import org.openbw.tsbw.Squad;
import org.openbw.tsbw.strategy.MiningStrategy;
import org.openbw.tsbw.unit.MineralPatch;

public class SimpleMiningStrategy implements MiningStrategy {

	private static final Logger logger = LogManager.getLogger();
	
	protected MapDrawer mapDrawer;
	protected MapAnalyzer mapAnalyzer;
	protected InteractionHandler interactionHandler;
	
	protected Squad<SCV> miningSquad;
	protected Group<CommandCenter> commandCenters;
	protected Group<MineralPatch> mineralPatches;
	
	private GroupListener<SCV> miningWorkerListener = new GroupListener<SCV>() {

		@Override
		public void onAdd(SCV worker) {
			
			addWorker(worker);
		}

		@Override
		public void onRemove(SCV worker) {
			
			// do nothing
		}

		@Override
		public void onDestroy(SCV worker) {
			
			// do nothing
		}
		
	};
	
	public SimpleMiningStrategy(MapAnalyzer mapAnalyzer, MapDrawer mapDrawer, InteractionHandler interactionHandler) {
		
		this.mapAnalyzer = mapAnalyzer;
		this.mapDrawer = mapDrawer;
		this.interactionHandler = interactionHandler;
	}

	@Override
	public void initialize(Group<CommandCenter> commandCenters, Group<Refinery> refineries, Squad<SCV> miningSquad, Squad<SCV> gasSquad, Group<MineralPatch> mineralPatches) {
		
		this.commandCenters = commandCenters;
		this.miningSquad = miningSquad;
		this.mineralPatches = mineralPatches;
		
		this.miningSquad.addListener(this.miningWorkerListener);
		
		logger.debug("added " + mineralPatches.size() + " mineral patches.");
		
		for (CommandCenter commandCenter : this.commandCenters) {
			updatePatchDistances(commandCenter);
		}
		firstTimeSpread();
	}

	private void firstTimeSpread() {
		
		Iterator<MineralPatch> iterator = mineralPatches.iterator();
		List<MineralPatch> patchesToUpdate = new ArrayList<MineralPatch>();
		
		for (SCV worker : this.miningSquad) {
			if (iterator.hasNext()) {
				MineralPatch patch = iterator.next();
				logger.debug("mineral patch {} chosen. roundtrip time: {}", patch.getId(), patch.getRoundTripTime());
				gatherMinerals(worker, patch);
				patchesToUpdate.add(patch);
			}
		}
	}
	
	private void addWorker(SCV worker) {
		
		if (mineralPatches.isEmpty()) {
			logger.warn("Could not find suitable mineral patch!");
		} else if (commandCenters.isEmpty()) {
			logger.warn("No command center left to return minerals to!");
		} else {
			MineralPatch targetPatch = mineralPatches.first();
			logger.debug("best patch {} has roundtrip time {} and mining factor of {}", targetPatch.getId(), targetPatch.getRoundTripTime(), targetPatch.getMiningFactor());
			
			gatherMinerals(worker, targetPatch);
		}
	}
	
	private void gatherMinerals(SCV worker, MineralPatch targetPatch) {
		
		boolean shift = false;
		if (worker.isCarryingMinerals()) {
			
			if (!worker.returnCargo()) {
				logger.debug("return cargo command for {} failed with error {}", worker, interactionHandler.getLastError());
			}
			shift = true;
		}
		
		if (targetPatch.isVisible()) {
			
			if (!worker.gather(targetPatch, shift)) {
				logger.debug("gather command for {} failed with error {}", worker, interactionHandler.getLastError());
			}
		} else {
			worker.move(targetPatch.getPosition(), shift);
		}
		logger.debug("assigning {} to {} with factor {}", worker, targetPatch, targetPatch.getMiningFactor());
	}

	/**
	 * For all mineral patches, check if the given command center shortens any distances. Then re-sort.
	 * @param commandCenter
	 * @param wipeScvCount
	 */
	private void updatePatchDistances(CommandCenter commandCenter) {
		
		TreeSet<MineralPatch> patchesToReSort = new TreeSet<MineralPatch>();
		for(MineralPatch patch : mineralPatches) {
			patch.updateDistance(this.mapAnalyzer, commandCenter, true);
			patchesToReSort.add(patch);
		}
		mineralPatches.clear();
		mineralPatches.addAll(patchesToReSort);
	}
	
//	private void processIdleMiningWorkers() {
//		
//		for (SCV worker : this.miningSquad) {
//			if (worker.isIdle()) {
//				addWorker(worker);
//			}
//		}
//	}
	
	@Override
	public void run(int frame) {
		
		// processIdleMiningWorkers();
	}
}
