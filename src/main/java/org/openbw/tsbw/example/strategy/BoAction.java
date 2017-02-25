package org.openbw.tsbw.example.strategy;

/* default */ interface BoAction {

	boolean execute(int availableMinerals, int availableGas, int availableSupply);
}
