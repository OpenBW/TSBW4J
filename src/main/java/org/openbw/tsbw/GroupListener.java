package org.openbw.tsbw;

import org.openbw.tsbw.unit.Unit;

/**
 * Provides callback methods if a unit gets added to a group, removed from a group, or gets destroyed.
 */
public interface GroupListener<T extends Unit> {

	
	/**
	 * Callback when a unit gets added to a group.
	 * @param added unit
	 */
	public void onAdd(T unit);
	
	/**
	 * Callback when a unit gets removed from a group.
	 * @param removed unit
	 */
	public void onRemove(T unit);
	
	/**
	 * Callback when a unit gets destroyed (and thus is removed from the group).
	 * A unit getting removed because it got destroyed will not trigger onRemove additionally.
	 * @param destroyed unit
	 */
	public void onDestroy(T unit);
}
