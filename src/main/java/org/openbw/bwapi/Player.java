package org.openbw.bwapi;

import org.openbw.tsbw.UnitInventory;

public class Player {

	private bwapi.Player player;
	private UnitInventory unitInventory;
	
	public Player(UnitInventory unitInventory) {
		this.unitInventory = unitInventory;
	}
	
	public void initialize(bwapi.Player player) {
		
		this.player = player;
	}
	
	public UnitInventory getUnitInventory() {
		return this.unitInventory;
	}
	
	public int minerals() {
		return player.minerals();
	}

	public int gatheredMinerals() {
		return player.gatheredMinerals();
	}
	
	public int spentMinerals() {
		return player.spentMinerals();
	}
	
	public int gas() {
		return player.gas();
	}
	
	public int gatheredGas() {
		return player.gatheredGas();
	}
	
	public int spentGas() {
		return player.spentGas();
	}
	
	public int supplyTotal() {
		return player.supplyTotal();
	}
	
	public int supplyUsed() {
		return player.supplyUsed();
	}
}
