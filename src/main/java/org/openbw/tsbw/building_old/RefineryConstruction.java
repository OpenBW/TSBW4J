package org.openbw.tsbw.building_old;

import java.util.Queue;

import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.SCV;
import org.openbw.tsbw.Group;
import org.openbw.tsbw.MapAnalyzer;
import org.openbw.tsbw.UnitInventory;
import org.openbw.tsbw.unit.VespeneGeyser;

public class RefineryConstruction extends DefaultConstruction {

	public RefineryConstruction(MapAnalyzer mapAnalyzer) {
		super(UnitType.Terran_Refinery, mapAnalyzer);
	}

	@Override
	public TilePosition getBuildTile(SCV builder, TilePosition aroundHere, UnitInventory unitInventory, Queue<ConstructionProject> projects) {
		
		Position around = aroundHere.toPosition();
		
		Group<VespeneGeyser> geysers = unitInventory.getVespeneGeysers();
		if (geysers.isEmpty()) {
			return null;
		} else {
			return geysers.stream().min((u1, u2) -> Double.compare(u1.getDistance(around), u2.getDistance(around))).get().getTilePosition();
		}
	}
	
	@Override
	public TilePosition getBuildTile(SCV builder, UnitInventory unitInventory, Queue<ConstructionProject> projects) {
		
		TilePosition aroundHere = builder == null ? unitInventory.getMain().getTilePosition() : builder.getTilePosition();
		return getBuildTile(builder, aroundHere, unitInventory, projects);
	}
}
