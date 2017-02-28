package org.openbw.tsbw.example.mining;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbw.bwapi.InteractionHandler;
import org.openbw.bwapi.MapDrawer;
import org.openbw.tsbw.Group;
import org.openbw.tsbw.GroupListener;
import org.openbw.tsbw.Squad;
import org.openbw.tsbw.strategy.MiningStrategy;
import org.openbw.tsbw.unit.CommandCenter;
import org.openbw.tsbw.unit.MineralPatch;
import org.openbw.tsbw.unit.Worker;

public class SimpleMiningStrategy implements MiningStrategy {

	private static final Logger logger = LogManager.getLogger();
	
	protected MapDrawer mapDrawer;
	protected InteractionHandler interactionHandler;
	
	protected Squad<Worker> miningSquad;
	protected Group<CommandCenter> commandCenters;
	protected Group<MineralPatch> mineralPatches;
	
	private GroupListener<Worker> miningWorkerListener = new GroupListener<Worker>() {

		@Override
		public void onAdd(Worker worker) {
			
			addWorker(worker);
		}

		@Override
		public void onRemove(Worker worker) {
			
			// do nothing
		}

		@Override
		public void onDestroy(Worker worker) {
			
			// do nothing
		}
		
	};
	
	public SimpleMiningStrategy(MapDrawer mapDrawer, InteractionHandler interactionHandler) {
		
		this.mapDrawer = mapDrawer;
		this.interactionHandler = interactionHandler;
	}

	@Override
	public void initialize(Group<CommandCenter> commandCenters, Squad<Worker> miningSquad, Group<MineralPatch> mineralPatches) {
		
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
		
		for (Worker worker : this.miningSquad) {
			if (iterator.hasNext()) {
				MineralPatch patch = iterator.next();
				logger.debug("mineral patch {} chosen. roundtrip time: {}", patch.getID(), patch.getRoundTripTime());
				gatherMinerals(worker, patch);
				patchesToUpdate.add(patch);
			}
		}
	}
	
	private void addWorker(Worker worker) {
		
		if (mineralPatches.isEmpty()) {
			logger.warn("Could not find suitable mineral patch!");
		} else if (commandCenters.isEmpty()) {
			logger.warn("No command center left to return minerals to!");
		} else {
			MineralPatch targetPatch = mineralPatches.first();
			logger.debug("best patch {} has roundtrip time {} and mining factor of {}", targetPatch.getID(), targetPatch.getRoundTripTime(), targetPatch.getMiningFactor());
			
			gatherMinerals(worker, targetPatch);
		}
	}
	
	private void gatherMinerals(Worker worker, MineralPatch targetPatch) {
		
		boolean shift = false;
		if (worker.isCarryingMinerals()) {
			worker.returnCargo();
			shift = true;
		}
		
		if (targetPatch.isVisible()) {
			
			boolean success = worker.gather(targetPatch, shift);
			
			if (!success) {
				logger.debug("gather command failed with error {}", interactionHandler.getLastError());
			}
		} else {
			worker.move(targetPatch.getPosition(), shift);
		}
		logger.debug("assigning SCV {} to mineral patch {} with factor {}", worker.getID(), targetPatch.getID(), targetPatch.getMiningFactor());
	}

	/**
	 * For all mineral patches, check if the given command center shortens any distances. Then re-sort.
	 * @param commandCenter
	 * @param wipeScvCount
	 */
	private void updatePatchDistances(CommandCenter commandCenter) {
		
		TreeSet<MineralPatch> patchesToReSort = new TreeSet<MineralPatch>();
		for(MineralPatch patch : mineralPatches) {
			patch.updateDistance(commandCenter, true);
			patchesToReSort.add(patch);
		}
		mineralPatches.clear();
		mineralPatches.addAll(patchesToReSort);
	}
	
	private void processIdleMiningWorkers() {
		
		for (Worker worker : this.miningSquad) {
			if (worker.isIdle()) {
				addWorker(worker);
			}
		}
	}
	
	@Override
	public void run(int frame) {
		
		processIdleMiningWorkers();
	}
}
