package org.openbw.tsbw.example.mining;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbw.bwapi.InteractionHandler;
import org.openbw.bwapi.MapDrawer;
import org.openbw.tsbw.Group;
import org.openbw.tsbw.GroupListener;
import org.openbw.tsbw.Squad;
import org.openbw.tsbw.micro.math.AssignmentProblem;
import org.openbw.tsbw.strategy.MiningStrategy;
import org.openbw.tsbw.unit.CommandCenter;
import org.openbw.tsbw.unit.MineralPatch;
import org.openbw.tsbw.unit.Worker;

import bwapi.Color;
import bwapi.Key;

public class SimpleMiningStrategy implements MiningStrategy {

	private static final Logger logger = LogManager.getLogger();
	
	protected MapDrawer mapDrawer;
	protected InteractionHandler interactionHandler;
	
	protected Squad<Worker> miningSquad;
	protected Group<CommandCenter> commandCenters;
	protected Group<MineralPatch> mineralPatches;
	protected List<Assignment> assignments;
	protected Map<CommandCenter, MiningSquad> workerSquads;
	
	private GroupListener<Worker> miningWorkerListener = new GroupListener<Worker>() {

		@Override
		public void onAdd(Worker worker) {
			
			addWorker(worker);
		}

		@Override
		public void onRemove(Worker worker) {
			
			for (Assignment assignment : assignments) {
				if (assignment.getWorker().equals(worker)) {
					logger.debug("removing SCV {} from mineral patch {}", worker.getID(), assignment.getMineralPatch().getID());
					
					// re-sort
					MineralPatch patch = assignment.getMineralPatch();
					mineralPatches.remove(patch);
					patch.removeScv();
					mineralPatches.add(patch);
					
					assignments.remove(assignment);
					CommandCenter commandCenter = patch.getClosestCommandCenter();
					MiningSquad squad = workerSquads.get(commandCenter);
					squad.remove(worker);
					break;
				}
			}
		}

		@Override
		public void onDestroy(Worker worker) {
			
			// do nothing
		}
		
	};
	
	private GroupListener<MineralPatch> mineralPatchListener = new GroupListener<MineralPatch>() {

		@Override
		public void onAdd(MineralPatch patch) {
			
			logger.trace("Mineral patch {} was added", patch.getID()); // should only happen in the context of re-sorting patches
		}

		@Override
		public void onRemove(MineralPatch patch) {
			
			logger.trace("Mineral patch {} was removed", patch.getID()); // should only happen in the context of re-sorting patches
		}

		@Override
		public void onDestroy(MineralPatch patch) {
			
			logger.trace("Mineral patch {} was destroyed.", patch.getID());
			
			// find all assignments to remove
			List<Assignment> assignmentsToRemove = new LinkedList<Assignment>();
			for (Assignment assignment : assignments) {
				if (assignment.getMineralPatch().equals(patch)) {
					assignmentsToRemove.add(assignment);
				}
			}
			
			// remove all assignments
			List<Worker> workersToReassign = new LinkedList<Worker>();
			for (Assignment assignment : assignmentsToRemove) {
				workersToReassign.add(assignment.getWorker());
				assignments.remove(assignment);
			}
			
			//reassign all workers
			for (Worker worker : workersToReassign) {
				logger.info("reassigning worker {} because its mineral patch {} got destroyed", worker.getID(), patch.getID());
				addWorker(worker);
			}
		}
		
	};
	
	private GroupListener<CommandCenter> commandCenterListener = new GroupListener<CommandCenter>() {

		@Override
		public void onAdd(CommandCenter commandCenter) {
			
			reinitialize();
		}

		@Override
		public void onRemove(CommandCenter commandCenter) {
			
			// do nothing
		}

		@Override
		public void onDestroy(CommandCenter commandCenter) {
			
			reinitialize();
		}
		
		private void reinitialize() {
			
			updatePatchDistances(true);
			
			MineralPatch[] selectedPatches = new MineralPatch[miningSquad.size()];
			double[][] distances = new double[miningSquad.size()][];
			
			// find the m mineral patches that will be mined (multiple occurrence of same patch is allowed)
			for (int i = 0; i < miningSquad.size(); i++) {
				
				MineralPatch targetPatch = mineralPatches.first();
				selectedPatches[i] = targetPatch;
				logger.debug("best patch {} has roundtrip time {} and mining factor of {}", targetPatch.getID(), targetPatch.getRoundTripTime(), targetPatch.getMiningFactor());
				
				// re-sort
				mineralPatches.remove(targetPatch);
				targetPatch.addScv();
				mineralPatches.add(targetPatch);
			}
			
			// calculate distance from each worker to each selected patch
			int i = 0;
			for (Worker worker : miningSquad) {
				
				distances[i] = new double[miningSquad.size()];
				for (int j = 0; j < miningSquad.size(); j++) {
					distances[i][j] = worker.getDistance(selectedPatches[j]);
					logger.debug("worker {} patch {}: d = {}", worker.getID(), selectedPatches[j].getID(), distances[i][j]);
				}
				i++;
			}
			
			// re-create the mining squads per base
			workerSquads.clear();
			for (CommandCenter commandCenter : commandCenters) {
				MiningSquad workerSquad = new MiningSquad();
				workerSquads.put(commandCenter, workerSquad);
			}
						
			// solve the linear assignment problem and assign workers accordingly
			assignments.clear();
			if (commandCenters.size() > 0) {
				AssignmentProblem assignmentProblem = new AssignmentProblem(distances);
				i = 0;
				for (Worker worker : miningSquad) {
					int j = assignmentProblem.sol(i);
					gatherMinerals(worker, selectedPatches[j]);
					i++;
				}
			}
		}
		
	};
	
	public SimpleMiningStrategy(MapDrawer mapDrawer, InteractionHandler interactionHandler) {
		
		this.mapDrawer = mapDrawer;
		this.interactionHandler = interactionHandler;
		this.assignments = new ArrayList<Assignment>();
		this.workerSquads = new HashMap<CommandCenter, MiningSquad>();
	}

	@Override
	public void initialize(Group<CommandCenter> commandCenters, Squad<Worker> miningSquad, Group<MineralPatch> mineralPatches) {
		
		this.commandCenters = commandCenters;
		this.miningSquad = miningSquad;
		this.mineralPatches = mineralPatches;
		this.workerSquads.clear();
		this.assignments.clear();
		
		this.commandCenters.addListener(this.commandCenterListener);
		this.miningSquad.addListener(this.miningWorkerListener);
		this.mineralPatches.addListener(this.mineralPatchListener);
		
		logger.debug("added " + mineralPatches.size() + " mineral patches.");
		
		for (CommandCenter commandCenter : commandCenters) {
			MiningSquad workerSquad = new MiningSquad();
			workerSquads.put(commandCenter, workerSquad);
		}
		
		updatePatchDistances(true);
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
		
		for (MineralPatch patch : patchesToUpdate) {
			
			// re-sort
			this.mineralPatches.remove(patch);
			patch.addScv();
			this.mineralPatches.add(patch);
		}
	}
	
	private void addWorker(Worker worker) {
		
		if (mineralPatches.isEmpty()) {
			logger.warn("Could not find suitable mineral patch!");
		} else if (commandCenters.size() == 0) {
			logger.warn("No command center left to return minerals to!");
		} else {
			MineralPatch targetPatch = mineralPatches.first();
			logger.debug("best patch {} has roundtrip time {} and mining factor of {}", targetPatch.getID(), targetPatch.getRoundTripTime(), targetPatch.getMiningFactor());
			
			gatherMinerals(worker, targetPatch);
			
			// re-sort
			this.mineralPatches.remove(targetPatch);
			targetPatch.addScv();
			this.mineralPatches.add(targetPatch);
		}
	}
	
	private void gatherMinerals(Worker worker, MineralPatch targetPatch) {
		
		boolean shift = false;
		if (worker.isCarryingMinerals()) {
			worker.returnCargo();
			shift = true;
		}
		
		this.assignments.add(new Assignment(worker, targetPatch));
		this.workerSquads.get(targetPatch.getClosestCommandCenter()).add(worker);
		
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
	 * Updates distances for all mineral patches and all command centers.
	 * @param wipeScvCount
	 */
	private void updatePatchDistances(boolean wipeScvCount) {
		
		for (CommandCenter commandCenter : this.commandCenters) {
			updatePatchDistances(commandCenter, wipeScvCount);
		}
	}
	
	/**
	 * For all mineral patches, check if the given command center shortens any distances. Then re-sort.
	 * @param commandCenter
	 * @param wipeScvCount
	 */
	private void updatePatchDistances(CommandCenter commandCenter, boolean wipeScvCount) {
		
		TreeSet<MineralPatch> patchesToReSort = new TreeSet<MineralPatch>();
		for(MineralPatch patch : mineralPatches) {
			patch.updateDistance(commandCenter, wipeScvCount);
			patchesToReSort.add(patch);
		}
		mineralPatches.clear();
		mineralPatches.addAll(patchesToReSort);
	}
	
	private void processIdleMiningWorkers() {
		
		for (Assignment assignment : assignments) {
			
			Worker worker = assignment.getWorker();
			if (worker.isIdle()) {
				
				if (assignment.getMineralPatch().isVisible()) {
					worker.gather(assignment.getMineralPatch());
				} else {
					worker.move(assignment.getMineralPatch().getPosition());
				}
			}
		}
	}
	
	protected void displayScvStatus() {
		
		for (Assignment assignment : assignments) {
			
			Worker worker = assignment.getWorker();
			MineralPatch patch = assignment.getMineralPatch();
			
			if (worker.isSelected()){
				mapDrawer.drawTextMap(worker.getX(), worker.getY() - 10, assignment.getStatus().toString());
				mapDrawer.drawLineMap(worker.getPosition(), patch.getPosition(), Color.Green);
				mapDrawer.drawTextMap(worker.getX(), worker.getY() - 20, "rtt:" + (int)patch.getRoundTripTime());
				mapDrawer.drawTextMap(worker.getX(), worker.getY() - 30, "ID:" + worker.getID());
			}
		}
	}
	
	protected void displayAssignments() {

		for (MineralPatch patch : this.mineralPatches) {
			patch.drawInfo(mapDrawer);
		}
		mapDrawer.drawTextScreen(300, 25, "total SCVs assigned: " + assignments.size());
	}
	
	@Override
	public void run(int frame) {
		
		if (interactionHandler.getKeyState(Key.K_D)) {
			displayAssignments();
			displayScvStatus();
		}
		
		processIdleMiningWorkers();
	}

	@Override
	public String toString() {
		return "simple";
	}
}
