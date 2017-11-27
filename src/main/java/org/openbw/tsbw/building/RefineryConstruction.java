package org.openbw.tsbw.building;

import java.util.Queue;

import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.SCV;
import org.openbw.tsbw.Group;
import org.openbw.tsbw.MapAnalyzer;
import org.openbw.tsbw.UnitInventory;
import org.openbw.tsbw.unit.VespeneGeyser;

public class RefineryConstruction extends ConstructionProvider {

	public RefineryConstruction() {
		super(UnitType.Terran_Refinery);
	}

	@Override
	public TilePosition getBuildTile(UnitInventory unitInventory, MapAnalyzer mapAnalyzer, SCV builder, Queue<Project> projects, TilePosition aroundHere) {
		
		Position around = aroundHere.toPosition();
		
		Group<VespeneGeyser> geysers = unitInventory.getVespeneGeysers();
		if (geysers.isEmpty()) {
			return null;
		} else {
			return geysers.stream().min((u1, u2) -> Double.compare(u1.getDistance(around), u2.getDistance(around))).get().getTilePosition();
		}
	}
}
