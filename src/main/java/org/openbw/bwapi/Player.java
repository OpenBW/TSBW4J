package org.openbw.bwapi;

import org.openbw.tsbw.UnitInventory;

import bwapi.Race;
import bwapi.TechType;
import bwapi.TilePosition;
import bwapi.UpgradeType;

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

	/**
	 * {@link bwapi.Player#getUpgradeLevel(UpgradeType)}
	 */
	public int getUpgradeLevel(UpgradeType type) {
		return this.player.getUpgradeLevel(type);
	}

	/**
	 * {@link bwapi.Player#hasResearched(TechType)}
	 */
	public boolean hasResearched(TechType type) {
		return this.player.hasResearched(type);
	}
	
	/**
	 * {@link bwapi.Player#getStartLocation()}
	 */
	public TilePosition getStartLocation() {
		return player.getStartLocation();
	}

	/**
	 * {@link bwapi.Player#getID()}
	 */
	public int getID() {
		return player.getID();
	}

	/**
	 * {@link bwapi.Player#minerals()}
	 */
	public int minerals() {
		return player.minerals();
	}

	/**
	 * {@link bwapi.Player#gatheredMinerals()}
	 */
	public int gatheredMinerals() {
		return player.gatheredMinerals();
	}

	/**
	 * {@link bwapi.Player#spentMinerals()}
	 */
	public int spentMinerals() {
		return player.spentMinerals();
	}

	/**
	 * {@link bwapi.Player#gas()}
	 */
	public int gas() {
		return player.gas();
	}

	/**
	 * {@link bwapi.Player#gatheredGas()}
	 */
	public int gatheredGas() {
		return player.gatheredGas();
	}

	/**
	 * {@link bwapi.Player#spentGas()}
	 */
	public int spentGas() {
		return player.spentGas();
	}

	/**
	 * {@link bwapi.Player#supplyTotal()}
	 */
	public int supplyTotal() {
		return player.supplyTotal();
	}

	/**
	 * {@link bwapi.Player#supplyUsed()}
	 */
	public int supplyUsed() {
		return player.supplyUsed();
	}

	/**
	 * {@link bwapi.Player#getRace()}
	 */
	public Race getRace() {
		return player.getRace();
	}

	/**
	 * {@link bwapi.Player#getName()}
	 */
	public String getName() {
		return player.getName();
	}
}
