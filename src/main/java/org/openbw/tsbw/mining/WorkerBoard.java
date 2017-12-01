package org.openbw.tsbw.mining;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.openbw.bwapi4j.unit.Refinery;
import org.openbw.tsbw.unit.MineralPatch;
import org.openbw.tsbw.unit.SCV;

import co.paralleluniverse.strands.concurrent.ReentrantLock;

public class WorkerBoard {

	private ConcurrentHashMap<SCV, MineralPatch> patchAssignments;
	private ConcurrentHashMap<SCV, Refinery> gasAssignments;
	private final ReentrantLock lock;
	private boolean token;
	
	WorkerBoard() {

		this.patchAssignments = new ConcurrentHashMap<>();
		this.gasAssignments = new ConcurrentHashMap<>();
		this.lock = new ReentrantLock();
		this.token = false;
	}
	
	Set<SCV> getMineralMiningSCVs() {
		
		return this.patchAssignments.keySet();
	}
	
	void assign(SCV scv, MineralPatch patch) {
		
		this.patchAssignments.put(scv, patch);
		patch.addScv();
		scv.gather(patch);
	}
	
	void assign(SCV scv, Refinery refinery) {
		
		this.gasAssignments.put(scv, refinery);
		scv.gather(refinery);
	}
	
	void removeFromPatch(SCV scv) {
		
		MineralPatch patch = this.patchAssignments.get(scv);
		if (patch != null) {
			
			this.patchAssignments.remove(scv);
			patch.removeScv();
		}
	}
	
	void removeFromGas(SCV scv) {
		
		this.gasAssignments.remove(scv);
	}
	
	boolean reserveToken() {
		
		boolean success;
		
		try {
			this.lock.lock();
			
			if (this.token) {
				success = false;
			} else {
				this.token = true;
				success = true;
			}
			
		} finally {
			this.lock.unlock();
		}
		
		return success;
	}
	
	void releaseToken() {
		
		try {
			this.lock.lock();
			
			this.token = false;
			
		} finally {
			this.lock.unlock();
		}
	}
}
