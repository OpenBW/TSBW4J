package org.openbw.tsbw.example.mining;

import java.util.HashSet;
import java.util.Set;

import org.openbw.tsbw.unit.Worker;

public class MiningSquad {

	private Set<Worker> workers;
	
	public MiningSquad() {
		
		this.workers = new HashSet<Worker>();
	}
	
	public void add(Worker worker) {
		this.workers.add(worker);
	}
	
	public void remove(Worker worker) {
		this.workers.remove(worker);
	}
	
	public void escape() {
		
	}
	
	public void fight() {
		
	}
}
