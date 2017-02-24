package org.openbw.tsbw;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbw.tsbw.unit.Unit;

public class Group<T extends Unit> extends TreeSet<T> {

	private static final long serialVersionUID = 4561633085234598587L;
	private static final Logger logger = LogManager.getLogger();
	
	protected String name;
	
	protected Set<GroupListener<T>> listeners;
	
	/* default */ Group(String name) {
		this.name = name;
		this.listeners = new HashSet<GroupListener<T>>();
	}
	
	/* default */ Group(String name, List<T> units) {
		this(name);
		this.addAll(units);
	}
	
	/**
	 * Clears members of the group and listeners.
	 */
	public void clear() {
		listeners.clear();
		super.clear();
	}
	
	public void addListener(GroupListener<T> listener) {
		listeners.add(listener);
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public boolean add(T unit) {
		
		if (unit == null) {
			return false;
		}
		
		if (super.add(unit)) {
			for (GroupListener<T> listener : listeners) {
				
				listener.onAdd(unit);
			}
			return true;
		} else {
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	public boolean remove(Object o) {
		
		if (super.remove(o)) {
			for (GroupListener<T> listener : listeners) {
				
				if (((T)o).exists() || !((T)o).isVisible()) {
					listener.onRemove((T)o);
				} else {
					logger.trace("calling destroyed listeners for {}", o);
					listener.onDestroy((T)o);
				}
			}
			return true;
		} else {
			return false;
		}
	}

	public  <TT extends Unit> void move(TT unit, Group<TT> newGroup) {
		
		this.remove(unit);
		newGroup.add(unit);
	}

	// TODO use index to speed up (replace naive implementation)
	public T getValue(int id) {
		for (T t : this) {
			if (t.getID() == id) {
				return t;
			}
		}
		return null;
	}
	
	// TODO use index to speed up (replace naive implementation)
	public boolean containsKey(int id) {
		
		for (T t : this) {
			if (t.getID() == id) {
				return true;
			}
		}
		return false;
	}
}
