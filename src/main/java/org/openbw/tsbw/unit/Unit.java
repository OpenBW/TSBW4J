package org.openbw.tsbw.unit;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.openbw.bwapi.BWMap;

import bwapi.Position;
import bwapi.Region;
import bwapi.TilePosition;

public class Unit implements Comparable<Unit> {

	protected bwapi.Unit bwUnit;
	protected BWMap bwMap;
	protected int timeSpotted;
	protected int lastSpotted;
	
	/* default */ Unit(bwapi.Unit bwUnit, BWMap bwMap, int timeSpotted) {
		
		this.bwUnit = bwUnit;
		this.bwMap = bwMap;
		this.timeSpotted = timeSpotted;
		this.lastSpotted = timeSpotted;
	}
	
	protected Unit() {
		
	}
	
	public int getLeft() {
		return this.bwUnit.getLeft();
	}
	public int getRight() {
		return this.bwUnit.getRight();
	}
	public int getTop() {
		return this.bwUnit.getTop();
	}
	public int getBottom() {
		return this.bwUnit.getBottom();
	}
	
	public Position getMiddle(Unit unit) {
	
		int x = this.getPosition().getX();
		int y = this.getPosition().getY();
		
		int dx = unit.getPosition().getX() - x;
		int dy = unit.getPosition().getY() - y;
		
		return new Position(x + dx / 2, y + dy / 2);
	}
	
	public <T extends Unit> T getClosest(Collection<T> group) {
	
		Comparator<T> comp = (u1, u2) -> Integer.compare(this.getDistance(u1), this.getDistance(u2));
		return group.parallelStream().min(comp).get();
	}
	
	public <T extends Unit> List<T> getUnitsInRadius(int radius, Collection<T> group) {
		
		return group.parallelStream().filter(t -> this.getDistance(t) <= radius).collect(Collectors.toList());
	}
	
	public void update(int timeSpotted) {
		this.lastSpotted = timeSpotted;
	}
	
	public int getID() {
		return this.bwUnit.getID();
	}
	
	public int getX() {
		return this.bwUnit.getX();
	}
	
	public int getY() {
		return this.bwUnit.getY();
	}
	
	public int height() {
		return this.bwUnit.getType().height();
	}
	
	public int width() {
		return this.bwUnit.getType().width();
	}
	
	public int tileHeight() {
		return this.bwUnit.getType().tileHeight();
	}
	
	public int tileWidth() {
		return this.bwUnit.getType().tileWidth();
	}
	
	public Region getRegion() {
		return this.bwUnit.getRegion();
	}
	
	public TilePosition getTilePosition() {
		return this.bwUnit.getTilePosition();
	}

	public Position getPosition() {
		return this.bwUnit.getPosition();
	}
	
	public int getDistance(Position target) {
		return this.bwUnit.getDistance(target);
	}
	
	public double getDistance(int x, int y) {
		return this.bwUnit.getDistance(x, y);
	}
	
	public int getDistance(Unit target) {
		return this.bwUnit.getDistance(target.getPosition());
	}
	
	public boolean isVisible() {
		return this.bwUnit.isVisible();
	}
	
	public boolean isSelected() {
		return this.bwUnit.isSelected();
	}
	
	public boolean exists() {
		return this.bwUnit.exists();
	}
	
	@Override
	public int hashCode() {
		return bwUnit.getID();
	}

	@Override
	public boolean equals(Object obj) {
		
		if (obj instanceof Unit) {
			return this.getID() == ((Unit)obj).getID();
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return this.bwUnit.getID() + ":" + this.bwUnit.getType();
	}

	@Override
	public int compareTo(Unit otherUnit) {
		return this.getID() - otherUnit.getID();
	}
}
