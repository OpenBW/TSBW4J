package org.openbw.tsbw.mining;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.unit.Mechanical;
import org.openbw.bwapi4j.unit.MobileUnit;
import org.openbw.tsbw.Squad;
import org.openbw.tsbw.UnitInventory;

public class WorkerSquad {

//	private static final Logger logger = LogManager.getLogger();
//	
//	private Squad<MobileUnit> units;
//	private UnitInventory unitInventory;
//	private Position position;
//	private List<MobileUnit> hostileUnits;
//	
//	public WorkerSquad(UnitInventory unitInventory) {
//		
//	}
//	
//	public void add(MobileUnit unit) {
//		this.units.add(unit);
//	}
//	
//	public void remove(MobileUnit unit) {
//		this.units.remove(unit);
//	}
//	
//	public int size() {
//		return units.size();
//	}
//	
//	public Squad<MobileUnit> getSquad() {
//		return this.units;
//	}
//	
//	public void run() {
//		
//		Set<MobileUnit> toMove = new HashSet<MobileUnit>();
//		
//		for (MobileUnit fighter : this.units) {
//			
//			if (fighter instanceof SCV) {
//				if (fighter.getDistance(this.position) >= 192 || fighter.getHitPoints() <= 25) {
//					toMove.add(fighter);
//				} else {
//					this.getSquad().stream().filter(u -> u.getHitPoints() <= 25).findFirst().ifPresent(i -> ((SCV)fighter).repair((Mechanical)i));
//				}
//			}
//		}
//		for (MobileUnit unit : toMove) {
//			logger.debug("worker {} going back to mining.", unit);
//			this.units.move((SCV)unit, this.unitInventory.getMineralWorkers());
//		}
//	}
//
//	public void defend(int frame, Position position, List<MobileUnit> hostileUnits) {
//		
//		this.position = position;
//		this.hostileUnits = hostileUnits;
//		
//		run();
//		
//		if (unitInventory.getAllWorkers().isEmpty()) {
//			return;
//		}
//		
//		SCV workerDummy = unitInventory.getAllWorkers().first();
//		
//		if (hostileUnits.size() > 0) {
//			
//			logger.trace("{} enemy units in range of mining workers!", hostileUnits.size());
//			double combinedHitpoints = 0;
//			double combinedAttack = 0;
//			for (MobileUnit hostile : hostileUnits) {
//				
//				combinedHitpoints += hostile.getHitPoints();
//				combinedAttack += (double)hostile.getDamageTo(workerDummy) / hostile.getGroundWeapon().damageCooldown();
//			}
//			double frames = workerDummy.maxHitPoints() / combinedAttack;
//			logger.trace("doing {} damage per frame. kills a worker in {} frames. total HP: {}", combinedAttack, frames, combinedHitpoints);
//			double frames2 = combinedHitpoints / ((double)hostileUnits.get(0).getDamageFrom(workerDummy) / workerDummy.getGroundWeapon().damageCooldown());
//			logger.trace("I kill attackers in {} frames. {} required to hold off the attack.", frames2, (frames2 / frames));
//			
//			// additional force required:
//			int additionalDefense = (int)Math.ceil(frames2 / frames) - this.size();
//			for (int i = 0; i < additionalDefense && this.unitInventory.getMineralWorkers().size() > 0; i++) {
//				
//				Comparator<SCV> comp = (u1, u2) -> Integer.compare(u1.getHitPoints(), u2.getHitPoints());
//				this.unitInventory.getMineralWorkers().stream()
//						.filter(w -> w.getDistance(position) < 192 && w.getHitPoints() >= 35).max(comp)
//						.ifPresent(worker -> addToFighters(frame, worker));
//			}
//			
//			logger.trace("fighting off attackers with {} defenders.", this.getSquad().size());
//		} else {
//			
//			// disband fighting workers
//			if (this.units.size() > 0) {
//				logger.debug("disbanding");
//			}
//			
//			Set<MobileUnit> toMove = new HashSet<MobileUnit>();
//			toMove.addAll(this.units);
//			toMove.stream().filter(w -> w instanceof SCV)
//					.forEach(worker -> this.units.move((SCV)worker, unitInventory.getMineralWorkers()));
//			
//		}
//	}
//	
//	private void addToFighters(int frame, SCV worker) {
//		
//		this.unitInventory.getMineralWorkers().move(worker, this.getSquad());
//		worker.attack(worker.getWeakestUnitInRadius(worker.getGroundWeapon().maxRange() + 32, hostileUnits));
//		logger.debug("frame {}: {} attacks. {} hp left.", frame, worker, worker.getHitPoints());
//	}
}
